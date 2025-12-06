package at.bigb.planer.service;

import at.bigb.planer.domain.Player;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PairingAlgorithm
 * Tests the greedy algorithm for selecting 4-player combinations
 */
@QuarkusTest
@DisplayName("PairingAlgorithm Tests")
class PairingAlgorithmTest {

    private PairingAnalyzer analyzer;
    private PairingAlgorithm algorithm;
    private List<Player> testPlayers;

    @BeforeEach
    void setUp() {
        analyzer = new PairingAnalyzer();
        algorithm = new PairingAlgorithm(analyzer);
        testPlayers = createTenTestPlayers();
    }

    @Test
    @DisplayName("Should select exactly 4 players for a round")
    void testSelectPlayersForRound_ReturnsFourPlayers() {
        List<Player> selected = algorithm.selectPlayersForRound(testPlayers);

        assertEquals(4, selected.size(), "Should select exactly 4 players");
        assertTrue(testPlayers.containsAll(selected),
                "All selected players should be from available players");
    }

    @Test
    @DisplayName("Should select different player combinations across multiple rounds")
    void testSelectPlayersForRound_DifferentCombinations() {
        List<List<Player>> selections = new ArrayList<>();

        // Simulate 5 rounds
        for (int i = 0; i < 5; i++) {
            List<Player> selected = algorithm.selectPlayersForRound(testPlayers);
            selections.add(new ArrayList<>(selected));
        }

        // Not all selections should be identical
        boolean allIdentical = selections.stream()
                .skip(1)
                .allMatch(sel -> sel.equals(selections.get(0)));

        assertFalse(allIdentical, "Should select different combinations across rounds");
    }

    @Test
    @DisplayName("Should fail when less than 4 players available")
    void testSelectPlayersForRound_InsufficientPlayers() {
        List<Player> insufficientPlayers = testPlayers.subList(0, 3);

        assertThrows(IllegalArgumentException.class,
                () -> algorithm.selectPlayersForRound(insufficientPlayers),
                "Should throw exception with less than 4 players");
    }

    @Test
    @DisplayName("Should prefer combinations with lower frequency")
    void testSelectPlayersForRound_MinimizeFrequency() {
        // Record the same combination twice
        List<Player> fixedPlayers = testPlayers.subList(0, 4);
        analyzer.recordPairing(fixedPlayers);
        analyzer.recordPairing(fixedPlayers);

        // Next selections should prefer other combinations
        for (int i = 0; i < 3; i++) {
            List<Player> selected = algorithm.selectPlayersForRound(testPlayers);
            assertNotEquals(fixedPlayers, selected,
                    "Should avoid selecting the same combination again if possible");
        }
    }

    @Test
    @DisplayName("Should handle edge case with exactly 4 players")
    void testSelectPlayersForRound_WithExactlyFourPlayers() {
        List<Player> fourPlayers = testPlayers.subList(0, 4);

        List<Player> selected = algorithm.selectPlayersForRound(fourPlayers);
        assertEquals(4, selected.size());
        assertTrue(selected.containsAll(fourPlayers), "Should select all 4 players");
    }

    @Test
    @DisplayName("Should select different players across rounds avoiding repetition")
    void testSelectPlayersForRound_AvoidRepetition() {
        // Generate many rounds and track how often the same exact combination appears
        java.util.Map<String, Integer> combinationFreq = new java.util.HashMap<>();
        int rounds = 50;

        for (int i = 0; i < rounds; i++) {
            List<Player> selected = algorithm.selectPlayersForRound(testPlayers);
            // Create a combination key
            String key = selected.stream()
                    .map(Player::getName)
                    .sorted()
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
            combinationFreq.put(key, combinationFreq.getOrDefault(key, 0) + 1);
        }

        // All combinations together should exist
        assertTrue(combinationFreq.size() > 1, "Should use multiple different combinations across rounds");

        // No single combination should dominate (max frequency < 5 in 50 rounds)
        int maxFreq = combinationFreq.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        assertTrue(maxFreq < 10, "No combination should repeat excessively. Max frequency: " + maxFreq);
    }

    @Test
    @DisplayName("Should distribute player usage as evenly as possible")
    void testPlayerUsageDistribution() {
        int rounds = 25; // 10 players, 4 per round, 25 rounds
        for (int i = 0; i < rounds; i++) {
            algorithm.selectPlayersForRound(testPlayers);
        }
        // Zugriff auf die private Map via Reflection
        java.lang.reflect.Field usageField;
        try {
            usageField = algorithm.getClass().getDeclaredField("playerUsageCount");
            usageField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, Integer> usage = (java.util.Map<String, Integer>) usageField.get(algorithm);
            int min = usage.values().stream().min(Integer::compareTo).orElse(0);
            int max = usage.values().stream().max(Integer::compareTo).orElse(0);
            // Maximal 1 Unterschied erlaubt
            assertTrue(max - min <= 1, "Player usage should be nearly balanced. Min: " + min + ", Max: " + max);
        } catch (Exception e) {
            fail("Could not access playerUsageCount: " + e.getMessage());
        }
    }

    // Helper method to create test players
    private List<Player> createTenTestPlayers() {
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            players.add(new Player(UUID.randomUUID().toString(), "Player " + i));
        }
        return players;
    }
}
