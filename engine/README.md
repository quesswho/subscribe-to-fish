# fish, the engine

A reimplementation of the Battlecode 2026 game engine (official engine 1.2.6) for testing: it must play move for
move like the official engine, so a result on fish is a result on the real game. Replays, the client protocol
and profiling are left to the official engine (`tools/replay`).

## Layout

    src/game/     the rules, no JVM: types, java.util.Random, the .map26 reader, World (GameWorld +
                  InternalRobot + RobotControllerImpl), cats included
    src/host/     the JVM: creates it, registers NativeRC's natives, drives the Java sandboxes
    src/main.cpp  fish run / fish worker / fish validate
    java/fish/    the Java half: Host (entry points), Sandbox (a robot), NativeRC (RobotController over JNI),
                  SharedLoader + Ctx + Statics (shared classes), Profiler
    ref/          RefTrace: the official engine, printing the same per-round state hash (tools/verify)

Build with `make -C engine` (`tools/build` also writes `build/engine-classpath.txt` from Gradle). Needs JDK 21:
the official engine runs on Java 21, and the instrumented copies of `java.util` it hands to bots, so their
bytecode costs, come from the running JDK.

## Matching the official engine

- `World` follows the Java method by method, in the same order, and keeps the official engine's oddities on
  purpose: grid cells cleared by whichever robot is destroyed there, `(int) 1.5` cooldown multipliers, the cat
  locking onto a stale RobotInfo location, numBabyRats not dropping when a rat becomes king, rat-trap stats from
  `TrapType` (not the spec), and so on. Unchecked exceptions inside the engine end the game as `runRound` does;
  inside a bot's call they reach the bot.
- Iteration orders that decide outcomes (sensing, squeak delivery, trap triggers, execution order, part locations
  by chirality) are reproduced exactly. RNGs: the game's `Random(seed)`, the IDGenerator's shuffled blocks, and the
  cats' `Random(1092)` (static in the official engine, so fresh per game when one game runs per JVM).
- Floating point: built with `-ffp-contract=off` so no fused multiply-add changes a vision cone or a cheese roll.
- `setWinnerArbitrary` uses `Math.random()` in the official engine; fish draws it from the game seed, so ties
  broken by a coin flip can differ between the engines (and rarely matter).
- `--seed` overrides the map's seed, like the team's `src/battlecode` overrides do for the Gradle tasks.

## Bots

The official instrumenter (TeamClassLoaderFactory, InstrumentingMethodVisitor, RobotMonitor) instruments the bot,
so bytecode counts, costs of API methods, the exception penalty and `Clock` behave the same.

- A robot runs on a `jdk.internal.vm.Continuation`: `RobotMonitor.pause()` (Clock.yield, or bytecodes running
  out) yields it, the next turn resumes it. No threads, no handoffs.
- RobotController is `NativeRC`: each call is one JNI call into `World`; results come back through a direct
  buffer and are turned into the same objects (RobotInfo is cached per robot as `getRobotInfo` does). The shared
  arrays are mirrored in that buffer, so reading them costs no JNI call.
- **Shared classes** (default): one class loader per bot, not per robot. `SharedLoader` instruments exactly as
  the official loader does, then rewrites each robot class X: its non-constant static fields move to `X$$F`
  (same instruction size, so the 64 KB method limit is safe), its static initialiser becomes a plain method, and
  `X$$S.c()` initialises X for the running robot where the JVM would (new, get/putstatic, invokestatic,
  superclass first), skipped where an earlier call on every path already did it (a dataflow pass). `Ctx.switchTo`
  swaps every class's statics with the running robot's holders when a different robot runs. Bytecode counts are
  compiled in before the rewrite, so nothing a bot can observe changes. A bot that uses `Enum.valueOf`,
  `EnumMap` or `EnumSet` runs isolated instead (the JDK caches enum constants per Class).
- `FISH_ISOLATED=1`: a class loader per robot, as the official engine does.
- `FISH_CLASS_CACHE=DIR` (set by the tools): instrumented and rewritten classes on disk, so a new worker skips
  instrumenting.

## Running

    fish run MAP --a DIR:PKG --b DIR:PKG [--seed N] [--trace] [--dump ROUND] [--out-a F] [--out-b F]
    fish worker            jobs on stdin (tab separated key=value: id, map, a, b, seed, outA, outB),
                           one JSON result per line on stdout
    fish validate MAP...   LiveMap's checks

Environment: `FISH_CLASSPATH`, `FISH_HEAP` (default 1g), `FISH_JVM_OPTS`, `FISH_PROFILE=FILE` (sample the engine
thread every millisecond, write the hottest frames at exit; with `FISH_CLEAN_EXIT=1`).

## Verifying

`tools/verify` plays each (map, seed) on both engines and compares the state hash (every robot's id, team, type,
location, direction, health, cheese and cooldowns; both teams' resources and counts; map cheese and dirt) after
every round, dumping the first round that differs from both sides. Run it after any change here.
