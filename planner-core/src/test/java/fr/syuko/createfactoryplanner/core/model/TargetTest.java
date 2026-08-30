package fr.syuko.createfactoryplanner.core.model;

import fr.syuko.createfactoryplanner.core.math.Rate;
import fr.syuko.createfactoryplanner.core.math.RateUnit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TargetTest {

    private static final ResourceId IRON = ResourceId.item("minecraft:iron_ingot");

    @Test
    void namesAResourceAndTheRateItIsAimedAt() {
        Target target = new Target(IRON, Rate.perOperation(10, 20));
        assertEquals(IRON, target.resource());
        assertEquals(10.0, target.rate().toDouble(RateUnit.SECOND));
    }

    @Test
    void refusesARateThatAimsAtNothing() {
        assertThrows(IllegalArgumentException.class, () -> new Target(IRON, Rate.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new Target(IRON, Rate.perTick(-1, 2)));
    }

    @Test
    void refusesAnUnnamedTarget() {
        assertThrows(IllegalArgumentException.class, () -> new Target(null, Rate.of(1)));
        assertThrows(IllegalArgumentException.class, () -> new Target(IRON, null));
    }
}
