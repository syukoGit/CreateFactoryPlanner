package fr.syuko.createfactoryplanner.integration.create;

import fr.syuko.createfactoryplanner.core.model.NamespacedId;

public record HarvestProblem(NamespacedId recipeId, NamespacedId recipeType, String reason) {
}
