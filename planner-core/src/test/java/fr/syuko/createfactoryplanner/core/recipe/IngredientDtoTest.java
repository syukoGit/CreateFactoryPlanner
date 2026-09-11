package fr.syuko.createfactoryplanner.core.recipe;

import fr.syuko.createfactoryplanner.core.math.Rate;
import fr.syuko.createfactoryplanner.core.model.ResourceId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IngredientDtoTest {

    @Test
    void carriesTheAmountOneOperationConsumes() {
        IngredientDto ingredient = IngredientDto.of(ResourceId.item("minecraft:iron_ingot"), 4);
        assertEquals("minecraft:iron_ingot", ingredient.resource().value());
        assertEquals(Rate.of(4), ingredient.amountPerOperation());
    }

    @Test
    void carriesTheFractionalAmountANetBalanceLeaves() {
        IngredientDto obsidian = new IngredientDto(ResourceId.item("minecraft:obsidian"), Rate.ratio(1, 4));
        assertEquals(Rate.ratio(1, 4), obsidian.amountPerOperation());
    }

    @Test
    void refusesAnAmountNormalizationShouldHaveRemoved() {
        ResourceId iron = ResourceId.item("minecraft:iron_ingot");
        assertThrows(IllegalArgumentException.class, () -> IngredientDto.of(iron, 0));
        assertThrows(IllegalArgumentException.class, () -> IngredientDto.of(iron, -1));
        assertThrows(IllegalArgumentException.class, () -> new IngredientDto(iron, null));
    }

    @Test
    void refusesAMissingResource() {
        assertThrows(IllegalArgumentException.class, () -> IngredientDto.of(null, 1));
    }
}
