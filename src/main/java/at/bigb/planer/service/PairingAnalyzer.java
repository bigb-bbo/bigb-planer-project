package at.bigb.planer.service;

import at.bigb.planer.domain.Pairing;
import at.bigb.planer.domain.Player;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzes and tracks player pairings to monitor how often specific combinations appear
 */
@Slf4j
public class PairingAnalyzer {

    private final Map<Set<String>, Integer> pairingFrequency;

    public PairingAnalyzer() {
        this.pairingFrequency = new HashMap<>();
    }

    /**
     * Records a 4-player combination
     */
    public void recordPairing(List<Player> selectedPlayers) {
        if (selectedPlayers.size() != 4) {
            throw new IllegalArgumentException("Pairing must contain exactly 4 players");
        }

        Set<String> playerIds = selectedPlayers.stream()
                .map(Player::getId)
                .collect(Collectors.toSet());

        pairingFrequency.put(playerIds, pairingFrequency.getOrDefault(playerIds, 0) + 1);
        log.debug("Recorded pairing: {}", playerIds);
    }

    /**
     * Gets the frequency of a specific pairing
     */
    public int getFrequency(List<Player> players) {
        Set<String> playerIds = players.stream()
                .map(Player::getId)
                .collect(Collectors.toSet());
        return pairingFrequency.getOrDefault(playerIds, 0);
    }

    /**
     * Gets all pairings sorted by frequency (ascending)
     */
    public List<Pairing> getAllPairingsSortedByFrequency() {
        return pairingFrequency.entrySet().stream()
                .map(entry -> {
                    Pairing pairing = new Pairing(entry.getKey());
                    pairing.setFrequency(entry.getValue());
                    return pairing;
                })
                .sorted(Comparator.comparingInt(Pairing::getFrequency))
                .collect(Collectors.toList());
    }

    /**
     * Gets statistics about pairings
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUniquePairings", pairingFrequency.size());
        stats.put("totalPairingRecords", pairingFrequency.values().stream()
                .mapToInt(Integer::intValue).sum());

        if (!pairingFrequency.isEmpty()) {
            int maxFrequency = pairingFrequency.values().stream()
                    .mapToInt(Integer::intValue).max().orElse(0);
            int minFrequency = pairingFrequency.values().stream()
                    .mapToInt(Integer::intValue).min().orElse(0);
            double avgFrequency = pairingFrequency.values().stream()
                    .mapToInt(Integer::intValue).average().orElse(0.0);

            stats.put("maxFrequency", maxFrequency);
            stats.put("minFrequency", minFrequency);
            stats.put("avgFrequency", avgFrequency);
        }

        return stats;
    }

    /**
     * Resets all pairing statistics
     */
    public void reset() {
        pairingFrequency.clear();
        log.debug("Pairing analyzer reset");
    }
}
