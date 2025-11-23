package at.bigb.planer.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for displaying pairing statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PairingDto {
    private List<String> playerIds;
    private int frequency;
}

