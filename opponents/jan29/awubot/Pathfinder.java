

package awubot;

import battlecode.common.*;
import static awubot.util.Util.*;
import awubot.util.Rng;
import awubot.util.Profiler;
import awubot.util.string.FastMapLocationSet;
import static awubot.Game.*;
import static awubot.robot.rat.BabyRat.*; // TODO: make pathfinder compatible with rat king
import awubot.robot.king.RatKing;
import awubot.comms.GlobalArray;

// IF TLE: lower these constants (especially MAX_BUG_ITERS)

class Config {
    static boolean dirtAllowed = false;
    public static boolean allowDirt() {
        if(dirtAllowed) return true;
        var globalCheese = rc.getGlobalCheese();
        if(time >= 100 && globalCheese >= 1500 || globalCheese >= 3000) {
            dirtAllowed = true;
        }
        return dirtAllowed;
    }
}

class BugnavInstance {
    MapLocation target;

    // false for the instances spawned for lookahead
    boolean isMaster;
    // child instances for lookahead
    BugnavInstance leftBug, rightBug;
    // these are reset when we physically move
    boolean leftDone, rightDone;
    // detect movement with this
    MapLocation lastLoc;
    Direction lastDir;
    // loop detection
    long[] guessedLeft, guessedRight;
    // where we want to go, in case path length is zero, we should turn to here
    MapLocation outOfSightLoc;

    // extra "return variables" for lookahead instances
    boolean hitEdge = false, outOfSight = false, arrived = false;

    FastMapLocationSet vis = new FastMapLocationSet();
    boolean givenUp = false;

    Direction bottomDir, topDir;

    // state (you can reconstruct the entire state from these variables + target/bottomDir)
    MapLocation pos;
    int stackSize;
    int emptyStackTurns;
    boolean leftTurn;

    int windingNumber = 0;

    public int nextPathIndex;
    public MapLocation path0;
    public MapLocation path1;
    public MapLocation path2;
    public MapLocation path3;
    public MapLocation path4;
    public MapLocation path5;
    public MapLocation path6;
    public MapLocation path7;
    public MapLocation path8;
    public MapLocation path9;
    public MapLocation path10;
    public MapLocation path11;
    public MapLocation path12;

    void clearPath() {
        pos = myLoc;
        nextPathIndex = 0;
        stackSize = 0;
        windingNumber = 0;
        emptyStackTurns = 1000;
        bottomDir = topDir = pos.directionTo(target);
        leftBug = rightBug = null;
        path0 = new MapLocation(-1000, -1000);
        path1 = new MapLocation(-1000, -1000);
        path2 = new MapLocation(-1000, -1000);
        path3 = new MapLocation(-1000, -1000);
        path4 = new MapLocation(-1000, -1000);
        path5 = new MapLocation(-1000, -1000);
        path6 = new MapLocation(-1000, -1000);
        path7 = new MapLocation(-1000, -1000);
        path8 = new MapLocation(-1000, -1000);
        path9 = new MapLocation(-1000, -1000);
        path10 = new MapLocation(-1000, -1000);
        path11 = new MapLocation(-1000, -1000);
        path12 = new MapLocation(-1000, -1000);
    }

    BugnavInstance(boolean leftTurn, MapLocation target) {
        this.leftTurn = leftTurn;
        this.target = target;
        this.isMaster = true;
        guessedLeft = new long[mapHeight];
        guessedRight = new long[mapHeight];
        clearPath();
    }

    BugnavInstance(MapLocation pos, boolean leftTurn, MapLocation target, boolean isMaster) {
        this.leftTurn = leftTurn;
        this.target = target;
        this.isMaster = isMaster;
        clearPath();
        this.pos = pos;
        appendCurrentPosition();
    }

    void appendPosition(MapLocation loc) {
        switch (nextPathIndex) {
            case 0 -> {
                if (!path12.equals(loc)) {
                    if(vis.contains(loc) && (stackSize >= 8 || Math.abs(windingNumber) >= 8)) {
                        // indicate("loop detected");
                        // println("loop detected");
                        givenUp = true;
                    }
                    vis.add(loc);
                    path0 = loc;
                    nextPathIndex = 1;
                }
            }
            case 1 -> {
                if (!path0.equals(loc)) {
                    if(vis.contains(loc) && (stackSize >= 8 || Math.abs(windingNumber) >= 8)) {
                        // indicate("loop detected");
                        // println("loop detected");
                        givenUp = true;
                    }
                    vis.add(loc);
                    path1 = loc;
                    nextPathIndex = 2;
                }
            }
            case 2 -> {
                if (!path1.equals(loc)) {
                    if(vis.contains(loc) && (stackSize >= 8 || Math.abs(windingNumber) >= 8)) {
                        // indicate("loop detected");
                        // println("loop detected");
                        givenUp = true;
                    }
                    vis.add(loc);
                    path2 = loc;
                    nextPathIndex = 3;
                }
            }
            case 3 -> {
                if (!path2.equals(loc)) {
                    if(vis.contains(loc) && (stackSize >= 8 || Math.abs(windingNumber) >= 8)) {
                        // indicate("loop detected");
                        // println("loop detected");
                        givenUp = true;
                    }
                    vis.add(loc);
                    path3 = loc;
                    nextPathIndex = 4;
                }
            }
            case 4 -> {
                if (!path3.equals(loc)) {
                    if(vis.contains(loc) && (stackSize >= 8 || Math.abs(windingNumber) >= 8)) {
                        // indicate("loop detected");
                        // println("loop detected");
                        givenUp = true;
                    }
                    vis.add(loc);
                    path4 = loc;
                    nextPathIndex = 5;
                }
            }
            case 5 -> {
                if (!path4.equals(loc)) {
                    if(vis.contains(loc) && (stackSize >= 8 || Math.abs(windingNumber) >= 8)) {
                        // indicate("loop detected");
                        // println("loop detected");
                        givenUp = true;
                    }
                    vis.add(loc);
                    path5 = loc;
                    nextPathIndex = 6;
                }
            }
            case 6 -> {
                if (!path5.equals(loc)) {
                    if(vis.contains(loc) && (stackSize >= 8 || Math.abs(windingNumber) >= 8)) {
                        // indicate("loop detected");
                        // println("loop detected");
                        givenUp = true;
                    }
                    vis.add(loc);
                    path6 = loc;
                    nextPathIndex = 7;
                }
            }
            case 7 -> {
                if (!path6.equals(loc)) {
                    if(vis.contains(loc) && (stackSize >= 8 || Math.abs(windingNumber) >= 8)) {
                        // indicate("loop detected");
                        // println("loop detected");
                        givenUp = true;
                    }
                    vis.add(loc);
                    path7 = loc;
                    nextPathIndex = 8;
                }
            }
            case 8 -> {
                if (!path7.equals(loc)) {
                    if(vis.contains(loc) && (stackSize >= 8 || Math.abs(windingNumber) >= 8)) {
                        // indicate("loop detected");
                        // println("loop detected");
                        givenUp = true;
                    }
                    vis.add(loc);
                    path8 = loc;
                    nextPathIndex = 9;
                }
            }
            case 9 -> {
                if (!path8.equals(loc)) {
                    if(vis.contains(loc) && (stackSize >= 8 || Math.abs(windingNumber) >= 8)) {
                        // indicate("loop detected");
                        // println("loop detected");
                        givenUp = true;
                    }
                    vis.add(loc);
                    path9 = loc;
                    nextPathIndex = 10;
                }
            }
            case 10 -> {
                if (!path9.equals(loc)) {
                    if(vis.contains(loc) && (stackSize >= 8 || Math.abs(windingNumber) >= 8)) {
                        // indicate("loop detected");
                        // println("loop detected");
                        givenUp = true;
                    }
                    vis.add(loc);
                    path10 = loc;
                    nextPathIndex = 11;
                }
            }
            case 11 -> {
                if (!path10.equals(loc)) {
                    if(vis.contains(loc) && (stackSize >= 8 || Math.abs(windingNumber) >= 8)) {
                        // indicate("loop detected");
                        // println("loop detected");
                        givenUp = true;
                    }
                    vis.add(loc);
                    path11 = loc;
                    nextPathIndex = 12;
                }
            }
            case 12 -> {
                if (!path11.equals(loc)) {
                    if(vis.contains(loc) && (stackSize >= 8 || Math.abs(windingNumber) >= 8)) {
                        // indicate("loop detected");
                        // println("loop detected");
                        givenUp = true;
                    }
                    vis.add(loc);
                    path12 = loc;
                    nextPathIndex = 0;
                }
            }
            default -> {
                assert false;
            }
        }
    }

    void appendCurrentPosition() {
        appendPosition(pos);
    }

    MapLocation getRecentPos(int i) {
        switch ((nextPathIndex - i + 12) % 13) {
            case 0 -> {
                return path0;
            }
            case 1 -> {
                return path1;
            }
            case 2 -> {
                return path2;
            }
            case 3 -> {
                return path3;
            }
            case 4 -> {
                return path4;
            }
            case 5 -> {
                return path5;
            }
            case 6 -> {
                return path6;
            }
            case 7 -> {
                return path7;
            }
            case 8 -> {
                return path8;
            }
            case 9 -> {
                return path9;
            }
            case 10 -> {
                return path10;
            }
            case 11 -> {
                return path11;
            }
            case 12 -> {
                return path12;
            }
            default -> {
                assert false : "index out of range";
                return null;
            }
        }
    }

    private static boolean isPassable(MapLocation loc) throws GameActionException {
        if(isRatKing) return CellTracker.isKingPassable(loc);
        RobotInfo robot;
        return CellTracker.isPassable(loc) && !RobotTracker.kingSet.contains(loc) && (Config.allowDirt() || !CellTracker.isDirt(loc) || CellTracker.isWallAdjacent(loc))
            && !(canSense(myLoc, loc, myDir) && rc.getRawCheese() < 20 && (robot = rc.senseRobotAtLocation(loc)) != null && robot.team == team && robot.cheeseAmount >= 40);
    }

    private static boolean possiblyPassable(MapLocation loc) throws GameActionException {
        if(isRatKing) return CellTracker.isKingPassable(loc);
        return (CellTracker.isPassable(loc) || !CellTracker.isSeen(loc)) && !RobotTracker.kingSet.contains(loc) && (Config.allowDirt() || !CellTracker.isDirt(loc) || CellTracker.isWallAdjacent(loc));
    }

    boolean doLookahead() throws GameActionException {
        // indicate("lookahead!!");
        if(leftBug == null) {
            // initialise
            leftBug = new BugnavInstance(pos, true, target, false);
            rightBug = new BugnavInstance(pos, false, target, false);
            lastLoc = new MapLocation(-1, -1);
            lastDir = Direction.CENTER;
            return true;
        }
        // rc.setIndicatorDot(leftBug.pos, 255, 255, 0);
        // rc.setIndicatorDot(rightBug.pos, 255, 255, 0);
        // // Profiler.begin();
        if(!myLoc.equals(lastLoc) || !myDir.equals(lastDir)) {
            // we moved, so we might be able to advance previously stuck stuff further
            lastLoc = myLoc;
            lastDir = myDir;
            leftDone = leftBug.hitEdge || leftBug.nextPathIndex == 12;
            rightDone = rightBug.hitEdge || rightBug.nextPathIndex == 12;
        }

        if(!(leftDone && rightDone)) {
            if(rightDone) {
                            // indicate("advance left");
            leftBug.outOfSight = false;
            leftDone = !leftBug.advance();
            // indicate(leftBug.pos);
            // rc.setIndicatorDot(leftBug.pos, 255, 0, 255);

            } else if(leftDone) {
                            // indicate("advance right");
            rightBug.outOfSight = false;
            rightDone = !rightBug.advance();
            // indicate(rightBug.pos);
            // rc.setIndicatorDot(rightBug.pos, 255, 0, 255);

            } else if(leftBug.nextPathIndex > rightBug.nextPathIndex) {
                            // indicate("advance right");
            rightBug.outOfSight = false;
            rightDone = !rightBug.advance();
            // indicate(rightBug.pos);
            // rc.setIndicatorDot(rightBug.pos, 255, 0, 255);

            } else {
                            // indicate("advance left");
            leftBug.outOfSight = false;
            leftDone = !leftBug.advance();
            // indicate(leftBug.pos);
            // rc.setIndicatorDot(leftBug.pos, 255, 0, 255);

            }
        }
        // // Profiler.end("lookahead");

        boolean leftReady = leftBug.nextPathIndex >= 5 || leftBug.hitEdge || leftBug.outOfSight || leftBug.arrived;
        boolean rightReady = rightBug.nextPathIndex >= 5 || rightBug.hitEdge || rightBug.outOfSight || rightBug.arrived;
        // indicate("ready " + leftReady + " " + rightReady);
        if(!myLoc.equals(pos) && (!leftReady || !rightReady)) return !(leftDone && rightDone);
        // indicate("going ahead");

        var leftScore =  Math.sqrt(leftBug.pos.distanceSquaredTo(target)) + leftBug.nextPathIndex * 0.7 ;
        var rightScore =  Math.sqrt(rightBug.pos.distanceSquaredTo(target)) + rightBug.nextPathIndex * 0.7 ;
        // indicate("lookahead" + pos);
        // indicate(leftBug.pos);
        // indicate(leftScore);
        // indicate(rightBug.pos);
        // indicate(rightScore);


        if(leftBug.arrived) {
                    var bug = leftBug;
        if(bug.leftTurn) {
            guessedLeft[pos.y] |= 1l << pos.x;
        } else {
            guessedRight[pos.y] |= 1l << pos.x;
        }
        pos = bug.pos;
        stackSize = bug.stackSize;
        emptyStackTurns = bug.emptyStackTurns;
        leftTurn = bug.leftTurn;
        bottomDir = bug.bottomDir;
        topDir = bug.topDir;
        outOfSightLoc = bug.outOfSightLoc;
        copyPath: {
            appendPosition(bug.path0);
            if(bug.nextPathIndex == 1) break copyPath;
            appendPosition(bug.path1);
            if(bug.nextPathIndex == 2) break copyPath;
            appendPosition(bug.path2);
            if(bug.nextPathIndex == 3) break copyPath;
            appendPosition(bug.path3);
            if(bug.nextPathIndex == 4) break copyPath;
            appendPosition(bug.path4);
            if(bug.nextPathIndex == 5) break copyPath;
            appendPosition(bug.path5);
            if(bug.nextPathIndex == 6) break copyPath;
            appendPosition(bug.path6);
            if(bug.nextPathIndex == 7) break copyPath;
            appendPosition(bug.path7);
            if(bug.nextPathIndex == 8) break copyPath;
            appendPosition(bug.path8);
            if(bug.nextPathIndex == 9) break copyPath;
            appendPosition(bug.path9);
            if(bug.nextPathIndex == 10) break copyPath;
            appendPosition(bug.path10);
            if(bug.nextPathIndex == 11) break copyPath;
            appendPosition(bug.path11);
            if(bug.nextPathIndex == 12) break copyPath;
            appendPosition(bug.path12);
        }
        leftBug = null;
        rightBug = null;

        } else if(leftBug.arrived) {
                    var bug = rightBug;
        if(bug.leftTurn) {
            guessedLeft[pos.y] |= 1l << pos.x;
        } else {
            guessedRight[pos.y] |= 1l << pos.x;
        }
        pos = bug.pos;
        stackSize = bug.stackSize;
        emptyStackTurns = bug.emptyStackTurns;
        leftTurn = bug.leftTurn;
        bottomDir = bug.bottomDir;
        topDir = bug.topDir;
        outOfSightLoc = bug.outOfSightLoc;
        copyPath: {
            appendPosition(bug.path0);
            if(bug.nextPathIndex == 1) break copyPath;
            appendPosition(bug.path1);
            if(bug.nextPathIndex == 2) break copyPath;
            appendPosition(bug.path2);
            if(bug.nextPathIndex == 3) break copyPath;
            appendPosition(bug.path3);
            if(bug.nextPathIndex == 4) break copyPath;
            appendPosition(bug.path4);
            if(bug.nextPathIndex == 5) break copyPath;
            appendPosition(bug.path5);
            if(bug.nextPathIndex == 6) break copyPath;
            appendPosition(bug.path6);
            if(bug.nextPathIndex == 7) break copyPath;
            appendPosition(bug.path7);
            if(bug.nextPathIndex == 8) break copyPath;
            appendPosition(bug.path8);
            if(bug.nextPathIndex == 9) break copyPath;
            appendPosition(bug.path9);
            if(bug.nextPathIndex == 10) break copyPath;
            appendPosition(bug.path10);
            if(bug.nextPathIndex == 11) break copyPath;
            appendPosition(bug.path11);
            if(bug.nextPathIndex == 12) break copyPath;
            appendPosition(bug.path12);
        }
        leftBug = null;
        rightBug = null;

        } else if(leftBug.hitEdge != rightBug.hitEdge) {
                    var bug = rightBug.hitEdge ? leftBug : rightBug;
        if(bug.leftTurn) {
            guessedLeft[pos.y] |= 1l << pos.x;
        } else {
            guessedRight[pos.y] |= 1l << pos.x;
        }
        pos = bug.pos;
        stackSize = bug.stackSize;
        emptyStackTurns = bug.emptyStackTurns;
        leftTurn = bug.leftTurn;
        bottomDir = bug.bottomDir;
        topDir = bug.topDir;
        outOfSightLoc = bug.outOfSightLoc;
        copyPath: {
            appendPosition(bug.path0);
            if(bug.nextPathIndex == 1) break copyPath;
            appendPosition(bug.path1);
            if(bug.nextPathIndex == 2) break copyPath;
            appendPosition(bug.path2);
            if(bug.nextPathIndex == 3) break copyPath;
            appendPosition(bug.path3);
            if(bug.nextPathIndex == 4) break copyPath;
            appendPosition(bug.path4);
            if(bug.nextPathIndex == 5) break copyPath;
            appendPosition(bug.path5);
            if(bug.nextPathIndex == 6) break copyPath;
            appendPosition(bug.path6);
            if(bug.nextPathIndex == 7) break copyPath;
            appendPosition(bug.path7);
            if(bug.nextPathIndex == 8) break copyPath;
            appendPosition(bug.path8);
            if(bug.nextPathIndex == 9) break copyPath;
            appendPosition(bug.path9);
            if(bug.nextPathIndex == 10) break copyPath;
            appendPosition(bug.path10);
            if(bug.nextPathIndex == 11) break copyPath;
            appendPosition(bug.path11);
            if(bug.nextPathIndex == 12) break copyPath;
            appendPosition(bug.path12);
        }
        leftBug = null;
        rightBug = null;

        } else {
            var better = leftScore < rightScore ? leftBug : rightBug;
            var worse = leftScore < rightScore ? rightBug : leftBug;
            if(!better.outOfSight && worse.outOfSight) {
                // indicate("clear winner");
                        var bug = better;
        if(bug.leftTurn) {
            guessedLeft[pos.y] |= 1l << pos.x;
        } else {
            guessedRight[pos.y] |= 1l << pos.x;
        }
        pos = bug.pos;
        stackSize = bug.stackSize;
        emptyStackTurns = bug.emptyStackTurns;
        leftTurn = bug.leftTurn;
        bottomDir = bug.bottomDir;
        topDir = bug.topDir;
        outOfSightLoc = bug.outOfSightLoc;
        copyPath: {
            appendPosition(bug.path0);
            if(bug.nextPathIndex == 1) break copyPath;
            appendPosition(bug.path1);
            if(bug.nextPathIndex == 2) break copyPath;
            appendPosition(bug.path2);
            if(bug.nextPathIndex == 3) break copyPath;
            appendPosition(bug.path3);
            if(bug.nextPathIndex == 4) break copyPath;
            appendPosition(bug.path4);
            if(bug.nextPathIndex == 5) break copyPath;
            appendPosition(bug.path5);
            if(bug.nextPathIndex == 6) break copyPath;
            appendPosition(bug.path6);
            if(bug.nextPathIndex == 7) break copyPath;
            appendPosition(bug.path7);
            if(bug.nextPathIndex == 8) break copyPath;
            appendPosition(bug.path8);
            if(bug.nextPathIndex == 9) break copyPath;
            appendPosition(bug.path9);
            if(bug.nextPathIndex == 10) break copyPath;
            appendPosition(bug.path10);
            if(bug.nextPathIndex == 11) break copyPath;
            appendPosition(bug.path11);
            if(bug.nextPathIndex == 12) break copyPath;
            appendPosition(bug.path12);
        }
        leftBug = null;
        rightBug = null;

            } else if(!better.outOfSight || myLoc.isAdjacentTo(pos)) {
                // indicate("unclear, guessing");
                leftScore = Math.pow(leftScore, 15);
                rightScore = Math.pow(rightScore, 15);
                var total = leftScore + rightScore;
                leftScore /= total;
                rightScore /= total;
                // indicate(leftScore);
                // indicate(rightScore);
                        var bug = Rng.mcg() < rightScore * 0x7fffffff ? leftBug : rightBug;
        if(bug.leftTurn) {
            guessedLeft[pos.y] |= 1l << pos.x;
        } else {
            guessedRight[pos.y] |= 1l << pos.x;
        }
        pos = bug.pos;
        stackSize = bug.stackSize;
        emptyStackTurns = bug.emptyStackTurns;
        leftTurn = bug.leftTurn;
        bottomDir = bug.bottomDir;
        topDir = bug.topDir;
        outOfSightLoc = bug.outOfSightLoc;
        copyPath: {
            appendPosition(bug.path0);
            if(bug.nextPathIndex == 1) break copyPath;
            appendPosition(bug.path1);
            if(bug.nextPathIndex == 2) break copyPath;
            appendPosition(bug.path2);
            if(bug.nextPathIndex == 3) break copyPath;
            appendPosition(bug.path3);
            if(bug.nextPathIndex == 4) break copyPath;
            appendPosition(bug.path4);
            if(bug.nextPathIndex == 5) break copyPath;
            appendPosition(bug.path5);
            if(bug.nextPathIndex == 6) break copyPath;
            appendPosition(bug.path6);
            if(bug.nextPathIndex == 7) break copyPath;
            appendPosition(bug.path7);
            if(bug.nextPathIndex == 8) break copyPath;
            appendPosition(bug.path8);
            if(bug.nextPathIndex == 9) break copyPath;
            appendPosition(bug.path9);
            if(bug.nextPathIndex == 10) break copyPath;
            appendPosition(bug.path10);
            if(bug.nextPathIndex == 11) break copyPath;
            appendPosition(bug.path11);
            if(bug.nextPathIndex == 12) break copyPath;
            appendPosition(bug.path12);
        }
        leftBug = null;
        rightBug = null;

            }
        }
        return true;
    }

