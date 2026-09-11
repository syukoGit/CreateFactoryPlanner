package fr.syuko.createfactoryplanner.core.recipe;

import fr.syuko.createfactoryplanner.core.model.RecipeId;
import fr.syuko.createfactoryplanner.core.model.ResourceId;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record RecipeDto(RecipeId id, List<IngredientDto> ingredients, List<OutputDto> outputs,
                        List<CatalystDto> catalysts, int declaredDurationTicks) {

    public RecipeDto {
        if (id == null) {
            throw new IllegalArgumentException("a recipe must carry an id");
        }
        ingredients = List.copyOf(ingredients);
        outputs = List.copyOf(outputs);
        catalysts = List.copyOf(catalysts);
        if (declaredDurationTicks < 0) {
            throw new IllegalArgumentException("a declared duration cannot be negative, got " + declaredDurationTicks + " on " + id.value());
        }
        Set<ResourceId> consumed = requireDistinctResources(ingredients.stream().map(IngredientDto::resource).toList(),
                                                            "ingredient",
                                                            id);
        Set<ResourceId> produced = requireDistinctResources(outputs.stream().map(OutputDto::resource).toList(),
                                                            "output",
                                                            id);
        requireDistinctResources(catalysts.stream().map(CatalystDto::resource).toList(), "catalyst", id);
        for (CatalystDto catalyst : catalysts) {
            if (consumed.contains(catalyst.resource()) || produced.contains(catalyst.resource())) {
                throw new IllegalArgumentException("a catalyst never flows, got " + catalyst.resource()
                                                                                            .value() + " both as a catalyst and as a flowing resource on " + id.value());
            }
        }
    }

    private static Set<ResourceId> requireDistinctResources(List<ResourceId> resources, String side, RecipeId id) {
        Set<ResourceId> seen = new HashSet<>();
        for (ResourceId resource : resources) {
            if (!seen.add(resource)) {
                throw new IllegalArgumentException("normalization leaves one " + side + " per resource, got " + resource.value() + " twice on " + id.value());
            }
        }
        return seen;
    }

    public Optional<CatalystDto> catalyst(ResourceId resource) {
        return catalysts.stream().filter(catalyst -> catalyst.resource().equals(resource)).findFirst();
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