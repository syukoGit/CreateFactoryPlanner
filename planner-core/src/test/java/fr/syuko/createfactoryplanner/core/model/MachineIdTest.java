package fr.syuko.createfactoryplanner.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MachineIdTest {

    @Test
    void keepsItsValue() {
        assertEquals("millstone", new MachineId("millstone").value());
    }

    @Test
    void refusesBlankValues() {
        assertThrows(IllegalArgumentException.class, () -> new MachineId(" "));
        assertThrows(IllegalArgumentException.class, () -> new MachineId(null));
    }
}
