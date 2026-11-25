

package awubot.robot.king;

import battlecode.common.*;
import awubot.Game;
import awubot.Pathfinder;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import static awubot.robot.king.RatKing.*;
import static awubot.Game.*;
import static awubot.util.Util.*;

import awubot.CellTracker;
import awubot.util.string.FastIntSet;

// TODO: clean up

public class KingMiningStrategy extends Strategy {

    private MapLocation mineTarget = null;
    private MapLocation centerTarget;
    private boolean foundCheeseMine = false;

    // A king whose walk gets no closer to its mine gives the mine up and takes the next known one: the mine may be
    // out of a king's reach (diamond: each king's nearest mine sits in a pocket a 3x3 king cannot enter; our A king
    // wandered to another mine, the image team's king paced in its corner for the rest of the game).
    private static final int STUCK_MOVES = 30, STUCK_NEAR_SQ = 18;
    private final FastIntSet unreachable = new FastIntSet();
    private int bestDistSq, movesWithoutGain;

    private final int tabooMinX;
    private final int tabooMaxX;
    private final int tabooMinY;
    private final int tabooMaxY;

    public KingMiningStrategy() {
        if (mapWidth < 40 && mapHeight < 40) {
            tabooMinX = mapWidth / 3;
            tabooMaxX = 2 * mapWidth / 3;
            tabooMinY = mapHeight / 3;
            tabooMaxY = 2 * mapHeight / 3;
        } else if (mapWidth < 50 && mapHeight < 50) {
            tabooMinX = 3 * mapWidth / 7;
            tabooMaxX = 4 * mapWidth / 7;
            tabooMinY = 3 * mapHeight / 7;
            tabooMaxY = 4 * mapHeight / 7;
        } else {
            tabooMinX = 4 * mapWidth / 9;
            tabooMaxX = 5 * mapWidth / 9;
            tabooMinY = 4 * mapHeight / 9;
            tabooMaxY = 5 * mapHeight / 9;
        }

        centerTarget = selectSafeCenterTarget();
    }

    private boolean isInTabooZone(MapLocation aLoc) {
        return aLoc.x >= tabooMinX && aLoc.x <= tabooMaxX
            && aLoc.y >= tabooMinY && aLoc.y <= tabooMaxY;
    }

    private MapLocation selectSafeCenterTarget() {
        MapLocation myCenter = new MapLocation(mapWidth / 2, mapHeight / 2);
        if (!isInTabooZone(myCenter)) {
            return myCenter;
        }
        MapLocation myClosest = null;
        int myBestDist = Integer.MAX_VALUE;
        MapLocation[] myCandidates = {
            new MapLocation(tabooMinX - 1, mapHeight / 2),
            new MapLocation(tabooMaxX + 1, mapHeight / 2),
            new MapLocation(mapWidth / 2, tabooMinY - 1),
            new MapLocation(mapWidth / 2, tabooMaxY + 1)
        };
        for (MapLocation myCandidate : myCandidates) {
            if (myCandidate.x < 0 || myCandidate.x >= mapWidth) continue;
            if (myCandidate.y < 0 || myCandidate.y >= mapHeight) continue;
            int myDist = myLoc.distanceSquaredTo(myCandidate);
            if (myDist < myBestDist) {
                myBestDist = myDist;
                myClosest = myCandidate;
            }
        }
        return myClosest != null ? myClosest : myCenter;
    }

    public MapLocation getMineTarget() {
        return mineTarget;
    }

    public MapLocation getCenterTarget() {
        return centerTarget;
    }

    @Override
    public boolean runTick() throws GameActionException {
        scanForCheeseMines();

        if (!foundCheeseMine) {
            selectFromKnownMines();
        }

        if (foundCheeseMine && givesUpMine()) selectFromKnownMines();

        if (!foundCheeseMine) {
            moveTowardCenter();
        } else {
            GlobalArray.writeKingMineAssignment(kingId, mineTarget);
            moveTowardMine();
        }

        tryCollectCheese();
        return true;
    }

