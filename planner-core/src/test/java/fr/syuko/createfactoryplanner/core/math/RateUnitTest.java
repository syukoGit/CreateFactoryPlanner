package fr.syuko.createfactoryplanner.core.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateUnitTest {

    @Test
    void convertsExactlyBetweenTicksSecondsAndMinutes() {
        assertEquals(1, RateUnit.TICK.ticks());
        assertEquals(20, RateUnit.SECOND.ticks());
        assertEquals(1200, RateUnit.MINUTE.ticks());
        assertEquals(RateUnit.MINUTE.ticks(), RateUnit.SECOND.ticks() * 60);
    }
}
