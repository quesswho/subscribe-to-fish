package fish;

/**
 * The static state of one robot in shared-class mode. Bot code reads and writes plain static fields (moved to
 * X$$F classes by SharedLoader); this robot's values live in its holders while another robot runs, and switchTo
 * swaps them in and out. holders[i] is also the marker that class i has been initialised for this robot.
 */
public final class Ctx {
    /** The robot whose values are in the static fields. */
    public static Ctx cur;

    public Object[] holders = new Object[256];
    private final SharedLoader team;

    Ctx(SharedLoader team) {
        this.team = team;
    }

    /** Makes room for index i in the current robot's holder table. */
    public static Object[] grow(int i) {
        return grow(cur, i);
    }

    static void switchTo(Ctx next) {
        Ctx prev = cur;
        if (prev == next) return;
        if (prev != null) prev.save();
        if (next != null) next.load();
        cur = next;
    }

    private void save() {
        int[] idx = team.swapped;
        boolean[] lazy = team.lazy;
        for (int k = 0, n = team.swappedCount; k < n; k++) {
            int i = idx[k];
            Object[] h = holders;
            Object o = i < h.length ? h[i] : null;
            if (o == null) {
                // A class without a static initialiser has no initialisation to mark: this robot's values are
                // whatever its code left in the fields.
                if (!lazy[i]) continue;
                o = team.defaults[i].fresh();
                grow(this, i)[i] = o;
            }
            ((Statics) o).save();
        }
    }

    private static Object[] grow(Ctx c, int i) {
        if (i >= c.holders.length) c.holders = java.util.Arrays.copyOf(c.holders, Math.max(i + 1, 2 * c.holders.length));
        return c.holders;
    }

    private void load() {
        int[] idx = team.swapped;
        Statics[] defaults = team.defaults;
        Object[] h = holders;
        for (int k = 0, n = team.swappedCount; k < n; k++) {
            int i = idx[k];
            Object o = i < h.length ? h[i] : null;
            ((Statics) (o != null ? o : defaults[i])).load();
        }
    }
}
