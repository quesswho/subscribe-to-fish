

package awubot.robot.rat.combat;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.TrapType;
import battlecode.common.UnitType;
import awubot.CellTracker;
import awubot.Game;
import awubot.Pathfinder;
import awubot.comms.CatClock;
import awubot.comms.GlobalArray;
import awubot.comms.Squeaker;
import awubot.comms.Enemy;
import awubot.comms.EnemyManager;
import awubot.robot.Strategy;
import awubot.robot.rat.combat.moves.TryFight;
import awubot.robot.rat.combat.moves.TryAttack;
import awubot.robot.rat.combat.moves.TryRatNap;
import awubot.robot.rat.combat.moves.TryPosition;
import awubot.robot.rat.combat.moves.TryThrow;
import awubot.robot.rat.combat.moves.ThrowPlanner;
import awubot.robot.rat.combat.moves.TryTrap;
import awubot.util.Profiler;
import awubot.util.Util;

import static awubot.robot.rat.BabyRat.*;
import static awubot.Game.*;
import static awubot.util.Util.*;
import awubot.RobotTracker;

import java.util.function.BiConsumer;
import java.util.function.Consumer;



/*

  TODO:
  - add code for detecting enemies out of vision
  - consider avoiding cells within cheb dist of 1/2 of enemy rats
  - strongly prefer looking at enemies
  - offence squeak signals
  - combat tracker bugfix
  - better movement

*/

public class CombatStrategy extends Strategy {
    public static boolean ENABLE_PROFILE = true;


    private static final double INF = 1e9;
    private static final int RAT_TRAP_COST = 30;
    private static final int RAT_TRAP_CAP = 25;

    public CombatStrategy() {
    }

    /*

      the returned score should be a multiplier for any "damage" dealt to it

    */



    // in cheese units
    public double trapCost() {
        return RAT_TRAP_COST; // tf i cant see how many rat traps i have placed?
    }

    public static double[] cellScore; // [movement direction]

    static int precompStart, precompBudget;
    private void precomp() throws GameActionException {
        precompStart = Clock.getBytecodeNum();
        precompBudget = 5000;
        // negligable bytecode
        CombatCommon.precomp();

        // expensive
        try {
            CombatTracker.tick();
        } finally {
        }
        try {
            computeCellScores();
        } finally {
        }
    }

    public boolean runTick() throws GameActionException {
        try {
            return _runTick();
        } finally {
        }
    }

    public static int moveMask, sourceCnt;
    public MapLocation[] sources = new MapLocation[35];
    public static Enemy funnyTarget;

    private boolean _runTick() throws GameActionException {
        if (rc.isBeingCarried() || rc.isBeingThrown()) return false;
        // if (rc.getRawCheese() >= 50 && myLoc.distanceSquaredto(GlobalArray.nearestRatKing) <= 36) return false;

        funnyTarget = null;
        if (rc.getCarrying() == null || rc.getCarrying().getTeam() == team) {
            sourceCnt = 0;
            for (int i = 0; i < RobotTracker.numEnemies; i++) {
                sources[sourceCnt++] = RobotTracker.sortedEnemies[i].location;
            }
            for (int i = 0; i < RobotTracker.numCats; i++) {
                if (shouldFight(RobotTracker.sortedCats[i])) {
                    sources[sourceCnt++] = RobotTracker.sortedCats[i].location;
                }
            }
            if (sourceCnt == 0 && EnemyManager.numKnownEnemies > 0 && rc.canTurn()) {
                // find nearest
                int nearestDist = Integer.MAX_VALUE;
                for (int i = 0; i < EnemyManager.numKnownEnemies; i++) {
                    MapLocation enemyLoc = EnemyManager.knownEnemies[i].location;
                    int dist = myLoc.distanceSquaredTo(enemyLoc);
                    if (dist < nearestDist) {
                        nearestDist = dist;
                        funnyTarget = EnemyManager.knownEnemies[i];
                    }
                }
                sources[sourceCnt++] = funnyTarget.location;
            }
            if (sourceCnt == 0) return false;
            lookAtHeardNeighbour();
            // for (int i = 0; i < sourceCnt; i++) {
            // }
            int[] bfsResult = Pathfinder.multisourceBfs(sources, sourceCnt);
            moveMask = bfsResult[0];    
            int bfsIters = bfsResult[1];
            if (moveMask == 0) return false; // no enemies nearby
            if (funnyTarget != null && bfsIters <= 5 && rc.canTurn()) {
                turn(myLoc.directionTo(funnyTarget.location));
            }
        }

        try {
            precomp();
        } finally {
        }

        isLastMove = true;
        if (rc.getCarrying() != null) {
            try {
                boolean tryThrowRes = TryThrow.run();
                if (tryThrowRes) return true;
            } finally {
            }
            // The old throw code declined: aim for next turn's sure hit instead of plain positioning.
            if (ThrowPlanner.run()) return true;
            try {
                boolean tryPositionRes = TryPosition.run();
                if (tryPositionRes) return true;
            } finally {
            }
            return true; // bruh
        }
        try {
            boolean tryRatNapRes = TryRatNap.run();
            if (tryRatNapRes) return true;
        } finally {
        }
        try {
            boolean tryTrapRes = TryTrap.run();
            if (tryTrapRes) return true;
        } finally {
        }
        try {
            boolean tryAttackRes = TryAttack.run();
            if (tryAttackRes) return true;
        } finally {
        }
        boolean tryFightRes;
        try {
            tryFightRes = TryFight.run();
        } finally {
        }
        if (tryFightRes) {
            try {
                miniTick();
            } finally {
            }
            return true;
        }// println
        // TODO: deal with cats (maybe naman's job?)
        // return false;
        return false;
    }

