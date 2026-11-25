

package awubot.util;

import battlecode.common.*;
import static awubot.Game.*;

import java.util.*;

import awubot.CellTracker;
import awubot.Game;
import static java.lang.Math.*;

public class Util {
    public static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    public static final Direction[] allDirections = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
        Direction.CENTER
    };

    public static final Direction[] orthDirections = {
        Direction.NORTH,
        Direction.EAST,
        Direction.SOUTH,
        Direction.WEST
    };

    public static int dist(RobotInfo info) {
        switch (info.getType().ordinal()) {
            case 0:
                return myLoc.distanceSquaredTo(info.location);
            case 1:
                return myLoc.add(myLoc.directionTo(info.location)).distanceSquaredTo(info.location);
            case 2:
                int dist = myLoc.distanceSquaredTo(info.location);
                int dist1 = myLoc.distanceSquaredTo(info.location.add(Direction.NORTH));
                if (dist1 < dist) dist = dist1;
                dist1 = myLoc.distanceSquaredTo(info.location.add(Direction.EAST));
                if (dist1 < dist) dist = dist1;
                dist1 = myLoc.distanceSquaredTo(info.location.add(Direction.NORTHEAST));
                if (dist1 < dist) dist = dist1;
                return dist;
            default:
                assert false : "Unknown robot type " + info.getType();
                return -1;
        }
    }

    public static int dist(MapLocation loc, RobotInfo info) {
        switch (info.getType().ordinal()) {
            case 0:
                return loc.distanceSquaredTo(info.location);
            case 1:
                return loc.add(loc.directionTo(info.location)).distanceSquaredTo(info.location);
            case 2:
                int dist = loc.distanceSquaredTo(info.location);
                int dist1 = loc.distanceSquaredTo(info.location.add(Direction.NORTH));
                if (dist1 < dist) dist = dist1;
                dist1 = loc.distanceSquaredTo(info.location.add(Direction.EAST));
                if (dist1 < dist) dist = dist1;
                dist1 = loc.distanceSquaredTo(info.location.add(Direction.NORTHEAST));
                if (dist1 < dist) dist = dist1;
                return dist;
            default:
                assert false : "Unknown robot type " + info.getType();
                return -1;
        }
    }

    public static int chebyshevDistance(RobotInfo info) {
        int dx = info.location.x - myLocX;
        int dy = info.location.y - myLocY;
        switch (info.getType().ordinal()) {
            case 0:
                return max(abs(dx), abs(dy));
            case 1:
                return max(abs(dx), abs(dy)) -  1;
            case 2:
                if (dx < 0) dx = -dx - 1;
                if (dy < 0) dy = -dy - 1;
                return max(dx, dy);
            default:
                assert false : "Unknown robot type " + info.getType();
                return -1;
        }
    }

    public static int chebyshevDistance(MapLocation loc, RobotInfo info) {
        int dx = info.location.x - loc.x;
        int dy = info.location.y - loc.y;
        switch (info.getType().ordinal()) {
            case 0:
                return max(abs(dx), abs(dy));
            case 1:
                return max(abs(dx), abs(dy)) -  1;
            case 2:
                if (dx < 0) dx = -dx - 1;
                if (dy < 0) dy = -dy - 1;
                return max(dx, dy);
            default:
                assert false : "Unknown robot type " + info.getType();
                return -1;
        }
    }

    public static void preTick() {
        // indicatorString = ""; 
    }

    public static boolean canSense(int dx, int dy, Direction dir) {
        switch(dir.ordinal()) {
            case 0 -> {
                switch(dx) {
                    case -3 -> {
                        switch(dy) {
                            case 3 -> {
                                return true;
                            }
                        }
                    }
                    case -2 -> {
                        switch(dy) {
                            case 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case -1 -> {
                        switch(dy) {
                            case 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 0 -> {
                        switch(dy) {
                            case 0, 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 1 -> {
                        switch(dy) {
                            case 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 2 -> {
                        switch(dy) {
                            case 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 3 -> {
                        switch(dy) {
                            case 3 -> {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
            case 1 -> {
                switch(dx) {
                    case 0 -> {
                        switch(dy) {
                            case 0, 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 1 -> {
                        switch(dy) {
                            case 0, 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 2 -> {
                        switch(dy) {
                            case 0, 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 3 -> {
                        switch(dy) {
                            case 0, 1, 2, 3 -> {
                                return true;
                            }
                        }
                    }
                    case 4 -> {
                        switch(dy) {
                            case 0, 1, 2 -> {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
            case 2 -> {
                switch(dx) {
                    case 0 -> {
                        switch(dy) {
                            case 0 -> {
                                return true;
                            }
                        }
                    }
                    case 1 -> {
                        switch(dy) {
                            case -1, 0, 1 -> {
                                return true;
                            }
                        }
                    }
                    case 2 -> {
                        switch(dy) {
                            case -2, -1, 0, 1, 2 -> {
                                return true;
                            }
                        }
                    }
                    case 3 -> {
                        switch(dy) {
                            case -3, -2, -1, 0, 1, 2, 3 -> {
                                return true;
                            }
                        }
                    }
                    case 4 -> {
                        switch(dy) {
                            case -2, -1, 0, 1, 2 -> {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
            case 3 -> {
                switch(dx) {
                    case 0 -> {
                        switch(dy) {
                            case -4, -3, -2, -1, 0 -> {
                                return true;
                            }
                        }
                    }
                    case 1 -> {
                        switch(dy) {
                            case -4, -3, -2, -1, 0 -> {
                                return true;
                            }
                        }
                    }
                    case 2 -> {
                        switch(dy) {
                            case -4, -3, -2, -1, 0 -> {
                                return true;
                            }
                        }
                    }
                    case 3 -> {
                        switch(dy) {
                            case -3, -2, -1, 0 -> {
                                return true;
                            }
                        }
                    }
                    case 4 -> {
                        switch(dy) {
                            case -2, -1, 0 -> {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
            case 4 -> {
                switch(dx) {
                    case -3 -> {
                        switch(dy) {
                            case -3 -> {
                                return true;
                            }
                        }
                    }
                    case -2 -> {
                        switch(dy) {
                            case -4, -3, -2 -> {
                                return true;
                            }
                        }
                    }
                    case -1 -> {
                        switch(dy) {
                            case -4, -3, -2, -1 -> {
                                return true;
                            }
                        }
                    }
                    case 0 -> {
                        switch(dy) {
                            case -4, -3, -2, -1, 0 -> {
                                return true;
                            }
                        }
                    }
                    case 1 -> {
                        switch(dy) {
                            case -4, -3, -2, -1 -> {
                                return true;
                            }
                        }
                    }
                    case 2 -> {
                        switch(dy) {
                            case -4, -3, -2 -> {
                                return true;
                            }
                        }
                    }
                    case 3 -> {
                        switch(dy) {
                            case -3 -> {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
            case 5 -> {
                switch(dx) {
                    case -4 -> {
                        switch(dy) {
                            case -2, -1, 0 -> {
                                return true;
                            }
                        }
                    }
                    case -3 -> {
                        switch(dy) {
                            case -3, -2, -1, 0 -> {
                                return true;
                            }
                        }
                    }
                    case -2 -> {
                        switch(dy) {
                            case -4, -3, -2, -1, 0 -> {
                                return true;
                            }
                        }
                    }
                    case -1 -> {
                        switch(dy) {
                            case -4, -3, -2, -1, 0 -> {
                                return true;
                            }
                        }
                    }
                    case 0 -> {
                        switch(dy) {
                            case -4, -3, -2, -1, 0 -> {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
            case 6 -> {
                switch(dx) {
                    case -4 -> {
                        switch(dy) {
                            case -2, -1, 0, 1, 2 -> {
                                return true;
                            }
                        }
                    }
                    case -3 -> {
                        switch(dy) {
                            case -3, -2, -1, 0, 1, 2, 3 -> {
                                return true;
                            }
                        }
                    }
                    case -2 -> {
                        switch(dy) {
                            case -2, -1, 0, 1, 2 -> {
                                return true;
                            }
                        }
                    }
                    case -1 -> {
                        switch(dy) {
                            case -1, 0, 1 -> {
                                return true;
                            }
                        }
                    }
                    case 0 -> {
                        switch(dy) {
                            case 0 -> {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
            case 7 -> {
                switch(dx) {
                    case -4 -> {
                        switch(dy) {
                            case 0, 1, 2 -> {
                                return true;
                            }
                        }
                    }
                    case -3 -> {
                        switch(dy) {
                            case 0, 1, 2, 3 -> {
                                return true;
                            }
                        }
                    }
                    case -2 -> {
                        switch(dy) {
                            case 0, 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case -1 -> {
                        switch(dy) {
                            case 0, 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 0 -> {
                        switch(dy) {
                            case 0, 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
            default -> {
                return false;
            }
        }
    }

    // a cat on cell (x, y) is actually "centered" on (x + 0.5, y + 0.5) since they have size 2x2
    // they can see up to sqrt(17) away from their center
    // they can see an inclusive 180 degree around their facing direction
    public static boolean canCatSense(int dx, int dy, Direction dir) {
        switch(dir.ordinal()) {
            case 0 -> {
                switch(dx) {
                    case -3 -> {
                        switch(dy) {
                            case 1, 2 -> {
                                return true;
                            }
                        }
                    }
                    case -2 -> {
                        switch(dy) {
                            case 1, 2, 3 -> {
                                return true;
                            }
                        }
                    }
                    case -1 -> {
                        switch(dy) {
                            case 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 0 -> {
                        switch(dy) {
                            case 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 1 -> {
                        switch(dy) {
                            case 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 2 -> {
                        switch(dy) {
                            case 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 3 -> {
                        switch(dy) {
                            case 1, 2, 3 -> {
                                return true;
                            }
                        }
                    }
                    case 4 -> {
                        switch(dy) {
                            case 1, 2 -> {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
            case 1 -> {
                switch(dx) {
                    case -2 -> {
                        switch(dy) {
                            case 3 -> {
                                return true;
                            }
                        }
                    }
                    case -1 -> {
                        switch(dy) {
                            case 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 0 -> {
                        switch(dy) {
                            case 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 1 -> {
                        switch(dy) {
                            case 0, 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 2 -> {
                        switch(dy) {
                            case -1, 0, 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 3 -> {
                        switch(dy) {
                            case -2, -1, 0, 1, 2, 3 -> {
                                return true;
                            }
                        }
                    }
                    case 4 -> {
                        switch(dy) {
                            case -1, 0, 1, 2 -> {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
            case 2 -> {
                switch(dx) {
                    case 1 -> {
                        switch(dy) {
                            case -3, -2, -1, 0, 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 2 -> {
                        switch(dy) {
                            case -3, -2, -1, 0, 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 3 -> {
                        switch(dy) {
                            case -2, -1, 0, 1, 2, 3 -> {
                                return true;
                            }
                        }
                    }
                    case 4 -> {
                        switch(dy) {
                            case -1, 0, 1, 2 -> {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
            case 3 -> {
                switch(dx) {
                    case -2 -> {
                        switch(dy) {
                            case -2 -> {
                                return true;
                            }
                        }
                    }
                    case -1 -> {
                        switch(dy) {
                            case -3, -2, -1 -> {
                                return true;
                            }
                        }
                    }
                    case 0 -> {
                        switch(dy) {
                            case -3, -2, -1, 0 -> {
                                return true;
                            }
                        }
                    }
                    case 1 -> {
                        switch(dy) {
                            case -3, -2, -1, 0, 1 -> {
                                return true;
                            }
                        }
                    }
                    case 2 -> {
                        switch(dy) {
                            case -3, -2, -1, 0, 1, 2 -> {
                                return true;
                            }
                        }
                    }
                    case 3 -> {
                        switch(dy) {
                            case -2, -1, 0, 1, 2, 3 -> {
                                return true;
                            }
                        }
                    }
                    case 4 -> {
                        switch(dy) {
                            case -1, 0, 1, 2 -> {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
            case 4 -> {
                switch(dx) {
                    case -3 -> {
                        switch(dy) {
                            case -1, 0 -> {
                                return true;
                            }
                        }
                    }
                    case -2 -> {
                        switch(dy) {
                            case -2, -1, 0 -> {
                                return true;
                            }
                        }
                    }
                    case -1 -> {
                        switch(dy) {
                            case -3, -2, -1, 0 -> {
                                return true;
                            }
                        }
                    }
                    case 0 -> {
                        switch(dy) {
                            case -3, -2, -1, 0 -> {
                                return true;
                            }
                        }
                    }
                    case 1 -> {
                        switch(dy) {
                            case -3, -2, -1, 0 -> {
                                return true;
                            }
                        }
                    }
                    case 2 -> {
                        switch(dy) {
                            case -3, -2, -1, 0 -> {
                                return true;
                            }
                        }
                    }
                    case 3 -> {
                        switch(dy) {
                            case -2, -1, 0 -> {
                                return true;
                            }
                        }
                    }
                    case 4 -> {
                        switch(dy) {
                            case -1, 0 -> {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
            case 5 -> {
                switch(dx) {
                    case -3 -> {
                        switch(dy) {
                            case -1, 0, 1, 2 -> {
                                return true;
                            }
                        }
                    }
                    case -2 -> {
                        switch(dy) {
                            case -2, -1, 0, 1, 2, 3 -> {
                                return true;
                            }
                        }
                    }
                    case -1 -> {
                        switch(dy) {
                            case -3, -2, -1, 0, 1, 2 -> {
                                return true;
                            }
                        }
                    }
                    case 0 -> {
                        switch(dy) {
                            case -3, -2, -1, 0, 1 -> {
                                return true;
                            }
                        }
                    }
                    case 1 -> {
                        switch(dy) {
                            case -3, -2, -1, 0 -> {
                                return true;
                            }
                        }
                    }
                    case 2 -> {
                        switch(dy) {
                            case -3, -2, -1 -> {
                                return true;
                            }
                        }
                    }
                    case 3 -> {
                        switch(dy) {
                            case -2 -> {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
            case 6 -> {
                switch(dx) {
                    case -3 -> {
                        switch(dy) {
                            case -1, 0, 1, 2 -> {
                                return true;
                            }
                        }
                    }
                    case -2 -> {
                        switch(dy) {
                            case -2, -1, 0, 1, 2, 3 -> {
                                return true;
                            }
                        }
                    }
                    case -1 -> {
                        switch(dy) {
                            case -3, -2, -1, 0, 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 0 -> {
                        switch(dy) {
                            case -3, -2, -1, 0, 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
            case 7 -> {
                switch(dx) {
                    case -3 -> {
                        switch(dy) {
                            case -1, 0, 1, 2 -> {
                                return true;
                            }
                        }
                    }
                    case -2 -> {
                        switch(dy) {
                            case -2, -1, 0, 1, 2, 3 -> {
                                return true;
                            }
                        }
                    }
                    case -1 -> {
                        switch(dy) {
                            case -1, 0, 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 0 -> {
                        switch(dy) {
                            case 0, 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 1 -> {
                        switch(dy) {
                            case 1, 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 2 -> {
                        switch(dy) {
                            case 2, 3, 4 -> {
                                return true;
                            }
                        }
                    }
                    case 3 -> {
                        switch(dy) {
                            case 3 -> {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
            default -> {
                return false;
            }
        }
    }

    public static boolean canHardSense(int dx, int dy, Direction dir) {
        switch(dir.ordinal()) {
            case 0 -> {
                switch(dx) {
                    case -4 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -3 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -2 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return true;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -1 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return true;
                            }
                            case 4 -> {
                                return true;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 0 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return true;
                            }
                            case 3 -> {
                                return true;
                            }
                            case 4 -> {
                                return true;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 1 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return true;
                            }
                            case 4 -> {
                                return true;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 2 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return true;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 3 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 4 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    default -> {
                        return false;
                    }
                }
            }
            case 1 -> {
                switch(dx) {
                    case -4 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -3 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -2 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -1 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 0 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 1 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return true;
                            }
                            case 2 -> {
                                return true;
                            }
                            case 3 -> {
                                return true;
                            }
                            case 4 -> {
                                return true;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 2 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return true;
                            }
                            case 2 -> {
                                return true;
                            }
                            case 3 -> {
                                return true;
                            }
                            case 4 -> {
                                return true;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 3 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return true;
                            }
                            case 2 -> {
                                return true;
                            }
                            case 3 -> {
                                return true;
                            }
                            case 4 -> {
                                return true;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 4 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return true;
                            }
                            case 2 -> {
                                return true;
                            }
                            case 3 -> {
                                return true;
                            }
                            case 4 -> {
                                return true;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    default -> {
                        return false;
                    }
                }
            }
            case 2 -> {
                switch(dx) {
                    case -4 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -3 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -2 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -1 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 0 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 1 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 2 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return true;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 3 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return true;
                            }
                            case 0 -> {
                                return true;
                            }
                            case 1 -> {
                                return true;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 4 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return true;
                            }
                            case -1 -> {
                                return true;
                            }
                            case 0 -> {
                                return true;
                            }
                            case 1 -> {
                                return true;
                            }
                            case 2 -> {
                                return true;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    default -> {
                        return false;
                    }
                }
            }
            case 3 -> {
                switch(dx) {
                    case -4 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -3 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -2 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -1 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 0 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 1 -> {
                        switch(dy) {
                            case -4 -> {
                                return true;
                            }
                            case -3 -> {
                                return true;
                            }
                            case -2 -> {
                                return true;
                            }
                            case -1 -> {
                                return true;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 2 -> {
                        switch(dy) {
                            case -4 -> {
                                return true;
                            }
                            case -3 -> {
                                return true;
                            }
                            case -2 -> {
                                return true;
                            }
                            case -1 -> {
                                return true;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 3 -> {
                        switch(dy) {
                            case -4 -> {
                                return true;
                            }
                            case -3 -> {
                                return true;
                            }
                            case -2 -> {
                                return true;
                            }
                            case -1 -> {
                                return true;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 4 -> {
                        switch(dy) {
                            case -4 -> {
                                return true;
                            }
                            case -3 -> {
                                return true;
                            }
                            case -2 -> {
                                return true;
                            }
                            case -1 -> {
                                return true;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    default -> {
                        return false;
                    }
                }
            }
            case 4 -> {
                switch(dx) {
                    case -4 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -3 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -2 -> {
                        switch(dy) {
                            case -4 -> {
                                return true;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -1 -> {
                        switch(dy) {
                            case -4 -> {
                                return true;
                            }
                            case -3 -> {
                                return true;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 0 -> {
                        switch(dy) {
                            case -4 -> {
                                return true;
                            }
                            case -3 -> {
                                return true;
                            }
                            case -2 -> {
                                return true;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 1 -> {
                        switch(dy) {
                            case -4 -> {
                                return true;
                            }
                            case -3 -> {
                                return true;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 2 -> {
                        switch(dy) {
                            case -4 -> {
                                return true;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 3 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 4 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    default -> {
                        return false;
                    }
                }
            }
            case 5 -> {
                switch(dx) {
                    case -4 -> {
                        switch(dy) {
                            case -4 -> {
                                return true;
                            }
                            case -3 -> {
                                return true;
                            }
                            case -2 -> {
                                return true;
                            }
                            case -1 -> {
                                return true;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -3 -> {
                        switch(dy) {
                            case -4 -> {
                                return true;
                            }
                            case -3 -> {
                                return true;
                            }
                            case -2 -> {
                                return true;
                            }
                            case -1 -> {
                                return true;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -2 -> {
                        switch(dy) {
                            case -4 -> {
                                return true;
                            }
                            case -3 -> {
                                return true;
                            }
                            case -2 -> {
                                return true;
                            }
                            case -1 -> {
                                return true;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -1 -> {
                        switch(dy) {
                            case -4 -> {
                                return true;
                            }
                            case -3 -> {
                                return true;
                            }
                            case -2 -> {
                                return true;
                            }
                            case -1 -> {
                                return true;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 0 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 1 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 2 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 3 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 4 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    default -> {
                        return false;
                    }
                }
            }
            case 6 -> {
                switch(dx) {
                    case -4 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return true;
                            }
                            case -1 -> {
                                return true;
                            }
                            case 0 -> {
                                return true;
                            }
                            case 1 -> {
                                return true;
                            }
                            case 2 -> {
                                return true;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -3 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return true;
                            }
                            case 0 -> {
                                return true;
                            }
                            case 1 -> {
                                return true;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -2 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return true;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -1 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 0 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 1 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 2 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 3 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 4 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    default -> {
                        return false;
                    }
                }
            }
            case 7 -> {
                switch(dx) {
                    case -4 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return true;
                            }
                            case 2 -> {
                                return true;
                            }
                            case 3 -> {
                                return true;
                            }
                            case 4 -> {
                                return true;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -3 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return true;
                            }
                            case 2 -> {
                                return true;
                            }
                            case 3 -> {
                                return true;
                            }
                            case 4 -> {
                                return true;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -2 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return true;
                            }
                            case 2 -> {
                                return true;
                            }
                            case 3 -> {
                                return true;
                            }
                            case 4 -> {
                                return true;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case -1 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return true;
                            }
                            case 2 -> {
                                return true;
                            }
                            case 3 -> {
                                return true;
                            }
                            case 4 -> {
                                return true;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 0 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 1 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 2 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 3 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    case 4 -> {
                        switch(dy) {
                            case -4 -> {
                                return false;
                            }
                            case -3 -> {
                                return false;
                            }
                            case -2 -> {
                                return false;
                            }
                            case -1 -> {
                                return false;
                            }
                            case 0 -> {
                                return false;
                            }
                            case 1 -> {
                                return false;
                            }
                            case 2 -> {
                                return false;
                            }
                            case 3 -> {
                                return false;
                            }
                            case 4 -> {
                                return false;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                    default -> {
                        return false;
                    }
                }
            }
            default -> {
                return false;
            }
        }
    }

    public static int getSenseLevel(int dx, int dy, Direction dir) {
        if (!canSense(dx, dy, dir)) return 0;
        if (canHardSense(dx, dy, dir)) return 2;
        return 1;
    }

    public static boolean canSense(MapLocation s, MapLocation t, Direction dir) {
        int dx = t.x - s.x;
        int dy = t.y - s.y;
        if (dx * dx + dy * dy > 20) return false;
        int key = dx + 20 * dy;
        switch(dir.ordinal()) {
            case 0 -> {
                switch (key) {
                    case 0, 19, 20, 21, 38, 39, 40, 41, 42, 57, 58, 59, 60, 61, 62, 63, 78, 79, 80, 81, 82 -> {
                        return true;
                    }
                    default -> {
                        return false;
                    }
                }
            }
            case 4 -> {
                switch (key) {
                    case 0, -82, -81, -80, -79, -78, -63, -62, -61, -60, -59, -58, -57, -42, -41, -40, -39, -38, -21, -20, -19 -> {
                        return true;
                    }
                    default -> {
                        return false;
                    }
                }
            }
            case 2 -> {
                switch (key) {
                    case 0, -57, -38, -37, -36, -19, -18, -17, -16, 1, 2, 3, 4, 21, 22, 23, 24, 42, 43, 44, 63 -> {
                        return true;
                    }
                    default -> {
                        return false;
                    }
                }
            }
            case 6 -> {
                switch (key) {
                    case 0, -63, -44, -43, -42, -24, -23, -22, -21, -4, -3, -2, -1, 16, 17, 18, 19, 36, 37, 38, 57 -> {
                        return true;
                    }
                    default -> {
                        return false;
                    }
                }
            }
            case 1 -> {
                switch (key) {
                    case 0, 1, 2, 3, 4, 20, 21, 22, 23, 24, 40, 41, 42, 43, 44, 60, 61, 62, 63, 80, 81, 82 -> {
                        return true;
                    }
                    default -> {
                        return false;
                    }
                }
            }
            case 7 -> {
                switch (key) {
                    case 0, -4, -3, -2, -1, 16, 17, 18, 19, 20, 36, 37, 38, 39, 40, 57, 58, 59, 60, 78, 79, 80 -> {
                        return true;
                    }
                    default -> {
                        return false;
                    }
                }
            }
            case 3 -> {
                switch (key) {
                    case 0, -80, -79, -78, -60, -59, -58, -57, -40, -39, -38, -37, -36, -20, -19, -18, -17, -16, 1, 2, 3, 4 -> {
                        return true;
                    }
                    default -> {
                        return false;
                    }
                }
            }
            case 5 -> {
                switch (key) {
                    case 0, -82, -81, -80, -63, -62, -61, -60, -44, -43, -42, -41, -40, -24, -23, -22, -21, -20, -4, -3, -2, -1 -> {
                        return true;
                    }
                    default -> {
                        return false;
                    }
                }
            }
            default -> {
                return false;
            }
        }
    }

    public static int manhattanDistance(MapLocation a, MapLocation b) {
        return abs(a.x - b.x) + abs(a.y - b.y);
    }

    public static Direction cw(Direction dir) {
        return switch(dir.ordinal()) {
            case 0 -> Direction.NORTHEAST;
            case 1 -> Direction.EAST;
            case 2 -> Direction.SOUTHEAST;
            case 3 -> Direction.SOUTH;
            case 4 -> Direction.SOUTHWEST;
            case 5 -> Direction.WEST;
            case 6 -> Direction.NORTHWEST;
            case 7 -> Direction.NORTH;
            default -> Direction.CENTER;
        };
    }

    public static Direction ccw(Direction dir) {
        return switch(dir.ordinal()) {
            case 0 -> Direction.NORTHWEST;
            case 7 -> Direction.WEST;
            case 6 -> Direction.SOUTHWEST;
            case 5 -> Direction.SOUTH;
            case 4 -> Direction.SOUTHEAST;
            case 3 -> Direction.EAST;
            case 2 -> Direction.NORTHEAST;
            case 1 -> Direction.NORTH;
            default -> Direction.CENTER;
        };
    }

    public static boolean isCellMaybePassable(MapLocation loc) throws GameActionException {
        if (rc.canSenseLocation(loc)) {
            return rc.senseMapInfo(loc).isPassable() && rc.senseRobotAtLocation(loc) == null;
        } else {
            if (CellTracker.isSeen(loc)) {
                return CellTracker.isPassable(loc);
            } else {
                return false;
            }
        }
    }

    public static boolean isCellAThreat(MapLocation loc) throws GameActionException {
        if (loc.equals(myLoc)) return true;
        if (CellTracker.isSeen(loc) && !CellTracker.isPassable(loc)) return false;
        if (rc.canSenseLocation(loc)) {
            RobotInfo info = rc.senseRobotAtLocation(loc);
            return info == null || info.getTeam() != team;
        }
        return false;
    }

    public static int angleDistance(Direction a, Direction b) {
        int diff = abs(a.ordinal() - b.ordinal());
        // println("dist " + a + " " + b + " " + min(diff, 8 - diff));
        return min(diff, 8 - diff);
    }

    public static Direction dirFromDelta(int dx, int dy) {
        switch (dy) {
            case -1:
                switch (dx) {
                    case -1: return Direction.NORTHWEST;
                    case 0: return Direction.NORTH;
                    case 1: return Direction.NORTHEAST;
                }
                break;
            case 0:
                switch (dx) {
                    case -1: return Direction.WEST;
                    case 0: return Direction.CENTER;
                    case 1: return Direction.EAST;
                }
                break;
            case 1:
                switch (dx) {
                    case -1: return Direction.SOUTHWEST;
                    case 0: return Direction.SOUTH;
                    case 1: return Direction.SOUTHEAST;
                }
                break;
        }
        return null;
    }

    public static Direction[] relatedDirections(Direction dir) {
        return switch(dir.ordinal()) {
            case 0 -> new Direction[]{Direction.NORTH, Direction.NORTHEAST, Direction.NORTHWEST};
            case 4 -> new Direction[]{Direction.SOUTH, Direction.SOUTHEAST, Direction.SOUTHWEST};
            case 2 -> new Direction[]{Direction.EAST, Direction.NORTHEAST, Direction.SOUTHEAST};
            case 6 -> new Direction[]{Direction.WEST, Direction.NORTHWEST, Direction.SOUTHWEST};
            case 1 -> new Direction[]{Direction.NORTHEAST, Direction.NORTH, Direction.EAST};
            case 7 -> new Direction[]{Direction.NORTHWEST, Direction.NORTH, Direction.WEST};
            case 3 -> new Direction[]{Direction.SOUTHEAST, Direction.SOUTH, Direction.EAST};
            case 5 -> new Direction[]{Direction.SOUTHWEST, Direction.SOUTH, Direction.WEST};
            default -> throw new IllegalStateException("Unexpected dir: " + dir);
        };
    }

    public static Direction[] unrelated5Directions(Direction dir) {
        return switch(dir.ordinal()) {
            case 0 -> new Direction[]{Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST};
            case 4 -> new Direction[]{Direction.EAST, Direction.NORTHEAST, Direction.NORTH, Direction.NORTHWEST, Direction.WEST};
            case 2 -> new Direction[]{Direction.NORTH, Direction.NORTHWEST, Direction.WEST,  Direction.SOUTHWEST, Direction.SOUTH};
            case 6 -> new Direction[]{Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH};
            case 1 -> new Direction[]{Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.SOUTHWEST, Direction.SOUTH};
            case 7 -> new Direction[]{Direction.SOUTH, Direction.SOUTHEAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH};
            case 3 -> new Direction[]{Direction.NORTH, Direction.NORTHWEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTH};
            case 5 -> new Direction[]{Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.NORTHEAST, Direction.NORTH};
            default -> throw new IllegalStateException("Unexpected dir: " + dir);
        };
    }

    public static Direction[] relatedDirectionsIncludingCenter(Direction dir) {
        return switch(dir.ordinal()) {
            case 0 -> new Direction[]{Direction.NORTH, Direction.NORTHEAST, Direction.NORTHWEST, Direction.CENTER};
            case 4 -> new Direction[]{Direction.SOUTH, Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.CENTER};
            case 2 -> new Direction[]{Direction.EAST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.CENTER};
            case 6 -> new Direction[]{Direction.WEST, Direction.NORTHWEST, Direction.SOUTHWEST, Direction.CENTER};
            case 1 -> new Direction[]{Direction.NORTHEAST, Direction.NORTH, Direction.EAST, Direction.CENTER};
            case 7 -> new Direction[]{Direction.NORTHWEST, Direction.NORTH, Direction.WEST, Direction.CENTER};
            case 3 -> new Direction[]{Direction.SOUTHEAST, Direction.SOUTH, Direction.EAST, Direction.CENTER};
            case 5 -> new Direction[]{Direction.SOUTHWEST, Direction.SOUTH, Direction.WEST, Direction.CENTER};
            default -> throw new IllegalStateException("Unexpected dir: " + dir);
        };
    }

    public static Direction[] wideRelatedDirections(Direction dir) {
        return switch(dir.ordinal()) {
            case 0 -> new Direction[]{Direction.NORTH, Direction.NORTHEAST, Direction.NORTHWEST, Direction.EAST, Direction.WEST};
            case 4 -> new Direction[]{Direction.SOUTH, Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.EAST, Direction.WEST};
            case 2 -> new Direction[]{Direction.EAST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.NORTH, Direction.SOUTH};
            case 6 -> new Direction[]{Direction.WEST, Direction.NORTHWEST, Direction.SOUTHWEST, Direction.NORTH, Direction.SOUTH};
            case 1 -> new Direction[]{Direction.NORTHEAST, Direction.NORTH, Direction.EAST, Direction.NORTHWEST, Direction.SOUTHEAST};
            case 7 -> new Direction[]{Direction.NORTHWEST, Direction.NORTH, Direction.WEST, Direction.NORTHEAST, Direction.SOUTHWEST};
            case 3 -> new Direction[]{Direction.SOUTHEAST, Direction.SOUTH, Direction.EAST, Direction.NORTHEAST, Direction.SOUTHWEST};
            case 5 -> new Direction[]{Direction.SOUTHWEST, Direction.SOUTH, Direction.WEST, Direction.NORTHWEST, Direction.SOUTHEAST};
            default -> throw new IllegalStateException("Unexpected dir: " + dir);
        };
    }

    public static Direction[] wideRelatedDirectionsIncludingCenter(Direction dir) {
        return switch(dir.ordinal()) {
            case 0 -> new Direction[]{Direction.NORTH, Direction.NORTHEAST, Direction.NORTHWEST, Direction.EAST, Direction.WEST, Direction.CENTER};
            case 4 -> new Direction[]{Direction.SOUTH, Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.EAST, Direction.WEST, Direction.CENTER};
            case 2 -> new Direction[]{Direction.EAST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.NORTH, Direction.SOUTH, Direction.CENTER};
            case 6 -> new Direction[]{Direction.WEST, Direction.NORTHWEST, Direction.SOUTHWEST, Direction.NORTH, Direction.SOUTH, Direction.CENTER};
            case 1 -> new Direction[]{Direction.NORTHEAST, Direction.NORTH, Direction.EAST, Direction.NORTHWEST, Direction.SOUTHEAST, Direction.CENTER};
            case 7 -> new Direction[]{Direction.NORTHWEST, Direction.NORTH, Direction.WEST, Direction.NORTHEAST, Direction.SOUTHWEST,  Direction.CENTER};
            case 3 -> new Direction[]{Direction.SOUTHEAST, Direction.SOUTH, Direction.EAST, Direction.NORTHEAST, Direction.SOUTHWEST,  Direction.CENTER};
            case 5 -> new Direction[]{Direction.SOUTHWEST, Direction.SOUTH, Direction.WEST, Direction.NORTHWEST, Direction.SOUTHEAST,  Direction.CENTER};
            default -> throw new IllegalStateException("Unexpected dir: " + dir);
        };
    }

    public static MapLocation decode2x2(int pos) {
        return new MapLocation((pos & 0b11111) << 1, (pos >>> 5) << 1);
    }

    public static int encode2x2(MapLocation loc) {
        return (loc.x >>> 1) + ((loc.y >>> 1) << 5);
    }

    public static int encode2x2(int myLocX, int myLocY) {
        return (myLocX >>> 1) + ((myLocY >>> 1) << 5);
    }

    public static int encode2x2() {
        return (myLocX >>> 1) + ((myLocY >>> 1) << 5);
    }

    public static int writeBits(int bits, int start, int[] data, int[] lens) {
        int off = start;
        assert data.length == lens.length;
        for (int i = 0; i < data.length; i++) {
            bits |= (data[i] << off);
            off += lens[i];
        }
        return bits;
    }

    public static int[] getBits(int bits, int[] segs) {
        bits >>>= segs[0];
        int[] ret = new int[segs.length - 1];
        for (int i = 0; i < segs.length - 1; i++) {
            ret[i] = (bits & ((1 << segs[i + 1]) - 1));
            bits >>= segs[i + 1];
        }
        return ret;
    }

    public static int getBits(int bits, int l, int len) {
        int msk = (1 << len) - 1;
        msk <<= l;
        return (bits & msk) >>> l;
    }

    public static int writeBits(int bits, int data, int st) {
        return bits | (data << st);
    }

    public static MapLocation flipHor(MapLocation loc) {
        return new MapLocation(mapWidth - 1 - loc.x, loc.y);
    }

    public static MapLocation flipVer(MapLocation loc) {
        return new MapLocation(loc.x, mapHeight - 1 - loc.y);
    }

    public static MapLocation rot180(MapLocation loc) {
        return new MapLocation(mapWidth - 1 - loc.x, mapHeight - 1 - loc.y);
    }

    public static MapLocation getSymmetricLocation(MapLocation loc) {
        if (!CellTracker.isSymmetryKnown()) return null;
        return switch (CellTracker.getSymmetry()) {
            case Rotational -> rot180(loc);
            case Horizontal -> flipHor(loc);
            case Vertical -> flipVer(loc);
            default -> null;
        };
    }

    public static int getCanonicalEncoded(int encoded) {
        if (!CellTracker.isSymmetryKnown()) return encoded;
        MapLocation loc = decode2x2(encoded);
        MapLocation symLoc = getSymmetricLocation(loc);
        if (symLoc == null) return encoded;
        int symEncoded = encode2x2(symLoc);
        return Math.min(encoded, symEncoded);
    }

    public static int getSymmetricEncoded(int encoded) {
        if (!CellTracker.isSymmetryKnown()) return -1;
        MapLocation loc = decode2x2(encoded);
        MapLocation symLoc = getSymmetricLocation(loc);
        if (symLoc == null) return -1;
        return encode2x2(symLoc);
    }

    public static boolean am(int id, int t) {
        return rc.getID() == id && time == t;
    }

    public static boolean amAfter(int id, int t) {
        return rc.getID() == id && time >= t;
    }

    public static int manhattan(MapLocation a, MapLocation b) {
        return abs(a.x - b.x) + abs(a.y - b.y);
    }

    final static int SIZE = 60;
    public static MapLocation decodeLoc(int code) {
        // assert code < mapWidth * SIZE : code + " is not a valid map code " + rc.getID();
        return new MapLocation(code / SIZE, code % SIZE);
    }

    public static int encodeLoc(MapLocation loc) {
        // assert 0 <= loc.x && loc.x < SIZE && 0 <= loc.y && loc.y < SIZE;
        return loc.x * SIZE + loc.y;
    }

    // public static Pair<Double, Double> relativeDistsToCentre(MapLocation loc) {
    //     double relX = (double) loc.x / (double) mapWidth;
    //     double relY = (double) loc.y / (double) mapHeight;
    //     return new Pair(abs(relX - 0.5), abs(relY - 0.5));
    // }

    public static List<MapLocation> guessEnemyLocs(MapLocation src, boolean closestFirst) throws GameActionException {
        List<MapLocation> enemyLocs = new LinkedList<>();

        if (src == null) {
            // give 3 CustomRandom locations
            for (int i = 0; i < 3; i++) {
                enemyLocs.addLast(genExplorationTarget());
            }
            return enemyLocs;
        }


        // also make the path as easy as possible between targets
        int dist_hormiddle = Math.abs(src.x - mapWidth / 2);
        int dist_vertmiddle = Math.abs(src.y - mapHeight / 2);
        if(closestFirst) {
            dist_hormiddle *= -1;
            dist_vertmiddle *= -1;
        }
        if (dist_hormiddle > dist_vertmiddle) {
            // first is hor
            enemyLocs.addLast(flipHor(src));

            int d1 = flipHor(src).distanceSquaredTo(rot180(src)) + rot180(src).distanceSquaredTo(flipVer(src));
            int d2 = flipHor(src).distanceSquaredTo(flipVer(src)) + flipVer(src).distanceSquaredTo(rot180(src));

            if (d1 < d2) {
                enemyLocs.addLast(rot180(src));
                enemyLocs.addLast(flipVer(src));
            } else {
                enemyLocs.addLast(flipVer(src));
                enemyLocs.addLast(rot180(src));
            }

            // } else if (dist_hormiddle < dist_vertmiddle) {
        } else {
            // first is vert ref
            enemyLocs.addLast(flipVer(src));

            int d1 = flipVer(src).distanceSquaredTo(rot180(src)) + rot180(src).distanceSquaredTo(flipHor(src));
            int d2 = flipVer(src).distanceSquaredTo(flipHor(src)) + flipHor(src).distanceSquaredTo(rot180(src));

            if (d1 < d2) {
                enemyLocs.addLast(rot180(src));
                enemyLocs.addLast(flipHor(src));
            } else {
                enemyLocs.addLast(flipHor(src));
                enemyLocs.addLast(rot180(src));
            }

            // } else {
            //     // first is rot 180
            //     enemyLocs.addLast(rot180(src));

            //     int d1 = rot180(src).distanceSquaredTo(flipHor(src)) + flipHor(src).distanceSquaredTo(flipVer(src));
            //     int d2 = rot180(src).distanceSquaredTo(flipVer(src)) + flipVer(src).distanceSquaredTo(flipHor(src));

            //     if (d1 < d2) {
            //         enemyLocs.addLast(flipHor(src));
            //         enemyLocs.addLast(flipVer(src));
            //     } else {
            //         enemyLocs.addLast(flipVer(src));
            //         enemyLocs.addLast(flipHor(src));
            //     }
        }
        return enemyLocs;
    }
    // public static List<MapLocation> guessEnemyLocs(MapLocation src) throws GameActionException {
    //     return guessEnemyLocs(src, false);
    // }

    // public static int expectedRushDistance(MapLocation src) throws GameActionException {
    //     List<MapLocation> enemyLocs = guessEnemyLocs(src, true);
    //     MapLocation target = enemyLocs.get(0);
    //     return (int)Math.sqrt((double)src.distanceSquaredTo(target));
    // }

    // public static List<MapLocation> guessSpawnLocs() throws GameActionException {
    //     return guessEnemyLocs(origin);
    // }

    public static int chebyshevDistance(MapLocation a, MapLocation b) {
        return max(abs(a.x - b.x), abs(a.y - b.y));
    }

    public static boolean isOccupied(MapLocation loc) throws GameActionException {
        return rc.senseRobotAtLocation(loc) != null;
    }

    // public static boolean isAllyAgent(RobotInfo bot) {
    //     return isFriendly(bot) && bot.getType().isRobotType();
    // }

    // public static MapInfo getClosestNeighbourTo(MapLocation dest, GamePredicate<MapInfo> pred) throws GameActionException {
    //     MapInfo best = null;
    //     for (Direction dir : directions) {
    //         if (dir == Direction.CENTER) continue;
    //         MapLocation newLoc = myLoc.add(dir);
    //         MapInfo newInfo = rc.senseMapInfo(newLoc);
    //         if (pred.test(newInfo)) {
    //             if (best == null || best.getMapLocation().distanceSquaredTo(dest) > newLoc.distanceSquaredTo(dest)) {
    //                 best = newInfo;
    //             }
    //         }
    //     }
    //     return best;
    // }

    // public static boolean canMove(MapLocation loc) throws GameActionException {
    //     Direction dir = getDiff(myLoc, loc);
    //     return rc.canMove(dir);
    // }

    // public static Direction getClosestDirTo(MapLocation dest, GamePredicate<MapInfo> pred) throws GameActionException {
    //     Direction best = null;
    //     for (Direction dir : directions) {
    //         if (dir == Direction.CENTER) continue;
    //         MapLocation newLoc = myLoc.add(dir);
    //         if (rc.onTheMap(newLoc)) {
    //             MapInfo newInfo = rc.senseMapInfo(newLoc);
    //             if (pred.test(newInfo)) {
    //                 if (best == null || myLoc.add(best).distanceSquaredTo(dest) > newLoc.distanceSquaredTo(dest)) {
    //                     best = dir;
    //                 }
    //             }
    //         }
    //     }
    //     return best;
    // }

    public static Direction getDiff(MapLocation src, MapLocation dest) throws GameActionException {
        int dx = dest.x - src.x;
        int dy = dest.y - src.y;
        for (Direction dir : directions) {
            if (dir.getDeltaX() == dx && dir.getDeltaY() == dy) {
                return dir;
            }
        }
        return null;
    }

    // public static boolean isInAttackRange(MapLocation loc) {
    //     return loc.distanceSquaredTo(myLoc) <= rc.getType().actionRadiusSquared;
    // }

    // public static boolean isEnemyAgent(RobotInfo info) {
    //     return info.getType().isRobotType() && !isFriendly(info);
    // }

    // public static void println(Object obj) {
    //     if (time > 100) return;
    //     println(obj);
    // }

    // public static MapInfo getClosestCellTo(MapLocation dest, GamePredicate<MapInfo> pred) throws GameActionException {
    //     MapInfo best = null;
    //     for (MapInfo newLoc : rc.senseNearbyMapInfos()) {
    //         if (pred.test(newLoc)) {
    //             if (best == null || best.getMapLocation().distanceSquaredTo(dest) > newLoc.getMapLocation().distanceSquaredTo(dest)) {
    //                 best = newLoc;
    //             }
    //         }
    //     }
    //     return best;
    // }

    // public static int countNearbyBots(GamePredicate<RobotInfo> pred) throws GameActionException {
    //     int res = 0;
    //     for (RobotInfo bot : rc.senseNearbyRobots()) {
    //         if (pred.test(bot)) {
    //             res++;
    //         }
    //     }
    //     return res;
    // }

    public static MapLocation add(MapLocation a, MapLocation b) {
        return new MapLocation(a.x + b.x, a.y + b.y);
    }

    public static MapLocation subtract(MapLocation a, MapLocation b) {
        return new MapLocation(a.x - b.x, a.y - b.y);
    }

    public static MapLocation rotr(MapLocation vec) {
        return new MapLocation(vec.y, -vec.x);
    }

    public static MapLocation rotl(MapLocation vec) {
        return new MapLocation(-vec.y, vec.x);
    }

    public static MapLocation scale(MapLocation vec, double amt) {
        return new MapLocation((int) (vec.x * amt), (int) (vec.y * amt));
    }

    // public static boolean isFriendly(RobotInfo info) {
    //     return team.equals(info.getTeam());
    // }

    public static MapLocation project(MapLocation cur, MapLocation moveVec, double maxDist) {
        double dx = moveVec.x;
        double dy = moveVec.y;

        double magnitude = Math.sqrt(dx * dx + dy * dy);
        dx /= magnitude;
        dy /= magnitude;

        double tMin = maxDist;

        if (dx < 0) {
            double t = -cur.x / dx;
            if (t >= 0) tMin = Math.min(tMin, t);
        }

        if (dx > 0) {
            double t = (mapWidth - 1 - cur.x) / dx;
            if (t >= 0) tMin = Math.min(tMin, t);
        }

        if (dy < 0) {
            double t = -cur.y / dy;
            if (t >= 0) tMin = Math.min(tMin, t);
        }

        if (dy > 0) {
            double t = (mapHeight - 1 - cur.y) / dy;
            if (t >= 0) tMin = Math.min(tMin, t);
        }

        int x = (int) Math.round(cur.x + tMin * dx);
        int y = (int) Math.round(cur.y + tMin * dy);

        x = Math.max(0, Math.min(mapWidth - 1, x));
        y = Math.max(0, Math.min(mapHeight - 1, y));

        return new MapLocation(x, y);
    }

    public static MapLocation project(MapLocation cur, MapLocation moveVec) {
        return project(cur, moveVec, Double.MAX_VALUE);
    }

    public static MapLocation getOpposite(MapLocation cur) {
        MapLocation centre = new MapLocation(mapWidth / 2, mapHeight / 2);
        int dx = centre.x - cur.x;
        int dy = centre.y - cur.y;
        return project(cur, new MapLocation(dx, dy));
    }

    public static MapLocation genPassiveTarget() {
        while (true) {
            int x = Rng.mcg() % mapWidth;
            int y = Rng.mcg() % mapHeight;
            MapLocation pivot = rc.getLocation();
            if (new MapLocation(x, y).distanceSquaredTo(pivot) < 9) {
                continue;
            }
            MapLocation moveDir = subtract(new MapLocation(x, y), pivot);
            return project(pivot, moveDir, 0.5 * max(mapWidth, mapHeight));
        }
    }

    public static MapLocation genAggroTarget() {
        while (true) {
            int x = Rng.mcg() % mapWidth;
            int y = Rng.mcg() % mapHeight;
            MapLocation loc = new MapLocation(x, y);
            MapLocation pivot = spawn;
            if (loc.distanceSquaredTo(pivot) < 9 || pivot.distanceSquaredTo(loc) < pivot.distanceSquaredTo(new MapLocation(mapWidth / 2, mapHeight / 2))) {
                continue;
            }
            MapLocation moveDir = subtract(loc, pivot);
            return project(pivot, moveDir, 0.8 * max(mapWidth, mapHeight));
        }
    }

    public static MapLocation genExplorationTarget() {
        while (true) {
            int x = Rng.mcg() % mapWidth;
            int y = Rng.mcg() % mapHeight;
            MapLocation pivot = new MapLocation((rc.getLocation().x + spawn.x) / 2, (rc.getLocation().y + spawn.y) / 2);
            if (new MapLocation(x, y).distanceSquaredTo(rc.getLocation()) < 9 || new MapLocation(x, y).distanceSquaredTo(pivot) < 9) {
                continue;
            }
            MapLocation moveDir = subtract(new MapLocation(x, y), pivot);
            return project(pivot, moveDir, 0.7 * max(mapWidth, mapHeight));
        }
    }

    public static List<MapLocation> getEnemySpawnList() throws GameActionException {
        List<MapLocation> locs = guessEnemyLocs(spawn, false);
        return locs;
    }
}