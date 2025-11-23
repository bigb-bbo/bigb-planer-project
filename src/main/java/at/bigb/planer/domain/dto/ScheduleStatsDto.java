package at.bigb.planer.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for schedule statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleStatsDto {
    private int totalUniquePairings;
    private int totalPairingRecords;
    private int maxFrequency;
    private int minFrequency;
    private double avgFrequency;
}
