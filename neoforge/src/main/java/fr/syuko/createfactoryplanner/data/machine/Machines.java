package fr.syuko.createfactoryplanner.data.machine;

import fr.syuko.createfactoryplanner.core.model.MachineId;

import java.util.List;
import java.util.Set;

public final class Machines {

    public static final MachineId MILLSTONE = new MachineId("millstone");

    public static final MachineId CRUSHING_WHEELS = new MachineId("crushing_wheels");

    public static final MachineId MECHANICAL_PRESS = new MachineId("mechanical_press");

    public static final MachineId MECHANICAL_MIXER = new MachineId("mechanical_mixer");

    public static final MachineId MECHANICAL_SAW = new MachineId("mechanical_saw");

    public static final MachineId MECHANICAL_CRAFTER = new MachineId("mechanical_crafter");

    public static final MachineId ENCASED_FAN = new MachineId("encased_fan");

    public static final MachineId DEPLOYER = new MachineId("deployer");

    public static final MachineId SPOUT = new MachineId("spout");

    public static final MachineId ITEM_DRAIN = new MachineId("item_drain");

    private static final Set<MachineId> ALL = Set.copyOf(List.of(MILLSTONE,
                                                                 CRUSHING_WHEELS,
                                                                 MECHANICAL_PRESS,
                                                                 MECHANICAL_MIXER,
                                                                 MECHANICAL_SAW,
                                                                 MECHANICAL_CRAFTER,
                                                                 ENCASED_FAN,
                                                                 DEPLOYER,
                                                                 SPOUT,
                                                                 ITEM_DRAIN));

    private Machines() {
    }

    public static Set<MachineId> all() {
        return ALL;
    }
}