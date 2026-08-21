package fr.syuko.createfactoryplanner.data.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleItemRecipe;

import java.util.List;

public final class SingleStepRecipeReader implements RawRecipeReader {

    @Override
    public boolean handles(Recipe<?> recipe) {
        return recipe instanceof AbstractCookingRecipe || recipe instanceof SingleItemRecipe;
    }

    @Override
    public RawRecipe read(RecipeHolder<?> holder, HolderLookup.Provider registries) {
        Recipe<?> recipe = holder.value();

        List<RawIngredient> ingredients = recipe.getIngredients()
                                                .stream()
                                                .filter(ingredient -> !ingredient.isEmpty())
                                                .map(RawRecipes::of)
                                                .toList();

        ItemStack result = recipe.getResultItem(registries);
        List<RawResult> results = result.isEmpty()
                                  ? List.of()
                                  : List.of(new RawResult(RawRecipes.itemIdOf(result), result.getCount(), 1F));

        int declaredDuration = recipe instanceof AbstractCookingRecipe cooking
                               ? cooking.getCookingTime()
                               : 0;
        List<String> notes = declaredDuration > 0
                             ? List.of("duration is the vanilla cooking time")
                             : List.of();

        return new RawRecipe(holder.id().toString(),
                             RawRecipes.recipeTypeOf(recipe),
                             "single_step",
                             ingredients,
                             results,
                             List.of(),
                             List.of(),
                             declaredDuration,
                             "NONE",
                             notes);
    }
}