package fr.syuko.createfactoryplanner;

import com.mojang.logging.LogUtils;

import fr.syuko.createfactoryplanner.data.constants.OverrideLoader;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;

import org.slf4j.Logger;

@Mod(CreateFactoryPlanner.MODID)
public class CreateFactoryPlanner {
    public static final String MODID = "createfactoryplanner";

    private static final Logger LOGGER = LogUtils.getLogger();

    public CreateFactoryPlanner() {
        OverrideLoader.readFrom(FMLPaths.CONFIGDIR.get().resolve(MODID));
        LOGGER.debug("{} loaded", MODID);
    }
}
