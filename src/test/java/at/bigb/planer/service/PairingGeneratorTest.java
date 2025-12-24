package at.bigb.planer.service;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@DisplayName("PairingGenerator Tests")
class PairingGeneratorTest {

    private List<String> testPlayers;

    @BeforeEach
    void setUp() {
        testPlayers = new ArrayList<>();
        for (int i = 1; i <= 10; i++) testPlayers.add("P" + i);
    }

    @Test
    @DisplayName("Should generate correct number of rounds and pairs per round")
    void testGenerateCorrectRounds() {
        PairingGenerator gen = PairingGenerator.defaultGreedy();
        List<List<PairingGenerator.Pair>> schedule = gen.generate(testPlayers, 5);
        assertEquals(5, schedule.size(), "Should generate 5 rounds");
        for (List<PairingGenerator.Pair> round : schedule) {
            assertEquals(5, round.size(), "With 10 players there should be 5 pairs per round");
        }
    }

    @Test
    @DisplayName("Should not always produce the same dominating pair when unique combos are exhausted")
    void testNoSinglePairDominates() {
        PairingGenerator gen = new PairingGenerator(PairingGenerator.Strategy.GREEDY_SHUFFLE, null, 500, 200);
        int rounds = 30;
        List<List<PairingGenerator.Pair>> schedule = gen.generate(testPlayers, rounds);
        Map<PairingGenerator.Pair, Long> counts = schedule.stream().flatMap(List::stream)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        long max = counts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        // For 10 players and 30 rounds the average occurrences per pair is around 3.3; ensure no pair exceeds a safe threshold
        assertTrue(max < 10, "No single pair should dominate excessively, max=" + max);
    }
}
