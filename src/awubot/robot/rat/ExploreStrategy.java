package awubot.robot.rat;

import static awubot.Game.rc;

import awubot.Pathfinder;
import awubot.robot.Strategy;
import awubot.util.Util;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class ExploreStrategy extends Strategy {
    private MapLocation target;

    @Override
    public boolean runTick() throws GameActionException {
        while (target == null || rc.canSenseLocation(target)) {
            target = Util.genPassiveTarget();
        }
        BabyRat.autoMove(Pathfinder.getMove(target));
        // rc.setIndicatorLine(rc.getLocation(), target, 0, 0, 255);
        return true;
    }
}
