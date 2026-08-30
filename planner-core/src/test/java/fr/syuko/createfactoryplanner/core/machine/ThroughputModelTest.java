package fr.syuko.createfactoryplanner.core.machine;

import fr.syuko.createfactoryplanner.core.math.Rate;
import fr.syuko.createfactoryplanner.core.model.MachineId;
import fr.syuko.createfactoryplanner.core.model.RecipeId;
import fr.syuko.createfactoryplanner.core.recipe.RecipeDto;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThroughputModelTest {

    private static final MachineProfile PRESS = new MachineProfile(new MachineId("mechanical_press"),
                                                                   0,
                                                                   256,
                                                                   128,
                                                                   8,
                                                                   Map.of());

    private static final RecipeDto RECIPE = new RecipeDto(new RecipeId("create:pressing/iron_ingot"),
                                                          List.of(),
                                                          List.of(),
                                                          List.of(),
                                                          240,
                                                          null);

    @Test
    void readsItsParametersFromTheSettingsAndFallsBackOnTheProfile() {
        ThroughputModel model = new SpeedProportionalThroughput();
        assertEquals(Rate.perTick(128, 240), model.operationsPerTick(RECIPE, MachineSettings.NONE, PRESS));
        assertEquals(Rate.perTick(64, 240),
                     model.operationsPerTick(RECIPE, MachineSettings.of(ParamDescriptor.RPM, 64), PRESS));
    }

    @Test
    void derivesItsParameterRangeFromTheProfileItIsGiven() {
        List<ParamDescriptor> parameters = new SpeedProportionalThroughput().parameters(PRESS);
        assertEquals(1, parameters.size());
        assertEquals(256, parameters.getFirst().max());
        assertEquals(64,
                     new SpeedProportionalThroughput().parameters(new MachineProfile(new MachineId("mechanical_press"),
                                                                                     0,
                                                                                     64,
                                                                                     64,
                                                                                     8,
                                                                                     Map.of())).getFirst().max());
    }

    @Test
    void reportsStressAsAnInformativeScalar() {
        assertEquals(1024, new SpeedProportionalThroughput().stressUnits(MachineSettings.NONE, PRESS));
        assertEquals(512,
                     new SpeedProportionalThroughput().stressUnits(MachineSettings.of(ParamDescriptor.RPM, 64), PRESS));
    }

    @Test
    void assumesNothingUnlessTheModelSaysOtherwise() {
        assertTrue(new SpeedProportionalThroughput().assumptions().isEmpty());
        assertEquals(List.of(Assumption.of("createfactoryplanner.assumption.blocking_filter")),
                     new FilteredLineThroughput().assumptions());
    }

    private record SpeedProportionalThroughput() implements ThroughputModel {

        @Override
        public Rate operationsPerTick(RecipeDto recipe, MachineSettings settings, MachineProfile profile) {
            return Rate.perTick(settings.value(ParamDescriptor.RPM, profile.defaultRpm()),
                                recipe.declaredDurationTicks());
        }

        @Override
        public List<ParamDescriptor> parameters(MachineProfile profile) {
            return List.of(ParamDescriptor.rotationSpeed(profile));
        }

        @Override
        public double stressUnits(MachineSettings settings, MachineProfile profile) {
            return profile.stressImpactPerRpm() * settings.value(ParamDescriptor.RPM, profile.defaultRpm());
        }
    }

    private record FilteredLineThroughput() implements ThroughputModel {

        @Override
        public Rate operationsPerTick(RecipeDto recipe, MachineSettings settings, MachineProfile profile) {
            return Rate.ZERO;
        }

        @Override
        public List<ParamDescriptor> parameters(MachineProfile profile) {
            return List.of();
        }

        @Override
        public double stressUnits(MachineSettings settings, MachineProfile profile) {
            return 0;
        }

        @Override
        public List<Assumption> assumptions() {
            return List.of(Assumption.of("createfactoryplanner.assumption.blocking_filter"));
        }
    }
}