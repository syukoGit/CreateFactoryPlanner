package fr.syuko.createfactoryplanner.data.constants;

import java.util.Map;

public record MachineOverride(Integer defaultRpm, Map<String, Long> formulaScalars) {

    public MachineOverride {
        formulaScalars = formulaScalars == null
                         ? Map.of()
                         : Map.copyOf(formulaScalars);
    }
}
