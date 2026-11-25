

package awubot.robot.rat.combat.moves;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;

import awubot.robot.rat.combat.CombatCommon;
import awubot.robot.rat.combat.CombatStrategy;
import awubot.robot.rat.combat.CombatTracker;
import static awubot.Game.*;
import static awubot.util.Util.*;
import static awubot.robot.rat.BabyRat.*;
import awubot.RobotTracker;

public final class TryRatNap {
    private TryRatNap() {
    }

    static RobotInfo target;
    static RobotInfo info;
    static Direction bestMove;
    static Direction bestFacing;
    static Direction bestSecondaryMove;
    static double bestScore;
    static double score;
    static Direction moveDir;
    static MapLocation newLoc;
    static int dxToTarget;
    static int dyToTarget;
    static double best;
    static double weight;
    static MapLocation t;
    static int bytecodeStart, bytecodeBudget;

    // return true if successfully ratnapped someone
    // TODO: do danger levels and verify you dont die on the next turn
    public static boolean run() throws GameActionException {
        if (!rc.isActionReady()) return false;
        bytecodeBudget = Math.min(3000, (int) (Clock.getBytecodesLeft() - 1000) / 2);
        bytecodeStart = Clock.getBytecodeNum();
        target = null;
        bestMove = null;
        bestFacing = null;
        bestSecondaryMove = null;
        bestScore = -1;
        for (int i = 0; i < RobotTracker.numEnemies && i < 3; i++) {
            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            info = RobotTracker.sortedEnemies[i];
            t = info.location;
            if (chebyshevDistance(myLoc, t) > 2) continue;
            if (info.getHealth() <= GameConstants.RAT_BITE_DAMAGE) continue; // just kill
            if (info.getType() != UnitType.BABY_RAT) continue;
            // double weight = scoreEnemy(info);
            weight = 30;
            selectMoves();
            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;

            if (i == 0) {
                if(rc.getCurrentRatCost() >= 30) {
                    for (int j = 0; j < RobotTracker.numAllies; j++) {
                        info = RobotTracker.sortedAllies[j];
                        t = info.location;
                        if (chebyshevDistance(myLoc, t) > 2) continue;
                        if (info.getHealth() > 22) continue;
                        if (info.getType() != UnitType.BABY_RAT) continue;
                        weight = 10;
                        selectMoves();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                    }
                }
            }
        }
        if (bestScore < 0.5) return false;
        // println("target " + t);
        if (bestMove != null) {
            if (!CombatStrategy.performMoves(bestMove, bestFacing)) {
                // println(bestMove + " " + bestFacing);
                assert(false);
            }
            if (rc.canCarryRat(target.location)) {
                rc.carryRat(target.location);
            } else {
                attack(target);
            }
        } else {
            if (rc.canCarryRat(target.location)) {
                rc.carryRat(target.location);
            } else {
                attack(target);
            }
            assert(CombatStrategy.performMoves(bestSecondaryMove, bestFacing));
        }
        // println("!!!!!!!!!!!!!!!!!!!!!!! napped rat");
        // indicate("COMBAT STRAT: NAP");
        return true;
    }

