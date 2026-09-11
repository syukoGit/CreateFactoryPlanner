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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetBalanceKeepsOneSideOnlyTest {

    private static final ResourceId OBSIDIAN = ResourceId.item("minecraft:obsidian");

    private static final ResourceId DUST = ResourceId.item("create:powdered_obsidian");

    @Test
    void crushingObsidianLeavesAFractionalInputOnly() {
        RecipeDto normalized = RecipeNormalizer.normalize(new RecipeId("create:crushing/obsidian"),
                                                          List.of(IngredientDto.of(OBSIDIAN, 1)),
                                                          List.of(OutputDto.certain(DUST, 1),
                                                                  new OutputDto(OBSIDIAN, 0, Rate.ratio(3, 4))),
                                                          400,
                                                          Set.of());
        assertEquals(Rate.ratio(1, 4), normalized.ingredient(OBSIDIAN).orElseThrow().amountPerOperation());
        assertTrue(normalized.output(OBSIDIAN).isEmpty());
    }

    @Test
    void aPositiveNetLeavesAnOutputOnly() {
        RecipeDto normalized = RecipeNormalizer.normalize(new RecipeId("create:milling/gravel"),
                                                          List.of(IngredientDto.of(OBSIDIAN, 1)),
                                                          List.of(OutputDto.certain(OBSIDIAN, 3)),
                                                          100,
                                                          Set.of());
        assertTrue(normalized.ingredient(OBSIDIAN).isEmpty());
        OutputDto left = normalized.output(OBSIDIAN).orElseThrow();
        assertEquals(Rate.of(2), left.expectedPerOperation());
        assertEquals(2, left.guaranteed());
    }

    @Test
    void aNullNetWithoutDurabilityKeepsBothSides() {
        RecipeDto normalized = RecipeNormalizer.normalize(new RecipeId("create:deploying/kept"),
                                                          List.of(IngredientDto.of(OBSIDIAN, 1)),
                                                          List.of(OutputDto.certain(OBSIDIAN, 1)),
                                                          0,
                                                          Set.of());
        assertTrue(normalized.ingredient(OBSIDIAN).isPresent());
        assertTrue(normalized.output(OBSIDIAN).isPresent());
    }
}