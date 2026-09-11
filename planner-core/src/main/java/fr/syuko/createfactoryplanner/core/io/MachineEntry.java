package fr.syuko.createfactoryplanner.core.io;

import fr.syuko.createfactoryplanner.core.machine.MachineProfile;
import fr.syuko.createfactoryplanner.core.model.MachineId;

import java.util.Map;
import java.util.Optional;

public record MachineEntry(String id, int minimumRpm, int maximumRpm, int defaultRpm, Double stressImpactPerRpm,
                           Map<String, Long> formulaScalars, Map<String, Provenance> provenance) {

    public static final String MINIMUM_RPM = "minimumRpm";

    public static final String MAXIMUM_RPM = "maximumRpm";

    public static final String DEFAULT_RPM = "defaultRpm";

    public static final String STRESS_IMPACT_PER_RPM = "stressImpactPerRpm";

    public MachineEntry {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("a machine entry cannot be blank");
        }
        formulaScalars = formulaScalars == null
                         ? Map.of()
                         : Map.copyOf(formulaScalars);
        provenance = provenance == null
                     ? Map.of()
                     : Map.copyOf(provenance);
    }

    public MachineId machine() {
        return new MachineId(id);
    }

    public boolean readsItsStressImpact() {
        return stressImpactPerRpm != null;
    }

    public Provenance provenanceOf(String field) {
        return provenance.getOrDefault(field, Provenance.GAME);
    }

    public boolean carriesAUserOverride() {
        return provenance.containsValue(Provenance.USER);
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
