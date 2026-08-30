package fr.syuko.createfactoryplanner.core.model.node;

import fr.syuko.createfactoryplanner.core.model.NodeId;
import fr.syuko.createfactoryplanner.core.model.Vec2i;

import java.util.List;
import java.util.stream.IntStream;

public record RoutingNode(NodeId id, int inputCount, List<OutputBranch> branches, AllocationMode mode, Vec2i position)
        implements Node {

    public RoutingNode {
        if (id == null) {
            throw new IllegalArgumentException("a node must carry an id");
        }
        if (position == null) {
            throw new IllegalArgumentException("a node must carry a position");
        }
        if (mode == null) {
            throw new IllegalArgumentException("a routing node must declare its allocation mode");
        }
        branches = List.copyOf(branches);
        if (inputCount < 1) {
            throw new IllegalArgumentException("a routing node needs at least one input, got " + inputCount);
        }
        if (branches.isEmpty()) {
            throw new IllegalArgumentException("a routing node needs at least one output");
        }
        if (mode == AllocationMode.FIXED_RATIO && branches.stream().anyMatch(branch -> branch.fixedRatio() < 1)) {
            throw new IllegalArgumentException("a fixed ratio split gives every branch a ratio of at least one");
        }
    }

    public static RoutingNode dynamic(NodeId id, int inputCount, int outputCount, Vec2i position) {
        return new RoutingNode(id,
                               inputCount,
                               IntStream.range(0, outputCount).mapToObj(branch -> OutputBranch.everything()).toList(),
                               AllocationMode.DYNAMIC,
                               position);
    }

    @Override
    public NodeFamily family() {
        return NodeFamily.PROCESSING;
    }

    @Override
    public List<Port> inputs() {
        return IntStream.range(0, inputCount).mapToObj(index -> Port.untyped(id, index, PortDirection.INPUT)).toList();
    }

    @Override
    public List<Port> outputs() {
        return IntStream.range(0, branches.size())
                        .mapToObj(index -> Port.untyped(id, index, PortDirection.OUTPUT))
                        .toList();
    }

    @Override
    public RoutingNode movedTo(Vec2i target) {
        return new RoutingNode(id, inputCount, branches, mode, target);
    }

    public OutputBranch branchOf(Port port) {
        return branches.get(port.index());
    }
}