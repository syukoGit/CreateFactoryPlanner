package fr.syuko.createfactoryplanner.core.machine;

import fr.syuko.createfactoryplanner.core.model.MachineId;

import java.util.Optional;

public interface MachineCatalog {

    Optional<MachineProfile> profile(MachineId machine);

    Optional<ThroughputModel> model(MachineId machine);
}
