package fr.syuko.createfactoryplanner.data.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Arrays;
import java.util.List;

public final class RawRecipes {

    private RawRecipes() {
    }

    public static String recipeTypeOf(Recipe<?> recipe) {
        ResourceLocation key = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType());
        return key == null
               ? "unregistered"
               : key.toString();
    }

    public static String itemIdOf(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    public static String fluidIdOf(FluidStack stack) {
        return BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString();
    }

    public static RawIngredient of(Ingredient ingredient) {
        List<ItemStack> stacks = Arrays.asList(ingredient.getItems());
        List<String> items = stacks.stream().map(RawRecipes::itemIdOf).distinct().sorted().toList();
        boolean allDamageable = !stacks.isEmpty() && stacks.stream().allMatch(ItemStack::isDamageableItem);
        boolean allWithRemainder = !stacks.isEmpty() && stacks.stream()
                                                              .noneMatch(stack -> stack.getCraftingRemainingItem()
                                                                                       .isEmpty());
        return new RawIngredient(tagsOf(ingredient), items, ingredient.isCustom(), allDamageable, allWithRemainder);
    }

    public static RawFluidIngredient of(SizedFluidIngredient ingredient) {
        List<String> fluids = Arrays.stream(ingredient.getFluids())
                                    .map(RawRecipes::fluidIdOf)
                                    .distinct()
                                    .sorted()
                                    .toList();
        return new RawFluidIngredient(fluids, ingredient.amount());
    }

    public static RawFluidResult of(FluidStack stack) {
        return new RawFluidResult(fluidIdOf(stack), stack.getAmount());
    }

    private static List<String> tagsOf(Ingredient ingredient) {
        if (ingredient.isCustom()) {
            return List.of();
        }
        return Arrays.stream(ingredient.getValues())
                     .filter(Ingredient.TagValue.class::isInstance)
                     .map(Ingredient.TagValue.class::cast)
                     .map(value -> value.tag().location().toString())
                     .sorted()
                     .toList();
    }
}