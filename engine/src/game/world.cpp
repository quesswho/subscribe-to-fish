// Port of battlecode.world (engine 1.2.6). Method order and side-effect order follow the Java sources on purpose:
// several official quirks (grid cells cleared by the wrong robot, trap lists edited mid-iteration, identity
// checks, (int)1.5 multipliers) change outcomes, and matching them is the point.
#include "world.hpp"

#include <algorithm>
#include <cmath>
#include <cstdio>
#include <sstream>

namespace fish {

const char* winReasonName(WinReason r) {
    switch (r) {
        case WIN_NONE: return "none";
        case WIN_RESIGNATION: return "resignation";
        case WIN_KILL_ALL_RAT_KINGS: return "killed all rat kings";
        case WIN_MORE_POINTS: return "more points";
        case WIN_MORE_CHEESE: return "more cheese";
        case WIN_MORE_ROBOTS_ALIVE: return "more robots alive";
        case WIN_ARBITRARY: return "coin flip";
        case WIN_ENGINE_ERROR: return "engine error";
    }
    return "?";
}

namespace {

// Vision cones depend only on the offset, so they are tabulated once per (unit type, facing).
constexpr int VR = 7;
constexpr int VW = 2 * VR + 1;

struct VisionTable {
    bool v[3][9][VW][VW];
    VisionTable() {
        for (int t = 0; t < 3; t++)
            for (int d = 0; d < 9; d++)
                for (int dx = -VR; dx <= VR; dx++)
                    for (int dy = -VR; dy <= VR; dy++)
                        v[t][d][dx + VR][dy + VR] =
                            Loc{0, 0}.withinCone({dx, dy}, UNIT[t].visionRadiusSquared, Dir(d),
                                                 UNIT[t].visionAngle, usesBottomLeft(UnitType(t)));
    }
};
const VisionTable& vision() {
    static const VisionTable table;
    return table;
}

bool isRat(UnitType t) { return t == BABY_RAT || t == RAT_KING; }

}  // namespace

// ============================================================================
// construction
// ============================================================================

World::World(const GameMap& map, int seed, BotHost* host)
    : map_(map), host_(host), rand_(seed), catRand_(1092), arbitraryRand_(seed ^ 0x5eed), idRand_(seed) {
    map_.seed = seed;
    int n = map_.width * map_.height;
    walls_ = map_.walls;
    dirt_ = map_.dirt;
    cheese_ = map_.cheese;
    grid_.assign(n, nullptr);
    flying_.assign(n, nullptr);
    trapGrid_[0].assign(n, nullptr);
    trapGrid_[1].assign(n, nullptr);
    trapTriggers_.assign(n, {});
    bfs_[0].resize(n);
    bfs_[1].resize(n);

    idNextBlock_ = 10000;
    allocateIdBlock();

    globalCheese_[TEAM_A] += C::INITIAL_TEAM_CHEESE;
    globalCheese_[TEAM_B] += C::INITIAL_TEAM_CHEESE;

    mineAt_.assign(n, -1);
    for (int i = 0; i < n; i++)
        if (map_.mines[i]) {
            mineAt_[i] = int(mines_.size());
            mines_.push_back({map_.loc(i), -1, 0});
        }
    for (auto& m : mines_) m.pair = mineAt_[index({symX(m.loc.x), symY(m.loc.y)})];

    for (const auto& b : map_.bodies) spawnRobot(b.id, b.type, b.loc, b.dir, b.chirality, b.team);
}

World::~World() = default;

int World::nextId() {
    int id = idBlock_[idCursor_++];
    if (idCursor_ == 4096) allocateIdBlock();
    return id;
}

void World::allocateIdBlock() {
    idCursor_ = 0;
    for (int i = 0; i < 4096; i++) idBlock_[i] = idNextBlock_ + i + 1;
    for (int i = 4095; i > 0; i--) {
        int j = idRand_.nextInt(i + 1);
        std::swap(idBlock_[i], idBlock_[j]);
    }
    idNextBlock_ += 4096;
}

int World::symX(int x) const { return map_.symmetry == HORIZONTAL ? x : map_.width - 1 - x; }
int World::symY(int y) const { return map_.symmetry == VERTICAL ? y : map_.height - 1 - y; }

Dir World::flipDir(Dir d) const {
    int dx = DX[d], dy = DY[d];
    switch (map_.symmetry) {
        case HORIZONTAL: dy = -dy; break;
        case VERTICAL: dx = -dx; break;
        case ROTATIONAL: dx = -dx; dy = -dy; break;
    }
    return dirFromDelta(dx, dy);
}

// GameWorld.getAllLocationsWithinConeRadiusSquaredWithoutMap with facing CENTER: x-major, chirality 1 reverses
// the axes the map's symmetry mirrors.
template <class F>
void World::forLocationsInRadius(Loc center, int radiusSquared, int chirality, F&& f) const {
    int r = int(std::ceil(std::sqrt(double(radiusSquared)))) + 1;
    int minX = std::max(center.x - r, 0), minY = std::max(center.y - r, 0);
    int maxX = std::min(center.x + r, map_.width - 1), maxY = std::min(center.y + r, map_.height - 1);
    bool revX = false, revY = false;
    if (chirality == 1) {
        revX = map_.symmetry == VERTICAL || map_.symmetry == ROTATIONAL;
        revY = map_.symmetry == HORIZONTAL || map_.symmetry == ROTATIONAL;
    }
    for (int i = 0; i <= maxX - minX; i++) {
        int x = revX ? maxX - i : minX + i;
        for (int j = 0; j <= maxY - minY; j++) {
            int y = revY ? maxY - j : minY + j;
            Loc l{x, y};
            if (center.distSq(l) <= radiusSquared) f(l);
        }
    }
}

void World::ratPartLocations(const Robot& r, Loc* out, int& n) const {
    int size = UNIT[r.type].size;
    int lo = -(size - 1) / 2, hi = size / 2;
    bool revX = false, revY = false;
    if (r.chirality == 1) {
        revX = map_.symmetry == VERTICAL || map_.symmetry == ROTATIONAL;
        revY = map_.symmetry == HORIZONTAL || map_.symmetry == ROTATIONAL;
    }
    n = 0;
    for (int a = 0; a <= hi - lo; a++) {
        int i = revX ? hi - a : lo + a;
        for (int b = 0; b <= hi - lo; b++) {
            int j = revY ? hi - b : lo + b;
            out[n++] = {r.loc.x + i, r.loc.y - j};
        }
    }
}

Loc World::catCorner(const Robot& r) const {
    if (r.chirality == 0) return r.loc;
    switch (map_.symmetry) {
        case VERTICAL: return r.loc.add(EAST);
        case HORIZONTAL: return r.loc.add(NORTH);
        case ROTATIONAL: return r.loc.add(NORTHEAST);
    }
    return r.loc;
}

void World::catPartLocations(const Robot& r, Loc* out) const {
    Loc start = catCorner(r);
    Dir rot = NORTH;
    if (r.chirality != 0) rot = map_.symmetry == VERTICAL ? NORTH : map_.symmetry == HORIZONTAL ? SOUTH : WEST;
    for (int i = 0; i < 4; i++) {
        out[i] = start;
        start = start.add(rot);
        rot = r.chirality == 0 ? rotateRight(rotateRight(rot)) : rotateLeft(rotateLeft(rot));
    }
}

void World::partLocations(const Robot& r, Loc* out, int& n) const {
    if (r.type == CAT) {
        catPartLocations(r, out);
        n = 4;
    } else {
        ratPartLocations(r, out, n);
    }
}

const std::vector<Dir>& World::bfsTo(Loc target, int chirality) {
    auto& slot = bfs_[chirality][index(target)];
    if (!slot.empty()) return slot;
    int n = map_.width * map_.height;
    slot.assign(n, DIR_NULL);
    static constexpr Dir order[8] = {NORTH, EAST, SOUTH, WEST, NORTHEAST, SOUTHEAST, SOUTHWEST, NORTHWEST};
    Dir corners[4] = {CENTER, NORTH, NORTHEAST, EAST};
    if (chirality == 1)
        for (auto& c : corners) c = flipDir(c);
    std::vector<int> queue;
    queue.reserve(n);
    queue.push_back(index(target));
    slot[index(target)] = CENTER;
    for (size_t head = 0; head < queue.size(); head++) {
        Loc next = map_.loc(queue[head]);
        for (Dir d : order) {
            Dir use = chirality == 1 ? flipDir(d) : d;
            Loc nb = next.add(use);
            if (onMap(nb) && slot[index(nb)] != DIR_NULL) continue;
            bool valid = true;
            for (Dir c : corners) {
                Loc corner = nb.add(c);
                if (!onMap(corner) || wall(corner)) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                slot[index(nb)] = opposite(use);
                queue.push_back(index(nb));
            }
        }
    }
    return slot;
}

Dir World::bfsDir(Loc from, Loc to, int chirality) { return bfsTo(to, chirality)[index(from)]; }

// ============================================================================
// resources and state changes
// ============================================================================

void World::addTeamCheese(Team t, int amount) {
    if (globalCheese_[t] + amount < 0) throw CheeseError();
    globalCheese_[t] += amount;
}

void World::robotAddCheese(Robot& r, int amount) {
    if (r.type == RAT_KING) {
        addTeamCheese(r.team, amount);
        return;
    }
    if (r.cheese + amount >= 0) {
        r.cheese += amount;
    } else {
        amount += r.cheese;
        r.cheese = 0;
        addTeamCheese(r.team, amount);
    }
}

void World::addHealth(Robot& r, int amount) {
    int before = r.health, src = dmgSrc_;
    dmgSrc_ = SRC_OTHER;
    r.health += amount;
    if (amount < 0 && r.type != CAT) damageSuffered_[r.team] += -amount;
    r.health = std::min(r.health, UNIT[r.type].health);
    // Statistics only (what hurt and what killed each team's robots); no effect on the game.
    if (amount < 0 && r.team < 2 && before > 0) {
        if (r.type == RAT_KING) kingDamage_[r.team][src] += std::min(-amount, before);
        if (r.health <= 0) (r.type == RAT_KING ? kingKills_ : ratKills_)[r.team][src]++;
    }
    if (r.health <= 0) destroyRobot(r.id, false, true);
}

void World::addActionCooldown(Robot& r, int amount) {
    int up = amount * int(r.carrying ? C::CARRY_COOLDOWN_MULTIPLIER : 1);
    if (r.type == BABY_RAT) up = int(double(up) * (1.0 + r.cheese * C::CHEESE_COOLDOWN_PENALTY));
    r.actionCd += up;
}

void World::addMovementCooldown(Robot& r, Dir d) {
    int cd = UNIT[r.type].movementCooldown;
    if (r.type == BABY_RAT && r.dir != d) cd = C::MOVE_STRAFE_COOLDOWN;
    cd *= int(r.carrying ? C::CARRY_COOLDOWN_MULTIPLIER : 1);
    if (r.type == BABY_RAT) cd = int(double(cd) * (1.0 + r.cheese * C::CHEESE_COOLDOWN_PENALTY));
    r.moveCd += cd;
}

void World::addTurningCooldown(Robot& r) {
    r.turnCd += C::TURNING_COOLDOWN * int(r.carrying ? C::CARRY_COOLDOWN_MULTIPLIER : 1);
}

void World::backstab(Team t) {
    if (cooperation_) {
        cooperation_ = false;
        backstabRound_ = round_;
        backstabber_ = t;
    }
}

bool World::catTrapsAllowed(Team t) const {
    int since = cooperation_ ? 0 : round_ - backstabRound_;
    return cooperation_ || (since <= C::CAT_TRAP_ROUNDS_AFTER_BACKSTAB && backstabber_ != t);
}

// ============================================================================
// cheese mines
// ============================================================================

void World::runCheeseMines() {
    if (ranMinesThisRound_) return;
    for (auto& m : mines_) spawnCheese(m);
    ranMinesThisRound_ = true;
}

void World::spawnCheese(Mine& mine) {
    double prob = 1 - std::pow(double(1 - C::CHEESE_MINE_SPAWN_PROBABILITY), double(round_ - mine.lastSpawnRound));
    bool spawn = double(rand_.nextFloat()) < prob;
    if (!spawn) return;
    if (mine.pair < 0) throw EngineError("cheese mine has no symmetric pair");
    int dx = rand_.nextInt(-C::SQ_CHEESE_SPAWN_RADIUS, C::SQ_CHEESE_SPAWN_RADIUS + 1);
    int dy = rand_.nextInt(-C::SQ_CHEESE_SPAWN_RADIUS, C::SQ_CHEESE_SPAWN_RADIUS + 1);
    Mine& pair = mines_[mine.pair];
    Loc og = mine.loc, paired = pair.loc;
    int pdx = map_.symmetry == HORIZONTAL ? dx : -dx;
    int pdy = map_.symmetry == VERTICAL ? dy : -dy;
    Loc c{mine.loc.x + dx, mine.loc.y + dy}, p{pair.loc.x + pdx, pair.loc.y + pdy};
    if (onMap(c) && onMap(p) && !wall(c) && !wall(p)) {
        og = c;
        paired = p;
    }
    mine.lastSpawnRound = round_;
    pair.lastSpawnRound = round_;
    cheese_[index(og)] += C::CHEESE_SPAWN_AMOUNT;
    cheese_[index(paired)] += C::CHEESE_SPAWN_AMOUNT;
}

// ============================================================================
// traps
// ============================================================================

void World::placeTrapObject(Loc l, TrapType type, Team team) {
    int id = nextId();
    trapStore_.push_back(std::make_unique<Trap>(Trap{id, type, team, l}));
    const Trap* t = trapStore_.back().get();
    trapGrid_[team][index(l)] = t;
    forLocationsInRadius(l, TRAP[type].triggerRadiusSquared, 0, [&](Loc a) { trapTriggers_[index(a)].push_back(t); });
    trapCounts_[type][team]++;
}

void World::removeTrapObject(Loc l, Team team) {
    const Trap* t = trapGrid_[team][index(l)];
    if (!t) return;
    trapCounts_[t->type][team]--;
    trapGrid_[team][index(l)] = nullptr;
    forLocationsInRadius(l, TRAP[t->type].triggerRadiusSquared, 0, [&](Loc a) {
        auto& v = trapTriggers_[index(a)];
        auto it = std::find(v.begin(), v.end(), t);
        if (it != v.end()) v.erase(it);
    });
}

void World::triggerTrap(const Trap* trap, Robot& r) {
    TrapType type = trap->type;
    r.moveCd = TRAP[type].stunTime;
    if (type == CAT_TRAP && r.type == CAT && r.health > 0)
        damageToCats_[trap->team] += std::min(TRAP[type].damage, r.health);
    if (type != CAT_TRAP) backstab(opponent(r.team));
    Loc l = trap->loc;
    Team owner = trap->team;
    removeTrapObject(l, owner);
    dmgSrc_ = SRC_TRAP;
    addHealth(r, -TRAP[type].damage);
}

void World::processTrapsAt(Robot& r, Loc l) {
    auto& list = trapTriggers_[index(l)];
    for (int j = int(list.size()) - 1; j >= 0; j--) {
        if (size_t(j) >= list.size()) throw EngineError("trap trigger list changed while triggering");
        const Trap* trap = list[j];
        bool wrongType = (isRat(r.type) && trap->type == CAT_TRAP) || (r.type == CAT && trap->type == RAT_TRAP);
        if (trap->team == r.team || wrongType) continue;
        triggerTrap(trap, r);
    }
}

// ============================================================================
// robots
// ============================================================================

Robot& World::spawnRobot(int id, UnitType type, Loc l, Dir d, int chirality, Team team) {
    if (d == CENTER) d = l.directionTo({(map_.width - 1) / 2, (map_.height - 1) / 2});
    auto owned = std::make_unique<Robot>();
    Robot& r = *owned;
    r.id = id;
    r.team = team;
    r.type = type;
    r.loc = l;
    r.dir = d;
    r.health = UNIT[type].health;
    r.chirality = chirality;
    r.bytecodeLimit = UNIT[type].bytecodeLimit;
    if (type == CAT) {
        const CatPath* path = nullptr;
        for (auto& c : map_.cats)
            if (c.id == id) path = &c;
        if (!path || path->waypoints.empty()) throw EngineError("cannot find waypoints for cat " + std::to_string(id));
        for (int w : path->waypoints) r.waypoints.push_back(map_.loc(w));
        r.hasCatTargetLoc = true;
        r.catTargetLoc = r.waypoints[0];
    }
    robotStore_.push_back(std::move(owned));

    Loc parts[9];
    int n;
    partLocations(r, parts, n);
    for (int i = 0; i < n; i++) grid_[index(parts[i])] = &r;

    if (size_t(id) >= byId_.size()) byId_.resize(size_t(id) + 1024, nullptr);
    byId_[id] = &r;
    execOrder_.push_back(id);
    if (team != NEUTRAL && host_) host_->spawned(r);

    if (type == BABY_RAT) numBabyRats_[team]++;
    else if (type == RAT_KING) numRatKings_[team]++;
    else numCats_++;
    return r;
}

void World::destroyRobot(int id, bool fromException, bool fromDamage) {
    (void)fromException;
    (void)fromDamage;
    Robot* rp = robotById(id);
    if (!rp) return;
    Robot& r = *rp;
    Team team = r.team;
    Loc loc = r.loc;

    if (r.type == BABY_RAT) numBabyRats_[team]--;
    else if (r.type == RAT_KING) numRatKings_[team]--;
    else numCats_--;

    Loc parts[9];
    int n;
    partLocations(r, parts, n);
    for (int i = 0; i < n; i++) {
        if (!onMap(parts[i])) continue;
        grid_[index(parts[i])] = nullptr;
        flying_[index(parts[i])] = nullptr;
    }
    if (r.carrying) getDropped(*r.carrying, loc);
    if (r.grabbedBy) {
        Robot* carrier = r.grabbedBy;
        r.grabbedBy = nullptr;
        if (carrier->carrying == &r) carrier->carrying = nullptr;
    }
    if (r.cheese > 0) cheese_[index(loc)] += r.cheese;

    if (team != NEUTRAL && host_) host_->killed(r);
    r.alive = false;
    byId_[id] = nullptr;
    execOrder_.erase(std::find(execOrder_.begin(), execOrder_.end(), id));

    if (r.type == RAT_KING && numRatKings_[team] == 0) checkWin();
    else if (cooperation_ && r.type == CAT && numCats_ == 0) checkWin();
}

void World::translate(Robot& r, int dx, int dy) {
    Loc before[9];
    int n;
    partLocations(r, before, n);
    for (int i = 0; i < n; i++) grid_[index(before[i])] = nullptr;
    for (int i = 0; i < n; i++) grid_[index(before[i].translate(dx, dy))] = &r;
    r.loc = r.loc.translate(dx, dy);
    if (r.type != CAT && r.carrying) r.carrying->loc = r.loc;
}

void World::bite(Robot& r, Loc l, int cheeseConsumed) {
    int distSq = r.loc.distSq(l);
    if (distSq == 0 ||
        distSq > (r.type == RAT_KING ? C::RAT_KING_ATTACK_DISTANCE_SQUARED : C::ATTACK_DISTANCE_SQUARED))
        return;
    if (r.loc.directionTo(l) == CENTER) return;
    if (r.dir == CENTER) return;
    Robot* target = robotAt(l);
    if (!target || target->team == r.team) return;
    int damage = C::RAT_BITE_DAMAGE;
    if (cheeseConsumed > 0) {
        robotAddCheese(r, -cheeseConsumed);
        damage += int(std::ceil(std::sqrt(double(cheeseConsumed))));
    }
    if (target->type == CAT) damageToCats_[r.team] += std::min(damage, target->health);
    dmgSrc_ = SRC_BITE;
    addHealth(*target, -damage);
    if (target->type != CAT) backstab(r.team);
}

void World::scratch(Robot& r, Loc l) {
    Robot* target = robotAt(l);
    dmgSrc_ = SRC_SCRATCH;
    if (target && target->team != r.team) addHealth(*target, -C::CAT_SCRATCH_DAMAGE);
}

void World::grab(Robot& r, Loc l) {
    r.carrying = robotAt(l);
    getGrabbed(*r.carrying, r);
    if (r.carrying->team != r.team) backstab(r.team);
}

void World::getGrabbed(Robot& r, Robot& grabber) {
    r.turnsSinceThrownOrDropped = 0;
    r.grabbedBy = &grabber;
    r.lastGrabberId = grabber.id;
    grid_[index(r.loc)] = nullptr;
    if (r.carrying) {
        getDropped(*r.carrying, r.loc);
        r.carrying = nullptr;
    }
    r.loc = grabber.loc;
    r.remainingCarried = C::MAX_CARRY_DURATION;
}

void World::getDropped(Robot& r, Loc l) {
    if (!onMap(l)) throw EngineError("Cannot drop outside of map");
    if (robotAt(l)) throw EngineError("Cannot drop into occupied space");
    if (!passable(l)) throw EngineError("Cannot drop into impassable terrain");
    r.turnsSinceThrownOrDropped = 0;
    r.grabbedBy = nullptr;
    r.remainingCarried = 0;
    r.loc = l;
    if (r.health > 0) {
        grid_[index(r.loc)] = &r;
        processTrapsAt(r, r.loc);
    } else {
        destroyRobot(r.id);
    }
}

void World::swapGrabber(Robot& r) {
    Robot& grabber = *r.grabbedBy;
    Loc dropLoc = grabber.loc;
    r.carrying = &grabber;
    grabber.grabbedBy = &r;
    r.grabbedBy = nullptr;
    grabber.carrying = nullptr;
    grabber.loc = dropLoc;
    r.loc = dropLoc;
    grid_[index(dropLoc)] = &r;
    grabber.remainingCarried = C::MAX_CARRY_DURATION;
}

void World::throwRobot(Robot& r) {
    Robot& carried = *r.carrying;
    getThrown(carried, r.dir);
    if (size_t(carried.id) >= traveled_.size()) traveled_.resize(size_t(carried.id) + 1024, 0);
    traveled_[carried.id] = 1;
    traveledList_.push_back(carried.id);
    r.carrying = nullptr;
}

void World::getThrown(Robot& r, Dir d) {
    r.turnsSinceThrownOrDropped = 0;
    r.grabbedBy = nullptr;
    r.remainingCarried = 0;
    r.thrownDir = d;
    r.remainingThrow = 4;
    travelFlying(r, false);
    travelFlying(r, true);
}

void World::hitGround(Robot& r) {
    r.turnsSinceThrownOrDropped = 0;
    r.thrownDir = DIR_NULL;
    r.remainingThrow = 0;
    dmgSrc_ = SRC_THROWN;
    addHealth(r, -C::THROW_DAMAGE);
    flying_[index(r.loc)] = nullptr;
    if (r.health > 0) {
        grid_[index(r.loc)] = &r;
        processTrapsAt(r, r.loc);
    } else {
        destroyRobot(r.id);
    }
    r.moveCd += C::HIT_GROUND_COOLDOWN;
    r.actionCd += C::HIT_GROUND_COOLDOWN;
    r.turnCd += C::HIT_GROUND_COOLDOWN;
}

void World::hitTarget(Robot& r, bool secondMove) {
    int damage = C::THROW_DAMAGE +
                 C::THROW_DAMAGE_PER_TILE * (C::TILES_FLOWN_PER_TURN * r.remainingThrow + (secondMove ? 0 : 1));
    Loc next = r.loc.add(r.thrownDir);
    if (Robot* hit = robotAt(next)) {
        dmgSrc_ = SRC_THROW_HIT;
        addHealth(*hit, -damage);
    } else if (Robot* fly = flyingAt(next)) {
        fly->remainingThrow = 1;
    }
    r.thrownDir = DIR_NULL;
    r.remainingThrow = 0;
    dmgSrc_ = SRC_THROWN;
    addHealth(r, -damage);
    flying_[index(r.loc)] = nullptr;
    if (r.health > 0) {
        grid_[index(r.loc)] = &r;
        processTrapsAt(r, r.loc);
    } else {
        destroyRobot(r.id);
    }
    r.moveCd += C::HIT_TARGET_COOLDOWN;
    r.actionCd += C::HIT_TARGET_COOLDOWN;
    r.turnCd += C::HIT_TARGET_COOLDOWN;
}

void World::travelFlying(Robot& r, bool secondMove) {
    if (r.thrownDir == DIR_NULL || r.health == 0) return;
    Loc next = r.loc.add(r.thrownDir);
    if (!onMap(next)) {
        hitGround(r);
        return;
    }
    Robot* there = robotAt(next);
    if (there && there->type == CAT) {
        flying_[index(r.loc)] = nullptr;
        dmgSrc_ = SRC_EATEN;
        addHealth(r, -r.health);
        robotAt(next)->sleepTimeRemaining = C::CAT_SLEEP_TIME;
        return;
    }
    if (there || !passable(next)) {
        hitTarget(r, secondMove);
        return;
    }
    flying_[index(r.loc)] = nullptr;
    flying_[index(next)] = &r;
    r.loc = next;
}

bool World::canPounce(Robot& r, Loc target, int& outDx, int& outDy) {
    if (!r.canMoveCd()) return false;
    bool within = r.loc.bottomLeftDistSq(target) <= C::CAT_POUNCE_MAX_DISTANCE_SQUARED;
    if (!onMap(target) || !passable(target) || !within) return false;
    Loc parts[4];
    catPartLocations(r, parts);
    for (Loc corner : parts) {
        Dir toCenter = corner.directionTo(r.loc);
        int dx = DX[toCenter] + (target.x - r.loc.x);
        int dy = DY[toCenter] + (target.y - r.loc.y);
        bool valid = true;
        for (Loc tile : parts) {
            Loc land = tile.translate(dx, dy);
            if (!onMap(land)) {
                valid = false;
            } else if (!passable(land)) {
                valid = false;
            } else if (Robot* o = robotAt(land); o && (o->type == CAT || o->type == RAT_KING)) {
                valid = false;
            }
        }
        if (valid) {
            outDx = dx;
            outDy = dy;
            return true;
        }
    }
    return false;
}

void World::pounce(Robot& r, int dx, int dy) {
    Loc old[4];
    catPartLocations(r, old);
    for (Loc p : old) {
        Robot* crushed = robotAt(p.translate(dx, dy));
        if (crushed && crushed->id != r.id) {
            dmgSrc_ = SRC_POUNCE;
            if (crushed->carrying) addHealth(*crushed->carrying, -crushed->carrying->health);
            dmgSrc_ = SRC_POUNCE;
            addHealth(*crushed, -crushed->health);
        }
    }
    translate(r, dx, dy);
    Loc now[4];
    catPartLocations(r, now);
    for (Loc p : now) processTrapsAt(r, p);
    addMovementCooldown(r, r.dir);
    addMovementCooldown(r, r.dir);
}

// ============================================================================
// RobotController
// ============================================================================

bool World::canSenseLocation(const Robot& r, Loc l) const {
    int dx = l.x - r.loc.x, dy = l.y - r.loc.y;
    if (dx < -VR || dx > VR || dy < -VR || dy > VR) return false;
    Dir d = r.dir == DIR_NULL ? CENTER : r.dir;
    return vision().v[r.type][d][dx + VR][dy + VR];
}

int World::kingHealth(Team t) const {
    int hp = 0;
    for (const auto& o : robotStore_)
        if (o->alive && o->team == t && o->type == RAT_KING) hp += o->health;
    return hp;
}

bool World::sees(const Robot& r, Loc l, int what) const {
    if (r.team > 1) return canSenseLocation(r, l);  // cats are neutral
    if ((xray_[r.team] & SEE_TEAM) && what == SEE_ROBOTS) {
        if (canSenseLocation(r, l)) return true;
        int dx = l.x - r.loc.x, dy = l.y - r.loc.y;
        if (dx < -VR || dx > VR || dy < -VR || dy > VR || !vision().v[r.type][CENTER][dx + VR][dy + VR]) return false;
        for (const auto& o : robotStore_)
            if (o->alive && o->team == r.team && o.get() != &r && isRat(o->type) && onMap(o->loc) &&
                canSenseLocation(*o, l))
                return true;
        return false;
    }
    if ((xray_[r.team] & what) != what) return canSenseLocation(r, l);
    int dx = l.x - r.loc.x, dy = l.y - r.loc.y;
    if (dx < -VR || dx > VR || dy < -VR || dy > VR) return false;
    return vision().v[r.type][CENTER][dx + VR][dy + VR];
}

Err World::assertCanSense(const Robot& r, Loc l, int what) const {
    if (l.x == NULL_COORD) return E_NULL_ARGUMENT;
    if (!onMap(l)) return E_SENSE_OFF_MAP;
    if (!sees(r, l, what)) return E_SENSE_OUT_OF_VISION;
    return E_OK;
}

Err World::assertCanAct(const Robot& r, Loc l, float maxRadiusSquared) const {
    // Acting needs the real vision cone, x-ray or not.
    if (l.x == NULL_COORD) return E_NULL_ARGUMENT;
    if (!onMap(l)) return E_SENSE_OFF_MAP;
    if (!canSenseLocation(r, l)) return E_SENSE_OUT_OF_VISION;
    float distance = usesBottomLeft(r.type) ? r.loc.bottomLeftDistSq(l) : float(r.loc.distSq(l));
    if (distance > maxRadiusSquared) return E_ACT_OUT_OF_RANGE;
    return E_OK;
}

Err World::assertActionReady(const Robot& r) const {
    if (!r.canAct()) return E_ACTION_COOLDOWN;
    if (r.beingThrown()) return E_BEING_THROWN;
    if (r.grabbedBy) return E_BEING_CARRIED;
    return E_OK;
}

Err World::assertMovementReady(const Robot& r) const {
    if (!r.canMoveCd()) return E_MOVEMENT_COOLDOWN;
    if (r.beingThrown()) return E_BEING_THROWN;
    if (r.grabbedBy) return E_BEING_CARRIED;
    return E_OK;
}

Err World::assertTurningReady(const Robot& r) const {
    if (!r.canTurnCd()) return E_TURNING_COOLDOWN;
    if (r.beingThrown()) return E_BEING_THROWN;
    if (r.grabbedBy) return E_BEING_CARRIED;
    return E_OK;
}

Err World::canMove(Robot& r, Dir d) {
    if (Err e = assertMovementReady(r)) return e;
    if (d == DIR_NULL) return E_NULL_ARGUMENT;
    Loc parts[9];
    int n;
    partLocations(r, parts, n);
    for (int i = 0; i < n; i++) {
        Loc l = parts[i].add(d);
        if (!onMap(l)) return E_MOVE_OFF_MAP;
        Robot* o = robotAt(l);
        if (o && o->id != r.id && !(o->type == BABY_RAT && r.type == CAT)) return E_MOVE_OCCUPIED;
        if (!passable(l)) return E_MOVE_IMPASSABLE;
    }
    return E_OK;
}

Err World::move(Robot& r, Dir d) {
    if (Err e = canMove(r, d)) return e;
    Loc parts[9];
    int n;
    partLocations(r, parts, n);
    for (int i = 0; i < n; i++) {
        Robot* crushed = robotAt(parts[i].add(d));
        if (crushed && r.id != crushed->id && r.type == CAT && crushed->type == BABY_RAT) {
            dmgSrc_ = SRC_CRUSH;
            if (crushed->carrying) addHealth(*crushed->carrying, -crushed->carrying->health);
            dmgSrc_ = SRC_CRUSH;
            addHealth(*crushed, -crushed->health);
        }
    }
    translate(r, DX[d], DY[d]);
    for (int i = 0; i < n; i++) processTrapsAt(r, parts[i].add(d));
    addMovementCooldown(r, d);
    return E_OK;
}

Err World::canTurnNow(Robot& r) { return assertTurningReady(r); }

Err World::canTurn(Robot& r, Dir d) {
    if (Err e = assertTurningReady(r)) return e;
    if (d == DIR_NULL) return E_TURN_NULL;
    if (d == CENTER) return E_TURN_CENTER;
    return E_OK;
}

Err World::turn(Robot& r, Dir d) {
    if (Err e = canTurn(r, d)) return e;
    r.dir = d;
    addTurningCooldown(r);
    return E_OK;
}

int World::currentRatCost(const Robot& r) const {
    return C::BUILD_ROBOT_BASE_COST +
           C::BUILD_ROBOT_COST_INCREASE * (numBabyRats_[r.team] / C::NUM_ROBOTS_FOR_COST_INCREASE);
}

Err World::canBuildRat(Robot& r, Loc l) {
    if (Err e = assertCanAct(r, l, C::BUILD_ROBOT_RADIUS_SQUARED)) return e;
    if (Err e = assertActionReady(r)) return e;
    if (r.type != RAT_KING) return E_SPAWN_NOT_KING;
    if (globalCheese_[r.team] < currentRatCost(r)) return E_SPAWN_CHEESE;
    if (Err e = assertCanSense(r, l)) return e;
    if (robotAt(l)) return E_SPAWN_OCCUPIED;
    if (Err e = assertCanSense(r, l)) return e;
    if (!passable(l)) return E_SPAWN_IMPASSABLE;
    return E_OK;
}

Err World::buildRat(Robot& r, Loc l) {
    if (Err e = canBuildRat(r, l)) return e;
    int cost = currentRatCost(r);
    robotAddCheese(r, -cost);
    addActionCooldown(r, C::BUILD_ROBOT_COOLDOWN);
    spawnRobot(nextId(), BABY_RAT, l, r.dir, r.chirality, r.team);
    return E_OK;
}

static float buildRadius(UnitType t) {
    return t == RAT_KING ? float(C::RAT_KING_BUILD_DISTANCE_SQUARED)
                         : t == CAT ? C::CAT_BUILD_DISTANCE_SQUARED : float(C::BUILD_DISTANCE_SQUARED);
}

static float trapRadius(UnitType t) {
    return t == RAT_KING ? float(C::RAT_KING_BUILD_DISTANCE_SQUARED) : float(C::BUILD_DISTANCE_SQUARED);
}

Err World::canPlaceDirt(Robot& r, Loc l) {
    if (Err e = assertActionReady(r)) return e;
    if (Err e = assertCanAct(r, l, buildRadius(r.type))) return e;
    if (dirtCount_[r.team] <= 0) return E_NO_DIRT_STASH;
    if (r.cheese + globalCheese_[r.team] < C::PLACE_DIRT_CHEESE_COST) return E_PLACE_DIRT_CHEESE;
    if (wall(l)) return E_DIRT_ON_WALL;
    if (robotAt(l)) return E_DIRT_ON_ROBOT;
    if (dirt(l)) return E_DIRT_ON_DIRT;
    if (hasMine(l)) return E_TILE_HAS_MINE;
    return E_OK;
}

Err World::placeDirt(Robot& r, Loc l) {
    if (Err e = canPlaceDirt(r, l)) return e;
    dirt_[index(l)] = 1;
    if (r.team != NEUTRAL) dirtCount_[r.team] -= 1;
    robotAddCheese(r, -C::PLACE_DIRT_CHEESE_COST);
    addActionCooldown(r, C::DIG_COOLDOWN);
    return E_OK;
}

Err World::canRemoveDirt(Robot& r, Loc l) {
    if (Err e = assertActionReady(r)) return e;
    if (Err e = assertCanAct(r, l, buildRadius(r.type))) return e;
    if (isRat(r.type) && r.cheese + globalCheese_[r.team] < C::DIG_DIRT_CHEESE_COST) return E_REMOVE_DIRT_CHEESE;
    if (!dirt(l)) return E_NO_DIRT_THERE;
    return E_OK;
}

Err World::removeDirt(Robot& r, Loc l) {
    if (Err e = canRemoveDirt(r, l)) return e;
    dirt_[index(l)] = 0;
    if (r.team != NEUTRAL) dirtCount_[r.team] += 1;
    if (isRat(r.type)) robotAddCheese(r, -C::DIG_DIRT_CHEESE_COST);
    addActionCooldown(r, C::DIG_COOLDOWN);
    return E_OK;
}

Err World::canPlaceTrap(Robot& r, Loc l, TrapType t) {
    if (Err e = assertActionReady(r)) return e;
    if (Err e = assertCanAct(r, l, trapRadius(r.type))) return e;
    if (t == CAT_TRAP && !catTrapsAllowed(r.team)) return E_CAT_TRAP_BACKSTAB;
    if (!passable(l)) return E_TRAP_IMPASSABLE;
    if (robotAt(l)) return E_TRAP_OCCUPIED;
    if (trapGrid_[r.team][index(l)]) return E_TRAP_EXISTS;
    if (trapCounts_[t][r.team] >= TRAP[t].maxCount) return t == RAT_TRAP ? E_TRAP_MAX_RAT : E_TRAP_MAX_CAT;
    if (r.cheese + globalCheese_[r.team] < TRAP[t].buildCost) return E_TRAP_CHEESE;
    if (hasMine(l)) return E_TILE_HAS_MINE;
    return E_OK;
}

Err World::placeTrap(Robot& r, Loc l, TrapType t) {
    if (Err e = canPlaceTrap(r, l, t)) return e;
    addActionCooldown(r, TRAP[t].actionCooldown);
    robotAddCheese(r, -TRAP[t].buildCost);
    placeTrapObject(l, t, r.team);
    return E_OK;
}

Err World::canRemoveTrap(Robot& r, Loc l, TrapType t) {
    if (Err e = assertCanAct(r, l, trapRadius(r.type))) return e;
    const Trap* trap = trapGrid_[r.team][index(l)];
    if (!trap || trap->type != t) return t == RAT_TRAP ? E_NO_RAT_TRAP : E_NO_CAT_TRAP;
    return E_OK;
}

Err World::removeTrap(Robot& r, Loc l, TrapType t) {
    if (Err e = canRemoveTrap(r, l, t)) return e;
    removeTrapObject(l, r.team);
    return E_OK;
}

Err World::canPickUpCheese(Robot& r, Loc l) {
    if (Err e = assertCanAct(r, l, C::CHEESE_PICK_UP_RADIUS_SQUARED)) return e;
    if (cheeseAt(l) <= 0) return E_NO_CHEESE_THERE;
    if (!isRat(r.type)) return E_PICKUP_NOT_RAT;
    if (r.beingThrown()) return E_PICKUP_FLYING;
    return E_OK;
}

Err World::pickUpCheese(Robot& r, Loc l, int amount) {
    if (Err e = canPickUpCheese(r, l)) return e;
    int avail = cheeseAt(l);
    int take = amount == ALL_CHEESE ? avail : std::max(std::min(amount, avail), 0);
    cheese_[index(l)] -= take;
    robotAddCheese(r, take);
    cheeseCollected_[r.team] += take;
    if (r.type == RAT_KING) cheeseTransferred_[r.team] += take;
    return E_OK;
}

Err World::assertCanAttackRat(Robot& r, Loc l, int cheese) {
    if (Err e = assertActionReady(r)) return e;
    if (Err e = assertCanAct(r, l, float(r.visionRadiusSquared()))) return e;
    int distSq = r.loc.distSq(l);
    if (distSq == 0 ||
        distSq > (r.type == RAT_KING ? C::RAT_KING_ATTACK_DISTANCE_SQUARED : C::ATTACK_DISTANCE_SQUARED))
        return E_ATTACK_NOT_ADJACENT;
    if (!passable(l)) return E_ATTACK_IMPASSABLE;
    if (r.cheese + globalCheese_[r.team] < cheese) return E_BITE_CHEESE;
    if (r.type == CAT) return E_BITE_NOT_RAT;
    if (cheese < 0) return E_BITE_NEGATIVE;
    Robot* enemy = robotAt(l);
    if (!enemy) return E_ATTACK_NO_ROBOT;
    if (enemy->team == r.team) return E_ATTACK_ALLY;
    return E_OK;
}

Err World::assertCanAttackCat(Robot& r, Loc l) {
    if (Err e = assertActionReady(r)) return e;
    if (Err e = assertCanAct(r, l, float(r.visionRadiusSquared()))) return e;
    if (!passable(l)) return E_CAT_ATTACK_IMPASSABLE;
    Robot* enemy = robotAt(l);
    if (!enemy) return E_ATTACK_NO_ROBOT;
    if (enemy->team == r.team) return E_CAT_ATTACK_CAT;
    return E_OK;
}

Err World::canAttack(Robot& r, Loc l, int cheese) {
    return r.type == CAT ? assertCanAttackCat(r, l) : assertCanAttackRat(r, l, cheese);
}

Err World::attack(Robot& r, Loc l, int cheese, bool withCheese) {
    if (!withCheese) cheese = 0;
    if (Err e = canAttack(r, l, cheese)) return e;
    if (withCheese && r.cheese + globalCheese_[r.team] < cheese) return E_ATTACK_CHEESE;
    addActionCooldown(r, UNIT[r.type].actionCooldown);
    if (r.type == CAT) scratch(r, l);
    else bite(r, l, withCheese ? cheese : -1);
    return E_OK;
}

Err World::canBecomeRatKing(Robot& r) {
    if (Err e = assertActionReady(r)) return e;
    if (globalCheese_[r.team] < C::RAT_KING_UPGRADE_CHEESE_COST) return E_KING_CHEESE;
    int kings = numRatKings_[r.team];
    if (kings >= C::MAX_RAT_KINGS) return E_KING_MAX;
    if (kings >= C::MAX_RAT_KINGS_AFTER_CUTOFF && round_ > C::RAT_KING_CUTOFF_ROUND) return E_KING_CUTOFF;
    int allies = 0;
    for (int d = 0; d < 9; d++) {
        Loc l = r.loc.add(Dir(d));
        if (!onMap(l)) return E_KING_OFF_MAP;
        Robot* o = robotAt(l);
        if (o && o->team == r.team && o->type == BABY_RAT) allies++;
        if (o && o->type != BABY_RAT) return E_KING_BLOCKED;
        if (!passable(l)) return E_KING_IMPASSABLE;
    }
    if (allies < 7) return E_KING_FEW_RATS;
    return E_OK;
}

Err World::becomeRatKing(Robot& r) {
    if (Err e = canBecomeRatKing(r)) return e;
    int health = 0;
    for (int d = 0; d < 9; d++) {
        Loc l = r.loc.add(Dir(d));
        Robot* cur = robotAt(l);
        if (cur && cur->team == r.team) health += cur->health;
        if (cur && d != CENTER) {
            addTeamCheese(r.team, cur->cheese);
            robotAddCheese(*cur, -cur->cheese);
            if (cur->carrying) {
                Robot& carried = *cur->carrying;
                addTeamCheese(r.team, carried.cheese);
                robotAddCheese(carried, -carried.cheese);
                dmgSrc_ = SRC_FUSE;
                addHealth(carried, -carried.health);
            }
            dmgSrc_ = SRC_FUSE;
            addHealth(*cur, -cur->health);
        }
        grid_[index(l)] = &r;
    }
    dmgSrc_ = SRC_FUSE;
    if (r.carrying) addHealth(*r.carrying, -r.carrying->health);
    addTeamCheese(r.team, -C::RAT_KING_UPGRADE_CHEESE_COST);
    health = std::min(health, UNIT[RAT_KING].health);
    addTeamCheese(r.team, r.cheese);
    robotAddCheese(r, -r.cheese);
    r.type = RAT_KING;
    r.health = health;
    for (int d = 0; d < 8; d++) processTrapsAt(r, r.loc.add(Dir(d)));
    numRatKings_[r.team]++;
    return E_OK;
}

bool World::squeak(Robot& r, int content) {
    if (r.sentMessages >= C::MAX_MESSAGES_SENT_ROBOT) return false;
    Message m{content, r.id, round_, r.loc};
    std::vector<int> told;
    forLocationsInRadius(r.loc, C::SQUEAK_RADIUS_SQUARED, 0, [&](Loc l) {
        Robot* o = robotAt(l);
        if (o && o->id != r.id && std::find(told.begin(), told.end(), o->id) == told.end() &&
            (o->type == CAT || o->team == r.team)) {
            o->messages.push_back(m);
            told.push_back(o->id);
        }
    });
    r.sentMessages++;
    return true;
}

Err World::writeSharedArray(Robot& r, int index, int value) {
    if (r.type != RAT_KING) return E_SHARED_NOT_KING;
    if (index < 0 || index >= C::SHARED_ARRAY_SIZE) return E_SHARED_INDEX;
    if (value < 0 || value > C::COMM_ARRAY_MAX_VALUE) return E_SHARED_VALUE;
    shared_[r.team][index] = value;
    return E_OK;
}

Err World::canTransferCheese(Robot& r, Loc l, int amount) {
    if (Err e = assertCanAct(r, l, C::CHEESE_TRANSFER_RADIUS_SQUARED)) return e;
    if (Err e = assertActionReady(r)) return e;
    Robot* target = robotAt(l);
    if (!target) return E_TRANSFER_NO_ROBOT;
    if (amount == 0) return E_TRANSFER_ZERO;
    if (target->team != r.team) return E_TRANSFER_ENEMY;
    if (r.type != BABY_RAT) return E_TRANSFER_NOT_BABY;
    if (target->type != RAT_KING) return E_TRANSFER_NOT_KING;
    if (amount < 0) return E_TRANSFER_NEGATIVE;
    if (amount > r.cheese) return E_TRANSFER_TOO_MUCH;
    return E_OK;
}

Err World::transferCheese(Robot& r, Loc l, int amount) {
    if (Err e = canTransferCheese(r, l, amount)) return e;
    robotAddCheese(r, -amount);
    addTeamCheese(r.team, amount);
    cheeseTransferred_[r.team] += amount;
    addActionCooldown(r, C::CHEESE_TRANSFER_COOLDOWN);
    return E_OK;
}

Err World::canThrowRat(Robot& r) {
    if (Err e = assertActionReady(r)) return e;
    Loc next = r.loc.add(r.dir);
    if (r.type != BABY_RAT) return E_THROW_NOT_RAT;
    if (!r.carrying) return E_THROW_NOT_CARRYING;
    if (!onMap(next)) return E_THROW_OFF_MAP;
    if (!passable(next) || robotAt(next)) return E_THROW_BLOCKED;
    return E_OK;
}

Err World::throwRat(Robot& r) {
    if (Err e = canThrowRat(r)) return e;
    addActionCooldown(r, C::THROW_RAT_COOLDOWN);
    throwRobot(r);
    return E_OK;
}

Err World::canDropRat(Robot& r, Dir d) {
    if (Err e = assertActionReady(r)) return e;
    if (d == DIR_NULL) return E_NULL_ARGUMENT;
    Loc next = r.loc.add(d);
    if (r.type != BABY_RAT) return E_DROP_NOT_RAT;
    if (!r.carrying) return E_THROW_NOT_CARRYING;
    if (!onMap(next)) return E_DROP_OFF_MAP;
    if (!passable(next) || robotAt(next)) return E_DROP_BLOCKED;
    return E_OK;
}

Err World::dropRat(Robot& r, Dir d) {
    if (Err e = canDropRat(r, d)) return e;
    Robot& carried = *r.carrying;
    getDropped(carried, r.loc.add(d));
    r.carrying = nullptr;
    addActionCooldown(r, C::THROW_RAT_COOLDOWN);
    return E_OK;
}

Err World::canCarryRat(Robot& r, Loc l) {
    if (Err e = assertCanAct(r, l, 2)) return e;
    if (Err e = assertActionReady(r)) return e;
    if (r.type != BABY_RAT) return E_CARRY_NOT_THROWER;
    if (r.carrying) return E_CARRY_ALREADY;
    if (!l.isAdjacentTo(r.loc) && l != r.loc) return E_CARRY_NOT_ADJACENT;
    if (!canSenseLocation(r, l)) return E_CARRY_NOT_IN_FRONT;
    if (r.grabbedBy) return E_CARRY_WHILE_CARRIED;
    Robot* target = robotAt(l);
    if (!target) return E_CARRY_NO_ROBOT;
    if (target->type != BABY_RAT) return E_CARRY_NOT_THROWABLE;
    if (target->beingThrown()) return E_CARRY_TARGET_THROWN;
    if (target == &r) return E_CARRY_SELF;
    if (target->team != r.team && target->lastGrabberId == r.id &&
        target->turnsSinceThrownOrDropped < C::SAME_ROBOT_CARRY_COOLDOWN_TURNS)
        return E_CARRY_RECENT;
    bool canGrab = !canSenseLocation(*target, r.loc) || r.team == target->team ||
                   target->health + C::HEALTH_GRAB_THRESHOLD < r.health;
    if (!canGrab) return E_CARRY_REFUSED;
    return E_OK;
}

Err World::carryRat(Robot& r, Loc l) {
    if (Err e = canCarryRat(r, l)) return e;
    grab(r, l);
    addActionCooldown(r, C::THROW_RAT_COOLDOWN);
    return E_OK;
}

void World::resign(Robot& r) {
    Team t = r.team;
    std::vector<int> ids = execOrder_;
    for (int id : ids) {
        Robot* o = robotById(id);
        if (o && o->team == t) destroyRobot(id);
    }
    setWinner(opponent(t), WIN_RESIGNATION);
}

Err World::senseRobotById(const Robot& r, int id, Robot** out) const {
    Robot* o = robotById(id);
    if (!o || o->beingThrown() || !onMap(o->loc) || !sees(r, o->loc, SEE_ROBOTS)) return E_CANT_SENSE_ROBOT;
    *out = o;
    return E_OK;
}

Err World::senseNearbyRobots(const Robot& r, Loc center, int radiusSquared, int team,
                             std::vector<Robot*>& out) const {
    out.clear();
    if (radiusSquared < -1) return E_NEGATIVE_RADIUS;
    int actual = radiusSquared == -1 ? r.visionRadiusSquared() : std::min(radiusSquared, r.visionRadiusSquared());
    std::vector<Robot*>& sensed = scratch_;
    sensed.clear();
    forLocationsInRadius(center, actual, r.chirality, [&](Loc l) {
        Robot* o = robotAt(l);
        if (o && (team < 0 || o->team == team)) sensed.push_back(o);
    });
    for (Robot* o : sensed) {
        if (std::find(out.begin(), out.end(), o) != out.end()) continue;
        if (o == &r) continue;
        Loc parts[9];
        int n;
        partLocations(*o, parts, n);
        bool seen = false;
        for (int i = 0; i < n; i++) seen = seen || (sees(r, parts[i], SEE_ROBOTS) && center.distSq(parts[i]) <= actual);
        if (!seen) continue;
        if (team >= 0 && o->team != team) continue;
        out.push_back(o);
    }
    return E_OK;
}

Err World::senseNearbyMapInfos(const Robot& r, Loc center, int radiusSquared, std::vector<Loc>& out) const {
    out.clear();
    if (radiusSquared < -1) return E_NEGATIVE_RADIUS;
    int actual = radiusSquared == -1 ? r.visionRadiusSquared() : std::min(radiusSquared, r.visionRadiusSquared());
    forLocationsInRadius(center, actual, r.chirality, [&](Loc l) {
        if (sees(r, l, SEE_MAP)) out.push_back(l);
    });
    return E_OK;
}

Err World::allLocationsWithinRadiusSquared(const Robot& r, Loc center, int radiusSquared,
                                           std::vector<Loc>& out) const {
    return senseNearbyMapInfos(r, center, radiusSquared, out);
}

// ============================================================================
// turns and rounds
// ============================================================================

void World::processBeginningOfTurn(Robot& r) {
    r.sentMessages = 0;
    runCheeseMines();

    if (r.type == BABY_RAT && r.grabbedBy) {
        Robot& grabber = *r.grabbedBy;
        if (grabber.health <= 0) {
            getDropped(r, grabber.loc);
        } else if (r.remainingCarried == 0) {
            Loc dropLoc = grabber.loc.add(r.dir);
            if (onMap(dropLoc) && passable(dropLoc) && !robotAt(dropLoc)) {
                getDropped(r, dropLoc);
                grabber.carrying = nullptr;
            } else {
                swapGrabber(r);
            }
        } else {
            r.loc = grabber.loc;
            r.remainingCarried -= 1;
        }
    }

    if (r.type == BABY_RAT && r.beingThrown()) {
        r.remainingThrow -= 1;
        if (r.remainingThrow == 0) {
            hitGround(r);
        } else {
            bool traveled = size_t(r.id) < traveled_.size() && traveled_[r.id];
            if (!traveled) {
                travelFlying(r, false);
                travelFlying(r, true);
            }
            if (r.remainingThrow == 1) hitGround(r);
        }
    }

    bool sleepingCat = r.type == CAT && r.sleepTimeRemaining > 0;
    if (!r.grabbedBy && !r.beingThrown() && !sleepingCat) {
        r.actionCd = std::max(0, r.actionCd - C::COOLDOWNS_PER_TURN);
        r.turnCd = std::max(0, r.turnCd - C::COOLDOWNS_PER_TURN);
        r.moveCd = std::max(0, r.moveCd - C::COOLDOWNS_PER_TURN);
    }
    r.bytecodeLimit = UNIT[r.type].bytecodeLimit;
}

void World::catTurn(Robot& r) {
    static constexpr Dir nonCenter[8] = {WEST, NORTHWEST, NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST};
    auto randomDir = [&] { return nonCenter[catRand_.nextInt(8)]; };
    auto towards = [&](Loc target) {
        r.dir = bfsDir(catCorner(r), target, r.chirality);
        if (r.dir == DIR_NULL || r.dir == CENTER) {
            r.dir = catCorner(r).directionTo(target);
            if (r.dir == CENTER) r.dir = randomDir();
        }
    };
    if (r.dir == DIR_NULL || r.dir == CENTER) r.dir = randomDir();

    switch (r.catState) {
        case CAT_EXPLORE: {
            if (r.catTurnsStuck >= 4) {
                Dir random = randomDir();
                if (canTurnNow(r) == E_OK) turn(r, random);
            } else if (r.catTurnsStuck == 0) {
                Loc waypoint = r.waypoints[r.currentWaypoint];
                if (catCorner(r) == waypoint) {
                    if (r.currentWaypoint == r.previousWaypoint) {
                        r.currentWaypoint = (r.currentWaypoint + 1) % int(r.waypoints.size());
                    } else {
                        r.previousWaypoint = r.currentWaypoint;
                        r.catState = CAT_ATTACK;
                        break;
                    }
                }
                r.hasCatTargetLoc = true;
                r.catTargetLoc = r.waypoints[r.currentWaypoint];
                towards(r.catTargetLoc);
            }

            if (canMove(r, r.dir) == E_OK) {
                move(r, r.dir);
                r.catTurnsStuck = 0;
            } else {
                bool stuck = true;
                Loc parts[4];
                catPartLocations(r, parts);
                for (Loc p : parts) {
                    Loc next = p.add(r.dir);
                    if (canRemoveDirt(r, next) == E_OK) {
                        removeDirt(r, next);
                        addActionCooldown(r, C::CAT_DIG_ADDITIONAL_COOLDOWN);
                        stuck = false;
                    } else if (canAttack(r, next, 0) == E_OK) {
                        attack(r, next, 0, false);
                        stuck = false;
                    }
                }
                if (stuck) {
                    Loc twoAway = catCorner(r).add(r.dir).add(r.dir);
                    int dx, dy;
                    bool ok = canPounce(r, twoAway, dx, dy);
                    if (r.canMoveCd() && ok) {
                        pounce(r, dx, dy);
                        stuck = false;
                    }
                }
                r.catTurnsStuck = stuck ? r.catTurnsStuck + 1 : 0;
            }
            break;
        }
        case CAT_ATTACK: {
            r.catTurns += 1;
            if (r.catTurns > 8) {
                r.catTurns = 0;
                r.catState = CAT_EXPLORE;
                break;
            }
            bool hasSqueak = !r.messages.empty();
            Message squeak = hasSqueak ? r.messages.front() : Message{};
            r.messages.clear();
            std::vector<Robot*> nearby;
            senseNearbyRobots(r, r.loc, -1, -1, nearby);
            if (hasSqueak && r.loc.directionTo(squeak.source) != CENTER) r.dir = r.loc.directionTo(squeak.source);

            bool sensed = false;
            if (r.hasCatTarget) {
                for (Robot* o : nearby)
                    if (o->id == r.catTargetId) {
                        sensed = true;
                        r.hasCatTargetLoc = true;
                        r.catTargetLoc = r.catTargetSnapshotLoc;
                        break;
                    }
            }
            if (!sensed) {
                r.hasCatTarget = false;
                r.hasCatTargetLoc = false;
                for (Robot* o : nearby)
                    if (isRat(o->type)) {
                        sensed = true;
                        r.hasCatTarget = true;
                        r.catTargetId = o->id;
                        r.catTargetSnapshotLoc = o->loc;
                        r.hasCatTargetLoc = true;
                        r.catTargetLoc = o->loc;
                    }
            }
            if (r.hasCatTargetLoc) {
                if (canAttack(r, r.catTargetSnapshotLoc, 0) == E_OK) {
                    attack(r, r.catTargetSnapshotLoc, 0, false);
                    break;
                }
                int dx, dy;
                bool ok = canPounce(r, r.catTargetLoc, dx, dy);
                if (r.canMoveCd() && ok) {
                    pounce(r, dx, dy);
                    break;
                }
                towards(r.catTargetLoc);
                if (canMove(r, r.dir) == E_OK) {
                    move(r, r.dir);
                    r.catTurnsStuck = 0;
                    break;
                }
                Loc parts[4];
                catPartLocations(r, parts);
                for (Loc p : parts) {
                    Loc next = p.add(r.dir);
                    if (canRemoveDirt(r, next) == E_OK) {
                        removeDirt(r, next);
                        addActionCooldown(r, C::CAT_DIG_ADDITIONAL_COOLDOWN);
                        break;
                    }
                }
            }
            if (canTurnNow(r) == E_OK) turn(r, r.chirality == 0 ? rotateRight(r.dir) : rotateLeft(r.dir));
            break;
        }
    }
}

void World::processEndOfTurn(Robot& r) {
    if (!r.grabbedBy && !r.beingThrown()) r.turnsSinceThrownOrDropped += 1;
    if (r.type == RAT_KING && numRatKings_[r.team] > 0) {
        dmgSrc_ = SRC_STARVE;
        if (globalCheese_[r.team] < C::RAT_KING_CHEESE_CONSUMPTION) addHealth(r, -C::RAT_KING_HEALTH_LOSS);
        else robotAddCheese(r, -C::RAT_KING_CHEESE_CONSUMPTION);
    }
    if (r.type == CAT && r.sleepTimeRemaining > 0) r.sleepTimeRemaining -= 1;
    else if (r.type == CAT) catTurn(r);
    r.roundsAlive++;
}

void World::updateRobot(Robot& r) {
    processBeginningOfTurn(r);
    bool player = r.team != NEUTRAL && host_;
    if (player) host_->runTurn(r);
    r.bytecodesUsed = player ? host_->bytecodesUsed(r) : 0;
    processEndOfTurn(r);
    if (player && host_->terminated(r) && r.alive) destroyRobot(r.id);
}

bool World::runRound() {
    if (!running_) return false;
    try {
        round_++;
        for (int id : execOrder_) {
            Robot& r = *byId_[id];
            while (!r.messages.empty() && r.messages.front().round <= round_ - C::MESSAGE_ROUND_DURATION)
                r.messages.pop_front();
        }
        std::vector<int> order = execOrder_;
        for (int id : order) {
            Robot* r = robotById(id);
            if (r) updateRobot(*r);
        }
        processEndOfRound();
    } catch (const std::exception& e) {
        engineError_ = e.what();
        if (!hasWinner_) winReason_ = WIN_ENGINE_ERROR;
        running_ = false;
        return false;
    }
    return running_;
}

void World::runToEnd() {
    while (runRound()) {
    }
}

void World::processEndOfRound() {
    ranMinesThisRound_ = false;
    for (int id : traveledList_)
        if (size_t(id) < traveled_.size()) traveled_[id] = 0;
    traveledList_.clear();
    checkEndOfMatch();
    if (hasWinner_) running_ = false;
}

// ============================================================================
// winning
// ============================================================================

void World::setWinner(Team t, WinReason why) {
    hasWinner_ = true;
    winner_ = t;
    winReason_ = why;
}

bool World::setWinnerIfKilledAllRatKings() {
    if (numRatKings_[TEAM_A] == 0) {
        setWinner(TEAM_B, WIN_KILL_ALL_RAT_KINGS);
        return true;
    }
    if (numRatKings_[TEAM_B] == 0) {
        setWinner(TEAM_A, WIN_KILL_ALL_RAT_KINGS);
        return true;
    }
    return false;
}

bool World::setWinnerIfMorePoints() {
    double catW, kingW, cheeseW = 0.2;
    if (cooperation_) {
        catW = 0.5;
        kingW = 0.3;
    } else {
        catW = 0.3;
        kingW = 0.5;
    }
    int kings = numRatKings_[TEAM_A] + numRatKings_[TEAM_B];
    int moved = cheeseTransferred_[TEAM_A] + cheeseTransferred_[TEAM_B];
    int dmg = damageToCats_[TEAM_A] + damageToCats_[TEAM_B];
    int pts[2];
    for (int t = 0; t < 2; t++) {
        float pk = kings != 0 ? float(numRatKings_[t]) / kings : 0.0f;
        float pc = moved != 0 ? float(cheeseTransferred_[t]) / moved : 0.0f;
        float pd = dmg != 0 ? float(damageToCats_[t]) / dmg : 0.0f;
        pts[t] = int(catW * 100 * pd + kingW * 100 * pk + cheeseW * 100 * pc);
        points_[t] += pts[t];
    }
    if (pts[0] > pts[1]) {
        setWinner(TEAM_A, WIN_MORE_POINTS);
        return true;
    }
    if (pts[0] < pts[1]) {
        setWinner(TEAM_B, WIN_MORE_POINTS);
        return true;
    }
    return false;
}

bool World::setWinnerIfMoreCheese() {
    if (globalCheese_[TEAM_A] > globalCheese_[TEAM_B]) {
        setWinner(TEAM_A, WIN_MORE_CHEESE);
        return true;
    }
    if (globalCheese_[TEAM_B] > globalCheese_[TEAM_A]) {
        setWinner(TEAM_B, WIN_MORE_CHEESE);
        return true;
    }
    return false;
}

bool World::setWinnerIfMoreRatsAlive() {
    int a = numBabyRats_[TEAM_A] + numRatKings_[TEAM_A], b = numBabyRats_[TEAM_B] + numRatKings_[TEAM_B];
    if (a > b) {
        setWinner(TEAM_A, WIN_MORE_ROBOTS_ALIVE);
        return true;
    }
    if (b > a) {
        setWinner(TEAM_B, WIN_MORE_ROBOTS_ALIVE);
        return true;
    }
    return false;
}

void World::setWinnerArbitrary() { setWinner(arbitraryRand_.nextDouble() < 0.5 ? TEAM_A : TEAM_B, WIN_ARBITRARY); }

bool World::setWinnerIfAllCatsDead() {
    if (numCats_ == 0 && cooperation_) {
        if (setWinnerIfMorePoints()) return true;
        if (setWinnerIfMoreCheese()) return true;
        if (setWinnerIfMoreRatsAlive()) return true;
        setWinnerArbitrary();
        return true;
    }
    return false;
}

void World::checkEndOfMatch() {
    if (round_ >= C::MAX_ROUNDS && !hasWinner_) {
        if (setWinnerIfMorePoints()) return;
        if (setWinnerIfMoreCheese()) return;
        if (setWinnerIfMoreRatsAlive()) return;
        setWinnerArbitrary();
    }
}

void World::checkWin() {
    if (hasWinner_ && winReason_ == WIN_KILL_ALL_RAT_KINGS && setWinnerIfKilledAllRatKings()) {
        if (!setWinnerIfMorePoints() && !setWinnerIfMoreCheese() && !setWinnerIfMoreRatsAlive()) setWinnerArbitrary();
    }
    if (hasWinner_) return;
    if (setWinnerIfKilledAllRatKings()) return;
    if (setWinnerIfAllCatsDead()) return;
    throw EngineError("Reporting incorrect win");
}

// ============================================================================
// verification
// ============================================================================

uint64_t World::stateHash() const {
    uint64_t h = 0xcbf29ce484222325ULL;
    auto mix = [&](int64_t v) {
        h ^= uint64_t(v);
        h *= 1099511628211ULL;
    };
    std::vector<const Robot*> rs;
    for (int id : execOrder_) rs.push_back(byId_[id]);
    std::sort(rs.begin(), rs.end(), [](const Robot* a, const Robot* b) { return a->id < b->id; });
    for (const Robot* r : rs) {
        for (int64_t v : {int64_t(r->id), int64_t(r->team), int64_t(r->type), int64_t(r->loc.x), int64_t(r->loc.y),
                          int64_t(r->dir), int64_t(r->health), int64_t(r->cheese), int64_t(r->actionCd),
                          int64_t(r->moveCd), int64_t(r->turnCd)})
            mix(v);
    }
    for (int t = 0; t < 2; t++)
        for (int64_t v : {int64_t(globalCheese_[t]), int64_t(dirtCount_[t]), int64_t(cheeseTransferred_[t]),
                          int64_t(damageToCats_[t]), int64_t(numBabyRats_[t]), int64_t(numRatKings_[t])})
            mix(v);
    int64_t cheese = 0, dirt = 0;
    for (size_t i = 0; i < cheese_.size(); i++) {
        cheese += cheese_[i] * int64_t(i + 1);
        dirt += dirt_[i] * int64_t(i + 1);
    }
    mix(cheese);
    mix(dirt);
    mix(cooperation_);
    return h;
}

std::string World::stateDump() const {
    std::ostringstream o;
    std::vector<const Robot*> rs;
    for (int id : execOrder_) rs.push_back(byId_[id]);
    std::sort(rs.begin(), rs.end(), [](const Robot* a, const Robot* b) { return a->id < b->id; });
    for (const Robot* r : rs)
        o << "  robot " << r->id << " t" << int(r->team) << " u" << int(r->type) << " @" << r->loc.x << ","
          << r->loc.y << " d" << int(r->dir) << " hp" << r->health << " ch" << r->cheese << " cd" << r->actionCd
          << "/" << r->moveCd << "/" << r->turnCd << "\n";
    for (int t = 0; t < 2; t++)
        o << "  team " << t << " cheese " << globalCheese_[t] << " dirt " << dirtCount_[t] << " moved "
          << cheeseTransferred_[t] << " catdmg " << damageToCats_[t] << " babies " << numBabyRats_[t] << " kings "
          << numRatKings_[t] << " traps " << trapCounts_[RAT_TRAP][t] << "/" << trapCounts_[CAT_TRAP][t] << "\n";
    int64_t cheese = 0, dirt = 0;
    for (size_t i = 0; i < cheese_.size(); i++) {
        cheese += cheese_[i] * int64_t(i + 1);
        dirt += dirt_[i] * int64_t(i + 1);
    }
    o << "  map cheese " << cheese << " dirt " << dirt << " coop " << cooperation_ << "\n";
    return o.str();
}

}  // namespace fish
