package fr.syuko.createfactoryplanner.data.catalog;

import fr.syuko.createfactoryplanner.CreateFactoryPlanner;
import fr.syuko.createfactoryplanner.core.io.Catalog;
import fr.syuko.createfactoryplanner.core.io.CatalogMeta;
import fr.syuko.createfactoryplanner.core.io.MachineEntry;
import fr.syuko.createfactoryplanner.data.machine.ConstantStore;

import net.neoforged.fml.ModList;

import java.time.Instant;
import java.util.List;

public final class CatalogCodec {

    private static final String CREATE = "create";

    private CatalogCodec() {
    }

    public static Catalog read(String world) {
        List<MachineEntry> machines = ConstantStore.readMachines();
        return new Catalog(new CatalogMeta(Instant.now().toString(),
                                           world,
                                           versionOf(CreateFactoryPlanner.MODID),
                                           versionOf(CREATE),
                                           machines.size()), machines);
    }

    private static String versionOf(String modId) {
        return ModList.get()
                      .getModContainerById(modId)
                      .map(container -> container.getModInfo().getVersion().toString())
                      .orElse(null);
    }
}
