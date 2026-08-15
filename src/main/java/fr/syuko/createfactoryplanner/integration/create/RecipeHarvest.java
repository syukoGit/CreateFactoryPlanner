package fr.syuko.createfactoryplanner.integration.create;

import fr.syuko.createfactoryplanner.core.model.NamespacedId;
import fr.syuko.createfactoryplanner.core.model.RecipeNode;

import java.util.*;

public record RecipeHarvest(int inspectedRecipes, List<RecipeNode> nodes, Map<NamespacedId, Integer> supportedTypes,
                            Map<NamespacedId, Integer> unsupportedTypes, List<NamespacedId> manualOnly,
                            List<HarvestProblem> problems) {
    public RecipeHarvest {
        nodes = List.copyOf(Objects.requireNonNull(nodes, "nodes"));
        supportedTypes = sortedCopy(supportedTypes);
        unsupportedTypes = sortedCopy(unsupportedTypes);
        manualOnly = List.copyOf(Objects.requireNonNull(manualOnly, "manualOnly"));
        problems = List.copyOf(Objects.requireNonNull(problems, "problems"));
    }

    private static Map<NamespacedId, Integer> sortedCopy(Map<NamespacedId, Integer> source) {
        return Collections.unmodifiableSortedMap(new TreeMap<>(Objects.requireNonNull(source, "counts")));
    }
}