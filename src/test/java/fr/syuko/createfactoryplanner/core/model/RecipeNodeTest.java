package fr.syuko.createfactoryplanner.core.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecipeNodeTest {
    private static final NamespacedId RECIPE = NamespacedId.parse("create:deploying/precision_mechanism");

    private static final NamespacedId TYPE = NamespacedId.parse("create:deploying");

    private static final ItemKey GRAVEL = ItemKey.item(NamespacedId.parse("minecraft:gravel"));

    private static final ItemKey FLINT = ItemKey.item(NamespacedId.parse("minecraft:flint"));

    private static final ItemKey SAND_PAPER = ItemKey.item(NamespacedId.parse("create:sand_paper"));

    private static RecipeNode node(List<InputStack> inputs, List<OutputStack> outputs) {
        return new RecipeNode(RECIPE, TYPE, inputs, outputs, 100, HeatRequirement.NONE);
    }

    @Test
    void splitsConsumedInputsFromCatalysts() {
        InputStack gravel = new InputStack(List.of(GRAVEL), GRAVEL, 1, true);
        InputStack sandPaper = new InputStack(List.of(SAND_PAPER), SAND_PAPER, 1, false);

        RecipeNode recipe = node(List.of(gravel, sandPaper), List.of(OutputStack.guaranteed(FLINT, 1)));

        assertEquals(List.of(gravel), recipe.consumedInputs());
        assertEquals(List.of(sandPaper), recipe.catalysts());
    }

    @Test
    void sumsTheExpectationOfRepeatedOutputs() {
        RecipeNode recipe = node(List.of(new InputStack(List.of(GRAVEL), GRAVEL, 1, true)),
                                 List.of(OutputStack.guaranteed(FLINT, 1), new OutputStack(FLINT, 1, 0.25f)));

        assertEquals(1.25, recipe.expectedOutputOf(FLINT), 1e-6);
        assertEquals(0.0, recipe.expectedOutputOf(GRAVEL), 1e-9);
    }

    @Test
    void acceptsZeroDurationBecauseTheMachineDefinesIt() {
        RecipeNode recipe = new RecipeNode(RECIPE,
                                           TYPE,
                                           List.of(new InputStack(List.of(GRAVEL), GRAVEL, 1, true)),
                                           List.of(OutputStack.guaranteed(FLINT, 1)),
                                           0,
                                           HeatRequirement.NONE);

        assertEquals(0, recipe.durationTicks());
    }

    @Test
    void rejectsNegativeDuration() {
        assertThrows(IllegalArgumentException.class,
                     () -> new RecipeNode(RECIPE,
                                          TYPE,
                                          List.of(new InputStack(List.of(GRAVEL), GRAVEL, 1, true)),
                                          List.of(OutputStack.guaranteed(FLINT, 1)),
                                          -1,
                                          HeatRequirement.NONE));
    }
}