package fr.syuko.createfactoryplanner.data.constants;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

public record Overrides(Integer defaultRpm, Map<String, MachineOverride> machines) {

    public static final Overrides NONE = new Overrides(null, Map.of());

    public Overrides {
        machines = machines == null
                   ? Map.of()
                   : Map.copyOf(machines);
    }

    public OptionalInt defaultRpmOf(String machine) {
        Integer perMachine = Optional.ofNullable(machines.get(machine)).map(MachineOverride::defaultRpm).orElse(null);
        if (perMachine != null) {
            return OptionalInt.of(perMachine);
        }
        return defaultRpm == null
               ? OptionalInt.empty()
               : OptionalInt.of(defaultRpm);
    }

    public Map<String, Long> formulaScalarsOf(String machine) {
        return Optional.ofNullable(machines.get(machine)).map(MachineOverride::formulaScalars).orElse(Map.of());
    }

    public boolean isEmpty() {
        return defaultRpm == null && machines.isEmpty();
    }
}
