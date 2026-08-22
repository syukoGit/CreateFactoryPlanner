package fr.syuko.createfactoryplanner.data.recipe;

import fr.syuko.createfactoryplanner.core.model.MachineId;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public record HarvestRule(String label, List<String> recipeTypes, MachineId machine, String configFlag,
                          BooleanSupplier gate, Collector collector) {

    private static final BooleanSupplier ALWAYS = () -> true;

    private static final String UNCONDITIONAL = "";

    public HarvestRule {
        recipeTypes = List.copyOf(recipeTypes);
    }

    public static HarvestRule ofType(String label, String recipeType, MachineId machine, Supplier<RecipeType<?>> type) {
        return new HarvestRule(label,
                               List.of(recipeType),
                               machine,
                               UNCONDITIONAL,
                               ALWAYS,
                               context -> holdersOf(context, type.get()));
    }

    public static HarvestRule ofAdaptedType(String label,
                                            String recipeType,
                                            MachineId machine,
                                            Supplier<RecipeType<?>> type,
                                            Function<RecipeHolder<?>, RecipeHolder<?>> adapter) {
        return new HarvestRule(label,
                               List.of(recipeType),
                               machine,
                               UNCONDITIONAL,
                               ALWAYS,
                               context -> holdersOf(context, type.get()).stream().map(adapter).toList());
    }

    public static HarvestRule ofGatedType(String label,
                                          String recipeType,
                                          MachineId machine,
                                          String configFlag,
                                          BooleanSupplier gate,
                                          Supplier<RecipeType<?>> type) {
        return new HarvestRule(label,
                               List.of(recipeType),
                               machine,
                               configFlag,
                               gate,
                               context -> holdersOf(context, type.get()));
    }

    public static HarvestRule ofFilteredType(String label,
                                             String recipeType,
                                             MachineId machine,
                                             String configFlag,
                                             BooleanSupplier gate,
                                             Supplier<RecipeType<?>> type,
                                             Predicate<Recipe<?>> filter) {
        return new HarvestRule(label,
                               List.of(recipeType),
                               machine,
                               configFlag,
                               gate,
                               context -> holdersOf(context, type.get()).stream()
                                                                        .filter(holder -> filter.test(holder.value()))
                                                                        .toList());
    }

    public static HarvestRule ofSynthetic(String label,
                                          MachineId machine,
                                          String configFlag,
                                          BooleanSupplier gate,
                                          Collector collector) {
        return new HarvestRule(label, List.of(), machine, configFlag, gate, collector);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static List<RecipeHolder<?>> holdersOf(HarvestContext context, RecipeType<?> type) {
        return List.copyOf(context.recipes().getAllRecipesFor((RecipeType) type));
    }

    public boolean enabled() {
        return gate.getAsBoolean();
    }

    public boolean isConditional() {
        return !configFlag.isEmpty();
    }

    public interface Collector {

        List<RecipeHolder<?>> collect(HarvestContext context);
    }
}