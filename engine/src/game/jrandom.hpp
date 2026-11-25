// java.util.Random, bit for bit (including the Java 17+ bounded nextInt(origin, bound)).
#pragma once

#include <cstdint>

namespace fish {

class JRandom {
public:
    explicit JRandom(int64_t seed = 0) { setSeed(seed); }

    void setSeed(int64_t seed) { state_ = (uint64_t(seed) ^ MULT) & MASK; }

    int32_t nextInt() { return next(32); }

    int32_t nextInt(int32_t bound) {
        int32_t r = next(31);
        int32_t m = bound - 1;
        if ((bound & m) == 0) return int32_t((int64_t(bound) * int64_t(r)) >> 31);
        for (int32_t u = r; int32_t(uint32_t(u) - uint32_t(r = u % bound) + uint32_t(m)) < 0; u = next(31)) {
        }
        return r;
    }

    // RandomSupport.boundedNextInt, the default behind Random.nextInt(origin, bound).
    int32_t nextInt(int32_t origin, int32_t bound) {
        int32_t r = nextInt();
        if (origin < bound) {
            int32_t n = int32_t(uint32_t(bound) - uint32_t(origin));
            int32_t m = n - 1;
            if ((n & m) == 0) {
                r = int32_t(uint32_t(r & m) + uint32_t(origin));
            } else if (n > 0) {
                for (int32_t u = int32_t(uint32_t(r) >> 1);; u = int32_t(uint32_t(nextInt()) >> 1)) {
                    r = u % n;
                    if (int32_t(uint32_t(u) + uint32_t(m) - uint32_t(r)) >= 0) break;
                }
                r = int32_t(uint32_t(r) + uint32_t(origin));
            } else {
                while (r < origin || r >= bound) r = nextInt();
            }
        }
        return r;
    }

    float nextFloat() { return float(next(24)) * 0x1.0p-24f; }

    double nextDouble() { return double((int64_t(next(26)) << 27) + next(27)) * 0x1.0p-53; }

private:
    static constexpr uint64_t MULT = 0x5DEECE66DULL;
    static constexpr uint64_t MASK = (1ULL << 48) - 1;
    uint64_t state_;

    int32_t next(int bits) {
        state_ = (state_ * MULT + 0xBULL) & MASK;
        return int32_t(int64_t(state_) >> (48 - bits));
    }
};

}  // namespace fish
