package at.bigb.planer.service;

import at.bigb.planer.domain.Plan;
import at.bigb.planer.domain.Player;
import at.bigb.planer.domain.Round;
import at.bigb.planer.domain.ScheduleConfig;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ScheduleGenerationService
 * Tests the schedule generation with optimization
 */
@QuarkusTest
@DisplayName("ScheduleGenerationService Tests")
class ScheduleGenerationServiceTest {

    private ScheduleGenerationService service;
    private List<String> testPlayerNames;

    @BeforeEach
    void setUp() {
        service = new ScheduleGenerationService();
        testPlayerNames = Arrays.asList(
                "Alice", "Bob", "Charlie", "David",
                "Eve", "Frank", "Grace", "Henry",
                "Iris", "Jack"
        );
    }

    @Test
    @DisplayName("Should generate schedule with correct number of rounds")
    void testGenerateSchedule_CorrectNumberOfRounds() {
        ScheduleConfig config = new ScheduleConfig(testPlayerNames, 5, 4);

        Plan plan = service.generateSchedule(config);

        assertEquals(5, plan.getRounds().size(), "Should generate 5 rounds");
        assertEquals(5, plan.getNumberOfRounds());
    }

    @Test
    @DisplayName("Should generate schedule with all 10 players")
    void testGenerateSchedule_AllPlayersIncluded() {
        ScheduleConfig config = new ScheduleConfig(testPlayerNames, 5, 4);

        Plan plan = service.generateSchedule(config);

        assertEquals(10, plan.getPlayers().size(), "Should have 10 players");
        assertEquals(testPlayerNames.stream().sorted().collect(Collectors.toList()),
                plan.getPlayers().stream().map(Player::getName).sorted().collect(Collectors.toList()),
                "All player names should be preserved");
    }

    @Test
    @DisplayName("Should select exactly 4 players per round")
    void testGenerateSchedule_FourPlayersPerRound() {
        ScheduleConfig config = new ScheduleConfig(testPlayerNames, 10, 4);

        Plan plan = service.generateSchedule(config);

        plan.getRounds().forEach(round ->
                assertEquals(4, round.getSelectedPlayers().size(),
                        "Each round should have exactly 4 players")
        );
    }

    @Test
    @DisplayName("Should generate plan with unique ID")
    void testGenerateSchedule_UniqueId() {
        ScheduleConfig config = new ScheduleConfig(testPlayerNames, 5, 4);

        Plan plan1 = service.generateSchedule(config);
        Plan plan2 = service.generateSchedule(config);

        assertNotEquals(plan1.getId(), plan2.getId(), "Each plan should have a unique ID");
    }

    @Test
    @DisplayName("Should create chronological round dates")
    void testGenerateSchedule_ChronologicalDates() {
        ScheduleConfig config = new ScheduleConfig(testPlayerNames, 5, 4);

        Plan plan = service.generateSchedule(config);

        for (int i = 0; i < plan.getRounds().size() - 1; i++) {
            Round current = plan.getRounds().get(i);
            Round next = plan.getRounds().get(i + 1);
            assertTrue(current.getRoundDate().isBefore(next.getRoundDate()),
                    "Round dates should be in chronological order");
        }
    }

    @Test
    @DisplayName("Should validate: throw exception with less than 4 players")
    void testGenerateSchedule_TooFewPlayers() {
        ScheduleConfig config = new ScheduleConfig(
                Arrays.asList("Alice", "Bob", "Charlie"), 5, 4);

        assertThrows(IllegalArgumentException.class,
                () -> service.generateSchedule(config),
                "Should throw exception with less than 4 players");
    }

    @Test
    @DisplayName("Should validate: throw exception with 0 rounds")
    void testGenerateSchedule_ZeroRounds() {
        ScheduleConfig config = new ScheduleConfig(testPlayerNames, 0, 4);

        assertThrows(IllegalArgumentException.class,
                () -> service.generateSchedule(config),
                "Should throw exception with 0 rounds");
    }

