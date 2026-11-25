package awubot.util;

public final class FastRng {
    private static long S = 0x9E3779B97F4A7C15L; 

    public static void seed(long seed) {
        S = (seed != 0L) ? seed : 0x9E3779B97F4A7C15L;
    }
  
    // 30 bytecode
    public static int nextInt() {
        long x = S;
        x ^= x >>> 12;
        x ^= x << 25;
        x ^= x >>> 27;
        S = x;
        return (int)(x * 2685821657736338717L);
    }

    public static long nextLong() {
        long x = S;
        x ^= x >>> 12;
        x ^= x << 25;
        x ^= x >>> 27;
        S = x;
        return x * 2685821657736338717L;
    }

    public static int nextInt(int bound) {
        int x = nextInt();
        if (x < 0) x = -x;
        return x % bound;
    }

    public static boolean nextBool() {
        return (nextInt() & 1) != 0;
    }

    private long s;

    public FastRng(long seed) {
        s = (seed != 0L) ? seed : 0x9E3779B97F4A7C15L;
    }

    public void reseed(long seed) {
        s = (seed != 0L) ? seed : 0x9E3779B97F4A7C15L;
    }

    public int nextIntI() {
        long x = s;
        x ^= x >>> 12;
        x ^= x << 25;
        x ^= x >>> 27;
        s = x;
        return (int)(x * 2685821657736338717L);
    }

    public long nextLongI() {
        long x = s;
        x ^= x >>> 12;
        x ^= x << 25;
        x ^= x >>> 27;
        s = x;
        return x * 2685821657736338717L;
    }

    public int nextIntI(int bound) {
        int x = nextIntI();
        if (x < 0) x = -x;
        return x % bound;
    }

    public boolean nextBoolI() {
        return (nextIntI() & 1) != 0;
    }
}
