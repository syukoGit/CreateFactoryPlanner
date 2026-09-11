package fr.syuko.createfactoryplanner.core.reference;

import fr.syuko.createfactoryplanner.core.math.Rate;
import fr.syuko.createfactoryplanner.core.model.RecipeId;
import fr.syuko.createfactoryplanner.core.model.ResourceId;
import fr.syuko.createfactoryplanner.core.recipe.IngredientDto;
import fr.syuko.createfactoryplanner.core.recipe.OutputDto;
import fr.syuko.createfactoryplanner.core.recipe.RecipeDto;
import fr.syuko.createfactoryplanner.core.recipe.RecipeNormalizer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FourIngotsInFourSlotsBecomeOneInputTest {

    private static final RecipeId CRAFTING = new RecipeId("minecraft:iron_block");

    private static final ResourceId INGOT = ResourceId.item("minecraft:iron_ingot");

    private static final ResourceId BLOCK = ResourceId.item("minecraft:iron_block");

    @Test
    void fourSlotsOfTheSameIngotBecomeOneIngredientOfFour() {
        RecipeDto normalized = RecipeNormalizer.normalize(CRAFTING,
                                                          List.of(IngredientDto.of(INGOT, 1),
                                                                  IngredientDto.of(INGOT, 1),
                                                                  IngredientDto.of(INGOT, 1),
                                                                  IngredientDto.of(INGOT, 1)),
                                                          List.of(OutputDto.certain(BLOCK, 1)),
                                                          0);
        assertEquals(1, normalized.ingredients().size());
        assertEquals(Rate.of(4), normalized.ingredient(INGOT).orElseThrow().amountPerOperation());
    }
}
