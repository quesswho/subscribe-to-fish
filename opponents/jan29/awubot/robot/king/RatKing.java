package awubot.robot.king;

import awubot.CellTracker;
import static awubot.Game.*;
import awubot.robot.Strategy;
import awubot.RobotTracker;
import static awubot.util.Util.allDirections;
import static awubot.util.Util.directions;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;

public class RatKing {
    public static Strategy primaryStrategy;
    public static boolean is5x5;
    public static boolean spawnedThisTurn;

    public static void init() {
        primaryStrategy = new RatKingStrategy();
        is5x5 = false;
    }

    public static void run() throws GameActionException {
        // indicate("5x5: " + is5x5);
        spawnedThisTurn = false;
        // indicate(rc.getGlobalCheese() + " cheese, " + rc.getHealth() + " HP");
        CellTracker.storeVisibleCellsKing();

        RobotTracker.reset();

        tryUnburyIfStuck();

        // indicate("before primary strat: " + Clock.getBytecodeNum());

        primaryStrategy.runTick();
    }

    public static void rawMove(Direction dir) throws GameActionException {
        _rawMove(dir);
    }

    public static void turn(Direction dir) throws GameActionException {
        _turn(dir);
    }

    public static void digDirtInDirection(Direction dir) throws GameActionException {
        if(!rc.isActionReady()) return;
        // indicate("dig " + dir);
        int dx = dir.dx;
        int dy = dir.dy;

        if (dx != 0) {
            for (int y = -1; y <= 1; y++) {
                var loc = new MapLocation(myLocX + dx * 2, myLocY + dy + y);
                if(rc.canRemoveDirt(loc)) {
                    rc.removeDirt(loc);
                    return;
                }
            }
        }

        if (dy != 0) {
            for (int x = -1; x <= 1; x++) {
                var loc = new MapLocation(myLocX + dx + x, myLocY + dy * 2);
                if(rc.canRemoveDirt(loc)) {
                    rc.removeDirt(loc);
                    return;
                }
            }
        }
    }

    private static void tryUnburyIfStuck() throws GameActionException {
        if(!rc.isActionReady()) return;

        boolean completelyStuck = true;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (Math.max(Math.abs(dx), Math.abs(dy)) < 2) continue;
                MapLocation checkLoc = new MapLocation(myLoc.x + dx, myLoc.y + dy);
                if (!rc.onTheMap(checkLoc) || !rc.canSenseLocation(checkLoc)) continue;
                MapInfo info = rc.senseMapInfo(checkLoc);
                if (info.isPassable() && !info.isDirt()) {
                    // if (info != null) {
                    //     indicate("x: " + info.getMapLocation().x + ", y: " + info.getMapLocation().y);
                    // }
                    completelyStuck = false;
                    break;
                }
            }
        }

        if(!completelyStuck) return;

        int midVertDir = myLoc.y < mapHeight / 2 ? 1 : -1;
        int midHorzDir = myLoc.x < mapWidth / 2 ? 1 : -1;


        int vertDirtCount = 0;
        boolean vertValid = true;
        MapLocation vertDirtTarget = null;
        outer_vert:
        for (int dist = 2; dist <= 3; dist++) {
            int y = myLoc.y + dist * midVertDir;
            for (int dx = -1; dx <= 1; dx++) {
                MapLocation loc = new MapLocation(myLoc.x + dx, y);
                if (!rc.onTheMap(loc) || !rc.canSenseLocation(loc)) {
                    vertValid = false;
                    break outer_vert;
                }
                MapInfo info = rc.senseMapInfo(loc);
                if (info.isDirt()) {
                    vertDirtCount++;
                    if (vertDirtTarget == null && rc.canRemoveDirt(loc)) {
                        vertDirtTarget = loc;
                    }
                } else if (!info.isPassable()) {
                    vertValid = false;
                    break outer_vert;
                }
            }
        }

        int horzDirtCount = 0;
        boolean horzValid = true;
        MapLocation horzDirtTarget = null;
        outer_horz:
        for (int dist = 2; dist <= 3; dist++) {
            int x = myLoc.x + dist * midHorzDir;
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation loc = new MapLocation(x, myLoc.y + dy);
                if (!rc.onTheMap(loc) || !rc.canSenseLocation(loc)) {
                    horzValid = false;
                    break outer_horz;
                }
                MapInfo info = rc.senseMapInfo(loc);
                if (info.isDirt()) {
                    horzDirtCount++;
                    if (horzDirtTarget == null && rc.canRemoveDirt(loc)) {
                        horzDirtTarget = loc;
                    }
                } else if (!info.isPassable()) {
                    horzValid = false;
                    break outer_horz;
                }
            }
        }

        MapLocation targetDirt = null;
        if (vertValid && vertDirtTarget != null && (!horzValid || horzDirtTarget == null || vertDirtCount <= horzDirtCount)) {
            targetDirt = vertDirtTarget;
        } else if (horzValid && horzDirtTarget != null) {
            targetDirt = horzDirtTarget;
        }

        if (targetDirt != null) {
            rc.removeDirt(targetDirt);
            CellTracker.storeCellKing(rc.senseMapInfo(targetDirt));
            return;
        }

        MapLocation best = null;
        int bestScore = Integer.MIN_VALUE;

        for(int dx = -2; dx <= 2; dx++) {
            for(int dy = -2; dy <= 2; dy++) {
                if(Math.max(Math.abs(dx), Math.abs(dy)) < 2) continue;

                var loc = new MapLocation(myLoc.x + dx, myLoc.y + dy);
                if(!rc.onTheMap(loc) || !rc.canSenseLocation(loc)) continue;

                MapInfo info = rc.senseMapInfo(loc);
                if(!info.isDirt() || !rc.canRemoveDirt(loc)) continue;

                int score = 0;
                for(var dir : directions) {
                    var dest = myLoc.add(dir);
                    if(!rc.onTheMap(dest)) continue;
                    if(Math.abs(loc.x - dest.x) <= 2 && Math.abs(loc.y - dest.y) <= 2) {
                        score += 25;
                    }
                }

                if(CellTracker.isWallAdjacent(loc)) {
                    score += 200;
                }

                score -= 10 * myLoc.distanceSquaredTo(loc);

                int minDistToFree = Integer.MAX_VALUE;
                for(int ddx = -3; ddx <= 3; ddx++) {
                    for(int ddy = -3; ddy <= 3; ddy++) {
                        var checkLoc = new MapLocation(loc.x + ddx, loc.y + ddy);
                        if(!rc.onTheMap(checkLoc) || !rc.canSenseLocation(checkLoc))
                            continue;

                        int newx = ddx + dx;
                        int newy = ddy + dy;
                        if (newx * newx + newy * newy <= 2)
                            continue;

                        MapInfo checkInfo = rc.senseMapInfo(checkLoc);
                        if(checkInfo.isPassable()) {
                            int dist = ddx * ddx + ddy * ddy;
                            if(dist < minDistToFree) {
                                minDistToFree = dist;
                            }
                        }
                    }
                }

                score -= 10 * minDistToFree;

                if(score > bestScore) {
                    bestScore = score;
                    best = loc;
                }
            }
        }

        if(best != null) {
            rc.removeDirt(best);
            CellTracker.storeCellKing(rc.senseMapInfo(best));
        }
    }


}
