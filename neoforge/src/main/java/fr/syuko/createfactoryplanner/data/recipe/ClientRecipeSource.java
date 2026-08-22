package fr.syuko.createfactoryplanner.data.recipe;

import com.simibubi.create.AllRecipeTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.*;

public final class ClientRecipeSource implements RecipeSource {

    private static final List<RawRecipeReader> READERS = List.of(new ProcessingRecipeReader(),
                                                                 new CraftingRecipeReader(),
                                                                 new SingleStepRecipeReader(),
                                                                 new SequencedAssemblyRecipeReader());

    private final HarvestContext context;

    public ClientRecipeSource(HarvestContext context) {
        this.context = context;
    }

    public static ClientRecipeSource ofCurrentConnection() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener connection = minecraft.getConnection();
        ClientLevel level = minecraft.level;
        if (connection == null || level == null) {
            throw new IllegalStateException("recipes are only available once a world is joined");
        }
        return new ClientRecipeSource(new HarvestContext(connection.getRecipeManager(), level));
    }

    @Override
    public List<RecipeTypeEntry> knownTypes() {
        Set<String> bound = RecipeBindings.boundRecipeTypes();
        List<RecipeTypeEntry> entries = new ArrayList<>();
        for (AllRecipeTypes type : AllRecipeTypes.values()) {
            List<RecipeHolder<?>> holders = holdersOf(type.getType());
            entries.add(new RecipeTypeEntry(type.getId().toString(),
                                            registeredTypeIdOf(type),
                                            ownsItsType(type),
                                            bound.contains(registeredTypeIdOf(type)),
                                            holders.size(),
                                            (int) holders.stream().filter(ClientRecipeSource::isAutomatable).count()));
        }
        entries.sort(Comparator.comparing(RecipeTypeEntry::id));
        return List.copyOf(entries);
    }

    private static boolean isAutomatable(RecipeHolder<?> holder) {
        return !AllRecipeTypes.shouldIgnoreInAutomation(holder);
    }

    private static boolean ownsItsType(AllRecipeTypes type) {
        return type.getId().toString().equals(registeredTypeIdOf(type));
    }

    private static String registeredTypeIdOf(AllRecipeTypes type) {
        ResourceLocation key = BuiltInRegistries.RECIPE_TYPE.getKey(type.getType());
        return key == null
               ? "unregistered"
               : key.toString();
    }

    @Override
    public List<HarvestedRecipe> allRecipes() {
        Map<String, RawRecipe> bodies = new LinkedHashMap<>();
        Map<String, TreeSet<String>> rules = new LinkedHashMap<>();
        Map<String, TreeSet<String>> machines = new LinkedHashMap<>();

        for (HarvestRule rule : RecipeBindings.rules()) {
            if (!rule.enabled()) {
                continue;
            }
            for (RecipeHolder<?> holder : rule.collector().collect(context)) {
                if (!isAutomatable(holder)) {
                    continue;
                }
                String id = holder.id().toString();
                bodies.computeIfAbsent(id, key -> read(holder));
                rules.computeIfAbsent(id, key -> new TreeSet<>()).add(rule.label());
                machines.computeIfAbsent(id, key -> new TreeSet<>()).add(rule.machine().value());
            }
        }

        return bodies.keySet()
                     .stream()
                     .sorted()
                     .map(id -> new HarvestedRecipe(List.copyOf(rules.get(id)),
                                                    List.copyOf(machines.get(id)),
                                                    bodies.get(id)))
                     .toList();
    }

    private RawRecipe read(RecipeHolder<?> holder) {
        Recipe<?> recipe = holder.value();
        for (RawRecipeReader reader : READERS) {
            if (reader.handles(recipe)) {
                return reader.read(holder, context.registries());
            }
        }
        return new RawRecipe(holder.id().toString(),
                             RawRecipes.recipeTypeOf(recipe),
                             "unsupported",
                             List.of(),
                             List.of(),
                             List.of(),
                             List.of(),
                             0,
                             "NONE",
                             List.of("no reader handles " + recipe.getClass().getName()));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List<RecipeHolder<?>> holdersOf(RecipeType<?> type) {
        return List.copyOf(context.recipes().getAllRecipesFor((RecipeType) type));
    }
}