package fr.syuko.createfactoryplanner.core.machine;

import fr.syuko.createfactoryplanner.core.model.MachineId;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ParamDescriptorTest {

    private static final MachineId MIXER = new MachineId("mechanical_mixer");

    @Test
    void readsTheRotationRangeFromTheProfileRatherThanFromALiteral() {
        MachineProfile lowered = new MachineProfile(MIXER, 30, 64, 64, 4, Map.of());
        ParamDescriptor rpm = ParamDescriptor.rotationSpeed(lowered);
        assertEquals(ParamDescriptor.RPM, rpm.id());
        assertEquals(ParamType.ROTATION_SPEED, rpm.type());
        assertEquals(30, rpm.min());
        assertEquals(64, rpm.max());
        assertEquals(64, rpm.defaultValue());
        assertTrue(rpm.affectsThroughput());
    }

    @Test
    void raisesTheFloorToOneWhenTheMachineHasNoMinimum() {
        MachineProfile millstone = new MachineProfile(new MachineId("millstone"), 0, 256, 128, 4, Map.of());
        assertEquals(1, ParamDescriptor.rotationSpeed(millstone).min());
    }

    @Test
    void carriesTheSuOnlyFlagAsDataRatherThanAsACondition() {
        ParamDescriptor fanRpm = new ParamDescriptor(ParamDescriptor.RPM, ParamType.ROTATION_SPEED, 1, 256, 128, false);
        ParamDescriptor fanCount = new ParamDescriptor(ParamDescriptor.FAN_COUNT, ParamType.COUNT, 1, 64, 1, true);
        assertFalse(fanRpm.affectsThroughput());
        assertTrue(fanCount.affectsThroughput());
    }

    @Test
    void boundsTheValuesItIsGiven() {
        ParamDescriptor fanCount = new ParamDescriptor(ParamDescriptor.FAN_COUNT, ParamType.COUNT, 1, 64, 1, true);
        assertEquals(1, fanCount.clamp(0));
        assertEquals(64, fanCount.clamp(1000));
        assertEquals(6, fanCount.clamp(6));
        assertTrue(fanCount.accepts(6));
        assertFalse(fanCount.accepts(0));
        assertFalse(fanCount.accepts(65));
    }

    @Test
    void refusesADescriptorItCouldNeverSatisfy() {
        assertThrows(IllegalArgumentException.class, () -> new ParamDescriptor(" ", ParamType.COUNT, 1, 2, 1, true));
        assertThrows(IllegalArgumentException.class, () -> new ParamDescriptor("rpm", null, 1, 2, 1, true));
        assertThrows(IllegalArgumentException.class, () -> new ParamDescriptor("rpm", ParamType.COUNT, 5, 2, 3, true));
        assertThrows(IllegalArgumentException.class,
                     () -> new ParamDescriptor("rpm", ParamType.COUNT, 1, 10, 20, true));
        assertThrows(IllegalArgumentException.class,
                     () -> new ParamDescriptor("rpm", ParamType.COUNT, -1, 10, 1, true));
    }
}