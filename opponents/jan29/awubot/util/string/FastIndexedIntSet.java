package awubot.util.string;

// inline for better performance
public class FastIndexedIntSet {
    public StringBuilder set;

    public FastIndexedIntSet() {
        set = new StringBuilder();
    }

    public void add(int i) {
        String key = String.valueOf((char) i);
        if (set.indexOf(key) < 0) {
            set.append(key);
        }
    }

    public void unsafe_add(int i) {
        set.append(String.valueOf((char) i));
    }

    public boolean add_and_ret(int i) {
        String key = String.valueOf((char) i);
        if (set.indexOf(key) < 0) {
            set.append(key);
            return true;
        }
        return false;
    }

    public void remove(int i) {
        String key = String.valueOf((char) i);
        int index;
        if ((index = set.indexOf(key)) >= 0) {
            set.deleteCharAt(index);
        }
    }

    public boolean contains(int i) {
        return set.indexOf(String.valueOf((char) i)) >= 0;
    }

    public int at(int i) {
        return (int) set.charAt(i);
    }

    public void clear() {
        set = new StringBuilder();
    }

    public int size() {
        return set.length();
    }
}
