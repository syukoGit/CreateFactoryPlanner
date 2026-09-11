package fr.syuko.createfactoryplanner.data.machine;

import com.simibubi.create.AllBlocks;

import fr.syuko.createfactoryplanner.core.model.MachineId;

import net.minecraft.world.level.block.Block;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class MachineBlocks {

    private static final Map<MachineId, Block> BLOCKS = blocks();

    private MachineBlocks() {
    }

    private static Map<MachineId, Block> blocks() {
        Map<MachineId, Block> blocks = new LinkedHashMap<>();
        blocks.put(Machines.MILLSTONE, AllBlocks.MILLSTONE.get());
        blocks.put(Machines.CRUSHING_WHEELS, AllBlocks.CRUSHING_WHEEL.get());
        blocks.put(Machines.MECHANICAL_PRESS, AllBlocks.MECHANICAL_PRESS.get());
        blocks.put(Machines.MECHANICAL_MIXER, AllBlocks.MECHANICAL_MIXER.get());
        blocks.put(Machines.MECHANICAL_SAW, AllBlocks.MECHANICAL_SAW.get());
        blocks.put(Machines.MECHANICAL_CRAFTER, AllBlocks.MECHANICAL_CRAFTER.get());
        blocks.put(Machines.ENCASED_FAN, AllBlocks.ENCASED_FAN.get());
        blocks.put(Machines.DEPLOYER, AllBlocks.DEPLOYER.get());
        blocks.put(Machines.SPOUT, AllBlocks.SPOUT.get());
        blocks.put(Machines.ITEM_DRAIN, AllBlocks.ITEM_DRAIN.get());
        return Map.copyOf(blocks);
    }

    public static Optional<Block> of(MachineId machine) {
        return Optional.ofNullable(BLOCKS.get(machine));
    }
}
