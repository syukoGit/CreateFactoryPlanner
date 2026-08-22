package fr.syuko.createfactoryplanner.data.dump;

import fr.syuko.createfactoryplanner.data.recipe.HarvestedRecipe;
import fr.syuko.createfactoryplanner.data.recipe.RecipeTypeEntry;

import java.util.List;

public record RecipeDump(DumpMeta meta, List<RecipeTypeEntry> types, List<RuleEntry> rules,
                         List<HarvestedRecipe> recipes, List<String> problems) {

    public record DumpMeta(String generatedAt, String modVersion, String createVersion, int typeCount, int recipeCount,
                           List<String> loadedMods) {
    }
}