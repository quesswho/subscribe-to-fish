"""Shared code for the fish tools: paths, bot builds, the worker pool and the game cache."""
import contextlib
import hashlib
import json
import os
import queue
import shutil
import subprocess
import sys
import tempfile
import threading
import time
from dataclasses import dataclass, field
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MAPS = ROOT / "maps"
TESTMAPS = ROOT / "testmaps"
STATE = ROOT / ".fish"
FISH = ROOT / "engine/build/fish"
JDK = Path(os.environ.get("FISH_JDK", Path.home() / ".cache/caterpillow/jdk21"))
JAVA = str(JDK / "bin/java")
JAVAC = str(JDK / "bin/javac")
JAVA_OPENS = [
    f"--add-opens=java.base/{p}=ALL-UNNAMED"
    for p in ("jdk.internal.misc", "jdk.internal.math", "jdk.internal.util", "jdk.internal.access", "sun.security.action")
]
BOT_PACKAGE = "awubot"
# Maps that are not real games: never part of a default pool.
SKIP_MAPS = {"troll1", "troll2", "5t4rv4t10n_1337", "coop"}


def die(msg, code=3):
    print(f"error: {msg}", file=sys.stderr)
    sys.exit(code)


def git(*args, cwd=ROOT):
    return subprocess.run(["git", *args], cwd=cwd, check=True, capture_output=True, text=True).stdout.strip()


# ---------------------------------------------------------------- engine


def engine_classpath():
    p = ROOT / "engine/build/engine-classpath.txt"
    if not p.exists():
        die("the engine is not built: run tools/build")
    return p.read_text().strip()


def build_engine(quiet=True):
    """Builds engine/build/fish and its Java host (incremental)."""
    env = dict(os.environ, JAVA_HOME=str(JDK))
    if not (ROOT / "engine/build/engine-classpath.txt").exists():
        subprocess.run([str(ROOT / "gradlew"), "engineClasspath", "-q", "--console=plain"], cwd=ROOT, env=env,
                       check=True)
    out = subprocess.run(["make", "-C", str(ROOT / "engine"), f"-j{os.cpu_count() or 4}"], env=env,
                         capture_output=quiet, text=True)
    if out.returncode != 0:
        die("engine build failed:\n" + (out.stdout or "") + (out.stderr or ""))


# ---------------------------------------------------------------- maps


def list_maps(folder=MAPS):
    return sorted((p.stem for p in Path(folder).glob("*.map26")), key=str.lower)


def map_path(name):
    p = Path(name)
    if p.suffix == ".map26" and p.exists():
        return p.resolve()
    for folder in (MAPS, TESTMAPS):
        hit = sorted(Path(folder).rglob(f"{name}.map26"))
        if hit:
            return hit[0].resolve()
    raise FileNotFoundError(f"no map named {name}")


def resolve_maps(spec):
    """A map pool from a spec: comma separated names, folders, or the presets 'pool' (maps/ minus SKIP_MAPS),
    'gen' (testmaps/gen), 'jitter' (testmaps/jitter), 'all' (all three)."""
    out = []
    for part in (spec or "pool").split(","):
        part = part.strip()
        if not part:
            continue
        if part in ("pool", "all"):
            out += [map_path(m) for m in list_maps(MAPS) if m not in SKIP_MAPS]
        if part in ("gen", "all"):
            out += sorted((TESTMAPS / "gen").glob("*.map26"))
        if part in ("jitter", "all"):
            out += sorted((TESTMAPS / "jitter").glob("*.map26"))
        if part in ("pool", "gen", "jitter", "all"):
            continue
        p = Path(part)
        if p.is_dir():
            out += sorted(p.rglob("*.map26"))
        else:
            out.append(map_path(part))
    seen, unique = set(), []
    for p in out:
        if p not in seen:
            seen.add(p)
            unique.append(p)
    if not unique:
        die(f"no maps match {spec}")
    return unique


