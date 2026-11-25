

package awubot.robot.rat.combat;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.TrapType;
import battlecode.common.UnitType;

import static awubot.Game.*;
import static awubot.util.Util.*;
import awubot.util.Profiler;
import awubot.CellTracker;
import awubot.RobotTracker;
import awubot.comms.Enemy;
import awubot.comms.EnemyManager;

public class CombatTracker {
    private static final int GRID_RADIUS = 2;






    // isThreat flags for the 5x5 around the player.
    private static boolean t_m2_2;
    private static boolean t_m1_2;
    private static boolean t_0_2;
    private static boolean t_1_2;
    private static boolean t_2_2;
    private static boolean t_m2_1;
    private static boolean t_m1_1;
    private static boolean t_0_1;
    private static boolean t_1_1;
    private static boolean t_2_1;
    private static boolean t_m2_0;
    private static boolean t_m1_0;
    private static boolean t_0_0;
    private static boolean t_1_0;
    private static boolean t_2_0;
    private static boolean t_m2_m1;
    private static boolean t_m1_m1;
    private static boolean t_0_m1;
    private static boolean t_1_m1;
    private static boolean t_2_m1;
    private static boolean t_m2_m2;
    private static boolean t_m1_m2;
    private static boolean t_0_m2;
    private static boolean t_1_m2;
    private static boolean t_2_m2;

    // Byte-packed enemy counters (low/high health) per move direction.
    private static long lowN;
    private static long lowNE;
    private static long lowE;
    private static long lowSE;
    private static long lowS;
    private static long lowSW;
    private static long lowW;
    private static long lowNW;
    private static long lowC;
    private static long highN;
    private static long highNE;
    private static long highE;
    private static long highSE;
    private static long highS;
    private static long highSW;
    private static long highW;
    private static long highNW;
    private static long highC;

    // Byte-packed threat masks per move direction (0xFF per reachable neighbor).
    private static long moveThreatN;
    private static long moveThreatNE;
    private static long moveThreatE;
    private static long moveThreatSE;
    private static long moveThreatS;
    private static long moveThreatSW;
    private static long moveThreatW;
    private static long moveThreatNW;
    private static long moveThreatC;

    // Cached (combined & threat) per move/facing.
    public static long threatNN;
    public static long threatNNE;
    public static long threatNE;
    public static long threatNSE;
    public static long threatNS;
    public static long threatNSW;
    public static long threatNW;
    public static long threatNNW;
    public static long threatNEN;
    public static long threatNENE;
    public static long threatNEE;
    public static long threatNESE;
    public static long threatNES;
    public static long threatNESW;
    public static long threatNEW;
    public static long threatNENW;
    public static long threatEN;
    public static long threatENE;
    public static long threatEE;
    public static long threatESE;
    public static long threatES;
    public static long threatESW;
    public static long threatEW;
    public static long threatENW;
    public static long threatSEN;
    public static long threatSENE;
    public static long threatSEE;
    public static long threatSESE;
    public static long threatSES;
    public static long threatSESW;
    public static long threatSEW;
    public static long threatSENW;
    public static long threatSN;
    public static long threatSNE;
    public static long threatSE;
    public static long threatSSE;
    public static long threatSS;
    public static long threatSSW;
    public static long threatSW;
    public static long threatSNW;
    public static long threatSWN;
    public static long threatSWNE;
    public static long threatSWE;
    public static long threatSWSE;
    public static long threatSWS;
    public static long threatSWSW;
    public static long threatSWW;
    public static long threatSWNW;
    public static long threatWN;
    public static long threatWNE;
    public static long threatWE;
    public static long threatWSE;
    public static long threatWS;
    public static long threatWSW;
    public static long threatWW;
    public static long threatWNW;
    public static long threatNWN;
    public static long threatNWNE;
    public static long threatNWE;
    public static long threatNWSE;
    public static long threatNWS;
    public static long threatNWSW;
    public static long threatNWW;
    public static long threatNWNW;
    public static long threatCN;
    public static long threatCNE;
    public static long threatCE;
    public static long threatCSE;
    public static long threatCS;
    public static long threatCSW;
    public static long threatCW;
    public static long threatCNW;

    // Hidden-neighbor masks per facing (0xFF for hidden, 0x00 for visible).
private static final long hiddenN = 0x00ffffffffff0000L;
private static final long hiddenNE = 0xffffffffff000000L;
private static final long hiddenE = 0xffffffff000000ffL;
private static final long hiddenSE = 0xffffff000000ffffL;
private static final long hiddenS = 0xffff000000ffffffL;
private static final long hiddenSW = 0xff000000ffffffffL;
private static final long hiddenW = 0x000000ffffffffffL;
private static final long hiddenNW = 0x0000ffffffffff00L;
// indicate

    public static void tick() throws GameActionException {
        // // Profiler.push();
        loadThreatFromMap();
        // // Profiler.pop("CombatTracker: loading threats");

        // // Profiler.push();
        recomputeThreatMasks();
        // // Profiler.pop("CombatTracker: recomputing threat masks");

        resetEnemyCounts();
        
        // // Profiler.push();
        for (int i = 0; i < RobotTracker.numEnemies; i++) {
            RobotInfo info = RobotTracker.sortedEnemies[i];
            if (info.getType() != UnitType.BABY_RAT) continue;
            registerEnemy(new Enemy(info));
        }
        // be aware of squeaked danger (hopefully should reduce chance of getting napped while not blocking too many moves unnecessarily)
        for (int i = 0; i < EnemyManager.numKnownEnemies; i++) {
            Enemy enemy = EnemyManager.knownEnemies[i];
            registerEnemy(enemy);
        }
        // // Profiler.pop("CombatTracker: registering enemies");

        // // Profiler.push();
        precomputeThreatCombos();
        // // Profiler.pop("CombatTracker: precomputing threat combos");
    }

    public static void resetEnemyCounts() {
        lowN = 0L;
        lowNE = 0L;
        lowE = 0L;
        lowSE = 0L;
        lowS = 0L;
        lowSW = 0L;
        lowW = 0L;
        lowNW = 0L;
        lowC = 0L;
        highN = 0L;
        highNE = 0L;
        highE = 0L;
        highSE = 0L;
        highS = 0L;
        highSW = 0L;
        highW = 0L;
        highNW = 0L;
        highC = 0L;
    }

    private static void precomputeThreatCombos() {
        {
            long low = lowN;
            long high = highN;
            long threat = moveThreatN;
            long combined;
            combined = high + (low & hiddenN);
            threatNN = combined & threat;
            combined = high + (low & hiddenNE);
            threatNNE = combined & threat;
            combined = high + (low & hiddenE);
            threatNE = combined & threat;
            combined = high + (low & hiddenSE);
            threatNSE = combined & threat;
            combined = high + (low & hiddenS);
            threatNS = combined & threat;
            combined = high + (low & hiddenSW);
            threatNSW = combined & threat;
            combined = high + (low & hiddenW);
            threatNW = combined & threat;
            combined = high + (low & hiddenNW);
            threatNNW = combined & threat;
        }
        {
            long low = lowNE;
            long high = highNE;
            long threat = moveThreatNE;
            long combined;
            combined = high + (low & hiddenN);
            threatNEN = combined & threat;
            combined = high + (low & hiddenNE);
            threatNENE = combined & threat;
            combined = high + (low & hiddenE);
            threatNEE = combined & threat;
            combined = high + (low & hiddenSE);
            threatNESE = combined & threat;
            combined = high + (low & hiddenS);
            threatNES = combined & threat;
            combined = high + (low & hiddenSW);
            threatNESW = combined & threat;
            combined = high + (low & hiddenW);
            threatNEW = combined & threat;
            combined = high + (low & hiddenNW);
            threatNENW = combined & threat;
        }
        {
            long low = lowE;
            long high = highE;
            long threat = moveThreatE;
            long combined;
            combined = high + (low & hiddenN);
            threatEN = combined & threat;
            combined = high + (low & hiddenNE);
            threatENE = combined & threat;
            combined = high + (low & hiddenE);
            threatEE = combined & threat;
            combined = high + (low & hiddenSE);
            threatESE = combined & threat;
            combined = high + (low & hiddenS);
            threatES = combined & threat;
            combined = high + (low & hiddenSW);
            threatESW = combined & threat;
            combined = high + (low & hiddenW);
            threatEW = combined & threat;
            combined = high + (low & hiddenNW);
            threatENW = combined & threat;
        }
        {
            long low = lowSE;
            long high = highSE;
            long threat = moveThreatSE;
            long combined;
            combined = high + (low & hiddenN);
            threatSEN = combined & threat;
            combined = high + (low & hiddenNE);
            threatSENE = combined & threat;
            combined = high + (low & hiddenE);
            threatSEE = combined & threat;
            combined = high + (low & hiddenSE);
            threatSESE = combined & threat;
            combined = high + (low & hiddenS);
            threatSES = combined & threat;
            combined = high + (low & hiddenSW);
            threatSESW = combined & threat;
            combined = high + (low & hiddenW);
            threatSEW = combined & threat;
            combined = high + (low & hiddenNW);
            threatSENW = combined & threat;
        }
        {
            long low = lowS;
            long high = highS;
            long threat = moveThreatS;
            long combined;
            combined = high + (low & hiddenN);
            threatSN = combined & threat;
            combined = high + (low & hiddenNE);
            threatSNE = combined & threat;
            combined = high + (low & hiddenE);
            threatSE = combined & threat;
            combined = high + (low & hiddenSE);
            threatSSE = combined & threat;
            combined = high + (low & hiddenS);
            threatSS = combined & threat;
            combined = high + (low & hiddenSW);
            threatSSW = combined & threat;
            combined = high + (low & hiddenW);
            threatSW = combined & threat;
            combined = high + (low & hiddenNW);
            threatSNW = combined & threat;
        }
        {
            long low = lowSW;
            long high = highSW;
            long threat = moveThreatSW;
            long combined;
            combined = high + (low & hiddenN);
            threatSWN = combined & threat;
            combined = high + (low & hiddenNE);
            threatSWNE = combined & threat;
            combined = high + (low & hiddenE);
            threatSWE = combined & threat;
            combined = high + (low & hiddenSE);
            threatSWSE = combined & threat;
            combined = high + (low & hiddenS);
            threatSWS = combined & threat;
            combined = high + (low & hiddenSW);
            threatSWSW = combined & threat;
            combined = high + (low & hiddenW);
            threatSWW = combined & threat;
            combined = high + (low & hiddenNW);
            threatSWNW = combined & threat;
        }
        {
            long low = lowW;
            long high = highW;
            long threat = moveThreatW;
            long combined;
            combined = high + (low & hiddenN);
            threatWN = combined & threat;
            combined = high + (low & hiddenNE);
            threatWNE = combined & threat;
            combined = high + (low & hiddenE);
            threatWE = combined & threat;
            combined = high + (low & hiddenSE);
            threatWSE = combined & threat;
            combined = high + (low & hiddenS);
            threatWS = combined & threat;
            combined = high + (low & hiddenSW);
            threatWSW = combined & threat;
            combined = high + (low & hiddenW);
            threatWW = combined & threat;
            combined = high + (low & hiddenNW);
            threatWNW = combined & threat;
        }
        {
            long low = lowNW;
            long high = highNW;
            long threat = moveThreatNW;
            long combined;
            combined = high + (low & hiddenN);
            threatNWN = combined & threat;
            combined = high + (low & hiddenNE);
            threatNWNE = combined & threat;
            combined = high + (low & hiddenE);
            threatNWE = combined & threat;
            combined = high + (low & hiddenSE);
            threatNWSE = combined & threat;
            combined = high + (low & hiddenS);
            threatNWS = combined & threat;
            combined = high + (low & hiddenSW);
            threatNWSW = combined & threat;
            combined = high + (low & hiddenW);
            threatNWW = combined & threat;
            combined = high + (low & hiddenNW);
            threatNWNW = combined & threat;
        }
        {
            long low = lowC;
            long high = highC;
            long threat = moveThreatC;
            long combined;
            combined = high + (low & hiddenN);
            threatCN = combined & threat;
            combined = high + (low & hiddenNE);
            threatCNE = combined & threat;
            combined = high + (low & hiddenE);
            threatCE = combined & threat;
            combined = high + (low & hiddenSE);
            threatCSE = combined & threat;
            combined = high + (low & hiddenS);
            threatCS = combined & threat;
            combined = high + (low & hiddenSW);
            threatCSW = combined & threat;
            combined = high + (low & hiddenW);
            threatCW = combined & threat;
            combined = high + (low & hiddenNW);
            threatCNW = combined & threat;
        }
    }

    public static boolean hasAnyThreat(Direction moveDir) {
        switch (moveDir.ordinal()) {
            case 0 -> {
                long comb = lowN + highN;
                return (comb & moveThreatN) != 0L;
            }
            case 1 -> {
                long comb = lowNE + highNE;
                return (comb & moveThreatNE) != 0L;
            }
            case 2 -> {
                long comb = lowE + highE;
                return (comb & moveThreatE) != 0L;
            }
            case 3 -> {
                long comb = lowSE + highSE;
                return (comb & moveThreatSE) != 0L;
            }
            case 4 -> {
                long comb = lowS + highS;
                return (comb & moveThreatS) != 0L;
            }
            case 5 -> {
                long comb = lowSW + highSW;
                return (comb & moveThreatSW) != 0L;
            }
            case 6 -> {
                long comb = lowW + highW;
                return (comb & moveThreatW) != 0L;
            }
            case 7 -> {
                long comb = lowNW + highNW;
                return (comb & moveThreatNW) != 0L;
            }
            case 8 -> {
                long comb = lowC + highC;
                return (comb & moveThreatC) != 0L;
            }
            default -> { return false; }
        }
    }

    // TODO: account for dirt
    public static void loadThreatFromMap() throws GameActionException {
        int x = myLoc.x;
        int y = myLoc.y;
t_m2_2 = (x >= 2 && y < mapHeight - 2) && CellTracker.mapData[y  + 2].charAt(x  + -2) != '\2';t_m1_2 = (x >= 1 && y < mapHeight - 2) && CellTracker.mapData[y  + 2].charAt(x  + -1) != '\2';t_0_2 = (y < mapHeight - 2) && CellTracker.mapData[y  + 2].charAt(x ) != '\2';t_1_2 = (x < mapWidth - 1 && y < mapHeight - 2) && CellTracker.mapData[y  + 2].charAt(x  + 1) != '\2';t_2_2 = (x < mapWidth - 2 && y < mapHeight - 2) && CellTracker.mapData[y  + 2].charAt(x  + 2) != '\2';t_m2_1 = (x >= 2 && y < mapHeight - 1) && CellTracker.mapData[y  + 1].charAt(x  + -2) != '\2';t_m1_1 = (x >= 1 && y < mapHeight - 1) && CellTracker.mapData[y  + 1].charAt(x  + -1) != '\2';t_0_1 = (y < mapHeight - 1) && CellTracker.mapData[y  + 1].charAt(x ) != '\2';t_1_1 = (x < mapWidth - 1 && y < mapHeight - 1) && CellTracker.mapData[y  + 1].charAt(x  + 1) != '\2';t_2_1 = (x < mapWidth - 2 && y < mapHeight - 1) && CellTracker.mapData[y  + 1].charAt(x  + 2) != '\2';t_m2_0 = (x >= 2) && CellTracker.mapData[y ].charAt(x  + -2) != '\2';t_m1_0 = (x >= 1) && CellTracker.mapData[y ].charAt(x  + -1) != '\2';        t_0_0 = true;t_1_0 = (x < mapWidth - 1) && CellTracker.mapData[y ].charAt(x  + 1) != '\2';t_2_0 = (x < mapWidth - 2) && CellTracker.mapData[y ].charAt(x  + 2) != '\2';t_m2_m1 = (x >= 2 && y >= 1) && CellTracker.mapData[y  + -1].charAt(x  + -2) != '\2';t_m1_m1 = (x >= 1 && y >= 1) && CellTracker.mapData[y  + -1].charAt(x  + -1) != '\2';t_0_m1 = (y >= 1) && CellTracker.mapData[y  + -1].charAt(x ) != '\2';t_1_m1 = (x < mapWidth - 1 && y >= 1) && CellTracker.mapData[y  + -1].charAt(x  + 1) != '\2';t_2_m1 = (x < mapWidth - 2 && y >= 1) && CellTracker.mapData[y  + -1].charAt(x  + 2) != '\2';t_m2_m2 = (x >= 2 && y >= 2) && CellTracker.mapData[y  + -2].charAt(x  + -2) != '\2';t_m1_m2 = (x >= 1 && y >= 2) && CellTracker.mapData[y  + -2].charAt(x  + -1) != '\2';t_0_m2 = (y >= 2) && CellTracker.mapData[y  + -2].charAt(x ) != '\2';t_1_m2 = (x < mapWidth - 1 && y >= 2) && CellTracker.mapData[y  + -2].charAt(x  + 1) != '\2';t_2_m2 = (x < mapWidth - 2 && y >= 2) && CellTracker.mapData[y  + -2].charAt(x  + 2) != '\2';    }


