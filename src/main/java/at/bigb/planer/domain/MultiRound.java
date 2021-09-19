package at.bigb.planer.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MultiRound extends Round {

    private String participantThree;
    private String participantFour;
}
