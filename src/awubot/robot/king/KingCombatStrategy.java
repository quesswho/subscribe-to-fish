

package awubot.robot.king;

import battlecode.common.*;
import awubot.robot.Strategy;
import awubot.RobotTracker;
import awubot.Pathfinder;

import static awubot.robot.king.RatKing.*;
import static awubot.Game.*;
import static awubot.util.Util.*;
import awubot.CellTracker;

public class KingCombatStrategy extends Strategy {
    private static final int LANE_WEIGHT = 12;
    private static final double LANE_REACH = 12;
    private static final Direction[] LANE_DIRS = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST,
            Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};

    /**
     * A cat walks along its facing. When our 3x3 lies in the lane its 2x2 body sweeps ahead of it, each move is scored
     * by how far it takes us out of that lane; null when we are not in it.
     */
    private static int[] laneAdjust(RobotInfo cat) {
        if (cat == null) return null;
        Direction d = cat.direction;
        if (d == null || d == Direction.CENTER) return null;
        double len = d.dx != 0 && d.dy != 0 ? 1.4142135623730951 : 1.0;
        double ux = d.dx / len, uy = d.dy / len;
        double vx = myLocX - (cat.location.x + 0.5), vy = myLocY - (cat.location.y + 0.5);
        double along = vx * ux + vy * uy;
        if (along < 0 || along > LANE_REACH) return null;
        double perp = Math.abs(vx * uy - vy * ux);
        if (perp >= 2.5) return null;
        int[] adj = new int[8];
        for (int i = 0; i < 8; i++) {
            Direction m = LANE_DIRS[i];
            double nx = vx + m.dx, ny = vy + m.dy;
            double np = Math.min(2.5, Math.abs(nx * uy - ny * ux));
            adj[i] = (int) Math.round(LANE_WEIGHT * (np - perp));
        }
        return adj;
    }

    private static final int OPEN_WEIGHT = 1;
    private static final int OPEN_REACH = 3;
    private static final long[] centres = new long[2 * OPEN_REACH + 3];

    // A map corner reads as a pocket, but cats walk between waypoints and seldom pass it: a king that left its
    // corner for the open walked into the cats' path (gen_1_30, t_minus_five_minutes_j0, bed, gen_1_48).
    private static boolean inCorner() {
        int m = OPEN_REACH + 1;
        return (myLocX <= m || myLocX >= mapWidth - 1 - m) && (myLocY <= m || myLocY >= mapHeight - 1 - m);
    }

    /**
     * A walking cat blocked by the king scratches it every 3 turns and never turns away, and it moves twice as fast as
     * the king, so a king that flees into a pocket (a dead end for its 3x3, often a 2-wide passage for the cat) dies
     * there. Each move is scored by the room around where it lands: the king positions with a 3x3 free of walls and dirt within
     * OPEN_REACH of it, compared with here. Index 8 is here.
     */
    private static int[] openness() {
        int rows = centres.length;
        int base = myLocY - OPEN_REACH - 1;
        long below = CellTracker.openRow(base - 1), mid = CellTracker.openRow(base);
        for (int k = 0; k < rows; k++) {
            long above = CellTracker.openRow(base + k + 1);
            long v = below & mid & above;
            centres[k] = v & (v << 1) & (v >>> 1);
            below = mid;
            mid = above;
        }
        int[] out = new int[9];
        long window = (1L << (2 * OPEN_REACH + 1)) - 1;
        for (int i = 0; i < 9; i++) {
            int dx = i < 8 ? LANE_DIRS[i].dx : 0, dy = i < 8 ? LANE_DIRS[i].dy : 0;
            int lo = myLocX + dx - OPEN_REACH;
            int n = 0;
            for (int k = 1 + dy; k < 2 + 2 * OPEN_REACH + dy; k++) {
                long row = lo >= 0 ? centres[k] >>> lo : centres[k] << -lo;
                n += Long.bitCount(row & window);
            }
            out[i] = n;
        }
        return out;
    }

    // TODO: only dig if we havent seen the cat in some time
    private boolean digSurroundingDirt() throws GameActionException {
        if (!rc.isActionReady()) return false;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (Math.max(Math.abs(dx), Math.abs(dy)) < 2) continue;
                MapLocation checkLoc = new MapLocation(myLoc.x + dx, myLoc.y + dy);
                if (rc.canRemoveDirt(checkLoc) && rc.getGlobalCheese() >= 100) {
                    rc.removeDirt(checkLoc);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean runTick() throws GameActionException {
        boolean hasThreat = (RobotTracker.numEnemies + RobotTracker.numCats) > 0;
        if(!hasThreat) return false;
        var score0 = 0;
        var score1 = 0;
        var score2 = 0;
        var score3 = 0;
        var score4 = 0;
        var score5 = 0;
        var score6 = 0;
        var score7 = 0;
                {
            var loc = myLoc.translate(0, 2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score7 = -1000;
                score0 = -1000;
                score1 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score7 -= 2;
                score0 -= 2;
                score1 -= 2;
            }
        }

                {
            var loc = myLoc.translate(1, 2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score0 = -1000;
                score1 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score0 -= 2;
                score1 -= 2;
            }
        }

                {
            var loc = myLoc.translate(2, 2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score1 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score1 -= 2;
            }
        }

                {
            var loc = myLoc.translate(2, 1);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score1 = -1000;
                score2 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score1 -= 2;
                score2 -= 2;
            }
        }

                {
            var loc = myLoc.translate(2, 0);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score1 = -1000;
                score2 = -1000;
                score3 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score1 -= 2;
                score2 -= 2;
                score3 -= 2;
            }
        }

                {
            var loc = myLoc.translate(2, -1);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score2 = -1000;
                score3 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score2 -= 2;
                score3 -= 2;
            }
        }

                {
            var loc = myLoc.translate(2, -2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score3 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score3 -= 2;
            }
        }

                {
            var loc = myLoc.translate(1, -2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score3 = -1000;
                score4 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score3 -= 2;
                score4 -= 2;
            }
        }

                {
            var loc = myLoc.translate(0, -2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score3 = -1000;
                score4 = -1000;
                score5 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score3 -= 2;
                score4 -= 2;
                score5 -= 2;
            }
        }

                {
            var loc = myLoc.translate(-1, -2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score4 = -1000;
                score5 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score4 -= 2;
                score5 -= 2;
            }
        }

                {
            var loc = myLoc.translate(-2, -2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score5 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score5 -= 2;
            }
        }

                {
            var loc = myLoc.translate(-2, -1);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score5 = -1000;
                score6 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score5 -= 2;
                score6 -= 2;
            }
        }

                {
            var loc = myLoc.translate(-2, 0);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score5 = -1000;
                score6 = -1000;
                score7 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score5 -= 2;
                score6 -= 2;
                score7 -= 2;
            }
        }

                {
            var loc = myLoc.translate(-2, 1);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score6 = -1000;
                score7 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score6 -= 2;
                score7 -= 2;
            }
        }

                {
            var loc = myLoc.translate(-2, 2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score7 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score7 -= 2;
            }
        }

                {
            var loc = myLoc.translate(-1, 2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score7 = -1000;
                score0 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score7 -= 2;
                score0 -= 2;
            }
        }

        for(int idx = 0; idx < RobotTracker.numEnemies; idx++) {
            var robot = RobotTracker.sortedEnemies[idx];
            if(robot == null) continue;
            var weight = switch(robot.type) {
                case BABY_RAT -> 1;
                case RAT_KING -> 3;
                case CAT -> 5;
            };
            var dir = myLoc.directionTo(robot.location);
            switch(dir.ordinal()) {
                case 0 -> {
                    score0           -= weight * 3;
                    score6 += weight;
                    score2 += weight;
                    score3 += weight + 1;
                    score4 += weight + 1;
                    score5 += weight + 1;
                }
                case 1 -> {
                    score1           -= weight * 3;
                    score7 += weight;
                    score3 += weight;
                    score4 += weight + 1;
                    score5 += weight + 1;
                    score6 += weight + 1;
                }
                case 2 -> {
                    score2           -= weight * 3;
                    score0 += weight;
                    score4 += weight;
                    score5 += weight + 1;
                    score6 += weight + 1;
                    score7 += weight + 1;
                }
                case 3 -> {
                    score3           -= weight * 3;
                    score1 += weight;
                    score5 += weight;
                    score6 += weight + 1;
                    score7 += weight + 1;
                    score0 += weight + 1;
                }
                case 4 -> {
                    score4           -= weight * 3;
                    score2 += weight;
                    score6 += weight;
                    score7 += weight + 1;
                    score0 += weight + 1;
                    score1 += weight + 1;
                }
                case 5 -> {
                    score5           -= weight * 3;
                    score3 += weight;
                    score7 += weight;
                    score0 += weight + 1;
                    score1 += weight + 1;
                    score2 += weight + 1;
                }
                case 6 -> {
                    score6           -= weight * 3;
                    score4 += weight;
                    score0 += weight;
                    score1 += weight + 1;
                    score2 += weight + 1;
                    score3 += weight + 1;
                }
                case 7 -> {
                    score7           -= weight * 3;
                    score5 += weight;
                    score1 += weight;
                    score2 += weight + 1;
                    score3 += weight + 1;
                    score4 += weight + 1;
                }
                default -> {
                    assert false : "unexpected direction";
                }
            }
        }
        for(int idx = 0; idx < RobotTracker.numCats; idx++) {
            var robot = RobotTracker.sortedCats[idx];
            if(robot == null) continue;
            var weight = 5;
            var dir = myLoc.directionTo(robot.location);
            switch(dir.ordinal()) {
                case 0 -> {
                    score0           -= weight * 3;
                    score6 += weight;
                    score2 += weight;
                    score3 += weight + 1;
                    score4 += weight + 1;
                    score5 += weight + 1;
                }
                case 1 -> {
                    score1           -= weight * 3;
                    score7 += weight;
                    score3 += weight;
                    score4 += weight + 1;
                    score5 += weight + 1;
                    score6 += weight + 1;
                }
                case 2 -> {
                    score2           -= weight * 3;
                    score0 += weight;
                    score4 += weight;
                    score5 += weight + 1;
                    score6 += weight + 1;
                    score7 += weight + 1;
                }
                case 3 -> {
                    score3           -= weight * 3;
                    score1 += weight;
                    score5 += weight;
                    score6 += weight + 1;
                    score7 += weight + 1;
                    score0 += weight + 1;
                }
                case 4 -> {
                    score4           -= weight * 3;
                    score2 += weight;
                    score6 += weight;
                    score7 += weight + 1;
                    score0 += weight + 1;
                    score1 += weight + 1;
                }
                case 5 -> {
                    score5           -= weight * 3;
                    score3 += weight;
                    score7 += weight;
                    score0 += weight + 1;
                    score1 += weight + 1;
                    score2 += weight + 1;
                }
                case 6 -> {
                    score6           -= weight * 3;
                    score4 += weight;
                    score0 += weight;
                    score1 += weight + 1;
                    score2 += weight + 1;
                    score3 += weight + 1;
                }
                case 7 -> {
                    score7           -= weight * 3;
                    score5 += weight;
                    score1 += weight;
                    score2 += weight + 1;
                    score3 += weight + 1;
                    score4 += weight + 1;
                }
                default -> {
                    assert false : "unexpected direction";
                }
            }
        }
        for (int idx = 0; idx < RobotTracker.numCats; idx++) {
            int[] adj = laneAdjust(RobotTracker.sortedCats[idx]);
            if (adj == null) continue;
            score0 += adj[0];
            score1 += adj[1];
            score2 += adj[2];
            score3 += adj[3];
            score4 += adj[4];
            score5 += adj[5];
            score6 += adj[6];
            score7 += adj[7];
        }
        if (RobotTracker.numCats > 0 && !inCorner()) {
            int[] open = openness();
            score0 += OPEN_WEIGHT * (open[0] - open[8]);
            score1 += OPEN_WEIGHT * (open[1] - open[8]);
            score2 += OPEN_WEIGHT * (open[2] - open[8]);
            score3 += OPEN_WEIGHT * (open[3] - open[8]);
            score4 += OPEN_WEIGHT * (open[4] - open[8]);
            score5 += OPEN_WEIGHT * (open[5] - open[8]);
            score6 += OPEN_WEIGHT * (open[6] - open[8]);
            score7 += OPEN_WEIGHT * (open[7] - open[8]);
        }
        int best = -1;
        var move = Direction.CENTER;
        if(score0 > best) {
            best = score0;
            move = Direction.NORTH;
        }
        if(score1 > best) {
            best = score1;
            move = Direction.NORTHEAST;
        }
        if(score2 > best) {
            best = score2;
            move = Direction.EAST;
        }
        if(score3 > best) {
            best = score3;
            move = Direction.SOUTHEAST;
        }
        if(score4 > best) {
            best = score4;
            move = Direction.SOUTH;
        }
        if(score5 > best) {
            best = score5;
            move = Direction.SOUTHWEST;
        }
        if(score6 > best) {
            best = score6;
            move = Direction.WEST;
        }
        if(score7 > best) {
            best = score7;
            move = Direction.NORTHWEST;
        }
        digDirtInDirection(move);
        if(move != Direction.CENTER && rc.canMove(move)) RatKing.rawMove(move);
        KingTraps.ratTrap();
        // No defensive dirt toward cats: it cost 25 action cooldown and 10 cheese a block, and walled the king in.
        if (RobotTracker.numCats == 0) digSurroundingDirt();
        if(rc.isActionReady()) {
            RobotInfo bestEnemy = null;
            int bestHp = 1000000000;
            for(int idx = 0; idx < RobotTracker.numEnemies; idx++) {
                var robot = RobotTracker.sortedEnemies[idx];
                if(robot == null) continue;
                if(robot.location.distanceSquaredTo(myLoc) > 8) continue;
                if(robot.health < bestHp) {
                    bestEnemy = robot;
                    bestHp = robot.health;
                }
            }
            if(bestEnemy != null) {
                rc.attack(bestEnemy.location);
            } else if (!catTrap()) {
                biteCat();
            }
        }
        return true;
    }

    // While cooperating with no enemy known, a king also traps a cat's lane as rats do (10 cheese for 100 damage).
    private static boolean catTrap() throws GameActionException {
        if (!rc.isCooperation() || RobotTracker.numEnemies > 0 || awubot.comms.EnemyManager.numKnownEnemies > 0) return false;
        for (int idx = 0; idx < RobotTracker.numCats; idx++) {
            RobotInfo cat = RobotTracker.sortedCats[idx];
            if (awubot.comms.CatClock.hold(cat)) continue;
            if (awubot.robot.rat.CatLaneTrap.tryPlaceKing(cat)) return true;
        }
        return false;
    }

    /**
     * Cats are not enemies to the tracker, so the king never bit one. A walking cat pinned against the king scratches
     * it every 3 turns and never leaves; each turn the king bites back for 10.
     */
    private static void biteCat() throws GameActionException {
        for (int idx = 0; idx < RobotTracker.numCats; idx++) {
            MapLocation c = RobotTracker.sortedCats[idx].location;
            if (c.distanceSquaredTo(myLoc) > 18) continue;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    MapLocation t = c.translate(dx, dy);
                    if (rc.canAttack(t)) {
                        rc.attack(t);
                        return;
                    }
                }
            }
        }
    }
}