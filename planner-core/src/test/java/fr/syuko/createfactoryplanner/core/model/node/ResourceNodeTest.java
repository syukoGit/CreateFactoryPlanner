package fr.syuko.createfactoryplanner.core.model.node;

import fr.syuko.createfactoryplanner.core.math.Rate;
import fr.syuko.createfactoryplanner.core.model.NodeId;
import fr.syuko.createfactoryplanner.core.model.ResourceId;
import fr.syuko.createfactoryplanner.core.model.Vec2i;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ResourceNodeTest {

    private static final NodeId ID = new NodeId(1);

    private static final ResourceId IRON = ResourceId.item("minecraft:iron_ingot");

    @Test
    void carriesOneInputAndOneOutputWhenItIsAConveyor() {
        ResourceNode conveyor = ResourceNode.transport(ID, IRON, Vec2i.ORIGIN);
        assertEquals(NodeFamily.RESOURCE, conveyor.family());
        assertEquals(1, conveyor.inputs().size());
        assertEquals(1, conveyor.outputs().size());
        assertEquals(IRON, conveyor.inputs().getFirst().resource());
        assertTrue(conveyor.inputs().getFirst().isTyped());
    }

    @Test
    void closesTheGraphOnBothEnds() {
        ResourceNode source = ResourceNode.source(ID, IRON, Rate.of(3), Vec2i.ORIGIN);
        ResourceNode sink = ResourceNode.sink(ID, IRON, Vec2i.ORIGIN);
        assertTrue(source.inputs().isEmpty());
        assertEquals(1, source.outputs().size());
        assertEquals(1, sink.inputs().size());
        assertTrue(sink.outputs().isEmpty());
    }

    @Test
    void statesAnUnlimitedSupplyAsAnAbsentOneRatherThanAsNull() {
        ResourceNode unlimited = ResourceNode.unlimitedSource(ID, IRON, Vec2i.ORIGIN);
        assertTrue(unlimited.declaredSupply().isEmpty());
        assertTrue(unlimited.isUnlimitedSource());
        assertFalse(ResourceNode.source(ID, IRON, Rate.of(3), Vec2i.ORIGIN).isUnlimitedSource());
    }

    @Test
    void refusesADeclaredSupplyOnAnythingButASource() {
        Optional<Rate> supply = Optional.of(Rate.of(3));
        assertThrows(IllegalArgumentException.class,
                     () -> new ResourceNode(ID, IRON, ResourceRole.TRANSPORT, supply, Vec2i.ORIGIN));
        assertThrows(IllegalArgumentException.class,
                     () -> new ResourceNode(ID, IRON, ResourceRole.SINK, supply, Vec2i.ORIGIN));
    }

    @Test
    void refusesANegativeSupply() {
        Optional<Rate> negative = Optional.of(Rate.perTick(-1, 3));
        assertThrows(IllegalArgumentException.class,
                     () -> new ResourceNode(ID, IRON, ResourceRole.SOURCE, negative, Vec2i.ORIGIN));
    }

    @Test
    void movesWithoutLosingAnythingElse() {
        ResourceNode moved = ResourceNode.transport(ID, IRON, Vec2i.ORIGIN).movedTo(new Vec2i(120, 80));
        assertEquals(new Vec2i(120, 80), moved.position());
        assertEquals(IRON, moved.resource());
        assertEquals(ID, moved.id());
    }
}