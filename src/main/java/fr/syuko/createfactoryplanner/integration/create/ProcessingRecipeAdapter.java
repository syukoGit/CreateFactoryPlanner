package fr.syuko.createfactoryplanner.integration.create;

import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import fr.syuko.createfactoryplanner.core.model.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.*;

public final class ProcessingRecipeAdapter {
    private static final String MANUAL_ONLY_SUFFIX = "_manual_only";

    private static final int HELD_ITEM_INDEX = 1;

    private static final NamespacedId UNKNOWN_TYPE = new NamespacedId("createfactoryplanner", "unregistered_type");

    private static final List<String> PREFERRED_NAMESPACES = List.of("minecraft", "create");

    private static final Comparator<NamespacedId> REPRESENTATIVE_ORDER = Comparator.comparingInt(ProcessingRecipeAdapter::namespaceRank)
                                                                                   .thenComparing(Comparator.naturalOrder());

    private ProcessingRecipeAdapter() {
    }

    public static RecipeHarvest harvest(Collection<RecipeHolder<?>> holders) {
        List<RecipeNode> nodes = new ArrayList<>();
        Map<NamespacedId, Integer> supported = new TreeMap<>();
        Map<NamespacedId, Integer> unsupported = new TreeMap<>();
        List<NamespacedId> manualOnly = new ArrayList<>();
        List<HarvestProblem> problems = new ArrayList<>();

        for (RecipeHolder<?> holder : holders) {
            NamespacedId recipeId = toId(holder.id());
            NamespacedId typeId = recipeTypeId(holder);

            if (!(holder.value() instanceof ProcessingRecipe<?, ?> recipe)) {
                unsupported.merge(typeId, 1, Integer::sum);
                continue;
            }
            if (holder.id().getPath().endsWith(MANUAL_ONLY_SUFFIX)) {
                manualOnly.add(recipeId);
                continue;
            }
            try {
                nodes.add(toNode(recipeId, typeId, recipe));
                supported.merge(typeId, 1, Integer::sum);
            } catch (RuntimeException e) {
                problems.add(new HarvestProblem(recipeId, typeId, describe(e)));
            }
        }

        nodes.sort(Comparator.comparing(RecipeNode::recipeId));
        manualOnly.sort(Comparator.naturalOrder());
        problems.sort(Comparator.comparing(HarvestProblem::recipeId));

        return new RecipeHarvest(holders.size(), nodes, supported, unsupported, manualOnly, problems);
    }

    private static RecipeNode toNode(NamespacedId recipeId, NamespacedId typeId, ProcessingRecipe<?, ?> recipe) {
        List<InputStack> inputs = new ArrayList<>(itemInputs(recipe));
        inputs.addAll(fluidInputs(recipe));

        List<OutputStack> outputs = new ArrayList<>(itemOutputs(recipe));
        outputs.addAll(fluidOutputs(recipe));

        return new RecipeNode(recipeId,
                              typeId,
                              mergeEquivalent(inputs),
                              outputs,
                              recipe.getProcessingDuration(),
                              heatOf(recipe));
    }

    private static List<InputStack> itemInputs(ProcessingRecipe<?, ?> recipe) {
        boolean keepsHeldItem = recipe instanceof ItemApplicationRecipe application && application.shouldKeepHeldItem();
        List<Ingredient> ingredients = recipe.getIngredients();
        List<InputStack> inputs = new ArrayList<>();

        for (int index = 0; index < ingredients.size(); index++) {
            List<ItemKey> accepted = acceptedItems(ingredients.get(index));
            if (accepted.isEmpty()) {
                continue;
            }
            boolean consumed = !(keepsHeldItem && index == HELD_ITEM_INDEX);
            inputs.add(new InputStack(accepted, accepted.getFirst(), 1, consumed));
        }
        return inputs;
    }

    private static List<InputStack> fluidInputs(ProcessingRecipe<?, ?> recipe) {
        List<InputStack> inputs = new ArrayList<>();
        for (SizedFluidIngredient ingredient : recipe.getFluidIngredients()) {
            List<ItemKey> accepted = acceptedFluids(ingredient);
            if (accepted.isEmpty()) {
                continue;
            }
            inputs.add(new InputStack(accepted, accepted.getFirst(), Math.max(1, ingredient.amount()), true));
        }
        return inputs;
    }

