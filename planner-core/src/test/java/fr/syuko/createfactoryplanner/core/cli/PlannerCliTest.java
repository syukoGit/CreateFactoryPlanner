package fr.syuko.createfactoryplanner.core.cli;

import fr.syuko.createfactoryplanner.core.io.*;
import fr.syuko.createfactoryplanner.core.math.Rate;
import fr.syuko.createfactoryplanner.core.model.ResourceKind;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PlannerCliTest {

    private static Catalog catalog() {
        return new Catalog(new CatalogMeta("2026-09-11T12:00:00Z", "minecraft:overworld", "0.1.0", "6.0.11-295", 2, 0),
                           List.of(new MachineEntry("mechanical_mixer", 30, 256, 128, 4.0, Map.of()),
                                   new MachineEntry("encased_fan",
                                                    0,
                                                    256,
                                                    128,
                                                    2.0,
                                                    Map.of("fan_processing_time", 150L))),
                           List.of());
    }

    @Test
    void listsEveryMachineWithTheRangeItWasRead() {
        String report = PlannerCli.machines(catalog());
        assertTrue(report.contains("2 machines read at 2026-09-11T12:00:00Z"));
        assertTrue(report.contains("mechanical_mixer"));
        assertTrue(report.contains("30..256"));
        assertTrue(report.contains("fan_processing_time=150"));
    }

    @Test
    void saysWhenAStressImpactCouldNotBeRead() {
        Catalog unread = new Catalog(catalog().meta(),
                                     List.of(new MachineEntry("spout", 0, 256, 128, null, Map.of())),
                                     List.of());
        assertTrue(PlannerCli.machines(unread).contains("su/rpm unread"));
        assertFalse(PlannerCli.machines(unread).contains("su/rpm 0.00"));
    }

    @Test
    void saysWhereItLookedWhenTheCatalogIsMissing() {
        IllegalArgumentException missing = assertThrows(IllegalArgumentException.class,
                                                        () -> PlannerCli.run(List.of("machines",
                                                                                     "--catalog",
                                                                                     "nowhere/catalog.json")));
        assertTrue(missing.getMessage().contains("nowhere"));
        assertTrue(missing.getMessage().contains("/cfp dump catalog"));
    }

    @Test
    void printsARecipeWithItsGuaranteedFloorAndItsEquivalents() {
        RecipeEntry crushing = new RecipeEntry("create:crushing/iron_ore",
                                               List.of("crushing_wheels"),
                                               250,
                                               List.of(new IngredientEntry(new ResourceEntry("minecraft:iron_ore",
                                                                                             ResourceKind.ITEM),
                                                                           Rate.of(1).toString(),
                                                                           List.of("minecraft:iron_ore",
                                                                                   "minecraft:deepslate_iron_ore"))),
                                               List.of(new OutputEntry(new ResourceEntry("create:crushed_raw_iron",
                                                                                         ResourceKind.ITEM),
                                                                       1,
                                                                       Rate.ratio(7, 4).toString())),
                                               List.of());
        String report = PlannerCli.recipe(new Catalog(catalog().meta(), List.of(), List.of(crushing)),
                                          "create:crushing/iron_ore");
        assertTrue(report.contains("on crushing_wheels, 250 ticks"));
        assertTrue(report.contains("in   1 minecraft:iron_ore (+1 equivalents)"));
        assertTrue(report.contains("out  7/4 (1.7500) create:crushed_raw_iron (1 guaranteed)"));
    }

    @Test
    void dispatchesEveryCommandItAdvertises() {
        assertThrows(IllegalArgumentException.class, () -> PlannerCli.run(List.of("recipe", "--catalog", "x")));
        IllegalArgumentException missing = assertThrows(IllegalArgumentException.class,
                                                        () -> PlannerCli.run(List.of("recipe",
                                                                                     "create:crushing/iron_ore",
                                                                                     "--catalog",
                                                                                     "nowhere/catalog.json")));
        assertTrue(missing.getMessage().contains("nowhere"));
    }

    @Test
    void refusesACommandItDoesNotKnow() {
        assertThrows(IllegalArgumentException.class, () -> PlannerCli.run(List.of()));
        assertThrows(IllegalArgumentException.class, () -> PlannerCli.run(List.of("recipes", "--catalog", "x")));
        assertThrows(IllegalArgumentException.class, () -> PlannerCli.run(List.of("machines")));
    }
}