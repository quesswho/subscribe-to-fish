#!/usr/bin/env python3

import argparse
import sys
import time
from pathlib import Path

from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

from jinja2 import Environment, FileSystemLoader, nodes
from jinja2.ext import Extension


# =====================
# EXISTING COMPILER LOGIC
# =====================

class DebugExtension(Extension):
    tags = {"debug"}

    def parse(self, parser):
        lineno = next(parser.stream).lineno
        args = [nodes.Const(lineno)]
        while parser.stream.current.type != 'block_end':
            args.append(parser.parse_expression())
        return nodes.CallBlock(
            self.call_method("_debug", args), [], [], [], lineno=lineno
        )

    def _debug(self, lineno, *args, caller):
        flattened_args = []
        for arg in args:
            if isinstance(arg, tuple):
                flattened_args.extend(arg)
            else:
                flattened_args.append(arg)

        message = ' + ", " + '.join(
            f'"{str(arg)} = " + {str(arg)}' for arg in flattened_args
        )
        return f'System.out.println("[{lineno}] " + {message});'


def bitAnd(a, b): return a & b
def bitOr(a, b): return a | b
def bitLeftShift(a, b): return a << b
def bitRightShift(a, b): return a >> b
def intDiv(a, b): return a // b

def capitalise(s):
    return s if not s else s[0].upper() + s[1:]

def radialMask(rsqr, cx, cy):
    mask0 = mask1 = 0
    for x in range(9):
        for y in range(9):
            dist = (x - cx) ** 2 + (y - cy) ** 2
            if dist > rsqr:
                continue
            if y >= 7:
                mask1 |= 1 << (9 * (y - 7) + x)
            else:
                mask0 |= 1 << (9 * y + x)
    return (mask0, mask1)

directions_list = [
    'Direction.NORTH',
    'Direction.NORTHEAST',
    'Direction.EAST',
    'Direction.SOUTHEAST',
    'Direction.SOUTH',
    'Direction.SOUTHWEST',
    'Direction.WEST',
    'Direction.NORTHWEST',
]
short_directions_list = ['N', 'NE', 'E', 'SE', 'S', 'SW', 'W', 'NW']
all_directions_list = directions_list + ['Direction.CENTER']

try_ord = [0, 4, 1, -1, 3, -3, 2, -2]

wide_related_map_center = {
    0: ["Direction.CENTER", "Direction.NORTH", "Direction.NORTHEAST", "Direction.NORTHWEST", "Direction.EAST", "Direction.WEST"],
    1: ["Direction.CENTER", "Direction.NORTHEAST", "Direction.NORTH", "Direction.EAST", "Direction.NORTHWEST", "Direction.SOUTHEAST"],
    2: ["Direction.CENTER", "Direction.EAST", "Direction.NORTHEAST", "Direction.SOUTHEAST", "Direction.NORTH", "Direction.SOUTH"],
    3: ["Direction.CENTER", "Direction.SOUTHEAST", "Direction.SOUTH", "Direction.EAST", "Direction.NORTHEAST", "Direction.SOUTHWEST"],
    4: ["Direction.CENTER", "Direction.SOUTH", "Direction.SOUTHEAST", "Direction.SOUTHWEST", "Direction.EAST", "Direction.WEST"],
    5: ["Direction.CENTER", "Direction.SOUTHWEST", "Direction.SOUTH", "Direction.WEST", "Direction.NORTHWEST", "Direction.SOUTHEAST"],
    6: ["Direction.CENTER", "Direction.WEST", "Direction.NORTHWEST", "Direction.SOUTHWEST", "Direction.NORTH", "Direction.SOUTH"],
    7: ["Direction.CENTER", "Direction.NORTHWEST", "Direction.NORTH", "Direction.WEST", "Direction.NORTHEAST", "Direction.SOUTHWEST"],
}


def _build_movement_table():
    table = {}
    for idx, facing in enumerate(directions_list):
        highlight_set = set(wide_related_map_center[idx])
        ordered_dirs = ["Direction.CENTER"]
        for offset in try_ord:
            candidate_idx = (idx + offset + 8) % 8
            ordered_dirs.append(directions_list[candidate_idx])
        table[facing] = [(d, d in highlight_set) for d in ordered_dirs]
    table["Direction.CENTER"] = [("Direction.CENTER", True)]
    return table

movement_table = _build_movement_table()


def make_env():
    env = Environment(
        loader=FileSystemLoader("./"),
        trim_blocks=True,
        lstrip_blocks=True,
    )
    env.add_extension(DebugExtension)
    env.globals.update({
        "bitAnd": bitAnd,
        "bitOr": bitOr,
        "bitLeftShift": bitLeftShift,
        "bitRightShift": bitRightShift,
        "intDiv": intDiv,
        "capitalise": capitalise,
        "radialMask": radialMask,
        "MAX_MAP_SIZE": 60,
        "directions": directions_list,
        "shortDirections": short_directions_list,
        "allDirections": all_directions_list,
        "try_ord": try_ord,
        "movement_table": movement_table,
        "get_movements": movement_table.__getitem__,
    })
    return env


ENV = make_env()
ALLOWED_ROOT = Path("src/awubot").resolve()
TEMPLATES_DIR = (ALLOWED_ROOT / "templates").resolve()
GLOBAL_EXPORTS = set()
GLOBAL_EXPORTS_READY = False
GLOBAL_TEMPLATE_NAMES = None
WRAPPED_TEMPLATE_CACHE = {}


def _in_allowed_root(path: Path) -> bool:
    try:
        path.resolve().relative_to(ALLOWED_ROOT)
    except ValueError:
        return False
    return True


