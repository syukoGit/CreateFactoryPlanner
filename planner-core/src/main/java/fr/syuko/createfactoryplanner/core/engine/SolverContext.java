package fr.syuko.createfactoryplanner.core.engine;

import fr.syuko.createfactoryplanner.core.machine.MachineCatalog;
import fr.syuko.createfactoryplanner.core.recipe.RecipeCatalog;

public record SolverContext(MachineCatalog machines, RecipeCatalog recipes) {

    public SolverContext {
        if (machines == null) {
            throw new IllegalArgumentException("a solver context must carry a machine catalog");
        }
        if (recipes == null) {
            throw new IllegalArgumentException("a solver context must carry a recipe catalog");
        }
    }
}
