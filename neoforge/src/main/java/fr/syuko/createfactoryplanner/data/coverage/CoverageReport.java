package fr.syuko.createfactoryplanner.data.coverage;

import fr.syuko.createfactoryplanner.core.io.Coverage;
import fr.syuko.createfactoryplanner.core.io.MachineEntry;
import fr.syuko.createfactoryplanner.core.io.RecipeEntry;
import fr.syuko.createfactoryplanner.data.recipe.HarvestedRecipe;
import fr.syuko.createfactoryplanner.data.recipe.RecipeTypeEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class CoverageReport {

    private static final String UNSUPPORTED = "unsupported";

    private CoverageReport() {
    }

    public static Coverage of(List<MachineEntry> machines,
                              List<RecipeEntry> recipes,
                              List<RecipeTypeEntry> types,
                              List<HarvestedRecipe> harvested) {
        return new Coverage(typesWithoutMachine(types),
                            machinesWithout(machines, entry -> !entry.readsItsStressImpact()),
                            machinesWithoutThroughputModel(machines),
                            unreadable(harvested),
                            pendingRules(recipes));
    }

    private static List<String> typesWithoutMachine(List<RecipeTypeEntry> types) {
        return types.stream()
                    .filter(type -> !type.bound())
                    .filter(type -> type.recipeCount() > 0)
                    .map(type -> type.id() + " (" + type.recipeCount() + " recipes)")
                    .toList();
    }

    private static List<String> machinesWithout(List<MachineEntry> machines, Predicate<MachineEntry> gap) {
        return machines.stream().filter(gap).map(MachineEntry::id).toList();
    }

    private static List<String> machinesWithoutThroughputModel(List<MachineEntry> machines) {
        return machines.stream().map(MachineEntry::id).toList();
    }

    private static List<String> unreadable(List<HarvestedRecipe> harvested) {
        return harvested.stream()
                        .filter(recipe -> UNSUPPORTED.equals(recipe.recipe().reader()))
                        .map(recipe -> recipe.recipe().id())
                        .toList();
    }

    private static List<String> pendingRules(List<RecipeEntry> recipes) {
        Map<String, Long> counted = recipes.stream()
                                           .flatMap(recipe -> recipe.pending().stream())
                                           .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<String> gaps = new ArrayList<>();
        counted.forEach((rule, count) -> gaps.add(rule + ": " + count + " recipes"));
        return gaps;
    }
}