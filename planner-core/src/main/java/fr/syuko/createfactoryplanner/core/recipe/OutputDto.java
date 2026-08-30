package fr.syuko.createfactoryplanner.core.recipe;

import fr.syuko.createfactoryplanner.core.math.Rate;
import fr.syuko.createfactoryplanner.core.model.ResourceId;

public record OutputDto(ResourceId resource, long guaranteed, Rate expectedPerOperation) {

    public OutputDto {
        if (resource == null) {
            throw new IllegalArgumentException("an output must carry a resource");
        }
        if (expectedPerOperation == null) {
            throw new IllegalArgumentException("an output must carry an expectation");
        }
        if (guaranteed < 0) {
            throw new IllegalArgumentException("a guaranteed part cannot be negative, got " + guaranteed + " of " + resource.value());
        }
        if (expectedPerOperation.compareTo(Rate.of(guaranteed)) < 0) {
            throw new IllegalArgumentException("an expectation cannot fall below its guaranteed part, got " + expectedPerOperation + " for " + guaranteed + " of " + resource.value());
        }
    }

    public static OutputDto certain(ResourceId resource, long amount) {
        return new OutputDto(resource, amount, Rate.of(amount));
    }

    public boolean isProbabilistic() {
        return !expectedPerOperation.equals(Rate.of(guaranteed));
    }
}