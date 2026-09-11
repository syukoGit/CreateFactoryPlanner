package fr.syuko.createfactoryplanner.core.io;

import fr.syuko.createfactoryplanner.core.machine.MachineProfile;
import fr.syuko.createfactoryplanner.core.model.MachineId;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record Catalog(CatalogMeta meta, List<MachineEntry> machines) {

    public Catalog {
        if (meta == null) {
            throw new IllegalArgumentException("a catalog must carry its meta");
        }
        if (machines == null) {
            throw new IllegalArgumentException("a catalog must carry a machine list, even an empty one");
        }
        machines = List.copyOf(machines);
    }

    public Optional<MachineEntry> machine(MachineId id) {
        return machines.stream().filter(entry -> entry.machine().equals(id)).findFirst();
    }

    public Map<MachineId, MachineProfile> profiles() {
        Map<MachineId, MachineProfile> profiles = new LinkedHashMap<>();
        for (MachineEntry entry : machines) {
            entry.toProfile().ifPresent(profile -> profiles.put(entry.machine(), profile));
        }
        return Map.copyOf(profiles);
    }
}
