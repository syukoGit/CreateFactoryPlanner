package fr.syuko.createfactoryplanner.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import fr.syuko.createfactoryplanner.data.dump.RecipeDump;
import fr.syuko.createfactoryplanner.data.dump.RecipeDumps;
import fr.syuko.createfactoryplanner.data.recipe.ClientRecipeSource;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RecipeDumpCommand {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final String DUMP_DIRECTORY = "createfactoryplanner/dumps";

    private static final String DUMP_FILE = "recipes.json";

    private RecipeDumpCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cfp")
                                    .then(Commands.literal("dump")
                                                  .then(Commands.literal("recipes")
                                                                .executes(RecipeDumpCommand::dumpRecipes))));
    }

    private static int dumpRecipes(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            RecipeDump dump = RecipeDumps.of(ClientRecipeSource.ofCurrentConnection());
            Path file = write(dump);
            source.sendSuccess(() -> success(dump, file), false);
            return dump.recipes().size();
        } catch (IllegalStateException | IOException failure) {
            source.sendFailure(Component.translatable("commands.createfactoryplanner.dump.recipes.failed",
                                                      failure.getMessage()));
            return 0;
        }
    }

    private static Path write(RecipeDump dump) throws IOException {
        Path directory = Minecraft.getInstance().gameDirectory.toPath().resolve(DUMP_DIRECTORY);
        Files.createDirectories(directory);
        Path file = directory.resolve(DUMP_FILE);
        Files.writeString(file, GSON.toJson(dump), StandardCharsets.UTF_8);
        return file;
    }

    private static Component success(RecipeDump dump, Path file) {
        return Component.translatable("commands.createfactoryplanner.dump.recipes.success",
                                      dump.recipes().size(),
                                      dump.types().size(),
                                      dump.problems().size(),
                                      Component.literal(file.getFileName().toString())
                                               .withStyle(style -> style.withUnderlined(true)
                                                                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE,
                                                                                                       file.toAbsolutePath()
                                                                                                           .toString()))));
    }
}