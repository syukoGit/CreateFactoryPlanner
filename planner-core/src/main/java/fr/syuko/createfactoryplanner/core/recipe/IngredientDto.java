package fr.syuko.createfactoryplanner.core.recipe;

import fr.syuko.createfactoryplanner.core.math.Rate;
import fr.syuko.createfactoryplanner.core.model.ResourceId;

public record IngredientDto(ResourceId resource, Rate amountPerOperation) {

    public IngredientDto {
        if (resource == null) {
            throw new IllegalArgumentException("an ingredient must carry a resource");
        }
        if (amountPerOperation == null) {
            throw new IllegalArgumentException("an ingredient must carry an amount");
        }
        if (amountPerOperation.compareTo(Rate.ZERO) <= 0) {
            throw new IllegalArgumentException("an ingredient amount must be positive, got " + amountPerOperation + " of " + resource.value());
        }
    }

    public static IngredientDto of(ResourceId resource, long amountPerOperation) {
        return new IngredientDto(resource, Rate.of(amountPerOperation));
    }
}
