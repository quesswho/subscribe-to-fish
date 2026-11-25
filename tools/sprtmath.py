"""Statistics for game pairs: Elo estimates and the generalised SPRT (as in fishtest).

A pair is one opening (map, seed) played twice with the sides swapped; its score is the candidate's wins / 2, so
0, 0.5 or 1. Battlecode games always have a winner, so there are no draws to model.
"""
import math


def expected(elo):
    return 1 / (1 + 10 ** (-elo / 400))


def elo(score):
    score = min(max(score, 1e-6), 1 - 1e-6)
    return -400 * math.log10(1 / score - 1)


def stats(pairs):
    """pairs: counts [n0, n1, n2] of pairs the candidate won 0, 1 or 2 games of. Returns (n, mean, variance)."""
    n = sum(pairs)
    if n == 0:
        return 0, 0.5, 0.0
    mean = (0.5 * pairs[1] + 1.0 * pairs[2]) / n
    var = (pairs[0] * mean ** 2 + pairs[1] * (0.5 - mean) ** 2 + pairs[2] * (1 - mean) ** 2) / n
    return n, mean, var


def elo_interval(pairs, z=1.96):
    """Elo estimate and a 95% interval from the pair scores."""
    n, mean, var = stats(pairs)
    if n == 0:
        return 0.0, -math.inf, math.inf
    half = z * math.sqrt(var / n) if n > 1 else 0.5
    return elo(mean), elo(mean - half), elo(mean + half)


MIN_PAIRS = 20


def llr(pairs, elo0, elo1):
    """Generalised SPRT log likelihood ratio of H1 (Elo = elo1) against H0 (Elo = elo0).

    The variance is estimated with one extra pair of each outcome, so a short run where every pair went the same
    way cannot look certain."""
    n, mean, _ = stats(pairs)
    _, _, var = stats([c + 1 for c in pairs])
    s0, s1 = expected(elo0), expected(elo1)
    return n * (s1 - s0) * (2 * mean - s0 - s1) / (2 * var)


def bounds(alpha, beta):
    return math.log(beta / (1 - alpha)), math.log((1 - beta) / alpha)


# ---------------------------------------------------------------- paired mode (tools/sprt --ref)
#
# The candidate and a reference both play the same opponent on the same openings. Per opening the difference of
# their pair scores is d in {-1, -0.5, 0, 0.5, 1}; the test runs on t = (d + 1) / 2 in {0, .25, .5, .75, 1}
# (pentanomial, as fishtest's game pairs). Openings both bots play alike give t = 0.5 and only shrink the variance.

PENTA = (0.0, 0.25, 0.5, 0.75, 1.0)


def penta_stats(counts):
    """counts: openings with d = -1, -0.5, 0, 0.5, 1. Returns (n, mean of t, variance of t)."""
    n = sum(counts)
    if n == 0:
        return 0, 0.5, 0.0
    mean = sum(c * t for c, t in zip(counts, PENTA)) / n
    var = sum(c * (t - mean) ** 2 for c, t in zip(counts, PENTA)) / n
    return n, mean, var


def paired_hypotheses(ref_score, elo0, elo1):
    """H0 and H1 means of t: the candidate elo0 or elo1 Elo above the reference, both measured against the
    opponent at the reference's score there (so elo0 = 0 is t = 0.5 exactly)."""
    r = min(max(ref_score, 1e-3), 1 - 1e-3)
    e = elo(r)
    return 0.5 + (expected(e + elo0) - r) / 2, 0.5 + (expected(e + elo1) - r) / 2


def penta_llr(counts, s0, s1):
    """GSPRT log likelihood ratio on t; the variance is estimated with one extra opening of each outcome."""
    n, mean, _ = penta_stats(counts)
    _, _, var = penta_stats([c + 1 for c in counts])
    return n * (s1 - s0) * (2 * mean - s0 - s1) / (2 * var)


def paired_interval(counts, ref_score, z=1.96):
    """The difference of scores (candidate - reference) with a 95% interval, and the same as Elo against the
    opponent at the reference's score."""
    n, mean, var = penta_stats(counts)
    d = 2 * mean - 1
    half = 2 * z * math.sqrt(var / max(1, n - 1)) if n > 1 else 1.0
    r = min(max(ref_score, 1e-6), 1 - 1e-6)
    to_elo = lambda x: elo(min(max(r + x, 1e-6), 1 - 1e-6)) - elo(r)  # noqa: E731
    return d, d - half, d + half, to_elo(d), to_elo(d - half), to_elo(d + half)
