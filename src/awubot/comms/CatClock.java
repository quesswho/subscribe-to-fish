package awubot.comms;

import battlecode.common.*;
import awubot.RobotTracker;
import static awubot.Game.*;

/**
 * How close the cooperation is to its end. While cooperating the game ends, on points, the moment the last cat dies,
 * and living kings are 30% of those points. On most cat-race maps each team kills the cat on its own side, so the
 * cat a team fights is a clock: when it is low or dead the game ends within a few dozen rounds.
 *
 * Stages (bits, they only grow): LOW, a cat seen down 1500 hp (from there our lane traps fire and it dies within
 * 40-100 rounds); DEAD, a cat seen low and then gone from a tile still in view. A robot learns them by sight, from the
 * shared array (kings write it) and from squeaks: rats fighting the cat are seldom near a king, so every rat that
 * knows a stage the kings have not written squeaks it each turn, and rats relay it to a king.
 *
 * The clock is wrong when more cats live: with 4 or 6 cats (seepastyourgreed, rift) the first dies early and the game
 * goes on to round 2000. OTHER stops the clock for good: a cat seen over LOW_HP once a low one is known, a cat with an
 * id over 4, or (with a starting king's id over 4, a hint of more cats) a cat low before EARLY_LOW_ROUND. Starting ids
 * are not always 1..n (t_minus_five_minutes: kings 9-10, cats 11-14; groveisbeingdesecrated: kings 5-6, cats 3-4), so
 * ids only hint at the cat count. And a dead cat brings rats home for DEAD_ROUNDS only.
 *
 * ENDING is the clock for maps of 4 cats or more: there the game ends when the last cats die, so a robot that has
 * seen 4 different cats or more, and has seen them down to ENDING_SHARE of their hp in total (each counted at the
 * lowest hp it saw), knows that all of them are being killed (t_minus_five_minutes: 4 cats on 20x20, all dead by
 * round ~300; on larger maps of 4 or more cats no robot sees 4 of them while cooperating).
 */
public class CatClock {
    public static final int LOW = 1, DEAD = 2, OTHER = 4, MANY = 8;  // MANY: the map holds 4 cats or more (ids prove it)
    public static final int ENDING = 16;
    private static final int ENDING_CATS = 4;
    private static final int ENDING_SHARE_PERCENT = 60;
    private static final int MAX_TRACKED = 8;
    private static final int[] catIds = new int[MAX_TRACKED], catHp = new int[MAX_TRACKED];
    private static int numTracked = 0;
    public static final int LOW_HP = UnitType.CAT.health - 1500;
    private static final int DYING_HP = 200;
    private static final int DEAD_ROUNDS = 100;
    private static final int MAX_TWO_CAT_ID = 4;
    private static final int EARLY_LOW_ROUND = 100;

    private static int stage = 0;
    private static int lowCatId = -1;
    private static MapLocation lowCatLoc;
    private static int lowCatRound = -1;
    private static int deadRound = -1;
    private static boolean forming;

