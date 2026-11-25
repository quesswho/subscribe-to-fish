package awubot.comms;

import battlecode.common.*;
import static awubot.util.Util.*;

import awubot.CellTracker;
import awubot.Game;
import awubot.util.Profiler;
import awubot.util.string.FastIndexedIntSet;

import static awubot.Game.*;
import awubot.RobotTracker;

public class Squeaker {
    public static final int N = 32;
    public static final MapLocation NULL_LOC = new MapLocation(15, 15);
    public static final int SQUEAK_RAD = 16; // radius squared

    /*
        Packet layout:
        - Negative bytes: legacy special cases
            * 10: cheese mine locations (rounded to 2x2), packed as 3x 10-bit slots
            * 11: enemy combat
        - Non-negative bytes: 3-bit header (bits 31..29)
            * 000: map chunk (29-bit payload, MapTracker.encodeChunk)
            * 001: symmetry (payload uses low 2 bits)
            * 111: no-op (panic squeak)
            * others unused
    */
    public static final int HEADER_BITS = 3;
    private static final int HEADER_SHIFT = 32 - HEADER_BITS; // 29
    private static final int BODY_BITS = 32 - HEADER_BITS;    // 29
    private static final int BODY_MASK = (1 << BODY_BITS) - 1;
    private static final int HEADER_MAP = 0;
    private static final int HEADER_SYMMETRY = 1;
    private static final int HEADER_NOP = 7;

    private static final int CHEESE_LOC_BITS = 10;
    private static final int CHEESE_LOC_MASK = (1 << CHEESE_LOC_BITS) - 1; // 1023
    // encode2x2 never reaches 1023 on 60x60 maps, so we can use it as a sentinel.
    private static final int CHEESE_EMPTY = CHEESE_LOC_MASK;
    private static final int CHEESE_SLOTS = 3;

    public static FastIndexedIntSet minesToSend = new FastIndexedIntSet();

    public static void registerMine(MapLocation loc) {
        int encoded = encode2x2(loc);
        minesToSend.add(encoded);
        Game.knownCheeseMines.add(encoded);
    }

    // Squeak up to 3 mines per message (full 30-bit payload).
    private static boolean squeakMine() {
        int len = minesToSend.set.length();
        if (len == 0) return false;

        int packed = 0;
        int sent = 0;
        int idx = time % len;
        int scanned = 0;

        while (len > 0 && sent < CHEESE_SLOTS && scanned < len) {
            int loc = minesToSend.at(idx);
            if (GlobalArray.seenCheeseMine(loc)) {
                minesToSend.remove(loc);
                len--;
                if (len == 0) break;
                if (idx >= len) idx = 0;
                continue;
            }
            packed |= (loc & CHEESE_LOC_MASK) << (sent * CHEESE_LOC_BITS);
            sent++;
            scanned++;
            idx++;
            if (idx >= len) idx = 0;
        }

        if (sent == 0) return false;
        for (; sent < CHEESE_SLOTS; sent++) {
            packed |= CHEESE_EMPTY << (sent * CHEESE_LOC_BITS);
        }

        rc.squeak((1 << 31) | packed);
        return true;
    }

    private static void processMessage(Message msg) {
        RobotTracker.addAlly(msg.getSource());
        // println(rc.getID() + " received from " + msg.getSenderID() + " content " + msg.getBytes());
        int bytes = msg.getBytes();
        if (bytes < 0) {
            switch (1 & (bytes >>> 30)) {
                case 0:
                    int loc = bytes & 1023;
                    GlobalArray.registerGlobalCheeseMine(loc);
                    if (!isRatKing) {
                        minesToSend.add(loc);
                    }
                    break;
                case 1:
                    int lo = bytes & ((1 << 15) - 1);
                    int hi = (bytes >>> 15) & ((1 << 15) - 1);
                    EnemyManager.registerPacket(lo, msg.getSource());
                    EnemyManager.registerPacket(hi, msg.getSource());
                    break;
                default:
                    assert(false);
            }
        } else {
            int header = bytes >>> HEADER_SHIFT;
            int payload = bytes & BODY_MASK;
            switch (header) {
                case HEADER_MAP:
                    MapManager.applyGlobalChunk(payload);
                    break;
                case HEADER_SYMMETRY:
                    if (!isRatKing) return; // symmetry is for rat kings
                    CellTracker.setSymmetry(payload & 0b11);
                    break;
                case HEADER_NOP:
                    // Intentional no-op (panic squeak)
                    break;
                default:
                    // println("recieved bad packet " + msg);
                    // println("header is " + header);
                    break;
            }
        }
    }

