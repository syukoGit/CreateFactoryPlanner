package fr.syuko.createfactoryplanner.data.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

public record HarvestContext(RecipeManager recipes, Level level) {

    public HolderLookup.Provider registries() {
        return level.registryAccess();
    }
}
