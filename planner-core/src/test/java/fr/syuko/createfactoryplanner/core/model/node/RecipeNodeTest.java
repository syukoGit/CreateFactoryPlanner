package fr.syuko.createfactoryplanner.core.model.node;

import fr.syuko.createfactoryplanner.core.machine.MachineSettings;
import fr.syuko.createfactoryplanner.core.machine.ParamDescriptor;
import fr.syuko.createfactoryplanner.core.math.Rate;
import fr.syuko.createfactoryplanner.core.model.*;
import fr.syuko.createfactoryplanner.core.recipe.IngredientDto;
import fr.syuko.createfactoryplanner.core.recipe.OutputDto;
import fr.syuko.createfactoryplanner.core.recipe.RecipeDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecipeNodeTest {

    private static final NodeId ID = new NodeId(3);

    private static final MachineId DEPLOYER = new MachineId("deployer");

    private static final ResourceId ORE = ResourceId.item("minecraft:iron_ore");

    private static final ResourceId WATER = ResourceId.fluid("minecraft:water");

    private static final ResourceId POWDER = ResourceId.item("create:powdered_obsidian");

    private static RecipeDto polishing() {
        return new RecipeDto(new RecipeId("create:deploying/polished"),
                             List.of(new IngredientDto(ORE, 1), new IngredientDto(WATER, 200)),
                             List.of(new OutputDto(POWDER, 1, Rate.ratio(5, 4))),
                             0);
    }

    @Test
    void exposesOnePortPerNormalizedResourceOnEachSide() {
        RecipeNode node = RecipeNode.forRecipe(ID, polishing(), DEPLOYER, MachineSettings.NONE, Vec2i.ORIGIN);
        assertEquals(NodeFamily.PROCESSING, node.family());
        assertEquals(List.of(ORE, WATER), node.inputResources());
        assertEquals(List.of(POWDER), node.outputResources());
        assertEquals(2, node.inputs().size());
        assertEquals(1, node.outputs().size());
        assertEquals(WATER, node.inputs().get(1).resource());
        assertEquals(1, node.inputs().get(1).index());
    }

    @Test
    void refusesTwoPortsForTheSameResourceOnOneSide() {
        List<ResourceId> twice = List.of(ORE, ORE);
        assertThrows(IllegalArgumentException.class,
                     () -> new RecipeNode(ID,
                                          new RecipeId("create:crushing/iron"),
                                          DEPLOYER,
                                          MachineSettings.NONE,
                                          twice,
                                          List.of(),
                                          Vec2i.ORIGIN));
    }

    @Test
    void acceptsTheSameResourceOnBothSides() {
        RecipeNode node = new RecipeNode(ID,
                                         new RecipeId("create:crushing/iron"),
                                         DEPLOYER,
                                         MachineSettings.NONE,
                                         List.of(ORE),
                                         List.of(ORE),
                                         Vec2i.ORIGIN);
        assertEquals(1, node.inputs().size());
        assertEquals(1, node.outputs().size());
        assertNotEquals(node.inputs().getFirst(), node.outputs().getFirst());
    }

    @Test
    void keepsItsPortsWhenOnlyItsSettingsChange() {
        RecipeNode node = RecipeNode.forRecipe(ID, polishing(), DEPLOYER, MachineSettings.NONE, Vec2i.ORIGIN);
        RecipeNode faster = node.withSettings(MachineSettings.of(ParamDescriptor.RPM, 64));
        assertEquals(node.inputs(), faster.inputs());
        assertEquals(node.outputs(), faster.outputs());
        assertEquals(64, faster.settings().value(ParamDescriptor.RPM, 128));
    }

    @Test
    void recognisesOnlyItsOwnPorts() {
        RecipeNode node = RecipeNode.forRecipe(ID, polishing(), DEPLOYER, MachineSettings.NONE, Vec2i.ORIGIN);
        assertTrue(node.exposes(node.inputs().getFirst()));
        assertFalse(node.exposes(new Port(ID, 9, PortDirection.INPUT, ORE)));
        assertFalse(node.exposes(new Port(ID, 0, PortDirection.INPUT, POWDER)));
        assertFalse(node.exposes(new Port(new NodeId(99), 0, PortDirection.INPUT, ORE)));
    }
}