    // false = halt bug
    boolean advance() throws GameActionException {
        if(leftBug != null) {
            // we are currently in the middle of a lookahead
            return doLookahead();
        }
        // indicate("leftTurn " + leftTurn);

        if(pos.isAdjacentTo(target)) {
            pos = target;
            appendCurrentPosition();
            arrived = true;
            return false;
        }

        var dirToTarget = pos.directionTo(target);
        if(dirToTarget != bottomDir) {
            // indicate("changing bottomDir from " + bottomDir + " to " + dirToTarget);
            if(dirToTarget == Direction.CENTER) return false;
            // indicate("NEW DIR " + dirToTarget);
            if(dirToTarget == bottomDir.rotateLeft()) {
                windingNumber++;
                // indicate("INCREMENT");
            } else if(dirToTarget == bottomDir.rotateRight()) {
                windingNumber--;
                // indicate("DECREMENT");
            }
            if(stackSize == 0) {
                bottomDir = topDir = dirToTarget;
            } else {
                if(leftTurn) {
                                    if(dirToTarget == bottomDir.rotateRight()) {
                    bottomDir = dirToTarget;
                    stackSize++;
                } else {
                    if(dirToTarget != bottomDir.rotateLeft()) {
                        // System.out.println("unexpected direction change from " + bottomDir + " to " + dirToTarget);
                        clearPath();
                    } else {
                        bottomDir = dirToTarget;
                        stackSize--;
                        if(stackSize < 0) {
                            stackSize = 0;
                            topDir = bottomDir;
                        }
                    }
                }

                } else {
                                    if(dirToTarget == bottomDir.rotateLeft()) {
                    bottomDir = dirToTarget;
                    stackSize++;
                } else {
                    if(dirToTarget != bottomDir.rotateRight()) {
                        // System.out.println("unexpected direction change from " + bottomDir + " to " + dirToTarget);
                        clearPath();
                    } else {
                        bottomDir = dirToTarget;
                        stackSize--;
                        if(stackSize < 0) {
                            stackSize = 0;
                            topDir = bottomDir;
                        }
                    }
                }

                }
            }
        }
        // indicate("winding number " + windingNumber);
        // indicate("direction " + pos + " " + topDir + " " + bottomDir + " " + leftTurn);

        MapLocation greedyLoc = pos.add(bottomDir);
        if(stackSize <= 1) {
            var greedyGood = possiblyPassable(greedyLoc);
            if(greedyGood) {
                            if(isPassable(greedyLoc) && chebyshevDistance(myLoc, greedyLoc) <= 5) {
                // indicate("greedyLoc " + greedyLoc);
                pos = greedyLoc;
                topDir = bottomDir;
                stackSize = 0;
                emptyStackTurns++;
                appendCurrentPosition();
                return true;
            }
            // rc.setIndicatorDot(greedyLoc, 0, 255, 255);
            outOfSight = true;
            outOfSightLoc = greedyLoc;
            return false;

            }
            var leftLoc = pos.add(bottomDir.rotateLeft());
            var rightLoc = pos.add(bottomDir.rotateRight());
            var leftGood = possiblyPassable(leftLoc);
            var rightGood = possiblyPassable(rightLoc);
            if(leftGood && !rightGood) {
                            if(isPassable(leftLoc) && chebyshevDistance(myLoc, leftLoc) <= 5) {
                // indicate("leftLoc " + leftLoc);
                pos = leftLoc;
                topDir = bottomDir;
                stackSize = 0;
                emptyStackTurns++;
                appendCurrentPosition();
                return true;
            }
            // rc.setIndicatorDot(leftLoc, 0, 255, 255);
            outOfSight = true;
            outOfSightLoc = leftLoc;
            return false;

            }
            if(!leftGood && rightGood) {
                            if(isPassable(rightLoc) && chebyshevDistance(myLoc, rightLoc) <= 5) {
                // indicate("rightLoc " + rightLoc);
                pos = rightLoc;
                topDir = bottomDir;
                stackSize = 0;
                emptyStackTurns++;
                appendCurrentPosition();
                return true;
            }
            // rc.setIndicatorDot(rightLoc, 0, 255, 255);
            outOfSight = true;
            outOfSightLoc = rightLoc;
            return false;

            }
            if(leftGood && rightGood) {
                if(leftLoc.distanceSquaredTo(target) < rightLoc.distanceSquaredTo(target)) {
                                if(isPassable(leftLoc) && chebyshevDistance(myLoc, leftLoc) <= 5) {
                // indicate("leftLoc " + leftLoc);
                pos = leftLoc;
                topDir = bottomDir;
                stackSize = 0;
                emptyStackTurns++;
                appendCurrentPosition();
                return true;
            }
            // rc.setIndicatorDot(leftLoc, 0, 255, 255);
            outOfSight = true;
            outOfSightLoc = leftLoc;
            return false;

                } else {
                                if(isPassable(rightLoc) && chebyshevDistance(myLoc, rightLoc) <= 5) {
                // indicate("rightLoc " + rightLoc);
                pos = rightLoc;
                topDir = bottomDir;
                stackSize = 0;
                emptyStackTurns++;
                appendCurrentPosition();
                return true;
            }
            // rc.setIndicatorDot(rightLoc, 0, 255, 255);
            outOfSight = true;
            outOfSightLoc = rightLoc;
            return false;

                }
            }
        }

        // guess rotation direction (kinda inefficient but wtv)
        if(isMaster && stackSize == 0 && (CellTracker.isSeen(greedyLoc) || RobotTracker.kingSet.contains(greedyLoc))) {
            if(emptyStackTurns >= 2) {
                boolean pastLeft = ((guessedLeft[pos.y] >> pos.x) & 1l) != 0;
                boolean pastRight = ((guessedRight[pos.y] >> pos.x) & 1l) != 0;
                if(pastLeft && pastRight) {
                    // indicate("idk which way");
                    // just pick randomly, we have no idea what's going on
                    // leftTurn = Rng.mcg() >> 30 == 0;
                    // keep going the same way, we'll get there eventually
                } else if(pastLeft) {
                    // indicate("past left");
                    // try the other way
                    leftTurn = false;
                    guessedRight[pos.y] |= 1l << pos.x;
                } else if(pastRight) {
                    // indicate("past right");
                    leftTurn = true;
                    guessedLeft[pos.y] |= 1l << pos.x;
                } else if(isRatKing) {
                    if(leftTurn) {
                        guessedLeft[pos.y] |= 1l << pos.x;
                    } else {
                        guessedRight[pos.y] |= 1l << pos.x;
                    }
                } else {
                    // indicate("looking ahead");
                    return doLookahead();
                }
            } else if(emptyStackTurns < 0) {
                emptyStackTurns = 0;
            }
        }
        if(leftTurn) {
            return tryDirsLeft();
        } else {
            return tryDirsRight();
        }
    }
        private boolean tryDirsLeft() throws GameActionException {
        var tryDir = topDir.rotateRight().rotateRight();
        if(stackSize + -2 >= 0
           // only try -2 for diagonals
           && Math.abs(topDir.dx + topDir.dy) != 1
           )
        {
            // System.out.println("T " + tryDir);
            var nextPos = pos.add(tryDir);
            if(!rc.onTheMap(nextPos)) {
                 if(isMaster) {
                    // avoid following the edge of the map (TODO: 1-thick borders)
                    // indicate("bugnav edge: leftTurn = " + leftTurn + " bottomDir = " + bottomDir + " tryDir = " + tryDir + " turns = " + -2);
                    if(vis.contains(nextPos)) {
                        // indicate("gave up at edge");
                        // System.out.println("gave up at edge");
                        givenUp = true;
                        return false;
                    }
                    vis.add(nextPos);
                    leftTurn = !leftTurn;
                    clearPath();
                    // don't guess turn direction, we know that one direction is bad already
                    emptyStackTurns = -1000;
                    // start again
                    return tryDirsRight();
                } else {
                    hitEdge = true;
                }
                // // Profiler.end("bugnav " + -2 + " " + isMaster);
                // indicate("bugnav " + -2 + " " + isMaster);
                return false;
            }
            var isPassable = isPassable(nextPos);
            if(isPassable && chebyshevDistance(myLoc, nextPos) > 5 || possiblyPassable(nextPos) && !isPassable) {
                // rc.setIndicatorDot(nextPos, 0, 255, 255);
                outOfSight = true;
                outOfSightLoc = nextPos;
                // // Profiler.end("bugnav " + -2 + " " + isMaster);
                // indicate("bugnav " + -2 + " " + isMaster);
                return false;
            }
            if(isPassable) {
                pos = nextPos;
                topDir = tryDir;
                stackSize += -2;
                if(stackSize == 0) {
                    emptyStackTurns++;
                } else {
                    emptyStackTurns = 0;
                }
                appendCurrentPosition();
                // // Profiler.end("bugnav " + -2 + " " + isMaster);
                // indicate("bugnav " + -2 + " " + isMaster);
                return true;
            }
        }
        tryDir = tryDir.rotateLeft();
        if(stackSize + -1 >= 0
           // only try -2 for diagonals
           )
        {
            // System.out.println("T " + tryDir);
            var nextPos = pos.add(tryDir);
            if(!rc.onTheMap(nextPos)) {
                 if(isMaster) {
                    // avoid following the edge of the map (TODO: 1-thick borders)
                    // indicate("bugnav edge: leftTurn = " + leftTurn + " bottomDir = " + bottomDir + " tryDir = " + tryDir + " turns = " + -1);
                    if(vis.contains(nextPos)) {
                        // indicate("gave up at edge");
                        // System.out.println("gave up at edge");
                        givenUp = true;
                        return false;
                    }
                    vis.add(nextPos);
                    leftTurn = !leftTurn;
                    clearPath();
                    // don't guess turn direction, we know that one direction is bad already
                    emptyStackTurns = -1000;
                    // start again
                    return tryDirsRight();
                } else {
                    hitEdge = true;
                }
                // // Profiler.end("bugnav " + -1 + " " + isMaster);
                // indicate("bugnav " + -1 + " " + isMaster);
                return false;
            }
            var isPassable = isPassable(nextPos);
            if(isPassable && chebyshevDistance(myLoc, nextPos) > 5 || possiblyPassable(nextPos) && !isPassable) {
                // rc.setIndicatorDot(nextPos, 0, 255, 255);
                outOfSight = true;
                outOfSightLoc = nextPos;
                // // Profiler.end("bugnav " + -1 + " " + isMaster);
                // indicate("bugnav " + -1 + " " + isMaster);
                return false;
            }
            if(isPassable) {
                pos = nextPos;
                topDir = tryDir;
                stackSize += -1;
                if(stackSize == 0) {
                    emptyStackTurns++;
                } else {
                    emptyStackTurns = 0;
                }
                appendCurrentPosition();
                // // Profiler.end("bugnav " + -1 + " " + isMaster);
                // indicate("bugnav " + -1 + " " + isMaster);
                return true;
            }
        }
        tryDir = tryDir.rotateLeft();
        {
            // System.out.println("T " + tryDir);
            var nextPos = pos.add(tryDir);
            if(!rc.onTheMap(nextPos)) {
                 if(isMaster) {
                    // avoid following the edge of the map (TODO: 1-thick borders)
                    // indicate("bugnav edge: leftTurn = " + leftTurn + " bottomDir = " + bottomDir + " tryDir = " + tryDir + " turns = " + 0);
                    if(vis.contains(nextPos)) {
                        // indicate("gave up at edge");
                        // System.out.println("gave up at edge");
                        givenUp = true;
                        return false;
                    }
                    vis.add(nextPos);
                    leftTurn = !leftTurn;
                    clearPath();
                    // don't guess turn direction, we know that one direction is bad already
                    emptyStackTurns = -1000;
                    // start again
                    return tryDirsRight();
                } else {
                    hitEdge = true;
                }
                // // Profiler.end("bugnav " + 0 + " " + isMaster);
                // indicate("bugnav " + 0 + " " + isMaster);
                return false;
            }
            var isPassable = isPassable(nextPos);
            if(isPassable && chebyshevDistance(myLoc, nextPos) > 5 || possiblyPassable(nextPos) && !isPassable) {
                // rc.setIndicatorDot(nextPos, 0, 255, 255);
                outOfSight = true;
                outOfSightLoc = nextPos;
                // // Profiler.end("bugnav " + 0 + " " + isMaster);
                // indicate("bugnav " + 0 + " " + isMaster);
                return false;
            }
            if(isPassable) {
                pos = nextPos;
                topDir = tryDir;
                if(stackSize == 0) {
                    emptyStackTurns++;
                } else {
                    emptyStackTurns = 0;
                }
                appendCurrentPosition();
                // // Profiler.end("bugnav " + 0 + " " + isMaster);
                // indicate("bugnav " + 0 + " " + isMaster);
                return true;
            }
        }
        tryDir = tryDir.rotateLeft();
        {
            // System.out.println("T " + tryDir);
            var nextPos = pos.add(tryDir);
            if(!rc.onTheMap(nextPos)) {
                 if(isMaster) {
                    // avoid following the edge of the map (TODO: 1-thick borders)
                    // indicate("bugnav edge: leftTurn = " + leftTurn + " bottomDir = " + bottomDir + " tryDir = " + tryDir + " turns = " + 1);
                    if(vis.contains(nextPos)) {
                        // indicate("gave up at edge");
                        // System.out.println("gave up at edge");
                        givenUp = true;
                        return false;
                    }
                    vis.add(nextPos);
                    leftTurn = !leftTurn;
                    clearPath();
                    // don't guess turn direction, we know that one direction is bad already
                    emptyStackTurns = -1000;
                    // start again
                    return tryDirsRight();
                } else {
                    hitEdge = true;
                }
                // // Profiler.end("bugnav " + 1 + " " + isMaster);
                // indicate("bugnav " + 1 + " " + isMaster);
                return false;
            }
            var isPassable = isPassable(nextPos);
            if(isPassable && chebyshevDistance(myLoc, nextPos) > 5 || possiblyPassable(nextPos) && !isPassable) {
                // rc.setIndicatorDot(nextPos, 0, 255, 255);
                outOfSight = true;
                outOfSightLoc = nextPos;
                // // Profiler.end("bugnav " + 1 + " " + isMaster);
                // indicate("bugnav " + 1 + " " + isMaster);
                return false;
            }
            if(isPassable) {
                pos = nextPos;
                topDir = tryDir;
                stackSize += 1;
                if(stackSize == 0) {
                    emptyStackTurns++;
                } else {
                    emptyStackTurns = 0;
                }
                appendCurrentPosition();
                // // Profiler.end("bugnav " + 1 + " " + isMaster);
                // indicate("bugnav " + 1 + " " + isMaster);
                return true;
            }
        }
        tryDir = tryDir.rotateLeft();
        {
            // System.out.println("T " + tryDir);
            var nextPos = pos.add(tryDir);
            if(!rc.onTheMap(nextPos)) {
                 if(isMaster) {
                    // avoid following the edge of the map (TODO: 1-thick borders)
                    // indicate("bugnav edge: leftTurn = " + leftTurn + " bottomDir = " + bottomDir + " tryDir = " + tryDir + " turns = " + 2);
                    if(vis.contains(nextPos)) {
                        // indicate("gave up at edge");
                        // System.out.println("gave up at edge");
                        givenUp = true;
                        return false;
                    }
                    vis.add(nextPos);
                    leftTurn = !leftTurn;
                    clearPath();
                    // don't guess turn direction, we know that one direction is bad already
                    emptyStackTurns = -1000;
                    // start again
                    return tryDirsRight();
                } else {
                    hitEdge = true;
                }
                // // Profiler.end("bugnav " + 2 + " " + isMaster);
                // indicate("bugnav " + 2 + " " + isMaster);
                return false;
            }
            var isPassable = isPassable(nextPos);
            if(isPassable && chebyshevDistance(myLoc, nextPos) > 5 || possiblyPassable(nextPos) && !isPassable) {
                // rc.setIndicatorDot(nextPos, 0, 255, 255);
                outOfSight = true;
                outOfSightLoc = nextPos;
                // // Profiler.end("bugnav " + 2 + " " + isMaster);
                // indicate("bugnav " + 2 + " " + isMaster);
                return false;
            }
            if(isPassable) {
                pos = nextPos;
                topDir = tryDir;
                stackSize += 2;
                if(stackSize == 0) {
                    emptyStackTurns++;
                } else {
                    emptyStackTurns = 0;
                }
                appendCurrentPosition();
                // // Profiler.end("bugnav " + 2 + " " + isMaster);
                // indicate("bugnav " + 2 + " " + isMaster);
                return true;
            }
        }
        tryDir = tryDir.rotateLeft();
        {
            // System.out.println("T " + tryDir);
            var nextPos = pos.add(tryDir);
            if(!rc.onTheMap(nextPos)) {
                 if(isMaster) {
                    // avoid following the edge of the map (TODO: 1-thick borders)
                    // indicate("bugnav edge: leftTurn = " + leftTurn + " bottomDir = " + bottomDir + " tryDir = " + tryDir + " turns = " + 3);
                    if(vis.contains(nextPos)) {
                        // indicate("gave up at edge");
                        // System.out.println("gave up at edge");
                        givenUp = true;
                        return false;
                    }
                    vis.add(nextPos);
                    leftTurn = !leftTurn;
                    clearPath();
                    // don't guess turn direction, we know that one direction is bad already
                    emptyStackTurns = -1000;
                    // start again
                    return tryDirsRight();
                } else {
                    hitEdge = true;
                }
                // // Profiler.end("bugnav " + 3 + " " + isMaster);
                // indicate("bugnav " + 3 + " " + isMaster);
                return false;
            }
            var isPassable = isPassable(nextPos);
            if(isPassable && chebyshevDistance(myLoc, nextPos) > 5 || possiblyPassable(nextPos) && !isPassable) {
                // rc.setIndicatorDot(nextPos, 0, 255, 255);
                outOfSight = true;
                outOfSightLoc = nextPos;
                // // Profiler.end("bugnav " + 3 + " " + isMaster);
                // indicate("bugnav " + 3 + " " + isMaster);
                return false;
            }
            if(isPassable) {
                pos = nextPos;
                topDir = tryDir;
                stackSize += 3;
                if(stackSize == 0) {
                    emptyStackTurns++;
                } else {
                    emptyStackTurns = 0;
                }
                appendCurrentPosition();
                // // Profiler.end("bugnav " + 3 + " " + isMaster);
                // indicate("bugnav " + 3 + " " + isMaster);
                return true;
            }
        }
        tryDir = tryDir.rotateLeft();
        {
            // System.out.println("T " + tryDir);
            var nextPos = pos.add(tryDir);
            if(!rc.onTheMap(nextPos)) {
                 if(isMaster) {
                    // avoid following the edge of the map (TODO: 1-thick borders)
                    // indicate("bugnav edge: leftTurn = " + leftTurn + " bottomDir = " + bottomDir + " tryDir = " + tryDir + " turns = " + 4);
                    if(vis.contains(nextPos)) {
                        // indicate("gave up at edge");
                        // System.out.println("gave up at edge");
                        givenUp = true;
                        return false;
                    }
                    vis.add(nextPos);
                    leftTurn = !leftTurn;
                    clearPath();
                    // don't guess turn direction, we know that one direction is bad already
                    emptyStackTurns = -1000;
                    // start again
                    return tryDirsRight();
                } else {
                    hitEdge = true;
                }
                // // Profiler.end("bugnav " + 4 + " " + isMaster);
                // indicate("bugnav " + 4 + " " + isMaster);
                return false;
            }
            var isPassable = isPassable(nextPos);
            if(isPassable && chebyshevDistance(myLoc, nextPos) > 5 || possiblyPassable(nextPos) && !isPassable) {
                // rc.setIndicatorDot(nextPos, 0, 255, 255);
                outOfSight = true;
                outOfSightLoc = nextPos;
                // // Profiler.end("bugnav " + 4 + " " + isMaster);
                // indicate("bugnav " + 4 + " " + isMaster);
                return false;
            }
            if(isPassable) {
                pos = nextPos;
                topDir = tryDir;
                stackSize += 4;
                if(stackSize == 0) {
                    emptyStackTurns++;
                } else {
                    emptyStackTurns = 0;
                }
                appendCurrentPosition();
                // // Profiler.end("bugnav " + 4 + " " + isMaster);
                // indicate("bugnav " + 4 + " " + isMaster);
                return true;
            }
        }
        tryDir = tryDir.rotateLeft();
        {
            // System.out.println("T " + tryDir);
            var nextPos = pos.add(tryDir);
            if(!rc.onTheMap(nextPos)) {
                 if(isMaster) {
                    // avoid following the edge of the map (TODO: 1-thick borders)
                    // indicate("bugnav edge: leftTurn = " + leftTurn + " bottomDir = " + bottomDir + " tryDir = " + tryDir + " turns = " + 5);
                    if(vis.contains(nextPos)) {
                        // indicate("gave up at edge");
                        // System.out.println("gave up at edge");
                        givenUp = true;
                        return false;
                    }
                    vis.add(nextPos);
                    leftTurn = !leftTurn;
                    clearPath();
                    // don't guess turn direction, we know that one direction is bad already
                    emptyStackTurns = -1000;
                    // start again
                    return tryDirsRight();
                } else {
                    hitEdge = true;
                }
                // // Profiler.end("bugnav " + 5 + " " + isMaster);
                // indicate("bugnav " + 5 + " " + isMaster);
                return false;
            }
            var isPassable = isPassable(nextPos);
            if(isPassable && chebyshevDistance(myLoc, nextPos) > 5 || possiblyPassable(nextPos) && !isPassable) {
                // rc.setIndicatorDot(nextPos, 0, 255, 255);
                outOfSight = true;
                outOfSightLoc = nextPos;
                // // Profiler.end("bugnav " + 5 + " " + isMaster);
                // indicate("bugnav " + 5 + " " + isMaster);
                return false;
            }
            if(isPassable) {
                pos = nextPos;
                topDir = tryDir;
                stackSize += 5;
                if(stackSize == 0) {
                    emptyStackTurns++;
                } else {
                    emptyStackTurns = 0;
                }
                appendCurrentPosition();
                // // Profiler.end("bugnav " + 5 + " " + isMaster);
                // indicate("bugnav " + 5 + " " + isMaster);
                return true;
            }
        }
        tryDir = tryDir.rotateLeft();
        {
            // System.out.println("T " + tryDir);
            var nextPos = pos.add(tryDir);
            if(!rc.onTheMap(nextPos)) {
                 if(isMaster) {
                    // avoid following the edge of the map (TODO: 1-thick borders)
                    // indicate("bugnav edge: leftTurn = " + leftTurn + " bottomDir = " + bottomDir + " tryDir = " + tryDir + " turns = " + 6);
                    if(vis.contains(nextPos)) {
                        // indicate("gave up at edge");
                        // System.out.println("gave up at edge");
                        givenUp = true;
                        return false;
                    }
                    vis.add(nextPos);
                    leftTurn = !leftTurn;
                    clearPath();
                    // don't guess turn direction, we know that one direction is bad already
                    emptyStackTurns = -1000;
                    // start again
                    return tryDirsRight();
                } else {
                    hitEdge = true;
                }
                // // Profiler.end("bugnav " + 6 + " " + isMaster);
                // indicate("bugnav " + 6 + " " + isMaster);
                return false;
            }
            var isPassable = isPassable(nextPos);
            if(isPassable && chebyshevDistance(myLoc, nextPos) > 5 || possiblyPassable(nextPos) && !isPassable) {
                // rc.setIndicatorDot(nextPos, 0, 255, 255);
                outOfSight = true;
                outOfSightLoc = nextPos;
                // // Profiler.end("bugnav " + 6 + " " + isMaster);
                // indicate("bugnav " + 6 + " " + isMaster);
                return false;
            }
            if(isPassable) {
                pos = nextPos;
                topDir = tryDir;
                stackSize += 6;
                if(stackSize == 0) {
                    emptyStackTurns++;
                } else {
                    emptyStackTurns = 0;
                }
                appendCurrentPosition();
                // // Profiler.end("bugnav " + 6 + " " + isMaster);
                // indicate("bugnav " + 6 + " " + isMaster);
                return true;
            }
        }
        tryDir = tryDir.rotateLeft();
        // println("no valid move found from " + pos);
        return false;
    }

