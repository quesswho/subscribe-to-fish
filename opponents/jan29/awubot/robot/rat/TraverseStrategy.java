package awubot.robot.rat;

import battlecode.common.*;
import awubot.Pathfinder;
import awubot.robot.Strategy;

import static awubot.Game.*;// indicate

// *just* in case we optimise this in the future
public class TraverseStrategy extends Strategy {
    public MapLocation target;
    int distSquared;
    private int timer;
    private MapLocation lastLoc, secondLastLoc;
    private boolean stuck = false;

    public TraverseStrategy(MapLocation target, int distSquared) {
        this.target = target;
        this.distSquared = distSquared;
        timer = 25;
        lastLoc = myLoc;
        secondLastLoc = myLoc;
    }

    @Override
    public boolean runTick() throws GameActionException {
        if(rc.getLocation().distanceSquaredTo(target) <= distSquared) return false;
        // indicate("TRAVELLING TO " + target);
        // indicate("before move: " + Clock.getBytecodeNum());
        BabyRat.autoMove(Pathfinder.getMove(target));
        // indicate("after move: " + Clock.getBytecodeNum());
        timer--;
        if(timer == 0) {
            if(myLoc.distanceSquaredTo(lastLoc) <= 8) {
                stuck = true;
            }
            if(myLoc.distanceSquaredTo(secondLastLoc) <= 18) {
                stuck = true;
            }
            secondLastLoc = lastLoc;
            lastLoc = myLoc;
            timer = 25;
        }
        if(Pathfinder.givenUp() || rc.getRawCheese() < 20 && stuck && time % 25 == 0) {
            // indicate("GIVEN UP ON " + target);
            Pathfinder.reset();
            return false;
        }
        // rc.setIndicatorLine(myLoc, target, 255, 0, 0);
        return true;
    }
}
