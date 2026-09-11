package fr.syuko.createfactoryplanner.core.recipe;

import fr.syuko.createfactoryplanner.core.math.Rate;
import fr.syuko.createfactoryplanner.core.model.RecipeId;
import fr.syuko.createfactoryplanner.core.model.ResourceId;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RecipeNormalizer {

    private RecipeNormalizer() {
    }

    public static RecipeDto normalize(RecipeId id,
                                      List<IngredientDto> ingredients,
                                      List<OutputDto> outputs,
                                      int declaredDurationTicks) {
        Map<ResourceId, Rate> consumed = mergeIngredients(ingredients);
        Map<ResourceId, OutputDto> produced = mergeOutputs(outputs);
        List<IngredientDto> netIngredients = new ArrayList<>();
        List<OutputDto> netOutputs = new ArrayList<>();

        for (Map.Entry<ResourceId, Rate> entry : consumed.entrySet()) {
            OutputDto opposite = produced.get(entry.getKey());
            if (opposite == null) {
                netIngredients.add(new IngredientDto(entry.getKey(), entry.getValue()));
                continue;
            }
            balance(entry.getKey(), entry.getValue(), opposite, netIngredients, netOutputs);
        }
        for (Map.Entry<ResourceId, OutputDto> entry : produced.entrySet()) {
            if (!consumed.containsKey(entry.getKey())) {
                netOutputs.add(entry.getValue());
            }
        }
        return new RecipeDto(id, netIngredients, netOutputs, declaredDurationTicks);
    }

    private static Map<ResourceId, Rate> mergeIngredients(List<IngredientDto> ingredients) {
        Map<ResourceId, Rate> merged = new LinkedHashMap<>();
        for (IngredientDto ingredient : ingredients) {
            merged.merge(ingredient.resource(), ingredient.amountPerOperation(), Rate::plus);
        }
        return merged;
    }

    private static Map<ResourceId, OutputDto> mergeOutputs(List<OutputDto> outputs) {
        Map<ResourceId, OutputDto> merged = new LinkedHashMap<>();
        for (OutputDto output : outputs) {
            merged.merge(output.resource(),
                         output,
                         (left, right) -> new OutputDto(left.resource(),
                                                        left.guaranteed() + right.guaranteed(),
                                                        left.expectedPerOperation()
                                                            .plus(right.expectedPerOperation())));
        }
        return merged;
    }

    private static void balance(ResourceId resource,
                                Rate consumed,
                                OutputDto produced,
                                List<IngredientDto> netIngredients,
                                List<OutputDto> netOutputs) {
        Rate net = produced.expectedPerOperation().minus(consumed);
        int side = net.compareTo(Rate.ZERO);
        if (side > 0) {
            netOutputs.add(new OutputDto(resource, remainingGuarantee(produced.guaranteed(), consumed), net));
            return;
        }
        if (side < 0) {
            netIngredients.add(new IngredientDto(resource, Rate.ZERO.minus(net)));
            return;
        }
        netIngredients.add(new IngredientDto(resource, consumed));
        netOutputs.add(produced);
    }

    private static long remainingGuarantee(long guaranteed, Rate consumed) {
        Rate remaining = Rate.of(guaranteed).minus(consumed);
        if (remaining.compareTo(Rate.ZERO) <= 0) {
            return 0;
        }
        return Math.floorDiv(remaining.numerator(), remaining.denominator());
    }
}
