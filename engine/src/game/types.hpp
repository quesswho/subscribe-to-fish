// Core value types of Battlecode 2026, mirroring battlecode.common.
#pragma once

#include <cmath>
#include <cstdint>
#include <cstdlib>

namespace fish {

// Ordinals match battlecode.common.Direction.
enum Dir : int8_t { NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST, CENTER, DIR_NULL = -1 };

constexpr int DX[9] = {0, 1, 1, 1, 0, -1, -1, -1, 0};
constexpr int DY[9] = {1, 1, 0, -1, -1, -1, 0, 1, 0};

inline Dir opposite(Dir d) { return d >= 8 ? d : Dir((d + 4) % 8); }
inline Dir rotateLeft(Dir d) { return d >= 8 ? d : Dir((d + 7) % 8); }
inline Dir rotateRight(Dir d) { return d >= 8 ? d : Dir((d + 1) % 8); }

inline Dir dirFromDelta(int dx, int dy) {
    for (int d = 0; d < 9; d++)
        if (DX[d] == dx && DY[d] == dy) return Dir(d);
    return DIR_NULL;
}

enum Team : int8_t { TEAM_A, TEAM_B, NEUTRAL };
inline Team opponent(Team t) { return t == TEAM_A ? TEAM_B : t == TEAM_B ? TEAM_A : NEUTRAL; }

enum UnitType : int8_t { BABY_RAT, RAT_KING, CAT };

struct UnitStats {
    int health, size, visionRadiusSquared, visionAngle, actionCooldown, movementCooldown, bytecodeLimit;
};
constexpr UnitStats UNIT[3] = {
    {100, 1, 20, 90, 10, 10, 17500},
    {600, 3, 25, 360, 10, 40, 20000},
    {4000, 2, 17, 180, 30, 20, 17500},
};
inline bool usesBottomLeft(UnitType t) { return UNIT[t].size % 2 == 0; }

enum TrapType : int8_t { RAT_TRAP, CAT_TRAP, TRAP_NONE };
struct TrapStats {
    int buildCost, damage, stunTime, actionCooldown, maxCount, triggerRadiusSquared;
};
constexpr TrapStats TRAP[3] = {
    {20, 50, 30, 15, 25, 2},
    {10, 100, 20, 10, 10, 2},
    {0, 0, 0, 0, 0, 0},
};

// Ordinals match battlecode.world.MapSymmetry.
enum Symmetry : int8_t { ROTATIONAL, HORIZONTAL, VERTICAL };

struct Loc {
    int x = 0, y = 0;
    bool operator==(const Loc& o) const { return x == o.x && y == o.y; }
    bool operator!=(const Loc& o) const { return !(*this == o); }
    Loc add(Dir d) const { return {x + DX[d], y + DY[d]}; }
    Loc translate(int dx, int dy) const { return {x + dx, y + dy}; }
    int distSq(Loc o) const {
        int dx = x - o.x, dy = y - o.y;
        return dx * dx + dy * dy;
    }
    float bottomLeftDistSq(Loc o) const {
        double dx = x + 0.5 - o.x, dy = y + 0.5 - o.y;
        return (float)(dx * dx + dy * dy);
    }
    bool isAdjacentTo(Loc o) const { return std::abs(x - o.x) <= 1 && std::abs(y - o.y) <= 1; }

    Dir directionTo(Loc o) const {
        double dx = o.x - x, dy = o.y - y;
        if (std::fabs(dx) >= 2.414 * std::fabs(dy)) {
            if (dx > 0) return EAST;
            if (dx < 0) return WEST;
            return CENTER;
        } else if (std::fabs(dy) >= 2.414 * std::fabs(dx)) {
            return dy > 0 ? NORTH : SOUTH;
        } else if (dy > 0) {
            return dx > 0 ? NORTHEAST : NORTHWEST;
        } else {
            return dx > 0 ? SOUTHEAST : SOUTHWEST;
        }
    }

