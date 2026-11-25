package awubot.robot.rat;

import battlecode.common.*;
import awubot.CellTracker;
import awubot.Pathfinder;
import awubot.RobotTracker;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import awubot.robot.rat.combat.CombatStrategy;
import awubot.robot.rat.goHome.AntColony;
import awubot.util.Rng;
import static awubot.Game.*;
import static awubot.util.Util.*;
import awubot.util.Profiler;
import awubot.comms.EnemyManager;

public class BabyRat {
    private static Direction lastGlance;
    public static boolean shouldGlance;
    public static Strategy primaryStrategy;
    public static CombatStrategy combatStrategy;

    public static boolean transferSwitch = false;

    public static void init() throws GameActionException {
        int payload = GlobalArray.loadSpawnInfo();
        // println("BabyRat payload: " + payload + " id " + rc.getID() + " index " + myIndex);
        primaryStrategy = new MinerStrategy();
        combatStrategy = new CombatStrategy();
        Pathfinder.init();
        AntColony.init();
    }

    public static void considerPlacingCatTrap() throws GameActionException {
        if (!rc.isActionReady()) return;
        if (RobotTracker.numCats > 0 && rc.isCooperation() && RobotTracker.numEnemies == 0 && EnemyManager.numKnownEnemies == 0) {
            RobotInfo cat = RobotTracker.sortedCats[0];
            if (cat.location.isWithinDistanceSquared(myLoc, 9, cat.direction, 90, true)) {
                for (Direction off : relatedDirections(myLoc.directionTo(cat.location))) {
                    MapLocation targ = myLoc.add(off);
                    MapLocation bruh = targ.add(targ.directionTo(cat.location));
                    if (rc.canSenseLocation(bruh) && rc.senseRobotAtLocation(bruh).location.equals(cat.location)) {
                        if (rc.canPlaceCatTrap(targ)) {
                            rc.placeCatTrap(targ);
                        }
                    }
                }
                if (rc.canTurn()) {
                    for (Direction dir : directions) {
                        if (dir == myDir) continue;
                        if (canSense(myLoc, cat.location, dir)) {
                            turn(dir);
                            break;
                        }
                    }
                }
                for (Direction _moveDir : relatedDirections(myLoc.directionTo(cat.location))) {
                    Direction moveDir = _moveDir.opposite();
                    if (rc.canMove(moveDir)) {
                        rawMove(moveDir);
                        break;
                    }
                }
                if (cat.location.isWithinDistanceSquared(myLoc, 9, cat.direction, 90, true)) {
                    for (Direction off : relatedDirections(myLoc.directionTo(cat.location))) {
                        MapLocation targ = myLoc.add(off);
                        MapLocation bruh = targ.add(targ.directionTo(cat.location));
                        if (rc.canSenseLocation(bruh) && rc.senseRobotAtLocation(bruh).location.equals(cat.location)) {
                            if (rc.canPlaceCatTrap(targ)) {
                                rc.placeCatTrap(targ);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void run() throws GameActionException {
        isLastMove = false;
        // indicate("cheesemines: " + GlobalArray.cheeseMines.set.length());
        shouldGlance = true;

        CellTracker.storeVisibleCells();
        AntColony.preTick();

        tryPickUpCheese();
        tryTransferCheese();

        if (rc.isBeingCarried()) {
            if (rc.getHealth() < 15 && rc.senseRobotAtLocation(myLoc).getTeam() != team) {
                rc.disintegrate();
                Clock.yield();
            }
            return;
        }

        if (rc.isBeingThrown()) {
            return;
        }

        // indicate("before combat strat: " + Clock.getBytecodeNum());
        if (combatStrategy.runTick()) {
            considerPlacingCatTrap();
            return;
        }

        // all nonessential systems go here, only run if we are not in combat
        RobotTracker.reset();
        CellTracker.checkSymmetry();
        RobotTracker.updateHistoricalCatLocs();

        // indicate("sym " + CellTracker.hsym + " " + CellTracker.vsym + " " + CellTracker.rsym);

        considerPlacingCatTrap();
        primaryStrategy.runTick();
        combatStrategy.miniTick();

        isLastMove = true;
        executeGlance();
    }

    private static void executeGlance() throws GameActionException {
        if (!rc.canTurn() || !shouldGlance) return;

        // indicate("glancing");
        Direction myLeftDir = myDir.rotateLeft().rotateLeft();
        Direction myRightDir = myDir.rotateRight().rotateRight();

        if (lastGlance == myLeftDir) {
            turn(myRightDir);
            lastGlance = myRightDir;
        } else if (lastGlance == myRightDir || Rng.mcg() >> 30 != 0) {
            turn(myLeftDir);
            lastGlance = myLeftDir;
        } else {
            turn(myRightDir);
            lastGlance = myRightDir;
        }
    }

    // IF TLE: unroll
    public static void tryPickUpCheese() throws GameActionException {
        if(rc.canPickUpCheese(myLoc)) {
            rc.pickUpCheese(myLoc);
        }
        var dirs = relatedDirections(myDir);
        for(var dir : dirs) {
            var loc = myLoc.add(dir);
            if(rc.canPickUpCheese(loc)) {
                rc.pickUpCheese(loc);
            }
        }
    }

    // IF TLE: spam this less / optimise
    private static void tryTransferCheese() throws GameActionException {
        int myCheese = rc.getRawCheese();
        if (myCheese <= 0) return;
        for(int i = 0; i < 5; i++) {
            var kingLoc = GlobalArray.getRatKing(i);
            if(kingLoc != null && kingLoc.distanceSquaredTo(myLoc) <= 18) {
                AntColony.reachedHome();
                var dir = kingLoc.directionTo(myLoc).rotateLeft();
                for(int j = 0; j < 3; j++) {
                    var loc = kingLoc.add(dir);
                    if(rc.canTransferCheese(loc, myCheese)) {
                        rc.transferCheese(loc, myCheese);
                        transferSwitch = true;
                        return;
                    }
                    dir = dir.rotateRight();
                }
            }
        }
    }

    public static boolean isLastMove;
    public static void rawMove(Direction dir) throws GameActionException {
        if(dir == myDir) {
            rawMove();
            return;
        }
        _rawMove(dir);
        tryPickUpCheese();
        tryTransferCheese();
        AntColony.markCurrent();
        // IF TLE: optimise this
        if (!isLastMove) {
            RobotTracker.updateRobots();
            if(Clock.getBytecodesLeft() >= 3000) CellTracker.storeVisibleCells();
        }
    }

    public static void turn(Direction dir) throws GameActionException {
        if (dir == myDir) return;
        _turn(dir);
        tryPickUpCheese();
        tryTransferCheese();
        if (!isLastMove) {
            RobotTracker.updateRobots();
            if(Clock.getBytecodesLeft() >= 3000) CellTracker.storeVisibleCells();
        }
    }

    public static void rawMove() throws GameActionException {
        _rawMove(myDir);
        tryPickUpCheese();
        tryTransferCheese();
        AntColony.markCurrent();
        if (!isLastMove) {
            RobotTracker.updateRobots();
            if(Clock.getBytecodesLeft() >= 2000) CellTracker.storeVisibleCellsPostMoveForward();
        }
    }

    public static void autoMove(Direction dir) throws GameActionException {
        if(dir == Direction.CENTER) return;
        if(rc.canTurn()) turn(dir);
        var nextLoc = myLoc.add(dir);
        if(rc.onTheMap(nextLoc) && canSense(myLoc, nextLoc, myDir) && rc.senseMapInfo(nextLoc).isDirt()) {
            if(rc.canRemoveDirt(nextLoc)) rc.removeDirt(nextLoc);
            else return;
        }
        if(dir == myDir) {
            // bail out if we see that we will walk into a cat
            for(int i = RobotTracker.numCats - 1; i >= 0; i--) {
                var cat = RobotTracker.sortedCats[i];
                if(cat.location.isWithinDistanceSquared(nextLoc, 5, cat.direction, 180, true)) {
                    // indicate("avoiding cat at " + nextLoc);
                    return;
                }
            }
            if(rc.canMoveForward()) rawMove();
        } else {
            // if we can't turn, then strafe
            if(rc.canMove(dir)) rawMove(dir);
        }
    }

    public static void attack(RobotInfo info) throws GameActionException {
        attack(info, CombatStrategy.getBiteDamage());
    }

    public static void attack(RobotInfo info, int damage) throws GameActionException {
        if (info.getType() != UnitType.BABY_RAT) {
            damage = 10;
        }
        rc.attack(myLoc.add(myLoc.directionTo(info.location)), CombatStrategy.getCheeseNeeded(damage - 10));
    }
}
