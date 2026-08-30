package fr.syuko.createfactoryplanner.core.model.node;

import fr.syuko.createfactoryplanner.core.model.ResourceId;

import java.util.Set;

public record OutputBranch(Set<ResourceId> filter, int fixedRatio) {

    public OutputBranch {
        filter = Set.copyOf(filter);
        if (fixedRatio < 0) {
            throw new IllegalArgumentException("a fixed ratio cannot be negative, got " + fixedRatio);
        }
    }

    public static OutputBranch everything() {
        return new OutputBranch(Set.of(), 1);
    }

    public static OutputBranch accepting(Set<ResourceId> filter) {
        return new OutputBranch(filter, 1);
    }

    public boolean accepts(ResourceId resource) {
        return filter.isEmpty() || filter.contains(resource);
    }

    public OutputBranch withRatio(int ratio) {
        return new OutputBranch(filter, ratio);
    }
}