    private static List<OutputStack> itemOutputs(ProcessingRecipe<?, ?> recipe) {
        List<OutputStack> outputs = new ArrayList<>();
        for (ProcessingOutput output : recipe.getRollableResults()) {
            ItemStack stack = output.getStack();
            ResourceLocation key = stack.isEmpty()
                                   ? null
                                   : BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (key == null || stack.getCount() <= 0) {
                continue;
            }
            outputs.add(new OutputStack(ItemKey.item(toId(key)), stack.getCount(), output.getChance()));
        }
        return outputs;
    }

    private static List<OutputStack> fluidOutputs(ProcessingRecipe<?, ?> recipe) {
        List<OutputStack> outputs = new ArrayList<>();
        for (FluidStack stack : recipe.getFluidResults()) {
            ResourceLocation key = stack.isEmpty()
                                   ? null
                                   : BuiltInRegistries.FLUID.getKey(stack.getFluid());
            if (key == null || stack.getAmount() <= 0) {
                continue;
            }
            outputs.add(OutputStack.guaranteed(ItemKey.fluid(toId(key)), stack.getAmount()));
        }
        return outputs;
    }

    private static List<ItemKey> acceptedItems(Ingredient ingredient) {
        return Arrays.stream(ingredient.getItems())
                     .filter(stack -> !stack.isEmpty())
                     .map(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()))
                     .map(ProcessingRecipeAdapter::toId)
                     .distinct()
                     .sorted(REPRESENTATIVE_ORDER)
                     .map(ItemKey::item)
                     .toList();
    }

    private static List<ItemKey> acceptedFluids(SizedFluidIngredient ingredient) {
        return Arrays.stream(ingredient.getFluids())
                     .filter(stack -> !stack.isEmpty())
                     .map(stack -> BuiltInRegistries.FLUID.getKey(stack.getFluid()))
                     .map(ProcessingRecipeAdapter::toId)
                     .distinct()
                     .sorted(REPRESENTATIVE_ORDER)
                     .map(ItemKey::fluid)
                     .toList();
    }

    private static List<InputStack> mergeEquivalent(List<InputStack> inputs) {
        List<InputStack> merged = new ArrayList<>();
        for (InputStack input : inputs) {
            int existing = indexOfEquivalent(merged, input);
            if (existing < 0) {
                merged.add(input);
            } else {
                InputStack current = merged.get(existing);
                merged.set(existing, current.withAmount(current.amount() + input.amount()));
            }
        }
        return merged;
    }

    private static int indexOfEquivalent(List<InputStack> inputs, InputStack candidate) {
        for (int index = 0; index < inputs.size(); index++) {
            InputStack existing = inputs.get(index);
            if (existing.consumed() == candidate.consumed() && existing.accepted().equals(candidate.accepted())) {
                return index;
            }
        }
        return -1;
    }

    private static HeatRequirement heatOf(ProcessingRecipe<?, ?> recipe) {
        return switch (recipe.getRequiredHeat()) {
            case NONE -> HeatRequirement.NONE;
            case HEATED -> HeatRequirement.HEATED;
            case SUPERHEATED -> HeatRequirement.SUPERHEATED;
        };
    }

    private static NamespacedId recipeTypeId(RecipeHolder<?> holder) {
        ResourceLocation key = BuiltInRegistries.RECIPE_TYPE.getKey(holder.value().getType());
        return key == null
               ? UNKNOWN_TYPE
               : toId(key);
    }

    private static int namespaceRank(NamespacedId id) {
        int rank = PREFERRED_NAMESPACES.indexOf(id.namespace());
        return rank < 0
               ? PREFERRED_NAMESPACES.size()
               : rank;
    }

    private static String describe(RuntimeException e) {
        String message = e.getMessage();
        return message == null || message.isBlank()
               ? e.getClass().getSimpleName()
               : message;
    }

    private static NamespacedId toId(ResourceLocation location) {
        return new NamespacedId(location.getNamespace(), location.getPath());
    }
}