package awubot.comms;

import battlecode.common.*;
import static awubot.util.Util.*;

import javax.management.RuntimeErrorException;

import awubot.CellTracker;
import awubot.Game;
import awubot.util.Profiler;
import awubot.util.Rng;
import awubot.util.string.FastIndexedIntSet;

import static awubot.Game.*;

public class GlobalArray {
    public static final int N = 64;
    private static final int M = 10;

    /*

    ts hasnt been updated in like 3 weeks

      LAYOUT:
      0: global rat counter
      1: symmetry
      2-6: rat king locations (upper 5 bits of x and y, rounded to 2x2)
      7: rat king LSBs (2 bits per king: bit0=x LSB, bit1=y LSB)
      8: rat king turn counter (00, 01, 10 for alive) and (11) for dead
      9: formation coordinator king ID
      10-11: formation target location (exact, 2 slots)
      12-13: formation mine location (exact, 2 slots)
      14-15: unused
      16-20: king mine assignments (5 slots for 5 kings)
      21-31: rat spawn roles
      32-37: map chunk comms (two chunks, 3 slots each)
      38-63: cheese mines (count upwards from 38)
    */

    private static final int COUNTER = 0;
    private static final int SYMMETRY = 1;
    private static final int RAT_KING_LOC = 2;
    private static final int RAT_KING_LSBS = 7;
    private static final int RAT_KING_TURN_COUNTER = 8;
    private static final int FORMATION_COORDINATOR = 9;
    private static final int FORMATION_TARGET = 10;  // uses 2 slots: 10-11
    private static final int FORMATION_MINE = 12;    // uses 2 slots: 12-13
    private static final int ALIVE_RAT_COUNT = 14;
    private static final int KING_MINE_ASSIGNMENT = 16;  // 5 slots for 5 kings (indices 16-20)

    // layout: 
    // lower bits: store target rat id modulo 255 (255 = null)
    // upper bits: strategy index (0 to 3)
    private static final int KING_EXPAND = 21;
    private static final int RAT_SPAWN_INFO = 22;  // spam until MIN_CHEESE is hit
    private static final int MAP_CHUNK_START = 32; // slots 32-37
    private static final int MAP_CHUNK_SLOTS = 6;
    private static final int MAP_CHUNK_SLOTS_PER_CHUNK = 3;
    private static final int MAP_CHUNK_EMPTY_FLAG = 1 << 29; // top bit of 30-bit payload
    private static final int MAP_CHUNK_MASK = MAP_CHUNK_EMPTY_FLAG - 1; // low 29 bits
    private static final int MIN_CHEESE = MAP_CHUNK_START + MAP_CHUNK_SLOTS;
    public static FastIndexedIntSet globalCheeseMines = new FastIndexedIntSet();
    public static MapLocation nearestRatKing;
    public static int nearestRatKingId;
    public static int ratCounter;
    public static int numberOfRatsAlive;

    // Map chunk queue (per-robot, used by rat kings to broadcast chunks via shared array).
    private static final int MAP_CHUNK_QUEUE_MAX = 16;
    private static final int MAP_CHUNK_QUEUE_MASK = MAP_CHUNK_QUEUE_MAX - 1;
    private static final int[] mapChunkQueue = new int[MAP_CHUNK_QUEUE_MAX];
    private static int mapChunkQueueHead = 0;
    private static int mapChunkQueueSize = 0;
    private static int mapChunkSeenMask = 0;

    // Cheese mine queue (per-robot, used by rat kings to broadcast mines via shared array).
    private static final int CHEESE_QUEUE_MAX = 64; // power-of-two for cheap wrap
    private static final int CHEESE_QUEUE_MASK = CHEESE_QUEUE_MAX - 1;
    private static final int[] cheeseQueue = new int[CHEESE_QUEUE_MAX];
    private static int cheeseQueueHead = 0;
    private static int cheeseQueueSize = 0;

    private static MapLocation toSpawn0 = null, toSpawn1 = null;
    private static int pendingPayload0 = -1, pendingPayload1 = -1;

