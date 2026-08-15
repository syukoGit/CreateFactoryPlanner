package fr.syuko.createfactoryplanner.core.model;

import java.util.Objects;

public record OutputStack(ItemKey item, int count, float chance) {
    public OutputStack {
        Objects.requireNonNull(item, "item");
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive but was " + count);
        }
        if (chance < 0f) {
            throw new IllegalArgumentException("chance must not be negative but was " + chance);
        }
    }

    public static OutputStack guaranteed(ItemKey item, int count) {
        return new OutputStack(item, count, 1f);
    }

    public double expectedPerOperation() {
        return count * (double) chance;
    }

    public boolean certain() {
        return chance >= 1f;
    }
}