    private static void selectMoves() throws GameActionException {
        int dx = t.x - myLocX;
        int dy = t.y - myLocY;
        boolean canPerformSecondary = myLoc.distanceSquaredTo(t) <= 2 && (rc.getHealth() > info.getHealth() || !canSense(-dx, -dy, info.direction));
        switch (myDir.ordinal()) {
            case 0 -> {
                        moveC();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveN();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.SOUTH, Direction.NORTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTH, Direction.NORTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTH, Direction.NORTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveNE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveNW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.SOUTHEAST, Direction.NORTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHEAST, Direction.NORTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHEAST, Direction.NORTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.SOUTHWEST, Direction.NORTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHWEST, Direction.NORTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHWEST, Direction.NORTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 1 -> {
                        moveC();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveNE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.SOUTHWEST, Direction.NORTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHWEST, Direction.NORTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHWEST, Direction.EAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveN();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.SOUTH, Direction.NORTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTH, Direction.NORTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTH, Direction.EAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.WEST, Direction.NORTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.WEST, Direction.NORTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.WEST, Direction.EAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveSE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveNW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 2 -> {
                        moveC();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.WEST, Direction.EAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.WEST, Direction.NORTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.WEST, Direction.SOUTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveSE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveNE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.SOUTHWEST, Direction.EAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHWEST, Direction.NORTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHWEST, Direction.SOUTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.NORTHWEST, Direction.EAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHWEST, Direction.NORTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHWEST, Direction.SOUTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveS();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveN();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 3 -> {
                        moveC();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveSE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.NORTHWEST, Direction.SOUTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHWEST, Direction.SOUTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHWEST, Direction.EAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveS();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.WEST, Direction.SOUTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.WEST, Direction.SOUTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.WEST, Direction.EAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.NORTH, Direction.SOUTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTH, Direction.SOUTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTH, Direction.EAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveSW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveNE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 4 -> {
                        moveC();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveS();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.NORTH, Direction.SOUTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTH, Direction.SOUTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTH, Direction.SOUTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveSW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveSE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.NORTHWEST, Direction.SOUTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHWEST, Direction.SOUTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHWEST, Direction.SOUTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.NORTHEAST, Direction.SOUTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHEAST, Direction.SOUTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHEAST, Direction.SOUTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 5 -> {
                        moveC();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveSW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.NORTHEAST, Direction.SOUTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHEAST, Direction.SOUTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHEAST, Direction.WEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveS();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.NORTH, Direction.SOUTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTH, Direction.SOUTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTH, Direction.WEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.EAST, Direction.SOUTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.EAST, Direction.SOUTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.EAST, Direction.WEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveNW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveSE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 6 -> {
                        moveC();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.EAST, Direction.WEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.EAST, Direction.NORTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.EAST, Direction.SOUTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveNW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveSW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.NORTHEAST, Direction.WEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHEAST, Direction.NORTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHEAST, Direction.SOUTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.SOUTHEAST, Direction.WEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHEAST, Direction.NORTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHEAST, Direction.SOUTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveN();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveS();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 7 -> {
                        moveC();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveNW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.SOUTHEAST, Direction.NORTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHEAST, Direction.NORTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHEAST, Direction.WEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveN();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.EAST, Direction.NORTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.EAST, Direction.NORTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.EAST, Direction.WEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.SOUTH, Direction.NORTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTH, Direction.NORTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTH, Direction.WEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveNE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveSW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            default -> { }
        }
    }

    private static void moveN() throws GameActionException {
        if (!CombatCommon.canMoveN) return;
        moveDir = Direction.NORTH;
        if (info.getTeam() == team && CombatTracker.hasAnyThreat(moveDir)) return;
        newLoc = myLoc.add(moveDir);
        dxToTarget = t.x - newLoc.x;
        dyToTarget = t.y - newLoc.y;
        if (dxToTarget * dxToTarget + dyToTarget * dyToTarget > 2) return;
        if (info.getHealth() >= rc.getHealth() && canSense(-dxToTarget, -dyToTarget, info.direction)) return; // can i ratnap this guy
        switch (newLoc.directionTo(t).ordinal()) {
            case 0 -> {
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            default -> { }
        }
    }

    private static void moveNE() throws GameActionException {
        if (!CombatCommon.canMoveNE) return;
        moveDir = Direction.NORTHEAST;
        if (info.getTeam() == team && CombatTracker.hasAnyThreat(moveDir)) return;
        newLoc = myLoc.add(moveDir);
        dxToTarget = t.x - newLoc.x;
        dyToTarget = t.y - newLoc.y;
        if (dxToTarget * dxToTarget + dyToTarget * dyToTarget > 2) return;
        if (info.getHealth() >= rc.getHealth() && canSense(-dxToTarget, -dyToTarget, info.direction)) return; // can i ratnap this guy
        switch (newLoc.directionTo(t).ordinal()) {
            case 0 -> {
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            default -> { }
        }
    }

    private static void moveE() throws GameActionException {
        if (!CombatCommon.canMoveE) return;
        moveDir = Direction.EAST;
        if (info.getTeam() == team && CombatTracker.hasAnyThreat(moveDir)) return;
        newLoc = myLoc.add(moveDir);
        dxToTarget = t.x - newLoc.x;
        dyToTarget = t.y - newLoc.y;
        if (dxToTarget * dxToTarget + dyToTarget * dyToTarget > 2) return;
        if (info.getHealth() >= rc.getHealth() && canSense(-dxToTarget, -dyToTarget, info.direction)) return; // can i ratnap this guy
        switch (newLoc.directionTo(t).ordinal()) {
            case 0 -> {
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            default -> { }
        }
    }

    private static void moveSE() throws GameActionException {
        if (!CombatCommon.canMoveSE) return;
        moveDir = Direction.SOUTHEAST;
        if (info.getTeam() == team && CombatTracker.hasAnyThreat(moveDir)) return;
        newLoc = myLoc.add(moveDir);
        dxToTarget = t.x - newLoc.x;
        dyToTarget = t.y - newLoc.y;
        if (dxToTarget * dxToTarget + dyToTarget * dyToTarget > 2) return;
        if (info.getHealth() >= rc.getHealth() && canSense(-dxToTarget, -dyToTarget, info.direction)) return; // can i ratnap this guy
        switch (newLoc.directionTo(t).ordinal()) {
            case 0 -> {
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            default -> { }
        }
    }

    private static void moveS() throws GameActionException {
        if (!CombatCommon.canMoveS) return;
        moveDir = Direction.SOUTH;
        if (info.getTeam() == team && CombatTracker.hasAnyThreat(moveDir)) return;
        newLoc = myLoc.add(moveDir);
        dxToTarget = t.x - newLoc.x;
        dyToTarget = t.y - newLoc.y;
        if (dxToTarget * dxToTarget + dyToTarget * dyToTarget > 2) return;
        if (info.getHealth() >= rc.getHealth() && canSense(-dxToTarget, -dyToTarget, info.direction)) return; // can i ratnap this guy
        switch (newLoc.directionTo(t).ordinal()) {
            case 0 -> {
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            default -> { }
        }
    }

    private static void moveSW() throws GameActionException {
        if (!CombatCommon.canMoveSW) return;
        moveDir = Direction.SOUTHWEST;
        if (info.getTeam() == team && CombatTracker.hasAnyThreat(moveDir)) return;
        newLoc = myLoc.add(moveDir);
        dxToTarget = t.x - newLoc.x;
        dyToTarget = t.y - newLoc.y;
        if (dxToTarget * dxToTarget + dyToTarget * dyToTarget > 2) return;
        if (info.getHealth() >= rc.getHealth() && canSense(-dxToTarget, -dyToTarget, info.direction)) return; // can i ratnap this guy
        switch (newLoc.directionTo(t).ordinal()) {
            case 0 -> {
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            default -> { }
        }
    }

    private static void moveW() throws GameActionException {
        if (!CombatCommon.canMoveW) return;
        moveDir = Direction.WEST;
        if (info.getTeam() == team && CombatTracker.hasAnyThreat(moveDir)) return;
        newLoc = myLoc.add(moveDir);
        dxToTarget = t.x - newLoc.x;
        dyToTarget = t.y - newLoc.y;
        if (dxToTarget * dxToTarget + dyToTarget * dyToTarget > 2) return;
        if (info.getHealth() >= rc.getHealth() && canSense(-dxToTarget, -dyToTarget, info.direction)) return; // can i ratnap this guy
        switch (newLoc.directionTo(t).ordinal()) {
            case 0 -> {
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            default -> { }
        }
    }

    private static void moveNW() throws GameActionException {
        if (!CombatCommon.canMoveNW) return;
        moveDir = Direction.NORTHWEST;
        if (info.getTeam() == team && CombatTracker.hasAnyThreat(moveDir)) return;
        newLoc = myLoc.add(moveDir);
        dxToTarget = t.x - newLoc.x;
        dyToTarget = t.y - newLoc.y;
        if (dxToTarget * dxToTarget + dyToTarget * dyToTarget > 2) return;
        if (info.getHealth() >= rc.getHealth() && canSense(-dxToTarget, -dyToTarget, info.direction)) return; // can i ratnap this guy
        switch (newLoc.directionTo(t).ordinal()) {
            case 0 -> {
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            default -> { }
        }
    }

    private static void moveC() throws GameActionException {
        if (!CombatCommon.canMoveC) return;
        moveDir = Direction.CENTER;
        if (info.getTeam() == team && CombatTracker.hasAnyThreat(moveDir)) return;
        newLoc = myLoc.add(moveDir);
        dxToTarget = t.x - newLoc.x;
        dyToTarget = t.y - newLoc.y;
        if (dxToTarget * dxToTarget + dyToTarget * dyToTarget > 2) return;
        if (info.getHealth() >= rc.getHealth() && canSense(-dxToTarget, -dyToTarget, info.direction)) return; // can i ratnap this guy
        switch (newLoc.directionTo(t).ordinal()) {
            case 0 -> {
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            default -> { }
        }
    }

    private static void faceN() throws GameActionException {
        if (!canSense(dxToTarget, dyToTarget, Direction.NORTH)) return;
        int moveCost = CombatCommon.movementCost(moveDir, Direction.NORTH);
        if (moveCost == -1) return;
        score = weight;
        if (moveCost == 0) score -= CombatCommon.strafeCost();
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.NORTH, info);
        if (score <= 0.5) return;
        if (Direction.NORTH != myDir) {
            score += 2; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.NORTH;
            bestSecondaryMove = null;
        }
    }

    private static void faceNE() throws GameActionException {
        if (!canSense(dxToTarget, dyToTarget, Direction.NORTHEAST)) return;
        int moveCost = CombatCommon.movementCost(moveDir, Direction.NORTHEAST);
        if (moveCost == -1) return;
        score = weight;
        if (moveCost == 0) score -= CombatCommon.strafeCost();
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.NORTHEAST, info);
        if (score <= 0.5) return;
        if (Direction.NORTHEAST != myDir) {
            score += 2; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.NORTHEAST;
            bestSecondaryMove = null;
        }
    }

    private static void faceE() throws GameActionException {
        if (!canSense(dxToTarget, dyToTarget, Direction.EAST)) return;
        int moveCost = CombatCommon.movementCost(moveDir, Direction.EAST);
        if (moveCost == -1) return;
        score = weight;
        if (moveCost == 0) score -= CombatCommon.strafeCost();
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.EAST, info);
        if (score <= 0.5) return;
        if (Direction.EAST != myDir) {
            score += 2; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.EAST;
            bestSecondaryMove = null;
        }
    }

    private static void faceSE() throws GameActionException {
        if (!canSense(dxToTarget, dyToTarget, Direction.SOUTHEAST)) return;
        int moveCost = CombatCommon.movementCost(moveDir, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        score = weight;
        if (moveCost == 0) score -= CombatCommon.strafeCost();
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTHEAST, info);
        if (score <= 0.5) return;
        if (Direction.SOUTHEAST != myDir) {
            score += 2; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.SOUTHEAST;
            bestSecondaryMove = null;
        }
    }

    private static void faceS() throws GameActionException {
        if (!canSense(dxToTarget, dyToTarget, Direction.SOUTH)) return;
        int moveCost = CombatCommon.movementCost(moveDir, Direction.SOUTH);
        if (moveCost == -1) return;
        score = weight;
        if (moveCost == 0) score -= CombatCommon.strafeCost();
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTH, info);
        if (score <= 0.5) return;
        if (Direction.SOUTH != myDir) {
            score += 2; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.SOUTH;
            bestSecondaryMove = null;
        }
    }

    private static void faceSW() throws GameActionException {
        if (!canSense(dxToTarget, dyToTarget, Direction.SOUTHWEST)) return;
        int moveCost = CombatCommon.movementCost(moveDir, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        score = weight;
        if (moveCost == 0) score -= CombatCommon.strafeCost();
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTHWEST, info);
        if (score <= 0.5) return;
        if (Direction.SOUTHWEST != myDir) {
            score += 2; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.SOUTHWEST;
            bestSecondaryMove = null;
        }
    }

    private static void faceW() throws GameActionException {
        if (!canSense(dxToTarget, dyToTarget, Direction.WEST)) return;
        int moveCost = CombatCommon.movementCost(moveDir, Direction.WEST);
        if (moveCost == -1) return;
        score = weight;
        if (moveCost == 0) score -= CombatCommon.strafeCost();
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.WEST, info);
        if (score <= 0.5) return;
        if (Direction.WEST != myDir) {
            score += 2; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.WEST;
            bestSecondaryMove = null;
        }
    }

    private static void faceNW() throws GameActionException {
        if (!canSense(dxToTarget, dyToTarget, Direction.NORTHWEST)) return;
        int moveCost = CombatCommon.movementCost(moveDir, Direction.NORTHWEST);
        if (moveCost == -1) return;
        score = weight;
        if (moveCost == 0) score -= CombatCommon.strafeCost();
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.NORTHWEST, info);
        if (score <= 0.5) return;
        if (Direction.NORTHWEST != myDir) {
            score += 2; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.NORTHWEST;
            bestSecondaryMove = null;
        }
    }


    private static void considerSecondaryMove(Direction moveDir, Direction facing) throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, facing);
        if (moveCost == -1) return;
        if (info.getTeam() == team && CombatTracker.hasAnyThreat(moveDir)) return;
        double score = weight + 5;
        if (moveCost == 0) score -= CombatCommon.strafeCost();
        score += CombatStrategy.getCellScoreFor(moveDir, facing, info);
        if (score <= 0.5) return;
        if (facing != myDir) {
            score += 2;
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = null;
            bestFacing = facing;
            bestSecondaryMove = moveDir;
        }
    }

    public static boolean miniRun() throws GameActionException {
        // first try to nap a rat looking at us
        for (int i = 0; i < RobotTracker.numEnemies && i < 3; i++) {
            info = RobotTracker.sortedEnemies[i];
            if (info.getTeam() == team) continue;
            if (info.location.distanceSquaredTo(myLoc) > 2) continue;
            if (info.getHealth() <= CombatStrategy.getBiteDamage()) continue; // just kill
            if (info.getType() != UnitType.BABY_RAT) continue;
            int dx = info.location.x - myLocX;
            int dy = info.location.y - myLocY;
            if (canSense(dx, dy, info.direction)) {
                if (rc.canCarryRat(info.location)) {
                    rc.carryRat(info.location);
                    // println("!!!!!!!!!!!!!!!!!!!!!!! mini napped rat");
                    return true;
                }
            }
        }
        // nap anyone
        for (int i = 0; i < RobotTracker.numEnemies && i < 3; i++) {
            info = RobotTracker.sortedEnemies[i];
            if (info.getTeam() == team) continue;
            if (info.getHealth() <= CombatStrategy.getBiteDamage()) continue; // just kill
            if (info.getType() != UnitType.BABY_RAT) continue;
            if (rc.canCarryRat(info.location)) {
                rc.carryRat(info.location);
                // println("!!!!!!!!!!!!!!!!!!!!!!! mini napped rat");
                // indicate("COMBAT STRAT: MINI NAP");
                return true;
            }
        }
        return false;
    }
}