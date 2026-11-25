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
        spawningStrategy = new KingSpawningStrategy();
        combatStrategy = new KingCombatStrategy();
        miningStrategy = new KingMiningStrategy();
        formationStrategy = new KingFormationCoordinatorStrategy(miningStrategy);
    }

    @Override
    public boolean runTick() throws GameActionException {
        spawningStrategy.runTick();
        combatStrategy.runTick();
        formationStrategy.runTick();
        miningStrategy.runTick();
        return true;
    }
}
