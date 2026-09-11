package fr.syuko.createfactoryplanner.core.io;

import java.util.List;

public record Coverage(List<String> recipeTypesWithoutMachine, List<String> machinesWithoutStressImpact,
                       List<String> machinesWithoutThroughputModel, List<String> unreadableRecipes,
                       List<String> recipesPendingARule) {

    public static final Coverage NONE = new Coverage(List.of(), List.of(), List.of(), List.of(), List.of());

    public Coverage {
        recipeTypesWithoutMachine = copySorted(recipeTypesWithoutMachine);
        machinesWithoutStressImpact = copySorted(machinesWithoutStressImpact);
        machinesWithoutThroughputModel = copySorted(machinesWithoutThroughputModel);
        unreadableRecipes = copySorted(unreadableRecipes);
        recipesPendingARule = copySorted(recipesPendingARule);
    }

    private static List<String> copySorted(List<String> entries) {
        return entries == null
               ? List.of()
               : entries.stream().sorted().toList();
    }

    public int gapCount() {
        return recipeTypesWithoutMachine.size() + machinesWithoutStressImpact.size() + machinesWithoutThroughputModel.size() + unreadableRecipes.size() + recipesPendingARule.size();
    }

    public boolean isComplete() {
        return gapCount() == 0;
    }
}