    public static void registerSpawnedRat(MapLocation loc, int payload) throws GameActionException {
        // println("prepared rat to spawn at " + loc + " with payload " + payload);
        toSpawn1 = loc;
        pendingPayload1 = payload;
    }

    private static void _registerSpawnedRat(MapLocation loc, int payload) throws GameActionException {
        // println("actually registering spawned rat at " + loc + " with payload " + payload);
        assert(0 <= payload && payload < 8192);
        int dx = loc.x - myLocX + 4;
        assert(0 <= dx && dx < 9);
        int dy = loc.y - myLocY + 4;
        assert(0 <= dy && dy < 9);
        int marker = (dx * 9) + dy;

        int lo = payload & 1023, hi = payload >>> 10;
        assert(0 <= hi && hi < 8);
        assert(0 <= lo && lo < 1024);

        writeSharedArray(RAT_SPAWN_INFO + kingId * 2, (hi << 7) | marker);
        writeSharedArray(RAT_SPAWN_INFO + kingId * 2 + 1, lo);
        // println("registered spawned rat at " + loc + " with payload " + payload + " into slots " + (RAT_SPAWN_INFO + kingId * 2) + " and " + (RAT_SPAWN_INFO + kingId * 2 + 1));
        ratCounter++;
    }

    public static void expandKing() throws GameActionException {
        int x = rc.readSharedArray(KING_EXPAND);
        x |= (1 << kingId);
        rc.writeSharedArray(KING_EXPAND, x);
    }

    public static void contractKing() throws GameActionException {
        int x = rc.readSharedArray(KING_EXPAND);
        x &= ~(1 << kingId);
        rc.writeSharedArray(KING_EXPAND, x);
    }

    public static boolean isKingExpanded(int i) throws GameActionException {
        int x = rc.readSharedArray(KING_EXPAND);
        return (1 & x >> kingId) != 0;
    }

    public static int loadSpawnInfo() throws GameActionException {
        for (int i = RAT_SPAWN_INFO, king = 0; i < RAT_SPAWN_INFO + 10; i += 2, king++) {
            int x = rc.readSharedArray(i);
            // println("looking for spawn info in slot " + i + ": " + x);
            if (x == 1023) continue;
            int marker = x & 127;
            MapLocation loc = getRatKing(king);
            // println("location: " + loc);
            if (loc == null) continue;
            int dx = (marker / 9) - 4;
            int dy = (marker % 9) - 4;
            // println("dx, dy: " + dx + ", " + dy);
            if (!loc.translate(dx, dy).equals(myLoc)) continue;
            int lo = rc.readSharedArray(i + 1);
            int hi = x >>> 7;
            int indexInList = (i - RAT_SPAWN_INFO) / 2;
            myIndex = (ratCounter - 1 - indexInList + 1024) % 1024;
            return (hi << 10) | lo;
        }
        // println("FAILED TO FIND MY SPAWN INFO");
        return -1;
    }

    public static void writeSharedArray(int i, int x) throws GameActionException {
        rc.writeSharedArray(i, x);
    }

    public static void init() throws GameActionException {
        mapChunkQueueHead = 0;
        mapChunkQueueSize = 0;
        mapChunkSeenMask = 0;
        cheeseQueueHead = 0;
        cheeseQueueSize = 0;
        if (isRatKing && time == 1) {
            kingId = findRatKingSlot();
            writeSharedArray(RAT_KING_TURN_COUNTER, 1023);
            writeSharedArray(RAT_KING_LSBS, 0);
            writeSharedArray(N - 1, 1023);
            for (int i = 0; i < 5; i++) {
                writeSharedArray(i + RAT_KING_LOC, 1023);
            }
            initRatKing();
        }
    }

    public static void initRatKing() throws GameActionException {
        kingId = findRatKingSlot();
        writeMyLoc();
        markKingAlive();
        seedCheeseQueueFromSet();
    }

