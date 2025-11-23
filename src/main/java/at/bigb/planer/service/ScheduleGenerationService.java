package at.bigb.planer.service;

import at.bigb.planer.domain.Plan;
import at.bigb.planer.domain.Player;
import at.bigb.planer.domain.Round;
import at.bigb.planer.domain.ScheduleConfig;
import at.bigb.planer.domain.dto.PairingDto;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main service for generating optimized schedules with minimal player combination repetition.
 * Uses a greedy algorithm to ensure balanced distribution of player pairings across rounds.
 */
@Slf4j
public class ScheduleGenerationService {

    private final PairingAlgorithm algorithm;
    private final PairingAnalyzer analyzer;
    private List<Player> lastGeneratedPlayers = new ArrayList<>();

    public ScheduleGenerationService() {
        this.analyzer = new PairingAnalyzer();
        this.algorithm = new PairingAlgorithm(analyzer);
    }

    /**
     * Generates a complete schedule based on the provided configuration
     *
     * @param config Configuration containing player names and number of rounds
     * @return Generated Plan with optimized rounds
     */
    public Plan generateSchedule(ScheduleConfig config) {
        log.info("Generating schedule with {} players and {} rounds",
                config.getPlayerNames().size(), config.getNumberOfRounds());

        // Validate input
        validateConfig(config);

        // Reset analyzer for new schedule
        analyzer.reset();

        // Create players with IDs
        List<Player> players = createPlayers(config.getPlayerNames());
        lastGeneratedPlayers = players;

        // Create plan
        Plan plan = Plan.create(players, config.getNumberOfRounds());

        // Generate rounds
        List<Round> rounds = generateRounds(players, config.getNumberOfRounds());
        plan.setRounds(rounds);

        log.info("Schedule generation completed: {} rounds with {} players each",
                rounds.size(), config.getPlayersPerRound());

        return plan;
    }

    /**
     * Generates all rounds for the schedule
     */
    private List<Round> generateRounds(List<Player> players, int numberOfRounds) {
        List<Round> rounds = new ArrayList<>();
        LocalDate baseDate = LocalDate.now();

        for (int i = 1; i <= numberOfRounds; i++) {
            Round round = new Round();
            round.setRoundNo(i);
            round.setRoundDate(baseDate.plusDays((long) (i - 1) * 7)); // Weekly schedule

            // Select 4 players using the greedy algorithm
            List<Player> selectedPlayers = algorithm.selectPlayersForRound(players);
            round.setSelectedPlayers(selectedPlayers);

            rounds.add(round);

            log.debug("Generated round {}: {}", i, selectedPlayers.stream()
                    .map(Player::getName)
                    .collect(Collectors.joining(", ")));
        }

        return rounds;
    }

    /**
     * Creates Player objects from names with generated IDs
     */
    private List<Player> createPlayers(List<String> playerNames) {
        return playerNames.stream()
                .map(name -> new Player(UUID.randomUUID().toString(), name))
                .collect(Collectors.toList());
    }

    /**
     * Validates the configuration before schedule generation
     */
    private void validateConfig(ScheduleConfig config) {
        if (config.getPlayerNames() == null || config.getPlayerNames().isEmpty()) {
            throw new IllegalArgumentException("Player names list cannot be empty");
        }

        if (config.getPlayerNames().size() < 4) {
            throw new IllegalArgumentException("At least 4 players are required");
        }

        if (config.getNumberOfRounds() <= 0) {
            throw new IllegalArgumentException("Number of rounds must be greater than 0");
        }

        // Check for duplicate player names
        long uniqueNames = config.getPlayerNames().stream().distinct().count();
        if (uniqueNames != config.getPlayerNames().size()) {
            throw new IllegalArgumentException("Duplicate player names are not allowed");
        }
    }

    /**
     * Gets current pairing statistics
     */
    public Map<String, Object> getPairingStatistics() {
        return analyzer.getStatistics();
    }

    /**
     * Gets all pairings sorted by frequency
     */
    public List<PairingDto> getAllPairingsSorted() {
        return analyzer.getAllPairingsSortedByFrequency().stream()
                .map(pairing -> ScheduleMapper.mapPairingToDto(pairing, lastGeneratedPlayers))
                .collect(Collectors.toList());
    }
}
