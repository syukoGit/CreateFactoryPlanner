package fr.syuko.createfactoryplanner.data.recipe;

import java.util.List;

public interface RecipeSource {

    List<RecipeTypeEntry> knownTypes();

    List<RawRecipe> allRecipes();
}
