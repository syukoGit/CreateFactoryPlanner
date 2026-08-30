package fr.syuko.createfactoryplanner.core.model.node;

import fr.syuko.createfactoryplanner.core.model.NodeId;
import fr.syuko.createfactoryplanner.core.model.ResourceId;

public record Port(NodeId owner, int index, PortDirection direction, ResourceId resource) {

    public Port {
        if (owner == null) {
            throw new IllegalArgumentException("a port must belong to a node");
        }
        if (direction == null) {
            throw new IllegalArgumentException("a port must declare its direction");
        }
        if (index < 0) {
            throw new IllegalArgumentException("a port index cannot be negative, got " + index);
        }
    }

    public static Port untyped(NodeId owner, int index, PortDirection direction) {
        return new Port(owner, index, direction, null);
    }

    public boolean isTyped() {
        return resource != null;
    }

    public boolean carries(ResourceId candidate) {
        return resource == null || resource.equals(candidate);
    }
}
