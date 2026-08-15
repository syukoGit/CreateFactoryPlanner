package fr.syuko.createfactoryplanner.core.model;

import java.util.List;
import java.util.Objects;

public record InputStack(List<ItemKey> accepted, ItemKey representative, int amount, boolean consumed) {
    public InputStack {
        accepted = List.copyOf(Objects.requireNonNull(accepted, "accepted"));
        Objects.requireNonNull(representative, "representative");
        if (accepted.isEmpty()) {
            throw new IllegalArgumentException("an input must accept at least one item");
        }
        if (!accepted.contains(representative)) {
            throw new IllegalArgumentException("representative " + representative + " is not among the accepted items");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive but was " + amount);
        }
    }

    public boolean fluid() {
        return representative.fluid();
    }

    public InputStack withAmount(int newAmount) {
        return new InputStack(accepted, representative, newAmount, consumed);
    }
}
