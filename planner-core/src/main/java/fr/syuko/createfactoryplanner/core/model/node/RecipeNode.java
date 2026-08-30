package fr.syuko.createfactoryplanner.core.model.node;

import fr.syuko.createfactoryplanner.core.machine.MachineSettings;
import fr.syuko.createfactoryplanner.core.model.*;
import fr.syuko.createfactoryplanner.core.recipe.IngredientDto;
import fr.syuko.createfactoryplanner.core.recipe.OutputDto;
import fr.syuko.createfactoryplanner.core.recipe.RecipeDto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public record RecipeNode(NodeId id, RecipeId recipe, MachineId machine, MachineSettings settings,
                         List<ResourceId> inputResources, List<ResourceId> outputResources, Vec2i position)
        implements Node {

    public RecipeNode {
        if (id == null) {
            throw new IllegalArgumentException("a node must carry an id");
        }
        if (recipe == null) {
            throw new IllegalArgumentException("a recipe node must carry a recipe");
        }
        if (machine == null) {
            throw new IllegalArgumentException("a recipe node must carry a machine");
        }
        if (settings == null) {
            throw new IllegalArgumentException("a recipe node must carry its settings");
        }
        if (position == null) {
            throw new IllegalArgumentException("a node must carry a position");
        }
        inputResources = List.copyOf(inputResources);
        outputResources = List.copyOf(outputResources);
        requireDistinct(inputResources, "input", recipe);
        requireDistinct(outputResources, "output", recipe);
    }

    private static void requireDistinct(List<ResourceId> resources, String side, RecipeId recipe) {
        Set<ResourceId> seen = new HashSet<>();
        for (ResourceId resource : resources) {
            if (!seen.add(resource)) {
                throw new IllegalArgumentException("a recipe node exposes one " + side + " port per resource, got " + resource.value() + " twice on " + recipe.value());
            }
        }
    }

    public static RecipeNode forRecipe(NodeId id,
                                       RecipeDto recipe,
                                       MachineId machine,
                                       MachineSettings settings,
                                       Vec2i position) {
        return new RecipeNode(id,
                              recipe.id(),
                              machine,
                              settings,
                              recipe.ingredients().stream().map(IngredientDto::resource).toList(),
                              recipe.outputs().stream().map(OutputDto::resource).toList(),
                              position);
    }

    @Override
    public NodeFamily family() {
        return NodeFamily.PROCESSING;
    }

    @Override
    public List<Port> inputs() {
        return portsOf(inputResources, PortDirection.INPUT);
    }

    @Override
    public List<Port> outputs() {
        return portsOf(outputResources, PortDirection.OUTPUT);
    }

    @Override
    public RecipeNode movedTo(Vec2i target) {
        return new RecipeNode(id, recipe, machine, settings, inputResources, outputResources, target);
    }

    public RecipeNode withSettings(MachineSettings replacement) {
        return new RecipeNode(id, recipe, machine, replacement, inputResources, outputResources, position);
    }

    private List<Port> portsOf(List<ResourceId> resources, PortDirection direction) {
        return IntStream.range(0, resources.size())
                        .mapToObj(index -> new Port(id, index, direction, resources.get(index)))
                        .toList();
    }
}