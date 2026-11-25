package awubot.robot.rat.combat.moves;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;

import awubot.CellTracker;
import awubot.RobotTracker;
import awubot.robot.rat.combat.CombatCommon;
import awubot.robot.rat.combat.CombatStrategy;
import awubot.robot.rat.combat.CombatTracker;

import static awubot.Game.*;
import static awubot.robot.rat.BabyRat.turn;

/*
    Throws of a carried enemy, scored by the damage the engine will deal.

    A thrown rat flies along our facing: tiles 1 and 2 this round, 3-4 the next, 5-6 the one after, then lands for 10.
    Hitting a wall, dirt or a robot on tile k deals 10 + 4 * (2 * flightTurnsLeft + (first step of the round ? 1 : 0)),
    that is 42, 38, 34, 30, 26 for k = 2..6, to the thrown rat and to the robot hit; flying into a cat kills it. Only
    tiles 1-2 are certain (they resolve now; later tiles can change before the rat gets there), so we throw when a hit
    lands on tile 2, and otherwise hold the enemy (it cannot act while carried) and walk or turn to where such a throw
    exists, facing it for next turn. When the carry is about to run out or we are in danger, the best throw goes now.
*/
public final class ThrowPlanner {
    private ThrowPlanner() {
    }

    static int HOLD_TURNS = 8;        // throw anyway once we have carried this long (the carry lasts 10)
    static int DANGER_HP = 30;        // throw anyway at or below this health
    static double ALLY_HIT = 1.0;     // damage to an ally counts this much against the throw
    static double KILL_BONUS = 20;    // a throw that kills the carried rat
    static double SURE_HIT = 42;      // the damage a tile-2 hit deals
    public static boolean HOLD = false;
    public static boolean AIM = true;     // walk or turn to aim when no sure throw exists
    public static boolean MUST = false;    // throw the best direction anyway when the carry runs out or in danger   // when no aim exists, keep holding instead of handing over to TryThrow
    /** Set by run(): it chose to keep holding (no sure throw, no need to throw yet). */
    public static boolean holding;

