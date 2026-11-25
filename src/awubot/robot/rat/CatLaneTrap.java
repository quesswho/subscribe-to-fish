package awubot.robot.rat;

import battlecode.common.*;
import static awubot.Game.*;
import static awubot.util.Util.*;

/**
 * While cooperating, cat damage is half the points. A rat near a cat puts a cat trap on its lane: a tile next to the
 * rat that the cat's 2x2 body touches (trap radius sqrt 2) now or after one or two steps along its facing.
 */
public class CatLaneTrap {
    private static final int MAX_DIST_SQ = 18;
    private static final int KING_DIST_SQ = 25;
    private static final int MIN_CHEESE = 200;
    private static final int LOOKAHEAD = 2;
    // A cat this low is being killed (a cat race): it is trapped from any round.
    private static final int RACE_CAT_HP = UnitType.CAT.health - 1500;
    private static final int HEALTHY_MARGIN = TrapType.CAT_TRAP.damage + 100;
    // A healthier cat only from round 150, and only while the trap leaves it HEALTHY_MARGIN above the clock's LOW (a cat
    // down 1500 hp starts the cat clock's end kings). Most games are backstabbed by round 150 (80%); a cooperation that
    // lasts longer is more often a cat race or a long cooperation, where cat damage is points (half of them; 30% after
    // a backstab). Before that the cheese and turns go to the economy (healthy cats trapped from round 1: -8 vs v3).
    private static final int HEALTHY_FROM = 150;

    public static boolean tryPlace(RobotInfo cat) throws GameActionException {
        boolean race = cat.health <= RACE_CAT_HP;
        if ((!race && (time < HEALTHY_FROM || cat.health < RACE_CAT_HP + HEALTHY_MARGIN)) || myLoc.distanceSquaredTo(cat.location) > MAX_DIST_SQ) return false;
        if (rc.getAllCheese() < MIN_CHEESE || rc.getNumberCatTraps() >= TrapType.CAT_TRAP.maxCount) return false;
        int bx = cat.location.x, by = cat.location.y, sx = cat.direction.dx, sy = cat.direction.dy;
        MapLocation best = null;
        int bestStep = LOOKAHEAD + 1;
        for (Direction d : directions) {
            MapLocation t = myLoc.add(d);
            for (int k = 0; k < bestStep; k++) {
                int x0 = bx + k * sx, y0 = by + k * sy;
                if (t.x >= x0 - 1 && t.x <= x0 + 2 && t.y >= y0 - 1 && t.y <= y0 + 2) {
                    // k == 0 counts as one step: the trap fires on the cat's next move while it still touches
                    int step = Math.max(k, 1);
                    if (step < bestStep && rc.canPlaceCatTrap(t)) {
                        bestStep = step;
                        best = t;
                    }
                    break;
                }
            }
        }
        if (best == null) return false;
        rc.placeCatTrap(best);
        return true;
    }

    /** The same for a king: any tile within its build reach (d2 8 from its centre) outside its own 3x3. */
    public static boolean tryPlaceKing(RobotInfo cat) throws GameActionException {
        boolean race = cat.health <= RACE_CAT_HP;
        if ((!race && (time < HEALTHY_FROM || cat.health < RACE_CAT_HP + HEALTHY_MARGIN)) || myLoc.distanceSquaredTo(cat.location) > KING_DIST_SQ) return false;
        if (rc.getAllCheese() < MIN_CHEESE || rc.getNumberCatTraps() >= TrapType.CAT_TRAP.maxCount) return false;
        int bx = cat.location.x, by = cat.location.y, sx = cat.direction.dx, sy = cat.direction.dy;
        MapLocation best = null;
        int bestStep = LOOKAHEAD + 1;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx * dx + dy * dy > 8 || (dx * dx <= 1 && dy * dy <= 1)) continue;
                int tx = myLoc.x + dx, ty = myLoc.y + dy;
                for (int k = 0; k < bestStep; k++) {
                    int x0 = bx + k * sx, y0 = by + k * sy;
                    if (tx >= x0 - 1 && tx <= x0 + 2 && ty >= y0 - 1 && ty <= y0 + 2) {
                        int step = Math.max(k, 1);
                        MapLocation t = new MapLocation(tx, ty);
                        if (step < bestStep && rc.canPlaceCatTrap(t)) {
                            bestStep = step;
                            best = t;
                        }
                        break;
                    }
                }
            }
        }
        if (best == null) return false;
        rc.placeCatTrap(best);
        return true;
    }
}
