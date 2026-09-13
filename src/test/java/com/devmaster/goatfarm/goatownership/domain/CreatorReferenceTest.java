package com.devmaster.goatfarm.goatownership.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CreatorReferenceTest {
    private static final Instant RECORDED_AT = Instant.parse("2026-09-13T10:00:00Z");

    @Test
    void farmCreatorRetainsTodAndFarmLink() {
        CreatorReference creator = CreatorReference.farm(
                "12345", 7L, "Capril Vilar", CreatorSource.BIRTH, "birth-record", RECORDED_AT);

        assertEquals("12345", creator.creatorTod());
        assertEquals(7L, creator.creatorFarmId());
        assertTrue(creator.isFarmLinked());
        assertEquals("CAPRILVILAR", creator.creatorNameSnapshot());
    }

    @Test
    void externalCreatorMayKeepTodWithoutAnInternalFarm() {
        CreatorReference creator = CreatorReference.external(
                "55555", "External breeder", CreatorSource.ABCC, "abcc-42", RECORDED_AT);

        assertEquals("55555", creator.creatorTod());
        assertNull(creator.creatorFarmId());
        assertFalse(creator.isFarmLinked());
    }

    @Test
    void unknownCreatorCannotPretendToHaveFarmOrTod() {
        assertThrows(IllegalArgumentException.class,
                () -> new CreatorReference("55555", null, null, CreatorSource.UNKNOWN, null, RECORDED_AT));
        assertDoesNotThrow(() -> CreatorReference.unknown(RECORDED_AT));
    }

    @Test
    void farmCreatorRequiresTodBecauseFarmTodIsTheRegistralOrigin() {
        assertThrows(IllegalArgumentException.class,
                () -> CreatorReference.farm(null, 7L, null, CreatorSource.BIRTH, null, RECORDED_AT));
    }
}
