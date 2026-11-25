package awubot.robot.king;

import battlecode.common.*;
import awubot.comms.CatClock;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import awubot.RobotTracker;
import static awubot.Game.*;
import static awubot.util.Util.*;

import awubot.Game;

public class KingFormationCoordinatorStrategy extends Strategy {
    private static final int[] MINE_THRESHOLDS = {3, 3, 9, 15, 10000000};
    private static final int UPGRADE_COST = 50;
    private static final int MAX_FORMATION_DIST_TO_MINE_SQ = 25;
    private static final int COORDINATION_TIMEOUT = 120;

    private static int MIN_DIST_FROM_KINGS_SQ;

    private final KingMiningStrategy miningStrategy;

    private final int tabooMinX;
    private final int tabooMaxX;
    private final int tabooMinY;
    private final int tabooMaxY;

    private FormationState state = FormationState.IDLE;
    private MapLocation formationTarget = null;
    private MapLocation targetMine = null;
    private int coordinationStartTurn = 0;
    private int kingCount = 1;
    private RobotInfo nearestCat = null;
    // per mine (encode2x2): formations at it that timed out
    private final StringBuilder failedMines = new StringBuilder();

    private enum FormationState {
        IDLE,
        COORDINATING,
        MONITORING
    }

    public KingFormationCoordinatorStrategy(KingMiningStrategy aMiningStrategy) {
        MIN_DIST_FROM_KINGS_SQ = Math.max(400, mapWidth * mapHeight / 5);
        miningStrategy = aMiningStrategy;

        int minX, maxX, minY, maxY;
        if (mapWidth < 40 && mapHeight < 40) {
            minX = mapWidth / 3;
            maxX = 2 * mapWidth / 3;
            minY = mapHeight / 3;
            maxY = 2 * mapHeight / 3;
        } else if (mapWidth < 50 && mapHeight < 50) {
            minX = 3 * mapWidth / 7;
            maxX = 4 * mapWidth / 7;
            minY = 3 * mapHeight / 7;
            maxY = 4 * mapHeight / 7;
        } else {
            minX = 4 * mapWidth / 9;
            maxX = 5 * mapWidth / 9;
            minY = 4 * mapHeight / 9;
            maxY = 5 * mapHeight / 9;
        }
        // The image team (GlobalArray.reversedX/Y) keeps out of the mirror image of the other team's zone, and tests a
        // mine's 2x2 block by the cell mirroring the one the other team tests (on Stash_j0 only one of the 6 mines, on
        // team B's half, lay outside the unmirrored zone, so team A could form a second king there and team B never).
        boolean rx = GlobalArray.reversedX, ry = GlobalArray.reversedY;
        tabooMinX = rx ? mapWidth - 1 - maxX : minX;
        tabooMaxX = rx ? mapWidth - 1 - minX : maxX;
        tabooMinY = ry ? mapHeight - 1 - maxY : minY;
        tabooMaxY = ry ? mapHeight - 1 - minY : maxY;
        blockCellX = rx && mapWidth % 2 == 0 ? 1 : 0;
        blockCellY = ry && mapHeight % 2 == 0 ? 1 : 0;
    }

    private final int blockCellX, blockCellY;

    private boolean isInTabooZone(MapLocation aLoc) {
        return aLoc.x >= tabooMinX && aLoc.x <= tabooMaxX
            && aLoc.y >= tabooMinY && aLoc.y <= tabooMaxY;
    }

    // a mine known by its 2x2 block (Util.decode2x2 gives the block's low cell; along a reversed axis of odd size the
    // mirror of a block straddles two blocks, and the low cell stays)
    private boolean isMineInTabooZone(MapLocation aBlock) {
        return isInTabooZone(aBlock.translate(blockCellX, blockCellY));
    }


