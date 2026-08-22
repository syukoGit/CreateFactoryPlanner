package fr.syuko.createfactoryplanner.data.recipe;

import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public final class ItemApplicationRecipeReader implements RawRecipeReader {

    private static final int HELD_ITEM_INDEX = 1;

    private final ProcessingRecipeReader body = new ProcessingRecipeReader();

    private static List<String> notesOf(ItemApplicationRecipe recipe) {
        return List.of("ingredient " + HELD_ITEM_INDEX + " is the held item",
                       recipe.shouldKeepHeldItem()
                       ? "held item is kept untouched, keep_held_item is true"
                       : "held item is damaged when damageable, consumed otherwise");
    }

    @Override
    public boolean handles(Recipe<?> recipe) {
        return recipe instanceof ItemApplicationRecipe;
    }

    @Override
    public RawRecipe read(RecipeHolder<?> holder, HolderLookup.Provider registries) {
        ItemApplicationRecipe recipe = (ItemApplicationRecipe) holder.value();
        RawRecipe raw = body.read(holder, registries);
        return new RawRecipe(raw.id(),
                             raw.recipeType(),
                             "item_application",
                             raw.ingredients(),
                             raw.results(),
                             raw.fluidIngredients(),
                             raw.fluidResults(),
                             raw.declaredDuration(),
                             raw.heatRequirement(),
                             notesOf(recipe));
    }
}