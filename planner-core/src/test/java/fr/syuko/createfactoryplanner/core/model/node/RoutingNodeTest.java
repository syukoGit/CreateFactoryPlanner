package fr.syuko.createfactoryplanner.core.model.node;

import fr.syuko.createfactoryplanner.core.model.NodeId;
import fr.syuko.createfactoryplanner.core.model.ResourceId;
import fr.syuko.createfactoryplanner.core.model.Vec2i;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RoutingNodeTest {

    private static final NodeId ID = new NodeId(2);

    private static final ResourceId PLATE = ResourceId.item("create:iron_sheet");

    private static final ResourceId INGOT = ResourceId.item("minecraft:iron_ingot");

    @Test
    void isTheOnlyNodeWhosePortsCarryNoResource() {
        RoutingNode tunnel = RoutingNode.dynamic(ID, 2, 3, Vec2i.ORIGIN);
        assertEquals(NodeFamily.PROCESSING, tunnel.family());
        assertEquals(2, tunnel.inputs().size());
        assertEquals(3, tunnel.outputs().size());
        assertFalse(tunnel.inputs().getFirst().isTyped());
        assertTrue(tunnel.outputs().getFirst().carries(PLATE));
    }

    @Test
    void filtersDecideWhichBranchAResourceMayReach() {
        OutputBranch plates = OutputBranch.accepting(Set.of(PLATE));
        assertTrue(plates.accepts(PLATE));
        assertFalse(plates.accepts(INGOT));
        assertTrue(OutputBranch.everything().accepts(INGOT));
    }

    @Test
    void demandsARatioOnEveryBranchOfAFixedSplit() {
        List<OutputBranch> branches = List.of(OutputBranch.everything().withRatio(0), OutputBranch.everything());
        assertThrows(IllegalArgumentException.class,
                     () -> new RoutingNode(ID, 1, branches, AllocationMode.FIXED_RATIO, Vec2i.ORIGIN));
        assertEquals(AllocationMode.DYNAMIC,
                     new RoutingNode(ID, 1, branches, AllocationMode.DYNAMIC, Vec2i.ORIGIN).mode());
    }

    @Test
    void refusesANodeThatCouldNotRouteAnything() {
        List<OutputBranch> none = List.of();
        List<OutputBranch> one = List.of(OutputBranch.everything());
        assertThrows(IllegalArgumentException.class,
                     () -> new RoutingNode(ID, 1, none, AllocationMode.DYNAMIC, Vec2i.ORIGIN));
        assertThrows(IllegalArgumentException.class,
                     () -> new RoutingNode(ID, 0, one, AllocationMode.DYNAMIC, Vec2i.ORIGIN));
    }

    @Test
    void findsTheBranchBehindAnOutputPort() {
        RoutingNode tunnel = new RoutingNode(ID,
                                             1,
                                             List.of(OutputBranch.accepting(Set.of(PLATE)), OutputBranch.everything()),
                                             AllocationMode.DYNAMIC,
                                             Vec2i.ORIGIN);
        assertTrue(tunnel.branchOf(tunnel.outputs().getFirst()).accepts(PLATE));
        assertFalse(tunnel.branchOf(tunnel.outputs().getFirst()).accepts(INGOT));
        assertTrue(tunnel.branchOf(tunnel.outputs().get(1)).accepts(INGOT));
    }
}