    private boolean isNormal() {
        final int CAT_CRITICAL_DIST_SQ = 16;
        final int CAT_DANGER_DIST_SQ = 36;
        final int CRITICAL_HP = 150;

        RobotInfo nearestEnemyRat = null;
        RobotInfo nearestEnemyRatKing = null;
        nearestCat = null;

        int catDistSq = Integer.MAX_VALUE;
        int enemyRatDistSq = Integer.MAX_VALUE;
        int enemyRatKingDistSq = Integer.MAX_VALUE;

        int catLimit = RobotTracker.numCats;
        if (RobotTracker.sortedCats != null) catLimit = Math.min(catLimit, RobotTracker.sortedCats.length);

        for (int i = 0; i < catLimit; i++) {
            RobotInfo myCat = RobotTracker.sortedCats[i];
            if (myCat == null) continue;

            int myDistSq = myLoc.distanceSquaredTo(myCat.location);
            if (myDistSq < catDistSq) {
                catDistSq = myDistSq;
                nearestCat = myCat;
            }
        }

        int enemyLimit = RobotTracker.numEnemies;
        if (RobotTracker.sortedEnemies != null) enemyLimit = Math.min(enemyLimit, RobotTracker.sortedEnemies.length);

        for (int i = 0; i < enemyLimit; i++) {
            RobotInfo myEnemy = RobotTracker.sortedEnemies[i];
            if (myEnemy == null) continue;

            int myDistSq = myLoc.distanceSquaredTo(myEnemy.location);
            UnitType t = myEnemy.getType();

            if (t == UnitType.RAT_KING) {
                if (myDistSq < enemyRatKingDistSq) {
                    enemyRatKingDistSq = myDistSq;
                    nearestEnemyRatKing = myEnemy;
                }
            } else if (t == UnitType.BABY_RAT) {
                if (myDistSq < enemyRatDistSq) {
                    enemyRatDistSq = myDistSq;
                    nearestEnemyRat = myEnemy;
                }
            }
        }

        if (nearestEnemyRatKing != null && enemyRatKingDistSq <= 25) {
            return false;
        }

        int myHp = rc.getHealth();

        if (myHp <= CRITICAL_HP || catDistSq <= CAT_CRITICAL_DIST_SQ) {
            return false;
        } else if (catDistSq <= CAT_DANGER_DIST_SQ || (enemyRatDistSq <= 8 && myHp <= 250)) {
            return false;
        } else if (nearestCat != null || nearestEnemyRat != null) {
            return false;
        } else {
            return true;
        }
    }


    @Override
    public boolean runTick() throws GameActionException {
        watchCatClock();
        int myKingCount = GlobalArray.countRatKings();
        int numRats = (rc.getCurrentRatCost() - 10) * 2 / 5;

        var req = 12;
        if(mapWidth * mapHeight <= 1200) {
            req = 8;
        }
        boolean endKing = isEndKingPhase(myKingCount);
        if (isLastKingPhase(myKingCount) || endKing) req = 0;
        // the third king's window (unless the cat clock's end king forms it): looking for a spot, or gathering at it
        boolean third = !endKing && myKingCount == 2 && time >= THIRD_KING_FROM && time <= THIRD_KING_LAST_ROUND
            && (thirdKingTarget || state == FormationState.IDLE && time <= THIRD_KING_UNTIL);
        if (myKingCount >= 5 || (rc.getRoundNum() >= 1100 && myKingCount >= 2 && !third)
            || numRats < (third ? THIRD_KING_RATS : req * myKingCount)) {
            abortFormation();
            kingCount = myKingCount;
            return true;
        }

        if (myKingCount > kingCount) {
            abortFormation();
            kingCount = myKingCount;
            return true;
        }
        kingCount = myKingCount;
        // The end king goes first: a mine formation gathering when the clock falls low ends with the race
        // (clockwisdom_j1: a mine formation started at 312, the clock low at 378, the game over at 409 one king each).
        if (endKing && state != FormationState.IDLE && !endKingTarget && !lastKingTarget
            && rc.getGlobalCheese() >= END_KING_MIN_BANK && numRats >= END_KING_RATS_PER_KING * myKingCount) {
            abortFormation();
        }

        // if (!isNormal()) {
        //     if (state == FormationState.COORDINATING) {
        //         abortFormation();
        //     }
        //     return true;
        // }

        switch (state) {
            case IDLE:
                tryStartFormation();
                break;
            case COORDINATING:
            case MONITORING:
                monitorFormation();
                break;
        }
        return true;
    }

