package awubot.util.string;

public class FastIntIntMap {
    public static final int OFF = 65536 / 2;
    public StringBuilder keyVals;

    public FastIntIntMap() {
        keyVals = new StringBuilder();
    }

    public void add(int key, int val) {
        String keyString = String.valueOf((char) key);
        if (keyVals.indexOf(keyString) == -1) {
            keyVals.append(keyString + (char) (val + OFF));
        }
    }

    public void remove(int key) {
        String keyTemp = "^" + (char) (key + 0x100);
        int index;
        if ((index = keyVals.indexOf(keyTemp)) >= 0) {
            keyVals.delete(index, index + 3);
        }
    }

    public void addReplace(int key, int val) {
        // String keyTemp = keyToStr(key);
        String keyTemp = "^" + (char) (key + 0x100);
        int index = keyVals.indexOf(keyTemp);
        switch (index) {
            case -1:
                keyVals.append(keyTemp + (char) (val + 0x100));
                break;
            default:
                keyVals.setCharAt(index + 2, (char) (val + 0x100));
                break;
        }
    }

    public boolean contains(int key) {
        // return keyVals.indexOf(keyToStr(key)) >= 0;
        return keyVals.indexOf("^" + (char) (key + 0x100)) >= 0;
    }

    public void clear() {
        keyVals =    new StringBuilder();
    }

    public int getVal(int key) {
        String keyTemp = "^" + (char) (key + 0x100);
        // String keyTemp = keyToStr(key);
        int idx = keyVals.indexOf(keyTemp);
        if (idx != -1) {
            return (int) keyVals.charAt(idx + 2) - 0x100;
        }

        // Could not find element
        return -1;
    }
}