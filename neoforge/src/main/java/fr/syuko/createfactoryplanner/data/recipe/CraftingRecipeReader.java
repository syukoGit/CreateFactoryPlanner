package fr.syuko.createfactoryplanner.data.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public final class CraftingRecipeReader implements RawRecipeReader {

    @Override
    public boolean handles(Recipe<?> recipe) {
        return recipe instanceof CraftingRecipe;
    }

    @Override
    public RawRecipe read(RecipeHolder<?> holder, HolderLookup.Provider registries) {
        CraftingRecipe recipe = (CraftingRecipe) holder.value();

        List<RawIngredient> ingredients = recipe.getIngredients()
                                                .stream()
                                                .filter(ingredient -> !ingredient.isEmpty())
                                                .map(RawRecipes::of)
                                                .toList();

        ItemStack result = recipe.getResultItem(registries);
        List<RawResult> results = result.isEmpty()
                                  ? List.of()
                                  : List.of(new RawResult(RawRecipes.itemIdOf(result), result.getCount(), 1F));

        return new RawRecipe(holder.id().toString(),
                             RawRecipes.recipeTypeOf(recipe),
                             "crafting",
                             ingredients,
                             results,
                             List.of(),
                             List.of(),
                             0,
                             "NONE",
                             List.of());
    }
}