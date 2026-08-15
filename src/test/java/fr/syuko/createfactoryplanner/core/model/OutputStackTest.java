package fr.syuko.createfactoryplanner.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OutputStackTest {
    private static final ItemKey IRON_NUGGET = ItemKey.item(NamespacedId.parse("minecraft:iron_nugget"));

    @Test
    void expectationOfACertainOutputIsItsCount() {
        assertEquals(1.0, OutputStack.guaranteed(IRON_NUGGET, 1).expectedPerOperation(), 1e-9);
        assertEquals(3.0, OutputStack.guaranteed(IRON_NUGGET, 3).expectedPerOperation(), 1e-9);
    }

    @Test
    void expectationScalesWithChance() {
        assertEquals(0.3, new OutputStack(IRON_NUGGET, 1, 0.3f).expectedPerOperation(), 1e-6);
        assertEquals(0.8, new OutputStack(IRON_NUGGET, 1, 0.8f).expectedPerOperation(), 1e-6);
    }

    @Test
    void expectationOfASizedChanceOutputMultipliesBoth() {
        assertEquals(1.5, new OutputStack(IRON_NUGGET, 3, 0.5f).expectedPerOperation(), 1e-6);
    }

    @Test
    void certainOnlyWhenChanceReachesOne() {
        assertTrue(OutputStack.guaranteed(IRON_NUGGET, 1).certain());
        assertFalse(new OutputStack(IRON_NUGGET, 1, 0.99f).certain());
    }

    @Test
    void rejectsNonPositiveCountAndNegativeChance() {
        assertThrows(IllegalArgumentException.class, () -> new OutputStack(IRON_NUGGET, 0, 1f));
        assertThrows(IllegalArgumentException.class, () -> new OutputStack(IRON_NUGGET, 1, -0.1f));
    }
}