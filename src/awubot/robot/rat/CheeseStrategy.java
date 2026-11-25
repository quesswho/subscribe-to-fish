package awubot.robot.rat;

import battlecode.common.*;
import awubot.Pathfinder;
import awubot.robot.Strategy;
import static awubot.Game.*;
import static awubot.util.Util.*;

// TODO: go after a different target if given the opportunity
// this strategy doesn't pick up cheese (that's handled in BabyRat
// since we want to pick up in any circumstances since it doesn't cost action cd)
public class CheeseStrategy extends Strategy {
    public int lastSeenCheese;
    public MapLocation cheeseTarget;
    TraverseStrategy traverseStrategy;

    @Override
    public boolean runTick() throws GameActionException {
        refreshCheeseTarget();
        if(cheeseTarget == null) return false;
        lastSeenCheese = rc.getRoundNum();
        // TODO: handle the edge case when we're already on the cheese
        if(!traverseStrategy.runTick()) {
            traverseStrategy = null;
            cheeseTarget = null;
        }
        return true;
    }

    /** Whether loc is in Pathfinder's local reachable mask (11x11 around us, as computeReachable left it). */
    private static boolean reachable(MapLocation loc) {
        int xi = loc.x - myLocX + 5, yi = loc.y - myLocY + 5;
        if (xi < 0 || xi > 10 || yi < 0 || yi > 10) return false;
        int idx = yi * 11 + xi;
        return idx < 61 ? (Pathfinder.reach0 & (1L << idx)) != 0 : (Pathfinder.reach1 & (1L << (idx - 61))) != 0;
    }

    private void refreshCheeseTarget() throws GameActionException {
        if (cheeseTarget != null
            && rc.canSenseLocation(cheeseTarget)
            && (rc.senseMapInfo(cheeseTarget).getCheeseAmount() == 0)) {
            cheeseTarget = null;
        }
        if(cheeseTarget != null) return;
        int bestAmount = 0;
        boolean reachComputed = false;
        for (var info : rc.senseNearbyMapInfos()) {
            var amt = info.getCheeseAmount();
            if(amt > 0 && (cheeseTarget == null || amt > bestAmount)) {
                // Vision sees across walls and dirt: chase only piles we can walk to from here.
                if (!reachComputed) {
                    Pathfinder.computeReachable();
                    reachComputed = true;
                }
                if (!reachable(info.getMapLocation())) continue;
                cheeseTarget = info.getMapLocation();
                bestAmount = amt;
            }
        }
        if(cheeseTarget != null && (traverseStrategy == null ||
            !traverseStrategy.target.equals(cheeseTarget))) {
            traverseStrategy = new TraverseStrategy(cheeseTarget, 1);
        }
    }
}
