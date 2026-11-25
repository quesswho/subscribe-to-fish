package awubot.robot.rat;

import battlecode.common.*;
import awubot.CellTracker;
import awubot.Pathfinder;
import awubot.RobotTracker;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;

import static awubot.Game.*;

/*
    Some rats hunt the enemy king. On a symmetric map running a symmetric bot, the enemy king is near the mirror
    image of our first king, under whichever symmetries are still possible. A hunter that reaches a candidate and
    sees no king there rules that symmetry out for itself and tries the next one.
*/
public class HuntStrategy extends Strategy {
    static int START_ROUND = 150;
    static int SHARE = 4; // one rat in SHARE hunts

    // candidates still possible for this rat: rotational, x-mirror, y-mirror
    private boolean rot = true, xm = true, ym = true;

    static boolean isHunter() {
        return rc.getID() % SHARE == 0;
    }

    private MapLocation mirror(MapLocation k, int which) {
        return switch (which) {
            case 0 -> new MapLocation(mapWidth - 1 - k.x, mapHeight - 1 - k.y);
            case 1 -> new MapLocation(mapWidth - 1 - k.x, k.y);
            default -> new MapLocation(k.x, mapHeight - 1 - k.y);
        };
    }

    private boolean possible(int which) {
        return switch (which) {
            case 0 -> rot && CellTracker.rsym == 1;
            case 1 -> xm && CellTracker.hsym == 1;
            default -> ym && CellTracker.vsym == 1;
        };
    }

    private void ruleOut(int which) {
        if (which == 0) rot = false;
        else if (which == 1) xm = false;
        else ym = false;
        if (!rot && !xm && !ym) rot = xm = ym = true;
    }

    private MapLocation target() {
        MapLocation king = GlobalArray.getRatKing(0);
        if (king == null) king = GlobalArray.nearestRatKing;
        if (king == null) return null;
        MapLocation best = null;
        for (int which = 0; which < 3; which++) {
            if (!possible(which)) continue;
            MapLocation t = mirror(king, which);
            if (rc.canSenseLocation(t) && myLoc.distanceSquaredTo(t) <= 8 && !seesEnemyKing()) {
                ruleOut(which);
                continue;
            }
            if (best == null || myLoc.distanceSquaredTo(t) < myLoc.distanceSquaredTo(best)) best = t;
        }
        return best;
    }

    private static boolean seesEnemyKing() {
        for (int i = 0; i < RobotTracker.numEnemies; i++) {
            if (RobotTracker.sortedEnemies[i].getType() == UnitType.RAT_KING) return true;
        }
        return false;
    }

    // A hunter that barely moves for a while is stuck (walls, or a corridor our own carriers need): it mines instead.
    static final int CHECK = 40, MIN_MOVE_SQ = 16, REST = 300;
    private MapLocation anchor;
    private int anchorRound, restUntil = -1;

    @Override
    public boolean runTick() throws GameActionException {
        if (time < START_ROUND || !isHunter() || time < restUntil) return false;
        MapLocation t = target();
        if (t == null) return false;
        if (anchor == null || time - anchorRound > CHECK + 10) {
            anchor = myLoc;
            anchorRound = time;
        } else if (time - anchorRound >= CHECK) {
            boolean stuck = myLoc.distanceSquaredTo(anchor) <= MIN_MOVE_SQ;
            anchor = myLoc;
            anchorRound = time;
            if (stuck) {
                restUntil = time + REST;
                anchor = null;
                return false;
            }
        }
        BabyRat.autoMove(Pathfinder.getMove(t));
        return true;
    }
}