    // A rat with action and turn ready and no enemy rat in view within 2 tiles turns to an enemy within 2 that it
    // only heard of (a carried ally squeaks its carrier): then the grab and bite code can see it.
    private static void lookAtHeardNeighbour() throws GameActionException {
        if (!rc.isActionReady() || !rc.canTurn()) return;
        for (int i = 0; i < RobotTracker.numEnemies; i++) {
            RobotInfo e = RobotTracker.sortedEnemies[i];
            if (e.getType() == UnitType.BABY_RAT && chebyshevDistance(myLoc, e.location) <= 2) return;
        }
        for (int i = 0; i < EnemyManager.numKnownEnemies; i++) {
            MapLocation l = EnemyManager.knownEnemies[i].location;
            if (chebyshevDistance(myLoc, l) <= 2 && !l.equals(myLoc)) {
                turn(myLoc.directionTo(l));
                return;
            }
        }
    }

    public boolean miniTick() throws GameActionException {
        boolean tryRatNapRes = TryRatNap.miniRun();
        if (tryRatNapRes) return true;

        boolean tryAttackRes = TryAttack.miniRun();
        if (tryAttackRes) return true;
        boolean tryTrapRes = TryTrap.miniRun();
        if (tryTrapRes) return true;
        return false;
    }

    public static boolean performMoves(Direction moveDir, Direction facing) throws GameActionException {
        if (moveDir != Direction.CENTER && !CombatCommon.canMove(moveDir)) return false;
        if (moveDir == Direction.CENTER) {
            if (facing != myDir) {
                if (!rc.canTurn()) return false;
                turn(facing);
            }
            return true;
        }
        if (!rc.isMovementReady()) return false;
        if (moveDir == myDir && facing == myDir) {
            rawMove();
            return true; // move
        }
        if (moveDir == myDir) { // move turn
            rawMove();
            if (facing != myDir) {
                if (!rc.canTurn()) return false;
                turn(facing);
            }
            return true;
        }
        if (moveDir == facing) { // turn move
            if (!rc.canTurn()) return false;
            turn(moveDir);
            rawMove();
            return true;
        }
        if (facing == myDir) { // strafe
            rawMove(moveDir);
            return true;
        }
        rawMove(moveDir);
        if (!rc.canTurn()) return false;
        turn(facing);
        return true;
    }

    private static StringBuilder sb;
    private static double readCellScore(RobotInfo target, Direction moveDir) {
        String key = "=" + target.getID() + "+" + moveDir.ordinal() + "|";
        int i = sb.indexOf(key);
        if (i == -1) return 0;
        else return (((int) sb.charAt(i - 1)) - 32000) / 100.0;
    }
    private static double cacheCellScore(RobotInfo target, Direction moveDir, double value) {
        String key = ((char) ((int) value * 100 + 32000)) + "=" + target.getID() + "+" + moveDir.ordinal() + "|";
        sb.append(key);
        return value;
    }

