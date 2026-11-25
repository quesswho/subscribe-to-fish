package awubot.robot.rat;

import battlecode.common.*;
import awubot.CellTracker;
import awubot.Game;
import awubot.robot.Strategy;
import awubot.comms.GlobalArray;
import awubot.comms.MapManager;
import awubot.util.Rng;
import awubot.util.string.FastIndexedIntSet;
import awubot.robot.rat.goHome.GoHomeStrategy;
import static awubot.Game.*;
import static awubot.util.Util.*;

public class MinerStrategy extends Strategy {
    private static final int CRITICAL_HP = 20;
    private static final int EARLY_EXPLORE_TURNS = 20;
    // 16 compass-ish directions (gcd=1) for cheap ray sampling.
    private static final int[] EXPLORE_DIR_X = { 1, 1, 0, -1, -1, -1, 0, 1, 2, 1, -1, -2, -2, -1, 1, 2 };
    private static final int[] EXPLORE_DIR_Y = { 0, 1, 1,  1,  0, -1, -1, -1, 1, 2,  2,  1, -1, -2, -2, -1 };

    private FormationStrategy formationStrategy;
    private GoHomeStrategy goHomeStrategy;
    private CheeseStrategy cheeseStrategy;
    private TraverseStrategy goSomewhereStrategy; // either go to a mine or explore; TODO: make the mine strategy smarter

    private FastIndexedIntSet bannedMines;

    public MinerStrategy() {
        formationStrategy = new FormationStrategy();
        goHomeStrategy = new GoHomeStrategy();
        cheeseStrategy = new CheeseStrategy();
        bannedMines = new FastIndexedIntSet();
    }

    @Override
    public boolean runTick() throws GameActionException {
        if (BabyRat.transferSwitch) {
            bannedMines = new FastIndexedIntSet();
            BabyRat.transferSwitch = false;
        }
        if(rc.getRawCheese() == 0) {
            goHomeStrategy = null;
        } else if(goHomeStrategy == null && shouldGoHome()) {
            goHomeStrategy = new GoHomeStrategy();
            cheeseStrategy.cheeseTarget = null;
        }

        if(tryStrategy(formationStrategy)) return true;

        if(tryStrategy(goHomeStrategy)) return true;

        if(tryStrategy(cheeseStrategy)) return true;

        if (rc.getRawCheese() > 50) {
            goHomeStrategy = new GoHomeStrategy();
            goHomeStrategy.runTick();
            return true;
        }

        while(goSomewhereStrategy == null || !goSomewhereStrategy.runTick()) {
            MapLocation target;

            int knownMines = Game.knownCheeseMines.set.length();
            if(knownMines == 0
               || knownMines <= 2 && time <= mapWidth * mapHeight / 8) {
                target = pickExplorationTarget();
            } else {
                // var p = Math.exp(-12.0 * time / (mapWidth * mapHeight));
                var p = 0.2;
                if(Rng.mcg() < p * (1l << 31)) {
                    target = pickExplorationTarget();
                } else {
                    target = pickSeenMine();
                }
            }
            goSomewhereStrategy = new TraverseStrategy(target, 2);
        }

        return true;
    }

    private MapLocation pickSeenMine() {
        boolean nearKing = GlobalArray.nearestRatKing.distanceSquaredTo(myLoc) <= 49;
        var mines = new StringBuilder();
        outerLoop:
        for(int i = Game.knownCheeseMines.set.length() - 1; i >= 0; i--) {
            var iloc = Game.knownCheeseMines.at(i);
            var loc = decode2x2(iloc);
            if(canSense(myLoc, loc, myDir)) continue;
            if(bannedMines.contains(iloc)) continue;
            for(int j = 0; j < 5; j++) {
                var kingLoc = GlobalArray.getRatKing(j);
                if(kingLoc != null && kingLoc.distanceSquaredTo(loc) <= 2) {
                    continue outerLoop;
                }
            }
            if(nearKing) {
                // mines.append((char)iloc);
                mines.repeat(iloc, (int) (Math.sqrt(GlobalArray.nearestRatKing.distanceSquaredTo(loc))));
            } else {
                mines.repeat(iloc, (int) (Math.sqrt(myLoc.distanceSquaredTo(loc))));
            }
        }
        if(mines.length() == 0) {
            return pickExplorationTarget();
        }
        var chosen = mines.charAt(Rng.mcg() % mines.length());
        bannedMines.add(chosen);
        return decode2x2(chosen);
    }

