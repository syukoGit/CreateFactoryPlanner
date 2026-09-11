package fr.syuko.createfactoryplanner.client;

import fr.syuko.createfactoryplanner.CreateFactoryPlanner;
import fr.syuko.createfactoryplanner.command.CatalogDumpCommand;
import fr.syuko.createfactoryplanner.command.RecipeDumpCommand;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(modid = CreateFactoryPlanner.MODID, value = Dist.CLIENT)
public final class ClientCommands {

    private ClientCommands() {
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        RecipeDumpCommand.register(event.getDispatcher());
        CatalogDumpCommand.register(event.getDispatcher());
    }
}
