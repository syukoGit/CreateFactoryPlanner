package fr.syuko.createfactoryplanner.data.recipe;

import java.util.List;

public record RawFluidIngredient(List<String> fluids, int amount) {
}
