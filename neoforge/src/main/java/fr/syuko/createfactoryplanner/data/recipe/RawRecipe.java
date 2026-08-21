package fr.syuko.createfactoryplanner.data.recipe;

import java.util.List;

public record RawRecipe(String id, String recipeType, String reader, List<RawIngredient> ingredients,
                        List<RawResult> results, List<RawFluidIngredient> fluidIngredients,
                        List<RawFluidResult> fluidResults, int declaredDuration, String heatRequirement,
                        List<String> notes) {
}