    // MapLocation.isWithinDistanceSquared(loc, distSq, facing, theta, useBottomLeft), bit for bit.
    bool withinCone(Loc o, int distanceSquared, Dir facing, double theta, bool bottomLeft) const {
        if (*this == o) return true;
        bool validDistance = bottomLeft ? bottomLeftDistSq(o) <= distanceSquared : distSq(o) <= distanceSquared;
        double dx = o.x - (bottomLeft ? (x + 0.5) : x);
        double dy = o.y - (bottomLeft ? (y + 0.5) : y);
        bool validAngle;
        if (facing == CENTER) {
            validAngle = true;
        } else {
            double fdx = DX[facing], fdy = DY[facing];
            double cosSim = (fdx * dx + fdy * dy) / std::sqrt((dx * dx + dy * dy) * (fdx * fdx + fdy * fdy));
            double halfAngle = std::fabs(std::acos(cosSim)) * 57.29577951308232;
            validAngle = halfAngle - 1e-3 <= theta / 2;
        }
        return validDistance && validAngle;
    }
};

namespace C {
constexpr int MAX_ROUNDS = 2000;
constexpr int INITIAL_TEAM_CHEESE = 2500;
constexpr int CAT_TRAP_ROUNDS_AFTER_BACKSTAB = 100;
constexpr int MAX_RAT_KINGS = 5;
constexpr int MAX_RAT_KINGS_AFTER_CUTOFF = 2;
constexpr int RAT_KING_CUTOFF_ROUND = 1200;
constexpr int MOVE_STRAFE_COOLDOWN = 18;
constexpr double CHEESE_COOLDOWN_PENALTY = 0.01;
constexpr int RAT_KING_CHEESE_CONSUMPTION = 2;
constexpr int RAT_KING_HEALTH_LOSS = 10;
constexpr float CHEESE_MINE_SPAWN_PROBABILITY = 0.01f;
constexpr int SQ_CHEESE_SPAWN_RADIUS = 4;
constexpr int CHEESE_SPAWN_AMOUNT = 20;
constexpr int CHEESE_TRANSFER_RADIUS_SQUARED = 9;
constexpr int CHEESE_PICK_UP_RADIUS_SQUARED = 2;
constexpr int BUILD_ROBOT_RADIUS_SQUARED = 8;
constexpr int BUILD_ROBOT_BASE_COST = 10;
constexpr int BUILD_ROBOT_COST_INCREASE = 10;
constexpr int NUM_ROBOTS_FOR_COST_INCREASE = 4;
constexpr int BUILD_DISTANCE_SQUARED = 2;
constexpr float CAT_BUILD_DISTANCE_SQUARED = 4.5f;
constexpr int RAT_KING_BUILD_DISTANCE_SQUARED = 8;
constexpr int ATTACK_DISTANCE_SQUARED = 2;
constexpr int RAT_KING_ATTACK_DISTANCE_SQUARED = 8;
constexpr int MESSAGE_ROUND_DURATION = 5;
constexpr int MAX_MESSAGES_SENT_ROBOT = 1;
constexpr int SQUEAK_RADIUS_SQUARED = 16;
constexpr int THROW_DAMAGE = 10;
constexpr int THROW_DAMAGE_PER_TILE = 4;
constexpr int TILES_FLOWN_PER_TURN = 2;
constexpr int RAT_BITE_DAMAGE = 10;
constexpr int CAT_SCRATCH_DAMAGE = 20;
constexpr int CAT_POUNCE_MAX_DISTANCE_SQUARED = 13;
constexpr int CAT_DIG_ADDITIONAL_COOLDOWN = 5;
constexpr int HEALTH_GRAB_THRESHOLD = 0;
constexpr int RAT_KING_UPGRADE_CHEESE_COST = 50;
constexpr int DIG_DIRT_CHEESE_COST = 5;
constexpr int PLACE_DIRT_CHEESE_COST = 0;
constexpr int SHARED_ARRAY_SIZE = 64;
constexpr int COMM_ARRAY_MAX_VALUE = 1023;
constexpr int COOLDOWN_LIMIT = 10;
constexpr int COOLDOWNS_PER_TURN = 10;
constexpr int TURNING_COOLDOWN = 10;
constexpr int BUILD_ROBOT_COOLDOWN = 10;
constexpr int CHEESE_TRANSFER_COOLDOWN = 10;
constexpr int DIG_COOLDOWN = 25;
constexpr double CARRY_COOLDOWN_MULTIPLIER = 1.5;
constexpr int MAX_CARRY_DURATION = 10;
constexpr int SAME_ROBOT_CARRY_COOLDOWN_TURNS = 2;
constexpr int THROW_RAT_COOLDOWN = 20;
constexpr int HIT_GROUND_COOLDOWN = 10;
constexpr int HIT_TARGET_COOLDOWN = 20;
constexpr int CAT_SLEEP_TIME = 2;
constexpr long long MAX_TEAM_EXECUTION_TIME_NS = 1200000000000LL;
}  // namespace C

}  // namespace fish
