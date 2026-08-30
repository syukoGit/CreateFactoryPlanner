package fr.syuko.createfactoryplanner.core.machine;

import java.util.HashMap;
import java.util.Map;

public record MachineSettings(Map<String, Integer> values) {

    public static final MachineSettings NONE = new MachineSettings(Map.of());

    public MachineSettings {
        values = Map.copyOf(values);
    }

    public static MachineSettings of(String id, int value) {
        return new MachineSettings(Map.of(id, value));
    }

    public int value(String id, int fallback) {
        return values.getOrDefault(id, fallback);
    }

    public MachineSettings with(String id, int value) {
        Map<String, Integer> merged = new HashMap<>(values);
        merged.put(id, value);
        return new MachineSettings(merged);
    }
}
