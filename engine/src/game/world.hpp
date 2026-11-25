// The Battlecode 2026 game rules: a port of battlecode.world.{GameWorld, InternalRobot, RobotControllerImpl,
// TeamInfo, ObjectInfo, IDGenerator} that plays move for move like engine 1.2.6.
#pragma once

#include <cstdint>
#include <deque>
#include <memory>
#include <string>
#include <vector>

#include "errors.hpp"
#include "jrandom.hpp"
#include "map.hpp"
#include "types.hpp"

namespace fish {

// A null MapLocation argument from Java; it throws NullPointerException where the official engine would.
constexpr int NULL_COORD = -2147483647 - 1;

struct Message {
    int bytes, senderId, round;
    Loc source;
};

struct Trap {
    int id;
    TrapType type;
    Team team;
    Loc loc;
};

enum CatState : int8_t { CAT_EXPLORE, CAT_ATTACK };

// Mirrors DominationFactor.
enum WinReason : int8_t {
    WIN_NONE,
    WIN_RESIGNATION,
    WIN_KILL_ALL_RAT_KINGS,
    WIN_MORE_POINTS,
    WIN_MORE_CHEESE,
    WIN_MORE_ROBOTS_ALIVE,
    WIN_ARBITRARY,
    WIN_ENGINE_ERROR,
};
const char* winReasonName(WinReason r);

struct Robot {
    int id;
    Team team;
    UnitType type;
    Loc loc;
    Dir dir;
    int health;
    int cheese = 0;
    int chirality;

    int bytecodeLimit;
    int bytecodesUsed = 0;
    int roundsAlive = 0;
    int actionCd = C::COOLDOWN_LIMIT, moveCd = C::COOLDOWN_LIMIT, turnCd = C::COOLDOWN_LIMIT;

    int turnsSinceThrownOrDropped = C::MAX_ROUNDS;
    int lastGrabberId = -1;
    Robot* carrying = nullptr;
    Robot* grabbedBy = nullptr;
    Dir thrownDir = DIR_NULL;
    int remainingThrow = 0;
    int remainingCarried = 0;

    std::deque<Message> messages;
    int sentMessages = 0;
    int sleepTimeRemaining = 0;

    bool alive = true;      // registered in ObjectInfo
    int hostUsed = 0;             // bytecodes the host reported for the last turn
    bool hostTerminated = false;  // the player has stopped (exception, return, death)

    // Cat brain.
    std::vector<Loc> waypoints;
    int currentWaypoint = 0, previousWaypoint = 0;
    CatState catState = CAT_EXPLORE;
    bool hasCatTargetLoc = false;
    Loc catTargetLoc;
    bool hasCatTarget = false;
    int catTargetId = 0;
    Loc catTargetSnapshotLoc;  // RobotInfo.location of the snapshot the cat locked onto
    int catTurns = 0, catTurnsStuck = 0;

    bool beingThrown() const { return thrownDir != DIR_NULL; }
    bool canAct() const { return actionCd < C::COOLDOWN_LIMIT; }
    bool canMoveCd() const { return moveCd < C::COOLDOWN_LIMIT; }
    bool canTurnCd() const { return turnCd < C::COOLDOWN_LIMIT; }
    int visionRadiusSquared() const { return UNIT[type].visionRadiusSquared; }
};

// The bot side of a game: the JVM host for teams A and B (cats have none).
class BotHost {
public:
    virtual ~BotHost() = default;
    virtual void spawned(Robot& r) = 0;
    virtual void killed(Robot& r) = 0;
    // Runs one turn; returns false when the robot's player has terminated.
    virtual void runTurn(Robot& r) = 0;
    virtual bool terminated(Robot& r) = 0;
    virtual int bytecodesUsed(Robot& r) = 0;
};

struct RoundTrace {
    int round;
    uint64_t hash;
};

class World {
public:
    World(const GameMap& map, int seed, BotHost* host);
    ~World();
    World(const World&) = delete;
    World& operator=(const World&) = delete;

    // Plays one round; returns false when the game is over (before or after it).
    bool runRound();
    void runToEnd();

