package awubot.robot.king;

import battlecode.common.*;
import awubot.robot.Strategy;
import awubot.util.Rng;
import static awubot.Game.*;
import static awubot.util.Util.*;

import awubot.RobotTracker;
import awubot.comms.GlobalArray;
import awubot.util.MineCount;

public class KingSpawningStrategy extends Strategy {
    private static int numSpawned = 0;
    private static int lastPanicSpawn = -6767;

    // A map known to hold at least this many mines is rich: its income sustains a bigger army, and against an
    // opponent that grows early the opening cut costs more cheese than it saves.
    private static final int RICH_MINES = 10;

    private final KingFormationCoordinatorStrategy formation;

    private static final int FORMATION_SPAWNS = 8;
    private static MapLocation spawnSpot;
    private static int spawnedForSpot;

    public KingSpawningStrategy(KingFormationCoordinatorStrategy aFormation) {
        formation = aFormation;
    }

    public static int threshForNumRats(int x) {
        return ((x - 1) / 4) * 10 + 10;
    }

    @Override
    public boolean runTick() throws GameActionException {
        // The last or end king: rats spawned beside the king join its formation at once.
        // At most FORMATION_SPAWNS rats per spot: rats that do not reach it (a cramped spot between two kings and a
        // wall, Hike) are not counted there, and the king spawned on to the reserve while the formation stalled.
        MapLocation lastKing = formation.lastKingSpot();
        if (lastKing != null) {
            if (!lastKing.equals(spawnSpot)) {
                spawnSpot = lastKing;
                spawnedForSpot = 0;
            }
            if (spawnedForSpot < FORMATION_SPAWNS && rc.senseNearbyRobots(lastKing, 8, team).length < 8
                && formation.canSpawnForLastKing()) {
                int before = numSpawned;
                trySpawn(lastKing);
                spawnedForSpot += numSpawned - before;
            }
            return true;
        }
        int defaultThresh = 40;
        if (mapWidth * mapHeight <= 1200 && time < 400) {
            defaultThresh = 30;
        }
        // Opening: fewer, cheaper rats, keep cheese. Mine-poor small maps (fewer than 5 mines can exist): the same cut,
        // for as long as the map might be poor. The cuts do not add up.
        int mines = MineCount.atLeast(RICH_MINES);
        boolean minePoor = mapWidth * mapHeight <= 1600 && mines < 5;
        boolean rich = mines >= RICH_MINES;
        // Also to round 350 while the bank is low: the bank decides the early fights against a trap-heavy opponent.
        if ((time < 150 || minePoor || time < 350 && rc.getGlobalCheese() < 1300)) defaultThresh -= 10;
        if (rich) defaultThresh += 10;
        int thresh = Math.min(rc.getGlobalCheese() / 10 + 10, defaultThresh);
        if (rc.getGlobalCheese() >= 3000 - time * 3 / 4) {
            thresh = 1000;
        } else if (rc.getGlobalCheese() >= 2000 - time / 2 && time >= 100) {
            thresh = threshForNumRats(32);
        }

        MapLocation target = null;
        int targetNeighbours = 0;
        for (int i = 0; i < RobotTracker.numEnemies; i++) {
            RobotInfo robot = RobotTracker.sortedEnemies[i];
            MapLocation loc = robot.getLocation();
            int cnt = 0;
            for (RobotInfo info : rc.senseNearbyRobots(loc, 2, team)) {
                if (info.getTeam() == team) cnt++;
            }
            if (cnt <= 1 && (target == null || cnt > targetNeighbours)) {
                target = loc;
                targetNeighbours = cnt;
            }
        }
        if (target == null) {
            // even out spawning
            if (GlobalArray.ratCounter % GlobalArray.countRatKings() == GlobalArray.getIndexAmongAliveRatKings()) {
                target = new MapLocation(mapWidth / 2, mapHeight / 2);
            }
            
        } else {
            if (time - lastPanicSpawn > 4 && rc.getHealth() < lastHP) {
                thresh += 10;
                if (rc.getHealth() <= 100) {
                    thresh += 10;
                }
                lastPanicSpawn = time;
            }
        }

        if (target != null && rc.getCurrentRatCost() <= thresh && (rc.getGlobalCheese() >= 100 || rc.getCurrentRatCost() == 10)) {
            trySpawn(target);
        }
        return true;
    }


    void trySpawn(MapLocation target) throws GameActionException {
        MapLocation best = null;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (Math.max(Math.abs(dx), Math.abs(dy)) == 2) {
                    MapLocation loc = myLoc.translate(dx, dy);
                    if (rc.canBuildRat(loc)) {
                        if (best == null || best.distanceSquaredTo(target) > loc.distanceSquaredTo(target))
                        best = loc;
                    }
                }
            }
        }
        if (best != null) {
            rc.turn(best.directionTo(target));
            rc.buildRat(best);
            RobotTracker.addAlly(best);
            GlobalArray.registerSpawnedRat(best, Rng.mcg() & ((1 << 13) - 1));
            numSpawned++;
            RatKing.spawnedThisTurn = true;
        }
    }
}
