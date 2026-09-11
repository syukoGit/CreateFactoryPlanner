package fr.syuko.createfactoryplanner.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecipeIdTest {

    @Test
    void keepsItsValue() {
        assertEquals("create:crushing/iron_ore", new RecipeId("create:crushing/iron_ore").value());
    }

    @Test
    void refusesBlankValues() {
        assertThrows(IllegalArgumentException.class, () -> new RecipeId(" "));
        assertThrows(IllegalArgumentException.class, () -> new RecipeId(null));
    }
}