    public static void preTick() throws GameActionException {
        // indicate("my id " + kingId);
        ratCounter = rc.readSharedArray(COUNTER);
        numberOfRatsAlive = rc.readSharedArray(ALIVE_RAT_COUNT);
        readMapChunks();
        if (isRatKing) {
            if (isFirstKingInRound()) {
                for (int i = RAT_SPAWN_INFO; i < RAT_SPAWN_INFO + 10; i++) {
                    writeSharedArray(i, 1023);
                }
            }
            toSpawn0 = toSpawn1;
            pendingPayload0 = pendingPayload1;
            toSpawn1 = null;
            pendingPayload1 = -1;
            writeMyLoc();
            markKingAlive();
            updateRatKingTurnCounters();
        }
        checkSymmetry();
        readCheeseMines();
        findNearestRatKing();
    }

    public static boolean isFirstKingInRound() throws GameActionException {
        int x = rc.readSharedArray(RAT_KING_TURN_COUNTER);  
        for (int i = 0; i < 5; i++, x >>>= 2) {
            int bits = x & 0b11;
            if (bits == time % 3) return false;
        }
        return true;
    }

    public static void postTick() throws GameActionException {
        if (isRatKing) {
            if (cur_sym == 0 && CellTracker.isSymmetryKnown()) {
                writeSharedArray(SYMMETRY, CellTracker.getSymmetry().ordinal());
            }
            writeMapChunks();
            writeCheeseMines();
            writeMyLoc();
            if (toSpawn0 != null) {
                assert(pendingPayload0 != -1);
                _registerSpawnedRat(toSpawn0, pendingPayload0);
            }
            rc.writeSharedArray(COUNTER, ratCounter);
            rc.writeSharedArray(ALIVE_RAT_COUNT, (rc.getCurrentRatCost() - 10) * 2 / 5);

        }
    }

    /**
     * Records a chunk as seen by this rat king and pushes it to the front of the queue
     * if it has never been seen before.
     */
    public static void noteMapChunkSeen(int encodedChunk) {
        if (!isRatKing) return;
        int chunkX = MapTracker.decodeChunkX(encodedChunk);
        int chunkY = MapTracker.decodeChunkY(encodedChunk);
        int chunkIndex = (chunkY << 2) | chunkX;
        int bit = 1 << chunkIndex;
        if ((mapChunkSeenMask & bit) != 0) return;
        mapChunkSeenMask |= bit;
        if (mapChunkQueueSize < MAP_CHUNK_QUEUE_MAX) {
            mapChunkQueueHead = (mapChunkQueueHead - 1) & MAP_CHUNK_QUEUE_MASK;
            mapChunkQueue[mapChunkQueueHead] = chunkIndex;
            mapChunkQueueSize++;
        }
    }

    private static void seedCheeseQueueFromSet() {
        cheeseQueueHead = 0;
        cheeseQueueSize = 0;
        int n = Math.min(globalCheeseMines.set.length(), CHEESE_QUEUE_MAX);
        for (int i = 0; i < n; i++) {
            cheeseQueue[i] = globalCheeseMines.at(i);
        }
        cheeseQueueSize = n;
    }

    /**
     * Clears the per-robot map chunk queue/state (used when a baby converts to king).
     */
    public static void resetMapChunkQueue() {
        mapChunkQueueHead = 0;
        mapChunkQueueSize = 0;
        mapChunkSeenMask = 0;
    }

    private static int readChunkPayload(int slotBase) throws GameActionException {
        int a = rc.readSharedArray(slotBase);
        int b = rc.readSharedArray(slotBase + 1);
        int c = rc.readSharedArray(slotBase + 2);
        int value = a | (b << 10) | (c << 20);
        if ((value & MAP_CHUNK_EMPTY_FLAG) != 0) return -1;
        return value & MAP_CHUNK_MASK;
    }

    private static void writeChunkPayload(int slotBase, int payload) throws GameActionException {
        int value = (payload < 0) ? MAP_CHUNK_EMPTY_FLAG : (payload & MAP_CHUNK_MASK);
        writeSharedArray(slotBase, value & 1023);
        writeSharedArray(slotBase + 1, (value >>> 10) & 1023);
        writeSharedArray(slotBase + 2, (value >>> 20) & 1023);
    }

