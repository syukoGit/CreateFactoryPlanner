package fr.syuko.createfactoryplanner.core.model.node;

import fr.syuko.createfactoryplanner.core.math.Rate;
import fr.syuko.createfactoryplanner.core.model.NodeId;
import fr.syuko.createfactoryplanner.core.model.ResourceId;
import fr.syuko.createfactoryplanner.core.model.Vec2i;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record ResourceNode(NodeId id, ResourceId resource, ResourceRole role, Optional<Rate> declaredSupply,
                           Vec2i position) implements Node {

    public ResourceNode {
        if (id == null) {
            throw new IllegalArgumentException("a node must carry an id");
        }
        if (resource == null) {
            throw new IllegalArgumentException("a resource node carries exactly one resource");
        }
        if (role == null) {
            throw new IllegalArgumentException("a resource node must declare its role");
        }
        if (position == null) {
            throw new IllegalArgumentException("a node must carry a position");
        }
        Objects.requireNonNull(declaredSupply, "a declared supply must be stated, even as empty");
        if (role != ResourceRole.SOURCE && declaredSupply.isPresent()) {
            throw new IllegalArgumentException("only a source declares a supply, got one on a " + role + " node carrying " + resource.value());
        }
        if (declaredSupply.filter(supply -> supply.compareTo(Rate.ZERO) < 0).isPresent()) {
            throw new IllegalArgumentException("a declared supply cannot be negative on " + resource.value());
        }
    }

    public static ResourceNode transport(NodeId id, ResourceId resource, Vec2i position) {
        return new ResourceNode(id, resource, ResourceRole.TRANSPORT, Optional.empty(), position);
    }

    public static ResourceNode source(NodeId id, ResourceId resource, Rate supply, Vec2i position) {
        return new ResourceNode(id, resource, ResourceRole.SOURCE, Optional.of(supply), position);
    }

    public static ResourceNode unlimitedSource(NodeId id, ResourceId resource, Vec2i position) {
        return new ResourceNode(id, resource, ResourceRole.SOURCE, Optional.empty(), position);
    }

    public static ResourceNode sink(NodeId id, ResourceId resource, Vec2i position) {
        return new ResourceNode(id, resource, ResourceRole.SINK, Optional.empty(), position);
    }

    @Override
    public NodeFamily family() {
        return NodeFamily.RESOURCE;
    }

    @Override
    public List<Port> inputs() {
        return role == ResourceRole.SOURCE
               ? List.of()
               : List.of(new Port(id, 0, PortDirection.INPUT, resource));
    }

    @Override
    public List<Port> outputs() {
        return role == ResourceRole.SINK
               ? List.of()
               : List.of(new Port(id, 0, PortDirection.OUTPUT, resource));
    }

    @Override
    public ResourceNode movedTo(Vec2i target) {
        return new ResourceNode(id, resource, role, declaredSupply, target);
    }

    public boolean isUnlimitedSource() {
        return role == ResourceRole.SOURCE && declaredSupply.isEmpty();
    }
}