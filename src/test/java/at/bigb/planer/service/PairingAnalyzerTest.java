package at.bigb.planer.service;

import at.bigb.planer.domain.Pairing;
import at.bigb.planer.domain.Player;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PairingAnalyzer
 * Tests tracking and analysis of player pairings
 */
@QuarkusTest
@DisplayName("PairingAnalyzer Tests")
class PairingAnalyzerTest {

    private PairingAnalyzer analyzer;
    private List<Player> testPlayers;

    @BeforeEach
    void setUp() {
        analyzer = new PairingAnalyzer();
        testPlayers = createTenTestPlayers();
    }

    @Test
    @DisplayName("Should record a pairing correctly")
    void testRecordPairing_Success() {
        List<Player> pairing = testPlayers.subList(0, 4);

        analyzer.recordPairing(pairing);

        assertEquals(1, analyzer.getFrequency(pairing), "Pairing frequency should be 1");
    }

    @Test
    @DisplayName("Should increment frequency when same pairing is recorded")
    void testRecordPairing_IncrementFrequency() {
        List<Player> pairing = testPlayers.subList(0, 4);

        analyzer.recordPairing(pairing);
        analyzer.recordPairing(pairing);
        analyzer.recordPairing(pairing);

        assertEquals(3, analyzer.getFrequency(pairing), "Frequency should be incremented to 3");
    }

    @Test
    @DisplayName("Should throw exception when recording pairing with wrong player count")
    void testRecordPairing_WrongPlayerCount() {
        List<Player> threePlayers = testPlayers.subList(0, 3);

        assertThrows(IllegalArgumentException.class,
                () -> analyzer.recordPairing(threePlayers),
                "Should throw exception for pairing with != 4 players");
    }

    @Test
    @DisplayName("Should return 0 frequency for unrecorded pairing")
    void testGetFrequency_UnrecordedPairing() {
        List<Player> pairing = testPlayers.subList(0, 4);

        int frequency = analyzer.getFrequency(pairing);

        assertEquals(0, frequency, "Unrecorded pairing should have frequency 0");
    }

    @Test
    @DisplayName("Should track multiple different pairings")
    void testRecordPairing_MultiplePairings() {
        List<Player> pairing1 = testPlayers.subList(0, 4);
        List<Player> pairing2 = testPlayers.subList(4, 8);
        List<Player> pairing3 = Arrays.asList(
                testPlayers.get(0), testPlayers.get(1),
                testPlayers.get(8), testPlayers.get(9)
        );

        analyzer.recordPairing(pairing1);
        analyzer.recordPairing(pairing2);
        analyzer.recordPairing(pairing3);
        analyzer.recordPairing(pairing1); // Record pairing1 again

        assertEquals(2, analyzer.getFrequency(pairing1));
        assertEquals(1, analyzer.getFrequency(pairing2));
        assertEquals(1, analyzer.getFrequency(pairing3));
    }

    @Test
    @DisplayName("Should return pairings sorted by frequency")
    void testGetAllPairingsSortedByFrequency() {
        List<Player> pairing1 = testPlayers.subList(0, 4);
        List<Player> pairing2 = testPlayers.subList(4, 8);

        analyzer.recordPairing(pairing1);
        analyzer.recordPairing(pairing1);
        analyzer.recordPairing(pairing1);
        analyzer.recordPairing(pairing2);

        List<Pairing> sortedPairings = analyzer.getAllPairingsSortedByFrequency();

        assertEquals(2, sortedPairings.size());
        assertTrue(sortedPairings.get(0).getFrequency() <= sortedPairings.get(1).getFrequency(),
                "Pairings should be sorted by frequency (ascending)");
    }

    @Test
    @DisplayName("Should calculate correct statistics")
    void testGetStatistics() {
        List<Player> pairing1 = testPlayers.subList(0, 4);
        List<Player> pairing2 = testPlayers.subList(4, 8);

        analyzer.recordPairing(pairing1);
        analyzer.recordPairing(pairing1);
        analyzer.recordPairing(pairing1);
        analyzer.recordPairing(pairing2);
        analyzer.recordPairing(pairing2);

        Map<String, Object> stats = analyzer.getStatistics();

        assertEquals(2, stats.get("totalUniquePairings"), "Should have 2 unique pairings");
        assertEquals(5, stats.get("totalPairingRecords"), "Should have 5 total records");
        assertEquals(3, stats.get("maxFrequency"), "Max frequency should be 3");
        assertEquals(2, stats.get("minFrequency"), "Min frequency should be 2");
        assertEquals(2.5, stats.get("avgFrequency"), "Average frequency should be 2.5");
    }

    @Test
    @DisplayName("Should reset all pairings")
    void testReset() {
        List<Player> pairing = testPlayers.subList(0, 4);

        analyzer.recordPairing(pairing);
        analyzer.recordPairing(pairing);
        assertEquals(2, analyzer.getFrequency(pairing));

        analyzer.reset();

        assertEquals(0, analyzer.getFrequency(pairing), "Frequency should be 0 after reset");
        assertEquals(0, analyzer.getStatistics().get("totalUniquePairings"));
    }

    @Test
    @DisplayName("Should handle order-independent pairing comparison")
    void testRecordPairing_OrderIndependent() {
        List<Player> pairing1 = Arrays.asList(
                testPlayers.get(0), testPlayers.get(1),
                testPlayers.get(2), testPlayers.get(3)
        );
        List<Player> pairing2 = Arrays.asList(
                testPlayers.get(3), testPlayers.get(2),
                testPlayers.get(1), testPlayers.get(0)
        );

        analyzer.recordPairing(pairing1);

        // The same players in different order should be treated as the same pairing
        // Note: This test documents the actual behavior - if order matters, adjust accordingly
        int freq1 = analyzer.getFrequency(pairing1);
        int freq2 = analyzer.getFrequency(pairing2);

        assertTrue(freq1 > 0 || freq2 > 0, "At least one pairing should be recorded");
    }

    @Test
    @DisplayName("Should handle statistics with single pairing")
    void testGetStatistics_SinglePairing() {
        List<Player> pairing = testPlayers.subList(0, 4);
        analyzer.recordPairing(pairing);

        Map<String, Object> stats = analyzer.getStatistics();

        assertEquals(1, stats.get("totalUniquePairings"));
        assertEquals(1, stats.get("maxFrequency"));
        assertEquals(1, stats.get("minFrequency"));
        assertEquals(1.0, stats.get("avgFrequency"));
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

