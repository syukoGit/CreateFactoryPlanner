package fr.syuko.createfactoryplanner.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceIdTest {

    @Test
    void keepsItsValueAndItsKind() {
        ResourceId iron = ResourceId.item("minecraft:iron_ingot");
        assertEquals("minecraft:iron_ingot", iron.value());
        assertEquals(ResourceKind.ITEM, iron.kind());
        assertFalse(iron.isFluid());
    }

    @Test
    void tellsFluidsApartFromItemsOfTheSameName() {
        ResourceId item = ResourceId.item("create:honey");
        ResourceId fluid = ResourceId.fluid("create:honey");
        assertNotEquals(item, fluid);
        assertTrue(fluid.isFluid());
    }

    @Test
    void refusesBlankValues() {
        assertThrows(IllegalArgumentException.class, () -> ResourceId.item(" "));
        assertThrows(IllegalArgumentException.class, () -> ResourceId.fluid(null));
    }

    @Test
    void refusesAnUndeclaredKind() {
        assertThrows(IllegalArgumentException.class, () -> new ResourceId("minecraft:water", null));
    }
}
