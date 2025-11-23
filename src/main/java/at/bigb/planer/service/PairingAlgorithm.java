package at.bigb.planer.service;

import at.bigb.planer.domain.Player;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates player combinations with minimal pairing repetitions using greedy algorithm.
 * This algorithm minimizes the number of times the same 4-player combination appears.
 */
@Slf4j
public class PairingAlgorithm {

    private final PairingAnalyzer analyzer;
    private final Map<String, Integer> playerUsageCount = new HashMap<>(); // Einsatzhäufigkeit

    public PairingAlgorithm(PairingAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    /**
     * Generates the optimal 4-player combination for the next round
     * using a greedy algorithm that minimizes pairing repetitions.
     *
     * @param availablePlayers All available players (10)
     * @return A list of 4 selected players
     */
    public List<Player> selectPlayersForRound(List<Player> availablePlayers) {
        if (availablePlayers.size() < 4) {
            throw new IllegalArgumentException("Need at least 4 players");
        }

        // Initialisiere Zählung, falls leer
        for (Player p : availablePlayers) {
            playerUsageCount.putIfAbsent(p.getId(), 0);
        }

        log.debug("Selecting players for round from {} available players", availablePlayers.size());

        // Alle möglichen Kombinationen
        List<List<Player>> allCombinations = generateAllCombinations(availablePlayers);
        log.debug("Generated {} possible combinations", allCombinations.size());

        // Sortiere nach minimaler maximaler Einsatzhäufigkeit, dann nach Pairing-Frequenz
        List<List<Player>> sortedCombinations = allCombinations.stream()
                .sorted((comb1, comb2) -> {
                    int sumUsage1 = comb1.stream().mapToInt(p -> playerUsageCount.getOrDefault(p.getId(), 0)).sum();
                    int sumUsage2 = comb2.stream().mapToInt(p -> playerUsageCount.getOrDefault(p.getId(), 0)).sum();
                    if (sumUsage1 != sumUsage2) {
                        return Integer.compare(sumUsage1, sumUsage2);
                    }
                    int maxUsage1 = comb1.stream().mapToInt(p -> playerUsageCount.getOrDefault(p.getId(), 0)).max().orElse(0);
                    int maxUsage2 = comb2.stream().mapToInt(p -> playerUsageCount.getOrDefault(p.getId(), 0)).max().orElse(0);
                    if (maxUsage1 != maxUsage2) {
                        return Integer.compare(maxUsage1, maxUsage2);
                    }
                    // Falls gleich, wie bisher nach Pairing-Frequenz
                    return compareCombinations(comb1, comb2);
                })
                .toList();

        // Return the combination with the lowest total frequency
        List<Player> selectedPlayers = sortedCombinations.get(0);
        analyzer.recordPairing(selectedPlayers);
        // Zählung aktualisieren
        for (Player p : selectedPlayers) {
            playerUsageCount.put(p.getId(), playerUsageCount.getOrDefault(p.getId(), 0) + 1);
        }

        log.debug("Selected players: {}", selectedPlayers.stream()
                .map(Player::getName)
                .collect(Collectors.joining(", ")));

        return selectedPlayers;
    }

    /**
     * Generates all possible k-combinations from a list
     */
    private List<List<Player>> generateAllCombinations(List<Player> players) {
        List<List<Player>> combinations = new ArrayList<>();
        generateCombinationsHelper(players, 0, new ArrayList<>(), 4, combinations);
        return combinations;
    }

    /**
     * Helper method for generating combinations recursively
     */
    private void generateCombinationsHelper(List<Player> players, int start,
                                          List<Player> current, int k,
                                          List<List<Player>> combinations) {
        if (current.size() == k) {
            combinations.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i < players.size(); i++) {
            current.add(players.get(i));
            generateCombinationsHelper(players, i + 1, current, k, combinations);
            current.remove(current.size() - 1);
        }
    }

    /**
     * Compares two combinations for sorting.
     * Lower frequency = better (appears earlier in sorted list)
     * Scoring criteria (in order):
     * 1. Total frequency of the 4-player combination
     * 2. Sum of frequencies of all pairs within the combination
     * 3. Balance: prefer combinations where players haven't played together recently
     */
    private int compareCombinations(List<Player> comb1, List<Player> comb2) {
        int freq1 = analyzer.getFrequency(comb1);
        int freq2 = analyzer.getFrequency(comb2);

        // Primary criterion: exact 4-player combination frequency
        if (freq1 != freq2) {
            return Integer.compare(freq1, freq2);
        }

        // Secondary criterion: sum of pairwise frequencies
        int pairFreq1 = calculatePairwiseFrequencySum(comb1);
        int pairFreq2 = calculatePairwiseFrequencySum(comb2);

        return Integer.compare(pairFreq1, pairFreq2);
    }

    /**
     * Calculates the sum of frequencies for all pairs within a 4-player combination.
     * This helps identify if players have been paired together before.
     */
    private int calculatePairwiseFrequencySum(List<Player> players) {
        int sum = 0;
        for (int i = 0; i < players.size(); i++) {
            for (int j = i + 1; j < players.size(); j++) {
                List<Player> pair = Arrays.asList(players.get(i), players.get(j));
                sum += analyzer.getFrequency(pair);
            }
        }
        return sum;
    }
}
