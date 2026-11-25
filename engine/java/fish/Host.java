package fish;

import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.Team;
import battlecode.crossplay.CrossPlayLanguage;
import battlecode.instrumenter.TeamClassLoaderFactory;
import battlecode.server.Config;
import battlecode.world.control.PlayerControlProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * The JVM half of the fish engine. The C++ engine calls these static methods through JNI; bots call back into it
 * through NativeRC. Instrumented classes are cached per bot folder for the life of the process, so only the first
 * game of a worker pays for instrumentation.
 */
public final class Host {
    private Host() {
    }

    // ---- exceptions ----
    private static GameActionExceptionType[] errorTypes;
    private static String[] errorMessages;
    static int CANT_SENSE_ROBOT, ATTACK_NULL, INDICATOR_DOT_OFF_MAP, INDICATOR_LINE_OFF_MAP;
    private static int nullArgument, cheeseError, engineError;

    static boolean isUnchecked(int code) {
        return code == nullArgument || code == cheeseError || code == engineError;
    }

    static void raiseUnchecked(int code) {
        if (code == nullArgument) throw new NullPointerException("Argument has an invalid null value");
        if (code == cheeseError) throw new IllegalArgumentException("Invalid cheese change");
        throw new RuntimeException(NativeRC.nEngineError());
    }

    static void raise(int code) throws GameActionException {
        if (code == nullArgument) throw new NullPointerException("Argument has an invalid null value");
        if (code == cheeseError) throw new IllegalArgumentException("Invalid cheese change");
        if (code == engineError) throw new RuntimeException(NativeRC.nEngineError());
        throw new GameActionException(errorTypes[code], errorMessages[code]);
    }

    /** table[i] is "NAME|TYPE|message" for error code i. */
    public static void init(String[] table) {
        errorTypes = new GameActionExceptionType[table.length];
        errorMessages = new String[table.length];
        Map<String, Integer> byName = new HashMap<>();
        for (int i = 0; i < table.length; i++) {
            String[] parts = table[i].split("\\|", 3);
            byName.put(parts[0], i);
            errorTypes[i] = GameActionExceptionType.valueOf(parts[1]);
            errorMessages[i] = parts[2];
        }
        CANT_SENSE_ROBOT = byName.get("CANT_SENSE_ROBOT");
        ATTACK_NULL = byName.get("ATTACK_NULL");
        INDICATOR_DOT_OFF_MAP = byName.get("INDICATOR_DOT_OFF_MAP");
        INDICATOR_LINE_OFF_MAP = byName.get("INDICATOR_LINE_OFF_MAP");
        nullArgument = byName.get("NULL_ARGUMENT");
        cheeseError = byName.get("CHEESE_ERROR");
        engineError = byName.get("ENGINE_ERROR");

        Config config = new Config(new String[] {"-c=-"});
        config.set("bc.engine.debug-methods", "false");
        Config.setGlobalConfig(config);
        NativeRC.allocate();
        String profile = System.getenv("FISH_PROFILE");
        if (profile != null) Profiler.start(Thread.currentThread(), profile);
    }

    public static java.nio.ByteBuffer buffer() {
        return NativeRC.rawBuf;
    }

    // ---- teams ----
    private static final Map<String, TeamClassLoaderFactory> factories = new HashMap<>();
    private static final Map<String, SharedLoader> sharedLoaders = new HashMap<>();
    private static final boolean ISOLATED = System.getenv("FISH_ISOLATED") != null;
    private static final Sandbox.Team[] teams = new Sandbox.Team[2];

    private static final class SilentStream extends OutputStream {
        @Override public void write(int b) {}
        @Override public void write(byte[] b, int off, int len) {}
    }

    /** Elapsed bot time as Clock.getTimeElapsed reports it. */
    private static final class TimeProvider extends PlayerControlProvider {
        Sandbox.Team team;

        TimeProvider(Team t, String pkg, String url) {
            super(t, pkg, CrossPlayLanguage.JAVA, null, url, new SilentStream(), false);
        }

        @Override public long getTimeElapsed() { return team.timeNanos; }

