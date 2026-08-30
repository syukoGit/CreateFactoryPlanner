package fr.syuko.createfactoryplanner.core.model.link;

import fr.syuko.createfactoryplanner.core.model.LinkId;
import fr.syuko.createfactoryplanner.core.model.node.Port;
import fr.syuko.createfactoryplanner.core.model.node.PortDirection;

public record Link(LinkId id, Port from, Port to) {

    public Link {
        if (id == null) {
            throw new IllegalArgumentException("a link must carry an id");
        }
        if (from == null || to == null) {
            throw new IllegalArgumentException("a link must join two ports");
        }
        if (from.direction() != PortDirection.OUTPUT || to.direction() != PortDirection.INPUT) {
            throw new IllegalArgumentException("a link runs from an output port to an input port");
        }
        if (from.owner().equals(to.owner())) {
            throw new IllegalArgumentException("a link cannot loop a node onto itself, on " + from.owner().value());
        }
    }

    public boolean touches(Port port) {
        return from.equals(port) || to.equals(port);
    }
}
