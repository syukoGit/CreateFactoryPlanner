package fr.syuko.createfactoryplanner.data.machine;

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CKinetics;

import fr.syuko.createfactoryplanner.core.io.MachineEntry;
import fr.syuko.createfactoryplanner.core.model.MachineId;

import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.DoubleSupplier;

public final class ConstantStore {

    public static final int DEFAULT_RPM = 128;

    public static final String FAN_PROCESSING_TIME = "fan_processing_time";

    private ConstantStore() {
    }

    public static List<MachineEntry> readMachines() {
        CKinetics kinetics = AllConfigs.server().kinetics;
        int maximumRpm = kinetics.maxRotationSpeed.get();
        List<MachineEntry> entries = new ArrayList<>();
        for (MachineId machine : Machines.ordered()) {
            entries.add(readMachine(machine, kinetics, maximumRpm));
        }
        return List.copyOf(entries);
    }

    private static MachineEntry readMachine(MachineId machine, CKinetics kinetics, int maximumRpm) {
        int minimumRpm = minimumRpmOf(machine, kinetics);
        return new MachineEntry(machine.value(),
                                minimumRpm,
                                maximumRpm,
                                Math.clamp(DEFAULT_RPM, Math.max(1, minimumRpm), maximumRpm),
                                stressImpactOf(machine).orElse(null),
                                formulaScalarsOf(machine, kinetics));
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

    private static Map<String, Long> formulaScalarsOf(MachineId machine, CKinetics kinetics) {
        return Machines.ENCASED_FAN.equals(machine)
               ? Map.of(FAN_PROCESSING_TIME, kinetics.fanProcessingTime.get().longValue())
               : Map.of();
    }
}
