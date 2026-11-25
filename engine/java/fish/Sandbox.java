package fish;

import battlecode.common.RobotController;
import battlecode.instrumenter.RobotDeathException;
import battlecode.instrumenter.SandboxedRobotPlayer;
import battlecode.instrumenter.TeamClassLoaderFactory;
import battlecode.world.control.PlayerControlProvider;
import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;

import java.io.PrintStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * One robot's player, as battlecode.instrumenter.SandboxedRobotPlayer runs it, but on a continuation instead of a
 * thread: a pause (Clock.yield or running out of bytecodes) yields the continuation instead of blocking a thread.
 *
 * Isolated mode gives the robot its own instrumented copy of every class, as the official engine does. Shared mode
 * uses the team's SharedLoader: one copy of each class, with the robot's static fields in its own Ctx.
 */
final class Sandbox {
    static final ContinuationScope SCOPE = new ContinuationScope("battlecode");

    private static final MethodType VOID_INT = MethodType.methodType(void.class, int.class);
    private static final MethodType INT = MethodType.methodType(int.class);
    private static final MethodType VOID = MethodType.methodType(void.class);

    final int id;
    final Team team;
    final NativeRC rc;
    private final Ctx ctx;
    private final MethodHandle setBytecodeLimit, getBytecodeNum, killRobot;
    private final Continuation continuation;
    boolean running;
    boolean terminated;

    /** Pauses that could not yield (inside a static initialiser in isolated mode): they run on into the turn. */
    static long pinnedPauses;

    static final class Team {
        final TeamClassLoaderFactory factory;
        final SharedLoader shared;  // null in isolated mode
        final String packageName;
        final PlayerControlProvider provider;
        final PrintStream out;
        long timeNanos;
        int uncaughtExceptions;

        Team(TeamClassLoaderFactory factory, SharedLoader shared, String packageName, PlayerControlProvider provider,
             PrintStream out) {
            this.factory = factory;
            this.shared = shared;
            this.packageName = packageName;
            this.provider = provider;
            this.out = out;
        }
    }

    Sandbox(Team team, NativeRC rc) throws Throwable {
        this.id = rc.getID();
        this.team = team;
        this.rc = rc;
        this.ctx = team.shared != null ? new Ctx(team.shared) : null;
        ClassLoader loader = team.shared != null ? team.shared : team.factory.createLoader(false);
        Ctx prev = enter();
        try {
            if (ctx != null)
                for (String eager : SharedLoader.EAGER)
                    loader.loadClass(eager.replace('/', '.') + SharedLoader.HOLDER).getMethod("c").invoke(null);
            Class<?> monitor = loader.loadClass("battlecode.instrumenter.inject.RobotMonitor");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            setBytecodeLimit = lookup.findStatic(monitor, "setBytecodeLimit", VOID_INT);
            getBytecodeNum = lookup.findStatic(monitor, "getBytecodeNum", INT);
            killRobot = lookup.findStatic(monitor, "killRobot", VOID);
            MethodHandle reactivate = lookup.findStatic(monitor, "reactivate", VOID);

            SandboxedRobotPlayer.Pauser pauser = () -> {
                if (!Continuation.yield(SCOPE)) pinnedPauses++;
            };
            SandboxedRobotPlayer.Killer killer = () -> {
                throw new RobotDeathException();
            };
            Method init = monitor.getMethod("init", SandboxedRobotPlayer.Pauser.class, SandboxedRobotPlayer.Killer.class,
                    int.class, battlecode.instrumenter.profiler.Profiler.class, PlayerControlProvider.class);
            init.invoke(null, pauser, killer, id, null, team.provider);

            Class<?> system = loader.loadClass("battlecode.instrumenter.inject.System");
            system.getMethod("setSystemOut", PrintStream.class).invoke(null, team.out);

            continuation = new Continuation(SCOPE, () -> body(loader, reactivate)) {
                @Override
                protected void onPinned(Continuation.Pinned reason) {
                }
            };
        } finally {
            leave(prev);
        }
    }

    private Ctx enter() {
        Ctx prev = Ctx.cur;
        if (ctx != null) Ctx.switchTo(ctx);
        return prev;
    }

    private void leave(Ctx prev) {
        if (ctx != null) Ctx.switchTo(prev);
    }

    private void body(ClassLoader loader, MethodHandle reactivate) {
        try {
            // SandboxedRobotPlayer's first RobotMonitor.pause() returns here on the first step.
            reactivate.invokeExact();
            Class<?> player = loader.loadClass(team.packageName + ".RobotPlayer");
            Method run = player.getMethod("run", RobotController.class);
            if ((run.getModifiers() & Modifier.STATIC) == 0)
                throw new IllegalStateException("RobotPlayer.run(RobotController) is not static");
            // Reflection initialises RobotPlayer before run() starts; do the same for this robot's statics.
            if (team.shared != null)
                loader.loadClass(team.packageName + ".RobotPlayer$$S").getMethod("c").invoke(null);
            run.invoke(null, rc);
            team.out.println(rc.getTeam() + "'s " + id + " at location " + rc.getLocation() + " froze in round "
                    + rc.getRoundNum() + " because it returned from its run() method!");
        } catch (InvocationTargetException e) {
            if (!(e.getCause() instanceof RobotDeathException)) report(e.getCause());
        } catch (RobotDeathException e) {
            // killed
        } catch (Throwable e) {
            report(e);
        } finally {
            terminated = true;
        }
    }

    private void report(Throwable e) {
        team.uncaughtExceptions++;
        team.out.println("Stack trace: ");
        e.printStackTrace(team.out);
    }

    /** Runs one turn and returns the bytecodes it used. */
    int step(int bytecodeLimit) throws Throwable {
        // Statics stay swapped in after the turn: the next robot to run swaps them out.
        enter();
        if (terminated) return (int) getBytecodeNum.invokeExact();
        setBytecodeLimit.invokeExact(bytecodeLimit);
        running = true;
        long start = System.nanoTime();
        try {
            continuation.run();
        } finally {
            team.timeNanos += System.nanoTime() - start;
            running = false;
        }
        if (continuation.isDone()) terminated = true;
        return (int) getBytecodeNum.invokeExact();
    }

    /** PlayerControlProvider.robotKilled -> SandboxedRobotPlayer.terminate. */
    void kill() throws Throwable {
        if (terminated) return;
        Ctx prev = enter();
        try {
            killRobot.invokeExact();
        } finally {
            leave(prev);
        }
        // A paused player would only be resumed to throw RobotDeathException past bot code that cannot catch it,
        // so it is dropped instead. A running one dies at its next bytecode check, as in the official engine.
        if (!running) terminated = true;
    }
}
