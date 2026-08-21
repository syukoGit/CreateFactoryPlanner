package fr.syuko.createfactoryplanner.data.recipe;

import com.simibubi.create.AllRecipeTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ClientRecipeSource implements RecipeSource {

    private static final List<RawRecipeReader> READERS = List.of(new ProcessingRecipeReader(),
                                                                 new CraftingRecipeReader(),
                                                                 new SingleStepRecipeReader());

    private final RecipeManager recipes;

    private final HolderLookup.Provider registries;

    public ClientRecipeSource(RecipeManager recipes, HolderLookup.Provider registries) {
        this.recipes = recipes;
        this.registries = registries;
    }

    public static ClientRecipeSource ofCurrentConnection() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener connection = minecraft.getConnection();
        ClientLevel level = minecraft.level;
        if (connection == null || level == null) {
            throw new IllegalStateException("recipes are only available once a world is joined");
        }
        return new ClientRecipeSource(connection.getRecipeManager(), level.registryAccess());
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
    public List<RecipeTypeEntry> knownTypes() {
        List<RecipeTypeEntry> entries = new ArrayList<>();
        for (AllRecipeTypes type : AllRecipeTypes.values()) {
            List<RecipeHolder<?>> holders = holdersOf(type.getType());
            entries.add(new RecipeTypeEntry(type.getId().toString(),
                                            registeredTypeIdOf(type),
                                            ownsItsType(type),
                                            holders.size(),
                                            (int) holders.stream().filter(ClientRecipeSource::isAutomatable).count()));
        }
        entries.sort(Comparator.comparing(RecipeTypeEntry::id));
        return List.copyOf(entries);
    }

    @Override
    public List<RawRecipe> allRecipes() {
        List<RawRecipe> raw = new ArrayList<>();
        for (AllRecipeTypes type : AllRecipeTypes.values()) {
            if (!ownsItsType(type)) {
                continue;
            }
            for (RecipeHolder<?> holder : holdersOf(type.getType())) {
                if (isAutomatable(holder)) {
                    raw.add(read(holder));
                }
            }
        }
        raw.sort(Comparator.comparing(RawRecipe::id));
        return List.copyOf(raw);
    }

    private RawRecipe read(RecipeHolder<?> holder) {
        Recipe<?> recipe = holder.value();
        for (RawRecipeReader reader : READERS) {
            if (reader.handles(recipe)) {
                return reader.read(holder, registries);
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
        return List.copyOf(recipes.getAllRecipesFor((RecipeType) type));
    }
}