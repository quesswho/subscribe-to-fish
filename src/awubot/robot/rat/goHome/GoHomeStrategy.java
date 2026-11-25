

package awubot.robot.rat.goHome;

import battlecode.common.*;
import awubot.Pathfinder;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import awubot.Pathfinder;
import awubot.util.Profiler;
import awubot.robot.rat.BabyRat;
import awubot.robot.rat.TraverseStrategy;

import static awubot.Game.*;
import static awubot.util.Util.*;
import static awubot.robot.rat.BabyRat.*;

public class GoHomeStrategy extends Strategy {
    private int king;
    // The trail can end away from the king (it moved): then the lowest mark in sight bounces us between two cells.
    // A bounce, or a trail ending where we stand, sends us straight to the king for a while instead.
    private static final int DIRECT_TURNS = 40;
    private MapLocation lastLoc, prevLoc;
    private int directTurns = 0;
    private TraverseStrategy direct;

    private void goDirect(MapLocation kingLoc) throws GameActionException {
        if (direct == null || !direct.target.equals(kingLoc)) direct = new TraverseStrategy(kingLoc, 8);
        direct.runTick();
    }

    public GoHomeStrategy() {
        king = GlobalArray.nearestRatKingId;
    }

    @Override
    public boolean runTick() throws GameActionException {
        if(rc.getRawCheese() == 0) return false;

                if(rc.getRawCheese() != 0 && rc.canTurn()) {
            var nearestKingLoc = GlobalArray.nearestRatKing;
            int dx = Integer.compare(myLoc.x, nearestKingLoc.x);
            int dy = Integer.compare(myLoc.y, nearestKingLoc.y);
            var transferTarget = new MapLocation(nearestKingLoc.x + dx, nearestKingLoc.y + dy);
            if(transferTarget.distanceSquaredTo(myLoc) <= 9) {
                turn(myLoc.directionTo(transferTarget));
                return false;
            }
        }


        var kingLoc = GlobalArray.getRatKing(king);

        if (kingLoc == null) {
            // king died, try to find another king
            king = GlobalArray.nearestRatKingId;
            kingLoc = GlobalArray.nearestRatKing;
            if (kingLoc == null) return false;
        }

        if (!myLoc.equals(lastLoc)) {
            prevLoc = lastLoc;
            lastLoc = myLoc;
        }
        if (rc.isMovementReady() && directTurns > 0) {
            directTurns--;
            goDirect(kingLoc);
        } else if(rc.isMovementReady()) {
            Pathfinder.computeReachable();
            var g = AntColony.mark[king];
            char best = '\uFFFF';
            int bestX = -1, bestY = -1;
            var x = myLocX - 5;
            var y = myLocY - 5;
            {
                if(y >= 0 && y < mapHeight) {
                    var row = g[y];
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 0))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 1))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 2))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 3))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 4))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 5))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 6))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 7))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 8))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 9))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 10))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    y++;
                } else {
                    y++;
                    x += 10;
                }
            }
            {
                if(y >= 0 && y < mapHeight) {
                    var row = g[y];
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 21))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 20))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 19))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 18))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 17))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 16))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 15))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 14))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 13))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 12))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 11))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    y++;
                } else {
                    y++;
                    x -= 10;
                }
            }
            {
                if(y >= 0 && y < mapHeight) {
                    var row = g[y];
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 22))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 23))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 24))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 25))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 26))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 27))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 28))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 29))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 30))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 31))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 32))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    y++;
                } else {
                    y++;
                    x += 10;
                }
            }
            {
                if(y >= 0 && y < mapHeight) {
                    var row = g[y];
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 43))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 42))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 41))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 40))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 39))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 38))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 37))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 36))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 35))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 34))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 33))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    y++;
                } else {
                    y++;
                    x -= 10;
                }
            }
            {
                if(y >= 0 && y < mapHeight) {
                    var row = g[y];
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 44))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 45))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 46))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 47))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 48))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 49))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 50))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 51))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 52))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 53))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 54))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    y++;
                } else {
                    y++;
                    x += 10;
                }
            }
            {
                if(y >= 0 && y < mapHeight) {
                    var row = g[y];
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 4))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 3))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 2))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 1))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 0))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 59))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 58))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 57))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 56))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach0 & (1l << 55))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    y++;
                } else {
                    y++;
                    x -= 10;
                }
            }
            {
                if(y >= 0 && y < mapHeight) {
                    var row = g[y];
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 5))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 6))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 7))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 8))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 9))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 10))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 11))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 12))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 13))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 14))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 15))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    y++;
                } else {
                    y++;
                    x += 10;
                }
            }
            {
                if(y >= 0 && y < mapHeight) {
                    var row = g[y];
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 26))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 25))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 24))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 23))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 22))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 21))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 20))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 19))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 18))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 17))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 16))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    y++;
                } else {
                    y++;
                    x -= 10;
                }
            }
            {
                if(y >= 0 && y < mapHeight) {
                    var row = g[y];
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 27))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 28))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 29))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 30))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 31))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 32))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 33))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 34))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 35))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 36))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 37))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    y++;
                } else {
                    y++;
                    x += 10;
                }
            }
            {
                if(y >= 0 && y < mapHeight) {
                    var row = g[y];
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 48))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 47))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 46))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 45))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 44))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 43))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 42))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 41))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 40))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 39))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x--;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 38))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    y++;
                } else {
                    y++;
                    x -= 10;
                }
            }
            {
                if(y >= 0 && y < mapHeight) {
                    var row = g[y];
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 49))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 50))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 51))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 52))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 53))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 54))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 55))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 56))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 57))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 58))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                    x++;
                    {
                        if(x >= 0 && x < mapWidth &&
                           (Pathfinder.reach1 & (1l << 59))
                           != 0
                           ) {
                            var v = row.charAt(x);
                            if(v < best) {
                                best = v;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                } else {
                    y++;
                    x += 10;
                }
            }
            if(best == '\uFFFF') {
                goDirect(kingLoc);
            } else if ((bestX == myLocX && bestY == myLocY) || (prevLoc != null && bestX == prevLoc.x && bestY == prevLoc.y)) {
                directTurns = DIRECT_TURNS;
                goDirect(kingLoc);
            } else {
                var bestLoc = new MapLocation(bestX, bestY);
                BabyRat.autoMove(Pathfinder.doBfs(bestLoc));
                // rc.setIndicatorLine(myLoc, bestLoc, 0, 0, 255);
            }
                    if(rc.getRawCheese() != 0 && rc.canTurn()) {
            var nearestKingLoc = GlobalArray.nearestRatKing;
            int dx = Integer.compare(myLoc.x, nearestKingLoc.x);
            int dy = Integer.compare(myLoc.y, nearestKingLoc.y);
            var transferTarget = new MapLocation(nearestKingLoc.x + dx, nearestKingLoc.y + dy);
            if(transferTarget.distanceSquaredTo(myLoc) <= 9) {
                turn(myLoc.directionTo(transferTarget));
                return false;
            }
        }

        }

        // rc.setIndicatorLine(myLoc, kingLoc, 0, 255, 0);
        return true;
    }
}