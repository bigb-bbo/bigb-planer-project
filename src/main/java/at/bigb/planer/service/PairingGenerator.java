package at.bigb.planer.service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Simple pairing generator with two strategies: GREEDY_SHUFFLE and BACKTRACK_RANDOM.
 * Designed to be drop‑in and used by services that need rounds of pairings.
 */
public class PairingGenerator {

    public enum Strategy { GREEDY_SHUFFLE, BACKTRACK_RANDOM }

    public static class Pair {
        public final String a;
        public final String b;
        public Pair(String a, String b) {
            if (a.compareTo(b) <= 0) { this.a = a; this.b = b; }
            else { this.a = b; this.b = a; }
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pair p)) return false; // pattern variable (Java 16+)
            return a.equals(p.a) && b.equals(p.b);
        }
        @Override public int hashCode() { return Objects.hash(a, b); }
        @Override public String toString() { return a + "-" + b; }
    }

    private final Strategy strategy;
    private final Random rnd;
    private final int greedyReshuffles;
    private final long backtrackTimeoutMillis;

    public PairingGenerator(Strategy strategy, Long seed, int greedyReshuffles, long backtrackTimeoutMillis) {
        this.strategy = strategy;
        this.rnd = (seed == null) ? ThreadLocalRandom.current() : new Random(seed);
        this.greedyReshuffles = Math.max(1, greedyReshuffles);
        this.backtrackTimeoutMillis = Math.max(1, backtrackTimeoutMillis);
    }

    public List<List<Pair>> generate(List<String> players, int rounds) {
        if (players == null) throw new IllegalArgumentException("players null");
        if (players.size() % 2 != 0) throw new IllegalArgumentException("Anzahl Spieler muss gerade sein (oder handle bye)");
        Map<Pair, Integer> history = new HashMap<>();
        List<List<Pair>> schedule = new ArrayList<>();
        for (int r = 0; r < rounds; r++) {
            List<Pair> round;
            if (strategy == Strategy.GREEDY_SHUFFLE) {
                round = generateRoundGreedyShuffle(players, history);
            } else {
                round = generateRoundBacktrack(players, history, backtrackTimeoutMillis);
                if (round == null) { // fallback auf greedy
                    round = generateRoundGreedyShuffle(players, history);
                }
            }
            for (Pair p : round) history.merge(p, 1, Integer::sum);
            schedule.add(round);
        }
        return schedule;
    }

    private List<Pair> generateRoundGreedyShuffle(List<String> players, Map<Pair, Integer> history) {
        List<Pair> best = null;
        int bestRepeats = Integer.MAX_VALUE;
        List<String> working = new ArrayList<>(players);
        for (int attempt = 0; attempt < greedyReshuffles; attempt++) {
            Collections.shuffle(working, rnd);
            List<Pair> pairs = new ArrayList<>();
            for (int i = 0; i < working.size(); i += 2) {
                pairs.add(new Pair(working.get(i), working.get(i + 1)));
            }
            int repeats = countRepeats(pairs, history);
            if (repeats < bestRepeats) {
                bestRepeats = repeats;
                best = new ArrayList<>(pairs);
                if (bestRepeats == 0) break;
            }
        }
        if (best == null) {
            List<Pair> pairs = new ArrayList<>();
            List<String> copy = new ArrayList<>(players);
            Collections.shuffle(copy, rnd);
            for (int i = 0; i < copy.size(); i += 2) pairs.add(new Pair(copy.get(i), copy.get(i+1)));
            best = pairs;
        }
        return best;
    }

    private int countRepeats(List<Pair> pairs, Map<Pair, Integer> history) {
        int c = 0;
        for (Pair p : pairs) c += history.getOrDefault(p, 0);
        return c;
    }

    private List<Pair> generateRoundBacktrack(List<String> players, Map<Pair, Integer> history, long timeoutMillis) {
        long deadline = System.nanoTime() + timeoutMillis * 1_000_000L;
        List<String> pool = new ArrayList<>(players);
        Collections.shuffle(pool, rnd);
        List<Pair> result = new ArrayList<>();
        boolean found = backtrackRecursive(pool, result, history, deadline);
        return found ? result : null;
    }

    private boolean backtrackRecursive(List<String> pool, List<Pair> current, Map<Pair, Integer> history, long deadline) {
        if (System.nanoTime() > deadline) return false;
        if (current.size() * 2 == pool.size()) return true;
        // find first unused index
        int first = -1;
        Set<String> used = current.stream().flatMap(p -> Arrays.stream(new String[]{p.a, p.b})).collect(Collectors.toSet());
        for (int i = 0; i < pool.size(); i++) {
            if (!used.contains(pool.get(i))) { first = i; break; }
        }
        if (first == -1) return true;
        String a = pool.get(first);
        List<Integer> candidates = new ArrayList<>();
        for (int j = first + 1; j < pool.size(); j++) {
            if (!used.contains(pool.get(j))) candidates.add(j);
        }
        candidates.sort(Comparator.comparingInt(j -> history.getOrDefault(new Pair(a, pool.get(j)), 0)));
        Collections.shuffle(candidates, rnd);
        for (int j : candidates) {
            Pair p = new Pair(a, pool.get(j));
            current.add(p);
            if (backtrackRecursive(pool, current, history, deadline)) return true;
            current.remove(current.size() - 1);
            if (System.nanoTime() > deadline) return false;
        }
        return false;
    }

    // --- New: support selecting a group of k players (e.g., 4-player combination) ---
    /**
     * Selects a group of size k from the available players according to configured strategy.
     * The freqLookup maps a candidate set of player names to the historical frequency (lower is better).
     */
    public List<String> selectGroup(List<String> players, int k, Function<Set<String>, Integer> freqLookup) {
        if (players == null) throw new IllegalArgumentException("players null");
        if (k <= 0 || k > players.size()) throw new IllegalArgumentException("invalid group size");
        if (strategy == Strategy.GREEDY_SHUFFLE) {
            return selectGroupGreedyShuffle(players, k, freqLookup);
        } else {
            List<String> result = selectGroupBacktrack(players, k, freqLookup, backtrackTimeoutMillis);
            if (result == null) return selectGroupGreedyShuffle(players, k, freqLookup);
            return result;
        }
    }

    private List<String> selectGroupGreedyShuffle(List<String> players, int k, Function<Set<String>, Integer> freqLookup) {
        List<String> best = null;
        int bestRepeats = Integer.MAX_VALUE;
        List<String> working = new ArrayList<>(players);
        for (int attempt = 0; attempt < greedyReshuffles; attempt++) {
            Collections.shuffle(working, rnd);
            List<String> candidate = working.subList(0, k);
            Set<String> key = new HashSet<>(candidate);
            int repeats = freqLookup.apply(key);
            if (repeats < bestRepeats) {
                bestRepeats = repeats;
                best = new ArrayList<>(candidate);
                if (bestRepeats == 0) break;
            }
        }
        if (best == null) {
            List<String> copy = new ArrayList<>(players);
            Collections.shuffle(copy, rnd);
            best = new ArrayList<>(copy.subList(0, k));
        }
        return best;
    }

    private List<String> selectGroupBacktrack(List<String> players, int k, Function<Set<String>, Integer> freqLookup, long timeoutMillis) {
        long deadline = System.nanoTime() + timeoutMillis * 1_000_000L;
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) indices.add(i);
        Collections.shuffle(indices, rnd);
        List<Integer> current = new ArrayList<>();
        boolean found = backtrackGroup(indices, k, 0, current, players, freqLookup, deadline);
        if (!found) return null;
        return current.stream().map(players::get).collect(Collectors.toList());
    }

    @SuppressWarnings({"unused", "RedundantSuppression"})
    private boolean backtrackGroup(List<Integer> indices, int k, int startPos, List<Integer> current, List<String> players, Function<Set<String>, Integer> freqLookup, long deadline) {
        if (System.nanoTime() > deadline) return false;
        if (current.size() == k) return true;
        for (int i = startPos; i < indices.size(); i++) {
            current.add(indices.get(i));
            // optional: early pruning by estimating frequency
            Set<String> key = current.stream().map(players::get).collect(Collectors.toSet());
            int freq = 0;
            try {
                freq = freqLookup.apply(key);
            } catch (Exception ignored) {
                // If the lookup fails for any reason, treat as unknown (freq=0)
            }
            // modest pruning: if frequency is extremely high we can skip this branch early
            // (keeps behavior conservative; threshold chosen high so only very frequent combos are pruned)
            if (freq > 1000) { // practically never, but avoids unused variable warning and allows future tuning
                current.remove(current.size() - 1);
                continue;
            }
            if (backtrackGroup(indices, k, i + 1, current, players, freqLookup, deadline)) return true;
            current.remove(current.size() - 1);
            if (System.nanoTime() > deadline) return false;
        }
        return false;
    }

    // convenience factory
    public static PairingGenerator defaultGreedy() {
        return new PairingGenerator(Strategy.GREEDY_SHUFFLE, null, 200, 200);
    }
}
