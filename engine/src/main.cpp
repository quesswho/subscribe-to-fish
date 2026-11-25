// fish: the Battlecode 2026 match runner.
//
//   fish run MAP --a DIR:PKG --b DIR:PKG [--seed N] [--trace] [--out-a F] [--out-b F]
//   fish worker          jobs on stdin, one result line (JSON) per job on stdout; keeps the JVM warm
//   fish validate MAP..  checks LiveMap's guarantees
//
// Environment: FISH_CLASSPATH (else classpath.txt next to the binary), FISH_JVM_OPTS, FISH_HEAP.
#include <unistd.h>

#include <algorithm>
#include <chrono>
#include <cstdio>
#include <cstring>
#include <fstream>
#include <iostream>
#include <map>
#include <sstream>
#include <string>

#include "game/map.hpp"
#include "game/world.hpp"
#include "host/jvm.hpp"

using namespace fish;

namespace {

std::string exeDir() {
    char buf[4096];
    ssize_t n = readlink("/proc/self/exe", buf, sizeof(buf) - 1);
    if (n <= 0) return ".";
    buf[n] = 0;
    std::string p(buf);
    return p.substr(0, p.rfind('/'));
}

JvmOptions jvmOptions() {
    JvmOptions o;
    if (const char* cp = std::getenv("FISH_CLASSPATH")) {
        o.classpath = cp;
    } else {
        std::ifstream in(exeDir() + "/classpath.txt");
        std::getline(in, o.classpath);
    }
    if (o.classpath.empty()) throw HostError("no classpath: build with tools/build or set FISH_CLASSPATH");
    if (const char* opts = std::getenv("FISH_JVM_OPTS")) {
        std::istringstream ss(opts);
        std::string w;
        while (ss >> w) o.extra.push_back(w);
    }
    if (const char* heap = std::getenv("FISH_HEAP")) o.heap = heap;
    return o;
}

Bot parseBot(const std::string& s) {
    auto colon = s.rfind(':');
    if (colon == std::string::npos) throw std::runtime_error("bot must be DIR:PACKAGE, got " + s);
    return {s.substr(0, colon), s.substr(colon + 1)};
}

struct Job {
    std::string id, map, outA, outB;
    Bot a, b;
    bool hasSeed = false;
    int seed = 0;
    bool trace = false;
    int dumpRound = -1;
    // Testing oracle: teams whose rats sense a full circle, "A", "B" or "AB", optionally followed by ":robots", ":map" or
    // ":team" (robots any allied rat sees)
    std::string xray;
    int timeline = 0;  // every this many rounds, record both teams' state in the result
    std::string props;  // bc.testing properties for the bots, "A.name=value;B.name=value" (runtime tuning)
};

std::string jsonEscape(const std::string& s) {
    std::string o;
    for (char c : s) {
        if (c == '"' || c == '\\') o += '\\', o += c;
        else if (c == '\n') o += "\\n";
        else if (uint8_t(c) < 0x20) o += ' ';
        else o += c;
    }
    return o;
}

std::map<std::string, GameMap> mapCache;

const GameMap& getMap(const std::string& path) {
    auto it = mapCache.find(path);
    if (it != mapCache.end()) return it->second;
    GameMap m = loadMap(path);
    return mapCache.emplace(path, std::move(m)).first->second;
}

std::string play(JvmHost& host, const Job& job) {
    auto start = std::chrono::steady_clock::now();
    std::ostringstream out;
    out << "{\"id\":\"" << jsonEscape(job.id) << "\"";
    try {
        const GameMap& map = getMap(job.map);
        std::string invalid = validateMap(map);
        if (!invalid.empty()) throw std::runtime_error("invalid map " + map.name + ": " + invalid);
        int seed = job.hasSeed ? job.seed : map.seed;
        host.beginGame(map, job.a, job.b, job.outA, job.outB, job.props);
        struct EndGame {
            JvmHost& h;
            ~EndGame() { h.endGame(); }
        } guard{host};
        World world(map, seed, &host);
        {
            auto colon = job.xray.find(':');
            std::string teams = job.xray.substr(0, colon), kind = colon == std::string::npos ? "" : job.xray.substr(colon + 1);
            int what = kind == "robots" ? World::SEE_ROBOTS : kind == "map" ? World::SEE_MAP : kind == "team" ? World::SEE_TEAM
                     : World::SEE_ALL;
            if (teams.find('A') != std::string::npos) world.setXray(TEAM_A, what);
            if (teams.find('B') != std::string::npos) world.setXray(TEAM_B, what);
        }
        host.attach(world);
        std::string timeline;
        auto sample = [&] {
            if (!timeline.empty()) timeline += ",";
            timeline += "[" + std::to_string(world.round());
            for (Team t : {TEAM_A, TEAM_B})
                timeline += "," + std::to_string(world.numBabyRats(t)) + "," + std::to_string(world.numRatKings(t)) + "," +
                            std::to_string(world.teamCheese(t)) + "," + std::to_string(world.kingHealth(t)) + "," +
                            std::to_string(world.cheeseTransferred(t)) + "," + std::to_string(world.damageToCats(t)) + "," +
                            std::to_string(world.trapCount(RAT_TRAP, t));
            timeline += "]";
        };
        while (world.runRound()) {
            if (job.timeline > 0 && world.round() % job.timeline == 0) sample();
            if (job.trace) std::fprintf(stderr, "R %d %016llx\n", world.round(), (unsigned long long)world.stateHash());
            if (world.round() == job.dumpRound) std::fprintf(stderr, "%s", world.stateDump().c_str());
        }
        if (job.trace) std::fprintf(stderr, "R %d %016llx\n", world.round(), (unsigned long long)world.stateHash());
        TeamStats sa = host.teamStats(0), sb = host.teamStats(1);
        double secs = std::chrono::duration<double>(std::chrono::steady_clock::now() - start).count();
        out << ",\"map\":\"" << jsonEscape(map.name) << "\",\"seed\":" << seed << ",\"winner\":"
            << (world.hasWinner() ? (world.winner() == TEAM_A ? "\"A\"" : "\"B\"") : "null") << ",\"reason\":\""
            << winReasonName(world.winReason()) << "\",\"round\":" << world.round() << ",\"points\":["
            << world.points(TEAM_A) << "," << world.points(TEAM_B) << "],\"kings\":[" << world.numRatKings(TEAM_A)
            << "," << world.numRatKings(TEAM_B) << "],\"cheese\":[" << world.teamCheese(TEAM_A) << ","
            << world.teamCheese(TEAM_B) << "],\"transferred\":[" << world.cheeseTransferred(TEAM_A) << ","
            << world.cheeseTransferred(TEAM_B) << "],\"catDamage\":[" << world.damageToCats(TEAM_A) << ","
            << world.damageToCats(TEAM_B) << "],\"exceptions\":[" << sa.uncaughtExceptions << ","
            << sb.uncaughtExceptions << "],\"botSeconds\":[" << sa.timeNanos / 1e9 << "," << sb.timeNanos / 1e9
            << "],\"turns\":[" << sa.turns << "," << sb.turns << "],\"overruns\":[" << sa.overruns << ","
            << sb.overruns << "],\"bytecodes\":[" << sa.bytecodes << "," << sb.bytecodes << "],\"pinned\":" << host.pinnedPauses() << ",\"seconds\":" << secs;
        if (job.timeline > 0) out << ",\"timeline\":[" << timeline << "]";
        auto bySource = [&](const char* name, auto get) {
            out << ",\"" << name << "\":{";
            for (int k = 0; k < World::SRC_COUNT; k++)
                out << (k ? "," : "") << "\"" << World::SOURCE_NAMES[k] << "\":[" << get(TEAM_A, k) << "," << get(TEAM_B, k) << "]";
            out << "}";
        };
        bySource("kingDamage", [&](Team t, int k) { return world.kingDamage(t, k); });
        bySource("kingKilledBy", [&](Team t, int k) { return world.kingKills(t, k); });
        bySource("ratKilledBy", [&](Team t, int k) { return world.ratKills(t, k); });
        if (!world.engineError().empty()) out << ",\"error\":\"" << jsonEscape(world.engineError()) << "\"";
    } catch (const std::exception& e) {
        out << ",\"error\":\"" << jsonEscape(e.what()) << "\"";
    }
    out << "}";
    return out.str();
}

// Worker job line: tab separated key=value pairs (id, map, a, b, seed, outA, outB).
Job parseJobLine(const std::string& line) {
    Job job;
    std::istringstream ss(line);
    std::string field;
    while (std::getline(ss, field, '\t')) {
        auto eq = field.find('=');
        if (eq == std::string::npos) continue;
        std::string k = field.substr(0, eq), v = field.substr(eq + 1);
        if (k == "id") job.id = v;
        else if (k == "map") job.map = v;
        else if (k == "a") job.a = parseBot(v);
        else if (k == "b") job.b = parseBot(v);
        else if (k == "seed") job.hasSeed = true, job.seed = std::stoi(v);
        else if (k == "outA") job.outA = v;
        else if (k == "outB") job.outB = v;
        else if (k == "xray") job.xray = v;
        else if (k == "timeline") job.timeline = std::stoi(v);
        else if (k == "props") job.props = v;
    }
    return job;
}

int usage() {
    std::fprintf(stderr,
                 "usage:\n"
                 "  fish run MAP --a DIR:PKG --b DIR:PKG [--seed N] [--trace] [--dump ROUND] [--out-a F] [--out-b F] [--xray A|B|AB[:robots|:map|:team]]\n"
                 "           [--timeline N]  (every N rounds: [round, then per team babies, kings, cheese,\n"
                 "            king hp, transferred, cat damage, rat traps])\n"
                 "  fish worker\n"
                 "  fish validate MAP...\n");
    return 2;
}

}  // namespace

