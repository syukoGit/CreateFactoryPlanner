package fr.syuko.createfactoryplanner.core.cli;

import fr.syuko.createfactoryplanner.core.io.Catalog;
import fr.syuko.createfactoryplanner.core.io.CatalogMeta;
import fr.syuko.createfactoryplanner.core.io.MachineEntry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PlannerCliTest {

    private static Catalog catalog() {
        return new Catalog(new CatalogMeta("2026-09-11T12:00:00Z", "minecraft:overworld", "0.1.0", "6.0.11-295", 2),
                           List.of(new MachineEntry("mechanical_mixer", 30, 256, 128, 4.0, Map.of()),
                                   new MachineEntry("encased_fan",
                                                    0,
                                                    256,
                                                    128,
                                                    2.0,
                                                    Map.of("fan_processing_time", 150L))));
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
        Catalog unread = new Catalog(catalog().meta(), List.of(new MachineEntry("spout", 0, 256, 128, null, Map.of())));
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
    void refusesACommandItDoesNotKnow() {
        assertThrows(IllegalArgumentException.class, () -> PlannerCli.run(List.of()));
        assertThrows(IllegalArgumentException.class, () -> PlannerCli.run(List.of("recipes", "--catalog", "x")));
        assertThrows(IllegalArgumentException.class, () -> PlannerCli.run(List.of("machines")));
    }
}