package awubot.robot.king;

import awubot.CellTracker;
import static awubot.Game.*;
import awubot.robot.Strategy;
import awubot.RobotTracker;
import static awubot.util.Util.allDirections;
import static awubot.util.Util.directions;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;

public class RatKing {
    public static Strategy primaryStrategy;
    public static boolean is5x5;
    public static boolean spawnedThisTurn;
    // formed late beside two living kings (see KingFormationCoordinatorStrategy.thirdKingValve)
    public static boolean extraKing;
    // formed beside a king before round 1100, as the cat clock's end kings are (see
    // KingFormationCoordinatorStrategy.thirdKingValve)
    public static boolean clockKing;
    public static int bornRound;

    public static void init() {
        primaryStrategy = new RatKingStrategy();
        is5x5 = false;
        // (a formation beside a king can make two at once, so also the lone king's second ones; the cat clock's end
        // kings keep their own reserve)
        extraKing = time >= KingFormationCoordinatorStrategy.LONE_RICH_FROM && awubot.comms.GlobalArray.countRatKings() >= 3
            && !awubot.comms.CatClock.low();
        bornRound = time;
        clockKing = time >= KingFormationCoordinatorStrategy.RACE_MIN_ROUND && time < 1100 && besideKing();
    }

    /** Formed beside a living king (an end king's spot is 3 tiles from one; a mine king forms 20 or more tiles
     * from every king). Not whether the clock is still on: a healthy cat seen, or a backstab, while its rats
     * gathered turns the clock off before the king exists (checkerboard against v3). */
    private static boolean besideKing() {
        for (int i = 0; i < 5; i++) {
            if (i == kingId) continue;
            MapLocation k = awubot.comms.GlobalArray.getRatKing(i);
            if (k != null && k.distanceSquaredTo(myLoc) <= 50) return true;
        }
        return false;
    }

    public static void run() throws GameActionException {
        spawnedThisTurn = false;
        CellTracker.storeVisibleCellsKing();

        RobotTracker.reset();


        primaryStrategy.runTick();
    }

    public static void rawMove(Direction dir) throws GameActionException {
        _rawMove(dir);
    }

    public static void turn(Direction dir) throws GameActionException {
        _turn(dir);
    }

    public static void digDirtInDirection(Direction dir) throws GameActionException {
        if(!rc.isActionReady()) return;
        int dx = dir.dx;
        int dy = dir.dy;

        if (dx != 0) {
            for (int y = -1; y <= 1; y++) {
                var loc = new MapLocation(myLocX + dx * 2, myLocY + dy + y);
                if(rc.canRemoveDirt(loc)) {
                    rc.removeDirt(loc);
                    return;
                }
            }
        }

        if (dy != 0) {
            for (int x = -1; x <= 1; x++) {
                var loc = new MapLocation(myLocX + dx + x, myLocY + dy * 2);
                if(rc.canRemoveDirt(loc)) {
                    rc.removeDirt(loc);
                    return;
                }
            }
        }
    }



}