    /** Reads what the team knows and what this robot sees. After RobotTracker and the shared array are read. */
    public static void preTick() throws GameActionException {
        forming = false;
        if (!rc.isCooperation()) return;
        stage |= GlobalArray.getCatClock();
        // A starting king's id over 4 only hints at more cats: 2-cat maps with kings 5 and 6 (cats 3 and 4) are as
        // common as 4-cat ones (cats 1-4). So it stops the race king (MANY) but not the clock, unless a cat falls low
        // before EARLY_LOW_ROUND: on a 4-cat map the cat beside our king does (seepastyourgreed: low by 49, the game
        // goes on), on groveisbeingdesecrated (2 cats) ours falls low at ~100-180 and the race ends ~250.
        if (time == 1 && isRatKing && rc.getID() > MAX_TWO_CAT_ID) stage |= MANY;
        if (lowCatRound == time - 1 && rc.canSenseLocation(lowCatLoc) && !rc.canSenseRobot(lowCatId)) stage |= DEAD;
        lowCatRound = -1;
        for (int i = RobotTracker.numCats - 1; i >= 0; i--) {
            RobotInfo cat = RobotTracker.sortedCats[i];
            if (cat.getID() > MAX_TWO_CAT_ID) stage |= OTHER | MANY;
            if ((stage & MANY) != 0) track(cat);  // (4 cats seen prove MANY; on 2-cat maps no work)
            if (cat.health > LOW_HP) continue;
            stage |= LOW;
            if ((stage & MANY) != 0 && time < EARLY_LOW_ROUND) stage |= OTHER;
            if (cat.health <= DYING_HP) {
                lowCatId = cat.getID();
                lowCatLoc = cat.location;
                lowCatRound = time;
            }
        }
        for (int i = RobotTracker.numCats - 1; i >= 0 && (stage & LOW) != 0; i--) {
            if (RobotTracker.sortedCats[i].health > LOW_HP) stage |= OTHER;
        }
        if (numTracked >= ENDING_CATS && (stage & ENDING) == 0) {
            int sum = 0;
            for (int i = numTracked - 1; i >= 0; i--) sum += catHp[i];
            if (sum * 100 <= ENDING_SHARE_PERCENT * UnitType.CAT.health * numTracked) stage |= ENDING;
        }
        if ((stage & DEAD) != 0 && deadRound < 0) deadRound = time;
        if (isRatKing) GlobalArray.addCatClock(stage);
        // (not on ENDING: with 4 cats a held cat is seldom the last one, and the game waits for it)
        forming = (stage & (LOW | OTHER)) == LOW && GlobalArray.getFormationTarget() != null
            && GlobalArray.countRatKings() < 3;
    }

    /** The lowest hp this robot has seen each cat at. */
    private static void track(RobotInfo cat) {
        int id = cat.getID();
        for (int i = numTracked - 1; i >= 0; i--) {
            if (catIds[i] == id) {
                if (cat.health < catHp[i]) catHp[i] = cat.health;
                return;
            }
        }
        if (numTracked < MAX_TRACKED) {
            catIds[numTracked] = id;
            catHp[numTracked++] = cat.health;
        }
    }

    public static void heard(int payload) {
        stage |= payload & (LOW | DEAD | OTHER | MANY | ENDING);
    }

    /** A rat squeaks what the kings have not written yet. */
    public static boolean trySqueak() throws GameActionException {
        if (isRatKing || !rc.isCooperation() || (stage & ~GlobalArray.getCatClock()) == 0) return false;
        Squeaker.squeakCatClock(stage);
        return true;
    }

    /** The map holds 4 cats or more: the first dead cat ends nothing (seepastyourgreed, rift). */
    public static boolean manyCats() {
        return (stage & MANY) != 0;
    }

    public static boolean low() {
        return ((stage & (LOW | OTHER)) == LOW || (stage & ENDING) != 0) && rc.isCooperation();
    }

    // While the end king is being gathered, a cat this low is left alone (no bites, no traps): on most race maps the
    // other team's cat died first, so ours is the clock, and the king has to exist when it stops.
    private static final int HOLD_HP = 300;

    public static boolean hold(RobotInfo cat) {
        return forming && cat.health - CAT_TRAP_DAMAGE * rc.getNumberCatTraps() <= HOLD_HP;
    }

    // Our laid cat traps may all fire on the held cat (TheHeist, clockwisdom: a cat held at 300 hp died to them
    // before the end king formed).
    private static final int CAT_TRAP_DAMAGE = 100;

    /** A cat died in the last DEAD_ROUNDS rounds (as far as this robot knows), cooperating. */
    public static boolean dead() {
        return deadRound >= 0 && time - deadRound < DEAD_ROUNDS && (stage & OTHER) == 0 && rc.isCooperation();
    }
}