        private boolean tryDirsRight() throws GameActionException {
        var tryDir = topDir.rotateLeft().rotateLeft();
        if(stackSize + -2 >= 0
           // only try -2 for diagonals
           && Math.abs(topDir.dx + topDir.dy) != 1
           )
        {
            // System.out.println("T " + tryDir);
            var nextPos = pos.add(tryDir);
            if(!rc.onTheMap(nextPos)) {
                 if(isMaster) {
                    // avoid following the edge of the map (TODO: 1-thick borders)
                    // indicate("bugnav edge: leftTurn = " + leftTurn + " bottomDir = " + bottomDir + " tryDir = " + tryDir + " turns = " + -2);
                    if(vis.contains(nextPos)) {
                        // indicate("gave up at edge");
                        // System.out.println("gave up at edge");
                        givenUp = true;
                        return false;
                    }
                    vis.add(nextPos);
                    leftTurn = !leftTurn;
                    clearPath();
                    // don't guess turn direction, we know that one direction is bad already
                    emptyStackTurns = -1000;
                    // start again
                    return tryDirsLeft();
                } else {
                    hitEdge = true;
                }
                // // Profiler.end("bugnav " + -2 + " " + isMaster);
                // indicate("bugnav " + -2 + " " + isMaster);
                return false;
            }
            var isPassable = isPassable(nextPos);
            if(isPassable && chebyshevDistance(myLoc, nextPos) > 5 || possiblyPassable(nextPos) && !isPassable) {
                // rc.setIndicatorDot(nextPos, 0, 255, 255);
                outOfSight = true;
                outOfSightLoc = nextPos;
                // // Profiler.end("bugnav " + -2 + " " + isMaster);
                // indicate("bugnav " + -2 + " " + isMaster);
                return false;
            }
            if(isPassable) {
                pos = nextPos;
                topDir = tryDir;
                stackSize += -2;
                if(stackSize == 0) {
                    emptyStackTurns++;
                } else {
                    emptyStackTurns = 0;
                }
                appendCurrentPosition();
                // // Profiler.end("bugnav " + -2 + " " + isMaster);
                // indicate("bugnav " + -2 + " " + isMaster);
                return true;
            }
        }
        tryDir = tryDir.rotateRight();
        if(stackSize + -1 >= 0
           // only try -2 for diagonals
           )
        {
            // System.out.println("T " + tryDir);
            var nextPos = pos.add(tryDir);
            if(!rc.onTheMap(nextPos)) {
                 if(isMaster) {
                    // avoid following the edge of the map (TODO: 1-thick borders)
                    // indicate("bugnav edge: leftTurn = " + leftTurn + " bottomDir = " + bottomDir + " tryDir = " + tryDir + " turns = " + -1);
                    if(vis.contains(nextPos)) {
                        // indicate("gave up at edge");
                        // System.out.println("gave up at edge");
                        givenUp = true;
                        return false;
                    }
                    vis.add(nextPos);
                    leftTurn = !leftTurn;
                    clearPath();
                    // don't guess turn direction, we know that one direction is bad already
                    emptyStackTurns = -1000;
                    // start again
                    return tryDirsLeft();
                } else {
                    hitEdge = true;
                }
                // // Profiler.end("bugnav " + -1 + " " + isMaster);
                // indicate("bugnav " + -1 + " " + isMaster);
                return false;
            }
            var isPassable = isPassable(nextPos);
            if(isPassable && chebyshevDistance(myLoc, nextPos) > 5 || possiblyPassable(nextPos) && !isPassable) {
                // rc.setIndicatorDot(nextPos, 0, 255, 255);
                outOfSight = true;
                outOfSightLoc = nextPos;
                // // Profiler.end("bugnav " + -1 + " " + isMaster);
                // indicate("bugnav " + -1 + " " + isMaster);
                return false;
            }
            if(isPassable) {
                pos = nextPos;
                topDir = tryDir;
                stackSize += -1;
                if(stackSize == 0) {
                    emptyStackTurns++;
                } else {
                    emptyStackTurns = 0;
                }
                appendCurrentPosition();
                // // Profiler.end("bugnav " + -1 + " " + isMaster);
                // indicate("bugnav " + -1 + " " + isMaster);
                return true;
            }
        }
        tryDir = tryDir.rotateRight();
        {
            // System.out.println("T " + tryDir);
            var nextPos = pos.add(tryDir);
            if(!rc.onTheMap(nextPos)) {
                 if(isMaster) {
                    // avoid following the edge of the map (TODO: 1-thick borders)
                    // indicate("bugnav edge: leftTurn = " + leftTurn + " bottomDir = " + bottomDir + " tryDir = " + tryDir + " turns = " + 0);
                    if(vis.contains(nextPos)) {
                        // indicate("gave up at edge");
                        // System.out.println("gave up at edge");
                        givenUp = true;
                        return false;
                    }
                    vis.add(nextPos);
                    leftTurn = !leftTurn;
                    clearPath();
                    // don't guess turn direction, we know that one direction is bad already
                    emptyStackTurns = -1000;
                    // start again
                    return tryDirsLeft();
                } else {
                    hitEdge = true;
                }
                // // Profiler.end("bugnav " + 0 + " " + isMaster);
                // indicate("bugnav " + 0 + " " + isMaster);
                return false;
            }
            var isPassable = isPassable(nextPos);
            if(isPassable && chebyshevDistance(myLoc, nextPos) > 5 || possiblyPassable(nextPos) && !isPassable) {
                // rc.setIndicatorDot(nextPos, 0, 255, 255);
                outOfSight = true;
                outOfSightLoc = nextPos;
                // // Profiler.end("bugnav " + 0 + " " + isMaster);
                // indicate("bugnav " + 0 + " " + isMaster);
                return false;
            }
            if(isPassable) {
                pos = nextPos;
                topDir = tryDir;
                if(stackSize == 0) {
                    emptyStackTurns++;
                } else {
                    emptyStackTurns = 0;
                }
                appendCurrentPosition();
                // // Profiler.end("bugnav " + 0 + " " + isMaster);
                // indicate("bugnav " + 0 + " " + isMaster);
                return true;
            }
        }
        tryDir = tryDir.rotateRight();
        {
            // System.out.println("T " + tryDir);
            var nextPos = pos.add(tryDir);
            if(!rc.onTheMap(nextPos)) {
                 if(isMaster) {
                    // avoid following the edge of the map (TODO: 1-thick borders)
                    // indicate("bugnav edge: leftTurn = " + leftTurn + " bottomDir = " + bottomDir + " tryDir = " + tryDir + " turns = " + 1);
                    if(vis.contains(nextPos)) {
                        // indicate("gave up at edge");
                        // System.out.println("gave up at edge");
                        givenUp = true;
                        return false;
                    }
                    vis.add(nextPos);
                    leftTurn = !leftTurn;
                    clearPath();
                    // don't guess turn direction, we know that one direction is bad already
                    emptyStackTurns = -1000;
                    // start again
                    return tryDirsLeft();
                } else {
                    hitEdge = true;
                }
                // // Profiler.end("bugnav " + 1 + " " + isMaster);
                // indicate("bugnav " + 1 + " " + isMaster);
                return false;
            }
            var isPassable = isPassable(nextPos);
            if(isPassable && chebyshevDistance(myLoc, nextPos) > 5 || possiblyPassable(nextPos) && !isPassable) {
                // rc.setIndicatorDot(nextPos, 0, 255, 255);
                outOfSight = true;
                outOfSightLoc = nextPos;
                // // Profiler.end("bugnav " + 1 + " " + isMaster);
                // indicate("bugnav " + 1 + " " + isMaster);
                return false;
            }
            if(isPassable) {
                pos = nextPos;
                topDir = tryDir;
                stackSize += 1;
                if(stackSize == 0) {
                    emptyStackTurns++;
                } else {
                    emptyStackTurns = 0;
                }
                appendCurrentPosition();
                // // Profiler.end("bugnav " + 1 + " " + isMaster);
                // indicate("bugnav " + 1 + " " + isMaster);
                return true;
            }
        }
        tryDir = tryDir.rotateRight();
        {
            // System.out.println("T " + tryDir);
            var nextPos = pos.add(tryDir);
            if(!rc.onTheMap(nextPos)) {
                 if(isMaster) {
                    // avoid following the edge of the map (TODO: 1-thick borders)
                    // indicate("bugnav edge: leftTurn = " + leftTurn + " bottomDir = " + bottomDir + " tryDir = " + tryDir + " turns = " + 2);
                    if(vis.contains(nextPos)) {
                        // indicate("gave up at edge");
                        // System.out.println("gave up at edge");
                        givenUp = true;
                        return false;
                    }
                    vis.add(nextPos);
                    leftTurn = !leftTurn;
                    clearPath();
                    // don't guess turn direction, we know that one direction is bad already
                    emptyStackTurns = -1000;
                    // start again
                    return tryDirsLeft();
                } else {
                    hitEdge = true;
                }
                // // Profiler.end("bugnav " + 2 + " " + isMaster);
                // indicate("bugnav " + 2 + " " + isMaster);
                return false;
            }
            var isPassable = isPassable(nextPos);
            if(isPassable && chebyshevDistance(myLoc, nextPos) > 5 || possiblyPassable(nextPos) && !isPassable) {
                // rc.setIndicatorDot(nextPos, 0, 255, 255);
                outOfSight = true;
                outOfSightLoc = nextPos;
                // // Profiler.end("bugnav " + 2 + " " + isMaster);
                // indicate("bugnav " + 2 + " " + isMaster);
                return false;
            }
            if(isPassable) {
                pos = nextPos;
                topDir = tryDir;
                stackSize += 2;
                if(stackSize == 0) {
                    emptyStackTurns++;
                } else {
                    emptyStackTurns = 0;
                }
                appendCurrentPosition();
                // // Profiler.end("bugnav " + 2 + " " + isMaster);
                // indicate("bugnav " + 2 + " " + isMaster);
                return true;
            }
        }
        tryDir = tryDir.rotateRight();
        {
            // System.out.println("T " + tryDir);
            var nextPos = pos.add(tryDir);
            if(!rc.onTheMap(nextPos)) {
                 if(isMaster) {
                    // avoid following the edge of the map (TODO: 1-thick borders)
                    // indicate("bugnav edge: leftTurn = " + leftTurn + " bottomDir = " + bottomDir + " tryDir = " + tryDir + " turns = " + 3);
                    if(vis.contains(nextPos)) {
                        // indicate("gave up at edge");
                        // System.out.println("gave up at edge");
                        givenUp = true;
                        return false;
                    }
                    vis.add(nextPos);
                    leftTurn = !leftTurn;
                    clearPath();
                    // don't guess turn direction, we know that one direction is bad already
                    emptyStackTurns = -1000;
                    // start again
                    return tryDirsLeft();
                } else {
                    hitEdge = true;
                }
                // // Profiler.end("bugnav " + 3 + " " + isMaster);
                // indicate("bugnav " + 3 + " " + isMaster);
                return false;
            }
            var isPassable = isPassable(nextPos);
            if(isPassable && chebyshevDistance(myLoc, nextPos) > 5 || possiblyPassable(nextPos) && !isPassable) {
                // rc.setIndicatorDot(nextPos, 0, 255, 255);
                outOfSight = true;
                outOfSightLoc = nextPos;
                // // Profiler.end("bugnav " + 3 + " " + isMaster);
                // indicate("bugnav " + 3 + " " + isMaster);
                return false;
            }
            if(isPassable) {
                pos = nextPos;
                topDir = tryDir;
                stackSize += 3;
                if(stackSize == 0) {
                    emptyStackTurns++;
                } else {
                    emptyStackTurns = 0;
                }
                appendCurrentPosition();
                // // Profiler.end("bugnav " + 3 + " " + isMaster);
                // indicate("bugnav " + 3 + " " + isMaster);
                return true;
            }
        }
        tryDir = tryDir.rotateRight();
        {
            // System.out.println("T " + tryDir);
            var nextPos = pos.add(tryDir);
            if(!rc.onTheMap(nextPos)) {
                 if(isMaster) {
                    // avoid following the edge of the map (TODO: 1-thick borders)
                    // indicate("bugnav edge: leftTurn = " + leftTurn + " bottomDir = " + bottomDir + " tryDir = " + tryDir + " turns = " + 4);
                    if(vis.contains(nextPos)) {
                        // indicate("gave up at edge");
                        // System.out.println("gave up at edge");
                        givenUp = true;
                        return false;
                    }
                    vis.add(nextPos);
                    leftTurn = !leftTurn;
                    clearPath();
                    // don't guess turn direction, we know that one direction is bad already
                    emptyStackTurns = -1000;
                    // start again
                    return tryDirsLeft();
                } else {
                    hitEdge = true;
                }
                // // Profiler.end("bugnav " + 4 + " " + isMaster);
                // indicate("bugnav " + 4 + " " + isMaster);
                return false;
            }
            var isPassable = isPassable(nextPos);
            if(isPassable && chebyshevDistance(myLoc, nextPos) > 5 || possiblyPassable(nextPos) && !isPassable) {
                // rc.setIndicatorDot(nextPos, 0, 255, 255);
                outOfSight = true;
                outOfSightLoc = nextPos;
                // // Profiler.end("bugnav " + 4 + " " + isMaster);
                // indicate("bugnav " + 4 + " " + isMaster);
                return false;
            }
            if(isPassable) {
                pos = nextPos;
                topDir = tryDir;
                stackSize += 4;
                if(stackSize == 0) {
                    emptyStackTurns++;
                } else {
                    emptyStackTurns = 0;
                }
                appendCurrentPosition();
                // // Profiler.end("bugnav " + 4 + " " + isMaster);
                // indicate("bugnav " + 4 + " " + isMaster);
                return true;
            }
        }
        tryDir = tryDir.rotateRight();
        {
            // System.out.println("T " + tryDir);
            var nextPos = pos.add(tryDir);
            if(!rc.onTheMap(nextPos)) {
                 if(isMaster) {
                    // avoid following the edge of the map (TODO: 1-thick borders)
                    // indicate("bugnav edge: leftTurn = " + leftTurn + " bottomDir = " + bottomDir + " tryDir = " + tryDir + " turns = " + 5);
                    if(vis.contains(nextPos)) {
                        // indicate("gave up at edge");
                        // System.out.println("gave up at edge");
                        givenUp = true;
                        return false;
                    }
                    vis.add(nextPos);
                    leftTurn = !leftTurn;
                    clearPath();
                    // don't guess turn direction, we know that one direction is bad already
                    emptyStackTurns = -1000;
                    // start again
                    return tryDirsLeft();
                } else {
                    hitEdge = true;
                }
                // // Profiler.end("bugnav " + 5 + " " + isMaster);
                // indicate("bugnav " + 5 + " " + isMaster);
                return false;
            }
            var isPassable = isPassable(nextPos);
            if(isPassable && chebyshevDistance(myLoc, nextPos) > 5 || possiblyPassable(nextPos) && !isPassable) {
                // rc.setIndicatorDot(nextPos, 0, 255, 255);
                outOfSight = true;
                outOfSightLoc = nextPos;
                // // Profiler.end("bugnav " + 5 + " " + isMaster);
                // indicate("bugnav " + 5 + " " + isMaster);
                return false;
            }
            if(isPassable) {
                pos = nextPos;
                topDir = tryDir;
                stackSize += 5;
                if(stackSize == 0) {
                    emptyStackTurns++;
                } else {
                    emptyStackTurns = 0;
                }
                appendCurrentPosition();
                // // Profiler.end("bugnav " + 5 + " " + isMaster);
                // indicate("bugnav " + 5 + " " + isMaster);
                return true;
            }
        }
        tryDir = tryDir.rotateRight();
        {
            // System.out.println("T " + tryDir);
            var nextPos = pos.add(tryDir);
            if(!rc.onTheMap(nextPos)) {
                 if(isMaster) {
                    // avoid following the edge of the map (TODO: 1-thick borders)
                    // indicate("bugnav edge: leftTurn = " + leftTurn + " bottomDir = " + bottomDir + " tryDir = " + tryDir + " turns = " + 6);
                    if(vis.contains(nextPos)) {
                        // indicate("gave up at edge");
                        // System.out.println("gave up at edge");
                        givenUp = true;
                        return false;
                    }
                    vis.add(nextPos);
                    leftTurn = !leftTurn;
                    clearPath();
                    // don't guess turn direction, we know that one direction is bad already
                    emptyStackTurns = -1000;
                    // start again
                    return tryDirsLeft();
                } else {
                    hitEdge = true;
                }
                // // Profiler.end("bugnav " + 6 + " " + isMaster);
                // indicate("bugnav " + 6 + " " + isMaster);
                return false;
            }
            var isPassable = isPassable(nextPos);
            if(isPassable && chebyshevDistance(myLoc, nextPos) > 5 || possiblyPassable(nextPos) && !isPassable) {
                // rc.setIndicatorDot(nextPos, 0, 255, 255);
                outOfSight = true;
                outOfSightLoc = nextPos;
                // // Profiler.end("bugnav " + 6 + " " + isMaster);
                // indicate("bugnav " + 6 + " " + isMaster);
                return false;
            }
            if(isPassable) {
                pos = nextPos;
                topDir = tryDir;
                stackSize += 6;
                if(stackSize == 0) {
                    emptyStackTurns++;
                } else {
                    emptyStackTurns = 0;
                }
                appendCurrentPosition();
                // // Profiler.end("bugnav " + 6 + " " + isMaster);
                // indicate("bugnav " + 6 + " " + isMaster);
                return true;
            }
        }
        tryDir = tryDir.rotateRight();
        // println("no valid move found from " + pos);
        return false;
    }

}

