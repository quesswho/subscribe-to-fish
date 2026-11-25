// A .map26 file (battlecode.schema.GameMap flatbuffer) and LiveMap's validity rules.
#pragma once

#include <string>
#include <vector>

#include "types.hpp"

namespace fish {

struct InitialBody {
    int id;
    Team team;
    UnitType type;
    Loc loc;
    Dir dir;
    int chirality;
};

struct CatPath {
    int id;
    std::vector<int> waypoints;  // location indices
};

struct GameMap {
    std::string name;
    int width = 0, height = 0;
    Symmetry symmetry = ROTATIONAL;
    int seed = 0;
    std::vector<uint8_t> walls, dirt, mines;
    std::vector<int> cheese;
    std::vector<CatPath> cats;
    std::vector<InitialBody> bodies;  // sorted by id, as LiveMap does

    int index(Loc l) const { return l.x + l.y * width; }
    Loc loc(int i) const { return {i % width, i / width}; }
    bool onMap(Loc l) const { return l.x >= 0 && l.y >= 0 && l.x < width && l.y < height; }
};

// Throws std::runtime_error on malformed input.
GameMap loadMap(const std::string& path);
GameMap parseMap(const std::vector<uint8_t>& bytes);

// LiveMap.assertIsValid: returns an empty string when valid, else the reason.
std::string validateMap(const GameMap& map);

}  // namespace fish
