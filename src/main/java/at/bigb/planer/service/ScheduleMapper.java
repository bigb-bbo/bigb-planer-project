package at.bigb.planer.service;

import at.bigb.planer.domain.Plan;
import at.bigb.planer.domain.Pairing;
import at.bigb.planer.domain.Player;
import at.bigb.planer.domain.Round;
import at.bigb.planer.domain.ScheduleConfig;
import at.bigb.planer.domain.dto.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper for converting between domain models and DTOs
 */
public class ScheduleMapper {

    private ScheduleMapper() {
        // Prevent instantiation
    }


    public static PlanDto mapPlanToDto(Plan plan) {
        PlanDto dto = new PlanDto();
        dto.setId(plan.getId());
        dto.setPlayers(plan.getPlayers().stream()
                .map(ScheduleMapper::mapPlayerToDto)
                .collect(Collectors.toList()));
        dto.setRounds(plan.getRounds().stream()
                .map(ScheduleMapper::mapRoundToDto)
                .collect(Collectors.toList()));
        dto.setNumberOfRounds(plan.getNumberOfRounds());
        dto.setCreatedAt(plan.getCreatedAt());
        return dto;
    }


    public static PlayerDto mapPlayerToDto(Player player) {
        return new PlayerDto(player.getId(), player.getName());
    }

    public static Player mapDtoToPlayer(PlayerDto dto) {
        return new Player(dto.getId(), dto.getName());
    }

    public static RoundDto mapRoundToDto(Round round) {
        return new RoundDto(
                round.getRoundNo(),
                round.getRoundDate(),
                round.getSelectedPlayers().stream()
                        .map(ScheduleMapper::mapPlayerToDto)
                        .collect(Collectors.toList())
        );
    }

    public static Round mapDtoToRound(RoundDto dto) {
        return new Round(
                dto.getRoundNo(),
                dto.getRoundDate(),
                dto.getSelectedPlayers().stream()
                        .map(ScheduleMapper::mapDtoToPlayer)
                        .collect(Collectors.toList())
        );
    }

    public static ScheduleConfig mapDtoToScheduleConfig(ScheduleConfigDto dto) {
        return new ScheduleConfig(
                dto.getPlayerNames(),
                dto.getNumberOfRounds(),
                dto.getPlayersPerRound()
        );
    }

    public static ScheduleStatsDto mapStatsToDto(Map<String, Object> stats) {
        return new ScheduleStatsDto(
                (Integer) stats.get("totalUniquePairings"),
                (Integer) stats.get("totalPairingRecords"),
                (Integer) stats.getOrDefault("maxFrequency", 0),
                (Integer) stats.getOrDefault("minFrequency", 0),
                ((Number) stats.getOrDefault("avgFrequency", 0.0)).doubleValue()
        );
    }

    public static PairingDto mapPairingToDto(Pairing pairing, List<Player> allPlayers) {
        List<String> playerNames = pairing.getPlayerIds().stream()
            .map(id -> allPlayers.stream()
                .filter(p -> p.getId().equals(id))
                .map(Player::getName)
                .findFirst().orElse(id)) // Falls Name nicht gefunden, ID anzeigen
            .toList();
        return new PairingDto(playerNames, pairing.getFrequency());
    }

}