# ---------------------------------------------------------------- bots


@dataclass
class Bot:
    name: str      # what the user typed: a ref, 'wt' for the working tree, or a folder
    key: str       # content hash: same code, same key
    classes: Path  # folder holding the compiled package
    package: str = BOT_PACKAGE

    @property
    def spec(self):
        return f"{self.classes}:{self.package}"


def _hash_tree(src: Path):
    h = hashlib.sha256()
    for p in sorted(src.rglob("*.java")):
        h.update(str(p.relative_to(src)).encode())
        h.update(p.read_bytes())
    return h.hexdigest()[:16]


def _publish(tmp: Path, out: Path):
    """Moves a finished folder into place in one rename. Several processes may build the same key at once (parallel
    tools/cluster submissions): each works in its own temporary folder, the first rename wins and the others drop
    theirs, so nobody ever reads a half-written build or source tree."""
    if out.is_dir() and not any(out.iterdir()):
        out.rmdir()
    try:
        tmp.rename(out)
    except OSError:
        shutil.rmtree(tmp, ignore_errors=True)
        if not out.is_dir():
            raise


def _compile(src: Path, out: Path):
    out.parent.mkdir(parents=True, exist_ok=True)
    tmp = Path(tempfile.mkdtemp(prefix=out.name + ".tmp-", dir=out.parent))
    files = [str(p) for p in src.rglob("*.java") if "/test/" not in str(p)]
    with slot():  # javac takes up to 4 GB
        r = subprocess.run([JAVAC, "-nowarn", "-encoding", "UTF-8", "--release", "21", "-J-Xmx4g", "-cp",
                            engine_classpath(), "-d", str(tmp), *files], capture_output=True, text=True)
    if r.returncode != 0:
        shutil.rmtree(tmp, ignore_errors=True)
        die(f"compile failed for {src}:\n{r.stdout}{r.stderr}")
    _publish(tmp, out)


def _class_package(p: Path):
    """The package a folder of compiled classes holds: awubot if present, else its one package folder."""
    if (p / BOT_PACKAGE).is_dir():
        return BOT_PACKAGE
    pkgs = [d.name for d in sorted(p.iterdir()) if d.is_dir() and any(d.rglob("*.class"))]
    return pkgs[0] if len(pkgs) == 1 else None


def _folder_key(p: Path):
    """A compiled folder's key: the source key it was built from (its KEY file, written by build_bot, so a build
    shipped elsewhere keeps its games in the cache), else a hash of its class files."""
    k = p / "KEY"
    if k.is_file():
        return k.read_text().strip()
    h = hashlib.sha256()
    for f in sorted((p / _class_package(p)).rglob("*.class")):
        h.update(str(f.relative_to(p)).encode())
        h.update(f.read_bytes())
    return "dir-" + h.hexdigest()[:12]


