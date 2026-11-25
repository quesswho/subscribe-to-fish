"""Reads and writes .map26 files (the battlecode.schema.GameMap flatbuffer) without the flatbuffers package.

    m = load("maps/rift.map26"); m.walls[y][x]; save(m, "testmaps/x.map26")

Directions in SpawnAction use FlatHelpers' ordinals: 0 CENTER, 1 W, 2 SW, 3 S, 4 SE, 5 E, 6 NE, 7 N, 8 NW.
"""
import struct
from dataclasses import dataclass, field
from pathlib import Path

ROTATIONAL, HORIZONTAL, VERTICAL = 0, 1, 2
NEUTRAL, TEAM_A, TEAM_B = 0, 1, 2
BABY_RAT, RAT_KING, CAT = 1, 2, 3


@dataclass
class Body:
    id: int
    x: int
    y: int
    dir: int = 0
    chirality: int = 0
    team: int = NEUTRAL
    type: int = CAT


@dataclass
class GameMap:
    name: str
    width: int
    height: int
    symmetry: int = ROTATIONAL
    seed: int = 0
    walls: list = field(default_factory=list)   # [y][x] bool
    dirt: list = field(default_factory=list)    # [y][x] bool
    cheese: list = field(default_factory=list)  # [y][x] int (-128..127)
    mines: list = field(default_factory=list)   # [(x, y)]
    bodies: list = field(default_factory=list)  # [Body]
    cats: dict = field(default_factory=dict)    # cat id -> [(x, y)] waypoints

    @staticmethod
    def blank(name, width, height, symmetry=ROTATIONAL):
        return GameMap(name, width, height, symmetry, 0,
                       [[False] * width for _ in range(height)], [[False] * width for _ in range(height)],
                       [[0] * width for _ in range(height)])

    def sym(self, x, y):
        """The location symmetric to (x, y) under the map's symmetry (GameWorld.symmetryLocation)."""
        sx = x if self.symmetry == HORIZONTAL else self.width - 1 - x
        sy = y if self.symmetry == VERTICAL else self.height - 1 - y
        return sx, sy


# ---------------------------------------------------------------- reading


class _Reader:
    def __init__(self, b):
        self.b = b

    def u8(self, at): return self.b[at]
    def u16(self, at): return struct.unpack_from("<H", self.b, at)[0]
    def i32(self, at): return struct.unpack_from("<i", self.b, at)[0]
    def u32(self, at): return struct.unpack_from("<I", self.b, at)[0]

    def field(self, table, i):
        vt = table - self.i32(table)
        if 4 + 2 * i >= self.u16(vt):
            return 0
        off = self.u16(vt + 4 + 2 * i)
        return table + off if off else 0

    def deref(self, at): return at + self.u32(at)

    def vec(self, table, i):
        f = self.field(table, i)
        if not f:
            return 0, 0
        v = self.deref(f)
        return v + 4, self.u32(v)

    def ints(self, table, i):
        at, n = self.vec(table, i)
        return [self.i32(at + 4 * k) for k in range(n)]


def load(path):
    r = _Reader(Path(path).read_bytes())
    root = r.u32(0)
    at, n = r.vec(root, 0)
    name = r.b[at:at + n].decode()
    size = r.field(root, 1)
    w, h = r.i32(size), r.i32(size + 4)
    sym = r.i32(r.field(root, 2)) if r.field(root, 2) else 0
    seed = r.i32(r.field(root, 4)) if r.field(root, 4) else 0
    m = GameMap.blank(name, w, h, sym)
    m.seed = seed
    wat, _ = r.vec(root, 5)
    dat, _ = r.vec(root, 6)
    cat_, _ = r.vec(root, 7)
    for i in range(w * h):
        x, y = i % w, i // w
        m.walls[y][x] = r.u8(wat + i) != 0
        m.dirt[y][x] = r.u8(dat + i) != 0
        c = r.u8(cat_ + i)
        m.cheese[y][x] = c - 256 if c > 127 else c
    mines = r.field(root, 8)
    if mines:
        t = r.deref(mines)
        m.mines = list(zip(r.ints(t, 0), r.ints(t, 1)))
    bodies = r.field(root, 3)
    if bodies:
        t = r.deref(bodies)
        at, n = r.vec(t, 0)
        for k in range(n):
            s = at + 8 * k
            m.bodies.append(Body(r.u16(s), r.u8(s + 2), r.u8(s + 3), r.u8(s + 4), r.u8(s + 5),
                                 struct.unpack_from("<b", r.b, s + 6)[0], r.u8(s + 7)))
    ids_at, nids = r.vec(root, 9)
    vecs_at, nvecs = r.vec(root, 10)
    for k in range(nvecs):
        t = r.deref(vecs_at + 4 * k)
        m.cats[r.u16(ids_at + 2 * k)] = list(zip(r.ints(t, 0), r.ints(t, 1)))
    return m


