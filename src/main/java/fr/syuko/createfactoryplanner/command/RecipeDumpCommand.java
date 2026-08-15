package fr.syuko.createfactoryplanner.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;
import fr.syuko.createfactoryplanner.CreateFactoryPlanner;
import fr.syuko.createfactoryplanner.core.model.*;
import fr.syuko.createfactoryplanner.integration.create.HarvestProblem;
import fr.syuko.createfactoryplanner.integration.create.ProcessingRecipeAdapter;
import fr.syuko.createfactoryplanner.integration.create.RecipeHarvest;
import fr.syuko.createfactoryplanner.integration.recipes.ClientRecipeSource;
import fr.syuko.createfactoryplanner.integration.recipes.RecipeSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = CreateFactoryPlanner.MODID, value = Dist.CLIENT)
public final class RecipeDumpCommand {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private RecipeDumpCommand() {
    }

    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher()
             .register(Commands.literal("cfp")
                               .then(Commands.literal("dump")
                                             .then(Commands.literal("recipes")
                                                           .executes(RecipeDumpCommand::dumpRecipes))));
    }

    private static int dumpRecipes(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        RecipeSource recipes = new ClientRecipeSource();

        if (!recipes.isAvailable()) {
            source.sendFailure(Component.translatable("commands.createfactoryplanner.dump.recipes.unavailable"));
            return 0;
        }

        RecipeHarvest harvest = ProcessingRecipeAdapter.harvest(recipes.all());
        Path target = dumpPath();

        try {
            writeDump(target, describe(harvest));
        } catch (IOException e) {
            source.sendFailure(Component.translatable("commands.createfactoryplanner.dump.recipes.failed",
                                                      String.valueOf(e.getMessage())));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable("commands.createfactoryplanner.dump.recipes.success",
                                                        harvest.nodes().size(),
                                                        harvest.inspectedRecipes(),
                                                        target.toString()), false);

        if (!harvest.unsupportedTypes().isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.createfactoryplanner.dump.recipes.unsupported",
                                                            harvest.unsupportedTypes().size()), false);
        }
        if (!harvest.problems().isEmpty()) {
            source.sendFailure(Component.translatable("commands.createfactoryplanner.dump.recipes.problems",
                                                      harvest.problems().size()));
        }
        return harvest.nodes().size();
    }

    private static JsonObject describe(RecipeHarvest harvest) {
        JsonObject root = new JsonObject();
        root.addProperty("inspected_recipes", harvest.inspectedRecipes());
        root.addProperty("node_count", harvest.nodes().size());
        root.add("supported_types", counts(harvest.supportedTypes()));
        root.add("unsupported_types", counts(harvest.unsupportedTypes()));
        root.add("manual_only", ids(harvest.manualOnly()));
        root.add("problems", problems(harvest.problems()));
        root.add("recipes", nodes(harvest.nodes()));
        return root;
    }

    private static JsonObject counts(Map<NamespacedId, Integer> countsByType) {
        JsonObject counts = new JsonObject();
        countsByType.forEach((type, count) -> counts.addProperty(type.toString(), count));
        return counts;
    }

    private static JsonArray ids(List<NamespacedId> values) {
        JsonArray array = new JsonArray();
        values.forEach(value -> array.add(value.toString()));
        return array;
    }

    private static JsonArray problems(List<HarvestProblem> values) {
        JsonArray array = new JsonArray();
        for (HarvestProblem problem : values) {
            JsonObject entry = new JsonObject();
            entry.addProperty("id", problem.recipeId().toString());
            entry.addProperty("type", problem.recipeType().toString());
            entry.addProperty("reason", problem.reason());
            array.add(entry);
        }
        return array;
    }

    private static JsonArray nodes(List<RecipeNode> values) {
        JsonArray array = new JsonArray();
        for (RecipeNode node : values) {
            JsonObject entry = new JsonObject();
            entry.addProperty("id", node.recipeId().toString());
            entry.addProperty("type", node.recipeType().toString());
            entry.addProperty("duration_ticks", node.durationTicks());
            entry.addProperty("heat", node.heat().name().toLowerCase(java.util.Locale.ROOT));
            entry.add("inputs", inputs(node.inputs()));
            entry.add("outputs", outputs(node.outputs()));
            array.add(entry);
        }
        return array;
    }

    private static JsonArray inputs(List<InputStack> values) {
        JsonArray array = new JsonArray();
        for (InputStack input : values) {
            JsonObject entry = new JsonObject();
            entry.addProperty("representative", input.representative().id().toString());
            entry.addProperty("amount", input.amount());
            entry.addProperty("consumed", input.consumed());
            entry.addProperty("fluid", input.fluid());
            if (input.accepted().size() > 1) {
                JsonArray accepted = new JsonArray();
                input.accepted().forEach(item -> accepted.add(item.id().toString()));
                entry.add("accepted", accepted);
            }
            array.add(entry);
        }
        return array;
    }

    private static JsonArray outputs(List<OutputStack> values) {
        JsonArray array = new JsonArray();
        for (OutputStack output : values) {
            ItemKey item = output.item();
            JsonObject entry = new JsonObject();
            entry.addProperty("item", item.id().toString());
            entry.addProperty("count", output.count());
            entry.addProperty("chance", output.chance());
            entry.addProperty("expected", output.expectedPerOperation());
            entry.addProperty("fluid", item.fluid());
            array.add(entry);
        }
        return array;
    }

    private static void writeDump(Path target, JsonObject dump) throws IOException {
        Files.createDirectories(target.getParent());
        Files.writeString(target, GSON.toJson(dump));
    }

    private static Path dumpPath() {
        return FMLPaths.CONFIGDIR.get().resolve("cfp").resolve("dumps").resolve("recipes.json");
    }
}