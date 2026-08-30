package fr.syuko.createfactoryplanner.core.model;

import fr.syuko.createfactoryplanner.core.math.Rate;

public record Target(ResourceId resource, Rate rate) {

    public Target {
        if (resource == null) {
            throw new IllegalArgumentException("a target must name a resource");
        }
        if (rate == null) {
            throw new IllegalArgumentException("a target must carry a rate");
        }
        if (rate.compareTo(Rate.ZERO) <= 0) {
            throw new IllegalArgumentException("a target rate must be positive, got " + rate + " of " + resource.value());
        }
    }
}