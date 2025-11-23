package at.bigb.planer.service;

import at.bigb.planer.domain.Plan;
import at.bigb.planer.domain.Player;
import at.bigb.planer.domain.Round;
import at.bigb.planer.domain.ScheduleConfig;
import at.bigb.planer.domain.dto.*;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ScheduleMapper
 * Tests conversion between domain models and DTOs
 */
@QuarkusTest
@DisplayName("ScheduleMapper Tests")
class ScheduleMapperTest {

    private List<Player> testPlayers;
    private List<PlayerDto> testPlayerDtos;

    @BeforeEach
    void setUp() {
        testPlayers = Arrays.asList(
                new Player(UUID.randomUUID().toString(), "Alice"),
                new Player(UUID.randomUUID().toString(), "Bob"),
                new Player(UUID.randomUUID().toString(), "Charlie"),
                new Player(UUID.randomUUID().toString(), "David")
        );

        testPlayerDtos = Arrays.asList(
                new PlayerDto(testPlayers.get(0).getId(), "Alice"),
                new PlayerDto(testPlayers.get(1).getId(), "Bob"),
                new PlayerDto(testPlayers.get(2).getId(), "Charlie"),
                new PlayerDto(testPlayers.get(3).getId(), "David")
        );
    }

    @Test
    @DisplayName("Should map Player to PlayerDto")
    void testMapPlayerToDto() {
        Player player = testPlayers.get(0);

        PlayerDto dto = ScheduleMapper.mapPlayerToDto(player);

        assertEquals(player.getId(), dto.getId());
        assertEquals(player.getName(), dto.getName());
    }

    @Test
    @DisplayName("Should map PlayerDto to Player")
    void testMapDtoToPlayer() {
        PlayerDto dto = testPlayerDtos.get(0);

        Player player = ScheduleMapper.mapDtoToPlayer(dto);

        assertEquals(dto.getId(), player.getId());
        assertEquals(dto.getName(), player.getName());
    }

    @Test
    @DisplayName("Should map Round to RoundDto")
    void testMapRoundToDto() {
        Round round = new Round(
                1,
                LocalDate.of(2025, 1, 15),
                testPlayers
        );

        RoundDto dto = ScheduleMapper.mapRoundToDto(round);

        assertEquals(round.getRoundNo(), dto.getRoundNo());
        assertEquals(round.getRoundDate(), dto.getRoundDate());
        assertEquals(testPlayers.size(), dto.getSelectedPlayers().size());
    }

    @Test
    @DisplayName("Should map RoundDto to Round")
    void testMapDtoToRound() {
        RoundDto dto = new RoundDto(
                1,
                LocalDate.of(2025, 1, 15),
                testPlayerDtos
        );

        Round round = ScheduleMapper.mapDtoToRound(dto);

        assertEquals(dto.getRoundNo(), round.getRoundNo());
        assertEquals(dto.getRoundDate(), round.getRoundDate());
        assertEquals(dto.getSelectedPlayers().size(), round.getSelectedPlayers().size());
    }

    @Test
    @DisplayName("Should map Plan to PlanDto with all data")
    void testMapPlanToDto() {
        Plan plan = new Plan(
                UUID.randomUUID().toString(),
                testPlayers,
                Arrays.asList(
                        new Round(1, LocalDate.of(2025, 1, 15), testPlayers.subList(0, 4)),
                        new Round(2, LocalDate.of(2025, 1, 22), testPlayers.subList(0, 4))
                ),
                2,
                LocalDateTime.of(2025, 1, 10, 10, 0)
        );

        PlanDto dto = ScheduleMapper.mapPlanToDto(plan);

        assertEquals(plan.getId(), dto.getId());
        assertEquals(plan.getNumberOfRounds(), dto.getNumberOfRounds());
        assertEquals(plan.getPlayers().size(), dto.getPlayers().size());
        assertEquals(plan.getRounds().size(), dto.getRounds().size());
        assertEquals(plan.getCreatedAt(), dto.getCreatedAt());
    }


    @Test
    @DisplayName("Should map ScheduleConfigDto to ScheduleConfig")
    void testMapDtoToScheduleConfig() {
        ScheduleConfigDto dto = new ScheduleConfigDto(
                Arrays.asList("Alice", "Bob", "Charlie", "David"),
                5,
                4
        );

        ScheduleConfig config = ScheduleMapper.mapDtoToScheduleConfig(dto);

        assertEquals(dto.getPlayerNames(), config.getPlayerNames());
        assertEquals(dto.getNumberOfRounds(), config.getNumberOfRounds());
        assertEquals(dto.getPlayersPerRound(), config.getPlayersPerRound());
    }

    @Test
    @DisplayName("Should map stats Map to ScheduleStatsDto")
    void testMapStatsToDto() {
        Map<String, Object> stats = Map.of(
                "totalUniquePairings", 10,
                "totalPairingRecords", 25,
                "maxFrequency", 5,
                "minFrequency", 2,
                "avgFrequency", 3.5
        );

        ScheduleStatsDto dto = ScheduleMapper.mapStatsToDto(stats);

        assertEquals(10, dto.getTotalUniquePairings());
        assertEquals(25, dto.getTotalPairingRecords());
        assertEquals(5, dto.getMaxFrequency());
        assertEquals(2, dto.getMinFrequency());
        assertEquals(3.5, dto.getAvgFrequency());
    }

    @Test
    @DisplayName("Should handle null values gracefully in stats mapping")
    void testMapStatsToDto_WithMissingValues() {
        Map<String, Object> stats = Map.of(
                "totalUniquePairings", 10,
                "totalPairingRecords", 25
        );

        // Should not throw exception with missing values
        ScheduleStatsDto dto = ScheduleMapper.mapStatsToDto(stats);

        assertEquals(10, dto.getTotalUniquePairings());
        assertEquals(25, dto.getTotalPairingRecords());
        assertEquals(0, dto.getMaxFrequency()); // Default value
        assertEquals(0.0, dto.getAvgFrequency()); // Default value
    }

    @Test
    @DisplayName("Should map list of Players to list of PlayerDtos")
    void testMapPlayerListToDto() {
        List<PlayerDto> dtos = testPlayers.stream()
                .map(ScheduleMapper::mapPlayerToDto)
                .toList();

        assertEquals(testPlayers.size(), dtos.size());
        for (int i = 0; i < testPlayers.size(); i++) {
            assertEquals(testPlayers.get(i).getId(), dtos.get(i).getId());
            assertEquals(testPlayers.get(i).getName(), dtos.get(i).getName());
        }
    }

    @Test
    @DisplayName("Should preserve empty rounds list")
    void testMapPlanToDto_EmptyRounds() {
        Plan plan = new Plan(
                UUID.randomUUID().toString(),
                testPlayers,
                List.of(),
                0,
                LocalDateTime.now()
        );

        PlanDto dto = ScheduleMapper.mapPlanToDto(plan);

        assertTrue(dto.getRounds().isEmpty(), "Empty rounds should be preserved");
    }
}