    private MapLocation pickExplorationTarget() {
        if(time == 2) {
            return new MapLocation(mapWidth / 2, mapHeight / 2);
        }
        if (time <= EARLY_EXPLORE_TURNS && GlobalArray.nearestRatKing != null) {
            MapLocation early = pickEarlyExplorationTarget(GlobalArray.nearestRatKing);
            if (early != null) return early;
        }
        MapLocation target = null;
        outerLoop:
        for(int i = 5; i >= 0; i--) { // tle guard
            target = MapManager.localTracker.sampleUnseenUnitCenter();
            if (target == null) {
                target = new MapLocation(Rng.mcg() % mapWidth, Rng.mcg() % mapHeight);
                break;
            }
            // not too close to a king
            for (int j = 0; j < 5; j++) {
                var kingLoc = GlobalArray.getRatKing(j);
                if (kingLoc != null && kingLoc.distanceSquaredTo(target) <= 36) {
                    continue outerLoop;
                }
            }
            return target;
        }
        return target;
    }

    private MapLocation pickEarlyExplorationTarget(MapLocation king) {
        int kx = king.x;
        int ky = king.y;
        int total = 0;
        for (int i = 0; i < EXPLORE_DIR_X.length; i++) {
            int dx = EXPLORE_DIR_X[i];
            int dy = EXPLORE_DIR_Y[i];
            int tx = (dx > 0) ? (mapWidth - 1 - kx) / dx : (dx < 0) ? kx / -dx : Integer.MAX_VALUE;
            int ty = (dy > 0) ? (mapHeight - 1 - ky) / dy : (dy < 0) ? ky / -dy : Integer.MAX_VALUE;
            int len = tx < ty ? tx : ty;
            if (len > 0) total += len;
        }
        if (total <= 0) return null;
        int r = Rng.mcg() % total;
        int acc = 0;
        for (int i = 0; i < EXPLORE_DIR_X.length; i++) {
            int dx = EXPLORE_DIR_X[i];
            int dy = EXPLORE_DIR_Y[i];
            int tx = (dx > 0) ? (mapWidth - 1 - kx) / dx : (dx < 0) ? kx / -dx : Integer.MAX_VALUE;
            int ty = (dy > 0) ? (mapHeight - 1 - ky) / dy : (dy < 0) ? ky / -dy : Integer.MAX_VALUE;
            int len = tx < ty ? tx : ty;
            if (len <= 0) continue;
            acc += len;
            if (r < acc) {
                int minDist = (len + 1) / 2;
                if (minDist < 1) minDist = 1;
                int span = len - minDist + 1;
                int dist = minDist + (span > 0 ? (Rng.mcg() % span) : 0);
                return new MapLocation(kx + dx * dist, ky + dy * dist);
            }
        }
        return null;
    }

    private boolean shouldGoHome() {
        if (shouldReturnForHealth()) return true;

        int cheese = rc.getRawCheese();
        int distSq = myLoc.distanceSquaredTo(GlobalArray.nearestRatKing);
        int thresh;
        if(distSq <= 25) thresh = 10;
        else if(distSq <= 100) thresh = 25;
        else if(distSq <= 225) thresh = 40;
        else if(distSq <= 400) thresh = 60;
        else if(distSq <= 900) thresh = 80;
        else thresh = 105;
        
        if (cheese >= thresh) return true;
        if (cheese * 2 >= thresh && cheeseStrategy.lastSeenCheese < time - 3) return true;

        int globalCheese = rc.getGlobalCheese();
        if (globalCheese < 100 && cheese >= 20) return true;
        if (globalCheese < 30 && cheese > 0) return true;

        return false;
    }

    private boolean shouldReturnForHealth() {
        int hp = rc.getHealth();
        int cheese = rc.getRawCheese();

        if (cheese == 0) return false;
        if (hp <= CRITICAL_HP) return true;

        int hpThreshold;
        if (cheese >= 150) hpThreshold = 70;
        else if (cheese >= 100) hpThreshold = 60;
        else if (cheese >= 50) hpThreshold = 50;
        else if (cheese >= 20) hpThreshold = 40;
        else return false;

        return hp <= hpThreshold;
    }
}
