package fr.syuko.createfactoryplanner.core.model;

import fr.syuko.createfactoryplanner.core.machine.MachineSettings;
import fr.syuko.createfactoryplanner.core.math.Rate;
import fr.syuko.createfactoryplanner.core.model.link.Link;
import fr.syuko.createfactoryplanner.core.model.node.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlanTest {

    private static final ResourceId IRON = ResourceId.item("minecraft:iron_ingot");

    private static final MachineId PRESS = new MachineId("mechanical_press");

    private static ResourceNode conveyor(long id) {
        return ResourceNode.transport(new NodeId(id), IRON, Vec2i.ORIGIN);
    }

    private static RecipeNode press(long id) {
        return new RecipeNode(new NodeId(id),
                              new RecipeId("create:pressing/iron_sheet"),
                              PRESS,
                              MachineSettings.NONE,
                              List.of(IRON),
                              List.of(),
                              Vec2i.ORIGIN);
    }

    private static Plan connected() {
        ResourceNode conveyor = conveyor(0);
        RecipeNode press = press(1);
        Link link = new Link(new LinkId(0), conveyor.outputs().getFirst(), press.inputs().getFirst());
        return Plan.of(List.of(conveyor, press), List.of(link), null);
    }

    @Test
    void startsEmptyAndMintsIdentifiersFromZero() {
        Plan plan = Plan.empty();
        assertTrue(plan.nodes().isEmpty());
        assertTrue(plan.links().isEmpty());
        assertTrue(plan.target().isEmpty());
        assertEquals(new NodeId(0), plan.nextNodeId());
        assertEquals(new LinkId(0), plan.nextLinkId());
    }

    @Test
    void findsTheLinkSittingOnAPortWithoutWalkingTheGraph() {
        Plan plan = connected();
        Port outgoing = plan.node(new NodeId(0)).orElseThrow().outputs().getFirst();
        Port incoming = plan.node(new NodeId(1)).orElseThrow().inputs().getFirst();
        assertEquals(new LinkId(0), plan.linkAt(outgoing).orElseThrow().id());
        assertEquals(new LinkId(0), plan.linkAt(incoming).orElseThrow().id());
        assertFalse(plan.isFree(outgoing));
        assertTrue(plan.isFree(plan.node(new NodeId(0)).orElseThrow().inputs().getFirst()));
    }

    @Test
    void refusesASecondLinkOnAPortThatAlreadyCarriesOne() {
        Plan plan = connected();
        Node conveyor = plan.node(new NodeId(0)).orElseThrow();
        RecipeNode second = press(2);
        Link duplicate = new Link(new LinkId(1), conveyor.outputs().getFirst(), second.inputs().getFirst());
        Plan withSecond = plan.withNode(second);
        assertThrows(IllegalArgumentException.class, () -> withSecond.withLink(duplicate));
    }

    @Test
    void refusesALinkPointingAtAPortNoNodeExposes() {
        ResourceNode conveyor = conveyor(0);
        RecipeNode press = press(1);
        Link beyond = new Link(new LinkId(0),
                               conveyor.outputs().getFirst(),
                               new Port(new NodeId(1), 7, PortDirection.INPUT, IRON));
        List<Node> nodes = List.of(conveyor, press);
        List<Link> links = List.of(beyond);
        assertThrows(IllegalArgumentException.class, () -> Plan.of(nodes, links, null));
    }

    @Test
    void refusesALinkPointingAtAMissingNode() {
        ResourceNode conveyor = conveyor(0);
        RecipeNode absent = press(1);
        Link dangling = new Link(new LinkId(0), conveyor.outputs().getFirst(), absent.inputs().getFirst());
        List<Node> alone = List.of(conveyor);
        List<Link> links = List.of(dangling);
        assertThrows(IllegalArgumentException.class, () -> Plan.of(alone, links, null));
    }

    @Test
    void demandsThatANodeBeDisconnectedBeforeItIsDropped() {
        Plan plan = connected();
        assertThrows(IllegalArgumentException.class, () -> plan.withoutNode(new NodeId(1)));
        Plan dropped = plan.withoutLink(new LinkId(0)).withoutNode(new NodeId(1));
        assertEquals(1, dropped.nodes().size());
        assertTrue(dropped.links().isEmpty());
    }

    @Test
    void refusesToReplaceANodeWithOneThatLostAConnectedPort() {
        Plan plan = connected();
        RecipeNode withoutItsInput = new RecipeNode(new NodeId(1),
                                                    new RecipeId("create:pressing/iron_sheet"),
                                                    PRESS,
                                                    MachineSettings.NONE,
                                                    List.of(),
                                                    List.of(),
                                                    Vec2i.ORIGIN);
        assertThrows(IllegalArgumentException.class, () -> plan.withNode(withoutItsInput));
    }

    @Test
    void neverHandsOutAnIdentifierItAlreadyUsed() {
        Plan plan = connected();
        assertEquals(new NodeId(2), plan.nextNodeId());
        assertEquals(new LinkId(1), plan.nextLinkId());
        Plan pruned = plan.withoutLink(new LinkId(0)).withoutNode(new NodeId(1));
        assertEquals(new NodeId(2), pruned.nextNodeId());
        assertEquals(new LinkId(1), pruned.nextLinkId());
    }

    @Test
    void leavesTheOriginalUntouchedOnEveryEdit() {
        Plan plan = connected();
        Plan edited = plan.withNode(conveyor(5)).withTarget(new Target(IRON, Rate.of(3)));
        assertEquals(2, plan.nodes().size());
        assertTrue(plan.target().isEmpty());
        assertEquals(3, edited.nodes().size());
        assertEquals(Rate.of(3), edited.target().orElseThrow().rate());
    }

    @Test
    void comparesOnItsContentRatherThanOnItsEditingHistory() {
        Plan built = Plan.empty().withNode(conveyor(0)).withNode(press(1));
        Plan rebuilt = Plan.of(List.of(conveyor(0), press(1)), List.of(), null);
        assertEquals(built, rebuilt);
        assertEquals(built.hashCode(), rebuilt.hashCode());

        Plan withGap = Plan.empty()
                           .withNode(conveyor(0))
                           .withNode(conveyor(9))
                           .withoutNode(new NodeId(9))
                           .withNode(press(1));
        assertEquals(built, withGap);
        assertEquals(new NodeId(10), withGap.nextNodeId());
        assertEquals(new NodeId(2), built.nextNodeId());
    }

    @Test
    void listsTheLinksAttachedToANode() {
        Plan plan = connected();
        assertEquals(1, plan.linksOf(new NodeId(0)).size());
        assertEquals(1, plan.linksOf(new NodeId(1)).size());
        assertTrue(plan.linksOf(new NodeId(42)).isEmpty());
    }
}