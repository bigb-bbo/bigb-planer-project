package at.bigb.planer.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a combination of 4 players and tracks how many times this combination has been used
 */
@Data
@NoArgsConstructor
public class Pairing {
    private Set<String> playerIds;
    private int frequency;

    public Pairing(Set<String> playerIds) {
        this.playerIds = new HashSet<>(playerIds);
        this.frequency = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pairing pairing = (Pairing) o;
        return playerIds.equals(pairing.playerIds);
    }

    @Override
    public int hashCode() {
        return playerIds.hashCode();
    }
}

