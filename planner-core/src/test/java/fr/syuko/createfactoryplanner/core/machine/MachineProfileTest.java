package fr.syuko.createfactoryplanner.core.machine;

import fr.syuko.createfactoryplanner.core.model.MachineId;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MachineProfileTest {

    private static final MachineId MIXER = new MachineId("mechanical_mixer");

    @Test
    void carriesTheSpeedsReadFromTheGame() {
        MachineProfile mixer = new MachineProfile(MIXER, 30, 256, 128, 4, Map.of("cycle", 512L));
        assertEquals(30, mixer.minimumRpm());
        assertEquals(256, mixer.maximumRpm());
        assertEquals(512, mixer.scalar("cycle", 0));
        assertEquals(240, mixer.scalar("unknown", 240));
    }

    @Test
    void acceptsACeilingLoweredByTheModpack() {
        MachineProfile lowered = new MachineProfile(MIXER, 30, 64, 64, 4, Map.of());
        assertEquals(64, lowered.maximumRpm());
    }

    @Test
    void acceptsAMachineWithoutAMinimumSpeed() {
        MachineProfile millstone = new MachineProfile(new MachineId("millstone"), 0, 256, 128, 4, Map.of());
        assertEquals(0, millstone.minimumRpm());
    }

    @Test
    void refusesADefaultSpeedOutsideItsOwnRange() {
        assertThrows(IllegalArgumentException.class, () -> new MachineProfile(MIXER, 30, 256, 16, 4, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new MachineProfile(MIXER, 30, 256, 300, 4, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new MachineProfile(MIXER, 300, 256, 128, 4, Map.of()));
    }

    @Test
    void refusesAnUnusableStressImpact() {
        assertThrows(IllegalArgumentException.class, () -> new MachineProfile(MIXER, 30, 256, 128, -1, Map.of()));
        assertThrows(IllegalArgumentException.class,
                     () -> new MachineProfile(MIXER, 30, 256, 128, Double.NaN, Map.of()));
    }
}