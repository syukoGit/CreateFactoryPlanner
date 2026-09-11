package fr.syuko.createfactoryplanner.data.machine;

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CKinetics;

import fr.syuko.createfactoryplanner.core.io.MachineEntry;
import fr.syuko.createfactoryplanner.core.io.Provenance;
import fr.syuko.createfactoryplanner.core.model.MachineId;
import fr.syuko.createfactoryplanner.data.constants.OverrideLoader;
import fr.syuko.createfactoryplanner.data.constants.Overrides;

import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.DoubleSupplier;

public final class ConstantStore {

    public static final int DEFAULT_RPM = 128;

    public static final String FAN_PROCESSING_TIME = "fan_processing_time";

    private ConstantStore() {
    }

    public static List<MachineEntry> readMachines() {
        CKinetics kinetics = AllConfigs.server().kinetics;
        Overrides overrides = OverrideLoader.current();
        int maximumRpm = kinetics.maxRotationSpeed.get();
        List<MachineEntry> entries = new ArrayList<>();
        for (MachineId machine : Machines.ordered()) {
            entries.add(readMachine(machine, kinetics, maximumRpm, overrides));
        }
        return List.copyOf(entries);
    }

    private static MachineEntry readMachine(MachineId machine,
                                            CKinetics kinetics,
                                            int maximumRpm,
                                            Overrides overrides) {
        Map<String, Provenance> provenance = new LinkedHashMap<>();
        provenance.put(MachineEntry.MINIMUM_RPM, Provenance.GAME);
        provenance.put(MachineEntry.MAXIMUM_RPM, Provenance.GAME);
        int minimumRpm = minimumRpmOf(machine, kinetics);
        Double impact = stressImpactOf(machine).orElse(null);
        if (impact != null) {
            provenance.put(MachineEntry.STRESS_IMPACT_PER_RPM, Provenance.GAME);
        }
        return new MachineEntry(machine.value(),
                                minimumRpm,
                                maximumRpm,
                                defaultRpmOf(machine, minimumRpm, maximumRpm, overrides, provenance),
                                impact,
                                formulaScalarsOf(machine, kinetics, overrides, provenance),
                                Map.copyOf(provenance));
    }

    private static int defaultRpmOf(MachineId machine,
                                    int minimumRpm,
                                    int maximumRpm,
                                    Overrides overrides,
                                    Map<String, Provenance> provenance) {
        OptionalInt overridden = overrides.defaultRpmOf(machine.value());
        provenance.put(MachineEntry.DEFAULT_RPM,
                       overridden.isPresent()
                       ? Provenance.USER
                       : Provenance.DEFAULT);
        return Math.clamp(overridden.orElse(DEFAULT_RPM), Math.max(1, minimumRpm), maximumRpm);
    }

    private static int minimumRpmOf(MachineId machine, CKinetics kinetics) {
        return Machines.MECHANICAL_MIXER.equals(machine)
               ? (int) Math.round(kinetics.mediumSpeed.get())
               : 0;
    }

    private static Optional<Double> stressImpactOf(MachineId machine) {
        return MachineBlocks.of(machine).flatMap(ConstantStore::registeredImpact);
    }

    private static Optional<Double> registeredImpact(Block block) {
        DoubleSupplier impact = BlockStressValues.IMPACTS.get(block);
        return impact == null
               ? Optional.empty()
               : Optional.of(impact.getAsDouble());
    }

    private static Map<String, Long> formulaScalarsOf(MachineId machine,
                                                      CKinetics kinetics,
                                                      Overrides overrides,
                                                      Map<String, Provenance> provenance) {
        Map<String, Long> scalars = new LinkedHashMap<>();
        if (Machines.ENCASED_FAN.equals(machine)) {
            scalars.put(FAN_PROCESSING_TIME, kinetics.fanProcessingTime.get().longValue());
            provenance.put(FAN_PROCESSING_TIME, Provenance.GAME);
        }
        overrides.formulaScalarsOf(machine.value()).forEach((scalar, value) -> {
            scalars.put(scalar, value);
            provenance.put(scalar, Provenance.USER);
        });
        return Map.copyOf(scalars);
    }
}