    // made up a better function for collecting cheese
    public boolean tryCollectCheese() throws GameActionException {
        if (!foundCheeseMine) return false;
        MapLocation nearestCheese = null;
        for(var info : rc.senseNearbyMapInfos()) {
            if (info.getCheeseAmount() > 0) {
                MapLocation checkLoc = info.getMapLocation();
                if(chebyshevDistance(checkLoc, mineTarget) > 4) continue;
                if (nearestCheese == null || myLoc.distanceSquaredTo(checkLoc) < myLoc.distanceSquaredTo(nearestCheese)) {
                    nearestCheese = checkLoc;
                }
            }
        }
        if (nearestCheese != null) {
            if (!rc.canPickUpCheese(nearestCheese)) {
                if(rc.isMovementReady()) {
                    Direction dir = Pathfinder.getMove(nearestCheese);
                    digDirtInDirection(dir);
                    if (rc.canMove(dir)) {
                        RatKing.rawMove(dir);
                    }
                }
                return true;
            } else {
                {
                    var loc = myLoc.translate(-1, -1);
                    if(rc.canPickUpCheese(loc)) {
                        rc.pickUpCheese(loc);
                    }
                }
                {
                    var loc = myLoc.translate(-1, 0);
                    if(rc.canPickUpCheese(loc)) {
                        rc.pickUpCheese(loc);
                    }
                }
                {
                    var loc = myLoc.translate(-1, 1);
                    if(rc.canPickUpCheese(loc)) {
                        rc.pickUpCheese(loc);
                    }
                }
                {
                    var loc = myLoc.translate(0, -1);
                    if(rc.canPickUpCheese(loc)) {
                        rc.pickUpCheese(loc);
                    }
                }
                {
                    var loc = myLoc.translate(0, 0);
                    if(rc.canPickUpCheese(loc)) {
                        rc.pickUpCheese(loc);
                    }
                }
                {
                    var loc = myLoc.translate(0, 1);
                    if(rc.canPickUpCheese(loc)) {
                        rc.pickUpCheese(loc);
                    }
                }
                {
                    var loc = myLoc.translate(1, -1);
                    if(rc.canPickUpCheese(loc)) {
                        rc.pickUpCheese(loc);
                    }
                }
                {
                    var loc = myLoc.translate(1, 0);
                    if(rc.canPickUpCheese(loc)) {
                        rc.pickUpCheese(loc);
                    }
                }
                {
                    var loc = myLoc.translate(1, 1);
                    if(rc.canPickUpCheese(loc)) {
                        rc.pickUpCheese(loc);
                    }
                }
            }
        }
        MapLocation resetLoc = mineTarget;
        // unrolled version:
        {
            MapLocation checkLoc = mineTarget.translate(0, 1);
            if (rc.canSenseLocation(checkLoc) && rc.senseMapInfo(checkLoc).hasCheeseMine()) {
                resetLoc = checkLoc;
            }
        }
        {
            MapLocation checkLoc = mineTarget.translate(1, 0);
            if (rc.canSenseLocation(checkLoc) && rc.senseMapInfo(checkLoc).hasCheeseMine()) {
                resetLoc = checkLoc;
            }
        }
        {
            MapLocation checkLoc = mineTarget.translate(1, 1);
            if (rc.canSenseLocation(checkLoc) && rc.senseMapInfo(checkLoc).hasCheeseMine()) {
                resetLoc = checkLoc;
            }
        }
        if (!myLoc.equals(resetLoc)) {
            if(rc.isMovementReady()) {
                Direction dir = Pathfinder.getMove(resetLoc);
                digDirtInDirection(dir);
                if (rc.canMove(dir)) {
                    RatKing.rawMove(dir);
                }
            }
            return true;
        }
        return false;
    }

    public boolean scanForCheeseMines() throws GameActionException {
        MapLocation myBestMine = null;
        int myBestDist = Integer.MAX_VALUE;

        for (MapInfo myInfo : rc.senseNearbyMapInfos()) {
            if (myInfo.hasCheeseMine()) {
                MapLocation myMine = myInfo.getMapLocation();
                int encoded = encode2x2(myMine);
                GlobalArray.registerGlobalCheeseMine(encoded);

                if (unreachable.contains(encoded)) continue;
                if (GlobalArray.isMineAssignedToAnyKing(myMine)) continue;
                // if (isInTabooZone(myMine)) continue;

                int myDist = myLoc.distanceSquaredTo(myMine);
                if (myDist < myBestDist) {
                    myBestDist = myDist;
                    myBestMine = myMine;
                }
            }
        }

        if (myBestMine != null) {
            if (!myBestMine.equals(mineTarget)) {
                bestDistSq = Integer.MAX_VALUE;
                movesWithoutGain = 0;
            }
            mineTarget = myBestMine;
            foundCheeseMine = true;
            GlobalArray.writeKingMineAssignment(kingId, mineTarget);
            return true;
        }
        return false;
    }

