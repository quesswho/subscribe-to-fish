package awubot.robot.rat.combat;

import battlecode.common.Direction;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;

import awubot.RobotTracker;

import static awubot.Game.*;
import static awubot.util.Util.*;

/**
 * Enemy rat traps are invisible, but both opponents lay one beside themselves, toward a healthy rat of ours they see,
 * almost whenever their action is ready. A healthy rat with an enemy rat at Chebyshev 2 that sees it starts its turn
 * inside such a trap's trigger area about 46% of the time, and a move then springs it 89% of the time (50 hp and three
 * turns pinned). So while healthy, moves ending within Chebyshev 1 of a tile that trap could stand on (adjacent to both
 * that enemy and us) are left out of TryFight (plain positioning; moves that bite, grab or trap are unchanged).
 * Standing still never springs a trap; moving away is still allowed. Measured, it springs about as many traps as
 * before (most moves in that state bite, grab or trap); what it gains is the healthy rat holding distance 2 instead
 * of stepping in.
 */
public final class TrapAvoid {
    private TrapAvoid() {
    }

    static final int MIN_HP = 100;

    static int mask, maskTime = -1;

    /** Whether TryFight should leave out a move in this direction this turn. */
    public static boolean blocked(Direction d) {
        if (maskTime != time) {
            maskTime = time;
            mask = 0;
            compute();
        }
        return (mask & (1 << d.ordinal())) != 0;
    }

    static void compute() {
        if (rc.getHealth() < MIN_HP || rc.getCarrying() != null) return;
        int n = RobotTracker.numEnemies;
        for (int i = 0; i < n; i++) {
            RobotInfo e = RobotTracker.sortedEnemies[i];
            int dx = e.location.x - myLocX, dy = e.location.y - myLocY;
            if (dx * dx + dy * dy > 8) break;  // sorted by distance: nothing at Chebyshev 2 beyond this
            if (e.getType() != UnitType.BABY_RAT) continue;
            if (Math.max(Math.abs(dx), Math.abs(dy)) != 2) continue;
            if (!canSense(-dx, -dy, e.direction)) continue;
            // trap tiles adjacent to both: t in [max(-1, d-1), min(1, d+1)] per axis
            int x0 = Math.max(-1, dx - 1), x1 = Math.min(1, dx + 1);
            int y0 = Math.max(-1, dy - 1), y1 = Math.min(1, dy + 1);
            for (int tx = x0; tx <= x1; tx++) {
                for (int ty = y0; ty <= y1; ty++) {
                    if (tx == 0 && ty == 0) continue;
                    drop(tx, ty);
                }
            }
        }
    }

    /** Drops the moves ending within Chebyshev 1 of the tile (tx, ty) relative to us (its trigger area). */
    private static void drop(int tx, int ty) {
        if (near(0, 1, tx, ty)) mask |= 1 << Direction.NORTH.ordinal();
        if (near(1, 1, tx, ty)) mask |= 1 << Direction.NORTHEAST.ordinal();
        if (near(1, 0, tx, ty)) mask |= 1 << Direction.EAST.ordinal();
        if (near(1, -1, tx, ty)) mask |= 1 << Direction.SOUTHEAST.ordinal();
        if (near(0, -1, tx, ty)) mask |= 1 << Direction.SOUTH.ordinal();
        if (near(-1, -1, tx, ty)) mask |= 1 << Direction.SOUTHWEST.ordinal();
        if (near(-1, 0, tx, ty)) mask |= 1 << Direction.WEST.ordinal();
        if (near(-1, 1, tx, ty)) mask |= 1 << Direction.NORTHWEST.ordinal();
    }

    private static boolean near(int x, int y, int tx, int ty) {
        return Math.abs(x - tx) <= 1 && Math.abs(y - ty) <= 1;
    }
}