    private static void readMapChunks() throws GameActionException {
        int payload0 = readChunkPayload(MAP_CHUNK_START);
        if (payload0 != -1) {
            MapManager.applyGlobalChunk(payload0);
        }
        int payload1 = readChunkPayload(MAP_CHUNK_START + MAP_CHUNK_SLOTS_PER_CHUNK);
        if (payload1 != -1) {
            MapManager.applyGlobalChunk(payload1);
        }
    }

    private static int queueAt(int offset) {
        return mapChunkQueue[(mapChunkQueueHead + offset) & MAP_CHUNK_QUEUE_MASK];
    }

    private static void rotateQueueOnce() {
        if (mapChunkQueueSize <= 1) return;
        int front = mapChunkQueue[mapChunkQueueHead];
        mapChunkQueueHead = (mapChunkQueueHead + 1) & MAP_CHUNK_QUEUE_MASK;
        int tailIndex = (mapChunkQueueHead + mapChunkQueueSize - 1) & MAP_CHUNK_QUEUE_MASK;
        mapChunkQueue[tailIndex] = front;
    }

    private static int cheeseQueueAt(int offset) {
        return cheeseQueue[(cheeseQueueHead + offset) & CHEESE_QUEUE_MASK];
    }

    private static void rotateCheeseQueueOnce() {
        if (cheeseQueueSize <= 1) return;
        int front = cheeseQueue[cheeseQueueHead];
        cheeseQueueHead = (cheeseQueueHead + 1) & CHEESE_QUEUE_MASK;
        int tailIndex = (cheeseQueueHead + cheeseQueueSize - 1) & CHEESE_QUEUE_MASK;
        cheeseQueue[tailIndex] = front;
    }

    private static int payloadForChunkIndex(int chunkIndex) {
        if (chunkIndex < 0 || MapManager.globalTracker == null) return -1;
        int chunkX = chunkIndex & 0b11;
        int chunkY = chunkIndex >>> 2;
        int mask = MapManager.globalTracker.getChunkMask(chunkX, chunkY);
        return MapTracker.encodeChunk(chunkX, chunkY, mask);
    }

    private static void writeMapChunks() throws GameActionException {
        int first = (mapChunkQueueSize >= 1) ? queueAt(0) : -1;
        int second = (mapChunkQueueSize >= 2) ? queueAt(1) : -1;

        writeChunkPayload(MAP_CHUNK_START, payloadForChunkIndex(first));
        writeChunkPayload(MAP_CHUNK_START + MAP_CHUNK_SLOTS_PER_CHUNK, payloadForChunkIndex(second));

        if (mapChunkQueueSize >= 1) rotateQueueOnce();
        if (mapChunkQueueSize >= 2) rotateQueueOnce();
    }

    private static void updateRatKingTurnCounters() throws GameActionException {
        // check living
        int x = rc.readSharedArray(RAT_KING_TURN_COUNTER);
        for (int j = 0; j < 5; j++, x >>= 2) {
            int t = (x & 0b11);
            if (t == (time + 1) % 3) {
                if (j == kingId) {
                    rc.setTimelineMarker("RAT KING THOUGHT THEY DIED", 255, 0, 0);
                }
                // rc.setTimelineMarker("RAT KING " + j + " DIED", 255, 0, 0);
                // println("RAT KING " + j + " DIED");
                writeSharedArray(RAT_KING_TURN_COUNTER, rc.readSharedArray(RAT_KING_TURN_COUNTER) | (0b11 << (2 * j)));
                writeSharedArray(RAT_KING_LOC + j, 1023);
            }
        }
    }

