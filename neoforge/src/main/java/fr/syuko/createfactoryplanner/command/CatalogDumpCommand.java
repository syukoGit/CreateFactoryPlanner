package fr.syuko.createfactoryplanner.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import fr.syuko.createfactoryplanner.core.io.Catalog;
import fr.syuko.createfactoryplanner.core.io.CatalogJson;
import fr.syuko.createfactoryplanner.data.catalog.CatalogCodec;
import fr.syuko.createfactoryplanner.data.machine.MachineRegistry;
import fr.syuko.createfactoryplanner.data.recipe.ClientRecipeSource;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CatalogDumpCommand {

    private static final String DUMP_DIRECTORY = "createfactoryplanner/dumps";

    private static final String DUMP_FILE = "catalog.json";

    private CatalogDumpCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cfp")
                                    .then(Commands.literal("dump")
                                                  .then(Commands.literal("catalog")
                                                                .executes(CatalogDumpCommand::dumpCatalog))));
    }

    private static int dumpCatalog(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            Catalog catalog = CatalogCodec.read(worldName(), ClientRecipeSource.ofCurrentConnection());
            Path file = write(catalog);
            source.sendSuccess(() -> success(catalog, file), false);
            return catalog.machines().size();
        } catch (IllegalStateException | IllegalArgumentException | IOException failure) {
            source.sendFailure(Component.translatable("commands.createfactoryplanner.dump.catalog.failed",
                                                      failure.getMessage()));
            return 0;
        }
    }

    private static String worldName() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null
               ? null
               : level.dimension().location().toString();
    }

    private static Path write(Catalog catalog) throws IOException {
        Path directory = Minecraft.getInstance().gameDirectory.toPath().resolve(DUMP_DIRECTORY);
        Files.createDirectories(directory);
        Path file = directory.resolve(DUMP_FILE);
        Files.writeString(file, CatalogJson.write(catalog), StandardCharsets.UTF_8);
        return file;
    }

    private static Component success(Catalog catalog, Path file) {
        long unread = catalog.machines().stream().filter(entry -> !entry.readsItsStressImpact()).count();
        return Component.translatable("commands.createfactoryplanner.dump.catalog.success",
                                      catalog.machines().size(),
                                      MachineRegistry.of(catalog).profileCount(),
                                      unread,
                                      catalog.recipes().size(),
                                      catalog.recipes().stream().filter(entry -> !entry.isSettled()).count(),
                                      Component.literal(file.getFileName().toString())
                                               .withStyle(style -> style.withUnderlined(true)
                                                                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE,
                                                                                                       file.toAbsolutePath()
                                                                                                           .toString()))));
    }
}
