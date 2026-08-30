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
        if (maximumRpm == 0) {
            if (minimumRpm != 0 || defaultRpm != 0 || stressImpactPerRpm != 0) {
                throw new IllegalArgumentException(
                        "a machine without a rotation ceiling is not kinetic and carries no speed nor stress, on " + machine.value());
            }
        } else {
            if (minimumRpm > maximumRpm) {
                throw new IllegalArgumentException("a minimum rotation speed cannot exceed the ceiling, got " + minimumRpm + " over " + maximumRpm + " on " + machine.value());
            }
            if (defaultRpm < Math.max(1, minimumRpm) || defaultRpm > maximumRpm) {
                throw new IllegalArgumentException("a default rotation speed must be usable, got " + defaultRpm + " outside [" + Math.max(
                        1,
                        minimumRpm) + ", " + maximumRpm + "] on " + machine.value());
            }
        }
    }

    public static MachineProfile nonKinetic(MachineId machine, Map<String, Long> formulaScalars) {
        return new MachineProfile(machine, 0, 0, 0, 0, formulaScalars);
    }

    public boolean isKinetic() {
        return maximumRpm > 0;
    }

    public long scalar(String id, long fallback) {
        return formulaScalars.getOrDefault(id, fallback);
    }
}