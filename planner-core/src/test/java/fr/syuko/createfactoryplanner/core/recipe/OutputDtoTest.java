package fr.syuko.createfactoryplanner.core.recipe;

import fr.syuko.createfactoryplanner.core.math.Rate;
import fr.syuko.createfactoryplanner.core.model.ResourceId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OutputDtoTest {

    private static final ResourceId IRON = ResourceId.item("minecraft:iron_ingot");

    @Test
    void keepsTheGuaranteedPartBesideTheExpectation() {
        OutputDto output = new OutputDto(IRON, 1, Rate.ratio(5, 4));
        assertEquals(1, output.guaranteed());
        assertEquals(Rate.ratio(5, 4), output.expectedPerOperation());
        assertTrue(output.isProbabilistic());
    }

    @Test
    void readsACertainOutputAsItsOwnExpectation() {
        OutputDto output = OutputDto.certain(IRON, 3);
        assertEquals(3, output.guaranteed());
        assertEquals(Rate.of(3), output.expectedPerOperation());
        assertFalse(output.isProbabilistic());
    }

    @Test
    void acceptsAnExclusiveDrawWithoutAGuaranteedPart() {
        OutputDto output = new OutputDto(IRON, 0, Rate.ratio(3, 10));
        assertEquals(0, output.guaranteed());
        assertTrue(output.isProbabilistic());
    }

    @Test
    void refusesAnExpectationBelowItsGuaranteedPart() {
        assertThrows(IllegalArgumentException.class, () -> new OutputDto(IRON, 2, Rate.ratio(5, 4)));
    }

    @Test
    void refusesANegativeGuaranteedPart() {
        assertThrows(IllegalArgumentException.class, () -> new OutputDto(IRON, -1, Rate.ZERO));
    }

    @Test
    void refusesAMissingExpectation() {
        assertThrows(IllegalArgumentException.class, () -> new OutputDto(IRON, 0, null));
    }
}