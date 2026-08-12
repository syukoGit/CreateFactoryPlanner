package fr.syuko.createfactoryplanner.integration.recipes;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Collection;
import java.util.List;

public interface RecipeSource {
    boolean isAvailable();

    <I extends RecipeInput, T extends Recipe<I>> List<RecipeHolder<T>> byType(RecipeType<T> type);

    Collection<RecipeHolder<?>> all();
}
