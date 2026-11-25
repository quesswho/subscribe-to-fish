// Every GameActionException RobotControllerImpl can throw, as (type, message).
// The Java side reads this table once (Host.errorTable) and throws the matching exception.
#pragma once

#include <stdexcept>
#include <string>

namespace fish {

#define FISH_ERRORS(X)                                                                                              \
    X(OK, INTERNAL_ERROR, "")                                                                                     \
    X(SENSE_OFF_MAP, CANT_SENSE_THAT, "Target location is not on the map")                                          \
    X(SENSE_OUT_OF_VISION, CANT_SENSE_THAT, "Target location not within vision range")                              \
    X(ACT_OUT_OF_RANGE, OUT_OF_RANGE, "Target location not within action range")                                    \
    X(ACTION_COOLDOWN, IS_NOT_READY, "This robot's action cooldown has not expired.")                               \
    X(MOVEMENT_COOLDOWN, IS_NOT_READY, "This robot's movement cooldown has not expired.")                           \
    X(TURNING_COOLDOWN, IS_NOT_READY, "This robot's turning cooldown has not expired.")                             \
    X(BEING_THROWN, IS_NOT_READY, "This robot is currently being thrown!")                                         \
    X(BEING_CARRIED, IS_NOT_READY, "This robot is currently being carried!")                                       \
    X(NO_DIRT_STASH, CANT_DO_THAT, "No dirt available to place!")                                                   \
    X(PLACE_DIRT_CHEESE, CANT_DO_THAT, "Insufficient cheese to place dirt!")                                        \
    X(DIRT_ON_WALL, CANT_DO_THAT, "Can't place dirt on a wall!")                                                    \
    X(DIRT_ON_ROBOT, CANT_DO_THAT, "Can't place dirt on an occupied tile!")                                         \
    X(DIRT_ON_DIRT, CANT_DO_THAT, "Tile already has dirt!")                                                         \
    X(TILE_HAS_MINE, CANT_DO_THAT, "Tile has a cheese mine!")                                                       \
    X(REMOVE_DIRT_CHEESE, CANT_DO_THAT, "Insufficient cheese to remove dirt!")                                      \
    X(NO_DIRT_THERE, CANT_DO_THAT, "No dirt to remove at that location!")                                           \
    X(NO_RAT_TRAP, CANT_DO_THAT, "No rat trap to remove at that location!")                                         \
    X(NO_CAT_TRAP, CANT_DO_THAT, "No cat trap to remove at that location!")                                         \
    X(CAT_TRAP_BACKSTAB, CANT_DO_THAT,                                                                              \
      "Can't place new cat traps in backstabbing mode unless you were backstabbed and within 100 rounds!")           \
    X(TRAP_IMPASSABLE, CANT_DO_THAT, "Can't place trap on a wall or dirt!")                                         \
    X(TRAP_OCCUPIED, CANT_DO_THAT, "Can't place trap on an occupied tile!")                                         \
    X(TRAP_EXISTS, CANT_DO_THAT, "Tile already has a trap!")                                                        \
    X(TRAP_MAX_RAT, CANT_DO_THAT, "Team has reached maximum number of RAT_TRAP traps on the map!")                  \
    X(TRAP_MAX_CAT, CANT_DO_THAT, "Team has reached maximum number of CAT_TRAP traps on the map!")                  \
    X(TRAP_CHEESE, CANT_DO_THAT, "Not enough cheese to build trap!")                                                \
    X(NO_CHEESE_THERE, CANT_DO_THAT, "No cheese at this location!")                                                 \
    X(PICKUP_NOT_RAT, CANT_DO_THAT, "Only rats can pick up cheese")                                                 \
    X(PICKUP_FLYING, CANT_DO_THAT, "Flying rats cannot pick up cheese")                                             \
    X(NEGATIVE_RADIUS, CANT_DO_THAT, "The radius for a sense command can't be negative and not -1")                 \
    X(CANT_SENSE_ROBOT, CANT_SENSE_THAT,                                                                            \
      "Can't sense given robot; It may be out of vision range or not exist anymore")                                \
    X(MOVE_OFF_MAP, OUT_OF_RANGE, "Can only move to locations on the map")                                          \
    X(MOVE_OCCUPIED, CANT_MOVE_THERE, "Cannot move to an occupied location")                                        \
    X(MOVE_IMPASSABLE, CANT_MOVE_THERE, "Cannot move to an impassable location")                                    \
    X(TURN_NULL, CANT_DO_THAT, "Direction to turn to is null!")                                                     \
    X(TURN_CENTER, CANT_DO_THAT, "Cannot turn to CENTER direction!")                                                \
    X(SPAWN_NOT_KING, CANT_DO_THAT, "Only rat kings can spawn other rats!")                                         \
    X(SPAWN_CHEESE, CANT_DO_THAT, "Not enough cheese to build new rat!")                                            \
    X(SPAWN_OCCUPIED, CANT_DO_THAT, "Location is already occupied!")                                                \
    X(SPAWN_IMPASSABLE, CANT_DO_THAT, "Location has dirt or a wall!")                                               \
    X(ATTACK_NULL, CANT_DO_THAT, "Robot units must specify a location to attack")                                   \
    X(ATTACK_NOT_ADJACENT, CANT_DO_THAT, "Rats can only attack adjacent squares!")                                  \
    X(ATTACK_IMPASSABLE, CANT_DO_THAT, "Rats cannot attack squares with walls or dirt on them!")                    \
    X(BITE_CHEESE, CANT_DO_THAT, "Not enough cheese to bite!")                                                      \
    X(BITE_NOT_RAT, CANT_DO_THAT, "Unit must be a baby rat or rat king to bite!")                                   \
    X(BITE_NEGATIVE, CANT_DO_THAT, "Cheese consumed must be non-negative!")                                         \
    X(ATTACK_NO_ROBOT, CANT_DO_THAT, "No robot to attack at the specified location!")                               \
    X(ATTACK_ALLY, CANT_DO_THAT, "Cannot attack ally robots!")                                                      \
    X(CAT_ATTACK_IMPASSABLE, CANT_DO_THAT, "Cats cannot attack squares with walls or dirt on them!")                \
    X(CAT_ATTACK_CAT, CANT_DO_THAT, "Cannot attack another cat!")                                                   \
    X(ATTACK_CHEESE, CANT_DO_THAT, "Not enough cheese to attack!")                                                  \
    X(KING_CHEESE, CANT_DO_THAT, "Not enough cheese to upgrade to a rat king")                                      \
    X(KING_MAX, CANT_DO_THAT, "Cannot have more than 5 rat kings per team!")                                        \
    X(KING_CUTOFF, CANT_DO_THAT,                                                                                    \
      "Cannot make a new rat king when your team has at least 2 rat kings after round 1200!")                      \
    X(KING_OFF_MAP, CANT_DO_THAT, "Can't become a rat king when the 3x3 vicinity goes off the map!")                \
    X(KING_BLOCKED, CANT_DO_THAT, "Can't become a rat king when there are nearby cats or rat kings!")               \
    X(KING_IMPASSABLE, CANT_DO_THAT, "Can only upgrade if all squares in the 3x3 vicinity are passable")            \
    X(KING_FEW_RATS, CANT_DO_THAT, "Not enough rats in the 3x3 square")                                             \
    X(SHARED_NOT_KING, CANT_DO_THAT, "Only rat kings can write to the shared array!")                               \
    X(SHARED_INDEX, CANT_DO_THAT, "Index is out of bounds for the shared array!")                                   \
    X(SHARED_VALUE, CANT_DO_THAT, "Value is out of bounds for the shared array!")                                   \
    X(TRANSFER_NO_ROBOT, CANT_DO_THAT, "There is no robot at this location!")                                       \
    X(TRANSFER_SELF, CANT_DO_THAT, "Cannot transfer cheese to yourself!")                                           \
    X(TRANSFER_ZERO, CANT_DO_THAT, "Cannot transfer zero cheese!")                                                  \
    X(TRANSFER_ENEMY, CANT_DO_THAT, "Cannot transfer resources to the enemy team!")                                 \
    X(TRANSFER_NOT_BABY, CANT_DO_THAT, "Only baby rats can transfer cheese!")                                       \
    X(TRANSFER_NOT_KING, CANT_DO_THAT, "Only rat kings can receive cheese!")                                        \
    X(TRANSFER_NEGATIVE, CANT_DO_THAT, "Cheese can only be given, not taken!")                                      \
    X(TRANSFER_TOO_MUCH, CANT_DO_THAT, "Cannot give more raw cheese than you currently have!")                      \
    X(THROW_NOT_RAT, CANT_DO_THAT, "Only rats can throw other rats!")                                               \
    X(THROW_NOT_CARRYING, CANT_DO_THAT, "This rat is not carrying any rat!")                                        \
    X(THROW_OFF_MAP, CANT_DO_THAT, "Cannot throw outside of map!")                                                  \
    X(THROW_BLOCKED, CANT_DO_THAT, "There must be at least 1 empty space in front the throwing rat!")               \
    X(DROP_NOT_RAT, CANT_DO_THAT, "Only rats can drop other rats!")                                                 \
    X(DROP_OFF_MAP, CANT_DO_THAT, "Cannot drop outside of map!")                                                    \
    X(DROP_BLOCKED, CANT_DO_THAT, "Can only drop rats into empty spaces!")                                          \
    X(CARRY_NOT_THROWER, CANT_DO_THAT, "Unit must be a rat to grab other rats")                                     \
    X(CARRY_ALREADY, CANT_DO_THAT, "Already carrying a rat")                                                        \
    X(CARRY_NOT_RAT, CANT_DO_THAT, "Only rats can grab other rats!")                                                \
    X(CARRY_NOT_ADJACENT, CANT_DO_THAT, "A rat can only grab adjacent robots!")                                     \
    X(CARRY_NOT_IN_FRONT, CANT_DO_THAT, "A rat can only grab robots in front of it")                                \
    X(CARRY_WHILE_CARRIED, CANT_DO_THAT, "Cannot grab while being carried")                                         \
    X(CARRY_NO_ROBOT, CANT_DO_THAT, "No robot at target location")                                                  \
    X(CARRY_NOT_THROWABLE, CANT_DO_THAT, "Target robot is not throwable")                                           \
    X(CARRY_TARGET_THROWN, CANT_DO_THAT, "Target robot is currently being thrown")                                  \
    X(CARRY_SELF, CANT_DO_THAT, "Robots cannot grab themselves")                                                    \
    X(CARRY_RECENT, CANT_DO_THAT, "Target robot (on the enemy team) was recently carried by this robot")            \
    X(CARRY_REFUSED, CANT_DO_THAT, "Cannot grab that robot")                                                        \
    X(INDICATOR_DOT_OFF_MAP, CANT_DO_THAT, "Indicator dots should have map locations on the map!")                  \
    X(INDICATOR_LINE_OFF_MAP, CANT_DO_THAT, "Indicator lines should have map locations on the map!")           \
    X(NULL_ARGUMENT, INTERNAL_ERROR, "Argument has an invalid null value")                                         \
    X(CHEESE_ERROR, INTERNAL_ERROR, "Invalid cheese change")                                                       \
    X(ENGINE_ERROR, INTERNAL_ERROR, "")

enum Err : int {
#define FISH_ERR_ENUM(name, type, msg) E_##name,
    FISH_ERRORS(FISH_ERR_ENUM)
#undef FISH_ERR_ENUM
        E_COUNT
};

// TeamInfo.addCheese's IllegalArgumentException; it reaches the bot as an unchecked exception.
struct CheeseError : std::runtime_error {
    CheeseError() : std::runtime_error("Invalid cheese change") {}
};

// A RuntimeException inside the official engine: it aborts the whole game (GameWorld.runRound).
struct EngineError : std::runtime_error {
    using std::runtime_error::runtime_error;
};

}  // namespace fish
