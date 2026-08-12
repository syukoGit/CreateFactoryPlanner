package fr.syuko.createfactoryplanner.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;
import fr.syuko.createfactoryplanner.CreateFactoryPlanner;
import fr.syuko.createfactoryplanner.integration.recipes.ClientRecipeSource;
import fr.syuko.createfactoryplanner.integration.recipes.RecipeSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@EventBusSubscriber(modid = CreateFactoryPlanner.MODID, value = Dist.CLIENT)
public final class RecipeDumpCommand {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String UNKNOWN = "unknown";

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

        Collection<RecipeHolder<?>> holders = recipes.all();
        Path target = dumpPath();

        try {
            writeDump(target, buildDump(holders, source.registryAccess()));
        } catch (IOException e) {
            source.sendFailure(Component.translatable("commands.createfactoryplanner.dump.recipes.failed",
                                                      String.valueOf(e.getMessage())));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable("commands.createfactoryplanner.dump.recipes.success",
                                                        holders.size(),
                                                        target.toString()), false);
        return holders.size();
    }

    private static JsonObject buildDump(Collection<RecipeHolder<?>> holders, HolderLookup.Provider registries) {
        List<RecipeHolder<?>> sorted = holders.stream()
                                              .sorted(Comparator.comparing(holder -> holder.id().toString()))
                                              .toList();

        JsonArray entries = new JsonArray();
        Map<String, Integer> countsByType = new TreeMap<>();

        for (RecipeHolder<?> holder : sorted) {
            String type = typeId(holder.value());
            countsByType.merge(type, 1, Integer::sum);
            entries.add(describe(holder, type, registries));
        }

        JsonObject counts = new JsonObject();
        countsByType.forEach(counts::addProperty);

        JsonObject root = new JsonObject();
        root.addProperty("recipe_count", sorted.size());
        root.add("count_by_type", counts);
        root.add("recipes", entries);
        return root;
    }

    private static JsonObject describe(RecipeHolder<?> holder, String type, HolderLookup.Provider registries) {
        Recipe<?> recipe = holder.value();

        JsonObject entry = new JsonObject();
        entry.addProperty("id", holder.id().toString());
        entry.addProperty("type", type);
        entry.add("ingredients", ingredients(recipe));

        ItemStack primary = primaryResult(recipe, registries);
        if (!primary.isEmpty()) {
            JsonObject result = new JsonObject();
            result.addProperty("item", itemId(primary));
            result.addProperty("count", primary.getCount());
            entry.add("primary_result", result);
        }

        return entry;
    }

    private static JsonArray ingredients(Recipe<?> recipe) {
        JsonArray all = new JsonArray();
        for (Ingredient ingredient : recipe.getIngredients()) {
            JsonArray matching = new JsonArray();
            for (ItemStack stack : ingredient.getItems()) {
                matching.add(itemId(stack));
            }
            all.add(matching);
        }
        return all;
    }

    private static ItemStack primaryResult(Recipe<?> recipe, HolderLookup.Provider registries) {
        try {
            return recipe.getResultItem(registries);
        } catch (RuntimeException e) {
            return ItemStack.EMPTY;
        }
    }

    private static String typeId(Recipe<?> recipe) {
        ResourceLocation key = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType());
        return key == null
               ? UNKNOWN
               : key.toString();
    }

    private static String itemId(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key == null
               ? UNKNOWN
               : key.toString();
    }

    private static void writeDump(Path target, JsonObject dump) throws IOException {
        Files.createDirectories(target.getParent());
        Files.writeString(target, GSON.toJson(dump));
    }

    private static Path dumpPath() {
        return FMLPaths.CONFIGDIR.get().resolve("cfp").resolve("dumps").resolve("recipes.json");
    }
}
