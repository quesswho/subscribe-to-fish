package awubot.robot.rat;

import battlecode.common.*;
import awubot.CellTracker;
import awubot.Pathfinder;
import awubot.RobotTracker;
import awubot.comms.CatClock;
import awubot.comms.GlobalArray;
import awubot.comms.Squeaker;
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
        primaryStrategy = new MinerStrategy();
        combatStrategy = new CombatStrategy();
        Pathfinder.init();
        AntColony.init();
    }

    private static boolean isRobotAt(MapLocation loc, MapLocation robotLoc) throws GameActionException {
        RobotInfo robot = rc.senseRobotAtLocation(loc);
        return robot != null && robot.location.equals(robotLoc);
    }

    public static void considerPlacingCatTrap() throws GameActionException {
        if (!rc.isActionReady()) return;
        if (RobotTracker.numCats > 0 && rc.isCooperation() && RobotTracker.numEnemies == 0 && EnemyManager.numKnownEnemies == 0) {
            RobotInfo cat = RobotTracker.sortedCats[0];
            if (CatClock.hold(cat)) return;
            CatLaneTrap.tryPlace(cat);
            if (cat.location.isWithinDistanceSquared(myLoc, 9, cat.direction, 90, true)) {
                for (Direction off : relatedDirections(myLoc.directionTo(cat.location))) {
                    MapLocation targ = myLoc.add(off);
                    MapLocation bruh = targ.add(targ.directionTo(cat.location));
                    if (rc.canSenseLocation(bruh) && isRobotAt(bruh, cat.location)) {
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
                        if (rc.canSenseLocation(bruh) && isRobotAt(bruh, cat.location)) {
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
        shouldGlance = true;

        CellTracker.storeVisibleCells();
        AntColony.preTick();

        tryPickUpCheese();
        tryTransferCheese();

        if (rc.isBeingCarried()) {
            RobotInfo carrier = rc.senseRobotAtLocation(myLoc);
            if (rc.getHealth() < 15 && carrier.getTeam() != team) {
                rc.disintegrate();
                Clock.yield();
            }
            // Allies that cannot see the carrier hear where it is: one that turns to it may grab it, which drops us.
            if (carrier != null && carrier.getTeam() != team) {
                Squeaker.squeakEnemy(EnemyManager.encodeEnemy(new awubot.comms.Enemy(carrier))
                    | (EnemyManager.NULL << EnemyManager.ENC_WIDTH));
            }
            return;
        }

        if (rc.isBeingThrown()) {
            return;
        }
        if (combatStrategy.runTick()) {
            considerPlacingCatTrap();
            return;
        }

        // all nonessential systems go here, only run if we are not in combat
        RobotTracker.reset();
        CellTracker.checkSymmetry();
        RobotTracker.updateHistoricalCatLocs();

        considerPlacingCatTrap();
        primaryStrategy.runTick();
        combatStrategy.miniTick();

        isLastMove = true;
        executeGlance();
    }

    private static void executeGlance() throws GameActionException {
        if (!rc.canTurn() || !shouldGlance) return;
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
        if(rc.canPickUpCheese(myLoc)) pickUpCheese(myLoc);
        var dirs = relatedDirections(myDir);
        for(var dir : dirs) {
            var loc = myLoc.add(dir);
            if(rc.canPickUpCheese(loc)) pickUpCheese(loc);
        }
    }

    // Only as much as Delivery allows: in a long cooperation a pile taken whole slows its carrier so much that
    // none of the load lands.
    private static void pickUpCheese(MapLocation loc) throws GameActionException {
        int room = Delivery.maxLoad() - rc.getRawCheese();
        if (room > 0) rc.pickUpCheese(loc, room);
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
        MapLocation at = myLoc.add(myLoc.directionTo(info.location));
        if (info.getType() == UnitType.CAT) {
            rc.attack(at, lateCatCheese());
            return;
        }
        if (info.getType() != UnitType.BABY_RAT) {
            damage = 10;
        }
        rc.attack(at, CombatStrategy.getCheeseNeeded(damage - 10));
    }

    // Near the end the bank scores nothing (only a points tie reads it) while cat damage is points: a bite on a cat
    // spends a share of what the kings will not need to round 2000 (+ceil(sqrt(c)) damage for c cheese).
    private static final int LATE_CAT_CHEESE_ROUND = 1850;

    private static int lateCatCheese() {
        if (time < LATE_CAT_CHEESE_ROUND) return 0;
        int reserve = GlobalArray.countRatKings() * GameConstants.RAT_KING_CHEESE_CONSUMPTION
            * (GameConstants.GAME_MAX_NUMBER_OF_ROUNDS + 1 - time) + 100;
        int spare = rc.getGlobalCheese() - reserve;
        return spare <= 0 ? 0 : Math.min(100, spare / 8);
    }
}
