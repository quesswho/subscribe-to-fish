package awubot.robot.rat;

import battlecode.common.MapLocation;
import awubot.comms.GlobalArray;
import awubot.robot.rat.goHome.AntColony;
import static awubot.Game.*;

// Each carried cheese adds 1% to a baby rat's cooldowns, so a load of c walks home at 1 / (1 + c/100) of full speed.
// In a cooperation that lasted to QUIET_ROUND the halves never met and the game is a pure income race: a rat then
// takes only as much as it can carry home within TRIP_TURNS (whole piles taken near a mine left carriers crawling with
// 300-2700 for hundreds of rounds, jamming the one-wide corridors behind them, and none of it landed by round 2000).
public final class Delivery {
    // A step home takes SLACK/10 x (1 + c/100) turns (detours, strafes, blocked moves).
    private static final int SLACK = 13;
    // Loads up to FLOOR (twice the farthest go-home load) are never cut.
    private static final int TRIP_TURNS = 80, FLOOR = 200, QUIET_ROUND = 500;

    /** Steps to the nearest king's transfer range: the trail's count, kept within 1x-3x the Chebyshev distance. */
    private static int stepsHome() {
        MapLocation k = GlobalArray.nearestRatKing;
        if (k == null) return 0;
        int cheb = Math.max(Math.abs(k.x - myLocX), Math.abs(k.y - myLocY)) - 2;
        if (cheb <= 0) return 0;
        int trail = AntColony.trailSteps();
        return trail < cheb ? cheb : Math.min(trail, 3 * cheb);
    }

    /** The largest load this rat should carry: unlimited, except within TRIP_TURNS home in a long cooperation. */
    public static int maxLoad() {
        if (time < QUIET_ROUND || !rc.isCooperation()) return Integer.MAX_VALUE;
        int steps = stepsHome();
        if (steps == 0) return Integer.MAX_VALUE;
        return Math.max(FLOOR, load(TRIP_TURNS, steps));
    }

    /** The load that walks the steps home in the given turns. */
    private static int load(int turns, int steps) {
        return turns * 1000 / (SLACK * steps) - 100;
    }
}
