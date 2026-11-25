package awubot.comms;

import static awubot.Game.*;
import static awubot.util.Util.*;

/**
 * Baby-rat map manager that keeps a global (shared) tracker and a local tracker
 * that includes personal vision. The global tracker is currently empty because
 * rat kings are not yet publishing map chunks.
 */
public class MapManager {
    // Global knowledge (from shared comms) and local knowledge (global + vision).
    public static MapTracker globalTracker;
    public static MapTracker localTracker;

    // Cached unit bounds for printing and bounds checks.
    private static int maxUnitX;
    private static int maxUnitY;

    /**
     * Initializes trackers for baby rats.
     * The local tracker tracks unseen units; the global tracker does not.
     */
    public static void init() {
        maxUnitX = (mapWidth - MapTracker.UNIT_SIZE) / MapTracker.UNIT_SIZE;
        maxUnitY = (mapHeight - MapTracker.UNIT_SIZE) / MapTracker.UNIT_SIZE;

        if (isRatKing) {
            // Kings still need trackers for map comms.
            localTracker = new MapTracker(false);
            globalTracker = localTracker;
            return;
        }

        globalTracker = new MapTracker(false);
        localTracker = new MapTracker(true);
    }

    /**
     * Per-turn update for baby rats. Incorporates vision into the local tracker
     * and prints the local seen-map at round 50.
     */
    public static void preTick() {
        // if (time % 50 == 0) {
        //     printLocalSeenUnits();
        //     printGlobalSeenUnits();
        // }
        if (localTracker == null) return;

        localTracker.updateVisibleUnits();
        if (isRatKing && localTracker.consumeNewInfo()) {
            enqueueSeenChunks();
        }
    }

    /**
     * Call when a baby rat converts to king so map comms keep working.
     * We reuse the baby's local tracker as the global source and seed the queue.
     */
    public static void onConvertKing() {
        if (localTracker == null) {
            localTracker = new MapTracker(false);
        }
        globalTracker = localTracker;
        GlobalArray.resetMapChunkQueue();
        enqueueSeenChunks();
    }

    /**
     * Applies a global chunk update (when comms exist) and mirrors it into local.
     * @return true if new info was added to the global tracker.
     */
    public static boolean applyGlobalChunk(int encodedChunk) {
        if (globalTracker == null || localTracker == null) return false;
        GlobalArray.noteMapChunkSeen(encodedChunk);
        boolean changed = globalTracker.applyEncodedChunk(encodedChunk);
        if (changed) {
            localTracker.applyEncodedChunk(encodedChunk);
        }
        return changed;
    }

    /**
     * Finds a chunk that differs between local and global for sending to a king.
     * @return encoded chunk payload, or -1 if no differences exist.
     */
    public static int getLocalDiffChunk() {
        if (globalTracker == null || localTracker == null) return -1;
        return MapTracker.findDiffChunk(localTracker, globalTracker);
    }// println

    /**
     * Pushes all seen chunk indices into the global queue (idempotent).
     */
    private static void enqueueSeenChunks() {
        if (!isRatKing || globalTracker == null) return;
        for (int cy = 0; cy < MapTracker.CHUNKS_PER_SIDE; cy++) {
            for (int cx = 0; cx < MapTracker.CHUNKS_PER_SIDE; cx++) {
                int mask = globalTracker.getChunkMask(cx, cy);
                if (mask != 0) {
                    GlobalArray.noteMapChunkSeen(MapTracker.encodeChunk(cx, cy, mask));
                }
            }
        }
    }
}
