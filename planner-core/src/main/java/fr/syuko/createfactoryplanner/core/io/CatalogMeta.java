package fr.syuko.createfactoryplanner.core.io;

public record CatalogMeta(String readAt, String world, String modVersion, String createVersion, int machineCount,
                          int recipeCount) {

    public CatalogMeta {
        if (readAt == null || readAt.isBlank()) {
            throw new IllegalArgumentException("a catalog must say when it was read");
        }
        if (machineCount < 0 || recipeCount < 0) {
            throw new IllegalArgumentException("a catalog cannot hold a negative count, on " + readAt);
        }
    }
}