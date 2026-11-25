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

    private void refreshCheeseTarget() throws GameActionException {
        if (cheeseTarget != null
            && rc.canSenseLocation(cheeseTarget)
            && (rc.senseMapInfo(cheeseTarget).getCheeseAmount() == 0)) {
            cheeseTarget = null;
        }
        if(cheeseTarget != null) return;
        int bestAmount = 0;
        for (var info : rc.senseNearbyMapInfos()) {
            var amt = info.getCheeseAmount();
            if(amt > 0 && (cheeseTarget == null || amt > bestAmount)) {
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
