package fr.syuko.createfactoryplanner.core.recipe;

import fr.syuko.createfactoryplanner.core.math.Rate;
import fr.syuko.createfactoryplanner.core.model.MachineId;
import fr.syuko.createfactoryplanner.core.model.RecipeId;
import fr.syuko.createfactoryplanner.core.model.ResourceId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecipeDtoTest {

    private static final RecipeId CRUSHING_IRON = new RecipeId("create:crushing/iron_ore");

    private static final ResourceId IRON_ORE = ResourceId.item("minecraft:iron_ore");

    private static final ResourceId CRUSHED_IRON = ResourceId.item("create:crushed_raw_iron");

    private static final ResourceId SAND_PAPER = ResourceId.item("create:sand_paper");

    private static RecipeDto crushing() {
        return new RecipeDto(CRUSHING_IRON,
                             List.of(new IngredientDto(IRON_ORE, 1)),
                             List.of(new OutputDto(CRUSHED_IRON, 1, Rate.ratio(5, 4))),
                             List.of(),
                             250,
                             null);
    }

    @Test
    void carriesWhatOneOperationConsumesAndProduces() {
        RecipeDto recipe = crushing();
        assertEquals(250, recipe.declaredDurationTicks());
        assertTrue(recipe.declaresItsDuration());
        assertFalse(recipe.isSequencedAssembly());
        assertEquals(1, recipe.ingredient(IRON_ORE).orElseThrow().amountPerOperation());
        assertEquals(Rate.ratio(5, 4), recipe.output(CRUSHED_IRON).orElseThrow().expectedPerOperation());
        assertTrue(recipe.output(IRON_ORE).isEmpty());
    }

    @Test
    void acceptsARecipeThatDeclaresNoDuration() {
        RecipeDto recipe = new RecipeDto(CRUSHING_IRON, List.of(), List.of(), List.of(), 0, null);
        assertFalse(recipe.declaresItsDuration());
    }

    @Test
    void keepsACatalystOutOfBothSides() {
        RecipeDto polishing = new RecipeDto(new RecipeId("create:deploying/polished_rose_quartz"),
                                            List.of(new IngredientDto(IRON_ORE, 1)),
                                            List.of(OutputDto.certain(CRUSHED_IRON, 1)),
                                            List.of(new CatalystDto(SAND_PAPER, 1)),
                                            0,
                                            null);
        assertEquals(1, polishing.catalysts().size());
        assertTrue(polishing.ingredient(SAND_PAPER).isEmpty());
        assertTrue(polishing.output(SAND_PAPER).isEmpty());
    }

    @Test
    void refusesACatalystThatAlsoFlows() {
        List<IngredientDto> ingredients = List.of(new IngredientDto(SAND_PAPER, 1));
        List<CatalystDto> catalysts = List.of(new CatalystDto(SAND_PAPER, 1));
        assertThrows(IllegalArgumentException.class,
                     () -> new RecipeDto(CRUSHING_IRON, ingredients, List.of(), catalysts, 0, null));
    }

    @Test
    void refusesTheSameResourceTwiceOnOneSide() {
        List<IngredientDto> twice = List.of(new IngredientDto(IRON_ORE, 1), new IngredientDto(IRON_ORE, 3));
        assertThrows(IllegalArgumentException.class,
                     () -> new RecipeDto(CRUSHING_IRON, twice, List.of(), List.of(), 0, null));

        List<OutputDto> bothOutputs = List.of(OutputDto.certain(CRUSHED_IRON, 1),
                                              new OutputDto(CRUSHED_IRON, 0, Rate.ratio(1, 4)));
        assertThrows(IllegalArgumentException.class,
                     () -> new RecipeDto(CRUSHING_IRON, List.of(), bothOutputs, List.of(), 0, null));
    }

    @Test
    void keepsAResourceThatSitsOnBothSidesWithoutDurability() {
        RecipeDto recipe = new RecipeDto(CRUSHING_IRON,
                                         List.of(new IngredientDto(IRON_ORE, 1)),
                                         List.of(OutputDto.certain(IRON_ORE, 1)),
                                         List.of(),
                                         0,
                                         null);
        assertTrue(recipe.ingredient(IRON_ORE).isPresent());
        assertTrue(recipe.output(IRON_ORE).isPresent());
    }

    @Test
    void carriesTheInstallationBehindASequencedAssembly() {
        SequenceProfile sequence = new SequenceProfile(List.of(new SequenceStep(new MachineId("deployer"), 7, 0),
                                                               new SequenceStep(new MachineId("mechanical_press"),
                                                                                3,
                                                                                0)));
        RecipeDto assembly = new RecipeDto(new RecipeId("create:sequenced_assembly/precision_mechanism"),
                                           List.of(new IngredientDto(IRON_ORE, 16)),
                                           List.of(new OutputDto(CRUSHED_IRON, 0, Rate.ratio(1, 2))),
                                           List.of(),
                                           0,
                                           sequence);
        assertTrue(assembly.isSequencedAssembly());
        assertEquals(10, assembly.sequence().totalPasses());
    }

    @Test
    void refusesANegativeDuration() {
        assertThrows(IllegalArgumentException.class,
                     () -> new RecipeDto(CRUSHING_IRON, List.of(), List.of(), List.of(), -1, null));
    }

    @Test
    void refusesAMissingId() {
        assertThrows(IllegalArgumentException.class,
                     () -> new RecipeDto(null, List.of(), List.of(), List.of(), 0, null));
    }
}