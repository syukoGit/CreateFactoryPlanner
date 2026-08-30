package fr.syuko.createfactoryplanner.core.model.link;

import fr.syuko.createfactoryplanner.core.model.LinkId;
import fr.syuko.createfactoryplanner.core.model.NodeId;
import fr.syuko.createfactoryplanner.core.model.ResourceId;
import fr.syuko.createfactoryplanner.core.model.node.Port;
import fr.syuko.createfactoryplanner.core.model.node.PortDirection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinkTest {

    private static final ResourceId IRON = ResourceId.item("minecraft:iron_ingot");

    private static final Port OUTGOING = new Port(new NodeId(0), 0, PortDirection.OUTPUT, IRON);

    private static final Port INCOMING = new Port(new NodeId(1), 0, PortDirection.INPUT, IRON);

    @Test
    void joinsTheTwoPortsItWasGiven() {
        Link link = new Link(new LinkId(0), OUTGOING, INCOMING);
        assertTrue(link.touches(OUTGOING));
        assertTrue(link.touches(INCOMING));
        assertFalse(link.touches(new Port(new NodeId(2), 0, PortDirection.INPUT, IRON)));
    }

    @Test
    void refusesToRunBackwards() {
        assertThrows(IllegalArgumentException.class, () -> new Link(new LinkId(0), INCOMING, OUTGOING));
        assertThrows(IllegalArgumentException.class, () -> new Link(new LinkId(0), OUTGOING, OUTGOING));
    }

    @Test
    void refusesToLoopANodeOntoItself() {
        Port ownOutput = new Port(new NodeId(0), 0, PortDirection.OUTPUT, IRON);
        Port ownInput = new Port(new NodeId(0), 0, PortDirection.INPUT, IRON);
        assertThrows(IllegalArgumentException.class, () -> new Link(new LinkId(0), ownOutput, ownInput));
    }
}