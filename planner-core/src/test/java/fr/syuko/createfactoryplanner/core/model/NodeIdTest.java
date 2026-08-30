package fr.syuko.createfactoryplanner.core.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NodeIdTest {

    @Test
    void keepsItsValue() {
        assertEquals(7, new NodeId(7).value());
    }

    @Test
    void refusesNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> new NodeId(-1));
    }

    @Test
    void ordersByValueSoDiagnosticsStayDeterministic() {
        List<NodeId> sorted = Stream.of(new NodeId(3), new NodeId(0), new NodeId(12)).sorted().toList();
        assertEquals(List.of(new NodeId(0), new NodeId(3), new NodeId(12)), sorted);
    }
}