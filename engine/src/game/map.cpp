#include "map.hpp"

#include <algorithm>
#include <cstring>
#include <fstream>
#include <iterator>
#include <stdexcept>

namespace fish {

namespace {

// Minimal read-only flatbuffer access: tables, vectors, strings, inline structs.
class FlatReader {
public:
    explicit FlatReader(const std::vector<uint8_t>& b) : b_(b) {}

    uint32_t u32(size_t at) const { return read<uint32_t>(at); }
    int32_t i32(size_t at) const { return read<int32_t>(at); }
    uint16_t u16(size_t at) const { return read<uint16_t>(at); }
    uint8_t u8(size_t at) const { return read<uint8_t>(at); }

    size_t root() const { return u32(0); }

    // Absolute position of field `id` of the table at `table`, or 0 if absent.
    size_t field(size_t table, int id) const {
        size_t vt = size_t(int64_t(table) - i32(table));
        uint16_t vtSize = u16(vt);
        size_t slot = 4 + 2 * size_t(id);
        if (slot >= vtSize) return 0;
        uint16_t off = u16(vt + slot);
        return off ? table + off : 0;
    }

    size_t deref(size_t at) const { return at + u32(at); }

    size_t table(size_t parent, int id) const {
        size_t f = field(parent, id);
        return f ? deref(f) : 0;
    }

    // Returns {start of elements, length} for the vector field, or {0, 0}.
    std::pair<size_t, uint32_t> vec(size_t parent, int id) const {
        size_t f = field(parent, id);
        if (!f) return {0, 0};
        size_t v = deref(f);
        return {v + 4, u32(v)};
    }

    std::string str(size_t parent, int id) const {
        auto [at, n] = vec(parent, id);
        check(at, n);
        return std::string(reinterpret_cast<const char*>(b_.data() + at), n);
    }

    int32_t i32Field(size_t parent, int id, int32_t def) const {
        size_t f = field(parent, id);
        return f ? i32(f) : def;
    }

private:
    const std::vector<uint8_t>& b_;

    void check(size_t at, size_t n) const {
        if (at + n > b_.size() || at + n < at) throw std::runtime_error("map file is truncated or corrupt");
    }

