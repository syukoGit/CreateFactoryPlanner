package fr.syuko.createfactoryplanner.core.machine;

import fr.syuko.createfactoryplanner.core.model.MachineId;

import java.util.Map;

public record MachineProfile(MachineId machine, int minimumRpm, int maximumRpm, int defaultRpm,
                             double stressImpactPerRpm, Map<String, Long> formulaScalars) {

    public MachineProfile {
        if (machine == null) {
            throw new IllegalArgumentException("a machine profile must carry a machine");
        }
        formulaScalars = Map.copyOf(formulaScalars);
        if (!Double.isFinite(stressImpactPerRpm) || stressImpactPerRpm < 0) {
            throw new IllegalArgumentException("a stress impact must be finite and positive, got " + stressImpactPerRpm + " on " + machine.value());
        }
        if (minimumRpm < 0 || maximumRpm < 0 || defaultRpm < 0) {
            throw new IllegalArgumentException("a rotation speed cannot be negative on " + machine.value());
        }
        if (minimumRpm > maximumRpm) {
            throw new IllegalArgumentException("a minimum rotation speed cannot exceed the ceiling, got " + minimumRpm + " over " + maximumRpm + " on " + machine.value());
        }
        if (defaultRpm < Math.max(1, minimumRpm) || defaultRpm > maximumRpm) {
            throw new IllegalArgumentException("a default rotation speed must be usable, got " + defaultRpm + " outside [" + Math.max(
                    1,
                    minimumRpm) + ", " + maximumRpm + "] on " + machine.value());
        }
    }

    public long scalar(String id, long fallback) {
        return formulaScalars.getOrDefault(id, fallback);
    }
}