public class Pathfinder {
    public static void init() {
        if(time <= 2) {
            Config.dirtAllowed = true;
        }
    }
    static BugnavInstance bug;
    static int lastDist = 0;

    public static  MapLocation getBugPos() {
        return bug.pos;
    }

    public static boolean givenUp() {
        return bug != null && bug.givenUp;
    }

    static MapLocation lastBugPos, lastLoc, secondLastBugPos, secondLastLoc;
    static int reps = 0;

    public static void reset() {
        bug = null;
    }

    public static Direction getMove(MapLocation target) throws GameActionException {
        // indicate("lastDist " + lastDist);
        // TODO: better handling when target changes
        if(bug == null || !target.equals(bug.target)) {
            if(bug != null && target.isAdjacentTo(bug.target)) {
                // indicate("target changed but adjacent, keep going");
                bug.target = target;
            } else {
                // indicate("RETARGET");
                lastBugPos = lastLoc = secondLastBugPos = secondLastLoc = new MapLocation(-1, -1);
                lastDist = 0;
                bug = new BugnavInstance(true, target);
            }
        }
        // int shade = 255;
        // // Profiler.begin();
        if(!isRatKing && (lastDist < 4 || RobotTracker.allySet.contains(bug.pos)) || RobotTracker.kingSet.contains(bug.pos)) {
            bugLoop: {
                // rc.setIndicatorDot(bug.pos, 0, Math.max(0, shade -= 30), 0);
                // // Profiler.begin();
                if(!bug.advance() || isRatKing || Clock.getBytecodesLeft() < 6500) break bugLoop;
                lastDist++;
                // // Profiler.end("bug");
                // rc.setIndicatorDot(bug.pos, 0, Math.max(0, shade -= 30), 0);
                // // Profiler.begin();
                if(!bug.advance() || isRatKing || Clock.getBytecodesLeft() < 6500) break bugLoop;
                lastDist++;
                // // Profiler.end("bug");
                // rc.setIndicatorDot(bug.pos, 0, Math.max(0, shade -= 30), 0);
                // // Profiler.begin();
                if(!bug.advance() || isRatKing || Clock.getBytecodesLeft() < 6500) break bugLoop;
                lastDist++;
                // // Profiler.end("bug");
                // rc.setIndicatorDot(bug.pos, 0, Math.max(0, shade -= 30), 0);
                // // Profiler.begin();
                if(!bug.advance() || isRatKing || Clock.getBytecodesLeft() < 6500) break bugLoop;
                lastDist++;
                // // Profiler.end("bug");
                // rc.setIndicatorDot(bug.pos, 0, Math.max(0, shade -= 30), 0);
                // // Profiler.begin();
                if(!bug.advance() || isRatKing || Clock.getBytecodesLeft() < 6500) break bugLoop;
                lastDist++;
                // // Profiler.end("bug");
            }
        }
        // // Profiler.end("bug");

        // deal with the edge case of moving immediately out of sight
        if(myLoc.equals(bug.pos)) {
            lastDist = 0;
            var loc = bug.outOfSightLoc;
            bug.outOfSightLoc = null;
            if(loc == null || loc.equals(myLoc)) return Direction.CENTER;
            var dir = myLoc.directionTo(loc);
            turn(dir);
            if(CellTracker.isPassable(loc) && (!CellTracker.isDirt(loc) || Config.allowDirt())) {
                return dir;
            }
        }

        if(!rc.isMovementReady()) return Direction.CENTER;

        if(bug.pos == secondLastBugPos && myLoc.equals(secondLastLoc)) {
            reps++;
            if(reps >= 5) {
                // indicate("stuck, resetting bug");
                // println("stuck, resetting bug");
                bug.clearPath();
                bug.advance();
                lastBugPos = lastLoc = new MapLocation(-1, -1);
                lastDist = 0;
                reps = 0;
            }
        } else {
            reps = 0;
        }
        if(!lastLoc.equals(myLoc) || !lastBugPos.equals(bug.pos)) {
            secondLastBugPos = lastBugPos;
            secondLastLoc = lastLoc;
            lastBugPos = bug.pos;
            lastLoc = myLoc;
        }
        // rc.setIndicatorDot(bug.pos, 255, 0, 0);

        // no bfs needed
        {
            var dir = myLoc.directionTo(bug.pos);
            if(myLoc.add(dir).equals(bug.pos)) {
                if(dir != Direction.CENTER) {
                    // indicate("adjacent");
                    return dir;
                }
                lastDist = 0;
                return Direction.CENTER;
            }
        }

        return doBugnavBfs(target);
    }



    private static int catPenalty(MapLocation loc) {
        // prefer avoiding cat vision range; weight visible cats more heavily
        int score = 0;
        for(int i = 0; i < 4; i++) {
            int penalty, angle;
            if(i == time % 4) {
                penalty = 3000;
                angle = 180;
            } else {
                penalty = 1300;
                angle = 360;
            }
            for(int j = RobotTracker.catHistNum[i] - 1; j >= 0; j--) {
                var cat = RobotTracker.catHist[i][j];
                if(cat == null) continue;
                if(cat.location.isWithinDistanceSquared(loc, UnitType.CAT.visionConeRadiusSquared, cat.direction, angle, true)) {
                    score += penalty;
                }
            }
        }
        return score;
    }

