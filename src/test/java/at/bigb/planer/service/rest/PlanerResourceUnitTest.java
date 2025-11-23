package at.bigb.planer.service.rest;

import at.bigb.planer.domain.dto.PlanDto;
import at.bigb.planer.domain.dto.PlayerDto;
import at.bigb.planer.domain.dto.ScheduleConfigDto;
import at.bigb.planer.domain.dto.ScheduleStatsDto;
import at.bigb.planer.domain.dto.PairingDto;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PlanerResource
 * Tests the REST endpoint error handling and response mapping
 */
@DisplayName("PlanerResource Unit Tests")
class PlanerResourceUnitTest {

    private PlanerResource resource;

    @BeforeEach
    void setUp() {
        resource = new PlanerResource();
    }

    @Test
    @DisplayName("Health endpoint should return OK status")
    void testGetHealthEndpoint() {
        String response = resource.getHealth();

        assertNotNull(response);
        assertTrue(response.contains("status"));
        assertTrue(response.contains("OK"));
    }

    @Test
    @DisplayName("Generate schedule should accept valid configuration")
    void testGenerateScheduleWithValidConfig() {
        ScheduleConfigDto configDto = new ScheduleConfigDto();
        configDto.setPlayerNames(Arrays.asList("Alice", "Bob", "Charlie", "David",
                                                "Eve", "Frank", "Grace", "Henry",
                                                "Iris", "Jack"));
        configDto.setNumberOfRounds(5);
        configDto.setPlayersPerRound(4);

        PlanDto result = resource.generateSchedule(configDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(5, result.getNumberOfRounds());
        assertEquals(10, result.getPlayers().size());
        assertEquals(5, result.getRounds().size());
    }

    @Test
    @DisplayName("Generate schedule should reject configuration with too few players")
    void testGenerateScheduleRejectsTooFewPlayers() {
        ScheduleConfigDto configDto = new ScheduleConfigDto();
        configDto.setPlayerNames(Arrays.asList("Alice", "Bob", "Charlie"));
        configDto.setNumberOfRounds(5);
        configDto.setPlayersPerRound(4);

        assertThrows(BadRequestException.class,
                () -> resource.generateSchedule(configDto),
                "Should throw BadRequestException with less than 4 players");
    }

    @Test
    @DisplayName("Generate schedule should reject configuration with 0 rounds")
    void testGenerateScheduleRejectsZeroRounds() {
        ScheduleConfigDto configDto = new ScheduleConfigDto();
        configDto.setPlayerNames(Arrays.asList("Alice", "Bob", "Charlie", "David"));
        configDto.setNumberOfRounds(0);
        configDto.setPlayersPerRound(4);

        assertThrows(BadRequestException.class,
                () -> resource.generateSchedule(configDto),
                "Should throw BadRequestException with 0 rounds");
    }

    @Test
    @DisplayName("Generate schedule should reject configuration with duplicate names")
    void testGenerateScheduleRejectsDuplicateNames() {
        ScheduleConfigDto configDto = new ScheduleConfigDto();
        configDto.setPlayerNames(Arrays.asList("Alice", "Bob", "Charlie", "David", "Alice"));
        configDto.setNumberOfRounds(5);
        configDto.setPlayersPerRound(4);

        assertThrows(BadRequestException.class,
                () -> resource.generateSchedule(configDto),
                "Should throw BadRequestException with duplicate names");
    }

    @Test
    @DisplayName("Generate schedule should reject empty player list")
    void testGenerateScheduleRejectsEmptyPlayerList() {
        ScheduleConfigDto configDto = new ScheduleConfigDto();
        configDto.setPlayerNames(new ArrayList<>());
        configDto.setNumberOfRounds(5);
        configDto.setPlayersPerRound(4);

        assertThrows(BadRequestException.class,
                () -> resource.generateSchedule(configDto),
                "Should throw BadRequestException with empty player list");
    }

    @Test
    @DisplayName("Get statistics should return valid stats")
    void testGetStatistics() {
        ScheduleStatsDto stats = resource.getStatistics();

        assertNotNull(stats);
        assertTrue(stats.getTotalUniquePairings() >= 0);
        assertTrue(stats.getTotalPairingRecords() >= 0);
    }

    @Test
    @DisplayName("Get all pairings should return list")
    void testGetAllPairings() {
        List<PairingDto> pairings = resource.getAllPairings();

        assertNotNull(pairings);
        assertInstanceOf(List.class, pairings);
    }

    @Test
    @DisplayName("Generated schedule should have all required fields")
    void testGenerateScheduleCompleteResponse() {
        ScheduleConfigDto configDto = new ScheduleConfigDto();
        configDto.setPlayerNames(Arrays.asList("A", "B", "C", "D"));
        configDto.setNumberOfRounds(1);
        configDto.setPlayersPerRound(4);

        PlanDto result = resource.generateSchedule(configDto);

        assertNotNull(result.getId(), "Plan should have ID");
        assertNotNull(result.getPlayers(), "Plan should have players");
        assertNotNull(result.getRounds(), "Plan should have rounds");
        assertNotNull(result.getCreatedAt(), "Plan should have creation timestamp");
        assertEquals(1, result.getNumberOfRounds());
    }

    @Test
    @DisplayName("Generated schedule should have correct round structure")
    void testGenerateScheduleRoundStructure() {
        ScheduleConfigDto configDto = new ScheduleConfigDto();
        configDto.setPlayerNames(Arrays.asList("Alice", "Bob", "Charlie", "David", "Eve", "Frank", "Grace", "Henry"));
        configDto.setNumberOfRounds(3);
        configDto.setPlayersPerRound(4);

        PlanDto result = resource.generateSchedule(configDto);

        assertEquals(3, result.getRounds().size());
        for (int i = 0; i < result.getRounds().size(); i++) {
            assertEquals(i + 1, result.getRounds().get(i).getRoundNo());
            assertEquals(4, result.getRounds().get(i).getSelectedPlayers().size());
            assertNotNull(result.getRounds().get(i).getRoundDate());
        }
    }

    @Test
    @DisplayName("Generate schedule should use all provided players")
    void testGenerateScheduleIncludesAllPlayers() {
        List<String> playerNames = Arrays.asList("Alice", "Bob", "Charlie", "David",
                                                  "Eve", "Frank", "Grace", "Henry",
                                                  "Iris", "Jack");
        ScheduleConfigDto configDto = new ScheduleConfigDto();
        configDto.setPlayerNames(playerNames);
        configDto.setNumberOfRounds(5);
        configDto.setPlayersPerRound(4);

        PlanDto result = resource.generateSchedule(configDto);

        List<String> resultPlayerNames = result.getPlayers().stream()
                .map(PlayerDto::getName)
                .sorted()
                .toList();
        List<String> expectedNames = new ArrayList<>(playerNames);
        Collections.sort(expectedNames);

        assertEquals(expectedNames, resultPlayerNames);
    }

    @Test
    @DisplayName("Pairings endpoint should return only player names and frequency")
    void testPairingsEndpointReturnsPlayerNamesOnly() {
        // Schedule generieren
        ScheduleConfigDto configDto = new ScheduleConfigDto();
        configDto.setPlayerNames(Arrays.asList("Anna", "Ben", "Chris", "Dora"));
        configDto.setNumberOfRounds(2);
        configDto.setPlayersPerRound(4);
        resource.generateSchedule(configDto);

        List<PairingDto> pairings = resource.getAllPairings();
        assertNotNull(pairings);
        assertFalse(pairings.isEmpty());
        for (PairingDto dto : pairings) {
            assertNotNull(dto.getPlayerNames(), "playerNames should not be null");
            assertFalse(dto.getPlayerNames().isEmpty(), "playerNames should contain names");
            assertTrue(dto.getFrequency() >= 1, "frequency should be >= 1");
        }
    }
}