    public static void computeCellScores() throws GameActionException {
        sb = new StringBuilder();
        cellScore = new double[9];
        if (1 == 1) return;
        for (int i = 0; i < RobotTracker.numCats && i < 3; i++) {
            RobotInfo info = RobotTracker.sortedCats[i];
            cellScore[0] += getCellScoreForEnemy(info, Direction.NORTH);
            cellScore[1] += getCellScoreForEnemy(info, Direction.NORTHEAST);
            cellScore[2] += getCellScoreForEnemy(info, Direction.EAST);
            cellScore[3] += getCellScoreForEnemy(info, Direction.SOUTHEAST);
            cellScore[4] += getCellScoreForEnemy(info, Direction.SOUTH);
            cellScore[5] += getCellScoreForEnemy(info, Direction.SOUTHWEST);
            cellScore[6] += getCellScoreForEnemy(info, Direction.WEST);
            cellScore[7] += getCellScoreForEnemy(info, Direction.NORTHWEST);
            cellScore[8] += getCellScoreForEnemy(info, Direction.CENTER);
        }
        for (int i = 0; i < RobotTracker.numEnemies && i < 3; i++) {
            RobotInfo info = RobotTracker.sortedEnemies[i];
            if (Clock.getBytecodeNum() - precompStart > precompBudget) break;
            cellScore[0] += getCellScoreForEnemy(info, Direction.NORTH);
            if (Clock.getBytecodeNum() - precompStart > precompBudget) break;
            cellScore[1] += getCellScoreForEnemy(info, Direction.NORTHEAST);
            if (Clock.getBytecodeNum() - precompStart > precompBudget) break;
            cellScore[2] += getCellScoreForEnemy(info, Direction.EAST);
            if (Clock.getBytecodeNum() - precompStart > precompBudget) break;
            cellScore[3] += getCellScoreForEnemy(info, Direction.SOUTHEAST);
            if (Clock.getBytecodeNum() - precompStart > precompBudget) break;
            cellScore[4] += getCellScoreForEnemy(info, Direction.SOUTH);
            if (Clock.getBytecodeNum() - precompStart > precompBudget) break;
            cellScore[5] += getCellScoreForEnemy(info, Direction.SOUTHWEST);
            if (Clock.getBytecodeNum() - precompStart > precompBudget) break;
            cellScore[6] += getCellScoreForEnemy(info, Direction.WEST);
            if (Clock.getBytecodeNum() - precompStart > precompBudget) break;
            cellScore[7] += getCellScoreForEnemy(info, Direction.NORTHWEST);
            if (Clock.getBytecodeNum() - precompStart > precompBudget) break;
            cellScore[8] += getCellScoreForEnemy(info, Direction.CENTER);
            if (Clock.getBytecodeNum() - precompStart > precompBudget) break;
        }// indicate
    }

    public static double getCellScoreFor(Direction moveDir, Direction facing, RobotInfo target, int damage) throws GameActionException {
        if (CombatTracker.hasThreat(moveDir, facing, target, damage)) return -100;
        return 0;
        // return cellScore[moveDir.ordinal()] - readCellScore(target, moveDir);
    }

    public static double getCellScoreFor(Direction moveDir, Direction facing, RobotInfo target) throws GameActionException {
        if (CombatTracker.hasThreat(moveDir, facing, target)) return -100;
        return 0;
        // return cellScore[moveDir.ordinal()] - readCellScore(target, moveDir);
    }

    public static double getCellScoreFor(Direction moveDir, Direction facing) throws GameActionException {
        int threats = CombatTracker.threatCount(moveDir, facing);
        if (threats > 0) return -50 - 50 * threats;
        return 0;
        // return cellScore[moveDir.ordinal()];
    }

