package fr.syuko.createfactoryplanner.core.recipe;

import fr.syuko.createfactoryplanner.core.model.ResourceId;

public record CatalystDto(ResourceId resource, long amountPerMachine) {

    public CatalystDto {
        if (resource == null) {
            throw new IllegalArgumentException("a catalyst must carry a resource");
        }
        if (amountPerMachine <= 0) {
            throw new IllegalArgumentException("a catalyst amount must be positive, got " + amountPerMachine + " of " + resource.value());
        }
    }
}