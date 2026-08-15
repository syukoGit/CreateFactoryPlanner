package fr.syuko.createfactoryplanner.core.model;

import java.util.List;
import java.util.Objects;

public record RecipeNode(NamespacedId recipeId, NamespacedId recipeType, List<InputStack> inputs,
                         List<OutputStack> outputs, int durationTicks, HeatRequirement heat) {
    public RecipeNode {
        Objects.requireNonNull(recipeId, "recipeId");
        Objects.requireNonNull(recipeType, "recipeType");
        Objects.requireNonNull(heat, "heat");
        inputs = List.copyOf(Objects.requireNonNull(inputs, "inputs"));
        outputs = List.copyOf(Objects.requireNonNull(outputs, "outputs"));
        if (durationTicks < 0) {
            throw new IllegalArgumentException("durationTicks must not be negative but was " + durationTicks);
        }
    }

    public List<InputStack> consumedInputs() {
        return inputs.stream().filter(InputStack::consumed).toList();
    }

    public List<InputStack> catalysts() {
        return inputs.stream().filter(input -> !input.consumed()).toList();
    }

    public double expectedOutputOf(ItemKey item) {
        return outputs.stream()
                      .filter(output -> output.item().equals(item))
                      .mapToDouble(OutputStack::expectedPerOperation)
                      .sum();
    }
}