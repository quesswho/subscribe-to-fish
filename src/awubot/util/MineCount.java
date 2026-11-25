package awubot.util;

import awubot.CellTracker;
import awubot.Game;
import awubot.util.string.FastIndexedIntSet;

import static awubot.Game.mapHeight;
import static awubot.Game.mapWidth;

/**
 * A lower bound on how many cheese mines the map holds. The map is symmetric, so the mirror of every known mine is a
 * mine too; under each symmetry still possible the count is the known mines plus the mirrors not yet known, and the
 * bound is the smallest of those. Known mines are never forgotten and symmetries are only ruled out, so the bound only
 * grows. Cached until the known mines or the symmetries change.
 */
public final class MineCount {
    private MineCount() {
    }

    private static int cachedMines = -1, cachedSyms = -1, cachedCap = -1, cachedValue;

    /** The bound, counted no higher than cap. */
    public static int atLeast(int cap) {
        FastIndexedIntSet mines = Game.knownCheeseMines;
        int n = mines.size();
        if (n >= cap) return cap;
        int syms = CellTracker.rsym | (CellTracker.hsym << 1) | (CellTracker.vsym << 2);
        if (n == cachedMines && syms == cachedSyms && cap == cachedCap) return cachedValue;
        int bound = n;
        if (syms != 0) {
            bound = cap;
            if (CellTracker.rsym == 1) bound = Math.min(bound, withMirrors(mines, n, cap, true, true));
            if (CellTracker.hsym == 1) bound = Math.min(bound, withMirrors(mines, n, cap, true, false));
            if (CellTracker.vsym == 1) bound = Math.min(bound, withMirrors(mines, n, cap, false, true));
        }
        cachedMines = n;
        cachedSyms = syms;
        cachedCap = cap;
        return cachedValue = bound;
    }

    /** Known mines plus their mirrors not yet known. A known mine is a 2x2 block; its mirror may straddle two blocks. */
    private static int withMirrors(FastIndexedIntSet mines, int n, int cap, boolean flipX, boolean flipY) {
        int count = n;
        for (int i = 0; i < n && count < cap; i++) {
            int block = mines.at(i);
            int x = (block & 31) << 1, y = (block >>> 5) << 1;
            int x0 = flipX ? mapWidth - 1 - x : x, x1 = flipX ? mapWidth - 2 - x : x + 1;
            int y0 = flipY ? mapHeight - 1 - y : y, y1 = flipY ? mapHeight - 2 - y : y + 1;
            if (known(mines, x0, y0) || known(mines, x1, y0) || known(mines, x0, y1) || known(mines, x1, y1)) continue;
            count++;
        }
        return count;
    }

    private static boolean known(FastIndexedIntSet mines, int x, int y) {
        return x >= 0 && y >= 0 && mines.contains(Util.encode2x2(x, y));
    }
}
