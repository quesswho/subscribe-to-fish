





package awubot;

import awubot.util.string.FastMapLocationSet;
import awubot.comms.GlobalArray;
import battlecode.common.*;
import static awubot.Game.*;
import static awubot.util.Util.*;
import awubot.util.Profiler;public class RobotTracker {

    private static StringBuilder baseString = new StringBuilder("\u0FFF\u1000\u1001\u1002\u1003\u1004\u1005\u1006\u1007\u1008\u1009\u100A\u100B\u100C\u100D\u100E\u100F\u1010\u1011\u1012\u1013\u1014\u1015\u1016\u1017\u1018\u1019\u101A\u101B\u101C\u101D\u101E\u101F\u1020\u1021\u1022\u1023\u1024\u1025\u1026\u1027\u1028\u1029");
    public static RobotInfo[] sortedRobots = new RobotInfo[25];
    public static RobotInfo[] sortedEnemies = new RobotInfo[25];
    public static RobotInfo[] sortedAllies = new RobotInfo[25];
    public static RobotInfo[] sortedCats = new RobotInfo[10];
    public static int numRobots, numEnemies, numAllies, numCats;
    private static char DELIM_CHAR = '\u0FFF';
    private static String DELIM_STRING = "\u0FFF";

    public static void init() {
    }

    public static void preTick() {
        // println("preTick");
        updateRobots();
    }

    public static void updateRobots() {
        if (team == Team.A) {
            updateRobotsA();
        } else {
            updateRobotsB();
        }
    }

    public static void updateRobotsA() {
        // kind of a hack job
        allyCount = 0;
        allySet = new FastMapLocationSet();
        kingSet = new FastMapLocationSet();

        // // Profiler.begin();
        StringBuilder sb = new StringBuilder(baseString);
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        numRobots = nearbyRobots.length;
        numEnemies = numAllies = numCats = 0;
        switch (numRobots) {
            case 43: {
                int d = nearbyRobots[42].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x002A);
                switch (nearbyRobots[42].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 42: {
                int d = nearbyRobots[41].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0029);
                switch (nearbyRobots[41].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 41: {
                int d = nearbyRobots[40].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0028);
                switch (nearbyRobots[40].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 40: {
                int d = nearbyRobots[39].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, '\''          );
                switch (nearbyRobots[39].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 39: {
                int d = nearbyRobots[38].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0026);
                switch (nearbyRobots[38].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 38: {
                int d = nearbyRobots[37].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0025);
                switch (nearbyRobots[37].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 37: {
                int d = nearbyRobots[36].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0024);
                switch (nearbyRobots[36].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 36: {
                int d = nearbyRobots[35].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0023);
                switch (nearbyRobots[35].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 35: {
                int d = nearbyRobots[34].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0022);
                switch (nearbyRobots[34].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 34: {
                int d = nearbyRobots[33].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0021);
                switch (nearbyRobots[33].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 33: {
                int d = nearbyRobots[32].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0020);
                switch (nearbyRobots[32].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 32: {
                int d = nearbyRobots[31].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x001F);
                switch (nearbyRobots[31].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 31: {
                int d = nearbyRobots[30].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x001E);
                switch (nearbyRobots[30].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 30: {
                int d = nearbyRobots[29].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x001D);
                switch (nearbyRobots[29].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 29: {
                int d = nearbyRobots[28].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x001C);
                switch (nearbyRobots[28].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 28: {
                int d = nearbyRobots[27].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x001B);
                switch (nearbyRobots[27].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 27: {
                int d = nearbyRobots[26].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x001A);
                switch (nearbyRobots[26].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 26: {
                int d = nearbyRobots[25].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0019);
                switch (nearbyRobots[25].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 25: {
                int d = nearbyRobots[24].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0018);
                switch (nearbyRobots[24].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 24: {
                int d = nearbyRobots[23].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0017);
                switch (nearbyRobots[23].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 23: {
                int d = nearbyRobots[22].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0016);
                switch (nearbyRobots[22].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 22: {
                int d = nearbyRobots[21].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0015);
                switch (nearbyRobots[21].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 21: {
                int d = nearbyRobots[20].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0014);
                switch (nearbyRobots[20].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 20: {
                int d = nearbyRobots[19].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0013);
                switch (nearbyRobots[19].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 19: {
                int d = nearbyRobots[18].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0012);
                switch (nearbyRobots[18].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 18: {
                int d = nearbyRobots[17].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0011);
                switch (nearbyRobots[17].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 17: {
                int d = nearbyRobots[16].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0010);
                switch (nearbyRobots[16].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 16: {
                int d = nearbyRobots[15].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x000F);
                switch (nearbyRobots[15].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 15: {
                int d = nearbyRobots[14].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x000E);
                switch (nearbyRobots[14].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 14: {
                int d = nearbyRobots[13].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, '\r');
                switch (nearbyRobots[13].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 13: {
                int d = nearbyRobots[12].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, '\f');
                switch (nearbyRobots[12].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 12: {
                int d = nearbyRobots[11].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x000B);
                switch (nearbyRobots[11].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 11: {
                int d = nearbyRobots[10].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, '\n');
                switch (nearbyRobots[10].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 10: {
                int d = nearbyRobots[9].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, '\t');
                switch (nearbyRobots[9].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 9: {
                int d = nearbyRobots[8].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, '\b');
                switch (nearbyRobots[8].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 8: {
                int d = nearbyRobots[7].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0007);
                switch (nearbyRobots[7].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 7: {
                int d = nearbyRobots[6].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0006);
                switch (nearbyRobots[6].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 6: {
                int d = nearbyRobots[5].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0005);
                switch (nearbyRobots[5].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 5: {
                int d = nearbyRobots[4].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0004);
                switch (nearbyRobots[4].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 4: {
                int d = nearbyRobots[3].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0003);
                switch (nearbyRobots[3].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 3: {
                int d = nearbyRobots[2].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0002);
                switch (nearbyRobots[2].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 2: {
                int d = nearbyRobots[1].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0001);
                switch (nearbyRobots[1].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 1: {
                int d = nearbyRobots[0].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0000);
                switch (nearbyRobots[0].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 0:
                        numAllies++;
                        break;
                    case 1:
                        numEnemies++;
                        break;
                }
            }
            case 0:
                break;
            default:
                assert(false);
                break;
        }
        int i;
        sb.deleteCharAt(sb.indexOf("\u0FFF"));
        sb.deleteCharAt(sb.indexOf("\u1000"));
        sb.deleteCharAt(sb.indexOf("\u1001"));
        sb.deleteCharAt(sb.indexOf("\u1002"));
        sb.deleteCharAt(sb.indexOf("\u1003"));
        sb.deleteCharAt(sb.indexOf("\u1004"));
        sb.deleteCharAt(sb.indexOf("\u1005"));
        sb.deleteCharAt(sb.indexOf("\u1006"));
        sb.deleteCharAt(sb.indexOf("\u1007"));
        sb.deleteCharAt(sb.indexOf("\u1008"));
        sb.deleteCharAt(sb.indexOf("\u1009"));
        sb.deleteCharAt(sb.indexOf("\u100A"));
        sb.deleteCharAt(sb.indexOf("\u100B"));
        sb.deleteCharAt(sb.indexOf("\u100C"));
        sb.deleteCharAt(sb.indexOf("\u100D"));
        sb.deleteCharAt(sb.indexOf("\u100E"));
        sb.deleteCharAt(sb.indexOf("\u100F"));
        sb.deleteCharAt(sb.indexOf("\u1010"));
        sb.deleteCharAt(sb.indexOf("\u1011"));
        sb.deleteCharAt(sb.indexOf("\u1012"));
        sb.deleteCharAt(sb.indexOf("\u1013"));
        sb.deleteCharAt(sb.indexOf("\u1014"));
        sb.deleteCharAt(sb.indexOf("\u1015"));
        sb.deleteCharAt(sb.indexOf("\u1016"));
        sb.deleteCharAt(sb.indexOf("\u1017"));
        sb.deleteCharAt(sb.indexOf("\u1018"));
        sb.deleteCharAt(sb.indexOf("\u1019"));
        sb.deleteCharAt(sb.indexOf("\u101A"));
        sb.deleteCharAt(sb.indexOf("\u101B"));
        sb.deleteCharAt(sb.indexOf("\u101C"));
        sb.deleteCharAt(sb.indexOf("\u101D"));
        sb.deleteCharAt(sb.indexOf("\u101E"));
        sb.deleteCharAt(sb.indexOf("\u101F"));
        sb.deleteCharAt(sb.indexOf("\u1020"));
        sb.deleteCharAt(sb.indexOf("\u1021"));
        sb.deleteCharAt(sb.indexOf("\u1022"));
        sb.deleteCharAt(sb.indexOf("\u1023"));
        sb.deleteCharAt(sb.indexOf("\u1024"));
        sb.deleteCharAt(sb.indexOf("\u1025"));
        sb.deleteCharAt(sb.indexOf("\u1026"));
        sb.deleteCharAt(sb.indexOf("\u1027"));
        sb.deleteCharAt(sb.indexOf("\u1028"));
        sb.deleteCharAt(sb.indexOf("\u1029"));

        int nextAlly = numAllies;
        int nextEnemy = numEnemies;
        int nextCat = numCats;
        switch (numRobots) {
            case 43: {
                sortedRobots[42] =  nearbyRobots[(int) sb.charAt(42)];
                switch (sortedRobots[42].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[42];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[42];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[42];
                        break;
                }
            }
            case 42: {
                sortedRobots[41] =  nearbyRobots[(int) sb.charAt(41)];
                switch (sortedRobots[41].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[41];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[41];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[41];
                        break;
                }
            }
            case 41: {
                sortedRobots[40] =  nearbyRobots[(int) sb.charAt(40)];
                switch (sortedRobots[40].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[40];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[40];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[40];
                        break;
                }
            }
            case 40: {
                sortedRobots[39] =  nearbyRobots[(int) sb.charAt(39)];
                switch (sortedRobots[39].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[39];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[39];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[39];
                        break;
                }
            }
            case 39: {
                sortedRobots[38] =  nearbyRobots[(int) sb.charAt(38)];
                switch (sortedRobots[38].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[38];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[38];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[38];
                        break;
                }
            }
            case 38: {
                sortedRobots[37] =  nearbyRobots[(int) sb.charAt(37)];
                switch (sortedRobots[37].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[37];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[37];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[37];
                        break;
                }
            }
            case 37: {
                sortedRobots[36] =  nearbyRobots[(int) sb.charAt(36)];
                switch (sortedRobots[36].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[36];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[36];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[36];
                        break;
                }
            }
            case 36: {
                sortedRobots[35] =  nearbyRobots[(int) sb.charAt(35)];
                switch (sortedRobots[35].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[35];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[35];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[35];
                        break;
                }
            }
            case 35: {
                sortedRobots[34] =  nearbyRobots[(int) sb.charAt(34)];
                switch (sortedRobots[34].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[34];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[34];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[34];
                        break;
                }
            }
            case 34: {
                sortedRobots[33] =  nearbyRobots[(int) sb.charAt(33)];
                switch (sortedRobots[33].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[33];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[33];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[33];
                        break;
                }
            }
            case 33: {
                sortedRobots[32] =  nearbyRobots[(int) sb.charAt(32)];
                switch (sortedRobots[32].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[32];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[32];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[32];
                        break;
                }
            }
            case 32: {
                sortedRobots[31] =  nearbyRobots[(int) sb.charAt(31)];
                switch (sortedRobots[31].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[31];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[31];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[31];
                        break;
                }
            }
            case 31: {
                sortedRobots[30] =  nearbyRobots[(int) sb.charAt(30)];
                switch (sortedRobots[30].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[30];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[30];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[30];
                        break;
                }
            }
            case 30: {
                sortedRobots[29] =  nearbyRobots[(int) sb.charAt(29)];
                switch (sortedRobots[29].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[29];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[29];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[29];
                        break;
                }
            }
            case 29: {
                sortedRobots[28] =  nearbyRobots[(int) sb.charAt(28)];
                switch (sortedRobots[28].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[28];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[28];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[28];
                        break;
                }
            }
            case 28: {
                sortedRobots[27] =  nearbyRobots[(int) sb.charAt(27)];
                switch (sortedRobots[27].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[27];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[27];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[27];
                        break;
                }
            }
            case 27: {
                sortedRobots[26] =  nearbyRobots[(int) sb.charAt(26)];
                switch (sortedRobots[26].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[26];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[26];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[26];
                        break;
                }
            }
            case 26: {
                sortedRobots[25] =  nearbyRobots[(int) sb.charAt(25)];
                switch (sortedRobots[25].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[25];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[25];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[25];
                        break;
                }
            }
            case 25: {
                sortedRobots[24] =  nearbyRobots[(int) sb.charAt(24)];
                switch (sortedRobots[24].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[24];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[24];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[24];
                        break;
                }
            }
            case 24: {
                sortedRobots[23] =  nearbyRobots[(int) sb.charAt(23)];
                switch (sortedRobots[23].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[23];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[23];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[23];
                        break;
                }
            }
            case 23: {
                sortedRobots[22] =  nearbyRobots[(int) sb.charAt(22)];
                switch (sortedRobots[22].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[22];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[22];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[22];
                        break;
                }
            }
            case 22: {
                sortedRobots[21] =  nearbyRobots[(int) sb.charAt(21)];
                switch (sortedRobots[21].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[21];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[21];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[21];
                        break;
                }
            }
            case 21: {
                sortedRobots[20] =  nearbyRobots[(int) sb.charAt(20)];
                switch (sortedRobots[20].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[20];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[20];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[20];
                        break;
                }
            }
            case 20: {
                sortedRobots[19] =  nearbyRobots[(int) sb.charAt(19)];
                switch (sortedRobots[19].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[19];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[19];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[19];
                        break;
                }
            }
            case 19: {
                sortedRobots[18] =  nearbyRobots[(int) sb.charAt(18)];
                switch (sortedRobots[18].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[18];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[18];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[18];
                        break;
                }
            }
            case 18: {
                sortedRobots[17] =  nearbyRobots[(int) sb.charAt(17)];
                switch (sortedRobots[17].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[17];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[17];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[17];
                        break;
                }
            }
            case 17: {
                sortedRobots[16] =  nearbyRobots[(int) sb.charAt(16)];
                switch (sortedRobots[16].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[16];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[16];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[16];
                        break;
                }
            }
            case 16: {
                sortedRobots[15] =  nearbyRobots[(int) sb.charAt(15)];
                switch (sortedRobots[15].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[15];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[15];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[15];
                        break;
                }
            }
            case 15: {
                sortedRobots[14] =  nearbyRobots[(int) sb.charAt(14)];
                switch (sortedRobots[14].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[14];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[14];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[14];
                        break;
                }
            }
            case 14: {
                sortedRobots[13] =  nearbyRobots[(int) sb.charAt(13)];
                switch (sortedRobots[13].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[13];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[13];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[13];
                        break;
                }
            }
            case 13: {
                sortedRobots[12] =  nearbyRobots[(int) sb.charAt(12)];
                switch (sortedRobots[12].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[12];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[12];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[12];
                        break;
                }
            }
            case 12: {
                sortedRobots[11] =  nearbyRobots[(int) sb.charAt(11)];
                switch (sortedRobots[11].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[11];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[11];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[11];
                        break;
                }
            }
            case 11: {
                sortedRobots[10] =  nearbyRobots[(int) sb.charAt(10)];
                switch (sortedRobots[10].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[10];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[10];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[10];
                        break;
                }
            }
            case 10: {
                sortedRobots[9] =  nearbyRobots[(int) sb.charAt(9)];
                switch (sortedRobots[9].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[9];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[9];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[9];
                        break;
                }
            }
            case 9: {
                sortedRobots[8] =  nearbyRobots[(int) sb.charAt(8)];
                switch (sortedRobots[8].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[8];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[8];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[8];
                        break;
                }
            }
            case 8: {
                sortedRobots[7] =  nearbyRobots[(int) sb.charAt(7)];
                switch (sortedRobots[7].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[7];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[7];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[7];
                        break;
                }
            }
            case 7: {
                sortedRobots[6] =  nearbyRobots[(int) sb.charAt(6)];
                switch (sortedRobots[6].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[6];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[6];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[6];
                        break;
                }
            }
            case 6: {
                sortedRobots[5] =  nearbyRobots[(int) sb.charAt(5)];
                switch (sortedRobots[5].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[5];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[5];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[5];
                        break;
                }
            }
            case 5: {
                sortedRobots[4] =  nearbyRobots[(int) sb.charAt(4)];
                switch (sortedRobots[4].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[4];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[4];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[4];
                        break;
                }
            }
            case 4: {
                sortedRobots[3] =  nearbyRobots[(int) sb.charAt(3)];
                switch (sortedRobots[3].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[3];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[3];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[3];
                        break;
                }
            }
            case 3: {
                sortedRobots[2] =  nearbyRobots[(int) sb.charAt(2)];
                switch (sortedRobots[2].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[2];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[2];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[2];
                        break;
                }
            }
            case 2: {
                sortedRobots[1] =  nearbyRobots[(int) sb.charAt(1)];
                switch (sortedRobots[1].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[1];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[1];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[1];
                        break;
                }
            }
            case 1: {
                sortedRobots[0] =  nearbyRobots[(int) sb.charAt(0)];
                switch (sortedRobots[0].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[0];
                        break;
                    case 0:
                        sortedAllies[--nextAlly] = sortedRobots[0];
                        break;
                    case 1:
                        sortedEnemies[--nextEnemy] = sortedRobots[0];
                        break;
                }
            }
            case 0:
                break;
            default:
                assert(false);
                break;
        }
        // // Profiler.end_print("updateRobots");
    }
    public static void updateRobotsB() {
        // kind of a hack job
        allyCount = 0;
        allySet = new FastMapLocationSet();
        kingSet = new FastMapLocationSet();

        // // Profiler.begin();
        StringBuilder sb = new StringBuilder(baseString);
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        numRobots = nearbyRobots.length;
        numEnemies = numAllies = numCats = 0;
        switch (numRobots) {
            case 43: {
                int d = nearbyRobots[42].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x002A);
                switch (nearbyRobots[42].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 42: {
                int d = nearbyRobots[41].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0029);
                switch (nearbyRobots[41].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 41: {
                int d = nearbyRobots[40].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0028);
                switch (nearbyRobots[40].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 40: {
                int d = nearbyRobots[39].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, '\''          );
                switch (nearbyRobots[39].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 39: {
                int d = nearbyRobots[38].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0026);
                switch (nearbyRobots[38].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 38: {
                int d = nearbyRobots[37].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0025);
                switch (nearbyRobots[37].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 37: {
                int d = nearbyRobots[36].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0024);
                switch (nearbyRobots[36].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 36: {
                int d = nearbyRobots[35].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0023);
                switch (nearbyRobots[35].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 35: {
                int d = nearbyRobots[34].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0022);
                switch (nearbyRobots[34].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 34: {
                int d = nearbyRobots[33].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0021);
                switch (nearbyRobots[33].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 33: {
                int d = nearbyRobots[32].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0020);
                switch (nearbyRobots[32].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 32: {
                int d = nearbyRobots[31].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x001F);
                switch (nearbyRobots[31].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 31: {
                int d = nearbyRobots[30].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x001E);
                switch (nearbyRobots[30].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 30: {
                int d = nearbyRobots[29].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x001D);
                switch (nearbyRobots[29].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 29: {
                int d = nearbyRobots[28].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x001C);
                switch (nearbyRobots[28].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 28: {
                int d = nearbyRobots[27].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x001B);
                switch (nearbyRobots[27].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 27: {
                int d = nearbyRobots[26].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x001A);
                switch (nearbyRobots[26].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 26: {
                int d = nearbyRobots[25].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0019);
                switch (nearbyRobots[25].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 25: {
                int d = nearbyRobots[24].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0018);
                switch (nearbyRobots[24].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 24: {
                int d = nearbyRobots[23].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0017);
                switch (nearbyRobots[23].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 23: {
                int d = nearbyRobots[22].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0016);
                switch (nearbyRobots[22].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 22: {
                int d = nearbyRobots[21].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0015);
                switch (nearbyRobots[21].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 21: {
                int d = nearbyRobots[20].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0014);
                switch (nearbyRobots[20].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 20: {
                int d = nearbyRobots[19].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0013);
                switch (nearbyRobots[19].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 19: {
                int d = nearbyRobots[18].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0012);
                switch (nearbyRobots[18].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 18: {
                int d = nearbyRobots[17].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0011);
                switch (nearbyRobots[17].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 17: {
                int d = nearbyRobots[16].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0010);
                switch (nearbyRobots[16].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 16: {
                int d = nearbyRobots[15].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x000F);
                switch (nearbyRobots[15].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 15: {
                int d = nearbyRobots[14].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x000E);
                switch (nearbyRobots[14].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 14: {
                int d = nearbyRobots[13].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, '\r');
                switch (nearbyRobots[13].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 13: {
                int d = nearbyRobots[12].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, '\f');
                switch (nearbyRobots[12].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 12: {
                int d = nearbyRobots[11].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x000B);
                switch (nearbyRobots[11].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 11: {
                int d = nearbyRobots[10].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, '\n');
                switch (nearbyRobots[10].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 10: {
                int d = nearbyRobots[9].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, '\t');
                switch (nearbyRobots[9].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 9: {
                int d = nearbyRobots[8].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, '\b');
                switch (nearbyRobots[8].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 8: {
                int d = nearbyRobots[7].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0007);
                switch (nearbyRobots[7].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 7: {
                int d = nearbyRobots[6].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0006);
                switch (nearbyRobots[6].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 6: {
                int d = nearbyRobots[5].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0005);
                switch (nearbyRobots[5].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 5: {
                int d = nearbyRobots[4].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0004);
                switch (nearbyRobots[4].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 4: {
                int d = nearbyRobots[3].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0003);
                switch (nearbyRobots[3].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 3: {
                int d = nearbyRobots[2].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0002);
                switch (nearbyRobots[2].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 2: {
                int d = nearbyRobots[1].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0001);
                switch (nearbyRobots[1].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 1: {
                int d = nearbyRobots[0].location.distanceSquaredTo(myLoc);
                int j = sb.indexOf(String.valueOf((char) (4095 + d)));
                sb.insert(j, (char)0x0000);
                switch (nearbyRobots[0].team.ordinal()) {
                    case 2:
                        numCats++;
                        break;
                    case 1:
                        numAllies++;
                        break;
                    case 0:
                        numEnemies++;
                        break;
                }
            }
            case 0:
                break;
            default:
                assert(false);
                break;
        }
        int i;
        sb.deleteCharAt(sb.indexOf("\u0FFF"));
        sb.deleteCharAt(sb.indexOf("\u1000"));
        sb.deleteCharAt(sb.indexOf("\u1001"));
        sb.deleteCharAt(sb.indexOf("\u1002"));
        sb.deleteCharAt(sb.indexOf("\u1003"));
        sb.deleteCharAt(sb.indexOf("\u1004"));
        sb.deleteCharAt(sb.indexOf("\u1005"));
        sb.deleteCharAt(sb.indexOf("\u1006"));
        sb.deleteCharAt(sb.indexOf("\u1007"));
        sb.deleteCharAt(sb.indexOf("\u1008"));
        sb.deleteCharAt(sb.indexOf("\u1009"));
        sb.deleteCharAt(sb.indexOf("\u100A"));
        sb.deleteCharAt(sb.indexOf("\u100B"));
        sb.deleteCharAt(sb.indexOf("\u100C"));
        sb.deleteCharAt(sb.indexOf("\u100D"));
        sb.deleteCharAt(sb.indexOf("\u100E"));
        sb.deleteCharAt(sb.indexOf("\u100F"));
        sb.deleteCharAt(sb.indexOf("\u1010"));
        sb.deleteCharAt(sb.indexOf("\u1011"));
        sb.deleteCharAt(sb.indexOf("\u1012"));
        sb.deleteCharAt(sb.indexOf("\u1013"));
        sb.deleteCharAt(sb.indexOf("\u1014"));
        sb.deleteCharAt(sb.indexOf("\u1015"));
        sb.deleteCharAt(sb.indexOf("\u1016"));
        sb.deleteCharAt(sb.indexOf("\u1017"));
        sb.deleteCharAt(sb.indexOf("\u1018"));
        sb.deleteCharAt(sb.indexOf("\u1019"));
        sb.deleteCharAt(sb.indexOf("\u101A"));
        sb.deleteCharAt(sb.indexOf("\u101B"));
        sb.deleteCharAt(sb.indexOf("\u101C"));
        sb.deleteCharAt(sb.indexOf("\u101D"));
        sb.deleteCharAt(sb.indexOf("\u101E"));
        sb.deleteCharAt(sb.indexOf("\u101F"));
        sb.deleteCharAt(sb.indexOf("\u1020"));
        sb.deleteCharAt(sb.indexOf("\u1021"));
        sb.deleteCharAt(sb.indexOf("\u1022"));
        sb.deleteCharAt(sb.indexOf("\u1023"));
        sb.deleteCharAt(sb.indexOf("\u1024"));
        sb.deleteCharAt(sb.indexOf("\u1025"));
        sb.deleteCharAt(sb.indexOf("\u1026"));
        sb.deleteCharAt(sb.indexOf("\u1027"));
        sb.deleteCharAt(sb.indexOf("\u1028"));
        sb.deleteCharAt(sb.indexOf("\u1029"));

        int nextAlly = numAllies;
        int nextEnemy = numEnemies;
        int nextCat = numCats;
        switch (numRobots) {
            case 43: {
                sortedRobots[42] =  nearbyRobots[(int) sb.charAt(42)];
                switch (sortedRobots[42].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[42];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[42];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[42];
                        break;
                }
            }
            case 42: {
                sortedRobots[41] =  nearbyRobots[(int) sb.charAt(41)];
                switch (sortedRobots[41].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[41];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[41];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[41];
                        break;
                }
            }
            case 41: {
                sortedRobots[40] =  nearbyRobots[(int) sb.charAt(40)];
                switch (sortedRobots[40].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[40];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[40];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[40];
                        break;
                }
            }
            case 40: {
                sortedRobots[39] =  nearbyRobots[(int) sb.charAt(39)];
                switch (sortedRobots[39].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[39];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[39];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[39];
                        break;
                }
            }
            case 39: {
                sortedRobots[38] =  nearbyRobots[(int) sb.charAt(38)];
                switch (sortedRobots[38].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[38];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[38];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[38];
                        break;
                }
            }
            case 38: {
                sortedRobots[37] =  nearbyRobots[(int) sb.charAt(37)];
                switch (sortedRobots[37].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[37];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[37];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[37];
                        break;
                }
            }
            case 37: {
                sortedRobots[36] =  nearbyRobots[(int) sb.charAt(36)];
                switch (sortedRobots[36].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[36];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[36];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[36];
                        break;
                }
            }
            case 36: {
                sortedRobots[35] =  nearbyRobots[(int) sb.charAt(35)];
                switch (sortedRobots[35].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[35];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[35];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[35];
                        break;
                }
            }
            case 35: {
                sortedRobots[34] =  nearbyRobots[(int) sb.charAt(34)];
                switch (sortedRobots[34].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[34];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[34];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[34];
                        break;
                }
            }
            case 34: {
                sortedRobots[33] =  nearbyRobots[(int) sb.charAt(33)];
                switch (sortedRobots[33].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[33];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[33];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[33];
                        break;
                }
            }
            case 33: {
                sortedRobots[32] =  nearbyRobots[(int) sb.charAt(32)];
                switch (sortedRobots[32].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[32];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[32];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[32];
                        break;
                }
            }
            case 32: {
                sortedRobots[31] =  nearbyRobots[(int) sb.charAt(31)];
                switch (sortedRobots[31].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[31];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[31];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[31];
                        break;
                }
            }
            case 31: {
                sortedRobots[30] =  nearbyRobots[(int) sb.charAt(30)];
                switch (sortedRobots[30].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[30];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[30];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[30];
                        break;
                }
            }
            case 30: {
                sortedRobots[29] =  nearbyRobots[(int) sb.charAt(29)];
                switch (sortedRobots[29].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[29];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[29];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[29];
                        break;
                }
            }
            case 29: {
                sortedRobots[28] =  nearbyRobots[(int) sb.charAt(28)];
                switch (sortedRobots[28].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[28];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[28];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[28];
                        break;
                }
            }
            case 28: {
                sortedRobots[27] =  nearbyRobots[(int) sb.charAt(27)];
                switch (sortedRobots[27].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[27];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[27];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[27];
                        break;
                }
            }
            case 27: {
                sortedRobots[26] =  nearbyRobots[(int) sb.charAt(26)];
                switch (sortedRobots[26].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[26];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[26];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[26];
                        break;
                }
            }
            case 26: {
                sortedRobots[25] =  nearbyRobots[(int) sb.charAt(25)];
                switch (sortedRobots[25].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[25];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[25];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[25];
                        break;
                }
            }
            case 25: {
                sortedRobots[24] =  nearbyRobots[(int) sb.charAt(24)];
                switch (sortedRobots[24].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[24];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[24];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[24];
                        break;
                }
            }
            case 24: {
                sortedRobots[23] =  nearbyRobots[(int) sb.charAt(23)];
                switch (sortedRobots[23].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[23];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[23];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[23];
                        break;
                }
            }
            case 23: {
                sortedRobots[22] =  nearbyRobots[(int) sb.charAt(22)];
                switch (sortedRobots[22].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[22];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[22];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[22];
                        break;
                }
            }
            case 22: {
                sortedRobots[21] =  nearbyRobots[(int) sb.charAt(21)];
                switch (sortedRobots[21].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[21];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[21];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[21];
                        break;
                }
            }
            case 21: {
                sortedRobots[20] =  nearbyRobots[(int) sb.charAt(20)];
                switch (sortedRobots[20].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[20];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[20];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[20];
                        break;
                }
            }
            case 20: {
                sortedRobots[19] =  nearbyRobots[(int) sb.charAt(19)];
                switch (sortedRobots[19].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[19];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[19];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[19];
                        break;
                }
            }
            case 19: {
                sortedRobots[18] =  nearbyRobots[(int) sb.charAt(18)];
                switch (sortedRobots[18].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[18];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[18];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[18];
                        break;
                }
            }
            case 18: {
                sortedRobots[17] =  nearbyRobots[(int) sb.charAt(17)];
                switch (sortedRobots[17].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[17];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[17];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[17];
                        break;
                }
            }
            case 17: {
                sortedRobots[16] =  nearbyRobots[(int) sb.charAt(16)];
                switch (sortedRobots[16].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[16];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[16];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[16];
                        break;
                }
            }
            case 16: {
                sortedRobots[15] =  nearbyRobots[(int) sb.charAt(15)];
                switch (sortedRobots[15].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[15];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[15];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[15];
                        break;
                }
            }
            case 15: {
                sortedRobots[14] =  nearbyRobots[(int) sb.charAt(14)];
                switch (sortedRobots[14].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[14];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[14];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[14];
                        break;
                }
            }
            case 14: {
                sortedRobots[13] =  nearbyRobots[(int) sb.charAt(13)];
                switch (sortedRobots[13].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[13];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[13];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[13];
                        break;
                }
            }
            case 13: {
                sortedRobots[12] =  nearbyRobots[(int) sb.charAt(12)];
                switch (sortedRobots[12].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[12];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[12];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[12];
                        break;
                }
            }
            case 12: {
                sortedRobots[11] =  nearbyRobots[(int) sb.charAt(11)];
                switch (sortedRobots[11].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[11];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[11];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[11];
                        break;
                }
            }
            case 11: {
                sortedRobots[10] =  nearbyRobots[(int) sb.charAt(10)];
                switch (sortedRobots[10].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[10];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[10];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[10];
                        break;
                }
            }
            case 10: {
                sortedRobots[9] =  nearbyRobots[(int) sb.charAt(9)];
                switch (sortedRobots[9].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[9];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[9];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[9];
                        break;
                }
            }
            case 9: {
                sortedRobots[8] =  nearbyRobots[(int) sb.charAt(8)];
                switch (sortedRobots[8].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[8];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[8];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[8];
                        break;
                }
            }
            case 8: {
                sortedRobots[7] =  nearbyRobots[(int) sb.charAt(7)];
                switch (sortedRobots[7].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[7];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[7];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[7];
                        break;
                }
            }
            case 7: {
                sortedRobots[6] =  nearbyRobots[(int) sb.charAt(6)];
                switch (sortedRobots[6].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[6];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[6];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[6];
                        break;
                }
            }
            case 6: {
                sortedRobots[5] =  nearbyRobots[(int) sb.charAt(5)];
                switch (sortedRobots[5].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[5];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[5];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[5];
                        break;
                }
            }
            case 5: {
                sortedRobots[4] =  nearbyRobots[(int) sb.charAt(4)];
                switch (sortedRobots[4].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[4];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[4];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[4];
                        break;
                }
            }
            case 4: {
                sortedRobots[3] =  nearbyRobots[(int) sb.charAt(3)];
                switch (sortedRobots[3].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[3];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[3];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[3];
                        break;
                }
            }
            case 3: {
                sortedRobots[2] =  nearbyRobots[(int) sb.charAt(2)];
                switch (sortedRobots[2].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[2];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[2];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[2];
                        break;
                }
            }
            case 2: {
                sortedRobots[1] =  nearbyRobots[(int) sb.charAt(1)];
                switch (sortedRobots[1].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[1];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[1];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[1];
                        break;
                }
            }
            case 1: {
                sortedRobots[0] =  nearbyRobots[(int) sb.charAt(0)];
                switch (sortedRobots[0].team.ordinal()) {
                    case 2:
                        sortedCats[--nextCat] = sortedRobots[0];
                        break;
                    case 1:
                        sortedAllies[--nextAlly] = sortedRobots[0];
                        break;
                    case 0:
                        sortedEnemies[--nextEnemy] = sortedRobots[0];
                        break;
                }
            }
            case 0:
                break;
            default:
                assert(false);
                break;
        }
        // // Profiler.end_print("updateRobots");
    }

    public static RobotInfo[][] catHist = new RobotInfo[4][10];
    public static int[] catHistNum = new int[4];
    public static void updateHistoricalCatLocs() throws GameActionException {
        for(int i = 0; i < numCats; i++) {
            catHist[time % 4][i] = sortedCats[i];
        }
        catHistNum[time % 4] = numCats;
    }

    public static int allyCount = 0;
    public static MapLocation[] allies = new MapLocation[120];
    public static FastMapLocationSet allySet, kingSet;
    public static void addAlly(MapLocation loc) {
        if(!allySet.contains(loc)) {
            allies[allyCount++] = loc;
            allySet.add(loc);
        }
    }

    // IF TLE: optimise this
    public static void reset() throws GameActionException {
        for(var robot : rc.senseNearbyRobots(-1, team)) {
            if(robot.type == UnitType.BABY_RAT) {
                addAlly(robot.location);
            }
        }
        {
            var kingLoc = GlobalArray.getRatKing(0);
            if(kingLoc != null) {
                if(GlobalArray.isKingExpanded(0)) {
                    kingSet.add(kingLoc.translate(-2, -2));
                    kingSet.add(kingLoc.translate(-2, -1));
                    kingSet.add(kingLoc.translate(-2, 0));
                    kingSet.add(kingLoc.translate(-2, 1));
                    kingSet.add(kingLoc.translate(-2, 2));
                    kingSet.add(kingLoc.translate(-1, -2));
                    kingSet.add(kingLoc.translate(-1, -1));
                    kingSet.add(kingLoc.translate(-1, 0));
                    kingSet.add(kingLoc.translate(-1, 1));
                    kingSet.add(kingLoc.translate(-1, 2));
                    kingSet.add(kingLoc.translate(0, -2));
                    kingSet.add(kingLoc.translate(0, -1));
                    kingSet.add(kingLoc.translate(0, 0));
                    kingSet.add(kingLoc.translate(0, 1));
                    kingSet.add(kingLoc.translate(0, 2));
                    kingSet.add(kingLoc.translate(1, -2));
                    kingSet.add(kingLoc.translate(1, -1));
                    kingSet.add(kingLoc.translate(1, 0));
                    kingSet.add(kingLoc.translate(1, 1));
                    kingSet.add(kingLoc.translate(1, 2));
                    kingSet.add(kingLoc.translate(2, -2));
                    kingSet.add(kingLoc.translate(2, -1));
                    kingSet.add(kingLoc.translate(2, 0));
                    kingSet.add(kingLoc.translate(2, 1));
                    kingSet.add(kingLoc.translate(2, 2));
                } else {
                    kingSet.add(kingLoc.translate(-1, -1));
                    kingSet.add(kingLoc.translate(-1, 0));
                    kingSet.add(kingLoc.translate(-1, 1));
                    kingSet.add(kingLoc.translate(0, -1));
                    kingSet.add(kingLoc.translate(0, 0));
                    kingSet.add(kingLoc.translate(0, 1));
                    kingSet.add(kingLoc.translate(1, -1));
                    kingSet.add(kingLoc.translate(1, 0));
                    kingSet.add(kingLoc.translate(1, 1));
                }
            }
        }
        {
            var kingLoc = GlobalArray.getRatKing(1);
            if(kingLoc != null) {
                if(GlobalArray.isKingExpanded(1)) {
                    kingSet.add(kingLoc.translate(-2, -2));
                    kingSet.add(kingLoc.translate(-2, -1));
                    kingSet.add(kingLoc.translate(-2, 0));
                    kingSet.add(kingLoc.translate(-2, 1));
                    kingSet.add(kingLoc.translate(-2, 2));
                    kingSet.add(kingLoc.translate(-1, -2));
                    kingSet.add(kingLoc.translate(-1, -1));
                    kingSet.add(kingLoc.translate(-1, 0));
                    kingSet.add(kingLoc.translate(-1, 1));
                    kingSet.add(kingLoc.translate(-1, 2));
                    kingSet.add(kingLoc.translate(0, -2));
                    kingSet.add(kingLoc.translate(0, -1));
                    kingSet.add(kingLoc.translate(0, 0));
                    kingSet.add(kingLoc.translate(0, 1));
                    kingSet.add(kingLoc.translate(0, 2));
                    kingSet.add(kingLoc.translate(1, -2));
                    kingSet.add(kingLoc.translate(1, -1));
                    kingSet.add(kingLoc.translate(1, 0));
                    kingSet.add(kingLoc.translate(1, 1));
                    kingSet.add(kingLoc.translate(1, 2));
                    kingSet.add(kingLoc.translate(2, -2));
                    kingSet.add(kingLoc.translate(2, -1));
                    kingSet.add(kingLoc.translate(2, 0));
                    kingSet.add(kingLoc.translate(2, 1));
                    kingSet.add(kingLoc.translate(2, 2));
                } else {
                    kingSet.add(kingLoc.translate(-1, -1));
                    kingSet.add(kingLoc.translate(-1, 0));
                    kingSet.add(kingLoc.translate(-1, 1));
                    kingSet.add(kingLoc.translate(0, -1));
                    kingSet.add(kingLoc.translate(0, 0));
                    kingSet.add(kingLoc.translate(0, 1));
                    kingSet.add(kingLoc.translate(1, -1));
                    kingSet.add(kingLoc.translate(1, 0));
                    kingSet.add(kingLoc.translate(1, 1));
                }
            }
        }
        {
            var kingLoc = GlobalArray.getRatKing(2);
            if(kingLoc != null) {
                if(GlobalArray.isKingExpanded(2)) {
                    kingSet.add(kingLoc.translate(-2, -2));
                    kingSet.add(kingLoc.translate(-2, -1));
                    kingSet.add(kingLoc.translate(-2, 0));
                    kingSet.add(kingLoc.translate(-2, 1));
                    kingSet.add(kingLoc.translate(-2, 2));
                    kingSet.add(kingLoc.translate(-1, -2));
                    kingSet.add(kingLoc.translate(-1, -1));
                    kingSet.add(kingLoc.translate(-1, 0));
                    kingSet.add(kingLoc.translate(-1, 1));
                    kingSet.add(kingLoc.translate(-1, 2));
                    kingSet.add(kingLoc.translate(0, -2));
                    kingSet.add(kingLoc.translate(0, -1));
                    kingSet.add(kingLoc.translate(0, 0));
                    kingSet.add(kingLoc.translate(0, 1));
                    kingSet.add(kingLoc.translate(0, 2));
                    kingSet.add(kingLoc.translate(1, -2));
                    kingSet.add(kingLoc.translate(1, -1));
                    kingSet.add(kingLoc.translate(1, 0));
                    kingSet.add(kingLoc.translate(1, 1));
                    kingSet.add(kingLoc.translate(1, 2));
                    kingSet.add(kingLoc.translate(2, -2));
                    kingSet.add(kingLoc.translate(2, -1));
                    kingSet.add(kingLoc.translate(2, 0));
                    kingSet.add(kingLoc.translate(2, 1));
                    kingSet.add(kingLoc.translate(2, 2));
                } else {
                    kingSet.add(kingLoc.translate(-1, -1));
                    kingSet.add(kingLoc.translate(-1, 0));
                    kingSet.add(kingLoc.translate(-1, 1));
                    kingSet.add(kingLoc.translate(0, -1));
                    kingSet.add(kingLoc.translate(0, 0));
                    kingSet.add(kingLoc.translate(0, 1));
                    kingSet.add(kingLoc.translate(1, -1));
                    kingSet.add(kingLoc.translate(1, 0));
                    kingSet.add(kingLoc.translate(1, 1));
                }
            }
        }
        {
            var kingLoc = GlobalArray.getRatKing(3);
            if(kingLoc != null) {
                if(GlobalArray.isKingExpanded(3)) {
                    kingSet.add(kingLoc.translate(-2, -2));
                    kingSet.add(kingLoc.translate(-2, -1));
                    kingSet.add(kingLoc.translate(-2, 0));
                    kingSet.add(kingLoc.translate(-2, 1));
                    kingSet.add(kingLoc.translate(-2, 2));
                    kingSet.add(kingLoc.translate(-1, -2));
                    kingSet.add(kingLoc.translate(-1, -1));
                    kingSet.add(kingLoc.translate(-1, 0));
                    kingSet.add(kingLoc.translate(-1, 1));
                    kingSet.add(kingLoc.translate(-1, 2));
                    kingSet.add(kingLoc.translate(0, -2));
                    kingSet.add(kingLoc.translate(0, -1));
                    kingSet.add(kingLoc.translate(0, 0));
                    kingSet.add(kingLoc.translate(0, 1));
                    kingSet.add(kingLoc.translate(0, 2));
                    kingSet.add(kingLoc.translate(1, -2));
                    kingSet.add(kingLoc.translate(1, -1));
                    kingSet.add(kingLoc.translate(1, 0));
                    kingSet.add(kingLoc.translate(1, 1));
                    kingSet.add(kingLoc.translate(1, 2));
                    kingSet.add(kingLoc.translate(2, -2));
                    kingSet.add(kingLoc.translate(2, -1));
                    kingSet.add(kingLoc.translate(2, 0));
                    kingSet.add(kingLoc.translate(2, 1));
                    kingSet.add(kingLoc.translate(2, 2));
                } else {
                    kingSet.add(kingLoc.translate(-1, -1));
                    kingSet.add(kingLoc.translate(-1, 0));
                    kingSet.add(kingLoc.translate(-1, 1));
                    kingSet.add(kingLoc.translate(0, -1));
                    kingSet.add(kingLoc.translate(0, 0));
                    kingSet.add(kingLoc.translate(0, 1));
                    kingSet.add(kingLoc.translate(1, -1));
                    kingSet.add(kingLoc.translate(1, 0));
                    kingSet.add(kingLoc.translate(1, 1));
                }
            }
        }
        {
            var kingLoc = GlobalArray.getRatKing(4);
            if(kingLoc != null) {
                if(GlobalArray.isKingExpanded(4)) {
                    kingSet.add(kingLoc.translate(-2, -2));
                    kingSet.add(kingLoc.translate(-2, -1));
                    kingSet.add(kingLoc.translate(-2, 0));
                    kingSet.add(kingLoc.translate(-2, 1));
                    kingSet.add(kingLoc.translate(-2, 2));
                    kingSet.add(kingLoc.translate(-1, -2));
                    kingSet.add(kingLoc.translate(-1, -1));
                    kingSet.add(kingLoc.translate(-1, 0));
                    kingSet.add(kingLoc.translate(-1, 1));
                    kingSet.add(kingLoc.translate(-1, 2));
                    kingSet.add(kingLoc.translate(0, -2));
                    kingSet.add(kingLoc.translate(0, -1));
                    kingSet.add(kingLoc.translate(0, 0));
                    kingSet.add(kingLoc.translate(0, 1));
                    kingSet.add(kingLoc.translate(0, 2));
                    kingSet.add(kingLoc.translate(1, -2));
                    kingSet.add(kingLoc.translate(1, -1));
                    kingSet.add(kingLoc.translate(1, 0));
                    kingSet.add(kingLoc.translate(1, 1));
                    kingSet.add(kingLoc.translate(1, 2));
                    kingSet.add(kingLoc.translate(2, -2));
                    kingSet.add(kingLoc.translate(2, -1));
                    kingSet.add(kingLoc.translate(2, 0));
                    kingSet.add(kingLoc.translate(2, 1));
                    kingSet.add(kingLoc.translate(2, 2));
                } else {
                    kingSet.add(kingLoc.translate(-1, -1));
                    kingSet.add(kingLoc.translate(-1, 0));
                    kingSet.add(kingLoc.translate(-1, 1));
                    kingSet.add(kingLoc.translate(0, -1));
                    kingSet.add(kingLoc.translate(0, 0));
                    kingSet.add(kingLoc.translate(0, 1));
                    kingSet.add(kingLoc.translate(1, -1));
                    kingSet.add(kingLoc.translate(1, 0));
                    kingSet.add(kingLoc.translate(1, 1));
                }
            }
        }
    }
}