    public static void findNearestRatKing() {
        nearestRatKing = null;
        for (int i = 0; i < 5; i++) {
            MapLocation king = getRatKing(i);
            if (king == null) continue;
            if (nearestRatKing == null || nearestRatKing.distanceSquaredTo(myLoc) > king.distanceSquaredTo(myLoc)) {
                nearestRatKing = king;
                nearestRatKingId = i;
            }
        }// println
    }

    public static int countRatKings() {
        int ret = 0;
        for (int i = 0; i < 5; i++) {
            MapLocation king = getRatKing(i);
            if (king == null) continue;
            ret++;
        }
        return ret;
    }

    public static int getIndexAmongAliveRatKings() {
        int ret = 0;
        for (int i = 0; i < kingId; i++) {
            MapLocation king = getRatKing(i);
            if (king == null) continue;
            ret++;
        }
        return ret;
    }

    public static void registerGlobalCheeseMine(int loc) {
        if (loc == 0) {
            throw new RuntimeException("attempted to register invalid cheese mine at loc 0");
        }
        if (globalCheeseMines.contains(loc)) return;
        globalCheeseMines.add(loc);
        knownCheeseMines.add(loc);
        if (!isRatKing) return;
        cheeseQueueHead = (cheeseQueueHead - 1) & CHEESE_QUEUE_MASK;
        cheeseQueue[cheeseQueueHead] = loc;
        if (cheeseQueueSize < CHEESE_QUEUE_MAX) {
            cheeseQueueSize++;
        }
    }

    public static void readCheeseMines() throws GameActionException {
        int i = N - 1;
        while (i >= MIN_CHEESE) {
            int x = rc.readSharedArray(i);
            if (x == 1023) break;
            registerGlobalCheeseMine(x);
            i--;
        }
    }

    public static void writeCheeseMines() throws GameActionException {
        int n = cheeseQueueSize;
        int slots = N - MIN_CHEESE;
        if (n > 0) {
            int count = Math.min(n, slots);
            for (int j = 0; j < count; j++) {
                writeSharedArray(N - 1 - j, cheeseQueueAt(j));
            }
            for (int j = 0; j < count; j++) {
                rotateCheeseQueueOnce();
            }
        }
        if (n < slots) {
            writeSharedArray(N - 1 - n, 1023);
        } else {
            assert(false);
        }
    }

    public static boolean seenCheeseMine(int loc) {
        return globalCheeseMines.contains(loc);
    }

    private static int cur_sym = 0;
    public static void checkSymmetry() throws GameActionException {
        if (cur_sym != 0) return;
        cur_sym = rc.readSharedArray(SYMMETRY);
        if (cur_sym != 0) {
            CellTracker.setSymmetry(cur_sym);
        }
    }

