package fr.syuko.createfactoryplanner.integration.recipes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.crafting.*;

import java.util.Collection;
import java.util.List;

public final class ClientRecipeSource implements RecipeSource {
    @Override
    public boolean isAvailable() {
        return Minecraft.getInstance().level != null;
    }

    @Override
    public <I extends RecipeInput, T extends Recipe<I>> List<RecipeHolder<T>> byType(RecipeType<T> type) {
        return recipeManager().getAllRecipesFor(type);
    }

    @Override
    public Collection<RecipeHolder<?>> all() {
        return recipeManager().getRecipes();
    }

    private RecipeManager recipeManager() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            throw new IllegalStateException("Recipes are only readable once a client level is loaded");
        }
        return level.getRecipeManager();
    }
}
