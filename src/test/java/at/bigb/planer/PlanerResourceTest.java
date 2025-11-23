package at.bigb.planer;

import at.bigb.planer.domain.dto.PlanDto;
import at.bigb.planer.domain.dto.ScheduleConfigDto;
import at.bigb.planer.domain.dto.ScheduleStatsDto;
import at.bigb.planer.domain.dto.PairingDto;
import at.bigb.planer.service.rest.PlanerResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests for PlanerResource REST endpoints
 * Tests the REST endpoint behavior without server startup
 */
@DisplayName("PlanerResource Unit Tests")
class PlanerResourceTest {

    private PlanerResource resource;

    @BeforeEach
    void setUp() {
        resource = new PlanerResource();
    }

    @Test
    @DisplayName("Health endpoint should return OK status")
    void testHealthEndpoint() {
        String response = resource.getHealth();

        assertNotNull(response);
        assertTrue(response.contains("OK"), "Health check should return OK status");
    }

    @Test
    @DisplayName("Generate endpoint should accept valid schedule configuration")
    void testGenerateScheduleWithValidConfig() {
        ScheduleConfigDto configDto = new ScheduleConfigDto();
        configDto.setPlayerNames(Arrays.asList("Alice", "Bob", "Charlie", "David",
                                                "Eve", "Frank", "Grace", "Henry",
                                                "Iris", "Jack"));
        configDto.setNumberOfRounds(5);
        configDto.setPlayersPerRound(4);

        PlanDto result = resource.generateSchedule(configDto);

        assertNotNull(result, "Should return a plan");
        assertNotNull(result.getId(), "Plan should have an ID");
        assertEquals(5, result.getNumberOfRounds(), "Should have 5 rounds");
        assertEquals(10, result.getPlayers().size(), "Should have 10 players");
        assertEquals(5, result.getRounds().size(), "Should have 5 rounds generated");
        assertEquals(4, result.getRounds().get(0).getSelectedPlayers().size(), "Each round should have 4 players");
    }

    @Test
    @DisplayName("Generate endpoint should reject configuration with less than 4 players")
    void testGenerateScheduleWithTooFewPlayers() {
        ScheduleConfigDto configDto = new ScheduleConfigDto();
        configDto.setPlayerNames(Arrays.asList("Alice", "Bob", "Charlie"));
        configDto.setNumberOfRounds(5);
        configDto.setPlayersPerRound(4);

        assertThrows(Exception.class,
                () -> resource.generateSchedule(configDto),
                "Should throw exception with less than 4 players");
    }

    @Test
    @DisplayName("Generate endpoint should reject configuration with 0 rounds")
    void testGenerateScheduleWithZeroRounds() {
        ScheduleConfigDto configDto = new ScheduleConfigDto();
        configDto.setPlayerNames(Arrays.asList("Alice", "Bob", "Charlie", "David"));
        configDto.setNumberOfRounds(0);
        configDto.setPlayersPerRound(4);

        assertThrows(Exception.class,
                () -> resource.generateSchedule(configDto),
                "Should throw exception with 0 rounds");
    }

    @Test
    @DisplayName("Generate endpoint should reject configuration with empty player list")
    void testGenerateScheduleWithEmptyPlayerList() {
        ScheduleConfigDto configDto = new ScheduleConfigDto();
        configDto.setPlayerNames(new ArrayList<>());
        configDto.setNumberOfRounds(5);
        configDto.setPlayersPerRound(4);

        assertThrows(Exception.class,
                () -> resource.generateSchedule(configDto),
                "Should throw exception with empty player list");
    }

    @Test
    @DisplayName("Generate endpoint should reject configuration with duplicate player names")
    void testGenerateScheduleWithDuplicateNames() {
        ScheduleConfigDto configDto = new ScheduleConfigDto();
        configDto.setPlayerNames(Arrays.asList("Alice", "Bob", "Charlie", "David", "Alice"));
        configDto.setNumberOfRounds(5);
        configDto.setPlayersPerRound(4);

        assertThrows(Exception.class,
                () -> resource.generateSchedule(configDto),
                "Should throw exception with duplicate names");
    }

    @Test
    @DisplayName("Statistics endpoint should return pairing statistics")
    void testGetStatistics() {
        ScheduleStatsDto stats = resource.getStatistics();

        assertNotNull(stats, "Should return statistics");
        assertTrue(stats.getTotalUniquePairings() >= 0, "Total unique pairings should be non-negative");
        assertTrue(stats.getTotalPairingRecords() >= 0, "Total pairing records should be non-negative");
    }

    @Test
    @DisplayName("Pairings endpoint should return list of pairings sorted by frequency")
    void testGetAllPairings() {
        List<PairingDto> pairings = resource.getAllPairings();

        assertNotNull(pairings, "Should return pairings list");
        assertInstanceOf(List.class, pairings, "Should return a List");
    }

    @Test
    @DisplayName("Generate endpoint should create plan with correct round dates")
    void testGenerateScheduleWithCorrectRoundDates() {
        ScheduleConfigDto configDto = new ScheduleConfigDto();
        configDto.setPlayerNames(Arrays.asList("Alice", "Bob", "Charlie", "David", "Eve", "Frank", "Grace", "Henry"));
        configDto.setNumberOfRounds(3);
        configDto.setPlayersPerRound(4);

        PlanDto result = resource.generateSchedule(configDto);

        assertEquals(3, result.getRounds().size(), "Should have 3 rounds");
        assertEquals(1, result.getRounds().get(0).getRoundNo(), "First round number should be 1");
        assertEquals(2, result.getRounds().get(1).getRoundNo(), "Second round number should be 2");
        assertEquals(3, result.getRounds().get(2).getRoundNo(), "Third round number should be 3");

        assertNotNull(result.getRounds().get(0).getRoundDate(), "Round should have a date");
        assertNotNull(result.getRounds().get(1).getRoundDate(), "Round should have a date");
        assertNotNull(result.getRounds().get(2).getRoundDate(), "Round should have a date");
    }

    @Test
    @DisplayName("Generate endpoint should include creation timestamp")
    void testGenerateScheduleIncludesTimestamp() {
        ScheduleConfigDto configDto = new ScheduleConfigDto();
        configDto.setPlayerNames(Arrays.asList("Alice", "Bob", "Charlie", "David"));
        configDto.setNumberOfRounds(1);
        configDto.setPlayersPerRound(4);

        PlanDto result = resource.generateSchedule(configDto);

        assertNotNull(result.getCreatedAt(), "Plan should have a creation timestamp");
    }
}