    private static long localPassableExceptAllies0, localPassableExceptAllies1;
    private static long nearCat0, nearCat1;
    private static void prepareBfsMasksBugnav() throws GameActionException {
        // Profiler.begin();
        CellTracker.computeLocalBfsMasks();
        localPassableExceptAllies0 = CellTracker.localPassable0;
        localPassableExceptAllies1 = CellTracker.localPassable1;
        // minkowski sum the walls for the rat king
        if(RatKing.is5x5) {
                    if(isRatKing) {
            localPassableExceptAllies0 &= (localPassableExceptAllies0 & 0xfbff7feffdffbffl) << 1;
            localPassableExceptAllies1 &= ((localPassableExceptAllies1 & 0x7feffdffbff7fefl) << 1) | 1;
            localPassableExceptAllies0 &= ((localPassableExceptAllies0 & 0x1f7feffdffbff7fel) >> 1) | 0x1000000000000000l;
            localPassableExceptAllies1 &= (localPassableExceptAllies1 & 0xffdffbff7feffdel) >> 1;

            localPassableExceptAllies1 &= ((localPassableExceptAllies1 & 0x1ffffffffffffl) << 11) | (localPassableExceptAllies0 >> 50);
            localPassableExceptAllies0 &= (localPassableExceptAllies0 & 0x3ffffffffffffl) << 11;
            localPassableExceptAllies0 &= ((localPassableExceptAllies0 >> 11) | ((localPassableExceptAllies1 & 0x7ffl) << 50));
            localPassableExceptAllies1 &= (localPassableExceptAllies1 >> 11);
        }

        }
        // can't dig dirt if we don't have enough cheese
        if(rc.getAllCheese() < GameConstants.DIG_DIRT_CHEESE_COST) {
            localPassableExceptAllies0 &= ~CellTracker.localDirt0;
            localPassableExceptAllies1 &= ~CellTracker.localDirt1;
        }
        // IF TLE: unroll this (and the one in multisourceBfs)
        for(int i = RobotTracker.allyCount - 1; i >= 0; i--) {
            if(isRatKing && RobotTracker.allies[i].isAdjacentTo(myLoc)) {
                continue;
            }
                {
        var pos = RobotTracker.allies[i];
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

        }
        // IF TLE: definitely optimise this (and the one in multisourceBfs)
        for(int i = 0; i < 5; i++) {
            var kingLoc = GlobalArray.getRatKing(i);
            if(kingLoc == null || kingLoc.equals(myLoc)) continue;
                {
        var pos = kingLoc.add(Direction.NORTH);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                {
        var pos = kingLoc.add(Direction.NORTHEAST);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                {
        var pos = kingLoc.add(Direction.EAST);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                {
        var pos = kingLoc.add(Direction.SOUTHEAST);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                {
        var pos = kingLoc.add(Direction.SOUTH);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                {
        var pos = kingLoc.add(Direction.SOUTHWEST);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                {
        var pos = kingLoc.add(Direction.WEST);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                {
        var pos = kingLoc.add(Direction.NORTHWEST);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                {
        var pos = kingLoc.add(Direction.CENTER);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

        }
        nearCat0 = nearCat1 = 0;
        for(int i = 0; i < 4; i++) {
            catLoop:
            for(int j = RobotTracker.catHistNum[i] - 1; j >= 0; j--) {
                var cat = RobotTracker.catHist[i][j];
                if(cat == null) continue;
                var catLoc = cat.location;
                for(int dx = 0; dx <= 1; dx++) {
                    for(int dy = 0; dy <= 1; dy++) {
                        var senseLoc = catLoc.translate(dx, dy);
                        if(canSense(myLoc, senseLoc, myDir)) {
                            var robot = rc.senseRobotAtLocation(senseLoc);
                            if(robot != null && robot.location.equals(catLoc) && robot.getType() == UnitType.CAT) {
                                    {
        var pos = catLoc.translate(0, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                    {
        var pos = catLoc.translate(0, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                    {
        var pos = catLoc.translate(1, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                    {
        var pos = catLoc.translate(1, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                switch(robot.direction.ordinal()) {
                                    case 0 -> {
                                            {
        var pos = catLoc.translate(-1, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 0))) rc.setIndicatorDot(catLoc.translate(-1, 0), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 1))) rc.setIndicatorDot(catLoc.translate(-1, 1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 2))) rc.setIndicatorDot(catLoc.translate(-1, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(0, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, 2))) rc.setIndicatorDot(catLoc.translate(0, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, 2))) rc.setIndicatorDot(catLoc.translate(1, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 2))) rc.setIndicatorDot(catLoc.translate(2, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 1))) rc.setIndicatorDot(catLoc.translate(2, 1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 0))) rc.setIndicatorDot(catLoc.translate(2, 0), 100, 0, 255);
                                    }
                                    case 1 -> {
                                            {
        var pos = catLoc.translate(-1, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 1))) rc.setIndicatorDot(catLoc.translate(-1, 1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 2))) rc.setIndicatorDot(catLoc.translate(-1, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(0, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, 2))) rc.setIndicatorDot(catLoc.translate(0, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, 2))) rc.setIndicatorDot(catLoc.translate(1, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 2))) rc.setIndicatorDot(catLoc.translate(2, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 1))) rc.setIndicatorDot(catLoc.translate(2, 1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 0))) rc.setIndicatorDot(catLoc.translate(2, 0), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, -1))) rc.setIndicatorDot(catLoc.translate(2, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, -1))) rc.setIndicatorDot(catLoc.translate(1, -1), 100, 0, 255);
                                    }
                                    case 2 -> {
                                            {
        var pos = catLoc.translate(0, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, 2))) rc.setIndicatorDot(catLoc.translate(0, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, 2))) rc.setIndicatorDot(catLoc.translate(1, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 2))) rc.setIndicatorDot(catLoc.translate(2, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 1))) rc.setIndicatorDot(catLoc.translate(2, 1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 0))) rc.setIndicatorDot(catLoc.translate(2, 0), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, -1))) rc.setIndicatorDot(catLoc.translate(2, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, -1))) rc.setIndicatorDot(catLoc.translate(1, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(0, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, -1))) rc.setIndicatorDot(catLoc.translate(0, -1), 100, 0, 255);
                                    }
                                    case 3 -> {
                                            {
        var pos = catLoc.translate(1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, 2))) rc.setIndicatorDot(catLoc.translate(1, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 2))) rc.setIndicatorDot(catLoc.translate(2, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 1))) rc.setIndicatorDot(catLoc.translate(2, 1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 0))) rc.setIndicatorDot(catLoc.translate(2, 0), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, -1))) rc.setIndicatorDot(catLoc.translate(2, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, -1))) rc.setIndicatorDot(catLoc.translate(1, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(0, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, -1))) rc.setIndicatorDot(catLoc.translate(0, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, -1))) rc.setIndicatorDot(catLoc.translate(-1, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 0))) rc.setIndicatorDot(catLoc.translate(-1, 0), 100, 0, 255);
                                    }
                                    case 4 -> {
                                            {
        var pos = catLoc.translate(2, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 1))) rc.setIndicatorDot(catLoc.translate(2, 1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 0))) rc.setIndicatorDot(catLoc.translate(2, 0), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, -1))) rc.setIndicatorDot(catLoc.translate(2, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, -1))) rc.setIndicatorDot(catLoc.translate(1, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(0, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, -1))) rc.setIndicatorDot(catLoc.translate(0, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, -1))) rc.setIndicatorDot(catLoc.translate(-1, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 0))) rc.setIndicatorDot(catLoc.translate(-1, 0), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 1))) rc.setIndicatorDot(catLoc.translate(-1, 1), 100, 0, 255);
                                    }
                                    case 5 -> {
                                            {
        var pos = catLoc.translate(2, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 0))) rc.setIndicatorDot(catLoc.translate(2, 0), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, -1))) rc.setIndicatorDot(catLoc.translate(2, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, -1))) rc.setIndicatorDot(catLoc.translate(1, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(0, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, -1))) rc.setIndicatorDot(catLoc.translate(0, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, -1))) rc.setIndicatorDot(catLoc.translate(-1, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 0))) rc.setIndicatorDot(catLoc.translate(-1, 0), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 1))) rc.setIndicatorDot(catLoc.translate(-1, 1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 2))) rc.setIndicatorDot(catLoc.translate(-1, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(0, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, 2))) rc.setIndicatorDot(catLoc.translate(0, 2), 100, 0, 255);
                                    }
                                    case 6 -> {
                                            {
        var pos = catLoc.translate(1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, -1))) rc.setIndicatorDot(catLoc.translate(1, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(0, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, -1))) rc.setIndicatorDot(catLoc.translate(0, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, -1))) rc.setIndicatorDot(catLoc.translate(-1, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 0))) rc.setIndicatorDot(catLoc.translate(-1, 0), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 1))) rc.setIndicatorDot(catLoc.translate(-1, 1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 2))) rc.setIndicatorDot(catLoc.translate(-1, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(0, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, 2))) rc.setIndicatorDot(catLoc.translate(0, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, 2))) rc.setIndicatorDot(catLoc.translate(1, 2), 100, 0, 255);
                                    }
                                    case 7 -> {
                                            {
        var pos = catLoc.translate(0, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, -1))) rc.setIndicatorDot(catLoc.translate(0, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, -1))) rc.setIndicatorDot(catLoc.translate(-1, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 0))) rc.setIndicatorDot(catLoc.translate(-1, 0), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 1))) rc.setIndicatorDot(catLoc.translate(-1, 1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 2))) rc.setIndicatorDot(catLoc.translate(-1, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(0, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, 2))) rc.setIndicatorDot(catLoc.translate(0, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, 2))) rc.setIndicatorDot(catLoc.translate(1, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 2))) rc.setIndicatorDot(catLoc.translate(2, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 1))) rc.setIndicatorDot(catLoc.translate(2, 1), 100, 0, 255);
                                    }
                                }
                            } else {
                                // invalidate this entry
                                RobotTracker.catHist[i][j] = null;
                            }
                            continue catLoop;
                        }
                    }
                }
                // we can't sense this cat, use historical info only
                    {
        var pos = catLoc.translate(0, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(0, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(1, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(1, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(-1, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(-1, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(-1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(0, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(2, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(2, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(2, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(2, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(0, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(-1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(-1, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(-1, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(-1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(0, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(2, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(2, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(2, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(2, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(0, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(-1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

            }
        }
        // minkowski sum the walls for the rat king again (5x5 hitbox for walls, 3x3 hitbox for allies and dirt)
                if(isRatKing) {
            localPassableExceptAllies0 &= (localPassableExceptAllies0 & 0xfbff7feffdffbffl) << 1;
            localPassableExceptAllies1 &= ((localPassableExceptAllies1 & 0x7feffdffbff7fefl) << 1) | 1;
            localPassableExceptAllies0 &= ((localPassableExceptAllies0 & 0x1f7feffdffbff7fel) >> 1) | 0x1000000000000000l;
            localPassableExceptAllies1 &= (localPassableExceptAllies1 & 0xffdffbff7feffdel) >> 1;

            localPassableExceptAllies1 &= ((localPassableExceptAllies1 & 0x1ffffffffffffl) << 11) | (localPassableExceptAllies0 >> 50);
            localPassableExceptAllies0 &= (localPassableExceptAllies0 & 0x3ffffffffffffl) << 11;
            localPassableExceptAllies0 &= ((localPassableExceptAllies0 >> 11) | ((localPassableExceptAllies1 & 0x7ffl) << 50));
            localPassableExceptAllies1 &= (localPassableExceptAllies1 >> 11);
        }

        localPassableExceptAllies0 &= ~(1l << 60);
    }

    private static Direction doBugnavBfs(MapLocation target) throws GameActionException
    {
        prepareBfsMasksBugnav();
        // d{i}0 + d{i}1 is the mask of reachable within i steps
        long d00 = 0, d01 = 0;
        long d10 = 0, d11 = 0;

        boolean lastIteration = false;
        bfsLoop: {
            if(lastIteration) break bfsLoop;
            {
                var bugPos = bug.getRecentPos(0 / 2);
                if(bugPos.equals(myLoc)) {
                    // we are completely stuck
                    break bfsLoop;
                } else {
                        {
        var pos = bugPos;
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                d00 |= (1l << idx);
            } else {
                d01 |= (1l << (idx - 61));
            }
        }
    }

                }
                if(bugPos.isAdjacentTo(myLoc) && isRatKing) {
                    lastIteration = true;
                }
            }
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d00, d01);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d00 |= (d00 & 0xfbff7feffdffbffl) << 1;
    d01 |= (d01 & 0x7feffdffbff7fefl) << 1;
    d00 |= (d00 & 0x1f7feffdffbff7fel) >> 1;
    d01 |= (d01 & 0xffdffbff7feffdel) >> 1;

    d01 |= ((d01 & 0x1ffffffffffffl) << 11) | (d00 >> 50);
    d00 |= (d00 & 0x3ffffffffffffl) << 11;
    d00 |= (d00 >> 11) | ((d01 & 0x7ffl) << 50);
    d01 |= d01 >> 11;
    d00 &= localPassableExceptAllies0;
    d01 &= localPassableExceptAllies1;

            long d20 = 0l;
            long d21 = 0l;
            d20 = d00;
            d21 = d01;
            d00 &= ~CellTracker.localDirt0;
            d01 &= ~CellTracker.localDirt1;
            d10 |= d00;
            d11 |= d01;
            // // Profiler.end("bfs iteration");
            if(lastIteration) break bfsLoop;
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d10, d11);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d10 & 0x807000000000000l) != 0 || (d11 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d10 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d11 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d11 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d10 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d10 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d10 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d11 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d11 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 1;
                    // indicate("bfs iters: " + 1);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d10 |= (d10 & 0xfbff7feffdffbffl) << 1;
    d11 |= (d11 & 0x7feffdffbff7fefl) << 1;
    d10 |= (d10 & 0x1f7feffdffbff7fel) >> 1;
    d11 |= (d11 & 0xffdffbff7feffdel) >> 1;

    d11 |= ((d11 & 0x1ffffffffffffl) << 11) | (d10 >> 50);
    d10 |= (d10 & 0x3ffffffffffffl) << 11;
    d10 |= (d10 >> 11) | ((d11 & 0x7ffl) << 50);
    d11 |= d11 >> 11;
    d10 &= localPassableExceptAllies0;
    d11 &= localPassableExceptAllies1;

            long d30 = 0l;
            long d31 = 0l;
            d30 = d10;
            d31 = d11;
            d10 &= ~CellTracker.localDirt0;
            d11 &= ~CellTracker.localDirt1;
            d20 |= d10;
            d21 |= d11;
            // // Profiler.end("bfs iteration");
            if(lastIteration) break bfsLoop;
            {
                var bugPos = bug.getRecentPos(2 / 2);
                if(bugPos.equals(myLoc)) {
                    // we are completely stuck
                    break bfsLoop;
                } else {
                        {
        var pos = bugPos;
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                d20 |= (1l << idx);
            } else {
                d21 |= (1l << (idx - 61));
            }
        }
    }

                }
                if(bugPos.isAdjacentTo(myLoc) && isRatKing) {
                    lastIteration = true;
                }
            }
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d20, d21);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d20 & 0x807000000000000l) != 0 || (d21 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d20 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d21 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d21 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d20 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d20 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d20 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d21 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d21 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 2;
                    // indicate("bfs iters: " + 2);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d20 |= (d20 & 0xfbff7feffdffbffl) << 1;
    d21 |= (d21 & 0x7feffdffbff7fefl) << 1;
    d20 |= (d20 & 0x1f7feffdffbff7fel) >> 1;
    d21 |= (d21 & 0xffdffbff7feffdel) >> 1;

    d21 |= ((d21 & 0x1ffffffffffffl) << 11) | (d20 >> 50);
    d20 |= (d20 & 0x3ffffffffffffl) << 11;
    d20 |= (d20 >> 11) | ((d21 & 0x7ffl) << 50);
    d21 |= d21 >> 11;
    d20 &= localPassableExceptAllies0;
    d21 &= localPassableExceptAllies1;

            long d40 = 0l;
            long d41 = 0l;
            d40 = d20;
            d41 = d21;
            d20 &= ~CellTracker.localDirt0;
            d21 &= ~CellTracker.localDirt1;
            d30 |= d20;
            d31 |= d21;
            // // Profiler.end("bfs iteration");
            if(lastIteration) break bfsLoop;
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d30, d31);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d30 & 0x807000000000000l) != 0 || (d31 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d30 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d31 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d31 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d30 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d30 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d30 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d31 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d31 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 3;
                    // indicate("bfs iters: " + 3);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d30 |= (d30 & 0xfbff7feffdffbffl) << 1;
    d31 |= (d31 & 0x7feffdffbff7fefl) << 1;
    d30 |= (d30 & 0x1f7feffdffbff7fel) >> 1;
    d31 |= (d31 & 0xffdffbff7feffdel) >> 1;

    d31 |= ((d31 & 0x1ffffffffffffl) << 11) | (d30 >> 50);
    d30 |= (d30 & 0x3ffffffffffffl) << 11;
    d30 |= (d30 >> 11) | ((d31 & 0x7ffl) << 50);
    d31 |= d31 >> 11;
    d30 &= localPassableExceptAllies0;
    d31 &= localPassableExceptAllies1;

            long d50 = 0l;
            long d51 = 0l;
            d50 = d30;
            d51 = d31;
            d30 &= ~CellTracker.localDirt0;
            d31 &= ~CellTracker.localDirt1;
            d40 |= d30;
            d41 |= d31;
            // // Profiler.end("bfs iteration");
            if(lastIteration) break bfsLoop;
            {
                var bugPos = bug.getRecentPos(4 / 2);
                if(bugPos.equals(myLoc)) {
                    // we are completely stuck
                    break bfsLoop;
                } else {
                        {
        var pos = bugPos;
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                d40 |= (1l << idx);
            } else {
                d41 |= (1l << (idx - 61));
            }
        }
    }

                }
                if(bugPos.isAdjacentTo(myLoc) && isRatKing) {
                    lastIteration = true;
                }
            }
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d40, d41);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d40 & 0x807000000000000l) != 0 || (d41 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d40 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d41 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d41 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d40 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d40 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d40 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d41 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d41 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 4;
                    // indicate("bfs iters: " + 4);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d40 |= (d40 & 0xfbff7feffdffbffl) << 1;
    d41 |= (d41 & 0x7feffdffbff7fefl) << 1;
    d40 |= (d40 & 0x1f7feffdffbff7fel) >> 1;
    d41 |= (d41 & 0xffdffbff7feffdel) >> 1;

    d41 |= ((d41 & 0x1ffffffffffffl) << 11) | (d40 >> 50);
    d40 |= (d40 & 0x3ffffffffffffl) << 11;
    d40 |= (d40 >> 11) | ((d41 & 0x7ffl) << 50);
    d41 |= d41 >> 11;
    d40 &= localPassableExceptAllies0;
    d41 &= localPassableExceptAllies1;

            long d60 = 0l;
            long d61 = 0l;
            d60 = d40;
            d61 = d41;
            d40 &= ~CellTracker.localDirt0;
            d41 &= ~CellTracker.localDirt1;
            d50 |= d40;
            d51 |= d41;
            // // Profiler.end("bfs iteration");
            if(lastIteration) break bfsLoop;
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d50, d51);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d50 & 0x807000000000000l) != 0 || (d51 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d50 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d51 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d51 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d50 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d50 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d50 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d51 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d51 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 5;
                    // indicate("bfs iters: " + 5);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d50 |= (d50 & 0xfbff7feffdffbffl) << 1;
    d51 |= (d51 & 0x7feffdffbff7fefl) << 1;
    d50 |= (d50 & 0x1f7feffdffbff7fel) >> 1;
    d51 |= (d51 & 0xffdffbff7feffdel) >> 1;

    d51 |= ((d51 & 0x1ffffffffffffl) << 11) | (d50 >> 50);
    d50 |= (d50 & 0x3ffffffffffffl) << 11;
    d50 |= (d50 >> 11) | ((d51 & 0x7ffl) << 50);
    d51 |= d51 >> 11;
    d50 &= localPassableExceptAllies0;
    d51 &= localPassableExceptAllies1;

            long d70 = 0l;
            long d71 = 0l;
            d70 = d50;
            d71 = d51;
            d50 &= ~CellTracker.localDirt0;
            d51 &= ~CellTracker.localDirt1;
            d60 |= d50;
            d61 |= d51;
            // // Profiler.end("bfs iteration");
            if(lastIteration) break bfsLoop;
            {
                var bugPos = bug.getRecentPos(6 / 2);
                if(bugPos.equals(myLoc)) {
                    // we are completely stuck
                    break bfsLoop;
                } else {
                        {
        var pos = bugPos;
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                d60 |= (1l << idx);
            } else {
                d61 |= (1l << (idx - 61));
            }
        }
    }

                }
                if(bugPos.isAdjacentTo(myLoc) && isRatKing) {
                    lastIteration = true;
                }
            }
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d60, d61);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d60 & 0x807000000000000l) != 0 || (d61 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d60 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d61 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d61 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d60 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d60 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d60 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d61 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d61 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 6;
                    // indicate("bfs iters: " + 6);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d60 |= (d60 & 0xfbff7feffdffbffl) << 1;
    d61 |= (d61 & 0x7feffdffbff7fefl) << 1;
    d60 |= (d60 & 0x1f7feffdffbff7fel) >> 1;
    d61 |= (d61 & 0xffdffbff7feffdel) >> 1;

    d61 |= ((d61 & 0x1ffffffffffffl) << 11) | (d60 >> 50);
    d60 |= (d60 & 0x3ffffffffffffl) << 11;
    d60 |= (d60 >> 11) | ((d61 & 0x7ffl) << 50);
    d61 |= d61 >> 11;
    d60 &= localPassableExceptAllies0;
    d61 &= localPassableExceptAllies1;

            long d80 = 0l;
            long d81 = 0l;
            d80 = d60;
            d81 = d61;
            d60 &= ~CellTracker.localDirt0;
            d61 &= ~CellTracker.localDirt1;
            d70 |= d60;
            d71 |= d61;
            // // Profiler.end("bfs iteration");
            if(lastIteration) break bfsLoop;
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d70, d71);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d70 & 0x807000000000000l) != 0 || (d71 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d70 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d71 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d71 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d70 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d70 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d70 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d71 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d71 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 7;
                    // indicate("bfs iters: " + 7);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d70 |= (d70 & 0xfbff7feffdffbffl) << 1;
    d71 |= (d71 & 0x7feffdffbff7fefl) << 1;
    d70 |= (d70 & 0x1f7feffdffbff7fel) >> 1;
    d71 |= (d71 & 0xffdffbff7feffdel) >> 1;

    d71 |= ((d71 & 0x1ffffffffffffl) << 11) | (d70 >> 50);
    d70 |= (d70 & 0x3ffffffffffffl) << 11;
    d70 |= (d70 >> 11) | ((d71 & 0x7ffl) << 50);
    d71 |= d71 >> 11;
    d70 &= localPassableExceptAllies0;
    d71 &= localPassableExceptAllies1;

            long d90 = 0l;
            long d91 = 0l;
            d90 = d70;
            d91 = d71;
            d70 &= ~CellTracker.localDirt0;
            d71 &= ~CellTracker.localDirt1;
            d80 |= d70;
            d81 |= d71;
            // // Profiler.end("bfs iteration");
            if(lastIteration) break bfsLoop;
            {
                var bugPos = bug.getRecentPos(8 / 2);
                if(bugPos.equals(myLoc)) {
                    // we are completely stuck
                    break bfsLoop;
                } else {
                        {
        var pos = bugPos;
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                d80 |= (1l << idx);
            } else {
                d81 |= (1l << (idx - 61));
            }
        }
    }

                }
                if(bugPos.isAdjacentTo(myLoc) && isRatKing) {
                    lastIteration = true;
                }
            }
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d80, d81);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d80 & 0x807000000000000l) != 0 || (d81 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d80 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d81 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d81 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d80 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d80 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d80 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d81 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d81 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 8;
                    // indicate("bfs iters: " + 8);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d80 |= (d80 & 0xfbff7feffdffbffl) << 1;
    d81 |= (d81 & 0x7feffdffbff7fefl) << 1;
    d80 |= (d80 & 0x1f7feffdffbff7fel) >> 1;
    d81 |= (d81 & 0xffdffbff7feffdel) >> 1;

    d81 |= ((d81 & 0x1ffffffffffffl) << 11) | (d80 >> 50);
    d80 |= (d80 & 0x3ffffffffffffl) << 11;
    d80 |= (d80 >> 11) | ((d81 & 0x7ffl) << 50);
    d81 |= d81 >> 11;
    d80 &= localPassableExceptAllies0;
    d81 &= localPassableExceptAllies1;

            long d100 = 0l;
            long d101 = 0l;
            d100 = d80;
            d101 = d81;
            d80 &= ~CellTracker.localDirt0;
            d81 &= ~CellTracker.localDirt1;
            d90 |= d80;
            d91 |= d81;
            // // Profiler.end("bfs iteration");
            if(lastIteration) break bfsLoop;
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d90, d91);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d90 & 0x807000000000000l) != 0 || (d91 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d90 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d91 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d91 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d90 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d90 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d90 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d91 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d91 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 9;
                    // indicate("bfs iters: " + 9);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d90 |= (d90 & 0xfbff7feffdffbffl) << 1;
    d91 |= (d91 & 0x7feffdffbff7fefl) << 1;
    d90 |= (d90 & 0x1f7feffdffbff7fel) >> 1;
    d91 |= (d91 & 0xffdffbff7feffdel) >> 1;

    d91 |= ((d91 & 0x1ffffffffffffl) << 11) | (d90 >> 50);
    d90 |= (d90 & 0x3ffffffffffffl) << 11;
    d90 |= (d90 >> 11) | ((d91 & 0x7ffl) << 50);
    d91 |= d91 >> 11;
    d90 &= localPassableExceptAllies0;
    d91 &= localPassableExceptAllies1;

            long d110 = 0l;
            long d111 = 0l;
            d110 = d90;
            d111 = d91;
            d90 &= ~CellTracker.localDirt0;
            d91 &= ~CellTracker.localDirt1;
            d100 |= d90;
            d101 |= d91;
            // // Profiler.end("bfs iteration");
            if(lastIteration) break bfsLoop;
            {
                var bugPos = bug.getRecentPos(10 / 2);
                if(bugPos.equals(myLoc)) {
                    // we are completely stuck
                    break bfsLoop;
                } else {
                        {
        var pos = bugPos;
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                d100 |= (1l << idx);
            } else {
                d101 |= (1l << (idx - 61));
            }
        }
    }

                }
                if(bugPos.isAdjacentTo(myLoc) && isRatKing) {
                    lastIteration = true;
                }
            }
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d100, d101);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d100 & 0x807000000000000l) != 0 || (d101 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d100 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d101 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d101 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d100 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d100 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d100 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d101 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d101 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 10;
                    // indicate("bfs iters: " + 10);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d100 |= (d100 & 0xfbff7feffdffbffl) << 1;
    d101 |= (d101 & 0x7feffdffbff7fefl) << 1;
    d100 |= (d100 & 0x1f7feffdffbff7fel) >> 1;
    d101 |= (d101 & 0xffdffbff7feffdel) >> 1;

    d101 |= ((d101 & 0x1ffffffffffffl) << 11) | (d100 >> 50);
    d100 |= (d100 & 0x3ffffffffffffl) << 11;
    d100 |= (d100 >> 11) | ((d101 & 0x7ffl) << 50);
    d101 |= d101 >> 11;
    d100 &= localPassableExceptAllies0;
    d101 &= localPassableExceptAllies1;

            long d120 = 0l;
            long d121 = 0l;
            d120 = d100;
            d121 = d101;
            d100 &= ~CellTracker.localDirt0;
            d101 &= ~CellTracker.localDirt1;
            d110 |= d100;
            d111 |= d101;
            // // Profiler.end("bfs iteration");
            if(lastIteration) break bfsLoop;
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d110, d111);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d110 & 0x807000000000000l) != 0 || (d111 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d110 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d111 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d111 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d110 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d110 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d110 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d111 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d111 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 11;
                    // indicate("bfs iters: " + 11);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d110 |= (d110 & 0xfbff7feffdffbffl) << 1;
    d111 |= (d111 & 0x7feffdffbff7fefl) << 1;
    d110 |= (d110 & 0x1f7feffdffbff7fel) >> 1;
    d111 |= (d111 & 0xffdffbff7feffdel) >> 1;

    d111 |= ((d111 & 0x1ffffffffffffl) << 11) | (d110 >> 50);
    d110 |= (d110 & 0x3ffffffffffffl) << 11;
    d110 |= (d110 >> 11) | ((d111 & 0x7ffl) << 50);
    d111 |= d111 >> 11;
    d110 &= localPassableExceptAllies0;
    d111 &= localPassableExceptAllies1;

            long d130 = 0l;
            long d131 = 0l;
            d130 = d110;
            d131 = d111;
            d110 &= ~CellTracker.localDirt0;
            d111 &= ~CellTracker.localDirt1;
            d120 |= d110;
            d121 |= d111;
            // // Profiler.end("bfs iteration");
            if(lastIteration) break bfsLoop;
            {
                var bugPos = bug.getRecentPos(12 / 2);
                if(bugPos.equals(myLoc)) {
                    // we are completely stuck
                    break bfsLoop;
                } else {
                        {
        var pos = bugPos;
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                d120 |= (1l << idx);
            } else {
                d121 |= (1l << (idx - 61));
            }
        }
    }

                }
                if(bugPos.isAdjacentTo(myLoc) && isRatKing) {
                    lastIteration = true;
                }
            }
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d120, d121);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d120 & 0x807000000000000l) != 0 || (d121 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d120 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d121 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d121 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d120 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d120 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d120 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d121 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            + nextLoc.distanceSquaredTo(bug.pos)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d121 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 12;
                    // indicate("bfs iters: " + 12);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d120 |= (d120 & 0xfbff7feffdffbffl) << 1;
    d121 |= (d121 & 0x7feffdffbff7fefl) << 1;
    d120 |= (d120 & 0x1f7feffdffbff7fel) >> 1;
    d121 |= (d121 & 0xffdffbff7feffdel) >> 1;

    d121 |= ((d121 & 0x1ffffffffffffl) << 11) | (d120 >> 50);
    d120 |= (d120 & 0x3ffffffffffffl) << 11;
    d120 |= (d120 >> 11) | ((d121 & 0x7ffl) << 50);
    d121 |= d121 >> 11;
    d120 &= localPassableExceptAllies0;
    d121 &= localPassableExceptAllies1;

            long d140 = 0l;
            long d141 = 0l;
            d140 = d120;
            d141 = d121;
            d120 &= ~CellTracker.localDirt0;
            d121 &= ~CellTracker.localDirt1;
            d130 |= d120;
            d131 |= d121;
            // // Profiler.end("bfs iteration");
        }
        // Profiler.end("bfs");

        // println("fallback when bfs can't find path");
        // indicate("fallback when bfs can't find path");

        if(!isRatKing) {
            // move in a random direction (to make way for any allies blocking us)
            var dirs = new Direction[8];
            int dirCount = 0;
            if(rc.canMove(Direction.NORTH)) dirs[dirCount++] = Direction.NORTH;
            if(rc.canMove(Direction.NORTHEAST)) dirs[dirCount++] = Direction.NORTHEAST;
            if(rc.canMove(Direction.EAST)) dirs[dirCount++] = Direction.EAST;
            if(rc.canMove(Direction.SOUTHEAST)) dirs[dirCount++] = Direction.SOUTHEAST;
            if(rc.canMove(Direction.SOUTH)) dirs[dirCount++] = Direction.SOUTH;
            if(rc.canMove(Direction.SOUTHWEST)) dirs[dirCount++] = Direction.SOUTHWEST;
            if(rc.canMove(Direction.WEST)) dirs[dirCount++] = Direction.WEST;
            if(rc.canMove(Direction.NORTHWEST)) dirs[dirCount++] = Direction.NORTHWEST;

            lastDist = 0;
            lastBugPos = lastLoc = secondLastBugPos = secondLastLoc = new MapLocation(-1, -1);
            bug.clearPath();

            if(dirCount > 0) return dirs[Rng.mcg() % dirCount];
        }

        return Direction.CENTER;
    }
    private static void prepareBfsMasksSource() throws GameActionException {
        // Profiler.begin();
        CellTracker.computeLocalBfsMasks();
        localPassableExceptAllies0 = CellTracker.localPassable0;
        localPassableExceptAllies1 = CellTracker.localPassable1;
        // minkowski sum the walls for the rat king
        if(RatKing.is5x5) {
                    if(isRatKing) {
            localPassableExceptAllies0 &= (localPassableExceptAllies0 & 0xfbff7feffdffbffl) << 1;
            localPassableExceptAllies1 &= ((localPassableExceptAllies1 & 0x7feffdffbff7fefl) << 1) | 1;
            localPassableExceptAllies0 &= ((localPassableExceptAllies0 & 0x1f7feffdffbff7fel) >> 1) | 0x1000000000000000l;
            localPassableExceptAllies1 &= (localPassableExceptAllies1 & 0xffdffbff7feffdel) >> 1;

            localPassableExceptAllies1 &= ((localPassableExceptAllies1 & 0x1ffffffffffffl) << 11) | (localPassableExceptAllies0 >> 50);
            localPassableExceptAllies0 &= (localPassableExceptAllies0 & 0x3ffffffffffffl) << 11;
            localPassableExceptAllies0 &= ((localPassableExceptAllies0 >> 11) | ((localPassableExceptAllies1 & 0x7ffl) << 50));
            localPassableExceptAllies1 &= (localPassableExceptAllies1 >> 11);
        }

        }
        // can't dig dirt if we don't have enough cheese
        if(rc.getAllCheese() < GameConstants.DIG_DIRT_CHEESE_COST) {
            localPassableExceptAllies0 &= ~CellTracker.localDirt0;
            localPassableExceptAllies1 &= ~CellTracker.localDirt1;
        }
        // IF TLE: unroll this (and the one in multisourceBfs)
        for(int i = RobotTracker.allyCount - 1; i >= 0; i--) {
            if(isRatKing && RobotTracker.allies[i].isAdjacentTo(myLoc)) {
                continue;
            }
                {
        var pos = RobotTracker.allies[i];
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

        }
        nearCat0 = nearCat1 = 0;
        for(int i = 0; i < 4; i++) {
            catLoop:
            for(int j = RobotTracker.catHistNum[i] - 1; j >= 0; j--) {
                var cat = RobotTracker.catHist[i][j];
                if(cat == null) continue;
                var catLoc = cat.location;
                for(int dx = 0; dx <= 1; dx++) {
                    for(int dy = 0; dy <= 1; dy++) {
                        var senseLoc = catLoc.translate(dx, dy);
                        if(canSense(myLoc, senseLoc, myDir)) {
                            var robot = rc.senseRobotAtLocation(senseLoc);
                            if(robot != null && robot.location.equals(catLoc) && robot.getType() == UnitType.CAT) {
                                    {
        var pos = catLoc.translate(0, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                    {
        var pos = catLoc.translate(0, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                    {
        var pos = catLoc.translate(1, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                    {
        var pos = catLoc.translate(1, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                switch(robot.direction.ordinal()) {
                                    case 0 -> {
                                            {
        var pos = catLoc.translate(-1, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 0))) rc.setIndicatorDot(catLoc.translate(-1, 0), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 1))) rc.setIndicatorDot(catLoc.translate(-1, 1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 2))) rc.setIndicatorDot(catLoc.translate(-1, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(0, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, 2))) rc.setIndicatorDot(catLoc.translate(0, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, 2))) rc.setIndicatorDot(catLoc.translate(1, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 2))) rc.setIndicatorDot(catLoc.translate(2, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 1))) rc.setIndicatorDot(catLoc.translate(2, 1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 0))) rc.setIndicatorDot(catLoc.translate(2, 0), 100, 0, 255);
                                    }
                                    case 1 -> {
                                            {
        var pos = catLoc.translate(-1, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 1))) rc.setIndicatorDot(catLoc.translate(-1, 1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 2))) rc.setIndicatorDot(catLoc.translate(-1, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(0, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, 2))) rc.setIndicatorDot(catLoc.translate(0, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, 2))) rc.setIndicatorDot(catLoc.translate(1, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 2))) rc.setIndicatorDot(catLoc.translate(2, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 1))) rc.setIndicatorDot(catLoc.translate(2, 1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 0))) rc.setIndicatorDot(catLoc.translate(2, 0), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, -1))) rc.setIndicatorDot(catLoc.translate(2, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, -1))) rc.setIndicatorDot(catLoc.translate(1, -1), 100, 0, 255);
                                    }
                                    case 2 -> {
                                            {
        var pos = catLoc.translate(0, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, 2))) rc.setIndicatorDot(catLoc.translate(0, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, 2))) rc.setIndicatorDot(catLoc.translate(1, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 2))) rc.setIndicatorDot(catLoc.translate(2, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 1))) rc.setIndicatorDot(catLoc.translate(2, 1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 0))) rc.setIndicatorDot(catLoc.translate(2, 0), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, -1))) rc.setIndicatorDot(catLoc.translate(2, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, -1))) rc.setIndicatorDot(catLoc.translate(1, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(0, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, -1))) rc.setIndicatorDot(catLoc.translate(0, -1), 100, 0, 255);
                                    }
                                    case 3 -> {
                                            {
        var pos = catLoc.translate(1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, 2))) rc.setIndicatorDot(catLoc.translate(1, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 2))) rc.setIndicatorDot(catLoc.translate(2, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 1))) rc.setIndicatorDot(catLoc.translate(2, 1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 0))) rc.setIndicatorDot(catLoc.translate(2, 0), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, -1))) rc.setIndicatorDot(catLoc.translate(2, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, -1))) rc.setIndicatorDot(catLoc.translate(1, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(0, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, -1))) rc.setIndicatorDot(catLoc.translate(0, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, -1))) rc.setIndicatorDot(catLoc.translate(-1, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 0))) rc.setIndicatorDot(catLoc.translate(-1, 0), 100, 0, 255);
                                    }
                                    case 4 -> {
                                            {
        var pos = catLoc.translate(2, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 1))) rc.setIndicatorDot(catLoc.translate(2, 1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 0))) rc.setIndicatorDot(catLoc.translate(2, 0), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, -1))) rc.setIndicatorDot(catLoc.translate(2, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, -1))) rc.setIndicatorDot(catLoc.translate(1, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(0, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, -1))) rc.setIndicatorDot(catLoc.translate(0, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, -1))) rc.setIndicatorDot(catLoc.translate(-1, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 0))) rc.setIndicatorDot(catLoc.translate(-1, 0), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 1))) rc.setIndicatorDot(catLoc.translate(-1, 1), 100, 0, 255);
                                    }
                                    case 5 -> {
                                            {
        var pos = catLoc.translate(2, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 0))) rc.setIndicatorDot(catLoc.translate(2, 0), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, -1))) rc.setIndicatorDot(catLoc.translate(2, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, -1))) rc.setIndicatorDot(catLoc.translate(1, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(0, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, -1))) rc.setIndicatorDot(catLoc.translate(0, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, -1))) rc.setIndicatorDot(catLoc.translate(-1, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 0))) rc.setIndicatorDot(catLoc.translate(-1, 0), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 1))) rc.setIndicatorDot(catLoc.translate(-1, 1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 2))) rc.setIndicatorDot(catLoc.translate(-1, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(0, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, 2))) rc.setIndicatorDot(catLoc.translate(0, 2), 100, 0, 255);
                                    }
                                    case 6 -> {
                                            {
        var pos = catLoc.translate(1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, -1))) rc.setIndicatorDot(catLoc.translate(1, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(0, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, -1))) rc.setIndicatorDot(catLoc.translate(0, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, -1))) rc.setIndicatorDot(catLoc.translate(-1, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 0))) rc.setIndicatorDot(catLoc.translate(-1, 0), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 1))) rc.setIndicatorDot(catLoc.translate(-1, 1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 2))) rc.setIndicatorDot(catLoc.translate(-1, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(0, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, 2))) rc.setIndicatorDot(catLoc.translate(0, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, 2))) rc.setIndicatorDot(catLoc.translate(1, 2), 100, 0, 255);
                                    }
                                    case 7 -> {
                                            {
        var pos = catLoc.translate(0, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, -1))) rc.setIndicatorDot(catLoc.translate(0, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, -1))) rc.setIndicatorDot(catLoc.translate(-1, -1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 0))) rc.setIndicatorDot(catLoc.translate(-1, 0), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 1))) rc.setIndicatorDot(catLoc.translate(-1, 1), 100, 0, 255);
                                            {
        var pos = catLoc.translate(-1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(-1, 2))) rc.setIndicatorDot(catLoc.translate(-1, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(0, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(0, 2))) rc.setIndicatorDot(catLoc.translate(0, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(1, 2))) rc.setIndicatorDot(catLoc.translate(1, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 2))) rc.setIndicatorDot(catLoc.translate(2, 2), 100, 0, 255);
                                            {
        var pos = catLoc.translate(2, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                                        // if(rc.onTheMap(catLoc.translate(2, 1))) rc.setIndicatorDot(catLoc.translate(2, 1), 100, 0, 255);
                                    }
                                }
                            } else {
                                // invalidate this entry
                                RobotTracker.catHist[i][j] = null;
                            }
                            continue catLoop;
                        }
                    }
                }
                // we can't sense this cat, use historical info only
                    {
        var pos = catLoc.translate(0, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(0, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(1, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(1, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(-1, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(-1, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(-1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(0, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(2, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(2, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(2, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(2, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(0, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(-1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(-1, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(-1, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(-1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(0, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(1, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(2, 2);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(2, 1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(2, 0);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(2, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(0, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

                    {
        var pos = catLoc.translate(-1, -1);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                nearCat0 |= (1l << idx);
            } else {
                nearCat1 |= (1l << (idx - 61));
            }
        }
    }

            }
        }
        // minkowski sum the walls for the rat king again (5x5 hitbox for walls, 3x3 hitbox for allies and dirt)
                if(isRatKing) {
            localPassableExceptAllies0 &= (localPassableExceptAllies0 & 0xfbff7feffdffbffl) << 1;
            localPassableExceptAllies1 &= ((localPassableExceptAllies1 & 0x7feffdffbff7fefl) << 1) | 1;
            localPassableExceptAllies0 &= ((localPassableExceptAllies0 & 0x1f7feffdffbff7fel) >> 1) | 0x1000000000000000l;
            localPassableExceptAllies1 &= (localPassableExceptAllies1 & 0xffdffbff7feffdel) >> 1;

            localPassableExceptAllies1 &= ((localPassableExceptAllies1 & 0x1ffffffffffffl) << 11) | (localPassableExceptAllies0 >> 50);
            localPassableExceptAllies0 &= (localPassableExceptAllies0 & 0x3ffffffffffffl) << 11;
            localPassableExceptAllies0 &= ((localPassableExceptAllies0 >> 11) | ((localPassableExceptAllies1 & 0x7ffl) << 50));
            localPassableExceptAllies1 &= (localPassableExceptAllies1 >> 11);
        }

        localPassableExceptAllies0 &= ~(1l << 60);
    }

    public static Direction doBfs(MapLocation target) throws GameActionException
    {
        prepareBfsMasksSource();
        // d{i}0 + d{i}1 is the mask of reachable within i steps
        long d00 = 0, d01 = 0;
        long d10 = 0, d11 = 0;

            {
        var pos = target;
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                d00 |= (1l << idx);
            } else {
                d01 |= (1l << (idx - 61));
            }
        }
    }

        boolean lastIteration = false;
        bfsLoop: {
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d00, d01);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d00 |= (d00 & 0xfbff7feffdffbffl) << 1;
    d01 |= (d01 & 0x7feffdffbff7fefl) << 1;
    d00 |= (d00 & 0x1f7feffdffbff7fel) >> 1;
    d01 |= (d01 & 0xffdffbff7feffdel) >> 1;

    d01 |= ((d01 & 0x1ffffffffffffl) << 11) | (d00 >> 50);
    d00 |= (d00 & 0x3ffffffffffffl) << 11;
    d00 |= (d00 >> 11) | ((d01 & 0x7ffl) << 50);
    d01 |= d01 >> 11;
    d00 &= localPassableExceptAllies0;
    d01 &= localPassableExceptAllies1;

            long d20 = 0l;
            long d21 = 0l;
            d20 = d00;
            d21 = d01;
            d00 &= ~CellTracker.localDirt0;
            d01 &= ~CellTracker.localDirt1;
            d10 |= d00;
            d11 |= d01;
            // // Profiler.end("bfs iteration");
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d10, d11);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d10 & 0x807000000000000l) != 0 || (d11 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d10 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d11 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d11 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d10 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d10 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d10 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d11 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d11 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 1;
                    // indicate("bfs iters: " + 1);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d10 |= (d10 & 0xfbff7feffdffbffl) << 1;
    d11 |= (d11 & 0x7feffdffbff7fefl) << 1;
    d10 |= (d10 & 0x1f7feffdffbff7fel) >> 1;
    d11 |= (d11 & 0xffdffbff7feffdel) >> 1;

    d11 |= ((d11 & 0x1ffffffffffffl) << 11) | (d10 >> 50);
    d10 |= (d10 & 0x3ffffffffffffl) << 11;
    d10 |= (d10 >> 11) | ((d11 & 0x7ffl) << 50);
    d11 |= d11 >> 11;
    d10 &= localPassableExceptAllies0;
    d11 &= localPassableExceptAllies1;

            long d30 = 0l;
            long d31 = 0l;
            d30 = d10;
            d31 = d11;
            d10 &= ~CellTracker.localDirt0;
            d11 &= ~CellTracker.localDirt1;
            d20 |= d10;
            d21 |= d11;
            // // Profiler.end("bfs iteration");
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d20, d21);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d20 & 0x807000000000000l) != 0 || (d21 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d20 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d21 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d21 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d20 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d20 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d20 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d21 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d21 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 2;
                    // indicate("bfs iters: " + 2);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d20 |= (d20 & 0xfbff7feffdffbffl) << 1;
    d21 |= (d21 & 0x7feffdffbff7fefl) << 1;
    d20 |= (d20 & 0x1f7feffdffbff7fel) >> 1;
    d21 |= (d21 & 0xffdffbff7feffdel) >> 1;

    d21 |= ((d21 & 0x1ffffffffffffl) << 11) | (d20 >> 50);
    d20 |= (d20 & 0x3ffffffffffffl) << 11;
    d20 |= (d20 >> 11) | ((d21 & 0x7ffl) << 50);
    d21 |= d21 >> 11;
    d20 &= localPassableExceptAllies0;
    d21 &= localPassableExceptAllies1;

            long d40 = 0l;
            long d41 = 0l;
            d40 = d20;
            d41 = d21;
            d20 &= ~CellTracker.localDirt0;
            d21 &= ~CellTracker.localDirt1;
            d30 |= d20;
            d31 |= d21;
            // // Profiler.end("bfs iteration");
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d30, d31);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d30 & 0x807000000000000l) != 0 || (d31 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d30 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d31 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d31 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d30 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d30 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d30 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d31 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d31 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 3;
                    // indicate("bfs iters: " + 3);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d30 |= (d30 & 0xfbff7feffdffbffl) << 1;
    d31 |= (d31 & 0x7feffdffbff7fefl) << 1;
    d30 |= (d30 & 0x1f7feffdffbff7fel) >> 1;
    d31 |= (d31 & 0xffdffbff7feffdel) >> 1;

    d31 |= ((d31 & 0x1ffffffffffffl) << 11) | (d30 >> 50);
    d30 |= (d30 & 0x3ffffffffffffl) << 11;
    d30 |= (d30 >> 11) | ((d31 & 0x7ffl) << 50);
    d31 |= d31 >> 11;
    d30 &= localPassableExceptAllies0;
    d31 &= localPassableExceptAllies1;

            long d50 = 0l;
            long d51 = 0l;
            d50 = d30;
            d51 = d31;
            d30 &= ~CellTracker.localDirt0;
            d31 &= ~CellTracker.localDirt1;
            d40 |= d30;
            d41 |= d31;
            // // Profiler.end("bfs iteration");
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d40, d41);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d40 & 0x807000000000000l) != 0 || (d41 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d40 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d41 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d41 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d40 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d40 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d40 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d41 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d41 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 4;
                    // indicate("bfs iters: " + 4);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d40 |= (d40 & 0xfbff7feffdffbffl) << 1;
    d41 |= (d41 & 0x7feffdffbff7fefl) << 1;
    d40 |= (d40 & 0x1f7feffdffbff7fel) >> 1;
    d41 |= (d41 & 0xffdffbff7feffdel) >> 1;

    d41 |= ((d41 & 0x1ffffffffffffl) << 11) | (d40 >> 50);
    d40 |= (d40 & 0x3ffffffffffffl) << 11;
    d40 |= (d40 >> 11) | ((d41 & 0x7ffl) << 50);
    d41 |= d41 >> 11;
    d40 &= localPassableExceptAllies0;
    d41 &= localPassableExceptAllies1;

            long d60 = 0l;
            long d61 = 0l;
            d60 = d40;
            d61 = d41;
            d40 &= ~CellTracker.localDirt0;
            d41 &= ~CellTracker.localDirt1;
            d50 |= d40;
            d51 |= d41;
            // // Profiler.end("bfs iteration");
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d50, d51);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d50 & 0x807000000000000l) != 0 || (d51 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d50 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d51 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d51 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d50 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d50 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d50 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d51 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d51 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 5;
                    // indicate("bfs iters: " + 5);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d50 |= (d50 & 0xfbff7feffdffbffl) << 1;
    d51 |= (d51 & 0x7feffdffbff7fefl) << 1;
    d50 |= (d50 & 0x1f7feffdffbff7fel) >> 1;
    d51 |= (d51 & 0xffdffbff7feffdel) >> 1;

    d51 |= ((d51 & 0x1ffffffffffffl) << 11) | (d50 >> 50);
    d50 |= (d50 & 0x3ffffffffffffl) << 11;
    d50 |= (d50 >> 11) | ((d51 & 0x7ffl) << 50);
    d51 |= d51 >> 11;
    d50 &= localPassableExceptAllies0;
    d51 &= localPassableExceptAllies1;

            long d70 = 0l;
            long d71 = 0l;
            d70 = d50;
            d71 = d51;
            d50 &= ~CellTracker.localDirt0;
            d51 &= ~CellTracker.localDirt1;
            d60 |= d50;
            d61 |= d51;
            // // Profiler.end("bfs iteration");
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d60, d61);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d60 & 0x807000000000000l) != 0 || (d61 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d60 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d61 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d61 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d60 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d60 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d60 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d61 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d61 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 6;
                    // indicate("bfs iters: " + 6);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d60 |= (d60 & 0xfbff7feffdffbffl) << 1;
    d61 |= (d61 & 0x7feffdffbff7fefl) << 1;
    d60 |= (d60 & 0x1f7feffdffbff7fel) >> 1;
    d61 |= (d61 & 0xffdffbff7feffdel) >> 1;

    d61 |= ((d61 & 0x1ffffffffffffl) << 11) | (d60 >> 50);
    d60 |= (d60 & 0x3ffffffffffffl) << 11;
    d60 |= (d60 >> 11) | ((d61 & 0x7ffl) << 50);
    d61 |= d61 >> 11;
    d60 &= localPassableExceptAllies0;
    d61 &= localPassableExceptAllies1;

            long d80 = 0l;
            long d81 = 0l;
            d80 = d60;
            d81 = d61;
            d60 &= ~CellTracker.localDirt0;
            d61 &= ~CellTracker.localDirt1;
            d70 |= d60;
            d71 |= d61;
            // // Profiler.end("bfs iteration");
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d70, d71);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d70 & 0x807000000000000l) != 0 || (d71 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d70 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d71 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d71 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d70 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d70 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d70 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d71 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d71 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 7;
                    // indicate("bfs iters: " + 7);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d70 |= (d70 & 0xfbff7feffdffbffl) << 1;
    d71 |= (d71 & 0x7feffdffbff7fefl) << 1;
    d70 |= (d70 & 0x1f7feffdffbff7fel) >> 1;
    d71 |= (d71 & 0xffdffbff7feffdel) >> 1;

    d71 |= ((d71 & 0x1ffffffffffffl) << 11) | (d70 >> 50);
    d70 |= (d70 & 0x3ffffffffffffl) << 11;
    d70 |= (d70 >> 11) | ((d71 & 0x7ffl) << 50);
    d71 |= d71 >> 11;
    d70 &= localPassableExceptAllies0;
    d71 &= localPassableExceptAllies1;

            long d90 = 0l;
            long d91 = 0l;
            d90 = d70;
            d91 = d71;
            d70 &= ~CellTracker.localDirt0;
            d71 &= ~CellTracker.localDirt1;
            d80 |= d70;
            d81 |= d71;
            // // Profiler.end("bfs iteration");
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d80, d81);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d80 & 0x807000000000000l) != 0 || (d81 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d80 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d81 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d81 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d80 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d80 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d80 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d81 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d81 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 8;
                    // indicate("bfs iters: " + 8);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d80 |= (d80 & 0xfbff7feffdffbffl) << 1;
    d81 |= (d81 & 0x7feffdffbff7fefl) << 1;
    d80 |= (d80 & 0x1f7feffdffbff7fel) >> 1;
    d81 |= (d81 & 0xffdffbff7feffdel) >> 1;

    d81 |= ((d81 & 0x1ffffffffffffl) << 11) | (d80 >> 50);
    d80 |= (d80 & 0x3ffffffffffffl) << 11;
    d80 |= (d80 >> 11) | ((d81 & 0x7ffl) << 50);
    d81 |= d81 >> 11;
    d80 &= localPassableExceptAllies0;
    d81 &= localPassableExceptAllies1;

            long d100 = 0l;
            long d101 = 0l;
            d100 = d80;
            d101 = d81;
            d80 &= ~CellTracker.localDirt0;
            d81 &= ~CellTracker.localDirt1;
            d90 |= d80;
            d91 |= d81;
            // // Profiler.end("bfs iteration");
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d90, d91);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d90 & 0x807000000000000l) != 0 || (d91 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d90 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d91 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d91 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d90 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d90 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d90 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d91 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d91 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 9;
                    // indicate("bfs iters: " + 9);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d90 |= (d90 & 0xfbff7feffdffbffl) << 1;
    d91 |= (d91 & 0x7feffdffbff7fefl) << 1;
    d90 |= (d90 & 0x1f7feffdffbff7fel) >> 1;
    d91 |= (d91 & 0xffdffbff7feffdel) >> 1;

    d91 |= ((d91 & 0x1ffffffffffffl) << 11) | (d90 >> 50);
    d90 |= (d90 & 0x3ffffffffffffl) << 11;
    d90 |= (d90 >> 11) | ((d91 & 0x7ffl) << 50);
    d91 |= d91 >> 11;
    d90 &= localPassableExceptAllies0;
    d91 &= localPassableExceptAllies1;

            long d110 = 0l;
            long d111 = 0l;
            d110 = d90;
            d111 = d91;
            d90 &= ~CellTracker.localDirt0;
            d91 &= ~CellTracker.localDirt1;
            d100 |= d90;
            d101 |= d91;
            // // Profiler.end("bfs iteration");
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d100, d101);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d100 & 0x807000000000000l) != 0 || (d101 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d100 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d101 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d101 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d100 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d100 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d100 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d101 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d101 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 10;
                    // indicate("bfs iters: " + 10);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d100 |= (d100 & 0xfbff7feffdffbffl) << 1;
    d101 |= (d101 & 0x7feffdffbff7fefl) << 1;
    d100 |= (d100 & 0x1f7feffdffbff7fel) >> 1;
    d101 |= (d101 & 0xffdffbff7feffdel) >> 1;

    d101 |= ((d101 & 0x1ffffffffffffl) << 11) | (d100 >> 50);
    d100 |= (d100 & 0x3ffffffffffffl) << 11;
    d100 |= (d100 >> 11) | ((d101 & 0x7ffl) << 50);
    d101 |= d101 >> 11;
    d100 &= localPassableExceptAllies0;
    d101 &= localPassableExceptAllies1;

            long d120 = 0l;
            long d121 = 0l;
            d120 = d100;
            d121 = d101;
            d100 &= ~CellTracker.localDirt0;
            d101 &= ~CellTracker.localDirt1;
            d110 |= d100;
            d111 |= d101;
            // // Profiler.end("bfs iteration");
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d110, d111);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d110 & 0x807000000000000l) != 0 || (d111 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d110 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d111 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d111 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d110 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d110 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d110 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d111 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d111 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 11;
                    // indicate("bfs iters: " + 11);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d110 |= (d110 & 0xfbff7feffdffbffl) << 1;
    d111 |= (d111 & 0x7feffdffbff7fefl) << 1;
    d110 |= (d110 & 0x1f7feffdffbff7fel) >> 1;
    d111 |= (d111 & 0xffdffbff7feffdel) >> 1;

    d111 |= ((d111 & 0x1ffffffffffffl) << 11) | (d110 >> 50);
    d110 |= (d110 & 0x3ffffffffffffl) << 11;
    d110 |= (d110 >> 11) | ((d111 & 0x7ffl) << 50);
    d111 |= d111 >> 11;
    d110 &= localPassableExceptAllies0;
    d111 &= localPassableExceptAllies1;

            long d130 = 0l;
            long d131 = 0l;
            d130 = d110;
            d131 = d111;
            d110 &= ~CellTracker.localDirt0;
            d111 &= ~CellTracker.localDirt1;
            d120 |= d110;
            d121 |= d111;
            // // Profiler.end("bfs iteration");
            // if(amAfter(12556, 84)) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d120, d121);
            //     // CellTracker.printLocalMask(localPassableExceptAllies0, localPassableExceptAllies1);
            //     // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
            //     Clock.yield();
            //     // indicatorString = "";
            //     // CellTracker.printLocalMask(nearCat0, nearCat1);
            //     // Clock.yield();
            // }
            if((d120 & 0x807000000000000l) != 0 || (d121 & 0xe01) != 0) {
                // // Profiler.begin();
                // this is sloppy since we only care about tiles <= 2 chebyshev distance away
                long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
                notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
                notWallAdjacent0 &= notWallAdjacent0 << 1;
                notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
                notWallAdjacent1 &= notWallAdjacent1 >> 1;
                notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
                notWallAdjacent0 &= notWallAdjacent0 << 11;
                notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
                notWallAdjacent1 &= notWallAdjacent1 >> 11;

                Direction bestMove = null;
                double bestScore = 1e18;
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 49))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d120 & (1l << 49))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 49))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.EAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.EAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 0))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d121 & (1l << 0))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 0))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.EAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.EAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTH);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTH)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 10))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d121 & (1l << 10))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 10))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTH == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTH + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.WEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.WEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 59))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d120 & (1l << 59))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 59))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.WEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.WEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 48))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d120 & (1l << 48))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 48))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.SOUTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.SOUTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent0 & (1l << 50))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d120 & (1l << 50))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat0 & (1l << 50))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.SOUTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHWEST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHWEST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 9))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d121 & (1l << 9))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 9))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHWEST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                tryMove: {
                    var nextLoc = myLoc.add(Direction.NORTHEAST);
                    boolean isDirt = false;
                    if((rc.canMove(Direction.NORTHEAST)
                        || (isDirt = CellTracker.isDirt(nextLoc)) && rc.getAllCheese() >= GameConstants.DIG_DIRT_CHEESE_COST
                        || isRatKing && CellTracker.isKingPassable(nextLoc))
                       && (isRatKing || !RobotTracker.kingSet.contains(nextLoc))) {
                        double score = nextLoc.distanceSquaredTo(target)
                            ;
                        // prefer staying away from walls, this helps when changing direction
                        if(isRatKing) {
                            if(CellTracker.isPassable5x5(nextLoc)) {
                                score -= 100;
                            }
                        } else {
                            if(
                               (notWallAdjacent1 & (1l << 11))
                               != 0
                               ) {
                                score -= 100;
                            }
                        }
                        // prefer getting closer to the target
                        if(
                           (d121 & (1l << 11))
                           != 0
                           ) {
                            score -= 1200;
                        } else if(isRatKing) {
                            break tryMove;
                        }
                        // strongly prefer avoiding adjacent squares to historical cat locations
                        if(
                           (nearCat1 & (1l << 11))
                           != 0
                           ) {
                            score += 2700;
                        }
                        // prefer definitely empty squares over possibly dirt
                        if(isDirt) {
                            score += 1000;
                        }
                        // if we can't turn, then prefer moving straight
                        // however, we still prefer strafing to moving forward into dirt
                        if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                            score -= 500;
                        }
                        score += catPenalty(nextLoc);
                        // indicate(Direction.NORTHEAST + " " + score);
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // // Profiler.end("bfs best move");
                if(bestMove != null) {
                    // Profiler.end("bfs");
                    // indicate("bestMove: " + bestMove);
                    lastDist = 12;
                    // indicate("bfs iters: " + 12);
                    return bestMove;
                }
            }

            // // Profiler.begin();

                // we don't need to worry about the "seam" here because it's at the cell we are standing on
    d120 |= (d120 & 0xfbff7feffdffbffl) << 1;
    d121 |= (d121 & 0x7feffdffbff7fefl) << 1;
    d120 |= (d120 & 0x1f7feffdffbff7fel) >> 1;
    d121 |= (d121 & 0xffdffbff7feffdel) >> 1;

    d121 |= ((d121 & 0x1ffffffffffffl) << 11) | (d120 >> 50);
    d120 |= (d120 & 0x3ffffffffffffl) << 11;
    d120 |= (d120 >> 11) | ((d121 & 0x7ffl) << 50);
    d121 |= d121 >> 11;
    d120 &= localPassableExceptAllies0;
    d121 &= localPassableExceptAllies1;

            long d140 = 0l;
            long d141 = 0l;
            d140 = d120;
            d141 = d121;
            d120 &= ~CellTracker.localDirt0;
            d121 &= ~CellTracker.localDirt1;
            d130 |= d120;
            d131 |= d121;
            // // Profiler.end("bfs iteration");
        }
        // Profiler.end("bfs");

        // println("fallback when bfs can't find path");
        // indicate("fallback when bfs can't find path");


        return Direction.CENTER;
    }

    // returns a mask of good directions according to the default order
    public static int[] multisourceBfs(MapLocation[] sources, int sourceCnt) throws GameActionException {
        if (sourceCnt == 0) {
            // println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! MULTISOURCE BFS NO SOURCES !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            return new int[] {0, 1000};
        }
        CellTracker.computeLocalBfsMasks();
        long reach0 = 0, reach1 = 0;
        for (int i = 0; i < sourceCnt; i++) {
                {
        var pos = sources[i];
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                reach0 |= (1l << idx);
            } else {
                reach1 |= (1l << (idx - 61));
            }
        }
    }

        }
        var localPassable0 = CellTracker.localPassable0 & ~CellTracker.localDirt0;
        var localPassable1 = CellTracker.localPassable1 & ~CellTracker.localDirt1;
        for(int i = RobotTracker.allyCount - 1; i >= 0; i--) {
                {
        var pos = RobotTracker.allies[i];
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassable0 &= ~ (1l << idx);
            } else {
                localPassable1 &= ~ (1l << (idx - 61));
            }
        }
    }

        }
        for(int i = 0; i < 5; i++) {
            var kingLoc = GlobalArray.getRatKing(i);
            if(kingLoc == null) continue;
                {
        var pos = kingLoc.add(Direction.NORTH);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                {
        var pos = kingLoc.add(Direction.NORTHEAST);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                {
        var pos = kingLoc.add(Direction.EAST);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                {
        var pos = kingLoc.add(Direction.SOUTHEAST);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                {
        var pos = kingLoc.add(Direction.SOUTH);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                {
        var pos = kingLoc.add(Direction.SOUTHWEST);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                {
        var pos = kingLoc.add(Direction.WEST);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                {
        var pos = kingLoc.add(Direction.NORTHWEST);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

                {
        var pos = kingLoc.add(Direction.CENTER);
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassableExceptAllies0 &= ~ (1l << idx);
            } else {
                localPassableExceptAllies1 &= ~ (1l << (idx - 61));
            }
        }
    }

        }
        if((reach0 & 0x807000000000000l) != 0 || (reach1 & 0xe01) != 0) {
            // indicate("loop 0");
            int res = 0;
            if(
               (reach1 & (1l << 10))
               != 0) {
                res |= 1;
            }
            if(
               (reach1 & (1l << 11))
               != 0) {
                res |= 2;
            }
            if(
               (reach1 & (1l << 0))
               != 0) {
                res |= 4;
            }
            if(
               (reach0 & (1l << 50))
               != 0) {
                res |= 8;
            }
            if(
               (reach0 & (1l << 49))
               != 0) {
                res |= 16;
            }
            if(
               (reach0 & (1l << 48))
               != 0) {
                res |= 32;
            }
            if(
               (reach0 & (1l << 59))
               != 0) {
                res |= 64;
            }
            if(
               (reach1 & (1l << 9))
               != 0) {
                res |= 128;
            }
            return new int[] {res, 0};
        }
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
    reach0 |= (reach0 & 0xfbff7feffdffbffl) << 1;
    reach1 |= (reach1 & 0x7feffdffbff7fefl) << 1;
    reach0 |= (reach0 & 0x1f7feffdffbff7fel) >> 1;
    reach1 |= (reach1 & 0xffdffbff7feffdel) >> 1;

    reach1 |= ((reach1 & 0x1ffffffffffffl) << 11) | (reach0 >> 50);
    reach0 |= (reach0 & 0x3ffffffffffffl) << 11;
    reach0 |= (reach0 >> 11) | ((reach1 & 0x7ffl) << 50);
    reach1 |= reach1 >> 11;
    reach0 &= localPassable0;
    reach1 &= localPassable1;

        if((reach0 & 0x807000000000000l) != 0 || (reach1 & 0xe01) != 0) {
            // indicate("loop 1");
            int res = 0;
            if(
               (reach1 & (1l << 10))
               != 0) {
                res |= 1;
            }
            if(
               (reach1 & (1l << 11))
               != 0) {
                res |= 2;
            }
            if(
               (reach1 & (1l << 0))
               != 0) {
                res |= 4;
            }
            if(
               (reach0 & (1l << 50))
               != 0) {
                res |= 8;
            }
            if(
               (reach0 & (1l << 49))
               != 0) {
                res |= 16;
            }
            if(
               (reach0 & (1l << 48))
               != 0) {
                res |= 32;
            }
            if(
               (reach0 & (1l << 59))
               != 0) {
                res |= 64;
            }
            if(
               (reach1 & (1l << 9))
               != 0) {
                res |= 128;
            }
            return new int[] {res, 1};
        }
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
    reach0 |= (reach0 & 0xfbff7feffdffbffl) << 1;
    reach1 |= (reach1 & 0x7feffdffbff7fefl) << 1;
    reach0 |= (reach0 & 0x1f7feffdffbff7fel) >> 1;
    reach1 |= (reach1 & 0xffdffbff7feffdel) >> 1;

    reach1 |= ((reach1 & 0x1ffffffffffffl) << 11) | (reach0 >> 50);
    reach0 |= (reach0 & 0x3ffffffffffffl) << 11;
    reach0 |= (reach0 >> 11) | ((reach1 & 0x7ffl) << 50);
    reach1 |= reach1 >> 11;
    reach0 &= localPassable0;
    reach1 &= localPassable1;

        if((reach0 & 0x807000000000000l) != 0 || (reach1 & 0xe01) != 0) {
            // indicate("loop 2");
            int res = 0;
            if(
               (reach1 & (1l << 10))
               != 0) {
                res |= 1;
            }
            if(
               (reach1 & (1l << 11))
               != 0) {
                res |= 2;
            }
            if(
               (reach1 & (1l << 0))
               != 0) {
                res |= 4;
            }
            if(
               (reach0 & (1l << 50))
               != 0) {
                res |= 8;
            }
            if(
               (reach0 & (1l << 49))
               != 0) {
                res |= 16;
            }
            if(
               (reach0 & (1l << 48))
               != 0) {
                res |= 32;
            }
            if(
               (reach0 & (1l << 59))
               != 0) {
                res |= 64;
            }
            if(
               (reach1 & (1l << 9))
               != 0) {
                res |= 128;
            }
            return new int[] {res, 2};
        }
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
    reach0 |= (reach0 & 0xfbff7feffdffbffl) << 1;
    reach1 |= (reach1 & 0x7feffdffbff7fefl) << 1;
    reach0 |= (reach0 & 0x1f7feffdffbff7fel) >> 1;
    reach1 |= (reach1 & 0xffdffbff7feffdel) >> 1;

    reach1 |= ((reach1 & 0x1ffffffffffffl) << 11) | (reach0 >> 50);
    reach0 |= (reach0 & 0x3ffffffffffffl) << 11;
    reach0 |= (reach0 >> 11) | ((reach1 & 0x7ffl) << 50);
    reach1 |= reach1 >> 11;
    reach0 &= localPassable0;
    reach1 &= localPassable1;

        if((reach0 & 0x807000000000000l) != 0 || (reach1 & 0xe01) != 0) {
            // indicate("loop 3");
            int res = 0;
            if(
               (reach1 & (1l << 10))
               != 0) {
                res |= 1;
            }
            if(
               (reach1 & (1l << 11))
               != 0) {
                res |= 2;
            }
            if(
               (reach1 & (1l << 0))
               != 0) {
                res |= 4;
            }
            if(
               (reach0 & (1l << 50))
               != 0) {
                res |= 8;
            }
            if(
               (reach0 & (1l << 49))
               != 0) {
                res |= 16;
            }
            if(
               (reach0 & (1l << 48))
               != 0) {
                res |= 32;
            }
            if(
               (reach0 & (1l << 59))
               != 0) {
                res |= 64;
            }
            if(
               (reach1 & (1l << 9))
               != 0) {
                res |= 128;
            }
            return new int[] {res, 3};
        }
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
    reach0 |= (reach0 & 0xfbff7feffdffbffl) << 1;
    reach1 |= (reach1 & 0x7feffdffbff7fefl) << 1;
    reach0 |= (reach0 & 0x1f7feffdffbff7fel) >> 1;
    reach1 |= (reach1 & 0xffdffbff7feffdel) >> 1;

    reach1 |= ((reach1 & 0x1ffffffffffffl) << 11) | (reach0 >> 50);
    reach0 |= (reach0 & 0x3ffffffffffffl) << 11;
    reach0 |= (reach0 >> 11) | ((reach1 & 0x7ffl) << 50);
    reach1 |= reach1 >> 11;
    reach0 &= localPassable0;
    reach1 &= localPassable1;

        if((reach0 & 0x807000000000000l) != 0 || (reach1 & 0xe01) != 0) {
            // indicate("loop 4");
            int res = 0;
            if(
               (reach1 & (1l << 10))
               != 0) {
                res |= 1;
            }
            if(
               (reach1 & (1l << 11))
               != 0) {
                res |= 2;
            }
            if(
               (reach1 & (1l << 0))
               != 0) {
                res |= 4;
            }
            if(
               (reach0 & (1l << 50))
               != 0) {
                res |= 8;
            }
            if(
               (reach0 & (1l << 49))
               != 0) {
                res |= 16;
            }
            if(
               (reach0 & (1l << 48))
               != 0) {
                res |= 32;
            }
            if(
               (reach0 & (1l << 59))
               != 0) {
                res |= 64;
            }
            if(
               (reach1 & (1l << 9))
               != 0) {
                res |= 128;
            }
            return new int[] {res, 4};
        }
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
    reach0 |= (reach0 & 0xfbff7feffdffbffl) << 1;
    reach1 |= (reach1 & 0x7feffdffbff7fefl) << 1;
    reach0 |= (reach0 & 0x1f7feffdffbff7fel) >> 1;
    reach1 |= (reach1 & 0xffdffbff7feffdel) >> 1;

    reach1 |= ((reach1 & 0x1ffffffffffffl) << 11) | (reach0 >> 50);
    reach0 |= (reach0 & 0x3ffffffffffffl) << 11;
    reach0 |= (reach0 >> 11) | ((reach1 & 0x7ffl) << 50);
    reach1 |= reach1 >> 11;
    reach0 &= localPassable0;
    reach1 &= localPassable1;

        if((reach0 & 0x807000000000000l) != 0 || (reach1 & 0xe01) != 0) {
            // indicate("loop 5");
            int res = 0;
            if(
               (reach1 & (1l << 10))
               != 0) {
                res |= 1;
            }
            if(
               (reach1 & (1l << 11))
               != 0) {
                res |= 2;
            }
            if(
               (reach1 & (1l << 0))
               != 0) {
                res |= 4;
            }
            if(
               (reach0 & (1l << 50))
               != 0) {
                res |= 8;
            }
            if(
               (reach0 & (1l << 49))
               != 0) {
                res |= 16;
            }
            if(
               (reach0 & (1l << 48))
               != 0) {
                res |= 32;
            }
            if(
               (reach0 & (1l << 59))
               != 0) {
                res |= 64;
            }
            if(
               (reach1 & (1l << 9))
               != 0) {
                res |= 128;
            }
            return new int[] {res, 5};
        }
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
    reach0 |= (reach0 & 0xfbff7feffdffbffl) << 1;
    reach1 |= (reach1 & 0x7feffdffbff7fefl) << 1;
    reach0 |= (reach0 & 0x1f7feffdffbff7fel) >> 1;
    reach1 |= (reach1 & 0xffdffbff7feffdel) >> 1;

    reach1 |= ((reach1 & 0x1ffffffffffffl) << 11) | (reach0 >> 50);
    reach0 |= (reach0 & 0x3ffffffffffffl) << 11;
    reach0 |= (reach0 >> 11) | ((reach1 & 0x7ffl) << 50);
    reach1 |= reach1 >> 11;
    reach0 &= localPassable0;
    reach1 &= localPassable1;

        if((reach0 & 0x807000000000000l) != 0 || (reach1 & 0xe01) != 0) {
            // indicate("loop 6");
            int res = 0;
            if(
               (reach1 & (1l << 10))
               != 0) {
                res |= 1;
            }
            if(
               (reach1 & (1l << 11))
               != 0) {
                res |= 2;
            }
            if(
               (reach1 & (1l << 0))
               != 0) {
                res |= 4;
            }
            if(
               (reach0 & (1l << 50))
               != 0) {
                res |= 8;
            }
            if(
               (reach0 & (1l << 49))
               != 0) {
                res |= 16;
            }
            if(
               (reach0 & (1l << 48))
               != 0) {
                res |= 32;
            }
            if(
               (reach0 & (1l << 59))
               != 0) {
                res |= 64;
            }
            if(
               (reach1 & (1l << 9))
               != 0) {
                res |= 128;
            }
            return new int[] {res, 6};
        }
        return new int[] {0, 1000};
    }

    public static long reach0, reach1;
    public static void computeReachable() {
        CellTracker.computeLocalBfsMasks();
        var localPassable0 = CellTracker.localPassable0;
        var localPassable1 = CellTracker.localPassable1;
        for(int i = RobotTracker.allyCount - 1; i >= 0; i--) {
            var ally = RobotTracker.allies[i];
                {
        var pos = ally;
        int xi = pos.x - myLocX + 5;
        int yi = pos.y - myLocY + 5;
        if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
        {
            int idx = yi * 11 + xi;
            if(idx < 61) {
                localPassable0 &= ~ (1l << idx);
            } else {
                localPassable1 &= ~ (1l << (idx - 61));
            }
        }
    }

        }
        reach0 = 0x807000000000000l & localPassable0;
        reach1 = 0xe01 & localPassable1;
        // if(amAfter(12903, 468)) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(reach0, reach1);
        //     Clock.yield();
        // }
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
    reach0 |= (reach0 & 0xfbff7feffdffbffl) << 1;
    reach1 |= (reach1 & 0x7feffdffbff7fefl) << 1;
    reach0 |= (reach0 & 0x1f7feffdffbff7fel) >> 1;
    reach1 |= (reach1 & 0xffdffbff7feffdel) >> 1;

    reach1 |= ((reach1 & 0x1ffffffffffffl) << 11) | (reach0 >> 50);
    reach0 |= (reach0 & 0x3ffffffffffffl) << 11;
    reach0 |= (reach0 >> 11) | ((reach1 & 0x7ffl) << 50);
    reach1 |= reach1 >> 11;
    reach0 &= localPassable0;
    reach1 &= localPassable1;

        // if(amAfter(12903, 468)) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(reach0, reach1);
        //     Clock.yield();
        // }
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
    reach0 |= (reach0 & 0xfbff7feffdffbffl) << 1;
    reach1 |= (reach1 & 0x7feffdffbff7fefl) << 1;
    reach0 |= (reach0 & 0x1f7feffdffbff7fel) >> 1;
    reach1 |= (reach1 & 0xffdffbff7feffdel) >> 1;

    reach1 |= ((reach1 & 0x1ffffffffffffl) << 11) | (reach0 >> 50);
    reach0 |= (reach0 & 0x3ffffffffffffl) << 11;
    reach0 |= (reach0 >> 11) | ((reach1 & 0x7ffl) << 50);
    reach1 |= reach1 >> 11;
    reach0 &= localPassable0;
    reach1 &= localPassable1;

        // if(amAfter(12903, 468)) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(reach0, reach1);
        //     Clock.yield();
        // }
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
    reach0 |= (reach0 & 0xfbff7feffdffbffl) << 1;
    reach1 |= (reach1 & 0x7feffdffbff7fefl) << 1;
    reach0 |= (reach0 & 0x1f7feffdffbff7fel) >> 1;
    reach1 |= (reach1 & 0xffdffbff7feffdel) >> 1;

    reach1 |= ((reach1 & 0x1ffffffffffffl) << 11) | (reach0 >> 50);
    reach0 |= (reach0 & 0x3ffffffffffffl) << 11;
    reach0 |= (reach0 >> 11) | ((reach1 & 0x7ffl) << 50);
    reach1 |= reach1 >> 11;
    reach0 &= localPassable0;
    reach1 &= localPassable1;

        // if(amAfter(12903, 468)) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(reach0, reach1);
        //     Clock.yield();
        // }
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
    reach0 |= (reach0 & 0xfbff7feffdffbffl) << 1;
    reach1 |= (reach1 & 0x7feffdffbff7fefl) << 1;
    reach0 |= (reach0 & 0x1f7feffdffbff7fel) >> 1;
    reach1 |= (reach1 & 0xffdffbff7feffdel) >> 1;

    reach1 |= ((reach1 & 0x1ffffffffffffl) << 11) | (reach0 >> 50);
    reach0 |= (reach0 & 0x3ffffffffffffl) << 11;
    reach0 |= (reach0 >> 11) | ((reach1 & 0x7ffl) << 50);
    reach1 |= reach1 >> 11;
    reach0 &= localPassable0;
    reach1 &= localPassable1;

        // if(amAfter(12903, 468)) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(reach0, reach1);
        //     Clock.yield();
        // }
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
    reach0 |= (reach0 & 0xfbff7feffdffbffl) << 1;
    reach1 |= (reach1 & 0x7feffdffbff7fefl) << 1;
    reach0 |= (reach0 & 0x1f7feffdffbff7fel) >> 1;
    reach1 |= (reach1 & 0xffdffbff7feffdel) >> 1;

    reach1 |= ((reach1 & 0x1ffffffffffffl) << 11) | (reach0 >> 50);
    reach0 |= (reach0 & 0x3ffffffffffffl) << 11;
    reach0 |= (reach0 >> 11) | ((reach1 & 0x7ffl) << 50);
    reach1 |= reach1 >> 11;
    reach0 &= localPassable0;
    reach1 &= localPassable1;

    }
}