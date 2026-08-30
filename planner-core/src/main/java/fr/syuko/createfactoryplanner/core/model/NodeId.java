package fr.syuko.createfactoryplanner.core.model;

public record NodeId(long value) implements Comparable<NodeId> {

    public NodeId {
        if (value < 0) {
            throw new IllegalArgumentException("a node id cannot be negative, got " + value);
        }
    }

    @Override
    public int compareTo(NodeId other) {
        return Long.compare(value, other.value);
    }
}
