package fr.syuko.createfactoryplanner.core.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateTest {

    private static final Rate OVERFLOWING_PRODUCT = Rate.perTick(Long.MAX_VALUE / 2, 3)
                                                        .times(Rate.perTick(Long.MAX_VALUE / 2, 7));

    @Test
    void normalizesByGreatestCommonDivisor() {
        assertEquals(Rate.perTick(1, 2), Rate.perTick(4, 8));
        assertEquals(1, Rate.perTick(4, 8).numerator());
        assertEquals(2, Rate.perTick(4, 8).denominator());
    }

    @Test
    void keepsTheDenominatorPositive() {
        assertEquals(Rate.perTick(1, 2), Rate.perTick(-1, -2));
        assertEquals(2, Rate.perTick(1, -2).denominator());
        assertEquals(-1, Rate.perTick(1, -2).numerator());
    }

    @Test
    void collapsesEveryZeroToTheSameForm() {
        assertTrue(Rate.perTick(0, 7).isZero());
        assertEquals(1, Rate.perTick(0, 7).denominator());
        assertEquals(Rate.ZERO, Rate.perTick(0, 7));
    }

    @Test
    void leavesItsOperandUnchangedOnNeutralElements() {
        Rate third = Rate.perTick(1, 3);
        assertEquals(third, third.plus(Rate.ZERO));
        assertEquals(third, third.minus(Rate.ZERO));
        assertEquals(third, third.times(1));
        assertEquals(third, third.times(Rate.of(1)));
        assertTrue(third.times(0).isZero());
        assertTrue(Rate.ZERO.times(third).isZero());
    }

    @Test
    void addsSixThirdsToExactlyTwo() {
        Rate total = Rate.ZERO;
        for (int addition = 0; addition < 6; addition++) {
            total = total.plus(Rate.perTick(1, 3));
        }
        assertEquals(Rate.of(2), total);
        assertFalse(total.isApproximate());
    }

    @Test
    void doesNotDriftOverAChainOfCoprimeDurations() {
        Rate total = Rate.ZERO;
        for (int duration : new int[] { 100, 240, 20, 151, 30, 16 }) {
            total = total.plus(Rate.perOperation(1, duration));
        }
        assertEquals(Rate.perTick(1, 100)
                         .plus(Rate.perTick(1, 240))
                         .plus(Rate.perTick(1, 20))
                         .plus(Rate.perTick(1, 151))
                         .plus(Rate.perTick(1, 30))
                         .plus(Rate.perTick(1, 16)), total);
        assertFalse(total.isApproximate());
    }

    @Test
    void subtractsAndMultipliesExactly() {
        assertEquals(Rate.perTick(1, 6), Rate.perTick(1, 2).minus(Rate.perTick(1, 3)));
        assertEquals(Rate.perTick(1, 6), Rate.perTick(1, 2).times(Rate.perTick(1, 3)));
        assertEquals(Rate.perTick(3, 2), Rate.perTick(1, 2).times(3));
        assertEquals(Rate.perTick(-1, 6), Rate.perTick(1, 3).minus(Rate.perTick(1, 2)));
    }

    @Test
    void dividesIntoADimensionlessRatio() {
        assertEquals(Rate.perTick(1, 2), Rate.of(3).dividedBy(Rate.of(6)));
        assertEquals(Rate.of(2), Rate.perTick(1, 3).dividedBy(Rate.perTick(1, 6)));
    }

    @Test
    void convertsToTheDisplayUnitOnly() {
        Rate perOperation = Rate.perOperation(1, 20);
        assertEquals(Rate.perTick(1, 20), perOperation);
        assertEquals(0.05, perOperation.toDouble(RateUnit.TICK));
        assertEquals(1.0, perOperation.toDouble(RateUnit.SECOND));
        assertEquals(60.0, perOperation.toDouble(RateUnit.MINUTE));
    }

    @Test
    void degradesInsteadOfOverflowing() {
        assertTrue(OVERFLOWING_PRODUCT.isApproximate());
        assertTrue(OVERFLOWING_PRODUCT.numerator() > 0);
        assertTrue(OVERFLOWING_PRODUCT.denominator() > 0);
    }

    @Test
    void spreadsApproximationToEveryResultItTouches() {
        assertTrue(OVERFLOWING_PRODUCT.plus(Rate.of(1)).isApproximate());
        assertTrue(Rate.of(1).plus(OVERFLOWING_PRODUCT).isApproximate());
        assertTrue(OVERFLOWING_PRODUCT.times(2).isApproximate());
        assertTrue(OVERFLOWING_PRODUCT.dividedBy(Rate.of(2)).isApproximate());
        assertFalse(Rate.of(1).plus(Rate.of(1)).isApproximate());
    }

    @Test
    void ignoresApproximationWhenComparingValues() {
        Rate degraded = Rate.perTick(Long.MAX_VALUE, 1).times(2);
        Rate exact = Rate.perTick(Long.MAX_VALUE - 1, 1);
        assertTrue(degraded.isApproximate());
        assertFalse(exact.isApproximate());
        assertEquals(exact, degraded);
        assertEquals(exact.hashCode(), degraded.hashCode());
        assertEquals(0, exact.compareTo(degraded));
    }

    @Test
    void comparesWithoutOverflowingTheCrossProducts() {
        Rate larger = Rate.perTick(Long.MAX_VALUE, 3);
        Rate smaller = Rate.perTick(Long.MAX_VALUE, 5);
        assertTrue(larger.compareTo(smaller) > 0);
        assertTrue(smaller.compareTo(larger) < 0);
        assertEquals(larger, larger.max(smaller));
        assertEquals(smaller, larger.min(smaller));
    }

    @Test
    void ordersNegativeRatesBelowZero() {
        Rate negative = Rate.perTick(-1, 3);
        assertTrue(negative.compareTo(Rate.ZERO) < 0);
        assertTrue(Rate.ZERO.compareTo(negative) > 0);
        assertEquals(Rate.ZERO, negative.max(Rate.ZERO));
    }

    @Test
    void refusesADenominatorOfZero() {
        assertThrows(IllegalArgumentException.class, () -> Rate.perTick(1, 0));
        assertThrows(IllegalArgumentException.class, () -> Rate.perOperation(1, 0));
        assertThrows(IllegalArgumentException.class, () -> Rate.perOperation(1, -20));
    }

    @Test
    void refusesToDivideByZero() {
        assertThrows(ArithmeticException.class, () -> Rate.of(1).dividedBy(Rate.ZERO));
    }

    @Test
    void survivesTheUnnegatableExtreme() {
        Rate extreme = Rate.perTick(Long.MIN_VALUE, 1);
        assertTrue(extreme.isApproximate());
        assertTrue(extreme.numerator() < 0);
        assertEquals(1, extreme.denominator());
        assertTrue(extreme.minus(Rate.of(1)).numerator() < 0);
    }
}