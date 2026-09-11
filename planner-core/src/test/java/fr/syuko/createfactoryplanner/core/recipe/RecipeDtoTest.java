package fr.syuko.createfactoryplanner.core.recipe;

import fr.syuko.createfactoryplanner.core.math.Rate;
import fr.syuko.createfactoryplanner.core.model.RecipeId;
import fr.syuko.createfactoryplanner.core.model.ResourceId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecipeDtoTest {

    private static final RecipeId CRUSHING_IRON = new RecipeId("create:crushing/iron_ore");

    private static final ResourceId IRON_ORE = ResourceId.item("minecraft:iron_ore");

    private static final ResourceId CRUSHED_IRON = ResourceId.item("create:crushed_raw_iron");

    private static RecipeDto crushing() {
        return new RecipeDto(CRUSHING_IRON,
                             List.of(IngredientDto.of(IRON_ORE, 1)),
                             List.of(new OutputDto(CRUSHED_IRON, 1, Rate.ratio(5, 4))),
                             List.of(),
                             250);
    }

    @Test
    void carriesWhatOneOperationConsumesAndProduces() {
        RecipeDto recipe = crushing();
        assertEquals(250, recipe.declaredDurationTicks());
        assertTrue(recipe.declaresItsDuration());
        assertEquals(Rate.of(1), recipe.ingredient(IRON_ORE).orElseThrow().amountPerOperation());
        assertEquals(Rate.ratio(5, 4), recipe.output(CRUSHED_IRON).orElseThrow().expectedPerOperation());
        assertTrue(recipe.output(IRON_ORE).isEmpty());
    }

    @Test
    void acceptsARecipeThatDeclaresNoDuration() {
        RecipeDto recipe = new RecipeDto(CRUSHING_IRON, List.of(), List.of(), List.of(), 0);
        assertFalse(recipe.declaresItsDuration());
    }

    @Test
    void refusesTheSameResourceTwiceOnOneSide() {
        List<IngredientDto> twice = List.of(IngredientDto.of(IRON_ORE, 1), IngredientDto.of(IRON_ORE, 3));
        assertThrows(IllegalArgumentException.class,
                     () -> new RecipeDto(CRUSHING_IRON, twice, List.of(), List.of(), 0));

        List<OutputDto> bothOutputs = List.of(OutputDto.certain(CRUSHED_IRON, 1),
                                              new OutputDto(CRUSHED_IRON, 0, Rate.ratio(1, 4)));
        assertThrows(IllegalArgumentException.class,
                     () -> new RecipeDto(CRUSHING_IRON, List.of(), bothOutputs, List.of(), 0));
    }

    @Test
    void keepsAResourceThatSitsOnBothSidesWithoutDurability() {
        RecipeDto recipe = new RecipeDto(CRUSHING_IRON,
                                         List.of(IngredientDto.of(IRON_ORE, 1)),
                                         List.of(OutputDto.certain(IRON_ORE, 1)),
                                         List.of(),
                                         0);
        assertTrue(recipe.ingredient(IRON_ORE).isPresent());
        assertTrue(recipe.output(IRON_ORE).isPresent());
    }

    @Test
    void refusesANegativeDuration() {
        assertThrows(IllegalArgumentException.class,
                     () -> new RecipeDto(CRUSHING_IRON, List.of(), List.of(), List.of(), -1));
    }

    @Test
    void refusesAMissingId() {
        assertThrows(IllegalArgumentException.class, () -> new RecipeDto(null, List.of(), List.of(), List.of(), 0));
    }
}