    boolean danger;
    private void tryStartFormation() throws GameActionException {
        if (GlobalArray.getFormationTarget() != null) return;

        lastKingTarget = false;
        endKingTarget = false;
        thirdKingTarget = false;
        MapLocation formationTarget;
        if (isLastKingPhase(GlobalArray.countRatKings())) {
            formationTarget = lastKingFormationTarget();
        } else if (isEndKingPhase(GlobalArray.countRatKings())) {
            formationTarget = endKingFormationTarget();
        } else {
            formationTarget = null;
            if (time < 1100 || GlobalArray.countRatKings() < 2) {
                formationTarget = mineFormationTarget();
                if (formationTarget == null) formationTarget = catRaceFormationTarget();
            }
            if (formationTarget == null) formationTarget = loneRichFormationTarget();
            if (formationTarget == null) formationTarget = thirdKingFormationTarget();
        }
        if (formationTarget == null) return;

        targetMine = formationTarget;
        coordinationStartTurn = time;
        state = FormationState.COORDINATING;

        GlobalArray.writeFormationTarget(formationTarget, targetMine, kingId);
        GlobalArray.writeKingMineAssignment(kingId + 1, miningStrategy.getMineTarget());
    }

    /** The usual formation: at an unassigned known mine, once the economy and the known mines allow another king. */
    private MapLocation mineFormationTarget() throws GameActionException {
        danger = false;
        if(rc.getHealth() < 100 && GlobalArray.countRatKings() == 1) {
            danger = true;
        } else {
            int myGlobalCheese = rc.getGlobalCheese();
            if(myGlobalCheese < UPGRADE_COST + 100) return null;
            int myKingCount = GlobalArray.countRatKings();
            if (myKingCount == 5) return null;
            int numRats = (rc.getCurrentRatCost() - 10) * 2 / 5;
            if(time <= 1800) {
                if (Game.knownCheeseMines.size() < mineThreshold(GlobalArray.countRatKings())) return null;
                if(mapWidth * mapHeight > 1200) {
                    if (numRats < 20 * myKingCount) return null;
                } else {
                    if (numRats < 20 * myKingCount - 4) return null;
                }
            } else {
                if (numRats < 12) return null;
            }
        }
        return selectBestMineForFormation();
    }

    // Cat race: while cooperating, the game ends when the last cat dies, and living kings are 30% of the points
    // (a second king against one is +10). So a lone king with a bank forms a second king without waiting for a mine,
    // on any clear 3x3 it can see, once the cats are being killed.
    public static final int RACE_MIN_ROUND = 130;
    private static final int RACE_MIN_CHEESE = 1000;
    private static final int RACE_MIN_RATS = 12;
    private static final int RACE_HURT_CAT_HP = UnitType.CAT.health - 1500;
    // 15 traps (1500 damage, the hp test's share): at 10 a slowly hunted cat (uneruesansfin, 4 mines) brought the
    // mine-free king at round ~170, and the game went on to round 2000 with the bank drained
    private static final int RACE_TRAPS_FIRED = 15;
    private static final int RACE_RETRY = 50;
    private boolean catsDying = false;
    private int catTrapsFired = 0;
    private int lastCatTraps = 0;
    private int nextRaceSearch = 0;

    private void watchCatClock() {
        if (!rc.isCooperation()) return;
        if (lowSince < 0 && CatClock.low()) lowSince = time;
        for (int i = RobotTracker.numCats - 1; i >= 0; i--) {
            if (RobotTracker.sortedCats[i].health <= RACE_HURT_CAT_HP) catsDying = true;
        }
        int traps = rc.getNumberCatTraps();
        if (traps < lastCatTraps) catTrapsFired += lastCatTraps - traps;
        lastCatTraps = traps;
        if (catTrapsFired >= RACE_TRAPS_FIRED) catsDying = true;
    }

    // Last king: living kings are half the round-2000 points after a backstab (30% while cooperating), and a lone king
    // against two loses a third of them. From LAST_KING_ROUND a lone king forms a second one on a clear 3x3 beside it,
    // with the fewest rats a formation takes, while the bank (or the kings' hp, if it runs dry) carries both kings
    // to round 2000. Late, a king costs little: 50 cheese, its upkeep for the rounds left and 8 rats' last rounds.
    public static final int LAST_KING_ROUND = 1900;
    private static final int LAST_KING_RATS = 8;
    private static final int LAST_KING_RETRY = 10;
    private static final int LAST_KING_MAX_STARVE = 300;
    private boolean lastKingTarget = false;

