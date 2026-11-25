package awubot.util.string;

import battlecode.common.*;

// only insert and query
// if you actually want performance, inline this class
public class FastMapLocationSet {
    public StringBuilder set;

    public FastMapLocationSet() {
        set = new StringBuilder("|");
    }

    public void add(MapLocation loc) {
        set.append(loc.x + "," + loc.y + "|");
    }

    public boolean contains(MapLocation loc) {
        return set.indexOf("|" + loc.x + "," + loc.y + "|") >= 0;
    }
}
