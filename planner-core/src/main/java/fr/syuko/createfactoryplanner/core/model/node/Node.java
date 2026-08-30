package fr.syuko.createfactoryplanner.core.model.node;

import fr.syuko.createfactoryplanner.core.model.NodeId;
import fr.syuko.createfactoryplanner.core.model.Vec2i;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public sealed interface Node permits ResourceNode, RecipeNode, RoutingNode {

    NodeId id();

    NodeFamily family();

    List<Port> inputs();

    List<Port> outputs();

    Vec2i position();

    Node movedTo(Vec2i position);

    default List<Port> ports() {
        return Stream.concat(inputs().stream(), outputs().stream()).toList();
    }

    default Optional<Port> port(int index, PortDirection direction) {
        List<Port> side = direction == PortDirection.INPUT
                          ? inputs()
                          : outputs();
        return index >= 0 && index < side.size()
               ? Optional.of(side.get(index))
               : Optional.empty();
    }

    default boolean exposes(Port port) {
        return port.owner().equals(id()) && port(port.index(), port.direction()).filter(port::equals).isPresent();
    }
}
