package fr.syuko.createfactoryplanner.core.model;

public record MachineId(String value) {

    public MachineId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("a machine id cannot be blank");
        }
    }
}