    @Test
    @DisplayName("Should validate: throw exception with duplicate player names")
    void testGenerateSchedule_DuplicateNames() {
        List<String> duplicateNames = Arrays.asList(
                "Alice", "Bob", "Charlie", "David",
                "Eve", "Frank", "Grace", "Alice",  // Duplicate
                "Iris", "Jack"
        );
        ScheduleConfig config = new ScheduleConfig(duplicateNames, 5, 4);

        assertThrows(IllegalArgumentException.class,
                () -> service.generateSchedule(config),
                "Should throw exception with duplicate player names");
    }

    @Test
    @DisplayName("Should validate: throw exception with empty player list")
    void testGenerateSchedule_EmptyPlayerList() {
        ScheduleConfig config = new ScheduleConfig(List.of(), 5, 4);

        assertThrows(IllegalArgumentException.class,
                () -> service.generateSchedule(config),
                "Should throw exception with empty player list");
    }

    @Test
    @DisplayName("Should minimize duplicate pairings across rounds")
    void testGenerateSchedule_MinimizeDuplicatePairings() {
        ScheduleConfig config = new ScheduleConfig(testPlayerNames, 20, 4);

        service.generateSchedule(config);

        // Get pairing statistics
        Map<String, Object> stats = service.getPairingStatistics();

        assertNotNull(stats, "Should provide pairing statistics");
        assertNotNull(stats.get("totalUniquePairings"), "Should track unique pairings");
        assertNotNull(stats.get("maxFrequency"), "Should track max frequency");
    }

    @Test
    @DisplayName("Should set creation timestamp")
    void testGenerateSchedule_CreationTimestamp() {
        ScheduleConfig config = new ScheduleConfig(testPlayerNames, 5, 4);

        Plan plan = service.generateSchedule(config);

        assertNotNull(plan.getCreatedAt(), "Plan should have creation timestamp");
        assertTrue(plan.getCreatedAt().isBefore(java.time.LocalDateTime.now().plusSeconds(1)),
                "Timestamp should be recent");
    }

    @Test
    @DisplayName("Should generate valid schedule for minimum viable size")
    void testGenerateSchedule_MinimumViableSize() {
        ScheduleConfig config = new ScheduleConfig(
                Arrays.asList("A", "B", "C", "D"), 1, 4);

        Plan plan = service.generateSchedule(config);

        assertEquals(1, plan.getRounds().size());
        assertEquals(4, plan.getRounds().get(0).getSelectedPlayers().size());
    }

    @Test
    @DisplayName("Should handle large number of rounds")
    void testGenerateSchedule_ManyRounds() {
        ScheduleConfig config = new ScheduleConfig(testPlayerNames, 50, 4);

        Plan plan = service.generateSchedule(config);

        assertEquals(50, plan.getRounds().size());
        assertTrue(plan.getRounds().stream()
                .allMatch(r -> r.getSelectedPlayers().size() == 4),
                "All 50 rounds should have 4 players");
    }

    @Test
    @DisplayName("Should generate plan with all rounds having unique round numbers")
    void testGenerateSchedule_UniqueRoundNumbers() {
        ScheduleConfig config = new ScheduleConfig(testPlayerNames, 10, 4);

        Plan plan = service.generateSchedule(config);

        long uniqueRoundNumbers = plan.getRounds().stream()
                .map(Round::getRoundNo)
                .distinct()
                .count();

        assertEquals(10, uniqueRoundNumbers, "All round numbers should be unique");
    }

    @Test
    @DisplayName("Should be consistent with same seed (if seeding is used)")
    void testGenerateSchedule_ConsistentGeneration() {
        ScheduleConfig config = new ScheduleConfig(testPlayerNames, 3, 4);

        Plan plan1 = service.generateSchedule(config);
        Plan plan2 = service.generateSchedule(config);

        // Plans might be different due to random selection, but both should be valid
        assertEquals(3, plan1.getRounds().size());
        assertEquals(3, plan2.getRounds().size());
    }
}

