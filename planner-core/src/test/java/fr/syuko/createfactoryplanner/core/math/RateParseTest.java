package fr.syuko.createfactoryplanner.core.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RateParseTest {

    @Test
    void readsBackWhateverItPrinted() {
        assertEquals(Rate.ratio(5, 4), Rate.parse(Rate.ratio(5, 4).toString()));
        assertEquals(Rate.of(3), Rate.parse(Rate.of(3).toString()));
        assertEquals(Rate.ZERO, Rate.parse(Rate.ZERO.toString()));
    }

    @Test
    void acceptsAWholeNumberWithoutADenominator() {
        assertEquals(Rate.of(7), Rate.parse("7"));
    }

    @Test
    void refusesWhatIsNotARate() {
        assertThrows(IllegalArgumentException.class, () -> Rate.parse(""));
        assertThrows(IllegalArgumentException.class, () -> Rate.parse(null));
        assertThrows(IllegalArgumentException.class, () -> Rate.parse("five quarters"));
        assertThrows(IllegalArgumentException.class, () -> Rate.parse("5/0"));
    }
}