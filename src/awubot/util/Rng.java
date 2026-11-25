package awubot.util;

import battlecode.common.*;
import static awubot.Game.*;

public class Rng {
    static int s0, s1, s2, s3;

    /**
     * Call this at the start of each round if you're using mcg, lcg or xorshift<br>
     * 33 bytecode
     */
    public static void init() {
        s0 *= 1103515245;
        s0 ^= rc.getRoundNum();
        s0 *= 1103515245;
        s0 ^= myLocX;
        s0 *= 1103515245;
        s0 ^= myLocY;
    }

    /**
     * Cheap xorshift* (better than LCG for low bits / modulo).
     * @return a pseudo-random integer in the range [0, 2^31 - 1]
     */
    public static int mcg() {
        int x = s0;
        x ^= x << 13;
        x ^= x >>> 17;
        x ^= x << 5;
        s0 = x;
        return (x * 0x9E3779B9) >>> 1;
    }

    /**
     * 11 bytecode
     * @return a pseudo-random integer in the range [0, 2^31 - 1]
     */
    public static int lcg() {
        s0 = s0 * 1103515245 + 12345;
        return s0 >>> 1;
    }

    /**
     * 23 bytecode
     * @return a pseudo-random integer in the range [0, 2^31 - 1]
     */
    public static int xorshift() {
        s0 ^= s0 << 13;
        s0 ^= s0 >>> 17;
        s0 ^= s0 << 5;
        return s0 >>> 1;
    }

    /**
     * 22 bytecode
     */
    public static void seed() {
        s0 = rc.getID() * 0x41C64E6D;
        s1 = time * 0x515E1229;
        s2 = myLocX * 0x5BCE2D95;
        s3 = myLocY * 0x79797977;
    }

    /**
     * Call this at the start of every round if you're using xss
     * 22 bytecode
     */
    public static void init_xss() {
        s0 ^= Integer.rotateLeft(myLocX * 0x9E3779B9, 13);
        s2 ^= Integer.rotateLeft(myLocY * 0x517cc1b7, 7);
    }

    /**
     * 32 bytecode
     * @return a pseudo-random integer in the range [0, 2^31 - 1]
     */
    public static int xss() {
        int t = s1 << 9;
        s2 ^= s0;
        s3 ^= s1;
        s1 ^= s2;
        s0 ^= s3;
        s2 ^= t;
        s3 = Integer.rotateLeft(s3, 11);

        return s0>>>1;
    }
}
