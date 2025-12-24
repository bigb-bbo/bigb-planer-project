package at.bigb.planer.service;

import at.bigb.planer.domain.Player;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Legacy tests adapted from PairingAlgorithm to PairingGenerator behavior
 */
@QuarkusTest
@DisplayName("PairingGenerator (legacy PairingAlgorithm) Tests")
class PairingAlgorithmTest {

    private PairingAnalyzer analyzer;
    private PairingGenerator generator;
    private List<Player> testPlayers;

    @BeforeEach
    void setUp() {
        analyzer = new PairingAnalyzer();
        generator = new PairingGenerator(PairingGenerator.Strategy.GREEDY_SHUFFLE, null, 200, 200);
        testPlayers = createTenTestPlayers();
    }

    @Test
    @DisplayName("Should select exactly 4 players for a round")
    void testSelectPlayersForRound_ReturnsFourPlayers() {
        List<String> names = testPlayers.stream().map(Player::getName).collect(Collectors.toList());
        List<String> selected = generator.selectGroup(names, 4, (set) -> {
            List<Player> plist = testPlayers.stream().filter(p -> set.contains(p.getName())).collect(Collectors.toList());
            return analyzer.getFrequency(plist);
        });

        assertEquals(4, selected.size(), "Should select exactly 4 players");
        assertTrue(names.containsAll(selected), "All selected players should be from available players");
    }

    @Test
    @DisplayName("Should prefer combinations with lower frequency")
    void testSelectPlayersForRound_MinimizeFrequency() {
        // Record the same combination twice in analyzer
        List<Player> fixedPlayers = testPlayers.subList(0, 4);
        analyzer.recordPairing(fixedPlayers);
        analyzer.recordPairing(fixedPlayers);

        List<String> names = testPlayers.stream().map(Player::getName).collect(Collectors.toList());
        for (int i = 0; i < 3; i++) {
            List<String> selected = generator.selectGroup(names, 4, (set) -> {
                List<Player> plist = testPlayers.stream().filter(p -> set.contains(p.getName())).collect(Collectors.toList());
                return analyzer.getFrequency(plist);
            });
            // selected set should not equal fixedPlayers names if possible
            Set<String> selectedSet = new HashSet<>(selected);
            Set<String> fixedSet = fixedPlayers.stream().map(Player::getName).collect(Collectors.toSet());
            assertNotEquals(fixedSet, selectedSet, "Should avoid selecting the same combination again if possible");
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
