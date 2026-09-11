package fr.syuko.createfactoryplanner.core.io;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MachineEntryProvenanceTest {

    private static MachineEntry mixer(Map<String, Provenance> provenance) {
        return new MachineEntry("mechanical_mixer", 30, 256, 64, 4.0, Map.of(), provenance);
    }

    @Test
    void saysWhereEveryValueComesFrom() {
        MachineEntry overridden = mixer(Map.of(MachineEntry.DEFAULT_RPM,
                                               Provenance.USER,
                                               MachineEntry.MINIMUM_RPM,
                                               Provenance.GAME));
        assertEquals(Provenance.USER, overridden.provenanceOf(MachineEntry.DEFAULT_RPM));
        assertEquals(Provenance.GAME, overridden.provenanceOf(MachineEntry.MINIMUM_RPM));
        assertTrue(overridden.carriesAUserOverride());
    }

    @Test
    void readsAsComingFromTheGameWhenNothingSaysOtherwise() {
        MachineEntry plain = mixer(Map.of());
        assertEquals(Provenance.GAME, plain.provenanceOf(MachineEntry.STRESS_IMPACT_PER_RPM));
        assertFalse(plain.carriesAUserOverride());
    }

    @Test
    void survivesAJsonRoundTripWithItsProvenance() {
        Catalog catalog = new Catalog(new CatalogMeta("2026-09-11T12:00:00Z", null, "0.1.0", "6.0.11", 1, 0),
                                      java.util.List.of(mixer(Map.of(MachineEntry.DEFAULT_RPM, Provenance.USER))),
                                      java.util.List.of(),
                                      Coverage.NONE);
        MachineEntry rebuilt = CatalogJson.read(CatalogJson.write(catalog)).machines().getFirst();
        assertEquals(Provenance.USER, rebuilt.provenanceOf(MachineEntry.DEFAULT_RPM));
        assertEquals(64, rebuilt.toProfile().orElseThrow().defaultRpm());
    }
}
