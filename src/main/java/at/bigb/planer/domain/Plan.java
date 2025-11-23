package at.bigb.planer.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Generates a plan of (multiple) match-ups with players distributed across rounds
 * to minimize duplicate player combinations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Plan {
    private String id;
    private List<Player> players;
    private List<Round> rounds;
    private int numberOfRounds;
    private LocalDateTime createdAt;

    public static Plan create(List<Player> players, int numberOfRounds) {
        Plan plan = new Plan();
        plan.setId(UUID.randomUUID().toString());
        plan.setPlayers(players);
        plan.setNumberOfRounds(numberOfRounds);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setRounds(List.of());
        return plan;
    }
}