    public static MapLocation getRatKing(int i) {
        try {
            int pos = rc.readSharedArray(i + RAT_KING_LOC);
            if (pos == 1023) return null;
            int lsbs = rc.readSharedArray(RAT_KING_LSBS);
            int myLsb = (lsbs >>> (2 * i)) & 0b11;
            int x = ((pos & 0b11111) << 1) | (myLsb & 1);
            int y = ((pos >>> 5) << 1) | ((myLsb >>> 1) & 1);
            return new MapLocation(x, y);
        } catch (GameActionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static CellTracker.Symmetry peepSymmetry() {
        return CellTracker.Symmetry.values()[cur_sym];
    }

    public static void markKingAlive() throws GameActionException {
        // cycle counter
        int x = rc.readSharedArray(RAT_KING_TURN_COUNTER);
        int t = time % 3;
        x &= ~(0b11 << (2 * kingId));
        x |= t << (2 * kingId);
        writeSharedArray(RAT_KING_TURN_COUNTER, x);
    }

    public static void writeMyLoc() throws GameActionException {
        // println("wrote my loc as rat king: " + myLoc);
        writeSharedArray(kingId + RAT_KING_LOC, encode2x2());
        // write LSBs for exact position
        int lsbs = rc.readSharedArray(RAT_KING_LSBS);
        int myLsb = (myLocX & 1) | ((myLocY & 1) << 1);
        lsbs &= ~(0b11 << (2 * kingId));
        lsbs |= myLsb << (2 * kingId);
        writeSharedArray(RAT_KING_LSBS, lsbs);
    }

    public static int findRatKingSlot() throws GameActionException {
        int x = rc.readSharedArray(RAT_KING_TURN_COUNTER);
        int i = 0;
        while (i < 5) {
            if ((x & 0b11) == 0b11) {
                return i;
            }
            x >>= 2;
            i++;
        }
        return i;
    }

    // FORMATION

    public static void writeFormationTarget(MapLocation aTarget, MapLocation aMine, int aCoordinatorKingId) throws GameActionException {
        int[] targetEncoded = encodeExact(aTarget);
        int[] mineEncoded = encodeExact(aMine);
        writeSharedArray(FORMATION_TARGET, targetEncoded[0]);
        writeSharedArray(FORMATION_TARGET + 1, targetEncoded[1]);
        writeSharedArray(FORMATION_MINE, mineEncoded[0]);
        writeSharedArray(FORMATION_MINE + 1, mineEncoded[1]);
        writeSharedArray(FORMATION_COORDINATOR, aCoordinatorKingId);
    }

    public static MapLocation getFormationTarget() throws GameActionException {
        int slot1 = rc.readSharedArray(FORMATION_TARGET);
        int slot2 = rc.readSharedArray(FORMATION_TARGET + 1);
        return decodeExact(slot1, slot2);
    }

    public static MapLocation getFormationMine() throws GameActionException {
        int slot1 = rc.readSharedArray(FORMATION_MINE);
        int slot2 = rc.readSharedArray(FORMATION_MINE + 1);
        return decodeExact(slot1, slot2);
    }

    public static int getFormationCoordinator() throws GameActionException {
        return rc.readSharedArray(FORMATION_COORDINATOR);
    }

    public static boolean tryClaimFormationCoordinator(int aKingId) throws GameActionException {
        int current = rc.readSharedArray(FORMATION_COORDINATOR);
        if (current != 0) return false; 
        writeSharedArray(FORMATION_COORDINATOR, aKingId);
        return rc.readSharedArray(FORMATION_COORDINATOR) == aKingId;
    }

    public static void clearFormation() throws GameActionException {
        writeSharedArray(FORMATION_COORDINATOR, 0);
        writeSharedArray(FORMATION_TARGET, 0);
        writeSharedArray(FORMATION_TARGET + 1, 0);
        writeSharedArray(FORMATION_MINE, 0);
        writeSharedArray(FORMATION_MINE + 1, 0);
    }

    private static int[] encodeExact(MapLocation aLoc) {
        if (aLoc == null) return new int[] {0, 0};
        return new int[] {aLoc.x + 1, aLoc.y + 1};
    }

    private static MapLocation decodeExact(int aSlot1, int aSlot2) {
        if (aSlot1 == 0 && aSlot2 == 0) return null;
        return new MapLocation(aSlot1 - 1, aSlot2 - 1);
    }

    public static void writeKingMineAssignment(int aKingId, MapLocation aMine) throws GameActionException {
        if (aKingId >= 0 && aKingId < 5) {
            writeSharedArray(KING_MINE_ASSIGNMENT + aKingId, aMine != null ? encode2x2(aMine) + 1 : 0);
        }
    }

    public static MapLocation getKingMineAssignment(int aKingId) throws GameActionException {
        if (aKingId >= 0 && aKingId < 5) {
            int myEncoded = rc.readSharedArray(KING_MINE_ASSIGNMENT + aKingId);
            if (myEncoded == 0) return null;
            return decode2x2(myEncoded - 1);
        }
        return null;
    }

    public static boolean isMineAssignedToAnyKing(MapLocation aMine) throws GameActionException {
        if (aMine == null) return false;
        for (int i = 0; i < 5; i++) {
            MapLocation myKingMine = getKingMineAssignment(i);
            if (myKingMine != null && myKingMine.distanceSquaredTo(aMine) < 25) {
                return true;
            }
        }
        return false;
    }
}
