

package awubot.robot.rat.combat.moves;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

import awubot.robot.rat.combat.CombatCommon;
import awubot.robot.rat.combat.CombatStrategy;
import static awubot.Game.*;
import static awubot.util.Util.*;
import static awubot.robot.rat.BabyRat.*;
import awubot.RobotTracker;
import awubot.comms.Enemy;

public final class TryFight {
    private TryFight() {
    }

    static Direction md;
    static Direction fd;
    static double best;
    static Direction moveDir;
    static MapLocation newLoc;
    static double score;
    static boolean facingTarget;
    static Enemy target;
    static int dxToTarget;
    static int dyToTarget;
    static int bytecodeStart, bytecodeBudget;
    static boolean isCat;
    static int catFightCooldown = -1;
    static int curCatDist;

    public static boolean run() throws GameActionException {
        // // Profiler.push();
        md = null;
        fd = null;
        isCat = false;
        best = -1e9;
        if (RobotTracker.numEnemies > 0) {
            target = new Enemy(RobotTracker.sortedEnemies[0]);
        } else if (RobotTracker.numCats > 0) {
            if (time - catFightCooldown < 10) {
                return false;
            }
            target = new Enemy(RobotTracker.sortedCats[0]);
            curCatDist = catDist(myLoc);
            isCat = true;
        } else if (CombatStrategy.funnyTarget != null) {
            target = CombatStrategy.funnyTarget;
        } else {
            // println("wtf ?????????????????????????????????????????????????????????");
            assert(false);
            return false;
        }
        bytecodeStart = Clock.getBytecodeNum();
        bytecodeBudget = Clock.getBytecodesLeft() - 1500;
        selectMoves();
        if (isCat) {
            if (md == null) {
                catFightCooldown = time;
                return false;
            }
        }
        // // Profiler.pop("try fight");
        // force this strategy
        // if (md == null) return false;
        CombatStrategy.performMoves(md, fd);
        TryAttack.miniRun();
        // indicate("COMBAT STRAT: FIGHT");
        // try to squeeze some juice out
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
        if (!rc.isMovementReady()) return;
        if (!CombatCommon.canMoveN) return;
        moveDir = Direction.NORTH;
        newLoc = myLoc.add(moveDir);
        if (isCat) {
            if (catDist(newLoc) >= curCatDist) {
                return;
            }
        }
        dxToTarget = target.location.x - newLoc.x;
        dyToTarget = target.location.y - newLoc.y;
        if (isCat) {
            if (canCatSense(-dxToTarget, -dyToTarget, target.direction)) {
                return; 
            }
        }
        switch (newLoc.directionTo(target.location).ordinal()) {
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
        if (!rc.isMovementReady()) return;
        if (!CombatCommon.canMoveNE) return;
        moveDir = Direction.NORTHEAST;
        newLoc = myLoc.add(moveDir);
        if (isCat) {
            if (catDist(newLoc) >= curCatDist) {
                return;
            }
        }
        dxToTarget = target.location.x - newLoc.x;
        dyToTarget = target.location.y - newLoc.y;
        if (isCat) {
            if (canCatSense(-dxToTarget, -dyToTarget, target.direction)) {
                return; 
            }
        }
        switch (newLoc.directionTo(target.location).ordinal()) {
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
        if (!rc.isMovementReady()) return;
        if (!CombatCommon.canMoveE) return;
        moveDir = Direction.EAST;
        newLoc = myLoc.add(moveDir);
        if (isCat) {
            if (catDist(newLoc) >= curCatDist) {
                return;
            }
        }
        dxToTarget = target.location.x - newLoc.x;
        dyToTarget = target.location.y - newLoc.y;
        if (isCat) {
            if (canCatSense(-dxToTarget, -dyToTarget, target.direction)) {
                return; 
            }
        }
        switch (newLoc.directionTo(target.location).ordinal()) {
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
        if (!rc.isMovementReady()) return;
        if (!CombatCommon.canMoveSE) return;
        moveDir = Direction.SOUTHEAST;
        newLoc = myLoc.add(moveDir);
        if (isCat) {
            if (catDist(newLoc) >= curCatDist) {
                return;
            }
        }
        dxToTarget = target.location.x - newLoc.x;
        dyToTarget = target.location.y - newLoc.y;
        if (isCat) {
            if (canCatSense(-dxToTarget, -dyToTarget, target.direction)) {
                return; 
            }
        }
        switch (newLoc.directionTo(target.location).ordinal()) {
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
        if (!rc.isMovementReady()) return;
        if (!CombatCommon.canMoveS) return;
        moveDir = Direction.SOUTH;
        newLoc = myLoc.add(moveDir);
        if (isCat) {
            if (catDist(newLoc) >= curCatDist) {
                return;
            }
        }
        dxToTarget = target.location.x - newLoc.x;
        dyToTarget = target.location.y - newLoc.y;
        if (isCat) {
            if (canCatSense(-dxToTarget, -dyToTarget, target.direction)) {
                return; 
            }
        }
        switch (newLoc.directionTo(target.location).ordinal()) {
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
        if (!rc.isMovementReady()) return;
        if (!CombatCommon.canMoveSW) return;
        moveDir = Direction.SOUTHWEST;
        newLoc = myLoc.add(moveDir);
        if (isCat) {
            if (catDist(newLoc) >= curCatDist) {
                return;
            }
        }
        dxToTarget = target.location.x - newLoc.x;
        dyToTarget = target.location.y - newLoc.y;
        if (isCat) {
            if (canCatSense(-dxToTarget, -dyToTarget, target.direction)) {
                return; 
            }
        }
        switch (newLoc.directionTo(target.location).ordinal()) {
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
        if (!rc.isMovementReady()) return;
        if (!CombatCommon.canMoveW) return;
        moveDir = Direction.WEST;
        newLoc = myLoc.add(moveDir);
        if (isCat) {
            if (catDist(newLoc) >= curCatDist) {
                return;
            }
        }
        dxToTarget = target.location.x - newLoc.x;
        dyToTarget = target.location.y - newLoc.y;
        if (isCat) {
            if (canCatSense(-dxToTarget, -dyToTarget, target.direction)) {
                return; 
            }
        }
        switch (newLoc.directionTo(target.location).ordinal()) {
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
        if (!rc.isMovementReady()) return;
        if (!CombatCommon.canMoveNW) return;
        moveDir = Direction.NORTHWEST;
        newLoc = myLoc.add(moveDir);
        if (isCat) {
            if (catDist(newLoc) >= curCatDist) {
                return;
            }
        }
        dxToTarget = target.location.x - newLoc.x;
        dyToTarget = target.location.y - newLoc.y;
        if (isCat) {
            if (canCatSense(-dxToTarget, -dyToTarget, target.direction)) {
                return; 
            }
        }
        switch (newLoc.directionTo(target.location).ordinal()) {
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
        newLoc = myLoc.add(moveDir);
        if (isCat) {
            if (catDist(newLoc) >= curCatDist) {
                return;
            }
        }
        dxToTarget = target.location.x - newLoc.x;
        dyToTarget = target.location.y - newLoc.y;
        if (isCat) {
            if (canCatSense(-dxToTarget, -dyToTarget, target.direction)) {
                return; 
            }
        }
        switch (newLoc.directionTo(target.location).ordinal()) {
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
        score = CombatStrategy.getCellScoreFor(moveDir, Direction.NORTH);
        if (moveCost == 1) score += 10;
        facingTarget = canSense(dxToTarget, dyToTarget, Direction.NORTH);
        if (!facingTarget) {
            score -= 5;
        }
        if ((CombatStrategy.moveMask & (1 << moveDir.ordinal())) != 0) {
            score += 30;
        }
        if (Direction.NORTH != myDir) {
            // prefer turning
            score += 2;
        }
        if (score > best) {
            best = score;
            md = moveDir;
            fd = Direction.NORTH;
        }
    }

    private static void faceNE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, Direction.NORTHEAST);
        if (moveCost == -1) return;
        score = CombatStrategy.getCellScoreFor(moveDir, Direction.NORTHEAST);
        if (moveCost == 1) score += 10;
        facingTarget = canSense(dxToTarget, dyToTarget, Direction.NORTHEAST);
        if (!facingTarget) {
            score -= 5;
        }
        if ((CombatStrategy.moveMask & (1 << moveDir.ordinal())) != 0) {
            score += 30;
        }
        if (Direction.NORTHEAST != myDir) {
            // prefer turning
            score += 2;
        }
        if (score > best) {
            best = score;
            md = moveDir;
            fd = Direction.NORTHEAST;
        }
    }

    private static void faceE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, Direction.EAST);
        if (moveCost == -1) return;
        score = CombatStrategy.getCellScoreFor(moveDir, Direction.EAST);
        if (moveCost == 1) score += 10;
        facingTarget = canSense(dxToTarget, dyToTarget, Direction.EAST);
        if (!facingTarget) {
            score -= 5;
        }
        if ((CombatStrategy.moveMask & (1 << moveDir.ordinal())) != 0) {
            score += 30;
        }
        if (Direction.EAST != myDir) {
            // prefer turning
            score += 2;
        }
        if (score > best) {
            best = score;
            md = moveDir;
            fd = Direction.EAST;
        }
    }

    private static void faceSE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        score = CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTHEAST);
        if (moveCost == 1) score += 10;
        facingTarget = canSense(dxToTarget, dyToTarget, Direction.SOUTHEAST);
        if (!facingTarget) {
            score -= 5;
        }
        if ((CombatStrategy.moveMask & (1 << moveDir.ordinal())) != 0) {
            score += 30;
        }
        if (Direction.SOUTHEAST != myDir) {
            // prefer turning
            score += 2;
        }
        if (score > best) {
            best = score;
            md = moveDir;
            fd = Direction.SOUTHEAST;
        }
    }

    private static void faceS() throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, Direction.SOUTH);
        if (moveCost == -1) return;
        score = CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTH);
        if (moveCost == 1) score += 10;
        facingTarget = canSense(dxToTarget, dyToTarget, Direction.SOUTH);
        if (!facingTarget) {
            score -= 5;
        }
        if ((CombatStrategy.moveMask & (1 << moveDir.ordinal())) != 0) {
            score += 30;
        }
        if (Direction.SOUTH != myDir) {
            // prefer turning
            score += 2;
        }
        if (score > best) {
            best = score;
            md = moveDir;
            fd = Direction.SOUTH;
        }
    }

