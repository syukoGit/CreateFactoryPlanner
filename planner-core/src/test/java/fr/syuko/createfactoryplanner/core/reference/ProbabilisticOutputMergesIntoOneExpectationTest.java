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
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProbabilisticOutputMergesIntoOneExpectationTest {

    private static final RecipeId CRUSHING = new RecipeId("create:crushing/iron_ore");

    private static final ResourceId ORE = ResourceId.item("minecraft:iron_ore");

    private static final ResourceId IRON = ResourceId.item("create:crushed_raw_iron");

    @Test
    void oneIronPlusTwentyFivePercentIronBecomesOneAndAQuarter() {
        RecipeDto normalized = RecipeNormalizer.normalize(CRUSHING,
                                                          List.of(IngredientDto.of(ORE, 1)),
                                                          List.of(OutputDto.certain(IRON, 1),
                                                                  new OutputDto(IRON, 0, Rate.ratio(1, 4))),
                                                          400);
        assertEquals(1, normalized.outputs().size());
        OutputDto iron = normalized.output(IRON).orElseThrow();
        assertEquals(Rate.ratio(5, 4), iron.expectedPerOperation());
        assertEquals(1, iron.guaranteed());
        assertTrue(iron.isProbabilistic());
    }

    @Test
    void keepsTheGuaranteedFloorApartFromTheAverage() {
        RecipeDto normalized = RecipeNormalizer.normalize(CRUSHING,
                                                          List.of(IngredientDto.of(ORE, 1)),
                                                          List.of(new OutputDto(IRON, 0, Rate.ratio(1, 2)),
                                                                  new OutputDto(IRON, 0, Rate.ratio(1, 2))),
                                                          400);
        OutputDto iron = normalized.output(IRON).orElseThrow();
        assertEquals(Rate.of(1), iron.expectedPerOperation());
        assertEquals(0, iron.guaranteed());
    }
}