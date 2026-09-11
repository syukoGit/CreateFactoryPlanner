package fr.syuko.createfactoryplanner.core.io;

import fr.syuko.createfactoryplanner.core.math.Rate;
import fr.syuko.createfactoryplanner.core.recipe.OutputDto;

public record OutputEntry(ResourceEntry resource, long guaranteed, String expectedPerOperation) {

    public OutputEntry {
        if (resource == null) {
            throw new IllegalArgumentException("an output entry must carry a resource");
        }
    }

    public static OutputEntry of(OutputDto output) {
        return new OutputEntry(ResourceEntry.of(output.resource()),
                               output.guaranteed(),
                               output.expectedPerOperation().toString());
    }

    public OutputDto toDto() {
        return new OutputDto(resource.toResourceId(), guaranteed, Rate.parse(expectedPerOperation));
    }
}
