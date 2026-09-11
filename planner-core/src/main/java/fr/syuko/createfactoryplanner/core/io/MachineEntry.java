package fr.syuko.createfactoryplanner.core.io;

import fr.syuko.createfactoryplanner.core.machine.MachineProfile;
import fr.syuko.createfactoryplanner.core.model.MachineId;

import java.util.Map;
import java.util.Optional;

public record MachineEntry(String id, int minimumRpm, int maximumRpm, int defaultRpm, Double stressImpactPerRpm,
                           Map<String, Long> formulaScalars) {

    public MachineEntry {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("a machine entry cannot be blank");
        }
        formulaScalars = formulaScalars == null
                         ? Map.of()
                         : Map.copyOf(formulaScalars);
    }

    public MachineId machine() {
        return new MachineId(id);
    }

    public boolean readsItsStressImpact() {
        return stressImpactPerRpm != null;
    }

    public Optional<MachineProfile> toProfile() {
        if (!readsItsStressImpact()) {
            return Optional.empty();
        }
        return Optional.of(new MachineProfile(machine(),
                                              minimumRpm,
                                              maximumRpm,
                                              defaultRpm,
                                              stressImpactPerRpm,
                                              formulaScalars));
    }
}
