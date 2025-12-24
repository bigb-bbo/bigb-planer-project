package at.bigb.planer.service;

import at.bigb.planer.domain.Plan;
import at.bigb.planer.domain.Player;
import at.bigb.planer.domain.Round;
import at.bigb.planer.domain.ScheduleConfig;
import at.bigb.planer.domain.dto.PairingDto;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.Config;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main service for generating optimized schedules with minimal player combination repetition.
 * Uses a greedy algorithm to ensure balanced distribution of player pairings across rounds.
 */
@Slf4j
public class ScheduleGenerationService {

    private final PairingAnalyzer analyzer;
    private final PairingGenerator pairingGenerator;
    private List<Player> lastGeneratedPlayers = new ArrayList<>();
    private Plan lastGeneratedPlan; // last generated plan

    public ScheduleGenerationService() {
        this.analyzer = new PairingAnalyzer();
        // Read pairing generator configuration from application.properties (MicroProfile Config)
        Config config = ConfigProvider.getConfig();
        String strategyStr = config.getOptionalValue("planer.pairing.strategy", String.class).orElse("GREEDY_SHUFFLE");
        String seedStr = config.getOptionalValue("planer.pairing.seed", String.class).orElse("");
        int greedyReshuffles = config.getOptionalValue("planer.pairing.greedyReshuffles", Integer.class).orElse(200);
        long backtrackTimeout = config.getOptionalValue("planer.pairing.backtrackTimeoutMillis", Long.class).orElse(200L);
        PairingGenerator.Strategy strategy = PairingGenerator.Strategy.GREEDY_SHUFFLE;
        try {
            strategy = PairingGenerator.Strategy.valueOf(strategyStr);
        } catch (Exception e) {
            log.warn("Invalid planer.pairing.strategy='{}'. Falling back to GREEDY_SHUFFLE", strategyStr);
        }
        Long seed = null;
        if (!seedStr.isBlank()) {
            try { seed = Long.parseLong(seedStr); } catch (Exception ignored) { seed = null; }
        }
        this.pairingGenerator = new PairingGenerator(strategy, seed, greedyReshuffles, backtrackTimeout);
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
        List<Round> rounds = generateRounds(players, config.getNumberOfRounds(), config.getPlayersPerRound());
        plan.setRounds(rounds);
        lastGeneratedPlan = plan; // save plan

        log.info("Schedule generation completed: {} rounds with {} players each",
                rounds.size(), config.getPlayersPerRound());

        return plan;
    }

    /**
     * Generates all rounds for the schedule
     */
    private List<Round> generateRounds(List<Player> players, int numberOfRounds, int playersPerRound) {
        List<Round> rounds = new ArrayList<>();
        LocalDate baseDate = LocalDate.now();

        // Prepare a frequency lookup for existing pairings recorded by analyzer
        // The analyzer expects List<Player>, but our pairingGenerator works with Set<String> of names
        for (int i = 1; i <= numberOfRounds; i++) {
            Round round = new Round();
            round.setRoundNo(i);
            round.setRoundDate(baseDate.plusDays((long) (i - 1) * 7)); // weekly schedule

            // Select players for this round using PairingGenerator (group of size playersPerRound)
            List<String> availableNames = players.stream().map(Player::getName).collect(Collectors.toList());
            List<String> selectedNames = pairingGenerator.selectGroup(availableNames, playersPerRound, (set) -> {
                // convert Set<String> to List<Player> for analyzer frequency lookup
                List<Player> plist = players.stream()
                        .filter(p -> set.contains(p.getName()))
                        .collect(Collectors.toList());
                return analyzer.getFrequency(plist);
            });

            // Map selected names back to Player objects preserving original Player instances
            List<Player> selectedPlayers = players.stream()
                    .filter(p -> selectedNames.contains(p.getName()))
                    .collect(Collectors.toList());

            round.setSelectedPlayers(selectedPlayers);

            // record pairing into analyzer
            if (selectedPlayers.size() == 4) {
                analyzer.recordPairing(selectedPlayers);
            } else {
                log.debug("Skipping analyzer.recordPairing because selectedPlayers.size() != 4: {}", selectedPlayers.size());
            }

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

    /**
     * Returns a statistic of how often each player appears in the current plan
     */
    public Map<String, Integer> getPlayerUsageStatistics() {
        if (lastGeneratedPlan == null || lastGeneratedPlan.getRounds() == null) {
            return Collections.emptyMap();
        }
        Map<String, Integer> usage = new HashMap<>();
        for (Player p : lastGeneratedPlan.getPlayers()) {
            usage.put(p.getName(), 0);
        }
        for (Round r : lastGeneratedPlan.getRounds()) {
            for (Player p : r.getSelectedPlayers()) {
                usage.put(p.getName(), usage.getOrDefault(p.getName(), 0) + 1);
            }
        }
        return usage;
    }
}
