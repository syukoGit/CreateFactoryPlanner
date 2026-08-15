package fr.syuko.createfactoryplanner.core.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NamespacedIdTest {
    @Test
    void parsesNamespaceAndPath() {
        NamespacedId id = NamespacedId.parse("create:crushed_raw_iron");

        assertEquals("create", id.namespace());
        assertEquals("crushed_raw_iron", id.path());
        assertEquals("create:crushed_raw_iron", id.toString());
    }

    @Test
    void keepsColonsInsidePath() {
        NamespacedId id = NamespacedId.parse("create:crushing/raw_iron:variant");

        assertEquals("create", id.namespace());
        assertEquals("crushing/raw_iron:variant", id.path());
    }

    @Test
    void rejectsIdentifierWithoutSeparator() {
        assertThrows(IllegalArgumentException.class, () -> NamespacedId.parse("crushed_raw_iron"));
    }

    @Test
    void rejectsBlankParts() {
        assertThrows(IllegalArgumentException.class, () -> new NamespacedId(" ", "iron"));
        assertThrows(IllegalArgumentException.class, () -> new NamespacedId("create", ""));
    }

    @Test
    void sortsLexicographically() {
        List<NamespacedId> sorted = Stream.of(NamespacedId.parse("minecraft:iron_ingot"),
                                              NamespacedId.parse("create:andesite_alloy"),
                                              NamespacedId.parse("create:zinc_ingot")).sorted().toList();

        assertEquals(List.of("create:andesite_alloy", "create:zinc_ingot", "minecraft:iron_ingot"),
                     sorted.stream().map(NamespacedId::toString).toList());
    }
}