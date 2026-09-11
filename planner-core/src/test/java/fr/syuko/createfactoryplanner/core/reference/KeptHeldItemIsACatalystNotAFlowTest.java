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

class KeptHeldItemIsACatalystNotAFlowTest {

    private static final ResourceId AXE = ResourceId.item("minecraft:diamond_axe");

    private static final ResourceId LOG = ResourceId.item("minecraft:oak_log");

    private static final ResourceId STRIPPED = ResourceId.item("minecraft:stripped_oak_log");

    private static final ResourceId SAND_PAPER = ResourceId.item("create:red_sand_paper");

    private static final ResourceId QUARTZ = ResourceId.item("create:rose_quartz");

    private static final ResourceId POLISHED = ResourceId.item("create:polished_rose_quartz");

    @Test
    void aHeldItemTheGameKeepsIsPrimedOncePerMachineAndNeverFlows() {
        RecipeDto stripping = RecipeNormalizer.normalize(new RecipeId("create:deploying/stripped_oak_log"),
                                                         List.of(IngredientDto.of(LOG, 1), IngredientDto.of(AXE, 1)),
                                                         List.of(OutputDto.certain(STRIPPED, 1)),
                                                         0,
                                                         Set.of(AXE));
        assertEquals(1, stripping.catalyst(AXE).orElseThrow().amountPerMachine());
        assertTrue(stripping.ingredient(AXE).isEmpty());
        assertTrue(stripping.output(AXE).isEmpty());
        assertEquals(Rate.of(1), stripping.ingredient(LOG).orElseThrow().amountPerOperation());
    }

    @Test
    void aDamageableHeldItemTheGameDoesNotKeepStaysAnOrdinaryFlow() {
        RecipeDto polishing = RecipeNormalizer.normalize(new RecipeId(
                                                                 "create:sandpaper_polishing/rose_quartz_using_deployer"),
                                                         List.of(IngredientDto.of(QUARTZ, 1),
                                                                 IngredientDto.of(SAND_PAPER, 1)),
                                                         List.of(OutputDto.certain(POLISHED, 1)),
                                                         0,
                                                         Set.of());
        assertTrue(polishing.catalysts().isEmpty());
        assertEquals(Rate.of(1), polishing.ingredient(SAND_PAPER).orElseThrow().amountPerOperation());
    }
}