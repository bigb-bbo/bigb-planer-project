package at.bigb.planer.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoundDto {
    private int roundNo;
    private LocalDate roundDate;
    private List<PlayerDto> selectedPlayers;
}
