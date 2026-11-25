

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
import static awubot.Game.*;
import static awubot.util.Util.*;
import static awubot.robot.rat.BabyRat.*;
import awubot.RobotTracker;

public final class TryPosition {
    private TryPosition() {
    }

    static Direction bestMove;
    static Direction bestFacing;
    static double bestScore;
    static Direction moveDir;
    static MapLocation newLoc;
    static double score;
    static int bytecodeStart, bytecodeBudget;

    public static boolean run() throws GameActionException {
        if (!rc.isMovementReady()) return false;
        bytecodeStart = Clock.getBytecodeNum();
        bytecodeBudget = Math.min(3000, Clock.getBytecodesLeft() - 1000);
        // // Profiler.push();
        bestMove = null;
        bestFacing = null;
        bestScore = -1e9;
        selectMoves();
        assert(CombatStrategy.performMoves(bestMove, bestFacing));// indicate
        return true;
    }

    private static void selectMoves() throws GameActionException {
        switch (myDir.ordinal()) {
            case 0 -> {
                moveC();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
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
                moveSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
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
                moveW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
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
                moveNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
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
                moveN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
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
                moveNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
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
                moveE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
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
                moveSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
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
        newLoc = myLoc.add(Direction.NORTH);
        switch (myDir.ordinal()) {
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
        newLoc = myLoc.add(Direction.NORTHEAST);
        switch (myDir.ordinal()) {
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
        newLoc = myLoc.add(Direction.EAST);
        switch (myDir.ordinal()) {
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
        newLoc = myLoc.add(Direction.SOUTHEAST);
        switch (myDir.ordinal()) {
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
        newLoc = myLoc.add(Direction.SOUTH);
        switch (myDir.ordinal()) {
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
        newLoc = myLoc.add(Direction.SOUTHWEST);
        switch (myDir.ordinal()) {
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
        newLoc = myLoc.add(Direction.WEST);
        switch (myDir.ordinal()) {
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
        newLoc = myLoc.add(Direction.NORTHWEST);
        switch (myDir.ordinal()) {
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
        newLoc = myLoc.add(Direction.CENTER);
        switch (myDir.ordinal()) {
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
        int moveCost = CombatCommon.movementCost(moveDir, Direction.NORTH);
        if (moveCost == -1) return;
        score = 0;
        if (moveCost == 1) score += 10;
        if (moveDir != Direction.CENTER) score += 5;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.NORTH);
        if (Direction.NORTH != myDir) {
            score += 2;
        }
        if (score > bestScore) {
            bestScore = score;
            bestMove = moveDir;
            bestFacing = Direction.NORTH;
        }
    }
    private static void faceNE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, Direction.NORTHEAST);
        if (moveCost == -1) return;
        score = 0;
        if (moveCost == 1) score += 10;
        if (moveDir != Direction.CENTER) score += 5;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.NORTHEAST);
        if (Direction.NORTHEAST != myDir) {
            score += 2;
        }
        if (score > bestScore) {
            bestScore = score;
            bestMove = moveDir;
            bestFacing = Direction.NORTHEAST;
        }
    }
    private static void faceE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, Direction.EAST);
        if (moveCost == -1) return;
        score = 0;
        if (moveCost == 1) score += 10;
        if (moveDir != Direction.CENTER) score += 5;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.EAST);
        if (Direction.EAST != myDir) {
            score += 2;
        }
        if (score > bestScore) {
            bestScore = score;
            bestMove = moveDir;
            bestFacing = Direction.EAST;
        }
    }
    private static void faceSE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        score = 0;
        if (moveCost == 1) score += 10;
        if (moveDir != Direction.CENTER) score += 5;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTHEAST);
        if (Direction.SOUTHEAST != myDir) {
            score += 2;
        }
        if (score > bestScore) {
            bestScore = score;
            bestMove = moveDir;
            bestFacing = Direction.SOUTHEAST;
        }
    }
    private static void faceS() throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, Direction.SOUTH);
        if (moveCost == -1) return;
        score = 0;
        if (moveCost == 1) score += 10;
        if (moveDir != Direction.CENTER) score += 5;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTH);
        if (Direction.SOUTH != myDir) {
            score += 2;
        }
        if (score > bestScore) {
            bestScore = score;
            bestMove = moveDir;
            bestFacing = Direction.SOUTH;
        }
    }
    private static void faceSW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        score = 0;
        if (moveCost == 1) score += 10;
        if (moveDir != Direction.CENTER) score += 5;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTHWEST);
        if (Direction.SOUTHWEST != myDir) {
            score += 2;
        }
        if (score > bestScore) {
            bestScore = score;
            bestMove = moveDir;
            bestFacing = Direction.SOUTHWEST;
        }
    }
    private static void faceW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, Direction.WEST);
        if (moveCost == -1) return;
        score = 0;
        if (moveCost == 1) score += 10;
        if (moveDir != Direction.CENTER) score += 5;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.WEST);
        if (Direction.WEST != myDir) {
            score += 2;
        }
        if (score > bestScore) {
            bestScore = score;
            bestMove = moveDir;
            bestFacing = Direction.WEST;
        }
    }
    private static void faceNW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, Direction.NORTHWEST);
        if (moveCost == -1) return;
        score = 0;
        if (moveCost == 1) score += 10;
        if (moveDir != Direction.CENTER) score += 5;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.NORTHWEST);
        if (Direction.NORTHWEST != myDir) {
            score += 2;
        }
        if (score > bestScore) {
            bestScore = score;
            bestMove = moveDir;
            bestFacing = Direction.NORTHWEST;
        }
    }
}