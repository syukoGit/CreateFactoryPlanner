package fr.syuko.createfactoryplanner.core.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InputStackTest {
    private static final ItemKey IRON_INGOT = ItemKey.item(NamespacedId.parse("minecraft:iron_ingot"));

    private static final ItemKey ZINC_INGOT = ItemKey.item(NamespacedId.parse("create:zinc_ingot"));

    private static final ItemKey WATER = ItemKey.fluid(NamespacedId.parse("minecraft:water"));

    @Test
    void keepsEveryAcceptedItemOfATag() {
        InputStack input = new InputStack(List.of(IRON_INGOT, ZINC_INGOT), IRON_INGOT, 1, true);

        assertEquals(List.of(IRON_INGOT, ZINC_INGOT), input.accepted());
        assertEquals(IRON_INGOT, input.representative());
    }

    @Test
    void isFluidWhenItsRepresentativeIs() {
        assertTrue(new InputStack(List.of(WATER), WATER, 250, true).fluid());
        assertFalse(new InputStack(List.of(IRON_INGOT), IRON_INGOT, 1, true).fluid());
    }

    @Test
    void rejectsARepresentativeOutsideTheAcceptedSet() {
        assertThrows(IllegalArgumentException.class, () -> new InputStack(List.of(IRON_INGOT), ZINC_INGOT, 1, true));
    }

    @Test
    void rejectsEmptyAcceptedSetAndNonPositiveAmount() {
        assertThrows(IllegalArgumentException.class, () -> new InputStack(List.of(), IRON_INGOT, 1, true));
        assertThrows(IllegalArgumentException.class, () -> new InputStack(List.of(IRON_INGOT), IRON_INGOT, 0, true));
    }

    @Test
    void isImmutableAgainstItsSourceList() {
        List<ItemKey> source = new ArrayList<>(List.of(IRON_INGOT));
        InputStack input = new InputStack(source, IRON_INGOT, 1, true);

        source.add(ZINC_INGOT);

        assertEquals(List.of(IRON_INGOT), input.accepted());
        assertThrows(UnsupportedOperationException.class, () -> input.accepted().add(ZINC_INGOT));
    }

    @Test
    void withAmountKeepsEveryOtherField() {
        InputStack input = new InputStack(List.of(IRON_INGOT), IRON_INGOT, 1, false).withAmount(4);

        assertEquals(4, input.amount());
        assertFalse(input.consumed());
        assertEquals(IRON_INGOT, input.representative());
    }
}