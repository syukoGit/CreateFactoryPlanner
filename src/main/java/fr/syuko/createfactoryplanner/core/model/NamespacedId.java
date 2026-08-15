package fr.syuko.createfactoryplanner.core.model;

import org.jetbrains.annotations.NotNull;

public record NamespacedId(String namespace, String path) implements Comparable<NamespacedId> {
    public NamespacedId {
        if (namespace == null || namespace.isBlank()) {
            throw new IllegalArgumentException("namespace must not be blank");
        }
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path must not be blank");
        }
    }

    public static NamespacedId parse(String value) {
        if (value == null) {
            throw new IllegalArgumentException("identifier must not be null");
        }
        int separator = value.indexOf(':');
        if (separator < 0) {
            throw new IllegalArgumentException("identifier must contain ':' but was " + value);
        }
        return new NamespacedId(value.substring(0, separator), value.substring(separator + 1));
    }

    @Override
    public int compareTo(NamespacedId other) {
        return toString().compareTo(other.toString());
    }

    @Override
    public @NotNull String toString() {
        return namespace + ":" + path;
    }
}