    public static void recomputeThreatMasks() {
moveThreatN = (t_0_2 ? 0x00000000000000ffL : 0L) | (t_1_2 ? 0x000000000000ff00L : 0L) | (t_1_1 ? 0x0000000000ff0000L : 0L) | (t_1_0 ? 0x00000000ff000000L : 0L) | (t_0_0 ? 0x000000ff00000000L : 0L) | (t_m1_0 ? 0x0000ff0000000000L : 0L) | (t_m1_1 ? 0x00ff000000000000L : 0L) | (t_m1_2 ? 0xff00000000000000L : 0L);
moveThreatNE = (t_1_2 ? 0x00000000000000ffL : 0L) | (t_2_2 ? 0x000000000000ff00L : 0L) | (t_2_1 ? 0x0000000000ff0000L : 0L) | (t_2_0 ? 0x00000000ff000000L : 0L) | (t_1_0 ? 0x000000ff00000000L : 0L) | (t_0_0 ? 0x0000ff0000000000L : 0L) | (t_0_1 ? 0x00ff000000000000L : 0L) | (t_0_2 ? 0xff00000000000000L : 0L);
moveThreatE = (t_1_1 ? 0x00000000000000ffL : 0L) | (t_2_1 ? 0x000000000000ff00L : 0L) | (t_2_0 ? 0x0000000000ff0000L : 0L) | (t_2_m1 ? 0x00000000ff000000L : 0L) | (t_1_m1 ? 0x000000ff00000000L : 0L) | (t_0_m1 ? 0x0000ff0000000000L : 0L) | (t_0_0 ? 0x00ff000000000000L : 0L) | (t_0_1 ? 0xff00000000000000L : 0L);
moveThreatSE = (t_1_0 ? 0x00000000000000ffL : 0L) | (t_2_0 ? 0x000000000000ff00L : 0L) | (t_2_m1 ? 0x0000000000ff0000L : 0L) | (t_2_m2 ? 0x00000000ff000000L : 0L) | (t_1_m2 ? 0x000000ff00000000L : 0L) | (t_0_m2 ? 0x0000ff0000000000L : 0L) | (t_0_m1 ? 0x00ff000000000000L : 0L) | (t_0_0 ? 0xff00000000000000L : 0L);
moveThreatS = (t_0_0 ? 0x00000000000000ffL : 0L) | (t_1_0 ? 0x000000000000ff00L : 0L) | (t_1_m1 ? 0x0000000000ff0000L : 0L) | (t_1_m2 ? 0x00000000ff000000L : 0L) | (t_0_m2 ? 0x000000ff00000000L : 0L) | (t_m1_m2 ? 0x0000ff0000000000L : 0L) | (t_m1_m1 ? 0x00ff000000000000L : 0L) | (t_m1_0 ? 0xff00000000000000L : 0L);
moveThreatSW = (t_m1_0 ? 0x00000000000000ffL : 0L) | (t_0_0 ? 0x000000000000ff00L : 0L) | (t_0_m1 ? 0x0000000000ff0000L : 0L) | (t_0_m2 ? 0x00000000ff000000L : 0L) | (t_m1_m2 ? 0x000000ff00000000L : 0L) | (t_m2_m2 ? 0x0000ff0000000000L : 0L) | (t_m2_m1 ? 0x00ff000000000000L : 0L) | (t_m2_0 ? 0xff00000000000000L : 0L);
moveThreatW = (t_m1_1 ? 0x00000000000000ffL : 0L) | (t_0_1 ? 0x000000000000ff00L : 0L) | (t_0_0 ? 0x0000000000ff0000L : 0L) | (t_0_m1 ? 0x00000000ff000000L : 0L) | (t_m1_m1 ? 0x000000ff00000000L : 0L) | (t_m2_m1 ? 0x0000ff0000000000L : 0L) | (t_m2_0 ? 0x00ff000000000000L : 0L) | (t_m2_1 ? 0xff00000000000000L : 0L);
moveThreatNW = (t_m1_2 ? 0x00000000000000ffL : 0L) | (t_0_2 ? 0x000000000000ff00L : 0L) | (t_0_1 ? 0x0000000000ff0000L : 0L) | (t_0_0 ? 0x00000000ff000000L : 0L) | (t_m1_0 ? 0x000000ff00000000L : 0L) | (t_m2_0 ? 0x0000ff0000000000L : 0L) | (t_m2_1 ? 0x00ff000000000000L : 0L) | (t_m2_2 ? 0xff00000000000000L : 0L);
moveThreatC = (t_0_1 ? 0x00000000000000ffL : 0L) | (t_1_1 ? 0x000000000000ff00L : 0L) | (t_1_0 ? 0x0000000000ff0000L : 0L) | (t_1_m1 ? 0x00000000ff000000L : 0L) | (t_0_m1 ? 0x000000ff00000000L : 0L) | (t_m1_m1 ? 0x0000ff0000000000L : 0L) | (t_m1_0 ? 0x00ff000000000000L : 0L) | (t_m1_1 ? 0xff00000000000000L : 0L);
    }

    public static void registerEnemy(Enemy info) {
        int dx = info.location.x - myLocX;
        int dy = info.location.y - myLocY;
        boolean highHealth = info.health > rc.getHealth();
        if (highHealth) {
            registerEnemyHigh(dx, dy, info.direction);
        } else {
            registerEnemyLow(dx, dy, info.direction);
        }
    }

