package fr.syuko.createfactoryplanner.data.recipe;

import fr.syuko.createfactoryplanner.core.io.IngredientEntry;
import fr.syuko.createfactoryplanner.core.io.OutputEntry;
import fr.syuko.createfactoryplanner.core.io.RecipeEntry;
import fr.syuko.createfactoryplanner.core.math.Rate;
import fr.syuko.createfactoryplanner.core.model.RecipeId;
import fr.syuko.createfactoryplanner.core.model.ResourceId;
import fr.syuko.createfactoryplanner.core.recipe.IngredientDto;
import fr.syuko.createfactoryplanner.core.recipe.OutputDto;
import fr.syuko.createfactoryplanner.core.recipe.RecipeDto;
import fr.syuko.createfactoryplanner.core.recipe.RecipeNormalizer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RawRecipeTranslator {

    private static final String SEQUENCED_ASSEMBLY_READER = "sequenced_assembly";

    private static final long CHANCE_SCALE = 10_000L;

    public static final String PENDING_CATALYST_RULE = "pending_catalyst_rule";

    public static final String PENDING_SEQUENCE_RULE = "pending_sequence_rule";

    public static final String UNRESOLVED_INGREDIENT = "unresolved_ingredient";

    private RawRecipeTranslator() {
    }

    public static RecipeEntry translate(HarvestedRecipe harvested) {
        RawRecipe raw = harvested.recipe();
        List<String> pending = new ArrayList<>();
        Map<ResourceId, List<String>> equivalents = new LinkedHashMap<>();
        List<IngredientDto> ingredients = ingredientsOf(raw, equivalents, pending);
        RecipeDto normalized = RecipeNormalizer.normalize(new RecipeId(raw.id()),
                                                          ingredients,
                                                          outputsOf(raw),
                                                          Math.max(0, raw.declaredDuration()));
        if (carriesDurability(raw)) {
            pending.add(PENDING_CATALYST_RULE);
        }
        if (SEQUENCED_ASSEMBLY_READER.equals(raw.reader())) {
            pending.add(PENDING_SEQUENCE_RULE);
        }
        return new RecipeEntry(normalized.id().value(),
                               harvested.machines(),
                               normalized.declaredDurationTicks(),
                               normalized.ingredients()
                                         .stream()
                                         .map(ingredient -> IngredientEntry.of(ingredient,
                                                                               equivalents.getOrDefault(ingredient.resource(),
                                                                                                        List.of())))
                                         .toList(),
                               normalized.outputs().stream().map(OutputEntry::of).toList(),
                               List.copyOf(pending));
    }

    private static List<IngredientDto> ingredientsOf(RawRecipe raw,
                                                     Map<ResourceId, List<String>> equivalents,
                                                     List<String> pending) {
        List<IngredientDto> ingredients = new ArrayList<>();
        for (RawIngredient ingredient : raw.ingredients()) {
            List<String> items = ingredient.items();
            if (items == null || items.isEmpty()) {
                pending.add(UNRESOLVED_INGREDIENT);
                continue;
            }
            ResourceId canonical = ResourceId.item(items.getFirst());
            equivalents.putIfAbsent(canonical, List.copyOf(items));
            ingredients.add(IngredientDto.of(canonical, 1));
        }
        for (RawFluidIngredient fluid : raw.fluidIngredients()) {
            List<String> fluids = fluid.fluids();
            if (fluids == null || fluids.isEmpty()) {
                pending.add(UNRESOLVED_INGREDIENT);
                continue;
            }
            ResourceId canonical = ResourceId.fluid(fluids.getFirst());
            equivalents.putIfAbsent(canonical, List.copyOf(fluids));
            ingredients.add(IngredientDto.of(canonical, fluid.amount()));
        }
        return ingredients;
    }

    private static List<OutputDto> outputsOf(RawRecipe raw) {
        List<OutputDto> outputs = new ArrayList<>();
        for (RawResult result : raw.results()) {
            outputs.add(outputOf(ResourceId.item(result.item()), result.count(), result.chance()));
        }
        for (RawFluidResult fluid : raw.fluidResults()) {
            outputs.add(OutputDto.certain(ResourceId.fluid(fluid.fluid()), fluid.amount()));
        }
        return outputs;
    }

    private static OutputDto outputOf(ResourceId resource, int count, float chance) {
        if (chance >= 1) {
            return OutputDto.certain(resource, count);
        }
        return new OutputDto(resource, 0, Rate.of(count).times(chanceOf(chance)));
    }

    private static Rate chanceOf(float chance) {
        return Rate.ratio(Math.round(chance * CHANCE_SCALE), CHANCE_SCALE);
    }

    private static boolean carriesDurability(RawRecipe raw) {
        return raw.ingredients().stream().anyMatch(RawIngredient::allDamageable);
    }
}