int main(int argc, char** argv) {
    if (argc < 2) return usage();
    std::string cmd = argv[1];
    try {
        if (cmd == "validate") {
            int bad = 0;
            for (int i = 2; i < argc; i++) {
                GameMap m = loadMap(argv[i]);
                std::string why = validateMap(m);
                std::printf("%s %s %dx%d sym=%d cats=%zu mines=%d %s\n", why.empty() ? "ok " : "BAD", argv[i],
                            m.width, m.height, int(m.symmetry), m.cats.size(),
                            int(std::count(m.mines.begin(), m.mines.end(), 1)), why.c_str());
                bad += !why.empty();
            }
            return bad ? 1 : 0;
        }
        if (cmd == "run") {
            Job job;
            job.id = "run";
            for (int i = 2; i < argc; i++) {
                std::string a = argv[i];
                auto next = [&]() -> std::string {
                    if (i + 1 >= argc) throw std::runtime_error("missing value for " + a);
                    return argv[++i];
                };
                if (a == "--a") job.a = parseBot(next());
                else if (a == "--b") job.b = parseBot(next());
                else if (a == "--seed") job.hasSeed = true, job.seed = std::stoi(next());
                else if (a == "--trace") job.trace = true;
                else if (a == "--dump") job.dumpRound = std::stoi(next());
                else if (a == "--out-a") job.outA = next();
                else if (a == "--out-b") job.outB = next();
                else if (a == "--xray") job.xray = next();
                else if (a == "--timeline") job.timeline = std::stoi(next());
                else if (a == "--props") job.props = next();
                else if (job.map.empty() && a[0] != '-') job.map = a;
                else return usage();
            }
            if (job.map.empty() || job.a.dir.empty() || job.b.dir.empty()) return usage();
            JvmHost host(jvmOptions());
            std::printf("%s\n", play(host, job).c_str());
            std::fflush(stdout);
            host.exitProcess(0);
        }
        if (cmd == "worker") {
            JvmHost host(jvmOptions());
            std::printf("{\"ready\":true}\n");
            std::fflush(stdout);
            std::string line;
            while (std::getline(std::cin, line)) {
                if (line.empty()) continue;
                std::printf("%s\n", play(host, parseJobLine(line)).c_str());
                std::fflush(stdout);
            }
            host.exitProcess(0);
        }
    } catch (const std::exception& e) {
        std::fprintf(stderr, "fish: %s\n", e.what());
        return 1;
    }
    return usage();
}
