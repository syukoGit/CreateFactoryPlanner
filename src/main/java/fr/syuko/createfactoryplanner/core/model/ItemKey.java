package fr.syuko.createfactoryplanner.core.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record ItemKey(NamespacedId id, boolean fluid) implements Comparable<ItemKey> {
    public ItemKey {
        Objects.requireNonNull(id, "id");
    }

    public static ItemKey item(NamespacedId id) {
        return new ItemKey(id, false);
    }

    public static ItemKey fluid(NamespacedId id) {
        return new ItemKey(id, true);
    }

    @Override
    public int compareTo(ItemKey other) {
        int byKind = Boolean.compare(fluid, other.fluid);
        return byKind != 0
               ? byKind
               : id.compareTo(other.id);
    }

    @Override
    public @NotNull String toString() {
        return fluid
               ? "fluid/" + id
               : id.toString();
    }
}