package fr.syuko.createfactoryplanner.core.model;

public record ResourceId(String value, ResourceKind kind) {

    public ResourceId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("a resource id cannot be blank");
        }
        if (kind == null) {
            throw new IllegalArgumentException("a resource id must declare its kind");
        }
    }

    public static ResourceId item(String value) {
        return new ResourceId(value, ResourceKind.ITEM);
    }

    public static ResourceId fluid(String value) {
        return new ResourceId(value, ResourceKind.FLUID);
    }

    public boolean isFluid() {
        return kind == ResourceKind.FLUID;
    }
}
