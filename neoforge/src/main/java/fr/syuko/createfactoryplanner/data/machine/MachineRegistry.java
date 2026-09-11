package fr.syuko.createfactoryplanner.data.machine;

import fr.syuko.createfactoryplanner.core.io.Catalog;
import fr.syuko.createfactoryplanner.core.machine.MachineCatalog;
import fr.syuko.createfactoryplanner.core.machine.MachineProfile;
import fr.syuko.createfactoryplanner.core.machine.ThroughputModel;
import fr.syuko.createfactoryplanner.core.model.MachineId;

import java.util.Map;
import java.util.Optional;

public final class MachineRegistry implements MachineCatalog {

    private final Map<MachineId, MachineProfile> profiles;

    private MachineRegistry(Map<MachineId, MachineProfile> profiles) {
        this.profiles = Map.copyOf(profiles);
    }

    public static MachineRegistry of(Catalog catalog) {
        return new MachineRegistry(catalog.profiles());
    }

    public int profileCount() {
        return profiles.size();
    }

    @Override
    public Optional<MachineProfile> profile(MachineId machine) {
        return Optional.ofNullable(profiles.get(machine));
    }

    @Override
    public Optional<ThroughputModel> model(MachineId machine) {
        return Optional.empty();
    }
}
