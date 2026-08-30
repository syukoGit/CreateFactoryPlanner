package fr.syuko.createfactoryplanner.core.recipe;

import fr.syuko.createfactoryplanner.core.model.RecipeId;

import java.util.Optional;

public interface RecipeCatalog {

    Optional<RecipeDto> recipe(RecipeId id);
}