    private static void faceSW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        score = CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTHWEST);
        if (moveCost == 1) score += 10;
        facingTarget = canSense(dxToTarget, dyToTarget, Direction.SOUTHWEST);
        if (!facingTarget) {
            score -= 5;
        }
        if ((CombatStrategy.moveMask & (1 << moveDir.ordinal())) != 0) {
            score += 30;
        }
        if (Direction.SOUTHWEST != myDir) {
            // prefer turning
            score += 2;
        }
        if (score > best) {
            best = score;
            md = moveDir;
            fd = Direction.SOUTHWEST;
        }
    }

    private static void faceW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, Direction.WEST);
        if (moveCost == -1) return;
        score = CombatStrategy.getCellScoreFor(moveDir, Direction.WEST);
        if (moveCost == 1) score += 10;
        facingTarget = canSense(dxToTarget, dyToTarget, Direction.WEST);
        if (!facingTarget) {
            score -= 5;
        }
        if ((CombatStrategy.moveMask & (1 << moveDir.ordinal())) != 0) {
            score += 30;
        }
        if (Direction.WEST != myDir) {
            // prefer turning
            score += 2;
        }
        if (score > best) {
            best = score;
            md = moveDir;
            fd = Direction.WEST;
        }
    }

    private static void faceNW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, Direction.NORTHWEST);
        if (moveCost == -1) return;
        score = CombatStrategy.getCellScoreFor(moveDir, Direction.NORTHWEST);
        if (moveCost == 1) score += 10;
        facingTarget = canSense(dxToTarget, dyToTarget, Direction.NORTHWEST);
        if (!facingTarget) {
            score -= 5;
        }
        if ((CombatStrategy.moveMask & (1 << moveDir.ordinal())) != 0) {
            score += 30;
        }
        if (Direction.NORTHWEST != myDir) {
            // prefer turning
            score += 2;
        }
        if (score > best) {
            best = score;
            md = moveDir;
            fd = Direction.NORTHWEST;
        }
    }


    private static int catDist(MapLocation loc) {
        int catX = 2 * target.location.x + 1;
        int catY = 2 * target.location.y + 1;
        int dx = catX - 2 * loc.x;
        int dy = catY - 2 * loc.y;
        return dx * dx + dy * dy;
    }
}