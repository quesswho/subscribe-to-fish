package awubot.robot.king;

import battlecode.common.*;
import awubot.RobotTracker;

import static awubot.Game.*;
import static awubot.util.Util.chebyshevDistance;

/*
    In a backstab, the king's action goes to a rat trap on its own ring (the 16 tiles at Chebyshev 2, the farthest
    it can build) when enemy rats close in. Anything that wants to bite the king has to step onto or next to that
    ring, and traps are hidden from the enemy: 50 damage and a stun for 30 cheese, from a bank that can carry it.
    Only in a backstab: a trap sprung in cooperation would make us the backstabber. (Ring cat traps were tried too:
    no gain, ablation kt-ringcat.)
*/
public final class KingTraps {
    private KingTraps() {
    }

    private static final int[] RING_DX = {0, 1, 2, 2, 2, 2, 2, 1, 0, -1, -2, -2, -2, -2, -2, -1};
    private static final int[] RING_DY = {2, 2, 2, 1, 0, -1, -2, -2, -2, -2, -2, -1, 0, 1, 2, 2};

    private static final int RAT_NEAR_SQ = 18, KING_NEAR_SQ = 25;
    private static final int RAT_BANK = 200, RAT_BANK_HURT = 100, HURT_HP = 300;
    private static final int MAX_SCORED = 6;

    /** A rat trap on the ring tile the nearby enemies must cross. True when one was placed. */
    public static boolean ratTrap() throws GameActionException {
        if (!rc.isActionReady() || RobotTracker.numEnemies == 0 || rc.isCooperation()) return false;
        if (rc.getNumberRatTraps() >= TrapType.RAT_TRAP.maxCount) return false;
        if (rc.getGlobalCheese() < (rc.getHealth() <= HURT_HP ? RAT_BANK_HURT : RAT_BANK)) return false;
        int n = Math.min(RobotTracker.numEnemies, MAX_SCORED);
        boolean threat = false;
        for (int i = 0; i < n; i++) {
            RobotInfo e = RobotTracker.sortedEnemies[i];
            if (e == null) continue;
            int d = e.location.distanceSquaredTo(myLoc);
            // A bite that kills beats a trap that might.
            if (d <= 8 && e.health <= GameConstants.RAT_BITE_DAMAGE) return false;
            if (e.type == UnitType.BABY_RAT ? d <= RAT_NEAR_SQ : e.type == UnitType.RAT_KING && d <= KING_NEAR_SQ) {
                threat = true;
            }
        }
        if (!threat) return false;
        long covered = coveredByOurRatTraps();
        MapLocation best = null;
        int bestScore = 0;
        for (int r = 0; r < 16; r++) {
            int dx = RING_DX[r], dy = RING_DY[r];
            if ((covered >>> ((dx + 2) * 5 + dy + 2) & 1L) != 0) continue;
            MapLocation loc = myLoc.translate(dx, dy);
            if (!rc.canPlaceRatTrap(loc)) continue;
            int score = 0;
            for (int i = 0; i < n; i++) {
                RobotInfo e = RobotTracker.sortedEnemies[i];
                if (e == null) continue;
                int c = chebyshevDistance(loc, e.location);
                if (c > 2) continue;
                int s = c <= 1 ? 4 : 2;
                if (e.type == UnitType.RAT_KING) s += 2;
                else if (e.direction != null
                        && (loc.x - e.location.x) * e.direction.dx + (loc.y - e.location.y) * e.direction.dy > 0) s += 1;
                score += s;
            }
            if (score > bestScore) {
                bestScore = score;
                best = loc;
            }
        }
        if (best == null) return false;
        rc.placeRatTrap(best);
        return true;
    }

    /** Ring tiles (bit (dx+2)*5 + dy+2) already inside the trigger reach of one of our rat traps. */
    private static long coveredByOurRatTraps() throws GameActionException {
        if (rc.getNumberRatTraps() == 0) return 0L;
        long mask = 0L;
        for (MapInfo info : rc.senseNearbyMapInfos(myLoc, 18)) {
            if (info.getTrap() != TrapType.RAT_TRAP) continue;
            int tx = info.getMapLocation().x - myLocX, ty = info.getMapLocation().y - myLocY;
            for (int x = Math.max(-2, tx - 1); x <= Math.min(2, tx + 1); x++) {
                for (int y = Math.max(-2, ty - 1); y <= Math.min(2, ty + 1); y++) {
                    mask |= 1L << ((x + 2) * 5 + y + 2);
                }
            }
        }
        return mask;
    }
}