    private static int stuckTurns = 0;
    private static boolean pending5x5 = false;
    private void moveIfBugTargetFar(MapLocation target) throws GameActionException {
        var dir = Pathfinder.getMove(target);
        if(Pathfinder.getBugPos().distanceSquaredTo(myLoc) > 2) {
            if(dir != Direction.CENTER) {
                stuckTurns = 0;
                digDirtInDirection(dir);
                if(rc.canMove(dir)) {
                    RatKing.rawMove(dir);
                    if(pending5x5 && CellTracker.isPassable5x5(myLoc)) {
                        RatKing.is5x5 = true;
                    }
                }
            } else if(!RatKing.spawnedThisTurn) {
                stuckTurns++;
                if(stuckTurns == 17) {
                    GlobalArray.expandKing();
                    pending5x5 = true;
                }
            }
        }
    }

    private void moveTowardCenter() throws GameActionException {
        moveIfBugTargetFar(centerTarget);
    }

    private void moveTowardMine() throws GameActionException {
        if (!rc.isMovementReady() || mineTarget == null) return;
        if (myLoc.distanceSquaredTo(mineTarget) < 5) {
            // find nearest cheese
            int closestDist = Integer.MAX_VALUE;
            MapLocation closestLoc = null;
            for (MapInfo info : rc.senseNearbyMapInfos(mineTarget, 13)) {
                if (info.getCheeseAmount() > 0) {
                    int dist = myLoc.distanceSquaredTo(info.getMapLocation());
                    if (dist < closestDist) {
                        closestDist = dist;
                        closestLoc = info.getMapLocation();
                    }
                }
            }
            if (closestLoc == null) return;
            moveIfBugTargetFar(closestLoc);
            return;
        }
        moveIfBugTargetFar(mineTarget);
    }

    private boolean givesUpMine() throws GameActionException {
        if (!rc.isMovementReady()) return false;
        int d = myLoc.distanceSquaredTo(mineTarget);
        if (d < bestDistSq) {
            bestDistSq = d;
            movesWithoutGain = 0;
            return false;
        }
        if (d <= STUCK_NEAR_SQ || ++movesWithoutGain < STUCK_MOVES || !hasOtherMine()) return false;
        unreachable.add(encode2x2(mineTarget));
        GlobalArray.writeKingMineAssignment(kingId, null);
        mineTarget = null;
        foundCheeseMine = false;
        return true;
    }

    // another known mine, free and not given up, to switch to (with none the king keeps walking to this one)
    private boolean hasOtherMine() throws GameActionException {
        int target = encode2x2(mineTarget);
        int n = Game.knownCheeseMines.set.length();
        for (int i = 0; i < n && i < 10; i++) {
            int m = Game.knownCheeseMines.at(i);
            if (m == target || unreachable.contains(m)) continue;
            MapLocation loc = decode2x2(m);
            if (loc.distanceSquaredTo(mineTarget) >= 25 && !GlobalArray.isMineAssignedToAnyKing(loc)) return true;
        }
        return false;
    }

    private void selectFromKnownMines() throws GameActionException {
        if (mineTarget != null) return;

        MapLocation myBestMine = null;
        int myBestDist = Integer.MAX_VALUE;

        int myMineCount = Game.knownCheeseMines.set.length();
        for (int i = 0; i < myMineCount && i < 10; i++) {
            MapLocation myMine = decode2x2(Game.knownCheeseMines.at(i));
            if (myMine == null) continue;
            if (unreachable.contains(encode2x2(myMine))) continue;
            if (GlobalArray.isMineAssignedToAnyKing(myMine)) continue;
            // if (isInTabooZone(myMine)) continue;

            int myDist = myLoc.distanceSquaredTo(myMine);
            if (myDist < myBestDist) {
                myBestDist = myDist;
                myBestMine = myMine;
            }
        }

        if (myBestMine != null) {
            if (!myBestMine.equals(mineTarget)) {
                bestDistSq = Integer.MAX_VALUE;
                movesWithoutGain = 0;
            }
            mineTarget = myBestMine;
            foundCheeseMine = true;
            GlobalArray.writeKingMineAssignment(kingId, mineTarget);
        }
    }
}