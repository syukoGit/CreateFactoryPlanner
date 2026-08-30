package fr.syuko.createfactoryplanner.core.math;

public final class Rate implements Comparable<Rate> {

    public static final Rate ZERO = new Rate(0, 1, false);

    private final long numerator;

    private final long denominator;

    private final boolean approximate;

    private Rate(long numerator, long denominator, boolean approximate) {
        this.numerator = numerator;
        this.denominator = denominator;
        this.approximate = approximate;
    }

    public static Rate perTick(long numerator, long denominator) {
        return canonical(numerator, denominator, false);
    }

    public static Rate perOperation(long amount, int durationTicks) {
        if (durationTicks <= 0) {
            throw new IllegalArgumentException("an operation duration must be positive, got " + durationTicks);
        }
        return canonical(amount, durationTicks, false);
    }

    public static Rate of(long whole) {
        return canonical(whole, 1, false);
    }

    private static Rate canonical(long numerator, long denominator, boolean approximate) {
        if (denominator == 0) {
            throw new IllegalArgumentException("a rate denominator cannot be zero");
        }
        if (numerator == Long.MIN_VALUE || denominator == Long.MIN_VALUE) {
            long halvedDenominator = denominator / 2;
            return canonical(numerator / 2,
                             halvedDenominator == 0
                             ? 1
                             : halvedDenominator,
                             true);
        }
        if (numerator == 0) {
            return approximate
                   ? new Rate(0, 1, true)
                   : ZERO;
        }
        long signedNumerator = denominator < 0
                               ? -numerator
                               : numerator;
        long positiveDenominator = Math.abs(denominator);
        long divisor = greatestCommonDivisor(signedNumerator, positiveDenominator);
        return new Rate(signedNumerator / divisor, positiveDenominator / divisor, approximate);
    }

    private static long greatestCommonDivisor(long left, long right) {
        long remaining = Math.abs(left);
        long divisor = Math.abs(right);
        while (divisor != 0) {
            long remainder = remaining % divisor;
            remaining = divisor;
            divisor = remainder;
        }
        return remaining;
    }

    private static boolean halvesLeft(Rate left, Rate right) {
        if (left.denominator != right.denominator) {
            return left.denominator > right.denominator;
        }
        return Math.abs(left.numerator) >= Math.abs(right.numerator);
    }

    private static int compareProducts(long leftFactor, long leftMultiplier, long rightFactor, long rightMultiplier) {
        long leftHigh = Math.multiplyHigh(leftFactor, leftMultiplier);
        long rightHigh = Math.multiplyHigh(rightFactor, rightMultiplier);
        if (leftHigh != rightHigh) {
            return Long.compare(leftHigh, rightHigh);
        }
        return Long.compareUnsigned(leftFactor * leftMultiplier, rightFactor * rightMultiplier);
    }

    public long numerator() {
        return numerator;
    }

    public long denominator() {
        return denominator;
    }

    public boolean isZero() {
        return numerator == 0;
    }

    public boolean isApproximate() {
        return approximate;
    }

    public Rate plus(Rate other) {
        Rate left = this;
        Rate right = other;
        boolean degraded = false;
        while (true) {
            try {
                long addedDenominator = Math.multiplyExact(left.denominator, right.denominator);
                long addedNumerator = Math.addExact(Math.multiplyExact(left.numerator, right.denominator),
                                                    Math.multiplyExact(right.numerator, left.denominator));
                return canonical(addedNumerator, addedDenominator, approximate || other.approximate || degraded);
            } catch (ArithmeticException overflow) {
                if (halvesLeft(left, right)) {
                    left = left.halved();
                } else {
                    right = right.halved();
                }
                degraded = true;
            }
        }
    }

    public Rate minus(Rate other) {
        return plus(other.negated());
    }

    public Rate times(long factor) {
        Rate left = this;
        boolean degraded = false;
        while (true) {
            try {
                return canonical(Math.multiplyExact(left.numerator, factor), left.denominator, approximate || degraded);
            } catch (ArithmeticException overflow) {
                left = left.halved();
                degraded = true;
            }
        }
    }

    public Rate times(Rate other) {
        Rate left = this;
        Rate right = other;
        boolean degraded = false;
        while (true) {
            try {
                return canonical(Math.multiplyExact(left.numerator, right.numerator),
                                 Math.multiplyExact(left.denominator, right.denominator),
                                 approximate || other.approximate || degraded);
            } catch (ArithmeticException overflow) {
                if (halvesLeft(left, right)) {
                    left = left.halved();
                } else {
                    right = right.halved();
                }
                degraded = true;
            }
        }
    }

    public Rate dividedBy(Rate other) {
        if (other.isZero()) {
            throw new ArithmeticException("cannot divide a rate by zero");
        }
        Rate left = this;
        boolean degraded = false;
        while (true) {
            try {
                return canonical(Math.multiplyExact(left.numerator, other.denominator),
                                 Math.multiplyExact(left.denominator, other.numerator),
                                 approximate || other.approximate || degraded);
            } catch (ArithmeticException overflow) {
                left = left.halved();
                degraded = true;
            }
        }
    }

    public Rate min(Rate other) {
        return compareTo(other) <= 0
               ? this
               : other;
    }

    public Rate max(Rate other) {
        return compareTo(other) >= 0
               ? this
               : other;
    }

    public double toDouble(RateUnit unit) {
        return (double) numerator / (double) denominator * unit.ticks();
    }

    @Override
    public int compareTo(Rate other) {
        return compareProducts(numerator, other.denominator, other.numerator, denominator);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Rate rate && numerator == rate.numerator && denominator == rate.denominator;
    }

    @Override
    public int hashCode() {
        return 31 * Long.hashCode(numerator) + Long.hashCode(denominator);
    }

    @Override
    public String toString() {
        return numerator + "/" + denominator + (approximate
                                                ? "~"
                                                : "");
    }

    private Rate negated() {
        return numerator == 0
               ? this
               : new Rate(-numerator, denominator, approximate);
    }

    private Rate halved() {
        long halvedDenominator = denominator / 2;
        return canonical(numerator / 2,
                         halvedDenominator == 0
                         ? 1
                         : halvedDenominator,
                         true);
    }
}