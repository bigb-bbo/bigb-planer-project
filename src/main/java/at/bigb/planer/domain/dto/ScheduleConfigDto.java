package at.bigb.planer.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for schedule generation request input
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleConfigDto {
    private List<String> playerNames;
    private int numberOfRounds;
    private int playersPerRound = 4;
}