def build_bot(name="wt"):
    """Compiles a bot and returns it. name: 'wt' (working tree src/), a git ref (branch, tag, commit), or a folder
    of compiled classes holding one package, or a package's source folder (another team's bot). Builds are cached by source hash under .fish/bots."""
    p = Path(name)
    if name != "wt" and p.is_dir() and _class_package(p):
        return Bot(name, _folder_key(p), p.resolve(), _class_package(p))
    if name != "wt" and p.is_dir() and any(p.glob("*.java")):
        # Another team's bot: a package's source folder (its name is the package), e.g. an opponent.
        pkg = p.resolve().name
        key = f"{pkg}-{_hash_tree(p)}"
        out = STATE / "bots" / key
        if not (out / pkg).is_dir():
            print(f"compiling {name} ({key})...", file=sys.stderr, flush=True)
            _compile(p, out)
            (out / "KEY").write_text(key + "\n")
        return Bot(name, key, out, pkg)
    if name == "wt":
        src = ROOT / "src" / BOT_PACKAGE
    else:
        try:
            tree = git("rev-parse", f"{name}:src/{BOT_PACKAGE}")
        except subprocess.CalledProcessError:
            die(f"unknown bot {name}: not a folder, git ref, or 'wt'")
        stage = STATE / "src" / tree[:16]
        src = stage / "src" / BOT_PACKAGE
        if not src.is_dir():
            stage.parent.mkdir(parents=True, exist_ok=True)
            tmp = Path(tempfile.mkdtemp(prefix=stage.name + ".tmp-", dir=stage.parent))
            archive = subprocess.run(["git", "archive", name, f"src/{BOT_PACKAGE}"], cwd=ROOT, check=True,
                                     capture_output=True).stdout
            subprocess.run(["tar", "-x", "-C", str(tmp)], input=archive, check=True)
            _publish(tmp, stage)
    # Keyed by content, so the working tree and a ref with the same code share a build and their games.
    key = _hash_tree(src)
    out = STATE / "bots" / key
    if not (out / BOT_PACKAGE).is_dir():
        print(f"compiling {name} ({key})...", file=sys.stderr, flush=True)
        _compile(src, out)
        (out / "KEY").write_text(key + "\n")
    return Bot(name, key, out)


# ---------------------------------------------------------------- games


@dataclass
class Game:
    map: Path
    seed: int
    a: Bot
    b: Bot
    out_a: str = ""
    out_b: str = ""
    tag: object = None
    xray: str = ""  # teams whose rats sense a full circle (a testing oracle): "", "A", "B", "AB"
    timeline: int = 0  # record both teams' state every this many rounds (result["timeline"]; not cached)
    props: str = ""  # bc.testing properties for the bots, "A.name=value;B.name=value" (runtime-tuned constants)
    result: dict = field(default_factory=dict)

    def key(self):
        xray = f"_xray{self.xray}" if self.xray else ""
        props = "_p" + hashlib.sha256(self.props.encode()).hexdigest()[:10] if self.props else ""
        return f"{self.a.key}_{self.b.key}{xray}{props}/{self.map.stem}_{_file_hash(self.map)}_{self.seed}"


_file_hashes = {}


def _file_hash(p: Path):
    h = _file_hashes.get(p)
    if h is None:
        h = _file_hashes[p] = hashlib.sha256(p.read_bytes()).hexdigest()[:10]
    return h


class GameCache:
    """Results of games already played, by (bots, map content, seed). Engine changes clear it (see engine_key),
    except for the older engines listed in engine/same-play.txt, whose games are read too."""

    def __init__(self, enabled=True):
        self.enabled = enabled
        self.root = STATE / "games" / engine_key()
        self.roots = cache_roots()

    def get(self, g: Game):
        if not self.enabled:
            return None
        name = g.key() + ".json"
        for root in self.roots:
            try:
                return json.loads((root / name).read_text())
            except (OSError, ValueError):
                continue
        return None

    def put(self, g: Game, result):
        if not self.enabled or "error" in result:
            return
        p = self.root / (g.key() + ".json")
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(json.dumps(result))


def same_play_keys():
    """Older engine keys whose games the current engine plays identically, from engine/same-play.txt (one key per
    line, `#` starts a note). A key goes in only after a sample of its cached games was replayed on the new engine
    and every result matched (tools/sameplay); list the keys it already trusted too. The file is not part of the
    engine key, so editing it keeps the cache."""
    try:
        lines = (ROOT / "engine/same-play.txt").read_text().splitlines()
    except OSError:
        return []
    keys = [l.split("#")[0].strip() for l in lines]
    return [k for k in keys if k and k != engine_key()]


def cache_roots():
    """The current engine's game folder, then those of the engines it plays the same as."""
    return [STATE / "games" / k for k in [engine_key(), *same_play_keys()]]


def engine_key():
    h = hashlib.sha256()
    for p in sorted((ROOT / "engine/src").rglob("*")) + sorted((ROOT / "engine/java").rglob("*.java")):
        if p.is_file():
            h.update(p.read_bytes())
    return h.hexdigest()[:12]