# ---------------------------------------------------------------- writing


class _Writer:
    """Lays a flatbuffer out front to back: a table's children follow it, so every uoffset points forward."""

    def __init__(self):
        self.b = bytearray(4)  # root uoffset, patched at the end

    def align(self, n):
        while len(self.b) % n:
            self.b.append(0)

    def patch_uoffset(self, at, target):
        struct.pack_into("<I", self.b, at, target - at)

    def table(self, fields):
        """fields: list of (kind, value) per field id, kind in None/'i32'/'struct8'/'off'. Returns
        (table position, {field id: absolute position of the field})."""
        layout, off = [], 4
        for i, (kind, value) in enumerate(fields):
            if kind is None:
                layout.append(0)
                continue
            size = 8 if kind == "struct8" else 4
            off = (off + size - 1) // size * size if size == 8 else off
            layout.append(off)
            off += size
        tsize = (off + 3) // 4 * 4
        vt_size = 4 + 2 * len(fields)
        self.align(4)
        vt = len(self.b)
        self.b += struct.pack("<HH", vt_size, tsize) + b"".join(struct.pack("<H", o) for o in layout)
        # The table is 8-aligned so 8-byte inline structs are too.
        while (len(self.b) + 0) % 8:
            self.b.append(0)
        table = len(self.b)
        self.b += bytes(tsize)
        struct.pack_into("<i", self.b, table, table - vt)
        where = {}
        for i, (kind, value) in enumerate(fields):
            if kind is None:
                continue
            pos = table + layout[i]
            where[i] = pos
            if kind == "i32":
                struct.pack_into("<i", self.b, pos, value)
            elif kind == "struct8":
                self.b[pos:pos + 8] = value
        return table, where

    def vector(self, elem_bytes, count, align=4):
        self.align(max(4, align))
        while (len(self.b) + 4) % align:
            self.b.append(0)
        at = len(self.b)
        self.b += struct.pack("<I", count) + elem_bytes
        return at

    def string(self, s):
        data = s.encode()
        self.align(4)
        at = len(self.b)
        self.b += struct.pack("<I", len(data)) + data + b"\0"
        return at


def _vec_table(w, xs, ys):
    t, f = w.table([("off", 0), ("off", 0)])
    w.patch_uoffset(f[0], w.vector(b"".join(struct.pack("<i", v) for v in xs), len(xs)))
    w.patch_uoffset(f[1], w.vector(b"".join(struct.pack("<i", v) for v in ys), len(ys)))
    return t


def dumps(m: GameMap):
    w = _Writer()
    W, H = m.width, m.height
    size = struct.pack("<ii", W, H)
    root, f = w.table([("off", 0), ("struct8", size), ("i32", m.symmetry), ("off", 0), ("i32", m.seed),
                       ("off", 0), ("off", 0), ("off", 0), ("off", 0), ("off", 0), ("off", 0)])
    w.patch_uoffset(0, root)
    w.patch_uoffset(f[0], w.string(m.name))

    bt, bf = w.table([("off", 0)])
    w.patch_uoffset(f[3], bt)
    bodies = b"".join(struct.pack("<HBBBBbB", b.id, b.x, b.y, b.dir, b.chirality, b.team, b.type)
                      for b in sorted(m.bodies, key=lambda b: b.id))
    w.patch_uoffset(bf[0], w.vector(bodies, len(m.bodies)))

    flat = [(x, y) for y in range(H) for x in range(W)]
    w.patch_uoffset(f[5], w.vector(bytes(1 if m.walls[y][x] else 0 for x, y in flat), W * H))
    w.patch_uoffset(f[6], w.vector(bytes(1 if m.dirt[y][x] else 0 for x, y in flat), W * H))
    w.patch_uoffset(f[7], w.vector(bytes(m.cheese[y][x] & 0xFF for x, y in flat), W * H))
    w.patch_uoffset(f[8], _vec_table(w, [x for x, _ in m.mines], [y for _, y in m.mines]))

    ids = sorted(m.cats)
    w.patch_uoffset(f[9], w.vector(b"".join(struct.pack("<H", i) for i in ids) + bytes(2 * (len(ids) % 2)), len(ids)))
    vec_at = w.vector(bytes(4 * len(ids)), len(ids))
    for k, cid in enumerate(ids):
        pts = m.cats[cid]
        t = _vec_table(w, [x for x, _ in pts], [y for _, y in pts])
        w.patch_uoffset(vec_at + 4 + 4 * k, t)
    w.patch_uoffset(f[10], vec_at)
    return bytes(w.b)


def save(m: GameMap, path):
    Path(path).parent.mkdir(parents=True, exist_ok=True)
    Path(path).write_bytes(dumps(m))