    private static final Direction[] DIRS = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
            Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};

    /** Acts for a rat carrying an enemy rat; false leaves the turn to the old throw code. */
    public static boolean run() throws GameActionException {
        RobotInfo carried = rc.getCarrying();
        holding = false;
        // small maps: plain positioning (jan29); on 901-1200 tiles the planner was better against both opponents
        if (mapWidth * mapHeight <= 900) return false;
        if (carried == null || carried.getTeam() == team || carried.getType() != UnitType.BABY_RAT) return false;
        int hp = carried.getHealth();
        boolean mustThrow = MUST && (carryStartTime >= 0 && time - carryStartTime >= HOLD_TURNS) || rc.getHealth() <= DANGER_HP
                || CombatTracker.threatCount(Direction.CENTER, myDir) > 0;

        // Throws available now: our facing, or any facing if we can still turn.
        if (rc.isActionReady()) {
            Direction bestDir = null;
            double bestValue = 0;
            boolean bestSure = false;
            for (Direction d : DIRS) {
                if (d != myDir && !rc.canTurn()) continue;
                double v = throwValue(myLoc, d, hp, false);
                if (v == NO_THROW) continue;
                boolean isSure = sure && v >= Math.min(SURE_HIT, hp);  // a clean hit, no friendly fire
                if (bestDir == null || v > bestValue) {
                    bestDir = d;
                    bestValue = v;
                    bestSure = isSure;
                }
            }
            if (bestDir != null && (bestSure || mustThrow)) {
                if (bestDir != myDir) turn(bestDir);
                if (rc.canThrowRat()) {
                    rc.throwRat();
                    return true;
                }
            }
            if (mustThrow) return false;
        }
        holding = !mustThrow;
        if (!AIM) return false;

        // Hold and aim: the move (or none) and facing from which next turn's throw is a sure tile-2 hit.
        if (Clock.getBytecodesLeft() < 4000) return false;
        Direction bestMove = null, bestFace = null;
        double bestScore = 0;
        for (int m = 0; m < 9; m++) {
            Direction move = m == 8 ? Direction.CENTER : DIRS[m];
            if (move != Direction.CENTER && (!rc.isMovementReady() || !CombatCommon.canMove(move))) continue;
            MapLocation from = myLoc.add(move);
            for (Direction d : DIRS) {
                if (Clock.getBytecodesLeft() < 3000) break;
                if (CombatCommon.movementCost(move, d) == -1) continue;
                double v = throwValue(from, d, hp, true);
                if (v == NO_THROW || !sure || v < Math.min(SURE_HIT, hp)) continue;
                double score = v + CombatStrategy.getCellScoreFor(move, d);
                if (bestMove == null || score > bestScore) {
                    bestMove = move;
                    bestFace = d;
                    bestScore = score;
                }
            }
            if (Clock.getBytecodesLeft() < 2500) break;
        }
        if (bestMove == null || bestScore <= 0) return false;
        return CombatStrategy.performMoves(bestMove, bestFace);
    }

    private static final double NO_THROW = -1e18;
    private static boolean sure;  // set by throwValue: the throw hits on tile 2 (or a cat on tile 2)

    /** The value of throwing from `from` along d, or NO_THROW if tile 1 is blocked. */
    private static double throwValue(MapLocation from, Direction d, int hp, boolean aiming) throws GameActionException {
        sure = false;
        MapLocation t = from.add(d);
        if (!rc.onTheMap(t) || blocked(t) || robotAt(t, aiming) != null) return NO_THROW;
        for (int k = 2; k <= (aiming ? 2 : 6); k++) {
            t = t.add(d);
            int dmg = 10 + 4 * (2 * (5 - (k + 1) / 2) + (k % 2 == 1 ? 1 : 0));
            if (!rc.onTheMap(t)) return value(10, hp, null);
            RobotInfo hit = robotAt(t, aiming);
            if (hit != null && hit.getType() == UnitType.CAT) {
                sure = k == 2;
                return value(hp, hp, null) * (k == 2 ? 1 : 0.5);
            }
            if (hit != null || blocked(t)) {
                sure = k == 2;
                return value(dmg, hp, hit) * (k == 2 ? 1 : 0.5);
            }
        }
        return value(10, hp, null);
    }

    private static double value(int dmg, int hp, RobotInfo hit) {
        double v = Math.min(dmg, hp) + (dmg >= hp ? KILL_BONUS : 0);
        if (hit != null && hit.getType() != UnitType.CAT) {
            int d = Math.min(dmg, hit.getHealth());
            v += hit.getTeam() == team ? -ALLY_HIT * d : d;
        }
        return v;
    }

    /** A wall or dirt, as we see it now or as the map was last seen. */
    private static boolean blocked(MapLocation t) throws GameActionException {
        if (rc.canSenseLocation(t)) return !rc.sensePassability(t);
        return CellTracker.mapData[t.y].charAt(t.x) == '\2' || CellTracker.isDirt(t.x, t.y);
    }

    /** The robot on t: sensed when in view, else from this turn's robot lists (which only hold robots in view). */
    private static RobotInfo robotAt(MapLocation t, boolean aiming) throws GameActionException {
        if (t.equals(myLoc)) return null;
        if (rc.canSenseLocation(t)) return rc.isLocationOccupied(t) ? rc.senseRobotAtLocation(t) : null;
        for (int i = 0; i < RobotTracker.numEnemies; i++) if (covers(RobotTracker.sortedEnemies[i], t)) return RobotTracker.sortedEnemies[i];
        for (int i = 0; i < RobotTracker.numAllies; i++) if (covers(RobotTracker.sortedAllies[i], t)) return RobotTracker.sortedAllies[i];
        for (int i = 0; i < RobotTracker.numCats; i++) if (covers(RobotTracker.sortedCats[i], t)) return RobotTracker.sortedCats[i];
        return null;
    }

    private static boolean covers(RobotInfo r, MapLocation t) {
        int dx = t.x - r.location.x, dy = t.y - r.location.y;
        switch (r.getType()) {
            case RAT_KING: return dx >= -1 && dx <= 1 && dy >= -1 && dy <= 1;
            case CAT: return (dx == 0 || dx == 1) && (dy == 0 || dy == 1);
            default: return dx == 0 && dy == 0;
        }
    }
}