    // ---- state ----
    const GameMap& map() const { return map_; }
    int round() const { return round_; }
    bool running() const { return running_; }
    Team winner() const { return winner_; }
    bool hasWinner() const { return hasWinner_; }
    WinReason winReason() const { return winReason_; }
    const std::string& engineError() const { return engineError_; }
    bool cooperation() const { return cooperation_; }
    int backstabberOrNeg() const { return backstabber_; }
    uint64_t stateHash() const;
    std::string stateDump() const;

    int teamCheese(Team t) const { return globalCheese_[t]; }
    int teamDirt(Team t) const { return dirtCount_[t]; }
    int numBabyRats(Team t) const { return numBabyRats_[t]; }
    int numRatKings(Team t) const { return numRatKings_[t]; }
    int kingHealth(Team t) const;  // summed over the team's living kings
    // Damage statistics by source (no effect on the game): hp kings lost, kings and baby rats killed.
    enum DamageSource { SRC_OTHER, SRC_BITE, SRC_SCRATCH, SRC_POUNCE, SRC_CRUSH, SRC_EATEN, SRC_THROW_HIT, SRC_THROWN,
                        SRC_TRAP, SRC_STARVE, SRC_FUSE, SRC_COUNT };
    static constexpr const char* SOURCE_NAMES[SRC_COUNT] = {"other", "bite", "scratch", "pounce", "crush", "eaten",
                                                           "throwHit", "thrown", "trap", "starve", "fuse"};
    int kingDamage(Team t, int src) const { return kingDamage_[t][src]; }
    int kingKills(Team t, int src) const { return kingKills_[t][src]; }
    int ratKills(Team t, int src) const { return ratKills_[t][src]; }
    int cheeseTransferred(Team t) const { return cheeseTransferred_[t]; }
    int damageToCats(Team t) const { return damageToCats_[t]; }
    int points(Team t) const { return points_[t]; }
    int numCats() const { return numCats_; }
    int trapCount(TrapType ty, Team t) const { return trapCounts_[ty][t]; }
    const std::vector<int>& execOrder() const { return execOrder_; }
    Robot* robotById(int id) const { return id >= 0 && size_t(id) < byId_.size() ? byId_[id] : nullptr; }
    Robot* robotAt(Loc l) const { return grid_[index(l)]; }
    Robot* flyingAt(Loc l) const { return flying_[index(l)]; }
    bool wall(Loc l) const { return walls_[index(l)]; }
    bool dirt(Loc l) const { return dirt_[index(l)]; }
    int cheeseAt(Loc l) const { return cheese_[index(l)]; }
    bool hasMine(Loc l) const { return mineAt_[index(l)] >= 0; }
    bool passable(Loc l) const { return !walls_[index(l)] && !dirt_[index(l)] && !flying_[index(l)]; }
    bool onMap(Loc l) const { return map_.onMap(l); }
    int index(Loc l) const { return l.x + l.y * map_.width; }
    const Trap* trapAt(Loc l, Team t) const { return t <= TEAM_B ? trapGrid_[t][index(l)] : nullptr; }

