package fr.syuko.createfactoryplanner.data.recipe;

import java.util.List;

public record RawIngredient(List<String> tags, List<String> items, boolean custom, boolean allDamageable,
                            boolean allWithCraftingRemainder, boolean keptUntouched) {

    public RawIngredient markedAsKept() {
        return new RawIngredient(tags, items, custom, allDamageable, allWithCraftingRemainder, true);
    }
}
