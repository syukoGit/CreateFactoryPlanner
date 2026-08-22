package fr.syuko.createfactoryplanner.data.recipe;

import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SequencedAssemblyRecipeReader implements RawRecipeReader {

    private static List<RawResult> resultsOf(SequencedAssemblyRecipe recipe) {
        float totalWeight = 0F;
        for (ProcessingOutput output : recipe.resultPool) {
            totalWeight += output.getChance();
        }
        float total = totalWeight;
        return recipe.resultPool.stream().map(output -> {
            ItemStack stack = output.getStack();
            return new RawResult(RawRecipes.itemIdOf(stack), stack.getCount(), output.getChance() / total);
        }).toList();
    }

    private static List<String> notesOf(SequencedAssemblyRecipe recipe,
                                        List<SequencedRecipe<?>> sequence,
                                        int loops,
                                        String transitional) {
        List<String> steps = sequence.stream().map(step -> RawRecipes.recipeTypeOf(step.getRecipe())).toList();
        return List.of("black box over " + sequence.size() + " steps repeated " + loops + " times, " + sequence.size() * loops + " operations per item",
                       "steps: " + String.join(", ", steps),
                       "transitional item: " + transitional,
                       "results are an exclusive weighted draw, one result per item, chances are normalized weights");
    }

    private static boolean isTransitional(Ingredient ingredient, String transitional) {
        return Arrays.stream(ingredient.getItems()).map(RawRecipes::itemIdOf).anyMatch(transitional::equals);
    }

    @Override
    public boolean handles(Recipe<?> recipe) {
        return recipe instanceof SequencedAssemblyRecipe;
    }

    @Override
    public RawRecipe read(RecipeHolder<?> holder, HolderLookup.Provider registries) {
        SequencedAssemblyRecipe recipe = (SequencedAssemblyRecipe) holder.value();
        String transitional = RawRecipes.itemIdOf(recipe.getTransitionalItem());
        List<SequencedRecipe<?>> sequence = recipe.getSequence();
        int loops = recipe.getLoops();

        List<RawIngredient> ingredients = new ArrayList<>();
        List<RawFluidIngredient> fluidIngredients = new ArrayList<>();
        ingredients.add(RawRecipes.of(recipe.getIngredient()));

        for (int loop = 0; loop < loops; loop++) {
            for (SequencedRecipe<?> step : sequence) {
                ProcessingRecipe<?, ?> stepRecipe = step.getRecipe();
                stepRecipe.getIngredients()
                          .stream()
                          .filter(ingredient -> !isTransitional(ingredient, transitional))
                          .map(RawRecipes::of)
                          .forEach(ingredients::add);
                stepRecipe.getFluidIngredients().stream().map(RawRecipes::of).forEach(fluidIngredients::add);
            }
        }

        return new RawRecipe(holder.id().toString(),
                             RawRecipes.recipeTypeOf(recipe),
                             "sequenced_assembly",
                             List.copyOf(ingredients),
                             resultsOf(recipe),
                             List.copyOf(fluidIngredients),
                             List.of(),
                             0,
                             "NONE",
                             notesOf(recipe, sequence, loops, transitional));
    }
}