    public static void squeakEnemy(int data) {
        rc.squeak((0b11 << 30) | data);// indicate
    }

    public static void squeakLureSignal(MapLocation myCatLoc, MapLocation myEnemyKingLoc) {
        int myEncodedCatLoc = encode2x2(myCatLoc);
        int myEncodedEnemyLoc = encode2x2(myEnemyKingLoc);
        int mySqueakContent = (0b10 << HEADER_SHIFT) | (myEncodedCatLoc << 10) | myEncodedEnemyLoc;
        rc.squeak(mySqueakContent);
    }

    // messages come in chronological order
    // there is currently a bug where fat units get sent the same message multiple times
    private static int lastMessageCount = 0;
    public static void preTick() {
        Message[] msgs = rc.readSqueaks(time);
        var oldLastMessageCount = lastMessageCount;
        lastMessageCount = msgs.length;
        int j = 4;
        for (int i = msgs.length - 1; i >= 0; i--) {
            processMessage(msgs[i]);
            if(j-- == 0) return;
        }
        msgs = rc.readSqueaks(time - 1);
        for (int i = msgs.length - 1; i >= oldLastMessageCount; i--) {
            processMessage(msgs[i]);
            if(j-- == 0) return;
        }
    }

    public static boolean nearRatKing() throws GameActionException {
        RobotInfo bots[] = rc.senseNearbyRobots(SQUEAK_RAD);
        for (RobotInfo bot : bots) {
            if (bot.getType() == UnitType.RAT_KING && bot.getTeam() == team) {
                return true;
            }
        }
        return false;
    }

    public static void postTick() throws GameActionException {
        if (!isRatKing) {
            if (RobotTracker.catHistNum[0] > 0 || RobotTracker.catHistNum[1] > 0 || RobotTracker.catHistNum[2] > 0 || RobotTracker.catHistNum[3] > 0) {
                if (RobotTracker.numCats > 0 && RobotTracker.sortedCats[0].getLocation().distanceSquaredTo(GlobalArray.nearestRatKing) < 25) {
                    // panic squeak
                    squeakPanic();
                }
                return;
            }
        } else if (RobotTracker.catHistNum[0] > 0) {
            return;
        }
        if (nearRatKing()) {
            // indicate("NEAR RAT KING");
            if (GlobalArray.peepSymmetry() == CellTracker.Symmetry.Unknown && CellTracker.isSymmetryKnown()) {
                rc.squeak((HEADER_SYMMETRY << HEADER_SHIFT) | CellTracker.getSymmetry().ordinal());
                // indicate("SQUEAKING SYMMETRY");
                return;
            }
            if (!isRatKing && squeakMap()) return;
        }
        if (EnemyManager.trySqueak()) return;
        if (!isRatKing) {
            squeakMine(); // can add other squeaks later
        }
    }

    /**
     * Baby-rat map squeak: diff local vs global and send one differing chunk.
     * @return true if a chunk was squeaked.
     */
    private static boolean squeakMap() throws GameActionException {
        int payload = MapManager.getLocalDiffChunk();
        if (payload == -1) return false;
        rc.squeak((HEADER_MAP << HEADER_SHIFT) | payload);
        // Optimistically apply to our global copy to reduce repeat squeaks.
        MapManager.applyGlobalChunk(payload);
        return true;
    }

    /**
     * Panic squeak to distract cats: send an empty payload (0).
     * This is a no-op for receivers but still produces noise.
     */
    private static void squeakPanic() throws GameActionException {
        rc.squeak(HEADER_NOP << HEADER_SHIFT);
    }
}
