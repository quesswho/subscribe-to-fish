

package awubot.robot.king;

import battlecode.common.*;
import awubot.robot.Strategy;
import awubot.RobotTracker;
import awubot.Pathfinder;

import static awubot.robot.king.RatKing.*;
import static awubot.Game.*;
import static awubot.util.Util.*;
import awubot.CellTracker;

public class KingCombatStrategy extends Strategy {

    private boolean placeDefensiveDirt() throws GameActionException {
        if (!rc.isActionReady()) return false;
        if (RobotTracker.numCats == 0) return false;

        RobotInfo nearestCat = RobotTracker.sortedCats[0];
        MapLocation catLoc = nearestCat.location;
        if (nearestCat == null) return false;
        if (catLoc.distanceSquaredTo(myLoc) >= 20) return false;

        int bestScore = -1000000;
        MapLocation bestLoc = null;

        for (MapInfo info: rc.senseNearbyMapInfos()) {
            MapLocation loc = info.getMapLocation();
            if (!rc.canPlaceDirt(loc)) continue;
            if (loc.distanceSquaredTo(catLoc) > myLoc.distanceSquaredTo(catLoc)) continue;

            int numDirts = 0;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (!rc.onTheMap(new MapLocation(loc.x + dx, loc.y + dy))) continue;
                    MapInfo checkInfo = rc.senseMapInfo(new MapLocation(loc.x + dx, loc.y + dy));
                    if (checkInfo.isDirt()) {
                        numDirts++;
                    }
                }
            }

            // i tested this with thresh = 4
            // it performs basically the same
            if (numDirts >= 2) continue;

            int score = -loc.distanceSquaredTo(catLoc);

