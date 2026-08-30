package fr.syuko.createfactoryplanner.core.recipe;

import fr.syuko.createfactoryplanner.core.model.ResourceId;

public record IngredientDto(ResourceId resource, long amountPerOperation) {

    public IngredientDto {
        if (resource == null) {
            throw new IllegalArgumentException("an ingredient must carry a resource");
        }
        if (amountPerOperation <= 0) {
            throw new IllegalArgumentException("an ingredient amount must be positive, got " + amountPerOperation + " of " + resource.value());
        }
    }
}