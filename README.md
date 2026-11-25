# caterpillow - Battlecode 2026

One bot, `src/awubot`, and **fish**: a C++ reimplementation of the Battlecode 2026 game engine that plays games
move for move like the official engine (1.2.6), many times faster, plus the tools to test bot changes with it.

This repository is a single-commit snapshot. The bot started as the team's final tournament bot, tag `jan29` of
[awu7/battlecode-2026](https://github.com/awu7/battlecode-2026), whose history holds every earlier version and its
authors. That bot is included here as an opponent, `opponents/jan29/awubot`.

## The result

On all 345 maps (`--maps all`: the pool plus generated and jittered maps), every opening played twice with the sides
swapped, `src/awubot` beats `jan29` by +399.6 [+389.1, +410.8] Elo and wins 90.9% of games (1195 of 13120 lost,
seeds 66000000, 6560 pairs). To replay that test (games are deterministic in map, seed and bot code):

    tools/setup
    tools/sprt wt --base opponents/jan29/awubot --maps all --seed 66000000 --max-pairs 6560 --elo0 381 --elo1 386

## What took it from 50% to 90.9%

Every change was a paired test against the version before it on the same openings, and kept only if it won on fresh
seeds and lost nowhere else; a week of these added up. Grouped by kind:

- **Bug fixes.** Failed asserts killed robots (Battlecode runs with assertions on), the king's north-west flee move
  checked the wrong tiles, carriers bounced between two cells for hundreds of rounds when the king moved, and the king
  never bit a cat (cats belong to neither team, so they never showed up as enemies), so a cat pinned against it
  scratched it to death unanswered. Seat B also had unmirrored geometry: its rats led with the wrong hand on
  top-bottom mirrored maps, and its central no-formation zone was not mirrored, so on some maps only seat A could
  form a second king.
- **Rat traps.** Traps are invisible to the other team. Laying them once the army reaches 16 rats, from the tile a
  rat stands on and between us and an enemy two tiles away, and keeping healthy rats at that distance instead of
  stepping in, won the rat war that decides most games.
- **The cat race.** Cooperation games are scored half on cat damage. A cat clock (rats tell the kings when a cat is
  low) times extra kings and cat traps so we win the race to the last cat instead of starving or losing on points.
- **Kings and economy.** A third king when it is safe, second kings formed beside clusters of mines no king serves,
  kings that give up a mine they cannot reach, carriers that keep collecting on long trips, and a cap on loads that
  could never be carried home in time.
- **Combat details.** A graded threat score, king hunting, a throw planner, cheaper strafes, only chasing reachable
  cheese, and a carried rat calling out its carrier so allies turn and grab or bite it.
- **Deleting rules.** An ablation of 66 rules found two inherited ones that had turned harmful (rats always fighting
  cats near the king, a higher spawn threshold when a king sees a cat); removing them was worth about +20 Elo.

The engine made this possible: fish plays the official game exactly but fast enough to test each idea on thousands of
games, and oracle builds (the same engine with one rule bent for one team) showed where the remaining Elo was: our
baby rats' survival (enemy traps and grabs), not the king's own deaths.

The trap, strafe and reachable-cheese ideas came from
[Luzivog/battlecode-2026-challenger](https://github.com/Luzivog/battlecode-2026-challenger), another bot grown from the
same Jan 29 code. Only ideas were taken: that repository is AGPL-3.0, and all code here is our own.

In this snapshot the only git ref is `HEAD`, so where the examples below name tags, use `wt`, `HEAD` or a bot folder.

## Setup

    tools/setup        downloads a JDK 21 into ~/.cache/caterpillow/jdk21, builds the engine and the bot
    tools/build        rebuild (incremental); --clean from scratch

Needs `g++` (C++20), `make`, `python3`, `curl`. Gradle (`./gradlew`) is still the way to build a submission
(`./gradlew zipForSubmit`) and runs the official engine for `tools/replay`.

## Testing a change

A candidate is the working tree (`wt`), a branch, a tag or a commit; bots are compiled from git and cached by
content under `.fish/bots`.

    tools/sprt                        working tree vs HEAD: stronger?
    tools/sprt my-branch --base v3    a branch against an older version
    tools/sprt --maps all --elo1 20   on the pool plus generated maps, testing for a bigger gain

`tools/sprt` plays **pairs**: every opening (map, seed) twice with the sides swapped, so map and side advantages
cancel, and runs a sequential probability ratio test (fishtest's GSPRT) on the pair scores until it can say
stronger or not. Default hypotheses: Elo 0 against Elo 10, alpha = beta = 0.05.

Exit codes: 0 stronger, 1 not stronger, 2 undecided (hit `--max-pairs`), 3 broken (an engine error; with
`--strict` also uncaught exceptions in the candidate). It prints the Elo estimate with a 95% interval and the
maps where the candidate did best and worst. Every finished game is cached in `.fish/games` by (bot code, map
content, seed, engine version): rerunning a test, or testing against the same base again, only plays new games.

Other tools (`--help` on each):

    tools/run [A] [B] --map M --seed S [--out DIR]   one game on fish; bot output to DIR/{a,b}.txt
    tools/replay [A] [B] --map M --seed S            the same game on the official engine, saving a .bc26 replay
    tools/bench                                      games and rounds per second
    tools/verify                                     does fish still play exactly like the official engine?
    tools/mapgen fresh N | jitter MAP | show MAP     map generator, below

Since fish and the official engine play the same games, a bad game found by `tools/sprt` or `tools/run` can be
watched in the client: `tools/replay wt HEAD --map rift --seed 12`.

## Maps

`--maps` takes names, folders, or presets: `pool` (default: `maps/`, minus the joke maps in
`tools/fishlib.py:SKIP_MAPS`), `gen` (`testmaps/gen`), `jitter` (`testmaps/jitter`), `all` (all three).

- `maps/`: the official maps (sprint, qualifier and final tournament maps from the battlecode26 repo) and the
  team's own test maps.
- `testmaps/gen/`: generated by `tools/mapgen fresh`. Each map borrows its parameters from a random pool map
  (size, symmetry, share of walls and dirt, mine and cat counts, king distance) and builds a new layout from the
  motifs the pool is made of: long walls, arcs, rooms with doors, lattices, blobs, dirt corners, bands and speckle.
- `testmaps/jitter/`: near copies of pool maps (`tools/mapgen jitter`): mines, kings and cat patrols moved a few
  cells, some walls and dirt reshaped. Good for checking that a gain is not tied to the exact pool maps.

Every generated map passes the engine's own checks (`LiveMap.assertIsValid`) and playability checks (kings and
mines connected, room for the kings, cat patrols a 2x2 cat can walk). Generation is deterministic in `--seed`.

## How fish works

`engine/` holds the engine: C++ for the game rules, with your Java bot running unchanged inside an embedded JVM.
See `engine/README.md` for the details. In short:

- `engine/src/game/` ports `battlecode.world` (GameWorld, InternalRobot, RobotControllerImpl) line for line,
  including the official engine's quirks, and the cats' AI and RNG (`java.util.Random`) bit for bit.
- Bots are instrumented by the official instrumenter, so bytecode counts are identical. Each robot runs on a
  JVM continuation instead of a thread, and all robots of a team share one copy of each class, with each robot's
  static fields swapped in and out at every turn. That removes the per-robot class loading that dominates the
  official engine's time (every robot of awubot loads a 3.4 MB class), and lets the JIT compile the bot once.
- `fish worker` keeps a JVM warm across games; the tools run one worker per core.

`tools/verify` plays games on both engines and compares a hash of the whole game state after every round. Run it
after touching `engine/`. Set `FISH_ISOLATED=1` to run bots the way the official engine does (one class loader
per robot), which is slower but rules out the shared-class rewrite when chasing a difference.
