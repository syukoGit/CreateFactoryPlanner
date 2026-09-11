package fr.syuko.createfactoryplanner.core.io;

import fr.syuko.createfactoryplanner.core.recipe.CatalystDto;

public record CatalystEntry(ResourceEntry resource, long amountPerMachine) {

    public CatalystEntry {
        if (resource == null) {
            throw new IllegalArgumentException("a catalyst entry must carry a resource");
        }
    }

    public static CatalystEntry of(CatalystDto catalyst) {
        return new CatalystEntry(ResourceEntry.of(catalyst.resource()), catalyst.amountPerMachine());
    }

    public CatalystDto toDto() {
        return new CatalystDto(resource.toResourceId(), amountPerMachine);
    }
}
