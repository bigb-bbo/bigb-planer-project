package at.bigb.planer.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Configuration for schedule generation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleConfig {
    private List<String> playerNames;
    private int numberOfRounds;
    private int playersPerRound;
}
