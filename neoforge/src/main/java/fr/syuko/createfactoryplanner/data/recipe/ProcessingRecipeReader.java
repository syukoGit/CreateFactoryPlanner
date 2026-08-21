package fr.syuko.createfactoryplanner.data.recipe;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public final class ProcessingRecipeReader implements RawRecipeReader {

    @Override
    public boolean handles(Recipe<?> recipe) {
        return recipe instanceof ProcessingRecipe<?, ?>;
    }

    @Override
    public RawRecipe read(RecipeHolder<?> holder, HolderLookup.Provider registries) {
        ProcessingRecipe<?, ?> recipe = (ProcessingRecipe<?, ?>) holder.value();

        List<RawIngredient> ingredients = recipe.getIngredients().stream().map(RawRecipes::of).toList();

        List<RawResult> results = recipe.getRollableResults().stream().map(output -> {
            ItemStack stack = output.getStack();
            return new RawResult(RawRecipes.itemIdOf(stack), stack.getCount(), output.getChance());
        }).toList();

        List<RawFluidIngredient> fluidIngredients = recipe.getFluidIngredients().stream().map(RawRecipes::of).toList();

        List<RawFluidResult> fluidResults = recipe.getFluidResults().stream().map(RawRecipes::of).toList();

        return new RawRecipe(holder.id().toString(),
                             RawRecipes.recipeTypeOf(recipe),
                             "processing",
                             ingredients,
                             results,
                             fluidIngredients,
                             fluidResults,
                             recipe.getProcessingDuration(),
                             String.valueOf(recipe.getRequiredHeat()),
                             List.of());
    }
}