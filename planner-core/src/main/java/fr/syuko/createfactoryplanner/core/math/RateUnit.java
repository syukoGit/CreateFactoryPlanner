package fr.syuko.createfactoryplanner.core.math;

public enum RateUnit {

    TICK(1),

    SECOND(20),

    MINUTE(1200);

    private final long ticks;

    RateUnit(long ticks) {
        this.ticks = ticks;
    }

    public long ticks() {
        return ticks;
    }
}
