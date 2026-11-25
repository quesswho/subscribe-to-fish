package awubot.util.string;

// only insert and query
// if you actually want performance, inline this class
public class FastIntSet {
    public StringBuilder set;

    public FastIntSet() {
        set = new StringBuilder("|");
    }

    public void add(int i) {
        set.append(i + "|");
    }

    public boolean add_and_ret(int i) {
        if (set.indexOf("|" + i + "|") < 0) {
            set.append(i + "|");
            return true;
        }
        return false;
    }

    public boolean contains(int i) {
        return set.indexOf("|" + i + "|") >= 0;
    }
}
