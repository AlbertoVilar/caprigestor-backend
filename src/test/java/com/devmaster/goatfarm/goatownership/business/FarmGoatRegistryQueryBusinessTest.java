package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryDisposition;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryItem;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryRole;
import com.devmaster.goatfarm.goatownership.application.ports.out.FarmGoatRegistryQueryPort;
import com.devmaster.goatfarm.goatownership.domain.CreatorReference;
import com.devmaster.goatfarm.goatownership.domain.CreatorSource;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipExitType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FarmGoatRegistryQueryBusinessTest {

    private static final Instant T = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    @DisplayName("Case 1: Creator only - included in Farm Registry with role CREATOR and disposition NONE")
    void case01_creatorOnly() {
        var business = new FarmGoatRegistryQueryBusiness(farmId -> List.of(
                candidate(1, CreatorReference.farm("TOD_A", 1L, "Capril A", CreatorSource.BIRTH, null, T))
        ));

        List<FarmGoatRegistryItem> items = business.findForFarm(1L);

        assertThat(items).hasSize(1);
        FarmGoatRegistryItem item = items.getFirst();
        assertThat(item.roles()).containsExactly(FarmGoatRegistryRole.CREATOR);
        assertThat(item.disposition()).isEqualTo(FarmGoatRegistryDisposition.NONE);
        assertThat(item.creatorFarmId()).isEqualTo(1L);
        assertThat(item.creatorNameSnapshot()).isEqualTo("Capril A");
        assertThat(item.currentOwnerFarmId()).isNull();
    }

    @Test
    @DisplayName("Case 2: Current owner only - included with role CURRENT_OWNER and disposition CURRENT")
    void case02_currentOwnerOnly() {
        var business = new FarmGoatRegistryQueryBusiness(farmId -> List.of(
                candidate(1, null, open(1, 1L))
        ));

        List<FarmGoatRegistryItem> items = business.findForFarm(1L);

        assertThat(items).hasSize(1);
        FarmGoatRegistryItem item = items.getFirst();
        assertThat(item.roles()).containsExactly(FarmGoatRegistryRole.CURRENT_OWNER);
        assertThat(item.disposition()).isEqualTo(FarmGoatRegistryDisposition.CURRENT);
        assertThat(item.currentOwnerFarmId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Case 3: Creator + Current Owner - exactly one item with both roles and disposition CURRENT")
    void case03_creatorAndCurrentOwner() {
        var business = new FarmGoatRegistryQueryBusiness(farmId -> List.of(
                candidate(1, CreatorReference.farm("TOD_A", 1L, "Capril A", CreatorSource.BIRTH, null, T), open(1, 1L))
        ));

        List<FarmGoatRegistryItem> items = business.findForFarm(1L);

        assertThat(items).hasSize(1);
        FarmGoatRegistryItem item = items.getFirst();
        assertThat(item.roles()).containsExactlyInAnyOrder(FarmGoatRegistryRole.CREATOR, FarmGoatRegistryRole.CURRENT_OWNER);
        assertThat(item.disposition()).isEqualTo(FarmGoatRegistryDisposition.CURRENT);
        assertThat(item.currentOwnerFarmId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Case 4: A -> B internal transfer - Farm A is FORMER_OWNER / TRANSFERRED, Farm B is CURRENT_OWNER / CURRENT")
    void case04_internalTransfer_farmAFormerOwnerTransferred_farmBCurrentOwnerCurrent() {
        var candidate = candidate(1, null,
                closed(1, 1L, T, T.plusSeconds(10), OwnershipExitType.TRANSFER_OUT),
                open(2, 2L, T.plusSeconds(10)));

        var businessA = new FarmGoatRegistryQueryBusiness(farmId -> List.of(candidate));
        var businessB = new FarmGoatRegistryQueryBusiness(farmId -> List.of(candidate));

        List<FarmGoatRegistryItem> itemsA = businessA.findForFarm(1L);
        assertThat(itemsA).hasSize(1);
        assertThat(itemsA.getFirst().roles()).containsExactly(FarmGoatRegistryRole.FORMER_OWNER);
        assertThat(itemsA.getFirst().disposition()).isEqualTo(FarmGoatRegistryDisposition.TRANSFERRED);
        assertThat(itemsA.getFirst().currentOwnerFarmId()).isEqualTo(2L);

        List<FarmGoatRegistryItem> itemsB = businessB.findForFarm(2L);
        assertThat(itemsB).hasSize(1);
        assertThat(itemsB.getFirst().roles()).containsExactly(FarmGoatRegistryRole.CURRENT_OWNER);
        assertThat(itemsB.getFirst().disposition()).isEqualTo(FarmGoatRegistryDisposition.CURRENT);
        assertThat(itemsB.getFirst().currentOwnerFarmId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Case 5: A -> B -> A round-trip - Farm A has exactly one item with CURRENT_OWNER and disposition CURRENT")
    void case05_roundTripTransfer_farmAHasOneItemWithCurrentOwnerAndCurrent() {
        var candidate = candidate(1, null,
                closed(1, 1L, T, T.plusSeconds(10), OwnershipExitType.TRANSFER_OUT),
                closed(2, 2L, T.plusSeconds(10), T.plusSeconds(20), OwnershipExitType.TRANSFER_OUT),
                open(3, 1L, T.plusSeconds(20)));

        var business = new FarmGoatRegistryQueryBusiness(farmId -> List.of(candidate));

        List<FarmGoatRegistryItem> items = business.findForFarm(1L);

        assertThat(items).hasSize(1);
        FarmGoatRegistryItem item = items.getFirst();
        assertThat(item.roles()).contains(FarmGoatRegistryRole.CURRENT_OWNER)
                .doesNotContain(FarmGoatRegistryRole.FORMER_OWNER);
        assertThat(item.disposition()).isEqualTo(FarmGoatRegistryDisposition.CURRENT);
        assertThat(item.currentOwnerFarmId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Case 6: External sale from A - Farm A remains included with FORMER_OWNER and SOLD")
    void case06_externalSaleFromA() {
        var business = new FarmGoatRegistryQueryBusiness(farmId -> List.of(
                candidate(1, null, closed(1, 1L, T, T.plusSeconds(10), OwnershipExitType.EXTERNAL_SALE))
        ));

        List<FarmGoatRegistryItem> items = business.findForFarm(1L);

        assertThat(items).hasSize(1);
        FarmGoatRegistryItem item = items.getFirst();
        assertThat(item.roles()).containsExactly(FarmGoatRegistryRole.FORMER_OWNER);
        assertThat(item.disposition()).isEqualTo(FarmGoatRegistryDisposition.SOLD);
        assertThat(item.currentOwnerFarmId()).isNull();
    }

    @Test
    @DisplayName("Case 7: Donation from A - disposition is DONATED")
    void case07_donationFromA() {
        var business = new FarmGoatRegistryQueryBusiness(farmId -> List.of(
                candidate(1, null, closed(1, 1L, T, T.plusSeconds(10), OwnershipExitType.DONATION))
        ));

        List<FarmGoatRegistryItem> items = business.findForFarm(1L);

        assertThat(items).hasSize(1);
        FarmGoatRegistryItem item = items.getFirst();
        assertThat(item.roles()).containsExactly(FarmGoatRegistryRole.FORMER_OWNER);
        assertThat(item.disposition()).isEqualTo(FarmGoatRegistryDisposition.DONATED);
    }

    @Test
    @DisplayName("Case 8: Retirement / discard from A - disposition is RETIRED")
    void case08_retirementFromA() {
        var business = new FarmGoatRegistryQueryBusiness(farmId -> List.of(
                candidate(1, null, closed(1, 1L, T, T.plusSeconds(10), OwnershipExitType.RETIREMENT))
        ));

        List<FarmGoatRegistryItem> items = business.findForFarm(1L);

        assertThat(items).hasSize(1);
        FarmGoatRegistryItem item = items.getFirst();
        assertThat(item.roles()).containsExactly(FarmGoatRegistryRole.FORMER_OWNER);
        assertThat(item.disposition()).isEqualTo(FarmGoatRegistryDisposition.RETIRED);
    }

    @Test
    @DisplayName("Case 9: Death - for the farm owning the goat at death, disposition is DECEASED")
    void case09_deathInA() {
        var business = new FarmGoatRegistryQueryBusiness(farmId -> List.of(
                candidate(1, null, closed(1, 1L, T, T.plusSeconds(10), OwnershipExitType.DEATH))
        ));

        List<FarmGoatRegistryItem> items = business.findForFarm(1L);

        assertThat(items).hasSize(1);
        FarmGoatRegistryItem item = items.getFirst();
        assertThat(item.roles()).containsExactly(FarmGoatRegistryRole.FORMER_OWNER);
        assertThat(item.disposition()).isEqualTo(FarmGoatRegistryDisposition.DECEASED);
    }

    @Test
    @DisplayName("Case 10: A -> B then death in B - Farm A remains TRANSFERRED, Farm B is DECEASED, global FALECIDO does not pollute A")
    void case10_transferAToBThenDeathInB_farmARemainsTransferred_farmBIsDeceased() {
        var candidate = new FarmGoatRegistryQueryPort.Candidate(
                new GoatId(1L), "RG1", "Goat1", GoatStatus.FALECIDO, null,
                List.of(
                        closed(1, 1L, T, T.plusSeconds(10), OwnershipExitType.TRANSFER_OUT),
                        closed(2, 2L, T.plusSeconds(10), T.plusSeconds(20), OwnershipExitType.DEATH)
                )
        );

        var businessA = new FarmGoatRegistryQueryBusiness(farmId -> List.of(candidate));
        var businessB = new FarmGoatRegistryQueryBusiness(farmId -> List.of(candidate));

        List<FarmGoatRegistryItem> itemsA = businessA.findForFarm(1L);
        assertThat(itemsA.getFirst().roles()).containsExactly(FarmGoatRegistryRole.FORMER_OWNER);
        assertThat(itemsA.getFirst().disposition()).isEqualTo(FarmGoatRegistryDisposition.TRANSFERRED);
        assertThat(itemsA.getFirst().globalStatus()).isEqualTo(GoatStatus.FALECIDO);

        List<FarmGoatRegistryItem> itemsB = businessB.findForFarm(2L);
        assertThat(itemsB.getFirst().roles()).containsExactly(FarmGoatRegistryRole.FORMER_OWNER);
        assertThat(itemsB.getFirst().disposition()).isEqualTo(FarmGoatRegistryDisposition.DECEASED);
    }

    @Test
    @DisplayName("Case 11: Multiple ownership periods for same farm (A -> B -> A -> C) - exactly one row in Farm A's Registry")
    void case11_multipleOwnershipPeriodsSameFarm_exactlyOneRowInRegistry() {
        var candidate = candidate(1, null,
                closed(1, 1L, T, T.plusSeconds(10), OwnershipExitType.TRANSFER_OUT),
                closed(2, 2L, T.plusSeconds(10), T.plusSeconds(20), OwnershipExitType.TRANSFER_OUT),
                closed(3, 1L, T.plusSeconds(20), T.plusSeconds(30), OwnershipExitType.TRANSFER_OUT),
                open(4, 3L, T.plusSeconds(30)));

        var business = new FarmGoatRegistryQueryBusiness(farmId -> List.of(candidate));

        List<FarmGoatRegistryItem> items = business.findForFarm(1L);

        assertThat(items).hasSize(1);
        FarmGoatRegistryItem item = items.getFirst();
        assertThat(item.roles()).containsExactly(FarmGoatRegistryRole.FORMER_OWNER);
        assertThat(item.disposition()).isEqualTo(FarmGoatRegistryDisposition.TRANSFERRED);
        assertThat(item.currentOwnerFarmId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Case 12: Legacy ownership without CreatorReference - proven by ownership, no CREATOR role added")
    void case12_legacyOwnershipWithoutCreatorReference() {
        var business = new FarmGoatRegistryQueryBusiness(farmId -> List.of(
                candidate(1, null, open(1, 1L))
        ));

        List<FarmGoatRegistryItem> items = business.findForFarm(1L);

        assertThat(items).hasSize(1);
        FarmGoatRegistryItem item = items.getFirst();
        assertThat(item.creatorFarmId()).isNull();
        assertThat(item.creatorNameSnapshot()).isNull();
        assertThat(item.roles()).containsExactly(FarmGoatRegistryRole.CURRENT_OWNER);
        assertThat(item.disposition()).isEqualTo(FarmGoatRegistryDisposition.CURRENT);
    }

    @Test
    @DisplayName("Case 13: CreatorReference belongs to another Farm - does not grant CREATOR role to A, creator-only does not create membership")
    void case13_creatorReferenceBelongsToAnotherFarm() {
        // Sub-case a: Creator is Farm 2, ownership is Farm 1. Farm 1 gets CURRENT_OWNER, NOT CREATOR. Provenance shows Farm 2.
        var acquiredGoat = candidate(1,
                CreatorReference.farm("TOD_B", 2L, "Capril B", CreatorSource.ABCC, null, T),
                open(1, 1L));

        var businessA = new FarmGoatRegistryQueryBusiness(farmId -> List.of(acquiredGoat));
        List<FarmGoatRegistryItem> itemsA = businessA.findForFarm(1L);

        assertThat(itemsA).hasSize(1);
        assertThat(itemsA.getFirst().roles()).containsExactly(FarmGoatRegistryRole.CURRENT_OWNER);
        assertThat(itemsA.getFirst().creatorFarmId()).isEqualTo(2L);
        assertThat(itemsA.getFirst().creatorNameSnapshot()).isEqualTo("Capril B");

        // Sub-case b: Creator is Farm 2, NO ownership for Farm 1. Port returns empty for Farm 1.
        var businessAForNonOwned = new FarmGoatRegistryQueryBusiness(farmId -> List.of());
        assertThat(businessAForNonOwned.findForFarm(1L)).isEmpty();
    }

    @Test
    @DisplayName("Case 14: Projection-only relationship - goat must not appear in Farm A Registry without provenance/ownership")
    void case14_projectionOnlyRelationship_goatMustNotAppearInRegistry() {
        // Goat has projection pointing to Farm 1, but no CreatorReference and no GoatOwnershipPeriod for Farm 1.
        // Port finds candidates using only CreatorReference and GoatOwnershipPeriod, so port returns no candidate for Farm 1.
        var business = new FarmGoatRegistryQueryBusiness(farmId -> List.of());

        List<FarmGoatRegistryItem> items = business.findForFarm(1L);

        assertThat(items).isEmpty();
    }

    @Test
    @DisplayName("Case 15: Projection drift - projection says Farm A, canonical OPEN ownership says Farm B")
    void case15_projectionDrift_projectionDoesNotOverrideCanonicalOwnership() {
        // Goat has canonical OPEN ownership in Farm 2 (Farm B). Even if projection says Farm 1,
        // Farm 2 is CURRENT_OWNER + CURRENT, and Farm 1 does NOT receive CURRENT_OWNER.
        var candidate = candidate(1, null, open(1, 2L));

        // When queried for Farm 2 (the canonical owner):
        var businessB = new FarmGoatRegistryQueryBusiness(farmId -> List.of(candidate));
        List<FarmGoatRegistryItem> itemsB = businessB.findForFarm(2L);
        assertThat(itemsB).hasSize(1);
        assertThat(itemsB.getFirst().roles()).containsExactly(FarmGoatRegistryRole.CURRENT_OWNER);
        assertThat(itemsB.getFirst().disposition()).isEqualTo(FarmGoatRegistryDisposition.CURRENT);
        assertThat(itemsB.getFirst().currentOwnerFarmId()).isEqualTo(2L);

        // When queried for Farm 1 (drifted projection, but no ownership in Farm 1):
        var businessA = new FarmGoatRegistryQueryBusiness(farmId -> List.of());
        assertThat(businessA.findForFarm(1L)).isEmpty();

        // Even if Farm 1 had a prior closed period, it is FORMER_OWNER, never CURRENT_OWNER:
        var driftedWithPriorPeriod = candidate(1, null,
                closed(1, 1L, T, T.plusSeconds(10), OwnershipExitType.TRANSFER_OUT),
                open(2, 2L, T.plusSeconds(10)));
        var businessAPrior = new FarmGoatRegistryQueryBusiness(farmId -> List.of(driftedWithPriorPeriod));
        List<FarmGoatRegistryItem> itemsAPrior = businessAPrior.findForFarm(1L);
        assertThat(itemsAPrior).hasSize(1);
        assertThat(itemsAPrior.getFirst().roles()).containsExactly(FarmGoatRegistryRole.FORMER_OWNER);
        assertThat(itemsAPrior.getFirst().disposition()).isEqualTo(FarmGoatRegistryDisposition.TRANSFERRED);
        assertThat(itemsAPrior.getFirst().currentOwnerFarmId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Invalid farmId throws IllegalArgumentException")
    void findForFarm_invalidFarmId_throwsIllegalArgumentException() {
        var business = new FarmGoatRegistryQueryBusiness(farmId -> List.of());

        assertThatThrownBy(() -> business.findForFarm(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("farmId must be positive");

        assertThatThrownBy(() -> business.findForFarm(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("farmId must be positive");
    }

    @Test
    @DisplayName("Multiple open periods in history fails closed with IllegalArgumentException")
    void multipleOpenPeriods_failsClosed() {
        var candidate = candidate(1, null,
                open(10, 1L, 1L, T),
                open(20, 1L, 2L, T.plusSeconds(10)));
        var business = new FarmGoatRegistryQueryBusiness(farmId -> List.of(candidate));

        assertThatThrownBy(() -> business.findForFarm(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Overlapping closed periods in history fails closed with IllegalArgumentException")
    void overlappingClosedPeriods_failsClosed() {
        var candidate = candidate(1, null,
                closed(10, 1L, 1L, T, T.plusSeconds(20), OwnershipExitType.TRANSFER_OUT),
                closed(20, 1L, 1L, T.plusSeconds(10), T.plusSeconds(30), OwnershipExitType.TRANSFER_OUT));
        var business = new FarmGoatRegistryQueryBusiness(farmId -> List.of(candidate));

        assertThatThrownBy(() -> business.findForFarm(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ownership periods overlap for the same GoatId");
    }

    @Test
    @DisplayName("Two closed periods with same endedAt overlap and fail closed through canonical consistency validation")
    void sameEndedAtMalformedHistory_failsClosed() {
        var candidate = candidate(1, null,
                closed(10, 1L, 1L, T, T.plusSeconds(20), OwnershipExitType.TRANSFER_OUT),
                closed(20, 1L, 1L, T.plusSeconds(10), T.plusSeconds(20), OwnershipExitType.EXTERNAL_SALE));
        var business = new FarmGoatRegistryQueryBusiness(farmId -> List.of(candidate));

        assertThatThrownBy(() -> business.findForFarm(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ownership periods overlap for the same GoatId");
    }

    @Test
    @DisplayName("Cross-farm overlapping periods fail closed over complete goat history, not requested-farm subset")
    void crossFarmOverlap_failsClosedForBothFarms() {
        var candidate = candidate(1, null,
                closed(10, 1L, 1L, T, T.plusSeconds(20), OwnershipExitType.TRANSFER_OUT),
                closed(20, 1L, 2L, T.plusSeconds(10), T.plusSeconds(30), OwnershipExitType.EXTERNAL_SALE));
        var businessA = new FarmGoatRegistryQueryBusiness(farmId -> List.of(candidate));
        var businessB = new FarmGoatRegistryQueryBusiness(farmId -> List.of(candidate));

        assertThatThrownBy(() -> businessA.findForFarm(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ownership periods overlap for the same GoatId");

        assertThatThrownBy(() -> businessB.findForFarm(2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ownership periods overlap for the same GoatId");
    }

    @Test
    @DisplayName("Registry results are deterministically ordered by GoatId ascending regardless of candidate input order")
    void deterministicResultOrder_sortsByGoatIdAscending() {
        var candidate30 = candidate(30, null, open(30, 1L));
        var candidate10 = candidate(10, null, open(10, 1L));
        var candidate20 = candidate(20, null, open(20, 1L));

        var business = new FarmGoatRegistryQueryBusiness(farmId -> List.of(candidate30, candidate10, candidate20));

        List<FarmGoatRegistryItem> items = business.findForFarm(1L);

        assertThat(items).extracting(item -> item.goatId().value())
                .containsExactly(10L, 20L, 30L);
    }

    private static FarmGoatRegistryQueryPort.Candidate candidate(
            long id,
            CreatorReference creator,
            GoatOwnershipPeriod... periods
    ) {
        return new FarmGoatRegistryQueryPort.Candidate(
                new GoatId(id), "RG" + id, "Goat" + id, GoatStatus.ATIVO, creator, List.of(periods)
        );
    }

    private static GoatOwnershipPeriod open(long id, long farmId) {
        return GoatOwnershipPeriod.open(new GoatId(id), farmId, T, OwnershipEntryType.BIRTH, "test");
    }

    private static GoatOwnershipPeriod open(long id, long farmId, Instant startedAt) {
        return GoatOwnershipPeriod.rehydrate(id, new GoatId(id), farmId, startedAt, null, OwnershipEntryType.TRANSFER_IN, null, "test");
    }

    private static GoatOwnershipPeriod open(long periodId, long goatId, long farmId, Instant startedAt) {
        return GoatOwnershipPeriod.rehydrate(periodId, new GoatId(goatId), farmId, startedAt, null, OwnershipEntryType.TRANSFER_IN, null, "test");
    }

    private static GoatOwnershipPeriod closed(long id, long farmId, OwnershipExitType exitType) {
        return GoatOwnershipPeriod.rehydrate(id, new GoatId(id), farmId, T, T.plusSeconds(1), OwnershipEntryType.BIRTH, exitType, "test");
    }

    private static GoatOwnershipPeriod closed(long id, long farmId, Instant startedAt, Instant endedAt, OwnershipExitType exitType) {
        return GoatOwnershipPeriod.rehydrate(id, new GoatId(id), farmId, startedAt, endedAt, OwnershipEntryType.BIRTH, exitType, "test");
    }

    private static GoatOwnershipPeriod closed(long periodId, long goatId, long farmId, Instant startedAt, Instant endedAt, OwnershipExitType exitType) {
        return GoatOwnershipPeriod.rehydrate(periodId, new GoatId(goatId), farmId, startedAt, endedAt, OwnershipEntryType.BIRTH, exitType, "test");
    }
}
