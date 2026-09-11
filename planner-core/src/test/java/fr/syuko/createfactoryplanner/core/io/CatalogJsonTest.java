package fr.syuko.createfactoryplanner.core.io;

import fr.syuko.createfactoryplanner.core.machine.MachineProfile;
import fr.syuko.createfactoryplanner.core.math.Rate;
import fr.syuko.createfactoryplanner.core.model.MachineId;
import fr.syuko.createfactoryplanner.core.model.ResourceId;
import fr.syuko.createfactoryplanner.core.model.ResourceKind;
import fr.syuko.createfactoryplanner.core.recipe.RecipeDto;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CatalogJsonTest {

    private static final MachineId MIXER = new MachineId("mechanical_mixer");

    private static final MachineId SPOUT = new MachineId("spout");

    private static Catalog catalog() {
        return new Catalog(new CatalogMeta("2026-09-11T12:00:00Z", "minecraft:overworld", "0.1.0", "6.0.11-295", 2, 0),
                           List.of(new MachineEntry("mechanical_mixer", 30, 256, 128, 4.0, Map.of()),
                                   new MachineEntry("spout", 0, 256, 128, null, Map.of("filling_time", 20L))),
                           List.of());
    }

    @Test
    void survivesARoundTripThroughJson() {
        Catalog rebuilt = CatalogJson.read(CatalogJson.write(catalog()));
        assertEquals(catalog(), rebuilt);
        assertEquals("6.0.11-295", rebuilt.meta().createVersion());
        assertEquals(20, rebuilt.machine(SPOUT).orElseThrow().formulaScalars().get("filling_time"));
    }

    @Test
    void tellsAnUnreadStressImpactApartFromAZeroOne() {
        Catalog read = CatalogJson.read(CatalogJson.write(catalog()));
        assertTrue(read.machine(MIXER).orElseThrow().readsItsStressImpact());
        assertFalse(read.machine(SPOUT).orElseThrow().readsItsStressImpact());

        MachineEntry registeredWithoutImpact = new MachineEntry("item_drain", 0, 256, 128, 0.0, Map.of());
        assertTrue(registeredWithoutImpact.readsItsStressImpact());
        assertEquals(0, registeredWithoutImpact.toProfile().orElseThrow().stressImpactPerRpm());
    }

    @Test
    void buildsAProfileOnlyForAMachineWhoseImpactWasRead() {
        Catalog read = catalog();
        Map<MachineId, MachineProfile> profiles = read.profiles();
        assertEquals(1, profiles.size());
        assertEquals(30, profiles.get(MIXER).minimumRpm());
        assertTrue(read.machine(SPOUT).orElseThrow().toProfile().isEmpty());
    }

    @Test
    void carriesAFractionalAmountThroughJsonWithoutLosingItsRational() {
        RecipeEntry crushing = new RecipeEntry("create:crushing/obsidian",
                                               List.of("crushing_wheels"),
                                               400,
                                               List.of(new IngredientEntry(new ResourceEntry("minecraft:obsidian",
                                                                                             ResourceKind.ITEM),
                                                                           Rate.ratio(1, 4).toString(),
                                                                           List.of("minecraft:obsidian"))),
                                               List.of(new OutputEntry(new ResourceEntry("create:powdered_obsidian",
                                                                                         ResourceKind.ITEM),
                                                                       1,
                                                                       Rate.ratio(5, 4).toString())),
                                               List.of());
        Catalog written = new Catalog(catalog().meta(), List.of(), List.of(crushing));
        RecipeDto rebuilt = CatalogJson.read(CatalogJson.write(written))
                                       .recipe("create:crushing/obsidian")
                                       .orElseThrow()
                                       .toDto();
        assertEquals(Rate.ratio(1, 4),
                     rebuilt.ingredient(ResourceId.item("minecraft:obsidian")).orElseThrow().amountPerOperation());
        assertEquals(Rate.ratio(5, 4),
                     rebuilt.output(ResourceId.item("create:powdered_obsidian")).orElseThrow().expectedPerOperation());
    }

    @Test
    void refusesAJsonThatCarriesNoCatalog() {
        assertThrows(IllegalArgumentException.class, () -> CatalogJson.read("{}"));
        assertThrows(IllegalArgumentException.class, () -> CatalogJson.read("not json"));
    }
}