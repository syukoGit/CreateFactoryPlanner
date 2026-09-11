package fr.syuko.createfactoryplanner.core.io;

import fr.syuko.createfactoryplanner.core.model.RecipeId;
import fr.syuko.createfactoryplanner.core.recipe.RecipeDto;

import java.util.List;

public record RecipeEntry(String id, List<String> machines, int declaredDurationTicks,
                          List<IngredientEntry> ingredients, List<OutputEntry> outputs, List<CatalystEntry> catalysts,
                          List<String> pending) {

    public RecipeEntry {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("a recipe entry cannot be blank");
        }
        machines = machines == null
                   ? List.of()
                   : List.copyOf(machines);
        ingredients = ingredients == null
                      ? List.of()
                      : List.copyOf(ingredients);
        outputs = outputs == null
                  ? List.of()
                  : List.copyOf(outputs);
        catalysts = catalysts == null
                    ? List.of()
                    : List.copyOf(catalysts);
        pending = pending == null
                  ? List.of()
                  : List.copyOf(pending);
    }

    public RecipeId recipe() {
        return new RecipeId(id);
    }

    public RecipeDto toDto() {
        return new RecipeDto(recipe(),
                             ingredients.stream().map(IngredientEntry::toDto).toList(),
                             outputs.stream().map(OutputEntry::toDto).toList(),
                             catalysts.stream().map(CatalystEntry::toDto).toList(),
                             declaredDurationTicks);
    }

    public boolean isSettled() {
        return pending.isEmpty();
    }
}