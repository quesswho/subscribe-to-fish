// Runs Java bots inside an embedded JVM: the official instrumenter counts their bytecodes, each robot is a
// continuation, and NativeRC's natives call straight into the C++ World.
#pragma once

#include <jni.h>

#include <stdexcept>
#include <string>
#include <vector>

#include "../game/world.hpp"

namespace fish {

struct HostError : std::runtime_error {
    using std::runtime_error::runtime_error;
};

struct JvmOptions {
    std::string classpath;          // fish host classes + battlecode engine jar + its dependencies
    std::vector<std::string> extra;  // more -X / -D options
    std::string heap = "1g";
};

struct Bot {
    std::string dir;  // folder holding the compiled package (e.g. build/classes)
    std::string package;
};

struct TeamStats {
    long long timeNanos = 0;
    int uncaughtExceptions = 0;
    long long turns = 0, overruns = 0, bytecodes = 0;  // turns played, and those that ran out of bytecode
};

class JvmHost : public BotHost {
public:
    explicit JvmHost(const JvmOptions& opts);
    ~JvmHost() override;

    // Call before constructing the World (it spawns the initial bodies), then attach it.
    // outA/outB are files for the teams' System.out ("" discards it).
    void beginGame(const GameMap& map, const Bot& a, const Bot& b, const std::string& outA, const std::string& outB,
                   const std::string& props = "");
    void attach(World& world);
    void endGame();
    TeamStats teamStats(int team);
    long long pinnedPauses();
    // System.exit: runs JVM shutdown hooks (e.g. a Flight Recorder dump) and ends the process.
    [[noreturn]] void exitProcess(int code);

    void spawned(Robot& r) override;
    void killed(Robot& r) override;
    void runTurn(Robot& r) override;
    bool terminated(Robot& r) override;
    int bytecodesUsed(Robot& r) override;

private:
    long long turns_[2] = {0, 0}, overruns_[2] = {0, 0}, bytecodes_[2] = {0, 0};
    JavaVM* vm_ = nullptr;
    JNIEnv* env_ = nullptr;
    jclass host_ = nullptr;
    jmethodID beginGame_, endGame_, spawn_, step_, killed_, teamTime_, teamExceptions_, pinned_;
    World* world_ = nullptr;

    void rethrow(const char* what);
};

}  // namespace fish
