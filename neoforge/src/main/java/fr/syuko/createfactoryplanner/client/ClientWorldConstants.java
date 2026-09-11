package fr.syuko.createfactoryplanner.client;

import fr.syuko.createfactoryplanner.CreateFactoryPlanner;
import fr.syuko.createfactoryplanner.data.constants.OverrideLoader;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = CreateFactoryPlanner.MODID, value = Dist.CLIENT)
public final class ClientWorldConstants {

    private ClientWorldConstants() {
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        OverrideLoader.invalidate();
    }
}
