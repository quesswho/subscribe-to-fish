package awubot.robot.king;

import battlecode.common.*;
import awubot.robot.Strategy;
import awubot.util.Profiler;

import static awubot.Game.*;
import static awubot.util.Util.*;

public class RatKingStrategy extends Strategy {
    private KingSpawningStrategy spawningStrategy;
    private KingCombatStrategy combatStrategy;
    private KingMiningStrategy miningStrategy;
    private KingFormationCoordinatorStrategy formationStrategy;

    public RatKingStrategy() {
        combatStrategy = new KingCombatStrategy();
        miningStrategy = new KingMiningStrategy();
        formationStrategy = new KingFormationCoordinatorStrategy(miningStrategy);
        spawningStrategy = new KingSpawningStrategy(formationStrategy);
    }

    @Override
    public boolean runTick() throws GameActionException {
        spawningStrategy.runTick();
        combatStrategy.runTick();
        formationStrategy.runTick();
        formationStrategy.thirdKingValve();
        miningStrategy.runTick();
        return true;
    }
}