    public static void registerEnemyLow(int dx, int dy, Direction facing) {
        switch (dx) {
            case -3 -> {
                switch (dy) {
                    case -3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0000010000000000L;
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0000010000000000L;
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0000010000000000L;
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                lowW += 0x0000010000000000L;
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0001010000000000L;
                                lowW += 0x0000010000000000L;
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0001010000000000L;
                                lowW += 0x0000010000000000L;
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0000010000000000L;
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101010000000000L;
                                lowW += 0x0001010000000000L;
                                lowNW += 0x0000010000000000L;
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101010000000000L;
                                lowW += 0x0001010000000000L;
                                lowNW += 0x0000010000000000L;
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101010000000000L;
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                lowW += 0x0101010000000000L;
                                lowNW += 0x0001010000000000L;
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101000000000000L;
                                lowW += 0x0101010000000000L;
                                lowNW += 0x0001010000000000L;
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101000000000000L;
                                lowW += 0x0101010000000000L;
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0101010000000000L;
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0100000000000000L;
                                lowW += 0x0101000000000000L;
                                lowNW += 0x0101010000000000L;
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0100000000000000L;
                                lowW += 0x0101000000000000L;
                                lowNW += 0x0101010000000000L;
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0100000000000000L;
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                lowW += 0x0100000000000000L;
                                lowNW += 0x0101000000000000L;
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                lowW += 0x0100000000000000L;
                                lowNW += 0x0101000000000000L;
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                lowW += 0x0100000000000000L;
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0100000000000000L;
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0100000000000000L;
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0100000000000000L;
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case -2 -> {
                switch (dy) {
                    case -3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                lowS += 0x0000010000000000L;
                                lowSW += 0x0000010100000000L;
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                lowS += 0x0000010000000000L;
                                lowSW += 0x0000010100000000L;
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                lowS += 0x0000010000000000L;
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0001010100000000L;
                                lowW += 0x0000010100000000L;
                                
                                lowC += 0x0000010000000000L;
                            }
                            case 1 -> {
                                
                                
                                
                                
                                lowS += 0x0001010000000000L;
                                lowSW += 0x0001010100000000L;
                                lowW += 0x0000010100000000L;
                                
                                lowC += 0x0000010000000000L;
                            }
                            case 2 -> {
                                
                                
                                
                                
                                lowS += 0x0001010000000000L;
                                lowSW += 0x0001010100000000L;
                                
                                
                                lowC += 0x0000010000000000L;
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                lowN += 0x0000010000000000L;
                                
                                
                                
                                
                                
                                lowW += 0x0001010100000000L;
                                lowNW += 0x0000010100000000L;
                                
                            }
                            case 1 -> {
                                lowN += 0x0000010000000000L;
                                
                                
                                
                                lowS += 0x0101010000000000L;
                                lowSW += 0x0101010100000001L;
                                lowW += 0x0001010100000000L;
                                lowNW += 0x0000010100000000L;
                                lowC += 0x0001010000000000L;
                            }
                            case 2 -> {
                                lowN += 0x0000010000000000L;
                                
                                
                                
                                lowS += 0x0101010000000000L;
                                lowSW += 0x0101010100000001L;
                                lowW += 0x0001010100000000L;
                                
                                lowC += 0x0001010000000000L;
                            }
                            case 3 -> {
                                
                                
                                
                                
                                lowS += 0x0101010000000000L;
                                lowSW += 0x0101010100000001L;
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0001010100000000L;
                                
                            }
                            case 1 -> {
                                lowN += 0x0001010000000000L;
                                
                                
                                
                                
                                
                                lowW += 0x0101010100000001L;
                                lowNW += 0x0001010100000000L;
                                lowC += 0x0101010000000000L;
                            }
                            case 2 -> {
                                lowN += 0x0001010000000000L;
                                
                                
                                
                                lowS += 0x0101000000000000L;
                                lowSW += 0x0101000000000001L;
                                lowW += 0x0101010100000001L;
                                lowNW += 0x0001010100000000L;
                                lowC += 0x0101010000000000L;
                            }
                            case 3 -> {
                                
                                
                                
                                
                                lowS += 0x0101000000000000L;
                                lowSW += 0x0101000000000001L;
                                lowW += 0x0101010100000001L;
                                
                                lowC += 0x0101010000000000L;
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101000000000001L;
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                lowN += 0x0101010000000000L;
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0101010100000001L;
                                
                            }
                            case 2 -> {
                                lowN += 0x0101010000000000L;
                                
                                
                                
                                lowS += 0x0100000000000000L;
                                
                                lowW += 0x0101000000000001L;
                                lowNW += 0x0101010100000001L;
                                lowC += 0x0101000000000000L;
                            }
                            case 3 -> {
                                lowN += 0x0101010000000000L;
                                
                                
                                
                                lowS += 0x0100000000000000L;
                                lowSW += 0x0100000000000001L;
                                lowW += 0x0101000000000001L;
                                lowNW += 0x0101010100000001L;
                                lowC += 0x0101000000000000L;
                            }
                            case 4 -> {
                                
                                
                                
                                
                                lowS += 0x0100000000000000L;
                                lowSW += 0x0100000000000001L;
                                lowW += 0x0101000000000001L;
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                lowN += 0x0101000000000000L;
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0101000000000001L;
                                lowC += 0x0100000000000000L;
                            }
                            case 3 -> {
                                lowN += 0x0101000000000000L;
                                
                                
                                
                                
                                
                                lowW += 0x0100000000000001L;
                                lowNW += 0x0101000000000001L;
                                lowC += 0x0100000000000000L;
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                lowW += 0x0100000000000001L;
                                lowNW += 0x0101000000000001L;
                                lowC += 0x0100000000000000L;
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                lowN += 0x0100000000000000L;
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                lowN += 0x0100000000000000L;
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0100000000000001L;
                                
                            }
                            case 4 -> {
                                lowN += 0x0100000000000000L;
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0100000000000001L;
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case -1 -> {
                switch (dy) {
                    case -3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                lowSE += 0x0000010000000000L;
                                lowS += 0x0000010100000000L;
                                lowSW += 0x0000010101000000L;
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                lowSE += 0x0000010000000000L;
                                lowS += 0x0000010100000000L;
                                lowSW += 0x0000010101000000L;
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                lowSE += 0x0000010000000000L;
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0000010101000000L;
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                lowE += 0x0000010000000000L;
                                
                                lowS += 0x0001010100000000L;
                                lowSW += 0x0001010101010000L;
                                lowW += 0x0000010101000000L;
                                
                                lowC += 0x0000010100000000L;
                            }
                            case 1 -> {
                                
                                
                                lowE += 0x0000010000000000L;
                                lowSE += 0x0001010000000000L;
                                lowS += 0x0001010100000000L;
                                lowSW += 0x0001010101010000L;
                                lowW += 0x0000010101000000L;
                                
                                lowC += 0x0000010100000000L;
                            }
                            case 2 -> {
                                
                                
                                lowE += 0x0000010000000000L;
                                lowSE += 0x0001010000000000L;
                                lowS += 0x0001010100000000L;
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0001010101010000L;
                                lowW += 0x0000010101000000L;
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                lowN += 0x0000010100000000L;
                                lowNE += 0x0000010000000000L;
                                
                                
                                
                                lowSW += 0x0101010101010101L;
                                lowW += 0x0001010101010000L;
                                lowNW += 0x0000010101000000L;
                                lowC += 0x0001010100000000L;
                            }
                            case 1 -> {
                                lowN += 0x0000010100000000L;
                                lowNE += 0x0000010000000000L;
                                lowE += 0x0001010000000000L;
                                lowSE += 0x0101010000000000L;
                                lowS += 0x0101010100000001L;
                                lowSW += 0x0101010101010101L;
                                lowW += 0x0001010101010000L;
                                lowNW += 0x0000010101000000L;
                                lowC += 0x0001010100000000L;
                            }
                            case 2 -> {
                                
                                lowNE += 0x0000010000000000L;
                                lowE += 0x0001010000000000L;
                                lowSE += 0x0101010000000000L;
                                lowS += 0x0101010100000001L;
                                lowSW += 0x0101010101010101L;
                                
                                
                                lowC += 0x0001010100000000L;
                            }
                            case 3 -> {
                                
                                
                                
                                lowSE += 0x0101010000000000L;
                                lowS += 0x0101010100000001L;
                                lowSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101010101010101L;
                                lowW += 0x0001010101010000L;
                                lowNW += 0x0000010101000000L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                lowN += 0x0001010100000000L;
                                
                                
                                
                                
                                
                                lowW += 0x0101010101010101L;
                                lowNW += 0x0001010101010000L;
                                
                            }
                            case 1 -> {
                                lowN += 0x0001010100000000L;
                                lowNE += 0x0001010000000000L;
                                lowE += 0x0101010000000000L;
                                
                                
                                
                                lowW += 0x0101010101010101L;
                                lowNW += 0x0001010101010000L;
                                lowC += 0x0101010100000001L;
                            }
                            case 2 -> {
                                lowN += 0x0001010100000000L;
                                lowNE += 0x0001010000000000L;
                                lowE += 0x0101010000000000L;
                                lowSE += 0x0101000000000000L;
                                lowS += 0x0101000000000001L;
                                
                                lowW += 0x0101010101010101L;
                                
                                lowC += 0x0101010100000001L;
                            }
                            case 3 -> {
                                
                                
                                lowE += 0x0101010000000000L;
                                lowSE += 0x0101000000000000L;
                                lowS += 0x0101000000000001L;
                                lowSW += 0x0101000000010101L;
                                lowW += 0x0101010101010101L;
                                
                                lowC += 0x0101010100000001L;
                            }
                            case 4 -> {
                                
                                
                                
                                
                                lowS += 0x0101000000000001L;
                                lowSW += 0x0101000000010101L;
                                lowW += 0x0101010101010101L;
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101000000010101L;
                                lowW += 0x0101010101010101L;
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                lowW += 0x0101010101010101L;
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                lowW += 0x0101010101010101L;
                                lowNW += 0x0001010101010000L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0101010101010101L;
                                
                            }
                            case 1 -> {
                                lowN += 0x0101010100000001L;
                                lowNE += 0x0101010000000000L;
                                
                                
                                
                                
                                
                                lowNW += 0x0101010101010101L;
                                
                            }
                            case 2 -> {
                                lowN += 0x0101010100000001L;
                                lowNE += 0x0101010000000000L;
                                lowE += 0x0101000000000000L;
                                lowSE += 0x0100000000000000L;
                                
                                
                                
                                lowNW += 0x0101010101010101L;
                                lowC += 0x0101000000000001L;
                            }
                            case 3 -> {
                                lowN += 0x0101010100000001L;
                                lowNE += 0x0101010000000000L;
                                lowE += 0x0101000000000000L;
                                lowSE += 0x0100000000000000L;
                                lowS += 0x0100000000000001L;
                                lowSW += 0x0100000000000101L;
                                lowW += 0x0101000000010101L;
                                lowNW += 0x0101010101010101L;
                                lowC += 0x0101000000000001L;
                            }
                            case 4 -> {
                                
                                
                                
                                lowSE += 0x0100000000000000L;
                                lowS += 0x0100000000000001L;
                                lowSW += 0x0100000000000101L;
                                lowW += 0x0101000000010101L;
                                lowNW += 0x0101010101010101L;
                                lowC += 0x0101000000000001L;
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0100000000000101L;
                                lowW += 0x0101000000010101L;
                                lowNW += 0x0101010101010101L;
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0101010101010101L;
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0101010101010101L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                lowN += 0x0101000000000001L;
                                lowNE += 0x0101000000000000L;
                                lowE += 0x0100000000000000L;
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                lowN += 0x0101000000000001L;
                                lowNE += 0x0101000000000000L;
                                lowE += 0x0100000000000000L;
                                
                                
                                
                                lowW += 0x0100000000000101L;
                                lowNW += 0x0101000000010101L;
                                lowC += 0x0100000000000001L;
                            }
                            case 4 -> {
                                lowN += 0x0101000000000001L;
                                
                                lowE += 0x0100000000000000L;
                                
                                
                                
                                lowW += 0x0100000000000101L;
                                lowNW += 0x0101000000010101L;
                                lowC += 0x0100000000000001L;
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                lowW += 0x0100000000000101L;
                                lowNW += 0x0101000000010101L;
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                lowNE += 0x0100000000000000L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                lowN += 0x0100000000000001L;
                                lowNE += 0x0100000000000000L;
                                
                                
                                
                                
                                
                                lowNW += 0x0100000000000101L;
                                
                            }
                            case 4 -> {
                                lowN += 0x0100000000000001L;
                                lowNE += 0x0100000000000000L;
                                
                                
                                
                                
                                
                                lowNW += 0x0100000000000101L;
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0100000000000101L;
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case 0 -> {
                switch (dy) {
                    case -3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                lowSE += 0x0000010100000000L;
                                lowS += 0x0000010101000000L;
                                lowSW += 0x0000000101000000L;
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                lowSE += 0x0000010100000000L;
                                lowS += 0x0000010101000000L;
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                lowS += 0x0000010101000000L;
                                lowSW += 0x0000000101000000L;
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                lowE += 0x0000010100000000L;
                                lowSE += 0x0001010100000000L;
                                lowS += 0x0001010101010000L;
                                lowSW += 0x0000000101010000L;
                                lowW += 0x0000000101000000L;
                                
                                lowC += 0x0000010101000000L;
                            }
                            case 1 -> {
                                
                                
                                lowE += 0x0000010100000000L;
                                lowSE += 0x0001010100000000L;
                                lowS += 0x0001010101010000L;
                                
                                
                                
                                lowC += 0x0000010101000000L;
                            }
                            case 2 -> {
                                
                                
                                
                                lowSE += 0x0001010100000000L;
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0000000101010000L;
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                lowS += 0x0001010101010000L;
                                lowSW += 0x0000000101010000L;
                                lowW += 0x0000000101000000L;
                                
                                lowC += 0x0000010101000000L;
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                lowN += 0x0000010101000000L;
                                lowNE += 0x0000010100000000L;
                                lowE += 0x0001010100000000L;
                                
                                lowS += 0x0101010101010101L;
                                
                                lowW += 0x0000000101010000L;
                                lowNW += 0x0000000101000000L;
                                lowC += 0x0001010101010000L;
                            }
                            case 1 -> {
                                lowN += 0x0000010101000000L;
                                lowNE += 0x0000010100000000L;
                                lowE += 0x0001010100000000L;
                                lowSE += 0x0101010100000001L;
                                lowS += 0x0101010101010101L;
                                
                                
                                
                                lowC += 0x0001010101010000L;
                            }
                            case 2 -> {
                                
                                
                                lowE += 0x0001010100000000L;
                                lowSE += 0x0101010100000001L;
                                lowS += 0x0101010101010101L;
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                lowSE += 0x0101010100000001L;
                                lowS += 0x0101010101010101L;
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                lowS += 0x0101010101010101L;
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                lowS += 0x0101010101010101L;
                                lowSW += 0x0000000101010101L;
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                lowS += 0x0101010101010101L;
                                lowSW += 0x0000000101010101L;
                                lowW += 0x0000000101010000L;
                                
                                
                            }
                            case 7 -> {
                                lowN += 0x0000010101000000L;
                                
                                
                                
                                lowS += 0x0101010101010101L;
                                lowSW += 0x0000000101010101L;
                                lowW += 0x0000000101010000L;
                                lowNW += 0x0000000101000000L;
                                lowC += 0x0001010101010000L;
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                lowN += 0x0001010101010000L;
                                lowNE += 0x0001010100000000L;
                                
                                
                                
                                
                                
                                lowNW += 0x0000000101010000L;
                                lowC += 0x0101010101010101L;
                            }
                            case 1 -> {
                                lowN += 0x0001010101010000L;
                                lowNE += 0x0001010100000000L;
                                lowE += 0x0101010100000001L;
                                
                                
                                
                                
                                
                                lowC += 0x0101010101010101L;
                            }
                            case 2 -> {
                                
                                lowNE += 0x0001010100000000L;
                                lowE += 0x0101010100000001L;
                                lowSE += 0x0101000000000001L;
                                
                                
                                
                                
                                lowC += 0x0101010101010101L;
                            }
                            case 3 -> {
                                
                                
                                lowE += 0x0101010100000001L;
                                lowSE += 0x0101000000000001L;
                                lowS += 0x0101000000010101L;
                                
                                
                                
                                lowC += 0x0101010101010101L;
                            }
                            case 4 -> {
                                
                                
                                
                                lowSE += 0x0101000000000001L;
                                lowS += 0x0101000000010101L;
                                lowSW += 0x0000000000010101L;
                                
                                
                                lowC += 0x0101010101010101L;
                            }
                            case 5 -> {
                                
                                
                                
                                
                                lowS += 0x0101000000010101L;
                                lowSW += 0x0000000000010101L;
                                lowW += 0x0000000101010101L;
                                
                                lowC += 0x0101010101010101L;
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0000000000010101L;
                                lowW += 0x0000000101010101L;
                                lowNW += 0x0000000101010000L;
                                lowC += 0x0101010101010101L;
                            }
                            case 7 -> {
                                lowN += 0x0001010101010000L;
                                
                                
                                
                                
                                
                                lowW += 0x0000000101010101L;
                                lowNW += 0x0000000101010000L;
                                lowC += 0x0101010101010101L;
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                lowN += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                lowN += 0x0101010101010101L;
                                lowNE += 0x0101010100000001L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                lowN += 0x0101010101010101L;
                                lowNE += 0x0101010100000001L;
                                lowE += 0x0101000000000001L;
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                lowN += 0x0101010101010101L;
                                lowNE += 0x0101010100000001L;
                                lowE += 0x0101000000000001L;
                                lowSE += 0x0100000000000001L;
                                lowS += 0x0100000000000101L;
                                
                                
                                
                                lowC += 0x0101000000010101L;
                            }
                            case 4 -> {
                                lowN += 0x0101010101010101L;
                                
                                lowE += 0x0101000000000001L;
                                lowSE += 0x0100000000000001L;
                                lowS += 0x0100000000000101L;
                                lowSW += 0x0000000000000101L;
                                lowW += 0x0000000000010101L;
                                
                                lowC += 0x0101000000010101L;
                            }
                            case 5 -> {
                                lowN += 0x0101010101010101L;
                                
                                
                                
                                lowS += 0x0100000000000101L;
                                lowSW += 0x0000000000000101L;
                                lowW += 0x0000000000010101L;
                                lowNW += 0x0000000101010101L;
                                lowC += 0x0101000000010101L;
                            }
                            case 6 -> {
                                lowN += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                lowW += 0x0000000000010101L;
                                lowNW += 0x0000000101010101L;
                                
                            }
                            case 7 -> {
                                lowN += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0000000101010101L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                lowNE += 0x0101000000000001L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                lowN += 0x0101000000010101L;
                                lowNE += 0x0101000000000001L;
                                lowE += 0x0100000000000001L;
                                
                                
                                
                                
                                
                                lowC += 0x0100000000000101L;
                            }
                            case 4 -> {
                                lowN += 0x0101000000010101L;
                                lowNE += 0x0101000000000001L;
                                lowE += 0x0100000000000001L;
                                
                                
                                
                                lowW += 0x0000000000000101L;
                                lowNW += 0x0000000000010101L;
                                lowC += 0x0100000000000101L;
                            }
                            case 5 -> {
                                lowN += 0x0101000000010101L;
                                
                                
                                
                                
                                
                                lowW += 0x0000000000000101L;
                                lowNW += 0x0000000000010101L;
                                lowC += 0x0100000000000101L;
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0000000000010101L;
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                lowN += 0x0100000000000101L;
                                lowNE += 0x0100000000000001L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                lowN += 0x0100000000000101L;
                                lowNE += 0x0100000000000001L;
                                
                                
                                
                                
                                
                                lowNW += 0x0000000000000101L;
                                
                            }
                            case 5 -> {
                                lowN += 0x0100000000000101L;
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0000000000000101L;
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case 1 -> {
                switch (dy) {
                    case -3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                lowSE += 0x0000010101000000L;
                                lowS += 0x0000000101000000L;
                                lowSW += 0x0000000001000000L;
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                lowSE += 0x0000010101000000L;
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0000000001000000L;
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                lowSE += 0x0000010101000000L;
                                lowS += 0x0000000101000000L;
                                lowSW += 0x0000000001000000L;
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                lowE += 0x0000010101000000L;
                                lowSE += 0x0001010101010000L;
                                lowS += 0x0000000101010000L;
                                
                                lowW += 0x0000000001000000L;
                                
                                lowC += 0x0000000101000000L;
                            }
                            case 1 -> {
                                
                                
                                lowE += 0x0000010101000000L;
                                lowSE += 0x0001010101010000L;
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                lowS += 0x0000000101010000L;
                                lowSW += 0x0000000001010000L;
                                lowW += 0x0000000001000000L;
                                
                                
                            }
                            case 7 -> {
                                
                                
                                lowE += 0x0000010101000000L;
                                lowSE += 0x0001010101010000L;
                                lowS += 0x0000000101010000L;
                                lowSW += 0x0000000001010000L;
                                lowW += 0x0000000001000000L;
                                
                                lowC += 0x0000000101000000L;
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                lowN += 0x0000000101000000L;
                                lowNE += 0x0000010101000000L;
                                lowE += 0x0001010101010000L;
                                lowSE += 0x0101010101010101L;
                                
                                
                                
                                lowNW += 0x0000000001000000L;
                                lowC += 0x0000000101010000L;
                            }
                            case 1 -> {
                                
                                lowNE += 0x0000010101000000L;
                                lowE += 0x0001010101010000L;
                                lowSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                lowSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                lowSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                lowSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                lowSE += 0x0101010101010101L;
                                lowS += 0x0000000101010101L;
                                lowSW += 0x0000000001010100L;
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                lowSE += 0x0101010101010101L;
                                lowS += 0x0000000101010101L;
                                lowSW += 0x0000000001010100L;
                                lowW += 0x0000000001010000L;
                                lowNW += 0x0000000001000000L;
                                lowC += 0x0000000101010000L;
                            }
                            case 7 -> {
                                lowN += 0x0000000101000000L;
                                lowNE += 0x0000010101000000L;
                                lowE += 0x0001010101010000L;
                                lowSE += 0x0101010101010101L;
                                lowS += 0x0000000101010101L;
                                lowSW += 0x0000000001010100L;
                                lowW += 0x0000000001010000L;
                                lowNW += 0x0000000001000000L;
                                lowC += 0x0000000101010000L;
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                lowN += 0x0000000101010000L;
                                lowNE += 0x0001010101010000L;
                                lowE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                lowNE += 0x0001010101010000L;
                                lowE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                lowE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                lowE += 0x0101010101010101L;
                                lowSE += 0x0101000000010101L;
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                lowE += 0x0101010101010101L;
                                lowSE += 0x0101000000010101L;
                                lowS += 0x0000000000010101L;
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                lowE += 0x0101010101010101L;
                                lowSE += 0x0101000000010101L;
                                lowS += 0x0000000000010101L;
                                lowSW += 0x0000000000010100L;
                                lowW += 0x0000000001010100L;
                                
                                lowC += 0x0000000101010101L;
                            }
                            case 6 -> {
                                lowN += 0x0000000101010000L;
                                
                                lowE += 0x0101010101010101L;
                                
                                lowS += 0x0000000000010101L;
                                lowSW += 0x0000000000010100L;
                                lowW += 0x0000000001010100L;
                                lowNW += 0x0000000001010000L;
                                lowC += 0x0000000101010101L;
                            }
                            case 7 -> {
                                lowN += 0x0000000101010000L;
                                lowNE += 0x0001010101010000L;
                                lowE += 0x0101010101010101L;
                                
                                
                                
                                lowW += 0x0000000001010100L;
                                lowNW += 0x0000000001010000L;
                                lowC += 0x0000000101010101L;
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                lowNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                lowNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                lowNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                lowNE += 0x0101010101010101L;
                                lowE += 0x0101000000010101L;
                                lowSE += 0x0100000000000101L;
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                lowNE += 0x0101010101010101L;
                                lowE += 0x0101000000010101L;
                                lowSE += 0x0100000000000101L;
                                lowS += 0x0000000000000101L;
                                lowSW += 0x0000000000000100L;
                                
                                
                                lowC += 0x0000000000010101L;
                            }
                            case 5 -> {
                                lowN += 0x0000000101010101L;
                                lowNE += 0x0101010101010101L;
                                lowE += 0x0101000000010101L;
                                lowSE += 0x0100000000000101L;
                                lowS += 0x0000000000000101L;
                                lowSW += 0x0000000000000100L;
                                lowW += 0x0000000000010100L;
                                lowNW += 0x0000000001010100L;
                                lowC += 0x0000000000010101L;
                            }
                            case 6 -> {
                                lowN += 0x0000000101010101L;
                                lowNE += 0x0101010101010101L;
                                
                                
                                
                                lowSW += 0x0000000000000100L;
                                lowW += 0x0000000000010100L;
                                lowNW += 0x0000000001010100L;
                                lowC += 0x0000000000010101L;
                            }
                            case 7 -> {
                                lowN += 0x0000000101010101L;
                                lowNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                lowNW += 0x0000000001010100L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                lowNE += 0x0101000000010101L;
                                lowE += 0x0100000000000101L;
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                lowN += 0x0000000000010101L;
                                lowNE += 0x0101000000010101L;
                                lowE += 0x0100000000000101L;
                                
                                
                                
                                lowW += 0x0000000000000100L;
                                
                                lowC += 0x0000000000000101L;
                            }
                            case 5 -> {
                                lowN += 0x0000000000010101L;
                                lowNE += 0x0101000000010101L;
                                lowE += 0x0100000000000101L;
                                
                                
                                
                                lowW += 0x0000000000000100L;
                                lowNW += 0x0000000000010100L;
                                lowC += 0x0000000000000101L;
                            }
                            case 6 -> {
                                lowN += 0x0000000000010101L;
                                
                                
                                
                                
                                
                                lowW += 0x0000000000000100L;
                                lowNW += 0x0000000000010100L;
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                lowNE += 0x0100000000000101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                lowN += 0x0000000000000101L;
                                lowNE += 0x0100000000000101L;
                                
                                
                                
                                
                                
                                lowNW += 0x0000000000000100L;
                                
                            }
                            case 5 -> {
                                lowN += 0x0000000000000101L;
                                lowNE += 0x0100000000000101L;
                                
                                
                                
                                
                                
                                lowNW += 0x0000000000000100L;
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0000000000000100L;
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case 2 -> {
                switch (dy) {
                    case -3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                lowSE += 0x0000000101000000L;
                                lowS += 0x0000000001000000L;
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                lowS += 0x0000000001000000L;
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                lowSE += 0x0000000101000000L;
                                lowS += 0x0000000001000000L;
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                lowE += 0x0000000101000000L;
                                lowSE += 0x0000000101010000L;
                                
                                
                                
                                
                                lowC += 0x0000000001000000L;
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                lowSE += 0x0000000101010000L;
                                lowS += 0x0000000001010000L;
                                
                                
                                
                                lowC += 0x0000000001000000L;
                            }
                            case 7 -> {
                                
                                
                                lowE += 0x0000000101000000L;
                                lowSE += 0x0000000101010000L;
                                lowS += 0x0000000001010000L;
                                
                                
                                
                                lowC += 0x0000000001000000L;
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                lowN += 0x0000000001000000L;
                                lowNE += 0x0000000101000000L;
                                lowE += 0x0000000101010000L;
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                lowSE += 0x0000000101010101L;
                                lowS += 0x0000000001010100L;
                                
                                
                                
                                
                            }
                            case 6 -> {
                                lowN += 0x0000000001000000L;
                                
                                lowE += 0x0000000101010000L;
                                lowSE += 0x0000000101010101L;
                                lowS += 0x0000000001010100L;
                                
                                
                                
                                lowC += 0x0000000001010000L;
                            }
                            case 7 -> {
                                lowN += 0x0000000001000000L;
                                lowNE += 0x0000000101000000L;
                                lowE += 0x0000000101010000L;
                                lowSE += 0x0000000101010101L;
                                lowS += 0x0000000001010100L;
                                
                                
                                
                                lowC += 0x0000000001010000L;
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                lowNE += 0x0000000101010000L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                lowSE += 0x0000000000010101L;
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                lowE += 0x0000000101010101L;
                                lowSE += 0x0000000000010101L;
                                lowS += 0x0000000000010100L;
                                
                                
                                
                                lowC += 0x0000000001010100L;
                            }
                            case 6 -> {
                                lowN += 0x0000000001010000L;
                                lowNE += 0x0000000101010000L;
                                lowE += 0x0000000101010101L;
                                lowSE += 0x0000000000010101L;
                                lowS += 0x0000000000010100L;
                                
                                
                                
                                lowC += 0x0000000001010100L;
                            }
                            case 7 -> {
                                lowN += 0x0000000001010000L;
                                lowNE += 0x0000000101010000L;
                                lowE += 0x0000000101010101L;
                                
                                
                                
                                
                                
                                lowC += 0x0000000001010100L;
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                lowE += 0x0000000000010101L;
                                lowSE += 0x0000000000000101L;
                                lowS += 0x0000000000000100L;
                                
                                
                                
                                
                            }
                            case 5 -> {
                                lowN += 0x0000000001010100L;
                                lowNE += 0x0000000101010101L;
                                lowE += 0x0000000000010101L;
                                lowSE += 0x0000000000000101L;
                                lowS += 0x0000000000000100L;
                                
                                
                                
                                lowC += 0x0000000000010100L;
                            }
                            case 6 -> {
                                lowN += 0x0000000001010100L;
                                lowNE += 0x0000000101010101L;
                                lowE += 0x0000000000010101L;
                                
                                lowS += 0x0000000000000100L;
                                
                                
                                
                                lowC += 0x0000000000010100L;
                            }
                            case 7 -> {
                                lowN += 0x0000000001010100L;
                                lowNE += 0x0000000101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                lowNE += 0x0000000000010101L;
                                lowE += 0x0000000000000101L;
                                
                                
                                
                                
                                
                                lowC += 0x0000000000000100L;
                            }
                            case 5 -> {
                                lowN += 0x0000000000010100L;
                                lowNE += 0x0000000000010101L;
                                lowE += 0x0000000000000101L;
                                
                                
                                
                                
                                
                                lowC += 0x0000000000000100L;
                            }
                            case 6 -> {
                                lowN += 0x0000000000010100L;
                                lowNE += 0x0000000000010101L;
                                
                                
                                
                                
                                
                                
                                lowC += 0x0000000000000100L;
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                lowN += 0x0000000000000100L;
                                lowNE += 0x0000000000000101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                lowN += 0x0000000000000100L;
                                lowNE += 0x0000000000000101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                lowN += 0x0000000000000100L;
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case 3 -> {
                switch (dy) {
                    case -3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                lowSE += 0x0000000001000000L;
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                lowSE += 0x0000000001000000L;
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                lowSE += 0x0000000001000000L;
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                lowE += 0x0000000001000000L;
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                lowE += 0x0000000001000000L;
                                lowSE += 0x0000000001010000L;
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                lowE += 0x0000000001000000L;
                                lowSE += 0x0000000001010000L;
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                lowNE += 0x0000000001000000L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                lowSE += 0x0000000001010100L;
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                lowNE += 0x0000000001000000L;
                                lowE += 0x0000000001010000L;
                                lowSE += 0x0000000001010100L;
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                lowNE += 0x0000000001000000L;
                                lowE += 0x0000000001010000L;
                                lowSE += 0x0000000001010100L;
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                lowE += 0x0000000001010100L;
                                lowSE += 0x0000000000010100L;
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                lowNE += 0x0000000001010000L;
                                lowE += 0x0000000001010100L;
                                lowSE += 0x0000000000010100L;
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                lowNE += 0x0000000001010000L;
                                lowE += 0x0000000001010100L;
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                lowSE += 0x0000000000000100L;
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                lowNE += 0x0000000001010100L;
                                lowE += 0x0000000000010100L;
                                lowSE += 0x0000000000000100L;
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                lowNE += 0x0000000001010100L;
                                lowE += 0x0000000000010100L;
                                lowSE += 0x0000000000000100L;
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                lowNE += 0x0000000001010100L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                lowE += 0x0000000000000100L;
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                lowNE += 0x0000000000010100L;
                                lowE += 0x0000000000000100L;
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                lowNE += 0x0000000000010100L;
                                lowE += 0x0000000000000100L;
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                lowNE += 0x0000000000000100L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                lowNE += 0x0000000000000100L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                lowNE += 0x0000000000000100L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            default -> { }
        }
    }

    public static void registerEnemyHigh(int dx, int dy, Direction facing) {
        switch (dx) {
            case -3 -> {
                switch (dy) {
                    case -3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0000010000000000L;
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0000010000000000L;
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0000010000000000L;
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                highW += 0x0000010000000000L;
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0001010000000000L;
                                highW += 0x0000010000000000L;
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0001010000000000L;
                                highW += 0x0000010000000000L;
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                highNW += 0x0000010000000000L;
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101010000000000L;
                                highW += 0x0001010000000000L;
                                highNW += 0x0000010000000000L;
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101010000000000L;
                                highW += 0x0001010000000000L;
                                highNW += 0x0000010000000000L;
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101010000000000L;
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                highW += 0x0101010000000000L;
                                highNW += 0x0001010000000000L;
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101000000000000L;
                                highW += 0x0101010000000000L;
                                highNW += 0x0001010000000000L;
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101000000000000L;
                                highW += 0x0101010000000000L;
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                highNW += 0x0101010000000000L;
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0100000000000000L;
                                highW += 0x0101000000000000L;
                                highNW += 0x0101010000000000L;
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0100000000000000L;
                                highW += 0x0101000000000000L;
                                highNW += 0x0101010000000000L;
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0100000000000000L;
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                highW += 0x0100000000000000L;
                                highNW += 0x0101000000000000L;
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                highW += 0x0100000000000000L;
                                highNW += 0x0101000000000000L;
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                highW += 0x0100000000000000L;
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                highNW += 0x0100000000000000L;
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                highNW += 0x0100000000000000L;
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                highNW += 0x0100000000000000L;
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case -2 -> {
                switch (dy) {
                    case -3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                highS += 0x0000010000000000L;
                                highSW += 0x0000010100000000L;
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                highS += 0x0000010000000000L;
                                highSW += 0x0000010100000000L;
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                highS += 0x0000010000000000L;
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0001010100000000L;
                                highW += 0x0000010100000000L;
                                
                                highC += 0x0000010000000000L;
                            }
                            case 1 -> {
                                
                                
                                
                                
                                highS += 0x0001010000000000L;
                                highSW += 0x0001010100000000L;
                                highW += 0x0000010100000000L;
                                
                                highC += 0x0000010000000000L;
                            }
                            case 2 -> {
                                
                                
                                
                                
                                highS += 0x0001010000000000L;
                                highSW += 0x0001010100000000L;
                                
                                
                                highC += 0x0000010000000000L;
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                highN += 0x0000010000000000L;
                                
                                
                                
                                
                                
                                highW += 0x0001010100000000L;
                                highNW += 0x0000010100000000L;
                                
                            }
                            case 1 -> {
                                highN += 0x0000010000000000L;
                                
                                
                                
                                highS += 0x0101010000000000L;
                                highSW += 0x0101010100000001L;
                                highW += 0x0001010100000000L;
                                highNW += 0x0000010100000000L;
                                highC += 0x0001010000000000L;
                            }
                            case 2 -> {
                                highN += 0x0000010000000000L;
                                
                                
                                
                                highS += 0x0101010000000000L;
                                highSW += 0x0101010100000001L;
                                highW += 0x0001010100000000L;
                                
                                highC += 0x0001010000000000L;
                            }
                            case 3 -> {
                                
                                
                                
                                
                                highS += 0x0101010000000000L;
                                highSW += 0x0101010100000001L;
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                highNW += 0x0001010100000000L;
                                
                            }
                            case 1 -> {
                                highN += 0x0001010000000000L;
                                
                                
                                
                                
                                
                                highW += 0x0101010100000001L;
                                highNW += 0x0001010100000000L;
                                highC += 0x0101010000000000L;
                            }
                            case 2 -> {
                                highN += 0x0001010000000000L;
                                
                                
                                
                                highS += 0x0101000000000000L;
                                highSW += 0x0101000000000001L;
                                highW += 0x0101010100000001L;
                                highNW += 0x0001010100000000L;
                                highC += 0x0101010000000000L;
                            }
                            case 3 -> {
                                
                                
                                
                                
                                highS += 0x0101000000000000L;
                                highSW += 0x0101000000000001L;
                                highW += 0x0101010100000001L;
                                
                                highC += 0x0101010000000000L;
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101000000000001L;
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                highN += 0x0101010000000000L;
                                
                                
                                
                                
                                
                                
                                highNW += 0x0101010100000001L;
                                
                            }
                            case 2 -> {
                                highN += 0x0101010000000000L;
                                
                                
                                
                                highS += 0x0100000000000000L;
                                
                                highW += 0x0101000000000001L;
                                highNW += 0x0101010100000001L;
                                highC += 0x0101000000000000L;
                            }
                            case 3 -> {
                                highN += 0x0101010000000000L;
                                
                                
                                
                                highS += 0x0100000000000000L;
                                highSW += 0x0100000000000001L;
                                highW += 0x0101000000000001L;
                                highNW += 0x0101010100000001L;
                                highC += 0x0101000000000000L;
                            }
                            case 4 -> {
                                
                                
                                
                                
                                highS += 0x0100000000000000L;
                                highSW += 0x0100000000000001L;
                                highW += 0x0101000000000001L;
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                highN += 0x0101000000000000L;
                                
                                
                                
                                
                                
                                
                                highNW += 0x0101000000000001L;
                                highC += 0x0100000000000000L;
                            }
                            case 3 -> {
                                highN += 0x0101000000000000L;
                                
                                
                                
                                
                                
                                highW += 0x0100000000000001L;
                                highNW += 0x0101000000000001L;
                                highC += 0x0100000000000000L;
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                highW += 0x0100000000000001L;
                                highNW += 0x0101000000000001L;
                                highC += 0x0100000000000000L;
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                highN += 0x0100000000000000L;
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                highN += 0x0100000000000000L;
                                
                                
                                
                                
                                
                                
                                highNW += 0x0100000000000001L;
                                
                            }
                            case 4 -> {
                                highN += 0x0100000000000000L;
                                
                                
                                
                                
                                
                                
                                highNW += 0x0100000000000001L;
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case -1 -> {
                switch (dy) {
                    case -3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                highSE += 0x0000010000000000L;
                                highS += 0x0000010100000000L;
                                highSW += 0x0000010101000000L;
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                highSE += 0x0000010000000000L;
                                highS += 0x0000010100000000L;
                                highSW += 0x0000010101000000L;
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                highSE += 0x0000010000000000L;
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0000010101000000L;
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                highE += 0x0000010000000000L;
                                
                                highS += 0x0001010100000000L;
                                highSW += 0x0001010101010000L;
                                highW += 0x0000010101000000L;
                                
                                highC += 0x0000010100000000L;
                            }
                            case 1 -> {
                                
                                
                                highE += 0x0000010000000000L;
                                highSE += 0x0001010000000000L;
                                highS += 0x0001010100000000L;
                                highSW += 0x0001010101010000L;
                                highW += 0x0000010101000000L;
                                
                                highC += 0x0000010100000000L;
                            }
                            case 2 -> {
                                
                                
                                highE += 0x0000010000000000L;
                                highSE += 0x0001010000000000L;
                                highS += 0x0001010100000000L;
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0001010101010000L;
                                highW += 0x0000010101000000L;
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                highN += 0x0000010100000000L;
                                highNE += 0x0000010000000000L;
                                
                                
                                
                                highSW += 0x0101010101010101L;
                                highW += 0x0001010101010000L;
                                highNW += 0x0000010101000000L;
                                highC += 0x0001010100000000L;
                            }
                            case 1 -> {
                                highN += 0x0000010100000000L;
                                highNE += 0x0000010000000000L;
                                highE += 0x0001010000000000L;
                                highSE += 0x0101010000000000L;
                                highS += 0x0101010100000001L;
                                highSW += 0x0101010101010101L;
                                highW += 0x0001010101010000L;
                                highNW += 0x0000010101000000L;
                                highC += 0x0001010100000000L;
                            }
                            case 2 -> {
                                
                                highNE += 0x0000010000000000L;
                                highE += 0x0001010000000000L;
                                highSE += 0x0101010000000000L;
                                highS += 0x0101010100000001L;
                                highSW += 0x0101010101010101L;
                                
                                
                                highC += 0x0001010100000000L;
                            }
                            case 3 -> {
                                
                                
                                
                                highSE += 0x0101010000000000L;
                                highS += 0x0101010100000001L;
                                highSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101010101010101L;
                                highW += 0x0001010101010000L;
                                highNW += 0x0000010101000000L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                highN += 0x0001010100000000L;
                                
                                
                                
                                
                                
                                highW += 0x0101010101010101L;
                                highNW += 0x0001010101010000L;
                                
                            }
                            case 1 -> {
                                highN += 0x0001010100000000L;
                                highNE += 0x0001010000000000L;
                                highE += 0x0101010000000000L;
                                
                                
                                
                                highW += 0x0101010101010101L;
                                highNW += 0x0001010101010000L;
                                highC += 0x0101010100000001L;
                            }
                            case 2 -> {
                                highN += 0x0001010100000000L;
                                highNE += 0x0001010000000000L;
                                highE += 0x0101010000000000L;
                                highSE += 0x0101000000000000L;
                                highS += 0x0101000000000001L;
                                
                                highW += 0x0101010101010101L;
                                
                                highC += 0x0101010100000001L;
                            }
                            case 3 -> {
                                
                                
                                highE += 0x0101010000000000L;
                                highSE += 0x0101000000000000L;
                                highS += 0x0101000000000001L;
                                highSW += 0x0101000000010101L;
                                highW += 0x0101010101010101L;
                                
                                highC += 0x0101010100000001L;
                            }
                            case 4 -> {
                                
                                
                                
                                
                                highS += 0x0101000000000001L;
                                highSW += 0x0101000000010101L;
                                highW += 0x0101010101010101L;
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101000000010101L;
                                highW += 0x0101010101010101L;
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                highW += 0x0101010101010101L;
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                highW += 0x0101010101010101L;
                                highNW += 0x0001010101010000L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                highNW += 0x0101010101010101L;
                                
                            }
                            case 1 -> {
                                highN += 0x0101010100000001L;
                                highNE += 0x0101010000000000L;
                                
                                
                                
                                
                                
                                highNW += 0x0101010101010101L;
                                
                            }
                            case 2 -> {
                                highN += 0x0101010100000001L;
                                highNE += 0x0101010000000000L;
                                highE += 0x0101000000000000L;
                                highSE += 0x0100000000000000L;
                                
                                
                                
                                highNW += 0x0101010101010101L;
                                highC += 0x0101000000000001L;
                            }
                            case 3 -> {
                                highN += 0x0101010100000001L;
                                highNE += 0x0101010000000000L;
                                highE += 0x0101000000000000L;
                                highSE += 0x0100000000000000L;
                                highS += 0x0100000000000001L;
                                highSW += 0x0100000000000101L;
                                highW += 0x0101000000010101L;
                                highNW += 0x0101010101010101L;
                                highC += 0x0101000000000001L;
                            }
                            case 4 -> {
                                
                                
                                
                                highSE += 0x0100000000000000L;
                                highS += 0x0100000000000001L;
                                highSW += 0x0100000000000101L;
                                highW += 0x0101000000010101L;
                                highNW += 0x0101010101010101L;
                                highC += 0x0101000000000001L;
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0100000000000101L;
                                highW += 0x0101000000010101L;
                                highNW += 0x0101010101010101L;
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                highNW += 0x0101010101010101L;
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                highNW += 0x0101010101010101L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                highN += 0x0101000000000001L;
                                highNE += 0x0101000000000000L;
                                highE += 0x0100000000000000L;
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                highN += 0x0101000000000001L;
                                highNE += 0x0101000000000000L;
                                highE += 0x0100000000000000L;
                                
                                
                                
                                highW += 0x0100000000000101L;
                                highNW += 0x0101000000010101L;
                                highC += 0x0100000000000001L;
                            }
                            case 4 -> {
                                highN += 0x0101000000000001L;
                                
                                highE += 0x0100000000000000L;
                                
                                
                                
                                highW += 0x0100000000000101L;
                                highNW += 0x0101000000010101L;
                                highC += 0x0100000000000001L;
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                highW += 0x0100000000000101L;
                                highNW += 0x0101000000010101L;
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                highNE += 0x0100000000000000L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                highN += 0x0100000000000001L;
                                highNE += 0x0100000000000000L;
                                
                                
                                
                                
                                
                                highNW += 0x0100000000000101L;
                                
                            }
                            case 4 -> {
                                highN += 0x0100000000000001L;
                                highNE += 0x0100000000000000L;
                                
                                
                                
                                
                                
                                highNW += 0x0100000000000101L;
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                highNW += 0x0100000000000101L;
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case 0 -> {
                switch (dy) {
                    case -3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                highSE += 0x0000010100000000L;
                                highS += 0x0000010101000000L;
                                highSW += 0x0000000101000000L;
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                highSE += 0x0000010100000000L;
                                highS += 0x0000010101000000L;
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                highS += 0x0000010101000000L;
                                highSW += 0x0000000101000000L;
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                highE += 0x0000010100000000L;
                                highSE += 0x0001010100000000L;
                                highS += 0x0001010101010000L;
                                highSW += 0x0000000101010000L;
                                highW += 0x0000000101000000L;
                                
                                highC += 0x0000010101000000L;
                            }
                            case 1 -> {
                                
                                
                                highE += 0x0000010100000000L;
                                highSE += 0x0001010100000000L;
                                highS += 0x0001010101010000L;
                                
                                
                                
                                highC += 0x0000010101000000L;
                            }
                            case 2 -> {
                                
                                
                                
                                highSE += 0x0001010100000000L;
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0000000101010000L;
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                highS += 0x0001010101010000L;
                                highSW += 0x0000000101010000L;
                                highW += 0x0000000101000000L;
                                
                                highC += 0x0000010101000000L;
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                highN += 0x0000010101000000L;
                                highNE += 0x0000010100000000L;
                                highE += 0x0001010100000000L;
                                
                                highS += 0x0101010101010101L;
                                
                                highW += 0x0000000101010000L;
                                highNW += 0x0000000101000000L;
                                highC += 0x0001010101010000L;
                            }
                            case 1 -> {
                                highN += 0x0000010101000000L;
                                highNE += 0x0000010100000000L;
                                highE += 0x0001010100000000L;
                                highSE += 0x0101010100000001L;
                                highS += 0x0101010101010101L;
                                
                                
                                
                                highC += 0x0001010101010000L;
                            }
                            case 2 -> {
                                
                                
                                highE += 0x0001010100000000L;
                                highSE += 0x0101010100000001L;
                                highS += 0x0101010101010101L;
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                highSE += 0x0101010100000001L;
                                highS += 0x0101010101010101L;
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                highS += 0x0101010101010101L;
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                highS += 0x0101010101010101L;
                                highSW += 0x0000000101010101L;
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                highS += 0x0101010101010101L;
                                highSW += 0x0000000101010101L;
                                highW += 0x0000000101010000L;
                                
                                
                            }
                            case 7 -> {
                                highN += 0x0000010101000000L;
                                
                                
                                
                                highS += 0x0101010101010101L;
                                highSW += 0x0000000101010101L;
                                highW += 0x0000000101010000L;
                                highNW += 0x0000000101000000L;
                                highC += 0x0001010101010000L;
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                highN += 0x0001010101010000L;
                                highNE += 0x0001010100000000L;
                                
                                
                                
                                
                                
                                highNW += 0x0000000101010000L;
                                highC += 0x0101010101010101L;
                            }
                            case 1 -> {
                                highN += 0x0001010101010000L;
                                highNE += 0x0001010100000000L;
                                highE += 0x0101010100000001L;
                                
                                
                                
                                
                                
                                highC += 0x0101010101010101L;
                            }
                            case 2 -> {
                                
                                highNE += 0x0001010100000000L;
                                highE += 0x0101010100000001L;
                                highSE += 0x0101000000000001L;
                                
                                
                                
                                
                                highC += 0x0101010101010101L;
                            }
                            case 3 -> {
                                
                                
                                highE += 0x0101010100000001L;
                                highSE += 0x0101000000000001L;
                                highS += 0x0101000000010101L;
                                
                                
                                
                                highC += 0x0101010101010101L;
                            }
                            case 4 -> {
                                
                                
                                
                                highSE += 0x0101000000000001L;
                                highS += 0x0101000000010101L;
                                highSW += 0x0000000000010101L;
                                
                                
                                highC += 0x0101010101010101L;
                            }
                            case 5 -> {
                                
                                
                                
                                
                                highS += 0x0101000000010101L;
                                highSW += 0x0000000000010101L;
                                highW += 0x0000000101010101L;
                                
                                highC += 0x0101010101010101L;
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0000000000010101L;
                                highW += 0x0000000101010101L;
                                highNW += 0x0000000101010000L;
                                highC += 0x0101010101010101L;
                            }
                            case 7 -> {
                                highN += 0x0001010101010000L;
                                
                                
                                
                                
                                
                                highW += 0x0000000101010101L;
                                highNW += 0x0000000101010000L;
                                highC += 0x0101010101010101L;
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                highN += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                highN += 0x0101010101010101L;
                                highNE += 0x0101010100000001L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                highN += 0x0101010101010101L;
                                highNE += 0x0101010100000001L;
                                highE += 0x0101000000000001L;
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                highN += 0x0101010101010101L;
                                highNE += 0x0101010100000001L;
                                highE += 0x0101000000000001L;
                                highSE += 0x0100000000000001L;
                                highS += 0x0100000000000101L;
                                
                                
                                
                                highC += 0x0101000000010101L;
                            }
                            case 4 -> {
                                highN += 0x0101010101010101L;
                                
                                highE += 0x0101000000000001L;
                                highSE += 0x0100000000000001L;
                                highS += 0x0100000000000101L;
                                highSW += 0x0000000000000101L;
                                highW += 0x0000000000010101L;
                                
                                highC += 0x0101000000010101L;
                            }
                            case 5 -> {
                                highN += 0x0101010101010101L;
                                
                                
                                
                                highS += 0x0100000000000101L;
                                highSW += 0x0000000000000101L;
                                highW += 0x0000000000010101L;
                                highNW += 0x0000000101010101L;
                                highC += 0x0101000000010101L;
                            }
                            case 6 -> {
                                highN += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                highW += 0x0000000000010101L;
                                highNW += 0x0000000101010101L;
                                
                            }
                            case 7 -> {
                                highN += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                highNW += 0x0000000101010101L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                highNE += 0x0101000000000001L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                highN += 0x0101000000010101L;
                                highNE += 0x0101000000000001L;
                                highE += 0x0100000000000001L;
                                
                                
                                
                                
                                
                                highC += 0x0100000000000101L;
                            }
                            case 4 -> {
                                highN += 0x0101000000010101L;
                                highNE += 0x0101000000000001L;
                                highE += 0x0100000000000001L;
                                
                                
                                
                                highW += 0x0000000000000101L;
                                highNW += 0x0000000000010101L;
                                highC += 0x0100000000000101L;
                            }
                            case 5 -> {
                                highN += 0x0101000000010101L;
                                
                                
                                
                                
                                
                                highW += 0x0000000000000101L;
                                highNW += 0x0000000000010101L;
                                highC += 0x0100000000000101L;
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                highNW += 0x0000000000010101L;
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                highN += 0x0100000000000101L;
                                highNE += 0x0100000000000001L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                highN += 0x0100000000000101L;
                                highNE += 0x0100000000000001L;
                                
                                
                                
                                
                                
                                highNW += 0x0000000000000101L;
                                
                            }
                            case 5 -> {
                                highN += 0x0100000000000101L;
                                
                                
                                
                                
                                
                                
                                highNW += 0x0000000000000101L;
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case 1 -> {
                switch (dy) {
                    case -3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                highSE += 0x0000010101000000L;
                                highS += 0x0000000101000000L;
                                highSW += 0x0000000001000000L;
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                highSE += 0x0000010101000000L;
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0000000001000000L;
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                highSE += 0x0000010101000000L;
                                highS += 0x0000000101000000L;
                                highSW += 0x0000000001000000L;
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                highE += 0x0000010101000000L;
                                highSE += 0x0001010101010000L;
                                highS += 0x0000000101010000L;
                                
                                highW += 0x0000000001000000L;
                                
                                highC += 0x0000000101000000L;
                            }
                            case 1 -> {
                                
                                
                                highE += 0x0000010101000000L;
                                highSE += 0x0001010101010000L;
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                highS += 0x0000000101010000L;
                                highSW += 0x0000000001010000L;
                                highW += 0x0000000001000000L;
                                
                                
                            }
                            case 7 -> {
                                
                                
                                highE += 0x0000010101000000L;
                                highSE += 0x0001010101010000L;
                                highS += 0x0000000101010000L;
                                highSW += 0x0000000001010000L;
                                highW += 0x0000000001000000L;
                                
                                highC += 0x0000000101000000L;
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                highN += 0x0000000101000000L;
                                highNE += 0x0000010101000000L;
                                highE += 0x0001010101010000L;
                                highSE += 0x0101010101010101L;
                                
                                
                                
                                highNW += 0x0000000001000000L;
                                highC += 0x0000000101010000L;
                            }
                            case 1 -> {
                                
                                highNE += 0x0000010101000000L;
                                highE += 0x0001010101010000L;
                                highSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                highSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                highSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                highSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                highSE += 0x0101010101010101L;
                                highS += 0x0000000101010101L;
                                highSW += 0x0000000001010100L;
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                highSE += 0x0101010101010101L;
                                highS += 0x0000000101010101L;
                                highSW += 0x0000000001010100L;
                                highW += 0x0000000001010000L;
                                highNW += 0x0000000001000000L;
                                highC += 0x0000000101010000L;
                            }
                            case 7 -> {
                                highN += 0x0000000101000000L;
                                highNE += 0x0000010101000000L;
                                highE += 0x0001010101010000L;
                                highSE += 0x0101010101010101L;
                                highS += 0x0000000101010101L;
                                highSW += 0x0000000001010100L;
                                highW += 0x0000000001010000L;
                                highNW += 0x0000000001000000L;
                                highC += 0x0000000101010000L;
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                highN += 0x0000000101010000L;
                                highNE += 0x0001010101010000L;
                                highE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                highNE += 0x0001010101010000L;
                                highE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                highE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                highE += 0x0101010101010101L;
                                highSE += 0x0101000000010101L;
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                highE += 0x0101010101010101L;
                                highSE += 0x0101000000010101L;
                                highS += 0x0000000000010101L;
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                highE += 0x0101010101010101L;
                                highSE += 0x0101000000010101L;
                                highS += 0x0000000000010101L;
                                highSW += 0x0000000000010100L;
                                highW += 0x0000000001010100L;
                                
                                highC += 0x0000000101010101L;
                            }
                            case 6 -> {
                                highN += 0x0000000101010000L;
                                
                                highE += 0x0101010101010101L;
                                
                                highS += 0x0000000000010101L;
                                highSW += 0x0000000000010100L;
                                highW += 0x0000000001010100L;
                                highNW += 0x0000000001010000L;
                                highC += 0x0000000101010101L;
                            }
                            case 7 -> {
                                highN += 0x0000000101010000L;
                                highNE += 0x0001010101010000L;
                                highE += 0x0101010101010101L;
                                
                                
                                
                                highW += 0x0000000001010100L;
                                highNW += 0x0000000001010000L;
                                highC += 0x0000000101010101L;
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                highNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                highNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                highNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                highNE += 0x0101010101010101L;
                                highE += 0x0101000000010101L;
                                highSE += 0x0100000000000101L;
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                highNE += 0x0101010101010101L;
                                highE += 0x0101000000010101L;
                                highSE += 0x0100000000000101L;
                                highS += 0x0000000000000101L;
                                highSW += 0x0000000000000100L;
                                
                                
                                highC += 0x0000000000010101L;
                            }
                            case 5 -> {
                                highN += 0x0000000101010101L;
                                highNE += 0x0101010101010101L;
                                highE += 0x0101000000010101L;
                                highSE += 0x0100000000000101L;
                                highS += 0x0000000000000101L;
                                highSW += 0x0000000000000100L;
                                highW += 0x0000000000010100L;
                                highNW += 0x0000000001010100L;
                                highC += 0x0000000000010101L;
                            }
                            case 6 -> {
                                highN += 0x0000000101010101L;
                                highNE += 0x0101010101010101L;
                                
                                
                                
                                highSW += 0x0000000000000100L;
                                highW += 0x0000000000010100L;
                                highNW += 0x0000000001010100L;
                                highC += 0x0000000000010101L;
                            }
                            case 7 -> {
                                highN += 0x0000000101010101L;
                                highNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                highNW += 0x0000000001010100L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                highNE += 0x0101000000010101L;
                                highE += 0x0100000000000101L;
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                highN += 0x0000000000010101L;
                                highNE += 0x0101000000010101L;
                                highE += 0x0100000000000101L;
                                
                                
                                
                                highW += 0x0000000000000100L;
                                
                                highC += 0x0000000000000101L;
                            }
                            case 5 -> {
                                highN += 0x0000000000010101L;
                                highNE += 0x0101000000010101L;
                                highE += 0x0100000000000101L;
                                
                                
                                
                                highW += 0x0000000000000100L;
                                highNW += 0x0000000000010100L;
                                highC += 0x0000000000000101L;
                            }
                            case 6 -> {
                                highN += 0x0000000000010101L;
                                
                                
                                
                                
                                
                                highW += 0x0000000000000100L;
                                highNW += 0x0000000000010100L;
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                highNE += 0x0100000000000101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                highN += 0x0000000000000101L;
                                highNE += 0x0100000000000101L;
                                
                                
                                
                                
                                
                                highNW += 0x0000000000000100L;
                                
                            }
                            case 5 -> {
                                highN += 0x0000000000000101L;
                                highNE += 0x0100000000000101L;
                                
                                
                                
                                
                                
                                highNW += 0x0000000000000100L;
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                highNW += 0x0000000000000100L;
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case 2 -> {
                switch (dy) {
                    case -3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                highSE += 0x0000000101000000L;
                                highS += 0x0000000001000000L;
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                highS += 0x0000000001000000L;
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                highSE += 0x0000000101000000L;
                                highS += 0x0000000001000000L;
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                highE += 0x0000000101000000L;
                                highSE += 0x0000000101010000L;
                                
                                
                                
                                
                                highC += 0x0000000001000000L;
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                highSE += 0x0000000101010000L;
                                highS += 0x0000000001010000L;
                                
                                
                                
                                highC += 0x0000000001000000L;
                            }
                            case 7 -> {
                                
                                
                                highE += 0x0000000101000000L;
                                highSE += 0x0000000101010000L;
                                highS += 0x0000000001010000L;
                                
                                
                                
                                highC += 0x0000000001000000L;
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                highN += 0x0000000001000000L;
                                highNE += 0x0000000101000000L;
                                highE += 0x0000000101010000L;
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                highSE += 0x0000000101010101L;
                                highS += 0x0000000001010100L;
                                
                                
                                
                                
                            }
                            case 6 -> {
                                highN += 0x0000000001000000L;
                                
                                highE += 0x0000000101010000L;
                                highSE += 0x0000000101010101L;
                                highS += 0x0000000001010100L;
                                
                                
                                
                                highC += 0x0000000001010000L;
                            }
                            case 7 -> {
                                highN += 0x0000000001000000L;
                                highNE += 0x0000000101000000L;
                                highE += 0x0000000101010000L;
                                highSE += 0x0000000101010101L;
                                highS += 0x0000000001010100L;
                                
                                
                                
                                highC += 0x0000000001010000L;
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                highNE += 0x0000000101010000L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                highSE += 0x0000000000010101L;
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                highE += 0x0000000101010101L;
                                highSE += 0x0000000000010101L;
                                highS += 0x0000000000010100L;
                                
                                
                                
                                highC += 0x0000000001010100L;
                            }
                            case 6 -> {
                                highN += 0x0000000001010000L;
                                highNE += 0x0000000101010000L;
                                highE += 0x0000000101010101L;
                                highSE += 0x0000000000010101L;
                                highS += 0x0000000000010100L;
                                
                                
                                
                                highC += 0x0000000001010100L;
                            }
                            case 7 -> {
                                highN += 0x0000000001010000L;
                                highNE += 0x0000000101010000L;
                                highE += 0x0000000101010101L;
                                
                                
                                
                                
                                
                                highC += 0x0000000001010100L;
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                highE += 0x0000000000010101L;
                                highSE += 0x0000000000000101L;
                                highS += 0x0000000000000100L;
                                
                                
                                
                                
                            }
                            case 5 -> {
                                highN += 0x0000000001010100L;
                                highNE += 0x0000000101010101L;
                                highE += 0x0000000000010101L;
                                highSE += 0x0000000000000101L;
                                highS += 0x0000000000000100L;
                                
                                
                                
                                highC += 0x0000000000010100L;
                            }
                            case 6 -> {
                                highN += 0x0000000001010100L;
                                highNE += 0x0000000101010101L;
                                highE += 0x0000000000010101L;
                                
                                highS += 0x0000000000000100L;
                                
                                
                                
                                highC += 0x0000000000010100L;
                            }
                            case 7 -> {
                                highN += 0x0000000001010100L;
                                highNE += 0x0000000101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                highNE += 0x0000000000010101L;
                                highE += 0x0000000000000101L;
                                
                                
                                
                                
                                
                                highC += 0x0000000000000100L;
                            }
                            case 5 -> {
                                highN += 0x0000000000010100L;
                                highNE += 0x0000000000010101L;
                                highE += 0x0000000000000101L;
                                
                                
                                
                                
                                
                                highC += 0x0000000000000100L;
                            }
                            case 6 -> {
                                highN += 0x0000000000010100L;
                                highNE += 0x0000000000010101L;
                                
                                
                                
                                
                                
                                
                                highC += 0x0000000000000100L;
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                highN += 0x0000000000000100L;
                                highNE += 0x0000000000000101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                highN += 0x0000000000000100L;
                                highNE += 0x0000000000000101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                highN += 0x0000000000000100L;
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case 3 -> {
                switch (dy) {
                    case -3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                highSE += 0x0000000001000000L;
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                highSE += 0x0000000001000000L;
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                highSE += 0x0000000001000000L;
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                highE += 0x0000000001000000L;
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                highE += 0x0000000001000000L;
                                highSE += 0x0000000001010000L;
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                highE += 0x0000000001000000L;
                                highSE += 0x0000000001010000L;
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                highNE += 0x0000000001000000L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                highSE += 0x0000000001010100L;
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                highNE += 0x0000000001000000L;
                                highE += 0x0000000001010000L;
                                highSE += 0x0000000001010100L;
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                highNE += 0x0000000001000000L;
                                highE += 0x0000000001010000L;
                                highSE += 0x0000000001010100L;
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                highE += 0x0000000001010100L;
                                highSE += 0x0000000000010100L;
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                highNE += 0x0000000001010000L;
                                highE += 0x0000000001010100L;
                                highSE += 0x0000000000010100L;
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                highNE += 0x0000000001010000L;
                                highE += 0x0000000001010100L;
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                highSE += 0x0000000000000100L;
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                highNE += 0x0000000001010100L;
                                highE += 0x0000000000010100L;
                                highSE += 0x0000000000000100L;
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                highNE += 0x0000000001010100L;
                                highE += 0x0000000000010100L;
                                highSE += 0x0000000000000100L;
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                highNE += 0x0000000001010100L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                highE += 0x0000000000000100L;
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                highNE += 0x0000000000010100L;
                                highE += 0x0000000000000100L;
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                highNE += 0x0000000000010100L;
                                highE += 0x0000000000000100L;
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 3 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                highNE += 0x0000000000000100L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                highNE += 0x0000000000000100L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                highNE += 0x0000000000000100L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            default -> { }
        }
    }

    public static boolean hasThreat(Direction moveDir, Direction facing) {
        switch (moveDir.ordinal()) {
            case 0 -> {
                switch (facing.ordinal()) {
                    case 0 -> { return threatNN != 0L; }
                    case 1 -> { return threatNNE != 0L; }
                    case 2 -> { return threatNE != 0L; }
                    case 3 -> { return threatNSE != 0L; }
                    case 4 -> { return threatNS != 0L; }
                    case 5 -> { return threatNSW != 0L; }
                    case 6 -> { return threatNW != 0L; }
                    case 7 -> { return threatNNW != 0L; }
                    default -> { return false; }
                }
            }
            case 1 -> {
                switch (facing.ordinal()) {
                    case 0 -> { return threatNEN != 0L; }
                    case 1 -> { return threatNENE != 0L; }
                    case 2 -> { return threatNEE != 0L; }
                    case 3 -> { return threatNESE != 0L; }
                    case 4 -> { return threatNES != 0L; }
                    case 5 -> { return threatNESW != 0L; }
                    case 6 -> { return threatNEW != 0L; }
                    case 7 -> { return threatNENW != 0L; }
                    default -> { return false; }
                }
            }
            case 2 -> {
                switch (facing.ordinal()) {
                    case 0 -> { return threatEN != 0L; }
                    case 1 -> { return threatENE != 0L; }
                    case 2 -> { return threatEE != 0L; }
                    case 3 -> { return threatESE != 0L; }
                    case 4 -> { return threatES != 0L; }
                    case 5 -> { return threatESW != 0L; }
                    case 6 -> { return threatEW != 0L; }
                    case 7 -> { return threatENW != 0L; }
                    default -> { return false; }
                }
            }
            case 3 -> {
                switch (facing.ordinal()) {
                    case 0 -> { return threatSEN != 0L; }
                    case 1 -> { return threatSENE != 0L; }
                    case 2 -> { return threatSEE != 0L; }
                    case 3 -> { return threatSESE != 0L; }
                    case 4 -> { return threatSES != 0L; }
                    case 5 -> { return threatSESW != 0L; }
                    case 6 -> { return threatSEW != 0L; }
                    case 7 -> { return threatSENW != 0L; }
                    default -> { return false; }
                }
            }
            case 4 -> {
                switch (facing.ordinal()) {
                    case 0 -> { return threatSN != 0L; }
                    case 1 -> { return threatSNE != 0L; }
                    case 2 -> { return threatSE != 0L; }
                    case 3 -> { return threatSSE != 0L; }
                    case 4 -> { return threatSS != 0L; }
                    case 5 -> { return threatSSW != 0L; }
                    case 6 -> { return threatSW != 0L; }
                    case 7 -> { return threatSNW != 0L; }
                    default -> { return false; }
                }
            }
            case 5 -> {
                switch (facing.ordinal()) {
                    case 0 -> { return threatSWN != 0L; }
                    case 1 -> { return threatSWNE != 0L; }
                    case 2 -> { return threatSWE != 0L; }
                    case 3 -> { return threatSWSE != 0L; }
                    case 4 -> { return threatSWS != 0L; }
                    case 5 -> { return threatSWSW != 0L; }
                    case 6 -> { return threatSWW != 0L; }
                    case 7 -> { return threatSWNW != 0L; }
                    default -> { return false; }
                }
            }
            case 6 -> {
                switch (facing.ordinal()) {
                    case 0 -> { return threatWN != 0L; }
                    case 1 -> { return threatWNE != 0L; }
                    case 2 -> { return threatWE != 0L; }
                    case 3 -> { return threatWSE != 0L; }
                    case 4 -> { return threatWS != 0L; }
                    case 5 -> { return threatWSW != 0L; }
                    case 6 -> { return threatWW != 0L; }
                    case 7 -> { return threatWNW != 0L; }
                    default -> { return false; }
                }
            }
            case 7 -> {
                switch (facing.ordinal()) {
                    case 0 -> { return threatNWN != 0L; }
                    case 1 -> { return threatNWNE != 0L; }
                    case 2 -> { return threatNWE != 0L; }
                    case 3 -> { return threatNWSE != 0L; }
                    case 4 -> { return threatNWS != 0L; }
                    case 5 -> { return threatNWSW != 0L; }
                    case 6 -> { return threatNWW != 0L; }
                    case 7 -> { return threatNWNW != 0L; }
                    default -> { return false; }
                }
            }
            case 8 -> {
                switch (facing.ordinal()) {
                    case 0 -> { return threatCN != 0L; }
                    case 1 -> { return threatCNE != 0L; }
                    case 2 -> { return threatCE != 0L; }
                    case 3 -> { return threatCSE != 0L; }
                    case 4 -> { return threatCS != 0L; }
                    case 5 -> { return threatCSW != 0L; }
                    case 6 -> { return threatCW != 0L; }
                    case 7 -> { return threatCNW != 0L; }
                    default -> { return false; }
                }
            }
            default -> { return false; }
        }
    }

    public static boolean hasThreat(Direction moveDir, Direction facing, RobotInfo target) {
        long base;
        switch (moveDir.ordinal()) {
            case 0 -> {
                switch (facing.ordinal()) {
                    case 0 -> { base = threatNN; }
                    case 1 -> { base = threatNNE; }
                    case 2 -> { base = threatNE; }
                    case 3 -> { base = threatNSE; }
                    case 4 -> { base = threatNS; }
                    case 5 -> { base = threatNSW; }
                    case 6 -> { base = threatNW; }
                    case 7 -> { base = threatNNW; }
                    default -> { base = 0L; }
                }
            }
            case 1 -> {
                switch (facing.ordinal()) {
                    case 0 -> { base = threatNEN; }
                    case 1 -> { base = threatNENE; }
                    case 2 -> { base = threatNEE; }
                    case 3 -> { base = threatNESE; }
                    case 4 -> { base = threatNES; }
                    case 5 -> { base = threatNESW; }
                    case 6 -> { base = threatNEW; }
                    case 7 -> { base = threatNENW; }
                    default -> { base = 0L; }
                }
            }
            case 2 -> {
                switch (facing.ordinal()) {
                    case 0 -> { base = threatEN; }
                    case 1 -> { base = threatENE; }
                    case 2 -> { base = threatEE; }
                    case 3 -> { base = threatESE; }
                    case 4 -> { base = threatES; }
                    case 5 -> { base = threatESW; }
                    case 6 -> { base = threatEW; }
                    case 7 -> { base = threatENW; }
                    default -> { base = 0L; }
                }
            }
            case 3 -> {
                switch (facing.ordinal()) {
                    case 0 -> { base = threatSEN; }
                    case 1 -> { base = threatSENE; }
                    case 2 -> { base = threatSEE; }
                    case 3 -> { base = threatSESE; }
                    case 4 -> { base = threatSES; }
                    case 5 -> { base = threatSESW; }
                    case 6 -> { base = threatSEW; }
                    case 7 -> { base = threatSENW; }
                    default -> { base = 0L; }
                }
            }
            case 4 -> {
                switch (facing.ordinal()) {
                    case 0 -> { base = threatSN; }
                    case 1 -> { base = threatSNE; }
                    case 2 -> { base = threatSE; }
                    case 3 -> { base = threatSSE; }
                    case 4 -> { base = threatSS; }
                    case 5 -> { base = threatSSW; }
                    case 6 -> { base = threatSW; }
                    case 7 -> { base = threatSNW; }
                    default -> { base = 0L; }
                }
            }
            case 5 -> {
                switch (facing.ordinal()) {
                    case 0 -> { base = threatSWN; }
                    case 1 -> { base = threatSWNE; }
                    case 2 -> { base = threatSWE; }
                    case 3 -> { base = threatSWSE; }
                    case 4 -> { base = threatSWS; }
                    case 5 -> { base = threatSWSW; }
                    case 6 -> { base = threatSWW; }
                    case 7 -> { base = threatSWNW; }
                    default -> { base = 0L; }
                }
            }
            case 6 -> {
                switch (facing.ordinal()) {
                    case 0 -> { base = threatWN; }
                    case 1 -> { base = threatWNE; }
                    case 2 -> { base = threatWE; }
                    case 3 -> { base = threatWSE; }
                    case 4 -> { base = threatWS; }
                    case 5 -> { base = threatWSW; }
                    case 6 -> { base = threatWW; }
                    case 7 -> { base = threatWNW; }
                    default -> { base = 0L; }
                }
            }
            case 7 -> {
                switch (facing.ordinal()) {
                    case 0 -> { base = threatNWN; }
                    case 1 -> { base = threatNWNE; }
                    case 2 -> { base = threatNWE; }
                    case 3 -> { base = threatNWSE; }
                    case 4 -> { base = threatNWS; }
                    case 5 -> { base = threatNWSW; }
                    case 6 -> { base = threatNWW; }
                    case 7 -> { base = threatNWNW; }
                    default -> { base = 0L; }
                }
            }
            case 8 -> {
                switch (facing.ordinal()) {
                    case 0 -> { base = threatCN; }
                    case 1 -> { base = threatCNE; }
                    case 2 -> { base = threatCE; }
                    case 3 -> { base = threatCSE; }
                    case 4 -> { base = threatCS; }
                    case 5 -> { base = threatCSW; }
                    case 6 -> { base = threatCW; }
                    case 7 -> { base = threatCNW; }
                    default -> { base = 0L; }
                }
            }
            default -> { base = 0L; }
        }
        if (target == null) return base != 0L;
        if (target.getTeam() == rc.getTeam()) return base != 0L;
        if (target.getType() != UnitType.BABY_RAT) return base != 0L;
        int enemyDx = target.location.x - myLocX;
        int enemyDy = target.location.y - myLocY;
        if (enemyDx < -3 || enemyDx > 3 || enemyDy < -3 || enemyDy > 3) return base != 0L;
        long sub = enemyMaskForMove(enemyDx, enemyDy, target.direction, moveDir);
        long threat = getMoveThreatMask(moveDir);
        if (target.getHealth() > rc.getHealth()) {
            base -= (sub & threat);
        } else {
            base -= (sub & getHiddenMask(facing) & threat);
        }
        return base != 0L;
    }

    public static boolean hasThreat(Direction moveDir, Direction facing, RobotInfo target, int damage) {
        long base;
        switch (moveDir.ordinal()) {
            case 0 -> {
                switch (facing.ordinal()) {
                    case 0 -> { base = threatNN; }
                    case 1 -> { base = threatNNE; }
                    case 2 -> { base = threatNE; }
                    case 3 -> { base = threatNSE; }
                    case 4 -> { base = threatNS; }
                    case 5 -> { base = threatNSW; }
                    case 6 -> { base = threatNW; }
                    case 7 -> { base = threatNNW; }
                    default -> { base = 0L; }
                }
            }
            case 1 -> {
                switch (facing.ordinal()) {
                    case 0 -> { base = threatNEN; }
                    case 1 -> { base = threatNENE; }
                    case 2 -> { base = threatNEE; }
                    case 3 -> { base = threatNESE; }
                    case 4 -> { base = threatNES; }
                    case 5 -> { base = threatNESW; }
                    case 6 -> { base = threatNEW; }
                    case 7 -> { base = threatNENW; }
                    default -> { base = 0L; }
                }
            }
            case 2 -> {
                switch (facing.ordinal()) {
                    case 0 -> { base = threatEN; }
                    case 1 -> { base = threatENE; }
                    case 2 -> { base = threatEE; }
                    case 3 -> { base = threatESE; }
                    case 4 -> { base = threatES; }
                    case 5 -> { base = threatESW; }
                    case 6 -> { base = threatEW; }
                    case 7 -> { base = threatENW; }
                    default -> { base = 0L; }
                }
            }
            case 3 -> {
                switch (facing.ordinal()) {
                    case 0 -> { base = threatSEN; }
                    case 1 -> { base = threatSENE; }
                    case 2 -> { base = threatSEE; }
                    case 3 -> { base = threatSESE; }
                    case 4 -> { base = threatSES; }
                    case 5 -> { base = threatSESW; }
                    case 6 -> { base = threatSEW; }
                    case 7 -> { base = threatSENW; }
                    default -> { base = 0L; }
                }
            }
            case 4 -> {
                switch (facing.ordinal()) {
                    case 0 -> { base = threatSN; }
                    case 1 -> { base = threatSNE; }
                    case 2 -> { base = threatSE; }
                    case 3 -> { base = threatSSE; }
                    case 4 -> { base = threatSS; }
                    case 5 -> { base = threatSSW; }
                    case 6 -> { base = threatSW; }
                    case 7 -> { base = threatSNW; }
                    default -> { base = 0L; }
                }
            }
            case 5 -> {
                switch (facing.ordinal()) {
                    case 0 -> { base = threatSWN; }
                    case 1 -> { base = threatSWNE; }
                    case 2 -> { base = threatSWE; }
                    case 3 -> { base = threatSWSE; }
                    case 4 -> { base = threatSWS; }
                    case 5 -> { base = threatSWSW; }
                    case 6 -> { base = threatSWW; }
                    case 7 -> { base = threatSWNW; }
                    default -> { base = 0L; }
                }
            }
            case 6 -> {
                switch (facing.ordinal()) {
                    case 0 -> { base = threatWN; }
                    case 1 -> { base = threatWNE; }
                    case 2 -> { base = threatWE; }
                    case 3 -> { base = threatWSE; }
                    case 4 -> { base = threatWS; }
                    case 5 -> { base = threatWSW; }
                    case 6 -> { base = threatWW; }
                    case 7 -> { base = threatWNW; }
                    default -> { base = 0L; }
                }
            }
            case 7 -> {
                switch (facing.ordinal()) {
                    case 0 -> { base = threatNWN; }
                    case 1 -> { base = threatNWNE; }
                    case 2 -> { base = threatNWE; }
                    case 3 -> { base = threatNWSE; }
                    case 4 -> { base = threatNWS; }
                    case 5 -> { base = threatNWSW; }
                    case 6 -> { base = threatNWW; }
                    case 7 -> { base = threatNWNW; }
                    default -> { base = 0L; }
                }
            }
            case 8 -> {
                switch (facing.ordinal()) {
                    case 0 -> { base = threatCN; }
                    case 1 -> { base = threatCNE; }
                    case 2 -> { base = threatCE; }
                    case 3 -> { base = threatCSE; }
                    case 4 -> { base = threatCS; }
                    case 5 -> { base = threatCSW; }
                    case 6 -> { base = threatCW; }
                    case 7 -> { base = threatCNW; }
                    default -> { base = 0L; }
                }
            }
            default -> { base = 0L; }
        }
        if (target == null) return base != 0L;
        if (target.getTeam() == rc.getTeam()) return base != 0L;
        if (target.getType() != UnitType.BABY_RAT) return base != 0L;
        int enemyDx = target.location.x - myLocX;
        int enemyDy = target.location.y - myLocY;
        if (enemyDx < -3 || enemyDx > 3 || enemyDy < -3 || enemyDy > 3) return base != 0L;
        boolean beforeHigh = target.getHealth() > rc.getHealth();
        boolean afterHigh = (target.getHealth() - damage) > rc.getHealth();
        if (target.getHealth() <= damage || afterHigh != beforeHigh) {
            long sub = enemyMaskForMove(enemyDx, enemyDy, target.direction, moveDir);
            long threat = getMoveThreatMask(moveDir);
            if (target.getHealth() <= damage) {
                if (beforeHigh) {
                    base -= (sub & threat);
                } else {
                    base -= (sub & getHiddenMask(facing) & threat);
                }
            } else {
                base -= (sub & threat);
                base += (sub & getHiddenMask(facing) & threat);
            }
        }
        return base != 0L;
    }

    private static long getMoveThreatMask(Direction moveDir) {
        return switch (moveDir.ordinal()) {
            case 0 -> moveThreatN;
            case 1 -> moveThreatNE;
            case 2 -> moveThreatE;
            case 3 -> moveThreatSE;
            case 4 -> moveThreatS;
            case 5 -> moveThreatSW;
            case 6 -> moveThreatW;
            case 7 -> moveThreatNW;
            case 8 -> moveThreatC;
            default -> 0L;
        };
    }

    private static long getHiddenMask(Direction facing) {
        return switch (facing.ordinal()) {
            case 0 -> hiddenN;
            case 1 -> hiddenNE;
            case 2 -> hiddenE;
            case 3 -> hiddenSE;
            case 4 -> hiddenS;
            case 5 -> hiddenSW;
            case 6 -> hiddenW;
            case 7 -> hiddenNW;
            default -> 0L;
        };
    }

    private static long enemyMaskForMove(int dx, int dy, Direction facing, Direction moveDir) {
        int faceIdx = facing.ordinal();
        int moveIdx = moveDir.ordinal();
        switch (dx) {
            case -3 -> {
                switch (dy) {
                    case -3 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000010000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000010000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000010000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000010000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0001010000000000L; }
                                    case 6 -> { return 0x0000010000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0001010000000000L; }
                                    case 6 -> { return 0x0000010000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000010000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0101010000000000L; }
                                    case 6 -> { return 0x0001010000000000L; }
                                    case 7 -> { return 0x0000010000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0101010000000000L; }
                                    case 6 -> { return 0x0001010000000000L; }
                                    case 7 -> { return 0x0000010000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0101010000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 0 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0101010000000000L; }
                                    case 7 -> { return 0x0001010000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0101000000000000L; }
                                    case 6 -> { return 0x0101010000000000L; }
                                    case 7 -> { return 0x0001010000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0101000000000000L; }
                                    case 6 -> { return 0x0101010000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0101010000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0100000000000000L; }
                                    case 6 -> { return 0x0101000000000000L; }
                                    case 7 -> { return 0x0101010000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0100000000000000L; }
                                    case 6 -> { return 0x0101000000000000L; }
                                    case 7 -> { return 0x0101010000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0100000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0100000000000000L; }
                                    case 7 -> { return 0x0101000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0100000000000000L; }
                                    case 7 -> { return 0x0101000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0100000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 3 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0100000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0100000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0100000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    default -> { return 0L; }
                }
            }
            case -2 -> {
                switch (dy) {
                    case -3 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000010000000000L; }
                                    case 5 -> { return 0x0000010100000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000010000000000L; }
                                    case 5 -> { return 0x0000010100000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000010000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0001010100000000L; }
                                    case 6 -> { return 0x0000010100000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000010000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0001010000000000L; }
                                    case 5 -> { return 0x0001010100000000L; }
                                    case 6 -> { return 0x0000010100000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000010000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0001010000000000L; }
                                    case 5 -> { return 0x0001010100000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000010000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000010000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0001010100000000L; }
                                    case 7 -> { return 0x0000010100000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000010000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101010000000000L; }
                                    case 5 -> { return 0x0101010100000001L; }
                                    case 6 -> { return 0x0001010100000000L; }
                                    case 7 -> { return 0x0000010100000000L; }
                                    case 8 -> { return 0x0001010000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000010000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101010000000000L; }
                                    case 5 -> { return 0x0101010100000001L; }
                                    case 6 -> { return 0x0001010100000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0001010000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101010000000000L; }
                                    case 5 -> { return 0x0101010100000001L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 0 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0001010100000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0001010000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0101010100000001L; }
                                    case 7 -> { return 0x0001010100000000L; }
                                    case 8 -> { return 0x0101010000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0001010000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101000000000000L; }
                                    case 5 -> { return 0x0101000000000001L; }
                                    case 6 -> { return 0x0101010100000001L; }
                                    case 7 -> { return 0x0001010100000000L; }
                                    case 8 -> { return 0x0101010000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101000000000000L; }
                                    case 5 -> { return 0x0101000000000001L; }
                                    case 6 -> { return 0x0101010100000001L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0101010000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0101000000000001L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0101010100000001L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0100000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0101000000000001L; }
                                    case 7 -> { return 0x0101010100000001L; }
                                    case 8 -> { return 0x0101000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0100000000000000L; }
                                    case 5 -> { return 0x0100000000000001L; }
                                    case 6 -> { return 0x0101000000000001L; }
                                    case 7 -> { return 0x0101010100000001L; }
                                    case 8 -> { return 0x0101000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0100000000000000L; }
                                    case 5 -> { return 0x0100000000000001L; }
                                    case 6 -> { return 0x0101000000000001L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0101000000000001L; }
                                    case 8 -> { return 0x0100000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0100000000000001L; }
                                    case 7 -> { return 0x0101000000000001L; }
                                    case 8 -> { return 0x0100000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0100000000000001L; }
                                    case 7 -> { return 0x0101000000000001L; }
                                    case 8 -> { return 0x0100000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 3 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0100000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0100000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0100000000000001L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0100000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0100000000000001L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    default -> { return 0L; }
                }
            }
            case -1 -> {
                switch (dy) {
                    case -3 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000010000000000L; }
                                    case 4 -> { return 0x0000010100000000L; }
                                    case 5 -> { return 0x0000010101000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000010000000000L; }
                                    case 4 -> { return 0x0000010100000000L; }
                                    case 5 -> { return 0x0000010101000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000010000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000010101000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000010000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0001010100000000L; }
                                    case 5 -> { return 0x0001010101010000L; }
                                    case 6 -> { return 0x0000010101000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000010100000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000010000000000L; }
                                    case 3 -> { return 0x0001010000000000L; }
                                    case 4 -> { return 0x0001010100000000L; }
                                    case 5 -> { return 0x0001010101010000L; }
                                    case 6 -> { return 0x0000010101000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000010100000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000010000000000L; }
                                    case 3 -> { return 0x0001010000000000L; }
                                    case 4 -> { return 0x0001010100000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0001010101010000L; }
                                    case 6 -> { return 0x0000010101000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000010100000000L; }
                                    case 1 -> { return 0x0000010000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0101010101010101L; }
                                    case 6 -> { return 0x0001010101010000L; }
                                    case 7 -> { return 0x0000010101000000L; }
                                    case 8 -> { return 0x0001010100000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000010100000000L; }
                                    case 1 -> { return 0x0000010000000000L; }
                                    case 2 -> { return 0x0001010000000000L; }
                                    case 3 -> { return 0x0101010000000000L; }
                                    case 4 -> { return 0x0101010100000001L; }
                                    case 5 -> { return 0x0101010101010101L; }
                                    case 6 -> { return 0x0001010101010000L; }
                                    case 7 -> { return 0x0000010101000000L; }
                                    case 8 -> { return 0x0001010100000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000010000000000L; }
                                    case 2 -> { return 0x0001010000000000L; }
                                    case 3 -> { return 0x0101010000000000L; }
                                    case 4 -> { return 0x0101010100000001L; }
                                    case 5 -> { return 0x0101010101010101L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0001010100000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0101010000000000L; }
                                    case 4 -> { return 0x0101010100000001L; }
                                    case 5 -> { return 0x0101010101010101L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0101010101010101L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0101010101010101L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0101010101010101L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0101010101010101L; }
                                    case 6 -> { return 0x0001010101010000L; }
                                    case 7 -> { return 0x0000010101000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 0 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0001010100000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0101010101010101L; }
                                    case 7 -> { return 0x0001010101010000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0001010100000000L; }
                                    case 1 -> { return 0x0001010000000000L; }
                                    case 2 -> { return 0x0101010000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0101010101010101L; }
                                    case 7 -> { return 0x0001010101010000L; }
                                    case 8 -> { return 0x0101010100000001L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0001010100000000L; }
                                    case 1 -> { return 0x0001010000000000L; }
                                    case 2 -> { return 0x0101010000000000L; }
                                    case 3 -> { return 0x0101000000000000L; }
                                    case 4 -> { return 0x0101000000000001L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0101010101010101L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0101010100000001L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0101010000000000L; }
                                    case 3 -> { return 0x0101000000000000L; }
                                    case 4 -> { return 0x0101000000000001L; }
                                    case 5 -> { return 0x0101000000010101L; }
                                    case 6 -> { return 0x0101010101010101L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0101010100000001L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101000000000001L; }
                                    case 5 -> { return 0x0101000000010101L; }
                                    case 6 -> { return 0x0101010101010101L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0101000000010101L; }
                                    case 6 -> { return 0x0101010101010101L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0101010101010101L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0101010101010101L; }
                                    case 7 -> { return 0x0001010101010000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0101010101010101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010100000001L; }
                                    case 1 -> { return 0x0101010000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0101010101010101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010100000001L; }
                                    case 1 -> { return 0x0101010000000000L; }
                                    case 2 -> { return 0x0101000000000000L; }
                                    case 3 -> { return 0x0100000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0101010101010101L; }
                                    case 8 -> { return 0x0101000000000001L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010100000001L; }
                                    case 1 -> { return 0x0101010000000000L; }
                                    case 2 -> { return 0x0101000000000000L; }
                                    case 3 -> { return 0x0100000000000000L; }
                                    case 4 -> { return 0x0100000000000001L; }
                                    case 5 -> { return 0x0100000000000101L; }
                                    case 6 -> { return 0x0101000000010101L; }
                                    case 7 -> { return 0x0101010101010101L; }
                                    case 8 -> { return 0x0101000000000001L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0100000000000000L; }
                                    case 4 -> { return 0x0100000000000001L; }
                                    case 5 -> { return 0x0100000000000101L; }
                                    case 6 -> { return 0x0101000000010101L; }
                                    case 7 -> { return 0x0101010101010101L; }
                                    case 8 -> { return 0x0101000000000001L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0100000000000101L; }
                                    case 6 -> { return 0x0101000000010101L; }
                                    case 7 -> { return 0x0101010101010101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0101010101010101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0101010101010101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101000000000001L; }
                                    case 1 -> { return 0x0101000000000000L; }
                                    case 2 -> { return 0x0100000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101000000000001L; }
                                    case 1 -> { return 0x0101000000000000L; }
                                    case 2 -> { return 0x0100000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0100000000000101L; }
                                    case 7 -> { return 0x0101000000010101L; }
                                    case 8 -> { return 0x0100000000000001L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101000000000001L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0100000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0100000000000101L; }
                                    case 7 -> { return 0x0101000000010101L; }
                                    case 8 -> { return 0x0100000000000001L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0100000000000101L; }
                                    case 7 -> { return 0x0101000000010101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 3 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0100000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0100000000000001L; }
                                    case 1 -> { return 0x0100000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0100000000000101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0100000000000001L; }
                                    case 1 -> { return 0x0100000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0100000000000101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0100000000000101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    default -> { return 0L; }
                }
            }
            case 0 -> {
                switch (dy) {
                    case -3 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000010100000000L; }
                                    case 4 -> { return 0x0000010101000000L; }
                                    case 5 -> { return 0x0000000101000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000010100000000L; }
                                    case 4 -> { return 0x0000010101000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000010101000000L; }
                                    case 5 -> { return 0x0000000101000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000010100000000L; }
                                    case 3 -> { return 0x0001010100000000L; }
                                    case 4 -> { return 0x0001010101010000L; }
                                    case 5 -> { return 0x0000000101010000L; }
                                    case 6 -> { return 0x0000000101000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000010101000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000010100000000L; }
                                    case 3 -> { return 0x0001010100000000L; }
                                    case 4 -> { return 0x0001010101010000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000010101000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0001010100000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000101010000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0001010101010000L; }
                                    case 5 -> { return 0x0000000101010000L; }
                                    case 6 -> { return 0x0000000101000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000010101000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000010101000000L; }
                                    case 1 -> { return 0x0000010100000000L; }
                                    case 2 -> { return 0x0001010100000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101010101010101L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000101010000L; }
                                    case 7 -> { return 0x0000000101000000L; }
                                    case 8 -> { return 0x0001010101010000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000010101000000L; }
                                    case 1 -> { return 0x0000010100000000L; }
                                    case 2 -> { return 0x0001010100000000L; }
                                    case 3 -> { return 0x0101010100000001L; }
                                    case 4 -> { return 0x0101010101010101L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0001010101010000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0001010100000000L; }
                                    case 3 -> { return 0x0101010100000001L; }
                                    case 4 -> { return 0x0101010101010101L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0101010100000001L; }
                                    case 4 -> { return 0x0101010101010101L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101010101010101L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101010101010101L; }
                                    case 5 -> { return 0x0000000101010101L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101010101010101L; }
                                    case 5 -> { return 0x0000000101010101L; }
                                    case 6 -> { return 0x0000000101010000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000010101000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101010101010101L; }
                                    case 5 -> { return 0x0000000101010101L; }
                                    case 6 -> { return 0x0000000101010000L; }
                                    case 7 -> { return 0x0000000101000000L; }
                                    case 8 -> { return 0x0001010101010000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 0 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0001010101010000L; }
                                    case 1 -> { return 0x0001010100000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000101010000L; }
                                    case 8 -> { return 0x0101010101010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0001010101010000L; }
                                    case 1 -> { return 0x0001010100000000L; }
                                    case 2 -> { return 0x0101010100000001L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0101010101010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0001010100000000L; }
                                    case 2 -> { return 0x0101010100000001L; }
                                    case 3 -> { return 0x0101000000000001L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0101010101010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0101010100000001L; }
                                    case 3 -> { return 0x0101000000000001L; }
                                    case 4 -> { return 0x0101000000010101L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0101010101010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0101000000000001L; }
                                    case 4 -> { return 0x0101000000010101L; }
                                    case 5 -> { return 0x0000000000010101L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0101010101010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101000000010101L; }
                                    case 5 -> { return 0x0000000000010101L; }
                                    case 6 -> { return 0x0000000101010101L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0101010101010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000010101L; }
                                    case 6 -> { return 0x0000000101010101L; }
                                    case 7 -> { return 0x0000000101010000L; }
                                    case 8 -> { return 0x0101010101010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0001010101010000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000101010101L; }
                                    case 7 -> { return 0x0000000101010000L; }
                                    case 8 -> { return 0x0101010101010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010101010101L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010101010101L; }
                                    case 1 -> { return 0x0101010100000001L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010101010101L; }
                                    case 1 -> { return 0x0101010100000001L; }
                                    case 2 -> { return 0x0101000000000001L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010101010101L; }
                                    case 1 -> { return 0x0101010100000001L; }
                                    case 2 -> { return 0x0101000000000001L; }
                                    case 3 -> { return 0x0100000000000001L; }
                                    case 4 -> { return 0x0100000000000101L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0101000000010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010101010101L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0101000000000001L; }
                                    case 3 -> { return 0x0100000000000001L; }
                                    case 4 -> { return 0x0100000000000101L; }
                                    case 5 -> { return 0x0000000000000101L; }
                                    case 6 -> { return 0x0000000000010101L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0101000000010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010101010101L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0100000000000101L; }
                                    case 5 -> { return 0x0000000000000101L; }
                                    case 6 -> { return 0x0000000000010101L; }
                                    case 7 -> { return 0x0000000101010101L; }
                                    case 8 -> { return 0x0101000000010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010101010101L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000010101L; }
                                    case 7 -> { return 0x0000000101010101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010101010101L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000101010101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0101000000000001L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101000000010101L; }
                                    case 1 -> { return 0x0101000000000001L; }
                                    case 2 -> { return 0x0100000000000001L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0100000000000101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101000000010101L; }
                                    case 1 -> { return 0x0101000000000001L; }
                                    case 2 -> { return 0x0100000000000001L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000101L; }
                                    case 7 -> { return 0x0000000000010101L; }
                                    case 8 -> { return 0x0100000000000101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101000000010101L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000101L; }
                                    case 7 -> { return 0x0000000000010101L; }
                                    case 8 -> { return 0x0100000000000101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000010101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 3 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0100000000000101L; }
                                    case 1 -> { return 0x0100000000000001L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0100000000000101L; }
                                    case 1 -> { return 0x0100000000000001L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0100000000000101L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    default -> { return 0L; }
                }
            }
            case 1 -> {
                switch (dy) {
                    case -3 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000010101000000L; }
                                    case 4 -> { return 0x0000000101000000L; }
                                    case 5 -> { return 0x0000000001000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000010101000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000001000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000010101000000L; }
                                    case 4 -> { return 0x0000000101000000L; }
                                    case 5 -> { return 0x0000000001000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000010101000000L; }
                                    case 3 -> { return 0x0001010101010000L; }
                                    case 4 -> { return 0x0000000101010000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000001000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000101000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000010101000000L; }
                                    case 3 -> { return 0x0001010101010000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000101010000L; }
                                    case 5 -> { return 0x0000000001010000L; }
                                    case 6 -> { return 0x0000000001000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000010101000000L; }
                                    case 3 -> { return 0x0001010101010000L; }
                                    case 4 -> { return 0x0000000101010000L; }
                                    case 5 -> { return 0x0000000001010000L; }
                                    case 6 -> { return 0x0000000001000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000101000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000101000000L; }
                                    case 1 -> { return 0x0000010101000000L; }
                                    case 2 -> { return 0x0001010101010000L; }
                                    case 3 -> { return 0x0101010101010101L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000001000000L; }
                                    case 8 -> { return 0x0000000101010000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000010101000000L; }
                                    case 2 -> { return 0x0001010101010000L; }
                                    case 3 -> { return 0x0101010101010101L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0101010101010101L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0101010101010101L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0101010101010101L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0101010101010101L; }
                                    case 4 -> { return 0x0000000101010101L; }
                                    case 5 -> { return 0x0000000001010100L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0101010101010101L; }
                                    case 4 -> { return 0x0000000101010101L; }
                                    case 5 -> { return 0x0000000001010100L; }
                                    case 6 -> { return 0x0000000001010000L; }
                                    case 7 -> { return 0x0000000001000000L; }
                                    case 8 -> { return 0x0000000101010000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000101000000L; }
                                    case 1 -> { return 0x0000010101000000L; }
                                    case 2 -> { return 0x0001010101010000L; }
                                    case 3 -> { return 0x0101010101010101L; }
                                    case 4 -> { return 0x0000000101010101L; }
                                    case 5 -> { return 0x0000000001010100L; }
                                    case 6 -> { return 0x0000000001010000L; }
                                    case 7 -> { return 0x0000000001000000L; }
                                    case 8 -> { return 0x0000000101010000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 0 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000101010000L; }
                                    case 1 -> { return 0x0001010101010000L; }
                                    case 2 -> { return 0x0101010101010101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0001010101010000L; }
                                    case 2 -> { return 0x0101010101010101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0101010101010101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0101010101010101L; }
                                    case 3 -> { return 0x0101000000010101L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0101010101010101L; }
                                    case 3 -> { return 0x0101000000010101L; }
                                    case 4 -> { return 0x0000000000010101L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0101010101010101L; }
                                    case 3 -> { return 0x0101000000010101L; }
                                    case 4 -> { return 0x0000000000010101L; }
                                    case 5 -> { return 0x0000000000010100L; }
                                    case 6 -> { return 0x0000000001010100L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000101010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000101010000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0101010101010101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000010101L; }
                                    case 5 -> { return 0x0000000000010100L; }
                                    case 6 -> { return 0x0000000001010100L; }
                                    case 7 -> { return 0x0000000001010000L; }
                                    case 8 -> { return 0x0000000101010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000101010000L; }
                                    case 1 -> { return 0x0001010101010000L; }
                                    case 2 -> { return 0x0101010101010101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000001010100L; }
                                    case 7 -> { return 0x0000000001010000L; }
                                    case 8 -> { return 0x0000000101010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0101010101010101L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0101010101010101L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0101010101010101L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0101010101010101L; }
                                    case 2 -> { return 0x0101000000010101L; }
                                    case 3 -> { return 0x0100000000000101L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0101010101010101L; }
                                    case 2 -> { return 0x0101000000010101L; }
                                    case 3 -> { return 0x0100000000000101L; }
                                    case 4 -> { return 0x0000000000000101L; }
                                    case 5 -> { return 0x0000000000000100L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000101010101L; }
                                    case 1 -> { return 0x0101010101010101L; }
                                    case 2 -> { return 0x0101000000010101L; }
                                    case 3 -> { return 0x0100000000000101L; }
                                    case 4 -> { return 0x0000000000000101L; }
                                    case 5 -> { return 0x0000000000000100L; }
                                    case 6 -> { return 0x0000000000010100L; }
                                    case 7 -> { return 0x0000000001010100L; }
                                    case 8 -> { return 0x0000000000010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000101010101L; }
                                    case 1 -> { return 0x0101010101010101L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000100L; }
                                    case 6 -> { return 0x0000000000010100L; }
                                    case 7 -> { return 0x0000000001010100L; }
                                    case 8 -> { return 0x0000000000010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000101010101L; }
                                    case 1 -> { return 0x0101010101010101L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000001010100L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0101000000010101L; }
                                    case 2 -> { return 0x0100000000000101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000010101L; }
                                    case 1 -> { return 0x0101000000010101L; }
                                    case 2 -> { return 0x0100000000000101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000100L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000010101L; }
                                    case 1 -> { return 0x0101000000010101L; }
                                    case 2 -> { return 0x0100000000000101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000100L; }
                                    case 7 -> { return 0x0000000000010100L; }
                                    case 8 -> { return 0x0000000000000101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000010101L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000100L; }
                                    case 7 -> { return 0x0000000000010100L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 3 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0100000000000101L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000101L; }
                                    case 1 -> { return 0x0100000000000101L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000100L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000101L; }
                                    case 1 -> { return 0x0100000000000101L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000100L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000100L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    default -> { return 0L; }
                }
            }
            case 2 -> {
                switch (dy) {
                    case -3 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000101000000L; }
                                    case 4 -> { return 0x0000000001000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000001000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000101000000L; }
                                    case 4 -> { return 0x0000000001000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000101000000L; }
                                    case 3 -> { return 0x0000000101010000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000001000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000101010000L; }
                                    case 4 -> { return 0x0000000001010000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000001000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000101000000L; }
                                    case 3 -> { return 0x0000000101010000L; }
                                    case 4 -> { return 0x0000000001010000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000001000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000001000000L; }
                                    case 1 -> { return 0x0000000101000000L; }
                                    case 2 -> { return 0x0000000101010000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000101010101L; }
                                    case 4 -> { return 0x0000000001010100L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000001000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000101010000L; }
                                    case 3 -> { return 0x0000000101010101L; }
                                    case 4 -> { return 0x0000000001010100L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000001010000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000001000000L; }
                                    case 1 -> { return 0x0000000101000000L; }
                                    case 2 -> { return 0x0000000101010000L; }
                                    case 3 -> { return 0x0000000101010101L; }
                                    case 4 -> { return 0x0000000001010100L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000001010000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 0 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000101010000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000010101L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000101010101L; }
                                    case 3 -> { return 0x0000000000010101L; }
                                    case 4 -> { return 0x0000000000010100L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000001010100L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000001010000L; }
                                    case 1 -> { return 0x0000000101010000L; }
                                    case 2 -> { return 0x0000000101010101L; }
                                    case 3 -> { return 0x0000000000010101L; }
                                    case 4 -> { return 0x0000000000010100L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000001010100L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000001010000L; }
                                    case 1 -> { return 0x0000000101010000L; }
                                    case 2 -> { return 0x0000000101010101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000001010100L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000010101L; }
                                    case 3 -> { return 0x0000000000000101L; }
                                    case 4 -> { return 0x0000000000000100L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000001010100L; }
                                    case 1 -> { return 0x0000000101010101L; }
                                    case 2 -> { return 0x0000000000010101L; }
                                    case 3 -> { return 0x0000000000000101L; }
                                    case 4 -> { return 0x0000000000000100L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000010100L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000001010100L; }
                                    case 1 -> { return 0x0000000101010101L; }
                                    case 2 -> { return 0x0000000000010101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000100L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000010100L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000001010100L; }
                                    case 1 -> { return 0x0000000101010101L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000010101L; }
                                    case 2 -> { return 0x0000000000000101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000100L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000010100L; }
                                    case 1 -> { return 0x0000000000010101L; }
                                    case 2 -> { return 0x0000000000000101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000100L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000010100L; }
                                    case 1 -> { return 0x0000000000010101L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000100L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 3 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000100L; }
                                    case 1 -> { return 0x0000000000000101L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000100L; }
                                    case 1 -> { return 0x0000000000000101L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000100L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    default -> { return 0L; }
                }
            }
            case 3 -> {
                switch (dy) {
                    case -3 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000001000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000001000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000001000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000001000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000001000000L; }
                                    case 3 -> { return 0x0000000001010000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000001000000L; }
                                    case 3 -> { return 0x0000000001010000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000001000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000001010100L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000001000000L; }
                                    case 2 -> { return 0x0000000001010000L; }
                                    case 3 -> { return 0x0000000001010100L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000001000000L; }
                                    case 2 -> { return 0x0000000001010000L; }
                                    case 3 -> { return 0x0000000001010100L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 0 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000001010100L; }
                                    case 3 -> { return 0x0000000000010100L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000001010000L; }
                                    case 2 -> { return 0x0000000001010100L; }
                                    case 3 -> { return 0x0000000000010100L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000001010000L; }
                                    case 2 -> { return 0x0000000001010100L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000100L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000001010100L; }
                                    case 2 -> { return 0x0000000000010100L; }
                                    case 3 -> { return 0x0000000000000100L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000001010100L; }
                                    case 2 -> { return 0x0000000000010100L; }
                                    case 3 -> { return 0x0000000000000100L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000001010100L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000100L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000010100L; }
                                    case 2 -> { return 0x0000000000000100L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000010100L; }
                                    case 2 -> { return 0x0000000000000100L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 3 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000100L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000100L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000100L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    default -> { return 0L; }
                }
            }
            default -> { return 0L; }
        }
    }

    private static boolean isThreatAt(int dx, int dy) {
        switch (dx) {
            case -2 -> {
                switch (dy) {
                    case -2 -> { return t_m2_m2; }
                    case -1 -> { return t_m2_m1; }
                    case 0 -> { return t_m2_0; }
                    case 1 -> { return t_m2_1; }
                    case 2 -> { return t_m2_2; }
                    default -> { return false; }
                }
            }
            case -1 -> {
                switch (dy) {
                    case -2 -> { return t_m1_m2; }
                    case -1 -> { return t_m1_m1; }
                    case 0 -> { return t_m1_0; }
                    case 1 -> { return t_m1_1; }
                    case 2 -> { return t_m1_2; }
                    default -> { return false; }
                }
            }
            case 0 -> {
                switch (dy) {
                    case -2 -> { return t_0_m2; }
                    case -1 -> { return t_0_m1; }
                    case 0 -> { return t_0_0; }
                    case 1 -> { return t_0_1; }
                    case 2 -> { return t_0_2; }
                    default -> { return false; }
                }
            }
            case 1 -> {
                switch (dy) {
                    case -2 -> { return t_1_m2; }
                    case -1 -> { return t_1_m1; }
                    case 0 -> { return t_1_0; }
                    case 1 -> { return t_1_1; }
                    case 2 -> { return t_1_2; }
                    default -> { return false; }
                }
            }
            case 2 -> {
                switch (dy) {
                    case -2 -> { return t_2_m2; }
                    case -1 -> { return t_2_m1; }
                    case 0 -> { return t_2_0; }
                    case 1 -> { return t_2_1; }
                    case 2 -> { return t_2_2; }
                    default -> { return false; }
                }
            }
            default -> { return false; }
        }
    }

    // vision is fully precomputed into enemy masks, no runtime cone checks needed
}