package fr.syuko.createfactoryplanner.core.recipe;

import fr.syuko.createfactoryplanner.core.model.MachineId;

public record SequenceStep(MachineId machine, int passes, int declaredDurationTicks) {

    public SequenceStep {
        if (machine == null) {
            throw new IllegalArgumentException("a sequence step must carry a machine");
        }
        if (passes <= 0) {
            throw new IllegalArgumentException("a sequence step must be walked at least once, got " + passes + " on " + machine.value());
        }
        if (declaredDurationTicks < 0) {
            throw new IllegalArgumentException("a declared duration cannot be negative, got " + declaredDurationTicks + " on " + machine.value());
        }
    }

    public boolean declaresItsDuration() {
        return declaredDurationTicks > 0;
    }
}