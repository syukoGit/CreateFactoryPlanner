package fr.syuko.createfactoryplanner.data.recipe;

public record RecipeTypeEntry(String id, String recipeType, boolean ownType, int recipeCount, int automatableCount) {
}