        @Override public long getTimeLeft() {
            return Math.max(battlecode.common.GameConstants.MAX_TEAM_EXECUTION_TIME - team.timeNanos, 0L);
        }
    }

    /**
     * Starts a game. outPath "" discards bot output; otherwise each team's System.out goes to that file with
     * RoboPrintStream's "[A: #id@round] " headers.
     */
    public static void beginGame(int width, int height, String dirA, String pkgA, String dirB, String pkgB,
                                 String outA, String outB, String props) throws Exception {
        setProps(props);
        NativeRC.resetGame(width, height);
        sandboxes = new Sandbox[1 << 16];
        Sandbox.pinnedPauses = 0;
        teams[0] = makeTeam(Team.A, dirA, pkgA, outA);
        teams[1] = makeTeam(Team.B, dirB, pkgB, outB);
    }

    /** The game's bc.testing properties ("A.f_dmg=12;B.x=1"), which bots read with System.getProperty (Battlecode
     *  copies bc.testing.* into the sandbox): tuning a constant needs no recompile. A previous game's keys are
     *  blanked, and bots treat an empty value as unset. */
    private static final java.util.Set<String> propKeys = new java.util.HashSet<>();

    private static void setProps(String props) {
        Config config = Config.getGlobalConfig();
        for (String k : propKeys) config.set(k, "");
        propKeys.clear();
        if (props == null || props.isEmpty()) return;
        for (String kv : props.split(";")) {
            int eq = kv.indexOf('=');
            if (eq <= 0) continue;
            String key = "bc.testing." + kv.substring(0, eq);
            config.set(key, kv.substring(eq + 1));
            propKeys.add(key);
        }
    }

    private static Sandbox.Team makeTeam(Team t, String dir, String pkg, String outPath) throws Exception {
        String url = new File(dir).getAbsoluteFile().toURI().toURL().toString();
        TeamClassLoaderFactory factory = factories.computeIfAbsent(url, TeamClassLoaderFactory::new);
        if (factory.getError()) throw new IllegalStateException("cannot load bot classes from " + dir);
        TimeProvider provider = new TimeProvider(t, pkg, url);
        PrintStream out = outPath.isEmpty() ? new PrintStream(new SilentStream())
                : new BotOut(new FileOutputStream(outPath, true), t);
        SharedLoader shared = null;
        if (!ISOLATED && SharedSafety.check(new File(dir), pkg))
            shared = sharedLoaders.computeIfAbsent(url, u -> new SharedLoader(factory, new File(dir)));
        Sandbox.Team team = new Sandbox.Team(factory, shared, pkg, provider, out);
        provider.team = team;
        return team;
    }

    public static void endGame() {
        for (Sandbox.Team t : teams)
            if (t != null) t.out.flush();
        sandboxes = null;
        teams[0] = teams[1] = null;
    }

    // ---- robots ----
    private static Sandbox[] sandboxes;

    public static void spawn(long handle, int id, int team) throws Throwable {
        if (id >= sandboxes.length) sandboxes = java.util.Arrays.copyOf(sandboxes, id * 2);
        sandboxes[id] = new Sandbox(teams[team], new NativeRC(handle, id, team));
    }

    /** Runs one turn: returns bytecodes used, plus 1 << 32 if the player has terminated. */
    public static long step(int id, int bytecodeLimit, int round) throws Throwable {
        Sandbox s = sandboxes[id];
        if (s == null) return 1L << 32;
        if (s.team.out instanceof BotOut b) b.header(id, round);
        int used = s.step(bytecodeLimit);
        return (s.terminated ? 1L << 32 : 0) | (used & 0xffffffffL);
    }

    public static void killed(int id) throws Throwable {
        Sandbox s = sandboxes[id];
        if (s != null) s.kill();
        sandboxes[id] = null;
    }

    public static long teamTimeNanos(int team) {
        return teams[team].timeNanos;
    }

    public static int teamExceptions(int team) {
        return teams[team].uncaughtExceptions;
    }

    public static long pinnedPauses() {
        return Sandbox.pinnedPauses;
    }
}