    private static boolean isLastKingPhase(int kings) {
        return kings == 1 && time >= LAST_KING_ROUND;
    }

    private MapLocation lastKingFormationTarget() throws GameActionException {
        if (time < nextRaceSearch) return null;
        if (!canAffordLastKing(rc.getGlobalCheese())) return null;
        // The rats it has, and those it can still spawn
        int rats = (rc.getCurrentRatCost() - 10) * 2 / 5, bank = rc.getGlobalCheese();
        while (rats < LAST_KING_RATS) {
            int cost = 10 + 10 * (rats / 4);
            if (!canAffordLastKing(bank - cost)) break;
            bank -= cost;
            rats++;
        }
        if (rats < LAST_KING_RATS) return null;
        nextRaceSearch = time + LAST_KING_RETRY;
        MapLocation spot = clearSpotNearKing();
        if (spot != null) lastKingTarget = true;
        return spot;
    }

    /** Whether, with this bank, the formation (50) and two kings' upkeep to round 2000 leave both kings alive: once
     * the bank is dry each king loses 10 hp a round. */
    private static boolean canAffordLastKing(int bank) {
        if (bank < UPGRADE_COST + 10) return false;
        int left = GameConstants.GAME_MAX_NUMBER_OF_ROUNDS - time;
        int starve = 10 * Math.max(0, left - (bank - UPGRADE_COST) / 4);
        return starve <= LAST_KING_MAX_STARVE && rc.getHealth() > starve + 30;
    }

    /** The last or end king's 3x3 while it is being gathered: the king spawns rats toward it while that stays
     * affordable. */
    public MapLocation lastKingSpot() {
        return (lastKingTarget || endKingTarget) && state != FormationState.IDLE ? targetMine : null;
    }

    public boolean canSpawnForLastKing() {
        int bank = rc.getGlobalCheese() - rc.getCurrentRatCost();
        return endKingTarget ? bank >= END_KING_RESERVE : canAffordLastKing(bank);
    }

    // End king: on most cat-race maps each team kills the cat on its side, so a low cat (CatClock) means the game
    // ends on points within a few dozen rounds (36-89 from 2000 hp in replayed losses against v3, which kills its
    // cat first, and then we end the game when ours dies). Kings are 30% of those points, so up to END_KING_MAX
    // kings form another beside a king at once, without a mine, spawning its rats toward it: 3 kings against 2 win
    // any race with an even cat split. The clock can still be wrong (4 cats on seepastyourgreed, the first dead by
    // round 100), so a king forms only with 8 rats per king we have (no second end king from a 12-rat opening army)
    // and a bank that carries the kings if the game goes on (fullofsecrets starved with a 200 reserve).
    private static final int END_KING_MAX = 3;
    private static final int END_KING_RATS_PER_KING = 8;
    private static final int END_KING_MIN_BANK = 500;
    private static final int END_KING_RESERVE = 400;
    private boolean endKingTarget = false;
    private static final int END_KING_WINDOW = 100;
    private static int lowSince = -1;

    private static boolean isEndKingPhase(int kings) {
        // Not before RACE_MIN_ROUND: a cat that falls that early is often the king's own neighbour, and the game
        // goes on after it (gen_1_73: a backstab by round 100, then the mine-free second king starves the economy).
        // Only soon after the cat fell low (as this king saw it): a race ends within ~100 rounds of that, so a king
        // formed later is not for its end (Hike: a third end king tried 130 rounds after the first, the game went on).
        return kings < END_KING_MAX && CatClock.low() && time >= RACE_MIN_ROUND && time < 1100
            && time - lowSince <= END_KING_WINDOW;
    }

    private MapLocation endKingFormationTarget() throws GameActionException {
        if (time < nextRaceSearch || rc.getGlobalCheese() < END_KING_MIN_BANK) return null;
        if ((rc.getCurrentRatCost() - 10) * 2 / 5 < END_KING_RATS_PER_KING * GlobalArray.countRatKings()) return null;
        nextRaceSearch = time + LAST_KING_RETRY;
        MapLocation spot = clearSpotNearKing();
        if (spot != null) endKingTarget = true;
        return spot;
    }