    template <class T>
    T read(size_t at) const {
        check(at, sizeof(T));
        T v;
        std::memcpy(&v, b_.data() + at, sizeof(T));
        return v;
    }
};

// battlecode.util.FlatHelpers.getDirectionFromOrdinal
Dir dirFromFlat(int ordinal) {
    static constexpr Dir map[9] = {CENTER, WEST, SOUTHWEST, SOUTH, SOUTHEAST, EAST, NORTHEAST, NORTH, NORTHWEST};
    if (ordinal < 0 || ordinal > 8) throw std::runtime_error("invalid direction ordinal in map");
    return map[ordinal];
}

enum GameMapField { NAME, SIZE, SYMMETRY, INITIAL_BODIES, SEED, WALLS, DIRT, CHEESE, CHEESE_MINES, CAT_IDS, CAT_VECS };

std::vector<int> readIntVec(const FlatReader& r, size_t table, int id) {
    auto [at, n] = r.vec(table, id);
    std::vector<int> out(n);
    for (uint32_t i = 0; i < n; i++) out[i] = r.i32(at + 4 * i);
    return out;
}

}  // namespace

GameMap parseMap(const std::vector<uint8_t>& bytes) {
    FlatReader r(bytes);
    size_t root = r.root();
    GameMap m;
    m.name = r.str(root, NAME);

    size_t size = r.field(root, SIZE);
    if (!size) throw std::runtime_error("map has no size");
    m.width = r.i32(size);
    m.height = r.i32(size + 4);
    if (m.width <= 0 || m.height <= 0 || m.width > 1000 || m.height > 1000)
        throw std::runtime_error("map has an invalid size");

    int sym = r.i32Field(root, SYMMETRY, 0);
    if (sym < 0 || sym > 2) throw std::runtime_error("map has an invalid symmetry");
    m.symmetry = Symmetry(sym);
    m.seed = r.i32Field(root, SEED, 0);

    int n = m.width * m.height;
    m.walls.assign(n, 0);
    m.dirt.assign(n, 0);
    m.mines.assign(n, 0);
    m.cheese.assign(n, 0);

    auto [wallsAt, wallsN] = r.vec(root, WALLS);
    auto [dirtAt, dirtN] = r.vec(root, DIRT);
    auto [cheeseAt, cheeseN] = r.vec(root, CHEESE);
    if (wallsN < uint32_t(n) || dirtN < uint32_t(n) || cheeseN < uint32_t(n))
        throw std::runtime_error("map arrays are shorter than width * height");
    for (int i = 0; i < n; i++) {
        m.walls[i] = r.u8(wallsAt + i) != 0;
        m.dirt[i] = r.u8(dirtAt + i) != 0;
        m.cheese[i] = int8_t(r.u8(cheeseAt + i));
    }

    size_t minesTable = r.table(root, CHEESE_MINES);
    if (minesTable) {
        auto xs = readIntVec(r, minesTable, 0), ys = readIntVec(r, minesTable, 1);
        for (size_t i = 0; i < xs.size() && i < ys.size(); i++) {
            int idx = xs[i] + m.width * ys[i];
            if (idx < 0 || idx >= n) throw std::runtime_error("cheese mine off the map");
            m.mines[idx] = 1;
        }
    }

    auto [catIdsAt, catIdsN] = r.vec(root, CAT_IDS);
    auto [catVecsAt, catVecsN] = r.vec(root, CAT_VECS);
    for (uint32_t i = 0; i < catVecsN; i++) {
        CatPath cat;
        if (i >= catIdsN) throw std::runtime_error("cat waypoint ids missing");
        cat.id = r.u16(catIdsAt + 2 * i);
        size_t vt = r.deref(catVecsAt + 4 * i);
        auto xs = readIntVec(r, vt, 0), ys = readIntVec(r, vt, 1);
        for (size_t j = 0; j < xs.size() && j < ys.size(); j++) cat.waypoints.push_back(xs[j] + m.width * ys[j]);
        m.cats.push_back(std::move(cat));
    }

    size_t bodyTable = r.table(root, INITIAL_BODIES);
    if (bodyTable) {
        auto [at, count] = r.vec(bodyTable, 0);
        for (uint32_t i = 0; i < count; i++) {
            size_t s = at + 8 * size_t(i);
            InitialBody b;
            b.id = r.u16(s);
            b.loc = {r.u8(s + 2), r.u8(s + 3)};
            b.dir = dirFromFlat(r.u8(s + 4));
            b.chirality = r.u8(s + 5);
            int team = int8_t(r.u8(s + 6));
            int type = int8_t(r.u8(s + 7));
            if (type < 1 || type > 3) throw std::runtime_error("invalid robot type in map");
            b.type = UnitType(type - 1);
            if (team < 0 || team > 2) throw std::runtime_error("invalid team in map");
            b.team = team == 1 ? TEAM_A : team == 2 ? TEAM_B : NEUTRAL;
            if (b.type == CAT) b.team = NEUTRAL;
            m.bodies.push_back(b);
        }
    }
    std::stable_sort(m.bodies.begin(), m.bodies.end(),
                     [](const InitialBody& a, const InitialBody& b) { return a.id < b.id; });
    return m;
}

GameMap loadMap(const std::string& path) {
    std::ifstream in(path, std::ios::binary);
    if (!in) throw std::runtime_error("cannot open map " + path);
    std::vector<uint8_t> bytes((std::istreambuf_iterator<char>(in)), std::istreambuf_iterator<char>());
    return parseMap(bytes);
}

std::string validateMap(const GameMap& m) {
    if (m.width > 60) return "map width exceeds 60";
    if (m.width < 20) return "map width below 20";
    if (m.height > 60) return "map height exceeds 60";
    if (m.height < 20) return "map height below 20";
    int kingsA = 0, kingsB = 0;
    for (auto& b : m.bodies) {
        if (b.team == TEAM_A || b.team == TEAM_B) {
            if (b.type != RAT_KING) return "initial bodies of a team must be rat kings";
            (b.team == TEAM_A ? kingsA : kingsB)++;
        }
    }
    if (kingsA != 1) return "expected 1 initial team A rat king";
    if (kingsB != 1) return "expected 1 initial team B rat king";
    int n = m.width * m.height, walls = 0, dirt = 0;
    std::vector<Loc> mines;
    for (int i = 0; i < n; i++) {
        if (m.walls[i] && m.mines[i]) return "wall on a cheese mine";
        if (m.mines[i]) mines.push_back(m.loc(i));
        walls += m.walls[i];
        dirt += m.dirt[i];
    }
    if (walls * 100 >= n * 20) return "too much of the map is walls";
    if (dirt * 100 >= n * 50) return "too much of the map is dirt";
    for (size_t i = 0; i < mines.size(); i++)
        for (size_t j = i + 1; j < mines.size(); j++)
            if (mines[i].distSq(mines[j]) < 25) return "cheese mines too close together";
    for (int i = 0; i < n; i++)
        if (m.walls[i])
            for (Loc mine : mines)
                if (mine.distSq(m.loc(i)) <= 8) return "wall too close to a cheese mine";
    return "";
}

}  // namespace fish
