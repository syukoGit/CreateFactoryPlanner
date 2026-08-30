package fr.syuko.createfactoryplanner.core.recipe;

import fr.syuko.createfactoryplanner.core.model.MachineId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SequenceProfileTest {

    private static final MachineId DEPLOYER = new MachineId("deployer");

    private static final MachineId PRESS = new MachineId("mechanical_press");

    @Test
    void countsThePassesOfEachDistinctMachine() {
        SequenceProfile profile = new SequenceProfile(List.of(new SequenceStep(DEPLOYER, 7, 0),
                                                              new SequenceStep(PRESS, 3, 0)));
        assertEquals(2, profile.steps().size());
        assertEquals(10, profile.totalPasses());
    }

    @Test
    void refusesTheSameMachineTwiceBecauseOnlyOneIsInstalled() {
        List<SequenceStep> repeated = List.of(new SequenceStep(DEPLOYER, 3, 0), new SequenceStep(DEPLOYER, 4, 0));
        assertThrows(IllegalArgumentException.class, () -> new SequenceProfile(repeated));
    }

    @Test
    void refusesAnEmptySequence() {
        List<SequenceStep> none = List.of();
        assertThrows(IllegalArgumentException.class, () -> new SequenceProfile(none));
    }

    @Test
    void tellsWhetherAStepDeclaresItsOwnDuration() {
        assertTrue(new SequenceStep(PRESS, 1, 240).declaresItsDuration());
        assertFalse(new SequenceStep(PRESS, 1, 0).declaresItsDuration());
    }

    @Test
    void refusesAStepThatIsNeverWalked() {
        assertThrows(IllegalArgumentException.class, () -> new SequenceStep(PRESS, 0, 240));
        assertThrows(IllegalArgumentException.class, () -> new SequenceStep(PRESS, 1, -1));
    }
}