    // Third king: after round 1200 the engine allows no new king beyond the second, but a third formed before then
    // lives on, and living kings are half the round-2000 points after a backstab (30% while cooperating). A team with
    // two kings forms a third beside a king only while the bank is idle: 1800 and an army of 30 rats or more, past
    // the usual cap, which only a bank at the 32-rat spawn line (2000 - round / 2) buys. The upkeep (2 a round, ~1700
    // to round 2000) then comes out of those extra rats. Upkeep is what killed this before (all three kings starved;
    // under a 400 bank the army also stops being replaced): so the third king gives itself up (thirdKingValve) once
    // the bank falls under 700.
    public static final int THIRD_KING_FROM = 1050;
    private static final int THIRD_KING_UNTIL = 1160;
    private static final int THIRD_KING_LAST_ROUND = 1199;
    private static final int THIRD_KING_BANK = 1800;
    private static final int THIRD_KING_RATS = 30;
    private static final int VALVE_BANK = 700;
    private static final int VALVE2_BANK = 300;
    private static final int VALVE_TREND_ROUNDS = 50;
    private static final int CLOCK_STALE_ROUNDS = 150;
    private boolean thirdKingTarget = false;
    private final int[] bankHistory = new int[64];

    private MapLocation thirdKingFormationTarget() throws GameActionException {
        if (time < THIRD_KING_FROM || time > THIRD_KING_UNTIL || time < nextRaceSearch) return null;
        if (GlobalArray.countRatKings() != 2 || rc.getGlobalCheese() < THIRD_KING_BANK) return null;
        if ((rc.getCurrentRatCost() - 10) * 2 / 5 < THIRD_KING_RATS) return null;
        nextRaceSearch = time + 10;
        MapLocation spot = clearSpotNearKing();
        if (spot != null) thirdKingTarget = true;
        return spot;
    }

    /** A lone king that found no mine to form at but has the third king's bank and army forms its second king beside
     * it first, early enough for the third (maps like testpathfinding: one king to round 1900 on a 2000 bank). */
    public static final int LONE_RICH_FROM = 900;

    private MapLocation loneRichFormationTarget() throws GameActionException {
        if (time < LONE_RICH_FROM || time > THIRD_KING_UNTIL || time < nextRaceSearch) return null;
        if (GlobalArray.countRatKings() != 1 || rc.getGlobalCheese() < THIRD_KING_BANK) return null;
        if ((rc.getCurrentRatCost() - 10) * 2 / 5 < THIRD_KING_RATS) return null;
        nextRaceSearch = time + 10;
        return clearSpotNearKing();
    }

    /** Called every round by every king: a third king formed in the window disintegrates once the bank runs low and
     * keeps falling, so its upkeep never starves the army and the other kings.
     * Near the end the floor is the three kings' upkeep for the rounds left. The cat clock's end kings join it once
     * the clock is off (a backstab, or a healthy cat seen): the game goes on, and a king formed for its end only eats
     * the bank (checkerboard: a cat jan29 brought low, 3 kings by round 200, a backstab, all 3 starved by 1300). So do
     * they once the game outlives them by CLOCK_STALE_ROUNDS with the clock still on: a race ends within ~100 rounds
     * of a low cat (fullofsecrets: 2 end kings spawned the bank down to 400, cooperation went on, all 3 starved). */
    public void thirdKingValve() throws GameActionException {
        int bank = rc.getGlobalCheese();
        int ago = bankHistory[(time - VALVE_TREND_ROUNDS) & 63];
        bankHistory[time & 63] = bank;
        boolean clockOff = !CatClock.low() || time - RatKing.bornRound > CLOCK_STALE_ROUNDS;
        int kings = GlobalArray.countRatKings();
        int left = GameConstants.GAME_MAX_NUMBER_OF_ROUNDS - time;
        boolean give;
        if (kings >= 3) {
            give = (RatKing.extraKing || RatKing.clockKing && clockOff)
                && (bank < 10 || bank < Math.min(VALVE_BANK, 6 * left) && bank < ago);
        } else {
            // Two kings, one of them formed beside the other (no mine of its own): once the bank runs low and keeps
            // falling it gives itself up too, so one king lives on what the rats bring (Hike: an end king at ~320,
            // the game went on, the bank ran dry by 500 and both kings starved on ~3 cheese a round).
            give = kings == 2 && RatKing.clockKing
                && (bank < 10 && left > 60 || clockOff && bank < Math.min(VALVE2_BANK, 4 * left) && bank < ago);
        }
        if (give) {
            // the game outlived the clock: no more end kings
            if (RatKing.clockKing) GlobalArray.addCatClock(CatClock.OTHER);
            GlobalArray.clearMyKingSlot();
            rc.disintegrate();
        }
    }

