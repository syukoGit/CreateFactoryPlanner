package fr.syuko.createfactoryplanner.core.model;

import fr.syuko.createfactoryplanner.core.model.link.Link;
import fr.syuko.createfactoryplanner.core.model.node.Node;
import fr.syuko.createfactoryplanner.core.model.node.Port;

import java.util.*;
import java.util.stream.LongStream;

public final class Plan {

    private static final Plan EMPTY = new Plan(Map.of(), Map.of(), 0, 0);

    private final Map<NodeId, Node> nodes;

    private final Map<LinkId, Link> links;

    private final Map<Port, LinkId> linksByPort;

    private final long nextNodeValue;

    private final long nextLinkValue;

    private Plan(Map<NodeId, Node> nodes,
                 Map<LinkId, Link> links,
                 long nextNodeValue,
                 long nextLinkValue) {
        this.nodes = Map.copyOf(nodes);
        this.links = Map.copyOf(links);
        this.nextNodeValue = nextNodeValue;
        this.nextLinkValue = nextLinkValue;
        this.linksByPort = indexByPort(this.nodes, this.links);
    }

    public static Plan empty() {
        return EMPTY;
    }

    public static Plan of(Collection<Node> nodes, Collection<Link> links) {
        Map<NodeId, Node> nodesById = new LinkedHashMap<>();
        for (Node node : nodes) {
            if (nodesById.put(node.id(), node) != null) {
                throw new IllegalArgumentException("a plan carries one node per id, got " + node.id()
                                                                                                .value() + " twice");
            }
        }
        Map<LinkId, Link> linksById = new LinkedHashMap<>();
        for (Link link : links) {
            if (linksById.put(link.id(), link) != null) {
                throw new IllegalArgumentException("a plan carries one link per id, got " + link.id()
                                                                                                .value() + " twice");
            }
        }
        return new Plan(nodesById,
                        linksById,
                        nextValue(nodesById.keySet().stream().mapToLong(NodeId::value)),
                        nextValue(linksById.keySet().stream().mapToLong(LinkId::value)));
    }

    private static long nextValue(LongStream used) {
        return used.max().orElse(-1) + 1;
    }

    private static Map<Port, LinkId> indexByPort(Map<NodeId, Node> nodes, Map<LinkId, Link> links) {
        Map<Port, LinkId> index = new HashMap<>();
        for (Link link : links.values()) {
            requireExposed(nodes, link.from());
            requireExposed(nodes, link.to());
            claim(index, link.from(), link.id());
            claim(index, link.to(), link.id());
        }
        return Map.copyOf(index);
    }

    private static void requireExposed(Map<NodeId, Node> nodes, Port port) {
        Node owner = nodes.get(port.owner());
        if (owner == null) {
            throw new IllegalArgumentException("a link points at a missing node, " + port.owner().value());
        }
        if (!owner.exposes(port)) {
            throw new IllegalArgumentException("a link points at a port its node does not expose, index " + port.index() + " " + port.direction() + " on " + port.owner()
                                                                                                                                                                 .value());
        }
    }

    private static void claim(Map<Port, LinkId> index, Port port, LinkId link) {
        LinkId taken = index.put(port, link);
        if (taken != null) {
            throw new IllegalArgumentException("a port carries at most one link, port " + port.index() + " " + port.direction() + " on " + port.owner()
                                                                                                                                               .value() + " is claimed twice");
        }
    }

    public Map<NodeId, Node> nodes() {
        return nodes;
    }

    public Map<LinkId, Link> links() {
        return links;
    }

    public Optional<Node> node(NodeId id) {
        return Optional.ofNullable(nodes.get(id));
    }

    public Optional<Link> linkAt(Port port) {
        return Optional.ofNullable(linksByPort.get(port)).map(links::get);
    }

    public boolean isFree(Port port) {
        return !linksByPort.containsKey(port);
    }

    public List<Link> linksOf(NodeId id) {
        return links.values()
                    .stream()
                    .filter(link -> link.from().owner().equals(id) || link.to().owner().equals(id))
                    .toList();
    }

    public NodeId nextNodeId() {
        return new NodeId(nextNodeValue);
    }

    public LinkId nextLinkId() {
        return new LinkId(nextLinkValue);
    }

    public Plan withNode(Node node) {
        Map<NodeId, Node> replaced = new LinkedHashMap<>(nodes);
        replaced.put(node.id(), node);
        return new Plan(replaced, links, Math.max(nextNodeValue, node.id().value() + 1), nextLinkValue);
    }

    public Plan withoutNode(NodeId id) {
        if (!nodes.containsKey(id)) {
            throw new IllegalArgumentException("a plan cannot drop a node it does not carry, " + id.value());
        }
        if (!linksOf(id).isEmpty()) {
            throw new IllegalArgumentException("a node still carrying links is disconnected before it is dropped, " + id.value());
        }
        Map<NodeId, Node> remaining = new LinkedHashMap<>(nodes);
        remaining.remove(id);
        return new Plan(remaining, links, nextNodeValue, nextLinkValue);
    }

    public Plan withLink(Link link) {
        if (links.containsKey(link.id())) {
            throw new IllegalArgumentException("a plan carries one link per id, " + link.id()
                                                                                        .value() + " already exists");
        }
        Map<LinkId, Link> added = new LinkedHashMap<>(links);
        added.put(link.id(), link);
        return new Plan(nodes, added, nextNodeValue, Math.max(nextLinkValue, link.id().value() + 1));
    }

    public Plan withoutLink(LinkId id) {
        if (!links.containsKey(id)) {
            throw new IllegalArgumentException("a plan cannot drop a link it does not carry, " + id.value());
        }
        Map<LinkId, Link> remaining = new LinkedHashMap<>(links);
        remaining.remove(id);
        return new Plan(nodes, remaining, nextNodeValue, nextLinkValue);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Plan plan && nodes.equals(plan.nodes) && links.equals(plan.links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes, links);
    }

    @Override
    public String toString() {
        return "Plan[" + nodes.size() + " nodes, " + links.size() + " links]";
    }
}