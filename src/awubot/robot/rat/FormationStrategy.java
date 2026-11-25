package awubot.robot.rat;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;
import awubot.Game;
import awubot.Pathfinder;
import awubot.RobotPlayer;
import awubot.RobotTracker;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import awubot.robot.king.KingFormationCoordinatorStrategy;
import static awubot.Game.*;
import static awubot.util.Util.*;

// TODO: refactor this (AI slop)

public class FormationStrategy extends Strategy {
    private static final int FORMATION_TIMEOUT = 120;
    private static final int MIN_ALLIES_FOR_FORMATION = 7;
    private static final int MAX_DIST_TO_JOIN = 625;

    private MapLocation formationTarget = null;
    private int formationStartTurn = 0;
    private boolean active = false;
    private int formationKings = 0;
    private MapLocation formedTarget = null;

    private boolean isComplete() throws GameActionException {
        if(!active) return true;
        if(formationTarget == null
           || time - formationStartTurn > FORMATION_TIMEOUT
           || rc.getGlobalCheese() < 50) {
            active = false;
            return true;
        }
        for(int i = 0; i < 4; i++) {
            int angle;
            if(i == time % 4) {
                angle = 180;
            } else {
                angle = 360;
            }
            for(int j = RobotTracker.catHistNum[i] - 1; j >= 0; j--) {
                var cat = RobotTracker.catHist[i][j];
                if(cat == null) continue;
                if(cat.location.isWithinDistanceSquared(formationTarget, UnitType.CAT.visionConeRadiusSquared, cat.direction, angle, true)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean runTick() throws GameActionException {
        tryActivate();
        if(isComplete()) return false;
        if(rc.getRawCheese() > 0) {
            var distToFormation = myLoc.distanceSquaredTo(formationTarget);
            var distToHome = myLoc.distanceSquaredTo(GlobalArray.nearestRatKing);
            if(rc.getRawCheese() >= 40 && distToHome < distToFormation - 25) {
                return false;
            }
        }

        // Late formations beside a king gather 30 rats or more, enough for two 3x3s: one king per formation.
        if (time >= KingFormationCoordinatorStrategy.LONE_RICH_FROM && GlobalArray.countRatKings() > formationKings) {
            active = false;
            formedTarget = formationTarget;
            return false;
        }
        if (myLoc.distanceSquaredTo(formationTarget) <= 8 && rc.canBecomeRatKing()) {
            rc.becomeRatKing();
            Game.convertKing();
            // Only clear formation if we're at the coordinator's target location
            if (myLoc.equals(formationTarget)) {
                GlobalArray.clearFormation();
            }
            RobotPlayer.resetHasInitialisedRobot();
            return true;
        }
        if (RobotTracker.allyCount >= MIN_ALLIES_FOR_FORMATION) {
            if (myLoc.distanceSquaredTo(formationTarget) <= 2) {
                tryMoveToCentre();
                return true;
            }
        }
        BabyRat.autoMove(Pathfinder.getMove(formationTarget));
        return true;
    }

    public boolean tryActivate() throws GameActionException {
        MapLocation target = GlobalArray.getFormationTarget();
        if (target == null) {
            if (active) {
                active = false;
                formationTarget = null;
            }
            return false;
        }

        MapLocation mine = GlobalArray.getFormationMine();
        if (mine == null) {
            if (active) {
                active = false;
                formationTarget = null;
            }
            return false;
        }

        if (target.equals(formedTarget)) return false;
        formationTarget = target;

        if (active) {
            return true;
        }

        // In the last rounds every rat joins: a second king then is worth more than its last trips and bites.
        int distToMine = myLoc.distanceSquaredTo(mine);
        if (distToMine > MAX_DIST_TO_JOIN && time < KingFormationCoordinatorStrategy.LAST_KING_ROUND) return false;

        formationStartTurn = time;
        formationKings = GlobalArray.countRatKings();
        active = true;
        return true;
    }

    private void tryMoveToCentre() throws GameActionException {
        if (!rc.isMovementReady()) return;
        if (myLoc.equals(formationTarget)) return;

        if (myLoc.distanceSquaredTo(formationTarget) > 2 || shouldBecomeCentre()) {
            BabyRat.autoMove(Pathfinder.getMove(formationTarget));
            return;
        }
    }

    private boolean shouldBecomeCentre() throws GameActionException {
        if (!rc.canSenseRobotAtLocation(formationTarget)) {
            return false;
        }

        RobotInfo robotAtCentre = rc.senseRobotAtLocation(formationTarget);
        if (robotAtCentre != null) {
            return false;
        }

        for (RobotInfo robot : rc.senseNearbyRobots(2, rc.getTeam())) {
            if (robot.getType() == UnitType.BABY_RAT && robot.getID() < rc.getID()
                && robot.location.distanceSquaredTo(formationTarget) <= 2) {
                return false;
            }
        }

        return true;
    }
}
