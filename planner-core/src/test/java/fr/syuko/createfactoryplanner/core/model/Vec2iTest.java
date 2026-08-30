package fr.syuko.createfactoryplanner.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Vec2iTest {

    @Test
    void keepsItsCoordinates() {
        Vec2i position = new Vec2i(120, -80);
        assertEquals(120, position.x());
        assertEquals(-80, position.y());
        assertEquals(new Vec2i(0, 0), Vec2i.ORIGIN);
    }

    @Test
    void translatesBothWays() {
        Vec2i position = new Vec2i(120, 80);
        Vec2i offset = new Vec2i(-20, 5);
        assertEquals(new Vec2i(100, 85), position.plus(offset));
        assertEquals(position, position.plus(offset).minus(offset));
        assertEquals(position, position.plus(Vec2i.ORIGIN));
    }

    @Test
    void refusesToWrapAroundOnTranslation() {
        Vec2i extreme = new Vec2i(Integer.MAX_VALUE, 0);
        assertThrows(ArithmeticException.class, () -> extreme.plus(new Vec2i(1, 0)));
    }
}