    // ---- RobotController (each returns E_OK or the exception it would throw) ----
    bool canSenseLocation(const Robot& r, Loc l) const;  // the real vision cone (game rules)
    // Testing oracle (x-ray): the team's rats sense a full circle, their vision radius in every direction, for robots
    // (SEE_ROBOTS), the map (SEE_MAP) or both. Actions and rules that depend on facing (attacking, grabbing from
    // behind, carrying in front) keep the real cone.
    // SEE_TEAM: robots that any allied rat sees, within the rat's own vision radius (perfect sharing).
    enum Seen { SEE_ROBOTS = 1, SEE_MAP = 2, SEE_ALL = 3, SEE_TEAM = 4 };
    bool sees(const Robot& r, Loc l, int what = SEE_ALL) const;  // what the bot may sense
    void setXray(int team, int what) { if (team == 0 || team == 1) xray_[team] = what; }
    Err assertCanSense(const Robot& r, Loc l, int what = SEE_ALL) const;
    Err canMove(Robot& r, Dir d);
    Err move(Robot& r, Dir d);
    Err canTurnNow(Robot& r);  // canTurn()
    Err canTurn(Robot& r, Dir d);
    Err turn(Robot& r, Dir d);
    Err canBuildRat(Robot& r, Loc l);
    Err buildRat(Robot& r, Loc l);
    Err canBecomeRatKing(Robot& r);
    Err becomeRatKing(Robot& r);
    Err canPlaceDirt(Robot& r, Loc l);
    Err placeDirt(Robot& r, Loc l);
    Err canRemoveDirt(Robot& r, Loc l);
    Err removeDirt(Robot& r, Loc l);
    Err canPlaceTrap(Robot& r, Loc l, TrapType t);
    Err placeTrap(Robot& r, Loc l, TrapType t);
    Err canRemoveTrap(Robot& r, Loc l, TrapType t);
    Err removeTrap(Robot& r, Loc l, TrapType t);
    Err canPickUpCheese(Robot& r, Loc l);
    static constexpr int ALL_CHEESE = -2147483647 - 1;
    Err pickUpCheese(Robot& r, Loc l, int amount);  // ALL_CHEESE is pickUpCheese(loc)
    Err canAttack(Robot& r, Loc l, int cheese);
    Err attack(Robot& r, Loc l, int cheese, bool withCheese);  // withCheese: attack(loc, cheese)
    bool squeak(Robot& r, int content);
    Err writeSharedArray(Robot& r, int index, int value);
    int readSharedArray(const Robot& r, int index) const { return shared_[r.team][index]; }
    Err canTransferCheese(Robot& r, Loc l, int amount);
    Err transferCheese(Robot& r, Loc l, int amount);
    Err canThrowRat(Robot& r);
    Err throwRat(Robot& r);
    Err canDropRat(Robot& r, Dir d);
    Err dropRat(Robot& r, Dir d);
    Err canCarryRat(Robot& r, Loc l);
    Err carryRat(Robot& r, Loc l);
    void resign(Robot& r);
    int currentRatCost(const Robot& r) const;
    bool catTrapsAllowed(Team t) const;
    Err senseRobotById(const Robot& r, int id, Robot** out) const;

    // senseNearbyRobots(center, radiusSquared, team): team < 0 means any team.
    Err senseNearbyRobots(const Robot& r, Loc center, int radiusSquared, int team, std::vector<Robot*>& out) const;
    Err senseNearbyMapInfos(const Robot& r, Loc center, int radiusSquared, std::vector<Loc>& out) const;
    Err allLocationsWithinRadiusSquared(const Robot& r, Loc center, int radiusSquared, std::vector<Loc>& out) const;
    void partLocations(const Robot& r, Loc* out, int& n) const;

    // Robot was destroyed by its own uncaught exception or a return from run().
    void destroyRobot(int id, bool fromException = false, bool fromDamage = false);

private:
    GameMap map_;
    BotHost* host_;
    int round_ = 0;
    bool running_ = true;
    bool cooperation_ = true;
    int backstabRound_ = -1;
    int backstabber_ = -1;
    bool hasWinner_ = false;
    Team winner_ = NEUTRAL;
    WinReason winReason_ = WIN_NONE;
    std::string engineError_;

    std::vector<uint8_t> walls_, dirt_;
    std::vector<int> cheese_;
    std::vector<Robot*> grid_, flying_;
    std::vector<const Trap*> trapGrid_[2];
    std::vector<std::vector<const Trap*>> trapTriggers_;
    int trapCounts_[2][2] = {{0, 0}, {0, 0}};
    std::vector<std::unique_ptr<Trap>> trapStore_;

    struct Mine {
        Loc loc;
        int pair;
        int lastSpawnRound;
    };
    std::vector<Mine> mines_;
    std::vector<int> mineAt_;
    bool ranMinesThisRound_ = false;
    std::vector<uint8_t> traveled_;  // by robot id, cleared each round
    std::vector<int> traveledList_;

    int globalCheese_[3] = {0, 0, 0};
    int dirtCount_[3] = {0, 0, 0};
    int cheeseCollected_[3] = {0, 0, 0};
    int cheeseTransferred_[3] = {0, 0, 0};
    int numBabyRats_[3] = {0, 0, 0};
    int numRatKings_[3] = {0, 0, 0};
    int damageToCats_[3] = {0, 0, 0};
    int damageSuffered_[3] = {0, 0, 0};
    int points_[3] = {0, 0, 0};
    int numCats_ = 0;
    int shared_[2][C::SHARED_ARRAY_SIZE] = {};

