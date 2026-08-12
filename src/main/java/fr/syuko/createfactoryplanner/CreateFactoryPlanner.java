package fr.syuko.createfactoryplanner;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CreateFactoryPlanner.MODID)
public class CreateFactoryPlanner {
    public static final String MODID = "createfactoryplanner";

    private static final Logger LOGGER = LogUtils.getLogger();

    public CreateFactoryPlanner() {
        LOGGER.debug("{} loaded", MODID);
    }
}