    // TODO: cache this function
    public static double getCellScoreForEnemy(RobotInfo info, Direction moveDir) {
        MapLocation to = myLoc.add(moveDir);
        if (!rc.onTheMap(to)) return 0;
        int dist = chebyshevDistance(to, info);
        if (info.getType() == UnitType.CAT) {
            // TODO: implement vision check
            if (dist >= 4 || shouldFight(info)) return 0;
            else return cacheCellScore(info, moveDir, -100);
        }
        if (dist > 3) return 0;
        MapLocation enemyLoc = info.location;
        if (info.getType() == UnitType.RAT_KING) {
            if (dist > 2) return 0; // is this correct?
            else return cacheCellScore(info, moveDir, 1);
        } else {
            double base = 0;
            Direction enemyDir = info.direction;
            int dx = myLocX - enemyLoc.x;
            int dy = myLocY - enemyLoc.y;
            boolean canCurrentlySense = canSense(dx, dy, enemyDir);
            dx += moveDir.dx;
            dy += moveDir.dy;
            boolean canSenseAfterMove = canSense(dx, dy, enemyDir);

            if (canCurrentlySense) {
                if (!canSenseAfterMove) {
                    base += 4; // escaping vision
                } else {
                    base -= 1; // staying in vision
                }
            } else {
                if (canSenseAfterMove) {
                    base -= 4; // entering vision
                } else {
                    base = 0; // they dont see u
                }
            }

            if (canSenseAfterMove) {
                int dangerDistance;
                if (RobotTracker.numAllies > RobotTracker.numEnemies) dangerDistance = 0;
                // else if (time % 4 == 0) dangerDistance = 1;
                else dangerDistance = 2;
                if (dist <= dangerDistance) base -= 10;
            }

            if (rc.getHealth() > 1.5 * info.getHealth() && rc.getHealth() > 20) {
                base *= 0.5;
                // weak
            }
            return cacheCellScore(info, moveDir, base);
        }
    }

    public static final int LATE_CAT_ROUND = 1700;
    // From here a rat with no enemy rat in view fights a cat it sees: the 1200-1600 cat damage decides points races.
    public static final int MID_CAT_ROUND = 1100;

    public static int getBiteDamage() {
        return 10;
    }
    
    /*

        attacking another solo rat is safe as if:
        - you end the turn with at least as much health (no ratnap)
        - they can't see you
        if you are in a horde, it should be safe to run at them

    */
    public static boolean shouldFight(RobotInfo enemy) {
        if (enemy.getType() == UnitType.CAT) {
            if (CatClock.hold(enemy)) return false;
            // Late in the game cat damage is points (30% after a backstab, 50% in cooperation) and rat losses no longer
            // cost the future: every rat not carrying cheese fights a cat it sees.
            if (time >= LATE_CAT_ROUND && rc.getRawCheese() == 0) return true;
            if (time >= MID_CAT_ROUND && rc.getRawCheese() == 0 && RobotTracker.numEnemies == 0) return true;
            return time > 300 && rc.getAllCheese() > 500 && RobotTracker.numEnemies == 0 && rc.getRawCheese() == 0 && GlobalArray.numberOfRatsAlive >= 12 && (rc.getID() % 2 == 0 || time >= 1800);
        }
        if (enemy.getType() == UnitType.RAT_KING) return true;
        if (enemy.getHealth() - getBiteDamage() <= rc.getHealth()) return true;
        if (!canSense(enemy.location, myLoc, enemy.direction)) return true;
        if (GlobalArray.nearestRatKing != null && GlobalArray.nearestRatKing.distanceSquaredTo(myLoc) <= 36) return true;
        if (RobotTracker.numAllies > RobotTracker.numEnemies) return true;
        return false;
    }

    // currently given arbitrary weights
    public double scoreEnemy(RobotInfo info) {
        if (info.getType() == UnitType.RAT_KING) return 5;
        // one tap this guy
        if (info.getHealth() - GameConstants.RAT_BITE_DAMAGE <= 0) {
            return 3.0;
        }
        if (info.getHealth() <= 30) {
            // try to get rid of low hp rats
            return 1.3;
        }
        if (info.getHealth() >= rc.getHealth()) {
            // try to bring enemy rats into napping distance
            return 1.2;
        }
        return 1;
    }

    public static int getCheeseNeeded(int damageBuff) {
        if (damageBuff == 0) return 0;
        int base = damageBuff - 1;
        return base * base + 1;
    }

    public static int canEnemyReach(RobotInfo info, Direction moveDir) throws GameActionException {
        MapLocation to = myLoc.add(moveDir);
        int dist = chebyshevDistance(to, info.location);
        if (dist > 2) return 0;
        if (!isCellMaybePassable(to)) return 0;;
        return 1;
    }

    public static RobotInfo getNearestTarget() {
        for (int i = 0; i < RobotTracker.numEnemies; i++) {
            RobotInfo info = RobotTracker.sortedEnemies[i];
            if (info.getTeam() == rc.getTeam()) continue;
            if (shouldFight(info)) return info;
        }
        for  (int i = 0; i < RobotTracker.numCats; i++) {
            RobotInfo info = RobotTracker.sortedCats[i];
            if (shouldFight(info)) return info;
        }
        return null;
    }
}