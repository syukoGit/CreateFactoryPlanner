package fr.syuko.createfactoryplanner.core.engine;

import fr.syuko.createfactoryplanner.core.machine.MachineCatalog;
import fr.syuko.createfactoryplanner.core.machine.MachineProfile;
import fr.syuko.createfactoryplanner.core.machine.ThroughputModel;
import fr.syuko.createfactoryplanner.core.model.MachineId;
import fr.syuko.createfactoryplanner.core.model.RecipeId;
import fr.syuko.createfactoryplanner.core.recipe.RecipeCatalog;
import fr.syuko.createfactoryplanner.core.recipe.RecipeDto;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SolverContextTest {

    private static final MachineId PRESS = new MachineId("mechanical_press");

    private static final MachineId SAW = new MachineId("mechanical_saw");

    private static final RecipeId PRESSING = new RecipeId("create:pressing/iron_ingot");

    private static SolverContext context() {
        MachineProfile press = new MachineProfile(PRESS, 0, 256, 128, 8, Map.of());
        RecipeDto pressing = new RecipeDto(PRESSING, List.of(), List.of(), List.of(), 240, null);
        return new SolverContext(new MapMachineCatalog(Map.of(PRESS, press), Map.of()),
                                 new MapRecipeCatalog(Map.of(PRESSING, pressing)));
    }

    @Test
    void resolvesWhatThePlanOnlyReferencesByIdentifier() {
        SolverContext context = context();
        assertEquals(256, context.machines().profile(PRESS).orElseThrow().maximumRpm());
        assertEquals(240, context.recipes().recipe(PRESSING).orElseThrow().declaredDurationTicks());
    }

    @Test
    void reportsAMissingMachineWithoutThrowing() {
        SolverContext context = context();
        assertTrue(context.machines().profile(SAW).isEmpty());
        assertTrue(context.machines().model(SAW).isEmpty());
    }

    @Test
    void reportsAMachineWithoutAThroughputModelWithoutThrowing() {
        SolverContext context = context();
        assertTrue(context.machines().profile(PRESS).isPresent());
        assertTrue(context.machines().model(PRESS).isEmpty());
    }

    @Test
    void reportsARecipeMissingFromTheModpackWithoutThrowing() {
        assertTrue(context().recipes().recipe(new RecipeId("create:pressing/gone")).isEmpty());
    }

    @Test
    void refusesToBeBuiltWithoutBothCatalogs() {
        RecipeCatalog recipes = new MapRecipeCatalog(Map.of());
        MachineCatalog machines = new MapMachineCatalog(Map.of(), Map.of());
        assertThrows(IllegalArgumentException.class, () -> new SolverContext(null, recipes));
        assertThrows(IllegalArgumentException.class, () -> new SolverContext(machines, null));
    }

    private record MapMachineCatalog(Map<MachineId, MachineProfile> profiles, Map<MachineId, ThroughputModel> models)
            implements MachineCatalog {

        @Override
        public Optional<MachineProfile> profile(MachineId machine) {
            return Optional.ofNullable(profiles.get(machine));
        }

        @Override
        public Optional<ThroughputModel> model(MachineId machine) {
            return Optional.ofNullable(models.get(machine));
        }
    }

    private record MapRecipeCatalog(Map<RecipeId, RecipeDto> recipes) implements RecipeCatalog {

        @Override
        public Optional<RecipeDto> recipe(RecipeId id) {
            return Optional.ofNullable(recipes.get(id));
        }
    }
}