    std::vector<std::unique_ptr<Robot>> robotStore_;
    std::vector<Robot*> byId_;
    std::vector<int> execOrder_;
    mutable std::vector<Robot*> scratch_;
    int xray_[2] = {0, 0};
    int dmgSrc_ = SRC_OTHER;
    int kingDamage_[2][SRC_COUNT] = {}, kingKills_[2][SRC_COUNT] = {}, ratKills_[2][SRC_COUNT] = {};
    std::vector<std::vector<Dir>> bfs_[2];  // [chirality][target] -> dir per source, lazily built

    JRandom rand_;
    JRandom catRand_;  // InternalRobot.rand, seeded 1092 once per JVM; one game per JVM here
    JRandom arbitraryRand_;
    // IDGenerator
    JRandom idRand_;
    int idBlock_[4096];
    int idCursor_ = 0, idNextBlock_ = 0;

    // ---- helpers ----
    int nextId();
    void allocateIdBlock();
    Loc indexToLoc(int i) const { return map_.loc(i); }
    int symX(int x) const;
    int symY(int y) const;
    Dir flipDir(Dir d) const;
    template <class F>
    void forLocationsInRadius(Loc center, int radiusSquared, int chirality, F&& f) const;
    void ratPartLocations(const Robot& r, Loc* out, int& n) const;
    void catPartLocations(const Robot& r, Loc* out) const;
    Loc catCorner(const Robot& r) const;
    const std::vector<Dir>& bfsTo(Loc target, int chirality);
    Dir bfsDir(Loc from, Loc to, int chirality);

    void addTeamCheese(Team t, int amount);
    void robotAddCheese(Robot& r, int amount);
    void addHealth(Robot& r, int amount);
    void addActionCooldown(Robot& r, int amount);
    void addMovementCooldown(Robot& r, Dir d);
    void addTurningCooldown(Robot& r);
    void backstab(Team t);

    void spawnCheese(Mine& mine);
    void runCheeseMines();
    void placeTrapObject(Loc l, TrapType type, Team team);
    void removeTrapObject(Loc l, Team team);
    void triggerTrap(const Trap* trap, Robot& r);
    void processTrapsAt(Robot& r, Loc l);

    Robot& spawnRobot(int id, UnitType type, Loc l, Dir d, int chirality, Team team);
    void translate(Robot& r, int dx, int dy);
    void bite(Robot& r, Loc l, int cheeseConsumed);
    void scratch(Robot& r, Loc l);
    void grab(Robot& r, Loc l);
    void getGrabbed(Robot& r, Robot& grabber);
    void getDropped(Robot& r, Loc l);
    void swapGrabber(Robot& r);
    void throwRobot(Robot& r);
    void getThrown(Robot& r, Dir d);
    void hitGround(Robot& r);
    void hitTarget(Robot& r, bool secondMove);
    void travelFlying(Robot& r, bool secondMove);
    bool canPounce(Robot& r, Loc target, int& dx, int& dy);
    void pounce(Robot& r, int dx, int dy);
    void catTurn(Robot& r);

    Err assertActionReady(const Robot& r) const;
    Err assertMovementReady(const Robot& r) const;
    Err assertTurningReady(const Robot& r) const;
    Err assertCanAct(const Robot& r, Loc l, float maxRadiusSquared) const;
    Err assertCanAttackRat(Robot& r, Loc l, int cheese);
    Err assertCanAttackCat(Robot& r, Loc l);

    void processBeginningOfTurn(Robot& r);
    void processEndOfTurn(Robot& r);
    void updateRobot(Robot& r);
    void processEndOfRound();

    void setWinner(Team t, WinReason why);
    bool setWinnerIfKilledAllRatKings();
    bool setWinnerIfMorePoints();
    bool setWinnerIfMoreCheese();
    bool setWinnerIfMoreRatsAlive();
    bool setWinnerIfAllCatsDead();
    void setWinnerArbitrary();
    void checkEndOfMatch();
    void checkWin();
};

}  // namespace fish