    private MapLocation catRaceFormationTarget() throws GameActionException {
        if (!catsDying || !rc.isCooperation() || time < RACE_MIN_ROUND || time < nextRaceSearch) return null;
        // With 4 cats or more the first cats to die end nothing: the race king only costs the mine king.
        if (CatClock.manyCats()) return null;
        if (GlobalArray.countRatKings() != 1 || rc.getGlobalCheese() < RACE_MIN_CHEESE) return null;
        if ((rc.getCurrentRatCost() - 10) * 2 / 5 < RACE_MIN_RATS) return null;
        nextRaceSearch = time + 10;
        return clearSpotNearKing();
    }

    /** A centre 3 tiles from the king (Chebyshev) whose whole 3x3 is in view and passable, farthest from the cats. */
    private MapLocation clearSpotNearKing() throws GameActionException {
        MapLocation best = null;
        int bestScore = Integer.MIN_VALUE;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy += (dx == -3 || dx == 3) ? 1 : 6) {
                MapLocation c = myLoc.translate(dx, dy);
                if (!isClear3x3(c)) continue;
                int score = Integer.MAX_VALUE;
                for (int i = RobotTracker.numCats - 1; i >= 0; i--) {
                    score = Math.min(score, c.distanceSquaredTo(RobotTracker.sortedCats[i].location));
                }
                if (score > bestScore) {
                    bestScore = score;
                    best = c;
                }
            }
        }
        return best;
    }

    private boolean isClear3x3(MapLocation c) throws GameActionException {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                MapLocation t = c.translate(x, y);
                if (!rc.canSenseLocation(t) || !rc.senseMapInfo(t).isPassable()) return false;
            }
        }
        return true;
    }

    // A mine king's catchment: the known mines it would stand nearer to than any king does (their carriers walk
    // farther today). Formation spots counted only distance to the kings and the centre, so second kings formed at the
    // nearest allowed mine while whole clusters of far mines went unharvested (mine-rich maps).
    private static final int CATCHMENT_WEIGHT = 1000;
    private static MapLocation[] catchMines = new MapLocation[0];
    private static int[] catchKingDist = new int[0];

    private static void prepareCatchment() {
        int n = Game.knownCheeseMines.set.length();
        if (catchMines.length < n) {
            catchMines = new MapLocation[n];
            catchKingDist = new int[n];
        }
        for (int j = n - 1; j >= 0; j--) {
            MapLocation m = decode2x2(Game.knownCheeseMines.at(j));
            int best = Integer.MAX_VALUE;
            for (int i = 0; i < 5; i++) {
                MapLocation k = GlobalArray.getRatKing(i);
                if (k != null) best = Math.min(best, k.distanceSquaredTo(m));
            }
            catchMines[j] = m;
            catchKingDist[j] = best;
        }
    }

    private static int catchment(MapLocation aMine) {
        int c = 0;
        for (int j = Game.knownCheeseMines.set.length() - 1; j >= 0; j--) {
            if (catchMines[j].distanceSquaredTo(aMine) < catchKingDist[j]) c++;
        }
        return c;
    }

    private MapLocation selectBestMineForFormation() throws GameActionException {
        MapLocation myBestMine = null;
        int myBestScore = Integer.MIN_VALUE;

        prepareCatchment();
        int myMineCount = Game.knownCheeseMines.set.length();
        for (int i = 0; i < myMineCount && i < 10; i++) {
            MapLocation myMine = decode2x2(Game.knownCheeseMines.at(i));
            if (myMine == null) continue;

            if (GlobalArray.isMineAssignedToAnyKing(myMine)) continue;
            if (isMineInTabooZone(myMine)) continue;
            if (timeouts(myMine) >= 1) continue;

            int myScore = scoreMineForFormation(myMine);
            if (myScore > myBestScore) {
                myBestScore = myScore;
                myBestMine = myMine;
            }
        }
        if(time < 1800 && myBestScore < -40000) {
            return null;
        }
        return myBestMine;
    }

    /** Known mines needed before forming king number kings + 1. On small maps (<= 900 tiles) the second king still
     * waits for 5: the earlier one (3) gained on mid-sized and tortuous maps but lost there, against both opponents. */
    private static int mineThreshold(int kings) {
        if (kings == 1 && mapWidth * mapHeight <= 900) return 5;
        return MINE_THRESHOLDS[kings];
    }

    private int timeouts(MapLocation aMine) {
        char c = (char) encode2x2(aMine);
        int n = 0;
        for (int i = failedMines.length() - 1; i >= 0; i--) if (failedMines.charAt(i) == c) n++;
        return n;
    }

    private int scoreMineForFormation(MapLocation aMine) throws GameActionException {
        int myScore = 0;

        for (int i = 0; i < 5; i++) {
            MapLocation myKingLoc = GlobalArray.getRatKing(i);
            if (myKingLoc != null) {
                int myDist = aMine.distanceSquaredTo(myKingLoc);
                if (myDist < MIN_DIST_FROM_KINGS_SQ) {
                    myScore -= 50000;
                }

                myScore -= myDist;
            }
        }

        myScore -= aMine.distanceSquaredTo(new MapLocation(mapWidth / 2, mapHeight / 2));
        myScore += CATCHMENT_WEIGHT * catchment(aMine);

        int myDistFromSelf = myLoc.distanceSquaredTo(aMine);
        if (myDistFromSelf > 400) {
            myScore -= 50;
        }

        return myScore;
    }

    private MapLocation findFormationSpotNearMine(MapLocation aMine) throws GameActionException {
        int myBestDistFromMine = Integer.MAX_VALUE;
        MapLocation myBestSpot = null;

        for (int myDx = -5; myDx <= 5; myDx++) {
            for (int myDy = -5; myDy <= 5; myDy++) {
                int myDistToMine = myDx * myDx + myDy * myDy;
                if (myDistToMine > MAX_FORMATION_DIST_TO_MINE_SQ) continue;

                MapLocation myCandidate = aMine.translate(myDx, myDy);
                if (!rc.onTheMap(myCandidate)) continue;

                if (!isValidFormationCenter(myCandidate)) continue;

                if (myDistToMine < myBestDistFromMine) {
                    myBestDistFromMine = myDistToMine;
                    myBestSpot = myCandidate;
                }
            }
        }

        return myBestSpot;
    }

    private boolean isValidFormationCenter(MapLocation aCenter) throws GameActionException {
        if (isInTabooZone(aCenter)) return false;

        for (int i = 0; i < 5; i++) {
            MapLocation myKingLoc = GlobalArray.getRatKing(i);
            if (myKingLoc != null && aCenter.distanceSquaredTo(myKingLoc) < MIN_DIST_FROM_KINGS_SQ) {
                return false;
            }
        }

        return true;
    }

    private void monitorFormation() throws GameActionException {
        if (!lastKingTarget && isLastKingPhase(kingCount) && targetMine != null
            && targetMine.distanceSquaredTo(myLoc) > 64) {
            abortFormation();
            return;
        }
        if (time - coordinationStartTurn > COORDINATION_TIMEOUT) {
            if (targetMine != null) failedMines.append((char) encode2x2(targetMine));
            nextRaceSearch = time + (lastKingTarget || endKingTarget ? LAST_KING_RETRY : RACE_RETRY);
            abortFormation();
            return;
        }

        if (rc.getGlobalCheese() < UPGRADE_COST) {
            abortFormation();
            return;
        }

        MapLocation myCurrentTarget = GlobalArray.getFormationTarget();
        if (myCurrentTarget == null) {
            state = FormationState.IDLE;
            return;
        }

        if (nearestCat != null &&
            formationTarget != null &&
            nearestCat.location.distanceSquaredTo(formationTarget) < 625) {
            abortFormation();
            return;
        }
    }

    private void abortFormation() throws GameActionException {
        int myCoordinator = GlobalArray.getFormationCoordinator();
        if (myCoordinator == kingId) {
            GlobalArray.clearFormation();
        }
        state = FormationState.IDLE;
        formationTarget = null;
        targetMine = null;
        lastKingTarget = false;
        endKingTarget = false;
        thirdKingTarget = false;
    }
}
