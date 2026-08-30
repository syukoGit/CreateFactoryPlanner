package fr.syuko.createfactoryplanner.core.model;

public record LinkId(long value) implements Comparable<LinkId> {

    public LinkId {
        if (value < 0) {
            throw new IllegalArgumentException("a link id cannot be negative, got " + value);
        }
    }

    @Override
    public int compareTo(LinkId other) {
        return Long.compare(value, other.value);
    }
}