            if (score > bestScore) {
                bestScore = score;
                bestLoc = loc;
            }
        }

        if (bestLoc != null) {
            rc.placeDirt(bestLoc);
            return true;
        }

        return false;

    }

    // TODO: only dig if we havent seen the cat in some time
    private boolean digSurroundingDirt() throws GameActionException {
        if (!rc.isActionReady()) return false;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (Math.max(Math.abs(dx), Math.abs(dy)) < 2) continue;
                MapLocation checkLoc = new MapLocation(myLoc.x + dx, myLoc.y + dy);
                if (rc.canRemoveDirt(checkLoc) && rc.getGlobalCheese() >= 100) {
                    rc.removeDirt(checkLoc);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean runTick() throws GameActionException {
        boolean hasThreat = (RobotTracker.numEnemies + RobotTracker.numCats) > 0;
        if(!hasThreat) return false;
        var score0 = 0;
        var score1 = 0;
        var score2 = 0;
        var score3 = 0;
        var score4 = 0;
        var score5 = 0;
        var score6 = 0;
        var score7 = 0;
                {
            var loc = myLoc.translate(0, 2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score7 = -1000;
                score0 = -1000;
                score1 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score7 -= 2;
                score0 -= 2;
                score1 -= 2;
            }
        }

                {
            var loc = myLoc.translate(1, 2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score0 = -1000;
                score1 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score0 -= 2;
                score1 -= 2;
            }
        }

                {
            var loc = myLoc.translate(2, 2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score1 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score1 -= 2;
            }
        }

                {
            var loc = myLoc.translate(2, 1);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score1 = -1000;
                score2 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score1 -= 2;
                score2 -= 2;
            }
        }

                {
            var loc = myLoc.translate(2, 0);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score1 = -1000;
                score2 = -1000;
                score3 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score1 -= 2;
                score2 -= 2;
                score3 -= 2;
            }
        }

                {
            var loc = myLoc.translate(2, -1);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score2 = -1000;
                score3 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score2 -= 2;
                score3 -= 2;
            }
        }

                {
            var loc = myLoc.translate(2, -2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score3 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score3 -= 2;
            }
        }

                {
            var loc = myLoc.translate(1, -2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score3 = -1000;
                score4 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score3 -= 2;
                score4 -= 2;
            }
        }

                {
            var loc = myLoc.translate(0, -2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score3 = -1000;
                score4 = -1000;
                score5 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score3 -= 2;
                score4 -= 2;
                score5 -= 2;
            }
        }

                {
            var loc = myLoc.translate(-1, -2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score4 = -1000;
                score5 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score4 -= 2;
                score5 -= 2;
            }
        }

                {
            var loc = myLoc.translate(-2, -2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score5 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score5 -= 2;
            }
        }

                {
            var loc = myLoc.translate(-2, -1);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score5 = -1000;
                score6 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score5 -= 2;
                score6 -= 2;
            }
        }

                {
            var loc = myLoc.translate(-2, 0);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score5 = -1000;
                score6 = -1000;
                score7 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score5 -= 2;
                score6 -= 2;
                score7 -= 2;
            }
        }

                {
            var loc = myLoc.translate(-2, 1);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score6 = -1000;
                score7 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score6 -= 2;
                score7 -= 2;
            }
        }

                {
            var loc = myLoc.translate(2, -2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score7 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score7 -= 2;
            }
        }

                {
            var loc = myLoc.translate(1, -2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score7 = -1000;
                score0 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score7 -= 2;
                score0 -= 2;
            }
        }

        for(int idx = 0; idx < RobotTracker.numEnemies; idx++) {
            var robot = RobotTracker.sortedEnemies[idx];
            if(robot == null) continue;
            var weight = switch(robot.type) {
                case BABY_RAT -> 1;
                case RAT_KING -> 3;
                case CAT -> 5;
            };
            var dir = myLoc.directionTo(robot.location);
            switch(dir.ordinal()) {
                case 0 -> {
                    score0           -= weight * 3;
                    score6 += weight;
                    score2 += weight;
                    score3 += weight + 1;
                    score4 += weight + 1;
                    score5 += weight + 1;
                }
                case 1 -> {
                    score1           -= weight * 3;
                    score7 += weight;
                    score3 += weight;
                    score4 += weight + 1;
                    score5 += weight + 1;
                    score6 += weight + 1;
                }
                case 2 -> {
                    score2           -= weight * 3;
                    score0 += weight;
                    score4 += weight;
                    score5 += weight + 1;
                    score6 += weight + 1;
                    score7 += weight + 1;
                }
                case 3 -> {
                    score3           -= weight * 3;
                    score1 += weight;
                    score5 += weight;
                    score6 += weight + 1;
                    score7 += weight + 1;
                    score0 += weight + 1;
                }
                case 4 -> {
                    score4           -= weight * 3;
                    score2 += weight;
                    score6 += weight;
                    score7 += weight + 1;
                    score0 += weight + 1;
                    score1 += weight + 1;
                }
                case 5 -> {
                    score5           -= weight * 3;
                    score3 += weight;
                    score7 += weight;
                    score0 += weight + 1;
                    score1 += weight + 1;
                    score2 += weight + 1;
                }
                case 6 -> {
                    score6           -= weight * 3;
                    score4 += weight;
                    score0 += weight;
                    score1 += weight + 1;
                    score2 += weight + 1;
                    score3 += weight + 1;
                }
                case 7 -> {
                    score7           -= weight * 3;
                    score5 += weight;
                    score1 += weight;
                    score2 += weight + 1;
                    score3 += weight + 1;
                    score4 += weight + 1;
                }
                default -> {
                    assert false : "unexpected direction";
                }
            }
        }
        for(int idx = 0; idx < RobotTracker.numCats; idx++) {
            var robot = RobotTracker.sortedCats[idx];
            if(robot == null) continue;
            var weight = 5;
            var dir = myLoc.directionTo(robot.location);
            switch(dir.ordinal()) {
                case 0 -> {
                    score0           -= weight * 3;
                    score6 += weight;
                    score2 += weight;
                    score3 += weight + 1;
                    score4 += weight + 1;
                    score5 += weight + 1;
                }
                case 1 -> {
                    score1           -= weight * 3;
                    score7 += weight;
                    score3 += weight;
                    score4 += weight + 1;
                    score5 += weight + 1;
                    score6 += weight + 1;
                }
                case 2 -> {
                    score2           -= weight * 3;
                    score0 += weight;
                    score4 += weight;
                    score5 += weight + 1;
                    score6 += weight + 1;
                    score7 += weight + 1;
                }
                case 3 -> {
                    score3           -= weight * 3;
                    score1 += weight;
                    score5 += weight;
                    score6 += weight + 1;
                    score7 += weight + 1;
                    score0 += weight + 1;
                }
                case 4 -> {
                    score4           -= weight * 3;
                    score2 += weight;
                    score6 += weight;
                    score7 += weight + 1;
                    score0 += weight + 1;
                    score1 += weight + 1;
                }
                case 5 -> {
                    score5           -= weight * 3;
                    score3 += weight;
                    score7 += weight;
                    score0 += weight + 1;
                    score1 += weight + 1;
                    score2 += weight + 1;
                }
                case 6 -> {
                    score6           -= weight * 3;
                    score4 += weight;
                    score0 += weight;
                    score1 += weight + 1;
                    score2 += weight + 1;
                    score3 += weight + 1;
                }
                case 7 -> {
                    score7           -= weight * 3;
                    score5 += weight;
                    score1 += weight;
                    score2 += weight + 1;
                    score3 += weight + 1;
                    score4 += weight + 1;
                }
                default -> {
                    assert false : "unexpected direction";
                }
            }
        }
        int best = -1;
        var move = Direction.CENTER;
        if(score0 > best) {
            best = score0;
            move = Direction.NORTH;
        }
        if(score1 > best) {
            best = score1;
            move = Direction.NORTHEAST;
        }
        if(score2 > best) {
            best = score2;
            move = Direction.EAST;
        }
        if(score3 > best) {
            best = score3;
            move = Direction.SOUTHEAST;
        }
        if(score4 > best) {
            best = score4;
            move = Direction.SOUTH;
        }
        if(score5 > best) {
            best = score5;
            move = Direction.SOUTHWEST;
        }
        if(score6 > best) {
            best = score6;
            move = Direction.WEST;
        }
        if(score7 > best) {
            best = score7;
            move = Direction.NORTHWEST;
        }
        // indicate("evading " + move);
        digDirtInDirection(move);
        if(move != Direction.CENTER && rc.canMove(move)) RatKing.rawMove(move);
        if (RobotTracker.numCats > 0) {
            placeDefensiveDirt();
        } else {
            digSurroundingDirt();
        }
        if(rc.isActionReady()) {
            RobotInfo bestEnemy = null;
            int bestHp = 1000000000;
            for(int idx = 0; idx < RobotTracker.numEnemies; idx++) {
                var robot = RobotTracker.sortedEnemies[idx];
                if(robot == null) continue;
                if(robot.location.distanceSquaredTo(myLoc) > 8) continue;
                if(robot.health < bestHp) {
                    bestEnemy = robot;
                    bestHp = robot.health;
                }
            }
            if(bestEnemy != null) {
                rc.attack(bestEnemy.location);
            }
        }
        return true;
    }
}