IDLE_SECONDS = 5


class WorkerPool:
    """N `fish worker` processes, each a warm JVM playing one game at a time."""

    def __init__(self, n, heap=None, results=None):
        self.n = max(1, n)
        self.env = fish_env()
        if heap:
            self.env["FISH_HEAP"] = heap
        self.jobs = queue.Queue()
        self.results = results if results is not None else queue.Queue()  # pools may share one
        self.threads = [threading.Thread(target=self._run, daemon=True) for _ in range(self.n)]
        self.procs = []
        self.slots = {}  # pid -> held slot
        self.lock = threading.Lock()
        for t in self.threads:
            t.start()

    def _spawn(self):
        # A worker can fail to start on a busy machine (memory, a slow JVM boot): retry before giving up.
        for attempt in range(4):
            fd = acquire_slot()
            p = subprocess.Popen([str(FISH), "worker"], stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                                 stderr=subprocess.DEVNULL, text=True, bufsize=1, env=self.env)
            ready = p.stdout.readline()
            if '"ready"' in ready:
                with self.lock:
                    self.procs.append(p)
                    self.slots[p.pid] = fd
                return p
            p.kill()
            p.wait()
            release_slot(fd)
            time.sleep(2 + 5 * attempt)
        raise RuntimeError("fish worker failed to start")

    def _drop(self, p, kill):
        """Ends a worker and frees its slot."""
        with contextlib.suppress(OSError):
            p.kill() if kill else p.stdin.close()
        p.wait()
        with self.lock:
            release_slot(self.slots.pop(p.pid, None))

    def _run(self):
        proc = None
        while True:
            # An idle worker gives its slot back: slots are shared by every pool on the machine, and a pool whose
            # finished workers kept theirs while its queued games waited for one could deadlock with another pool.
            try:
                g = self.jobs.get(timeout=IDLE_SECONDS) if proc is not None else self.jobs.get()
            except queue.Empty:
                self._drop(proc, kill=False)
                proc = None
                continue
            if g is None:
                break
            for attempt in range(2):  # a game whose worker died is retried once on a fresh worker
                try:
                    if proc is not None and proc.poll() is not None:
                        self._drop(proc, kill=True)
                        proc = None
                    if proc is None:
                        proc = self._spawn()
                    line = "\t".join([f"id={id(g)}", f"map={g.map}", f"a={g.a.spec}", f"b={g.b.spec}",
                                      f"seed={g.seed}", f"outA={g.out_a}", f"outB={g.out_b}", f"xray={g.xray}",
                                      f"timeline={g.timeline}", f"props={g.props}"])
                    proc.stdin.write(line + "\n")
                    proc.stdin.flush()
                    out = proc.stdout.readline()
                    if not out:
                        raise RuntimeError("fish worker died")
                    g.result = json.loads(out)
                    break
                except Exception as e:  # noqa: BLE001 - a broken game is reported, not fatal
                    g.result = {"error": str(e)}
                    if proc is not None:
                        self._drop(proc, kill=True)
                    proc = None
            self.results.put(g)
        if proc is not None:
            self._drop(proc, kill=False)

    def submit(self, g: Game):
        self.jobs.put(g)

    def close(self):
        for _ in self.threads:
            self.jobs.put(None)
        for t in self.threads:
            t.join(timeout=5)
        with self.lock:
            for p in self.procs:
                if p.poll() is None:
                    p.kill()
            for fd in self.slots.values():
                release_slot(fd)
            self.slots.clear()


def fish_env():
    """Environment for fish processes: instrumented bot classes are cached on disk across workers."""
    env = dict(os.environ)
    env.setdefault("FISH_CLASS_CACHE", str(STATE / "classcache"))
    return env


