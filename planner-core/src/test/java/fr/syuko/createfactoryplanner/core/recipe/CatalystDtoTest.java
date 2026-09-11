package fr.syuko.createfactoryplanner.core.recipe;

import fr.syuko.createfactoryplanner.core.model.ResourceId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CatalystDtoTest {

    @Test
    void countsOnePrimingAmountPerMachine() {
        CatalystDto sandPaper = new CatalystDto(ResourceId.item("create:sand_paper"), 1);
        assertEquals("create:sand_paper", sandPaper.resource().value());
        assertEquals(1, sandPaper.amountPerMachine());
    }

    @Test
    void refusesAnEmptyPrimingAmount() {
        ResourceId sandPaper = ResourceId.item("create:sand_paper");
        assertThrows(IllegalArgumentException.class, () -> new CatalystDto(sandPaper, 0));
    }
}
