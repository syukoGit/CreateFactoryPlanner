package fr.syuko.createfactoryplanner.core.recipe;

import fr.syuko.createfactoryplanner.core.model.MachineId;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record SequenceProfile(List<SequenceStep> steps) {

    public SequenceProfile {
        steps = List.copyOf(steps);
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("a sequence profile must carry at least one step");
        }
        Set<MachineId> seen = new HashSet<>();
        for (SequenceStep step : steps) {
            if (!seen.add(step.machine())) {
                throw new IllegalArgumentException("a sequenced assembly installs one machine of each type, got " + step.machine()
                                                                                                                        .value() + " twice");
            }
        }
    }

    public int totalPasses() {
        return steps.stream().mapToInt(SequenceStep::passes).sum();
    }
}