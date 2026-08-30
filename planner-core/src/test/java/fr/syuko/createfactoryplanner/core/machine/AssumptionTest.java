package fr.syuko.createfactoryplanner.core.machine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssumptionTest {

    @Test
    void carriesATranslationKeyRatherThanADisplayString() {
        Assumption assumption = Assumption.of("createfactoryplanner.assumption.blocking_filter");
        assertEquals("createfactoryplanner.assumption.blocking_filter", assumption.translationKey());
        assertTrue(assumption.arguments().isEmpty());
    }

    @Test
    void keepsItsArgumentsImmutable() {
        Assumption assumption = new Assumption("createfactoryplanner.assumption.one_machine_each", List.of("3"));
        assertEquals(List.of("3"), assumption.arguments());
        assertThrows(UnsupportedOperationException.class, () -> assumption.arguments().add("4"));
    }

    @Test
    void refusesABlankKey() {
        assertThrows(IllegalArgumentException.class, () -> Assumption.of(" "));
        assertThrows(IllegalArgumentException.class, () -> Assumption.of(null));
    }
}