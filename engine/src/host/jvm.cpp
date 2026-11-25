#include "jvm.hpp"

#include <chrono>
#include <climits>
#include <cstdlib>
#include <cstring>
#include <string>

namespace fish {

namespace {

World* g_world = nullptr;
int* g_buf = nullptr;
std::string g_engineError;
std::vector<Robot*> g_robots;
std::vector<Loc> g_locs;

constexpr int ROBOT_RECORD = 10;
// The shared arrays are mirrored at the end of the buffer so bots read them without a JNI call (NativeRC.SHARED).
constexpr int SHARED_MIRROR = 65536 - 2 * C::SHARED_ARRAY_SIZE;
constexpr int MAPINFO_RECORD = 7;

Robot& R(jlong h) { return *reinterpret_cast<Robot*>(h); }
Loc L(jint x, jint y) { return {x, y}; }
jint fail(Err e) { return -jint(e); }
jint result(Err e, jint value) { return e ? fail(e) : value; }
jint pack(Loc l) { return jint((uint32_t(l.x) << 16) | (uint32_t(l.y) & 0xffff)); }

void writeRobot(const Robot& r, int at) {
    int* b = g_buf + at;
    b[0] = r.id;
    b[1] = r.team;
    b[2] = r.type;
    b[3] = r.health;
    b[4] = r.loc.x;
    b[5] = r.loc.y;
    b[6] = r.dir == DIR_NULL ? CENTER : r.dir;
    b[7] = r.chirality;
    b[8] = r.cheese;
    b[9] = r.carrying ? r.carrying->id : 0;
    if (r.carrying) writeRobot(*r.carrying, at + ROBOT_RECORD);
}

// Writes MapInfo for l at `at`; a flying robot's record goes at *tail.
void writeMapInfo(const Robot& self, Loc l, int at, int* tail) {
    World& w = *g_world;
    int* b = g_buf + at;
    const Trap* trap = w.trapAt(l, self.team);
    Robot* fly = w.flyingAt(l);
    b[0] = l.x;
    b[1] = l.y;
    b[2] = (w.passable(l) ? 1 : 0) | (w.wall(l) ? 2 : 0) | (w.dirt(l) ? 4 : 0) | (w.hasMine(l) ? 8 : 0);
    b[3] = w.cheeseAt(l);
    b[4] = trap ? trap->type : TRAP_NONE;
    b[5] = fly ? fly->id : 0;
    b[6] = 0;
    if (fly) {
        b[6] = *tail;
        writeRobot(*fly, *tail);
        *tail += 2 * ROBOT_RECORD;
    }
}

Dir D(jint d) { return d < 0 || d > 8 ? DIR_NULL : Dir(d); }

// Every native runs World code that may throw the official engine's unchecked exceptions; they must not unwind
// through JNI frames.
template <class F>
jint guard(F&& f) {
    try {
        return f();
    } catch (const CheeseError&) {
        return fail(E_CHEESE_ERROR);
    } catch (const std::exception& e) {
        g_engineError = e.what();
        return fail(E_ENGINE_ERROR);
    }
}

#define NATIVE(name, ...) jint JNICALL name(JNIEnv*, jclass, ##__VA_ARGS__)

NATIVE(nRound) { return g_world->round(); }
NATIVE(nCoop) { return g_world->cooperation(); }
NATIVE(nBackstabber) { return g_world->backstabberOrNeg(); }
NATIVE(nTrapCount, jlong h, jint type) { return g_world->trapCount(TrapType(type), R(h).team); }
NATIVE(nLoc, jlong h) { return pack(R(h).loc); }
NATIVE(nParts, jlong h) {
    Loc parts[9];
    int n;
    g_world->partLocations(R(h), parts, n);
    for (int i = 0; i < n; i++) g_buf[i] = pack(parts[i]);
    return n;
}
NATIVE(nDir, jlong h) { return R(h).dir == DIR_NULL ? CENTER : R(h).dir; }
NATIVE(nHealth, jlong h) { return R(h).health; }
NATIVE(nRaw, jlong h) { return R(h).cheese; }
NATIVE(nGlobal, jlong h) { return g_world->teamCheese(R(h).team); }
NATIVE(nDirt, jlong h) { return g_world->teamDirt(R(h).team); }
NATIVE(nType, jlong h) { return R(h).type; }
NATIVE(nCarrying, jlong h) {
    if (!R(h).carrying) return 0;
    writeRobot(*R(h).carrying, 0);
    return 1;
}
NATIVE(nState, jlong h, jint which) { return which == 0 ? R(h).beingThrown() : R(h).grabbedBy != nullptr; }
NATIVE(nCanSense, jlong h, jint x, jint y) { return g_world->assertCanSense(R(h), L(x, y)) == E_OK; }
NATIVE(nOccupied, jlong h, jint x, jint y) {
    Err e = g_world->assertCanSense(R(h), L(x, y), World::SEE_ROBOTS);
    return result(e, e ? 0 : g_world->robotAt(L(x, y)) != nullptr);
}
NATIVE(nRobotAt, jlong h, jint x, jint y) {
    if (Err e = g_world->assertCanSense(R(h), L(x, y), World::SEE_ROBOTS)) return fail(e);
    Robot* o = g_world->robotAt(L(x, y));
    if (!o) return 0;
    writeRobot(*o, 0);
    return 1;
}
NATIVE(nSenseRobot, jlong h, jint id) {
    Robot* o = nullptr;
    if (g_world->senseRobotById(R(h), id, &o) != E_OK) return 0;
    writeRobot(*o, 0);
    return 1;
}
NATIVE(nNearby, jlong h, jint x, jint y, jint r, jint team) {
    if (Err e = g_world->senseNearbyRobots(R(h), L(x, y), r, team, g_robots)) return fail(e);
    for (size_t i = 0; i < g_robots.size(); i++) writeRobot(*g_robots[i], int(i) * 2 * ROBOT_RECORD);
    return jint(g_robots.size());
}
NATIVE(nPassable, jlong h, jint x, jint y) {
    if (Err e = g_world->assertCanSense(R(h), L(x, y), World::SEE_MAP)) return fail(e);
    return g_world->passable(L(x, y));
}
NATIVE(nMapInfo, jlong h, jint x, jint y) {
    if (Err e = g_world->assertCanSense(R(h), L(x, y), World::SEE_MAP)) return fail(e);
    int tail = MAPINFO_RECORD;
    writeMapInfo(R(h), L(x, y), 0, &tail);
    return 0;
}
NATIVE(nMapInfos, jlong h, jint x, jint y, jint r) {
    if (Err e = g_world->senseNearbyMapInfos(R(h), L(x, y), r, g_locs)) return fail(e);
    int tail = int(g_locs.size()) * MAPINFO_RECORD;
    for (size_t i = 0; i < g_locs.size(); i++) writeMapInfo(R(h), g_locs[i], int(i) * MAPINFO_RECORD, &tail);
    return jint(g_locs.size());
}
NATIVE(nLocsWithin, jlong h, jint x, jint y, jint r) {
    if (Err e = g_world->allLocationsWithinRadiusSquared(R(h), L(x, y), r, g_locs)) return fail(e);
    for (size_t i = 0; i < g_locs.size(); i++) g_buf[i] = pack(g_locs[i]);
    return jint(g_locs.size());
}
NATIVE(nCooldown, jlong h, jint which) {
    const Robot& r = R(h);
    return which == 0 ? r.actionCd : which == 1 ? r.moveCd : r.turnCd;
}
NATIVE(nReady, jlong h, jint which) {
    const Robot& r = R(h);
    bool cd = which == 0 ? r.canAct() : which == 1 ? r.canMoveCd() : r.canTurnCd();
    return cd && !r.beingThrown() && !r.grabbedBy;
}
NATIVE(nMove, jlong h, jint dir, jint apply) {
    return guard([&] { return fail(apply ? g_world->move(R(h), D(dir)) : g_world->canMove(R(h), D(dir))); });
}
NATIVE(nTurn, jlong h, jint dir, jint apply) {
    return guard([&] {
        if (dir == -2) return fail(g_world->canTurnNow(R(h)));
        return fail(apply ? g_world->turn(R(h), D(dir)) : g_world->canTurn(R(h), D(dir)));
    });
}
NATIVE(nRatCost, jlong h) { return g_world->currentRatCost(R(h)); }
NATIVE(nBuildRat, jlong h, jint x, jint y, jint apply) {
    return guard([&] { return fail(apply ? g_world->buildRat(R(h), L(x, y)) : g_world->canBuildRat(R(h), L(x, y))); });
}
NATIVE(nBecomeKing, jlong h, jint apply) {
    return guard([&] { return fail(apply ? g_world->becomeRatKing(R(h)) : g_world->canBecomeRatKing(R(h))); });
}
NATIVE(nDirtAction, jlong h, jint x, jint y, jint place, jint apply) {
    return guard([&] {
        Loc l = L(x, y);
        if (place) return fail(apply ? g_world->placeDirt(R(h), l) : g_world->canPlaceDirt(R(h), l));
        return fail(apply ? g_world->removeDirt(R(h), l) : g_world->canRemoveDirt(R(h), l));
    });
}
NATIVE(nTrapAction, jlong h, jint x, jint y, jint type, jint place, jint apply) {
    return guard([&] {
        Loc l = L(x, y);
        TrapType t = TrapType(type);
        if (place) return fail(apply ? g_world->placeTrap(R(h), l, t) : g_world->canPlaceTrap(R(h), l, t));
        return fail(apply ? g_world->removeTrap(R(h), l, t) : g_world->canRemoveTrap(R(h), l, t));
    });
}
NATIVE(nPickUp, jlong h, jint x, jint y, jint amount, jint apply) {
    return guard([&] {
        return fail(apply ? g_world->pickUpCheese(R(h), L(x, y), amount) : g_world->canPickUpCheese(R(h), L(x, y)));
    });
}
NATIVE(nAttack, jlong h, jint x, jint y, jint cheese, jint withCheese, jint apply) {
    return guard([&] {
        if (apply) return fail(g_world->attack(R(h), L(x, y), cheese, withCheese != 0));
        return fail(g_world->canAttack(R(h), L(x, y), withCheese ? cheese : 0));
    });
}
NATIVE(nSqueak, jlong h, jint content) { return g_world->squeak(R(h), content); }
NATIVE(nSqueaks, jlong h, jint round) {
    int n = 0;
    for (const Message& m : R(h).messages) {
        if (round != -1 && m.round != round) continue;
        int* b = g_buf + 5 * n++;
        b[0] = m.bytes;
        b[1] = m.senderId;
        b[2] = m.round;
        b[3] = m.source.x;
        b[4] = m.source.y;
    }
    return n;
}
NATIVE(nWriteShared, jlong h, jint index, jint value) {
    Err e = g_world->writeSharedArray(R(h), index, value);
    if (e == E_OK) g_buf[SHARED_MIRROR + R(h).team * C::SHARED_ARRAY_SIZE + index] = value;
    return fail(e);
}
NATIVE(nReadShared, jlong h, jint index) { return g_world->readSharedArray(R(h), index); }
NATIVE(nTransfer, jlong h, jint x, jint y, jint amount, jint apply) {
    return guard([&] {
        Loc l = L(x, y);
        return fail(apply ? g_world->transferCheese(R(h), l, amount) : g_world->canTransferCheese(R(h), l, amount));
    });
}
NATIVE(nThrow, jlong h, jint apply) {
    return guard([&] { return fail(apply ? g_world->throwRat(R(h)) : g_world->canThrowRat(R(h))); });
}
NATIVE(nDrop, jlong h, jint dir, jint apply) {
    return guard([&] { return fail(apply ? g_world->dropRat(R(h), D(dir)) : g_world->canDropRat(R(h), D(dir))); });
}
NATIVE(nCarry, jlong h, jint x, jint y, jint apply) {
    return guard([&] { return fail(apply ? g_world->carryRat(R(h), L(x, y)) : g_world->canCarryRat(R(h), L(x, y))); });
}
void JNICALL nResign(JNIEnv*, jclass, jlong h) {
    guard([&] {
        g_world->resign(R(h));
        return 0;
    });
}
jstring JNICALL nEngineError(JNIEnv* env, jclass) { return env->NewStringUTF(g_engineError.c_str()); }

#undef NATIVE

#define N(name, sig) {const_cast<char*>(#name), const_cast<char*>(sig), reinterpret_cast<void*>(&name)}
const JNINativeMethod NATIVES[] = {
    N(nRound, "()I"),
    N(nCoop, "()I"),
    N(nBackstabber, "()I"),
    N(nTrapCount, "(JI)I"),
    N(nLoc, "(J)I"),
    N(nParts, "(J)I"),
    N(nDir, "(J)I"),
    N(nHealth, "(J)I"),
    N(nRaw, "(J)I"),
    N(nGlobal, "(J)I"),
    N(nDirt, "(J)I"),
    N(nType, "(J)I"),
    N(nCarrying, "(J)I"),
    N(nState, "(JI)I"),
    N(nCanSense, "(JII)I"),
    N(nOccupied, "(JII)I"),
    N(nRobotAt, "(JII)I"),
    N(nSenseRobot, "(JI)I"),
    N(nNearby, "(JIIII)I"),
    N(nPassable, "(JII)I"),
    N(nMapInfo, "(JII)I"),
    N(nMapInfos, "(JIII)I"),
    N(nLocsWithin, "(JIII)I"),
    N(nCooldown, "(JI)I"),
    N(nReady, "(JI)I"),
    N(nMove, "(JII)I"),
    N(nTurn, "(JII)I"),
    N(nRatCost, "(J)I"),
    N(nBuildRat, "(JIII)I"),
    N(nBecomeKing, "(JI)I"),
    N(nDirtAction, "(JIIII)I"),
    N(nTrapAction, "(JIIIII)I"),
    N(nPickUp, "(JIIII)I"),
    N(nAttack, "(JIIIII)I"),
    N(nSqueak, "(JI)I"),
    N(nSqueaks, "(JI)I"),
    N(nWriteShared, "(JII)I"),
    N(nReadShared, "(JI)I"),
    N(nTransfer, "(JIIII)I"),
    N(nThrow, "(JI)I"),
    N(nDrop, "(JII)I"),
    N(nCarry, "(JIII)I"),
    N(nResign, "(J)V"),
    N(nEngineError, "()Ljava/lang/String;"),
};
#undef N

const char* ERROR_TABLE[] = {
#define FISH_ERR_ROW(name, type, msg) #name "|" #type "|" msg,
    FISH_ERRORS(FISH_ERR_ROW)
#undef FISH_ERR_ROW
};

}  // namespace

JvmHost::JvmHost(const JvmOptions& opts) {
    std::vector<std::string> args = {
        "-Djava.class.path=" + opts.classpath,
        "-Xmx" + opts.heap,
        "--add-exports=java.base/jdk.internal.vm=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.math=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.util=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED",
        "--add-opens=java.base/sun.security.action=ALL-UNNAMED",
        "-XX:+UseSerialGC",
        "-XX:-DontCompileHugeMethods",
        "-Xshare:auto",
    };
    for (auto& e : opts.extra) args.push_back(e);
    std::vector<JavaVMOption> options(args.size());
    for (size_t i = 0; i < args.size(); i++) options[i].optionString = const_cast<char*>(args[i].c_str());
    JavaVMInitArgs init{};
    init.version = JNI_VERSION_21;
    init.nOptions = jint(options.size());
    init.options = options.data();
    init.ignoreUnrecognized = JNI_FALSE;
    if (JNI_CreateJavaVM(&vm_, reinterpret_cast<void**>(&env_), &init) != JNI_OK)
        throw HostError("cannot start the JVM");

    jclass rc = env_->FindClass("fish/NativeRC");
    if (!rc) rethrow("cannot find fish.NativeRC (is the host built?)");
    if (env_->RegisterNatives(rc, NATIVES, sizeof(NATIVES) / sizeof(NATIVES[0])) != 0)
        rethrow("cannot register natives");

    jclass host = env_->FindClass("fish/Host");
    if (!host) rethrow("cannot find fish.Host");
    host_ = static_cast<jclass>(env_->NewGlobalRef(host));
    auto method = [&](const char* name, const char* sig) {
        jmethodID m = env_->GetStaticMethodID(host_, name, sig);
        if (!m) rethrow(name);
        return m;
    };
    jmethodID init_ = method("init", "([Ljava/lang/String;)V");
    beginGame_ = method("beginGame",
                        "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;"
                        "Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    endGame_ = method("endGame", "()V");
    spawn_ = method("spawn", "(JII)V");
    step_ = method("step", "(III)J");
    killed_ = method("killed", "(I)V");
    teamTime_ = method("teamTimeNanos", "(I)J");
    teamExceptions_ = method("teamExceptions", "(I)I");
    pinned_ = method("pinnedPauses", "()J");
    jmethodID buffer = method("buffer", "()Ljava/nio/ByteBuffer;");

    int rows = sizeof(ERROR_TABLE) / sizeof(ERROR_TABLE[0]);
    jobjectArray table = env_->NewObjectArray(rows, env_->FindClass("java/lang/String"), nullptr);
    for (int i = 0; i < rows; i++) {
        jstring s = env_->NewStringUTF(ERROR_TABLE[i]);
        env_->SetObjectArrayElement(table, i, s);
        env_->DeleteLocalRef(s);
    }
    env_->CallStaticVoidMethod(host_, init_, table);
    rethrow("Host.init");
    jobject buf = env_->CallStaticObjectMethod(host_, buffer);
    rethrow("Host.buffer");
    g_buf = static_cast<int*>(env_->GetDirectBufferAddress(buf));
}

JvmHost::~JvmHost() {
    // The JVM cannot be restarted in-process and DestroyJavaVM waits for daemon threads; leave it to exit.
}

void JvmHost::rethrow(const char* what) {
    if (!env_->ExceptionCheck()) return;
    jthrowable t = env_->ExceptionOccurred();
    env_->ExceptionDescribe();
    env_->ExceptionClear();
    env_->DeleteLocalRef(t);
    throw HostError(std::string("JVM error in ") + what);
}

void JvmHost::beginGame(const GameMap& map, const Bot& a, const Bot& b, const std::string& outA,
                        const std::string& outB, const std::string& props) {
    turns_[0] = turns_[1] = overruns_[0] = overruns_[1] = bytecodes_[0] = bytecodes_[1] = 0;
    env_->PushLocalFrame(16);
    auto s = [&](const std::string& v) { return env_->NewStringUTF(v.c_str()); };
    env_->CallStaticVoidMethod(host_, beginGame_, jint(map.width), jint(map.height), s(a.dir), s(a.package),
                               s(b.dir), s(b.package), s(outA), s(outB), s(props));
    env_->PopLocalFrame(nullptr);
    rethrow("Host.beginGame");
}

void JvmHost::attach(World& world) {
    world_ = &world;
    g_world = &world;
    std::memset(g_buf + SHARED_MIRROR, 0, sizeof(int) * 2 * C::SHARED_ARRAY_SIZE);
}

void JvmHost::endGame() {
    env_->CallStaticVoidMethod(host_, endGame_);
    rethrow("Host.endGame");
    g_world = nullptr;
    world_ = nullptr;
}

TeamStats JvmHost::teamStats(int team) {
    TeamStats s;
    s.timeNanos = env_->CallStaticLongMethod(host_, teamTime_, jint(team));
    s.uncaughtExceptions = env_->CallStaticIntMethod(host_, teamExceptions_, jint(team));
    rethrow("Host.teamStats");
    s.turns = turns_[team];
    s.overruns = overruns_[team];
    s.bytecodes = bytecodes_[team];
    return s;
}

void JvmHost::exitProcess(int code) {
    if (std::getenv("FISH_CLEAN_EXIT")) {
        jclass sys = env_->FindClass("java/lang/System");
        env_->CallStaticVoidMethod(sys, env_->GetStaticMethodID(sys, "exit", "(I)V"), jint(code));
    }
    std::_Exit(code);
}

long long JvmHost::pinnedPauses() { return env_->CallStaticLongMethod(host_, pinned_); }

void JvmHost::spawned(Robot& r) {
    env_->CallStaticVoidMethod(host_, spawn_, jlong(reinterpret_cast<intptr_t>(&r)), jint(r.id), jint(r.team));
    rethrow("Host.spawn");
}

void JvmHost::killed(Robot& r) {
    r.hostTerminated = true;
    r.hostUsed = 0;
    env_->CallStaticVoidMethod(host_, killed_, jint(r.id));
    rethrow("Host.killed");
}

void JvmHost::runTurn(Robot& r) {
    if (r.hostTerminated) return;
    jlong res = env_->CallStaticLongMethod(host_, step_, jint(r.id), jint(r.bytecodeLimit), jint(world_->round()));
    rethrow("Host.step");
    if (!r.alive) return;  // killed during its own turn: its sandbox is gone
    r.hostUsed = int(uint32_t(res));
    r.hostTerminated = (res >> 32) != 0;
    turns_[r.team]++;
    bytecodes_[r.team] += r.hostUsed;
    if (!r.hostTerminated && r.hostUsed >= r.bytecodeLimit) overruns_[r.team]++;
    if (teamStats(r.team).timeNanos > C::MAX_TEAM_EXECUTION_TIME_NS) world_->resign(r);
}

bool JvmHost::terminated(Robot& r) { return r.hostTerminated; }

int JvmHost::bytecodesUsed(Robot& r) { return r.hostUsed; }

}  // namespace fish
