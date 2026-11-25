package fish;

/**
 * Where an outlined helper (see Outliner) leaves the locals and the return value its caller needs. All robots run
 * on one thread, and the helper writes here just before returning while its caller reads just after the call, with
 * no instrumented code in between, so no robot can pause and let another robot overwrite a value.
 */
public final class Spill {
    private Spill() {
    }

    private static final int[] I = new int[256];
    private static final long[] J = new long[256];
    private static final float[] F = new float[256];
    private static final double[] D = new double[256];
    private static final Object[] A = new Object[256];
    private static int ri;
    private static long rj;
    private static float rf;
    private static double rd;
    private static Object ra;

    public static void sI(int v, int k) { I[k] = v; }
    public static void sJ(long v, int k) { J[k] = v; }
    public static void sF(float v, int k) { F[k] = v; }
    public static void sD(double v, int k) { D[k] = v; }
    public static void sA(Object v, int k) { A[k] = v; }
    public static int gI(int k) { return I[k]; }
    public static long gJ(int k) { return J[k]; }
    public static float gF(int k) { return F[k]; }
    public static double gD(int k) { return D[k]; }
    public static Object gA(int k) { Object v = A[k]; A[k] = null; return v; }

    public static void rI(int v) { ri = v; }
    public static void rJ(long v) { rj = v; }
    public static void rF(float v) { rf = v; }
    public static void rD(double v) { rd = v; }
    public static void rA(Object v) { ra = v; }
    public static int rrI() { return ri; }
    public static long rrJ() { return rj; }
    public static float rrF() { return rf; }
    public static double rrD() { return rd; }
    public static Object rrA() { Object v = ra; ra = null; return v; }
}
