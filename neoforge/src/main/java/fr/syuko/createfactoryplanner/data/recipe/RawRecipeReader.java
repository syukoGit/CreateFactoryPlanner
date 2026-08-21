package fr.syuko.createfactoryplanner.data.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public interface RawRecipeReader {

    boolean handles(Recipe<?> recipe);

    RawRecipe read(RecipeHolder<?> holder, HolderLookup.Provider registries);
}