# ---------------------------------------------------------------- memory guard
#
# Every local fish or Java engine process holds one slot, a lock file under SLOTS, for as long as it runs. The slot
# count is per machine, not per tool, so any number of tools (or agents running tools) at once can never start more
# JVMs than the memory holds: the extra ones wait for a slot. A process also waits while the machine has less than
# MIN_FREE_GB available, whatever else is using it. FISH_SLOTS=N overrides the count.

JVM_GB = 1.5  # one worker: a 1 GB heap plus metaspace, code cache and the native engine
MIN_FREE_GB = 6.0
SLOTS = Path(os.environ.get("FISH_SLOTS_DIR", Path.home() / ".cache/caterpillow/slots"))


def cores():
    return len(os.sched_getaffinity(0)) if hasattr(os, "sched_getaffinity") else (os.cpu_count() or 2)


def mem_total_gb():
    """Physical memory, or the cgroup's limit when lower (a Slurm job)."""
    total = os.sysconf("SC_PAGE_SIZE") * os.sysconf("SC_PHYS_PAGES")
    try:
        limit = Path("/sys/fs/cgroup/memory.max").read_text().strip()
        if limit.isdigit():
            total = min(total, int(limit))
    except OSError:
        pass
    return total / 2**30


def mem_available_gb():
    try:
        for line in Path("/proc/meminfo").read_text().splitlines():
            if line.startswith("MemAvailable:"):
                return int(line.split()[1]) / 2**20
    except (OSError, ValueError, IndexError):
        pass
    return float("inf")


def slot_count():
    if os.environ.get("FISH_SLOTS"):
        return max(1, int(os.environ["FISH_SLOTS"]))
    # At most 40% of the memory: the desktop, browsers and Claude sessions share this machine. Heavy testing
    # belongs on the cluster (tools/cluster), whose jobs set FISH_SLOTS to their cores.
    return max(1, min(cores() - 1, int((mem_total_gb() - MIN_FREE_GB) * 0.4 / JVM_GB)))


def acquire_slot():
    """Blocks until a slot is free and the machine has memory to spare; returns the held lock (release_slot)."""
    import fcntl
    SLOTS.mkdir(parents=True, exist_ok=True)
    n, waited = slot_count(), 0.0
    while True:
        if mem_available_gb() >= MIN_FREE_GB:
            for i in range(n):
                fd = os.open(SLOTS / f"{i}.lock", os.O_RDWR | os.O_CREAT, 0o644)
                try:
                    fcntl.flock(fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
                    return fd
                except BlockingIOError:
                    os.close(fd)
        if waited == 10:
            print(f"fish: waiting for memory (all {n} slots taken or under {MIN_FREE_GB:g} GB free)",
                  file=sys.stderr, flush=True)
        time.sleep(0.5)
        waited += 0.5


def release_slot(fd):
    if fd is not None:
        try:
            os.close(fd)
        except OSError:
            pass


@contextlib.contextmanager
def slot():
    """`with F.slot(): subprocess.run(fish ...)` for a one-off engine process."""
    fd = acquire_slot()
    try:
        yield
    finally:
        release_slot(fd)


def default_workers():
    """One worker per core, minus one for the orchestrator, within the machine's slots."""
    return max(1, min(cores() - 1, slot_count()))


def play(games, workers=None, cache=None, on_result=None):
    """Plays games (cached ones are not replayed); calls on_result(game) as each finishes; returns them all."""
    cache = cache or GameCache()
    todo = []
    for g in games:
        hit = cache.get(g)
        if hit is not None:
            g.result = hit
            if on_result:
                on_result(g)
        else:
            todo.append(g)
    if todo:
        pool = WorkerPool(workers or default_workers())
        try:
            for g in todo:
                pool.submit(g)
            for _ in todo:
                g = pool.results.get()
                cache.put(g, g.result)
                if on_result:
                    on_result(g)
        finally:
            pool.close()
    return games


def now():
    return time.monotonic()
