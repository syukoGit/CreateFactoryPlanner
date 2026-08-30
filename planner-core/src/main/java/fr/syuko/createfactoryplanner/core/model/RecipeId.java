package fr.syuko.createfactoryplanner.core.model;

public record RecipeId(String value) {

    public RecipeId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("a recipe id cannot be blank");
        }
    }
}
