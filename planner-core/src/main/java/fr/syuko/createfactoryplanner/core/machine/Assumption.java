package fr.syuko.createfactoryplanner.core.machine;

import java.util.List;

public record Assumption(String translationKey, List<String> arguments) {

    public Assumption {
        if (translationKey == null || translationKey.isBlank()) {
            throw new IllegalArgumentException("an assumption cannot be blank");
        }
        arguments = List.copyOf(arguments);
    }

    public static Assumption of(String translationKey) {
        return new Assumption(translationKey, List.of());
    }
}
