package fr.syuko.createfactoryplanner.core.io;

import fr.syuko.createfactoryplanner.core.model.ResourceId;
import fr.syuko.createfactoryplanner.core.model.ResourceKind;

public record ResourceEntry(String resource, ResourceKind kind) {

    public ResourceEntry {
        if (resource == null || resource.isBlank()) {
            throw new IllegalArgumentException("a resource entry cannot be blank");
        }
        if (kind == null) {
            throw new IllegalArgumentException("a resource entry must declare its kind, on " + resource);
        }
    }

    public static ResourceEntry of(ResourceId id) {
        return new ResourceEntry(id.value(), id.kind());
    }

    public ResourceId toResourceId() {
        return new ResourceId(resource, kind);
    }
}
