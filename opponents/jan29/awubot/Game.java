package awubot;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.common.UnitType;
import static awubot.util.Util.*;

import awubot.util.string.FastIndexedIntSet;
import awubot.comms.GlobalArray;
import awubot.robot.king.RatKing;
import awubot.util.Profiler;
import awubot.util.Util;

public class Game {
    public static boolean DISABLE_OUTPUT = false;

    public static RobotController rc;
    public static int mapWidth, mapHeight;

    // Cached for cheaper access
    public static MapLocation myLoc;
    public static int myLocX, myLocY;
    public static int time;
    public static Direction myDir;
    public static Team team;
    public static int myIndex; // estimate of rat index
    public static MapLocation myPreviousLoc;
    public static int lastHP;
    public static FastIndexedIntSet knownCheeseMines;
    public static MapLocation spawn;
    public static int carryStartTime;

    public static void _rawMove(Direction dir) throws GameActionException {
        rc.move(dir);
        myLoc = rc.getLocation();
        myPreviousLoc = myLoc;
        myLocX = myLoc.x;
        myLocY = myLoc.y;
    }

    public static void _turn(Direction dir) throws GameActionException {
        assert dir != Direction.CENTER;
        if (dir == myDir) return;
        rc.turn(dir);
        myDir = dir;
    }

    public static boolean isRatKing;
    public static int kingId = -1;

    public static void convertKing() throws GameActionException {
        isRatKing = true;
        RobotPlayer.hasInitialisedRobot = false;
        GlobalArray.initRatKing();
        awubot.comms.MapManager.onConvertKing();
        Pathfinder.bug.clearPath();
        Clock.yield();
    }

    public static void init(RobotController rc) throws GameActionException {
        Game.rc = rc;
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        myLoc = rc.getLocation();
        myLocX = myLoc.x;
        myLocY = myLoc.y;
        myDir = rc.getDirection();
        time = rc.getRoundNum();
        isRatKing = (rc.getType() == UnitType.RAT_KING);
        team = rc.getTeam();
        spawn = myLoc;
        knownCheeseMines = new FastIndexedIntSet();
        lastHP = rc.getHealth();
        carryStartTime = -1;

        // StringBuilder sb = new StringBuilder();
        // Direction moveDir = Direction.NORTH;
        // RobotInfo target = rc.senseRobotAtLocation(myLoc);

        // int x = 1, y = 2;
        // StringBuilder[] arr = new StringBuilder[2];
        // arr[0] = new StringBuilder();
        // arr[0].repeat('\0', 100);
        // arr[1] = new StringBuilder();
        // arr[1].repeat('\0', 100);
        // // Profiler.begin();
        // char c0 = arr[x].charAt(y); 
        // // Profiler.end_print("arr");

        // StringBuilder sb = new StringBuilder();
        // sb.repeat('0', 3600);
        // // Profiler.begin();
        // char c1 = sb.charAt(myLocX * 60 + myLocY);
        // // Profiler.end_print("indx");


        // x = myLocX;
        // y = myLocY;
        // // Profiler.begin();
        // boolean b0 = rc.senseMapInfo(new MapLocation(x, y)).isPassable();
        // // Profiler.end_print("sense new maploc");

        // MapLocation loc = new MapLocation(x, y);
        // // Profiler.begin();
        // boolean b1 = rc.senseMapInfo(loc).isPassable();
        // // Profiler.end_print("sense existing maploc");

        // rc.resign();
    }

    public static void preTick() throws GameActionException {
        time = rc.getRoundNum();
        myLoc = rc.getLocation();
        myLocX = myLoc.x;
        myLocY = myLoc.y;
        myDir = rc.getDirection();
        // Clear indicator string at the start of each turn
        // indicatorString = "";
        // StringBuilder sb = new StringBuilder();
        if (rc.getCarrying() == null) carryStartTime = -1;

        // // Profiler.begin();   
        // for (int i = nearbyRobots.length - 1; i >= 0; i--) {
        //     RobotInfo ri = nearbyRobots[i];
        // }
        // // Profiler.end_print("                  index based for loop " + nearbyRobots.length);
        // rc.resign();
    }

    public static void postTick() throws GameActionException {
        myPreviousLoc = myLoc;
        lastHP = rc.getHealth();
        if (rc.getCarrying() == null) carryStartTime = -1;
        else if (carryStartTime == -1) carryStartTime = time;
        // // Profiler.begin();
        // MapInfo info = rc.senseMapInfo(myLoc);
        // // Profiler.end_print("sense");

        // // Profiler.begin();
        // boolean asdf = rc.senseMapInfo(new MapLocation(myLocX, myLocY)).isPassable();
        // // Profiler.end_print("passable");

        // // Profiler.begin();
        // boolean fdas = CellTracker.isPassable(myLocX, myLocY);
        // // Profiler.end_print("celltracker");

        // StringBuilder sb = new StringBuilder();
        // sb.repeat('1', 1000);
        // // Profiler.begin();
        // int ans = sb.charAt(myLocX * 60 + myLocY);
        // // Profiler.end_print("thoery");

        // rc.resign();

        // if (!isRatKing) println("index: " + myIndex);
        // if (time > 40) rc.resign();
        // if (time != rc.getRoundNum()) {
        //     println("bytecode exceeded");
        //     rc.resign();
        // }
    }
}
