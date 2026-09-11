package fr.syuko.createfactoryplanner.core.recipe;

import fr.syuko.createfactoryplanner.core.model.RecipeId;
import fr.syuko.createfactoryplanner.core.model.ResourceId;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record RecipeDto(RecipeId id, List<IngredientDto> ingredients, List<OutputDto> outputs,
                        int declaredDurationTicks) {

    public RecipeDto {
        if (id == null) {
            throw new IllegalArgumentException("a recipe must carry an id");
        }
        ingredients = List.copyOf(ingredients);
        outputs = List.copyOf(outputs);
        if (declaredDurationTicks < 0) {
            throw new IllegalArgumentException("a declared duration cannot be negative, got " + declaredDurationTicks + " on " + id.value());
        }
        requireDistinctResources(ingredients.stream().map(IngredientDto::resource).toList(), "ingredient", id);
        requireDistinctResources(outputs.stream().map(OutputDto::resource).toList(), "output", id);
    }

    private static void requireDistinctResources(List<ResourceId> resources, String side, RecipeId id) {
        Set<ResourceId> seen = new HashSet<>();
        for (ResourceId resource : resources) {
            if (!seen.add(resource)) {
                throw new IllegalArgumentException("normalization leaves one " + side + " per resource, got " + resource.value() + " twice on " + id.value());
            }
        }
    }

    public boolean declaresItsDuration() {
        return declaredDurationTicks > 0;
    }

    public Optional<IngredientDto> ingredient(ResourceId resource) {
        return ingredients.stream().filter(ingredient -> ingredient.resource().equals(resource)).findFirst();
    }

    public Optional<OutputDto> output(ResourceId resource) {
        return outputs.stream().filter(output -> output.resource().equals(resource)).findFirst();
    }
}
