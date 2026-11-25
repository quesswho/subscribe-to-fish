package awubot.robot;

import battlecode.common.GameActionException;

/*

as a general policy, are responsible for checking whether they are runnable
for example, a previous strategy may have made them unable to paint but still able to move
if the current strategy doesnt require painting, it should see that it can move but not paint and act accordingly

*/

public abstract class Strategy {
    public boolean tryStrategy(Strategy strat) throws GameActionException {
        if (strat == null) return false;
        return strat.runTick();
    }
    // returns true if the strategy did something (so the caller should exit)
    public abstract boolean runTick() throws GameActionException;
}
