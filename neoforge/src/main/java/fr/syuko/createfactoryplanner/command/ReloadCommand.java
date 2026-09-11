package fr.syuko.createfactoryplanner.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import fr.syuko.createfactoryplanner.data.constants.OverrideLoader;
import fr.syuko.createfactoryplanner.data.constants.Overrides;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class ReloadCommand {

    private ReloadCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cfp").then(Commands.literal("reload").executes(ReloadCommand::reload)));
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Overrides overrides = OverrideLoader.reload();
        String problem = OverrideLoader.lastProblem();
        if (problem != null) {
            source.sendFailure(Component.translatable("commands.createfactoryplanner.reload.failed",
                                                      OverrideLoader.file().toString(),
                                                      problem));
            return 0;
        }
        if (overrides.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.createfactoryplanner.reload.empty",
                                                            OverrideLoader.file().toString()), false);
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("commands.createfactoryplanner.reload.success",
                                                        overrides.machines().size(),
                                                        OverrideLoader.file().toString()), false);
        return overrides.machines().size();
    }
}