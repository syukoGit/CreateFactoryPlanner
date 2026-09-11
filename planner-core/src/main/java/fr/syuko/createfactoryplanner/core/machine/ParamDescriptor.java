package fr.syuko.createfactoryplanner.core.machine;

public record ParamDescriptor(String id, int min, int max, int defaultValue, boolean affectsThroughput) {

    public static final String RPM = "rpm";

    public static final String FAN_COUNT = "fan_count";

    public ParamDescriptor {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("a parameter id cannot be blank");
        }
        if (min < 0) {
            throw new IllegalArgumentException("a parameter cannot go below zero, got " + min + " on " + id);
        }
        if (max < min) {
            throw new IllegalArgumentException("a parameter ceiling cannot fall below its floor, got " + max + " under " + min + " on " + id);
        }
        if (defaultValue < min || defaultValue > max) {
            throw new IllegalArgumentException("a parameter default must be usable, got " + defaultValue + " outside [" + min + ", " + max + "] on " + id);
        }
    }

    public static ParamDescriptor rotationSpeed(MachineProfile profile) {
        return new ParamDescriptor(RPM,
                                   Math.max(1, profile.minimumRpm()),
                                   profile.maximumRpm(),
                                   profile.defaultRpm(),
                                   true);
    }

    public int clamp(int value) {
        return Math.clamp(value, min, max);
    }

    public boolean accepts(int value) {
        return value >= min && value <= max;
    }
}