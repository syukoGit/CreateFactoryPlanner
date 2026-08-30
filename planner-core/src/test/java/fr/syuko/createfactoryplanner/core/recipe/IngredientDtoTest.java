package fr.syuko.createfactoryplanner.core.recipe;

import fr.syuko.createfactoryplanner.core.model.ResourceId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IngredientDtoTest {

    @Test
    void carriesTheAmountOneOperationConsumes() {
        IngredientDto ingredient = new IngredientDto(ResourceId.item("minecraft:iron_ingot"), 4);
        assertEquals("minecraft:iron_ingot", ingredient.resource().value());
        assertEquals(4, ingredient.amountPerOperation());
    }

    @Test
    void refusesAnAmountNormalizationShouldHaveRemoved() {
        ResourceId iron = ResourceId.item("minecraft:iron_ingot");
        assertThrows(IllegalArgumentException.class, () -> new IngredientDto(iron, 0));
        assertThrows(IllegalArgumentException.class, () -> new IngredientDto(iron, -1));
    }

    @Test
    void refusesAMissingResource() {
        assertThrows(IllegalArgumentException.class, () -> new IngredientDto(null, 1));
    }
}
