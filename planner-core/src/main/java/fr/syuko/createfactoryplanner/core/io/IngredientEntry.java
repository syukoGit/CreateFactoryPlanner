package fr.syuko.createfactoryplanner.core.io;

import fr.syuko.createfactoryplanner.core.math.Rate;
import fr.syuko.createfactoryplanner.core.recipe.IngredientDto;

import java.util.List;

public record IngredientEntry(ResourceEntry resource, String amountPerOperation, List<String> equivalents) {

    public IngredientEntry {
        if (resource == null) {
            throw new IllegalArgumentException("an ingredient entry must carry a resource");
        }
        equivalents = equivalents == null
                      ? List.of()
                      : List.copyOf(equivalents);
    }

    public static IngredientEntry of(IngredientDto ingredient, List<String> equivalents) {
        return new IngredientEntry(ResourceEntry.of(ingredient.resource()),
                                   ingredient.amountPerOperation().toString(),
                                   equivalents);
    }

    public IngredientDto toDto() {
        return new IngredientDto(resource.toResourceId(), Rate.parse(amountPerOperation));
    }
}
