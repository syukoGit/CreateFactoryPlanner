package fr.syuko.createfactoryplanner.core.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LinkIdTest {

    @Test
    void keepsItsValue() {
        assertEquals(4, new LinkId(4).value());
    }

    @Test
    void refusesNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> new LinkId(-1));
    }

    @Test
    void ordersByValueSoSplitsAllocateInAStableOrder() {
        List<LinkId> sorted = Stream.of(new LinkId(5), new LinkId(1), new LinkId(9)).sorted().toList();
        assertEquals(List.of(new LinkId(1), new LinkId(5), new LinkId(9)), sorted);
    }
}