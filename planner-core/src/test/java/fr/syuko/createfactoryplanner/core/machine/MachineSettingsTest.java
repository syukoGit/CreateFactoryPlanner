package fr.syuko.createfactoryplanner.core.machine;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MachineSettingsTest {

    @Test
    void fallsBackWhenAParameterWasNeverSet() {
        MachineSettings settings = MachineSettings.of("rpm", 64);
        assertEquals(64, settings.value("rpm", 128));
        assertEquals(1, settings.value("fan_count", 1));
        assertEquals(128, MachineSettings.NONE.value("rpm", 128));
    }

    @Test
    void staysImmutableWhenAParameterChanges() {
        MachineSettings settings = MachineSettings.of("rpm", 64);
        MachineSettings faster = settings.with("rpm", 128);
        assertEquals(64, settings.value("rpm", 0));
        assertEquals(128, faster.value("rpm", 0));
        assertEquals(6, settings.with("fan_count", 6).value("fan_count", 0));
    }

    @Test
    void copiesTheMapItIsGiven() {
        Map<String, Integer> mutable = new HashMap<>(Map.of("rpm", 64));
        MachineSettings settings = new MachineSettings(mutable);
        mutable.put("rpm", 256);
        assertEquals(64, settings.value("rpm", 0));
        assertThrows(UnsupportedOperationException.class, () -> settings.values().put("rpm", 256));
    }
}