def _is_templates_path(path: Path) -> bool:
    try:
        path.resolve().relative_to(TEMPLATES_DIR)
    except ValueError:
        return False
    return True


def _template_name(path: Path) -> str:
    base = Path(".").resolve()
    try:
        rel = path.resolve().relative_to(base)
    except ValueError:
        return ""
    return rel.as_posix()


def _red(text: str) -> str:
    if sys.stdout.isatty():
        return f"\x1b[31m{text}\x1b[0m"
    return text


def _global_template_names():
    global GLOBAL_TEMPLATE_NAMES
    if GLOBAL_TEMPLATE_NAMES is not None:
        return GLOBAL_TEMPLATE_NAMES
    if not TEMPLATES_DIR.exists():
        GLOBAL_TEMPLATE_NAMES = []
        return GLOBAL_TEMPLATE_NAMES
    names = []
    for path in sorted(TEMPLATES_DIR.rglob("*.jinja2")):
        if path.name.startswith("."):
            continue
        name = _template_name(path)
        if name:
            names.append(name)
    GLOBAL_TEMPLATE_NAMES = names
    return names


def _refresh_global_exports():
    global GLOBAL_EXPORTS_READY, GLOBAL_TEMPLATE_NAMES
    for key in list(GLOBAL_EXPORTS):
        ENV.globals.pop(key, None)
    GLOBAL_EXPORTS.clear()
    WRAPPED_TEMPLATE_CACHE.clear()
    GLOBAL_TEMPLATE_NAMES = None

    for name in _global_template_names():
        module = ENV.get_template(name).module
        for key, val in module.__dict__.items():
            if key.startswith("_"):
                continue
            ENV.globals[key] = val
            GLOBAL_EXPORTS.add(key)

    GLOBAL_EXPORTS_READY = True


def _wrap_with_globals(template_name: str):
    cached = WRAPPED_TEMPLATE_CACHE.get(template_name)
    if cached is not None:
        return cached
    global_templates = _global_template_names()
    if not global_templates:
        tpl = ENV.get_template(template_name)
        WRAPPED_TEMPLATE_CACHE[template_name] = tpl
        return tpl
    parts = []
    for name in global_templates:
        parts.append(f"{{% include '{name}' %}}")
    parts.append(f"{{% include '{template_name}' %}}")
    tpl = ENV.from_string("\n".join(parts))
    WRAPPED_TEMPLATE_CACHE[template_name] = tpl
    return tpl


def compile_template(path: Path, debug: bool, include_maptracker: bool):
    if not path.name.endswith(".jinja2"):
        return
    if path.name.startswith("."):
        return
    if (not include_maptracker) and path.name == "MapTracker.java.jinja2":
        return
    if not _in_allowed_root(path):
        return  # skip other directories
    if _is_templates_path(path):
        return  # skip global templates
    template_name = _template_name(path)
    if not template_name:
        return

    try:
        if not GLOBAL_EXPORTS_READY:
            _refresh_global_exports()
        output_path = path.with_suffix("")
        template = _wrap_with_globals(template_name)

        stream = template.stream(
            directions=directions_list,
            shortDirections=short_directions_list,
            allDirections=all_directions_list,
            debug=debug
        )
        stream.dump(str(output_path), encoding="utf-8")
        print(f"[compiled] {path} -> {output_path}")

    except Exception as e:
        print(_red(f"[error] {path}: {e}"))


def compile_all(debug: bool, include_maptracker: bool):
    global GLOBAL_EXPORTS_READY
    GLOBAL_EXPORTS_READY = False
    _refresh_global_exports()
    for path in ALLOWED_ROOT.rglob("*.jinja2"):
        compile_template(path, debug, include_maptracker)


# =====================
# WATCH MODE
# =====================

class JinjaWatchHandler(FileSystemEventHandler):
    def __init__(self, debug: bool, include_maptracker: bool):
        self.debug = debug
        self.include_maptracker = include_maptracker

    def on_modified(self, event):
        if not event.is_directory:
            path = Path(event.src_path)
            if _is_templates_path(path):
                ENV.cache.clear()
                compile_all(self.debug, self.include_maptracker)
                return
            compile_template(path, self.debug, self.include_maptracker)

    def on_created(self, event):
        if not event.is_directory:
            path = Path(event.src_path)
            if _is_templates_path(path):
                ENV.cache.clear()
                compile_all(self.debug, self.include_maptracker)
                return
            compile_template(path, self.debug, self.include_maptracker)

    def on_moved(self, event):
        if not event.is_directory:
            src_path = Path(event.src_path)
            dest_path = Path(event.dest_path)
            if _is_templates_path(src_path) or _is_templates_path(dest_path):
                ENV.cache.clear()
                compile_all(self.debug, self.include_maptracker)
                return
            compile_template(dest_path, self.debug, self.include_maptracker)


# =====================
# MAIN
# =====================

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--watch", action="store_true",
                        help="Watch for changes and recompile incrementally")
    parser.add_argument("--debug", action="store_true",
                        help="Enable debug rendering")
    parser.add_argument("--include-maptracker", action="store_true",
                        help="Include MapTracker.java.jinja2 in compilation")
    args = parser.parse_args()

    # Always do a full rebuild first
    compile_all(args.debug, args.include_maptracker)

    if not args.watch:
        return

    print("Watching for .jinja2 changes (Ctrl+C to stop)...")
    observer = Observer()
    observer.schedule(
        JinjaWatchHandler(args.debug, args.include_maptracker),
        path=".",
        recursive=True
    )
    observer.start()

    try:
        while True:
            time.sleep(0.5)
    except KeyboardInterrupt:
        observer.stop()

    observer.join()


if __name__ == "__main__":
    main()
