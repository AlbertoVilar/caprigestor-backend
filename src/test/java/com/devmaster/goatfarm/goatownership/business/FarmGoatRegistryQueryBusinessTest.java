package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.domain.Goat;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.domain.RegistrationIdentity;
import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FarmGoatRegistryQueryBusinessTest {

    private static final Instant T = Instant.parse("2026-01-01T00:00:00Z");
    private final GoatPersistencePort dummyGoatPort = mock(GoatPersistencePort.class);

    @Test
    @DisplayName("Constructor requires non-null dependencies")
    void constructor_validatesNonNullDependencies() {
        assertThatThrownBy(() -> new FarmGoatRegistryQueryBusiness(null, dummyGoatPort))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("queryPort must not be null");

        assertThatThrownBy(() -> new FarmGoatRegistryQueryBusiness(mock(FarmGoatRegistryQueryPort.class), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("goatPersistencePort must not be null");
    }

    @Test
    @DisplayName("FarmGoatRegistryQueryPort.findCandidate method is mandatory without default implementation")
    void portInterface_findCandidateHasNoDefaultImplementation() throws NoSuchMethodException {
        var method = FarmGoatRegistryQueryPort.class.getMethod("findCandidate", long.class, GoatId.class);
        assertThat(method.isDefault()).isFalse();
    }

    @Test
    @DisplayName("Single item dossier query invokes queryPort.findCandidate(...) and never queryPort.findCandidates(...)")
    void singleItemQuery_invokesFindCandidateAndNeverFindCandidates() {
        FarmGoatRegistryQueryPort queryPort = mock(FarmGoatRegistryQueryPort.class);
        GoatPersistencePort goatPort = mock(GoatPersistencePort.class);
        var cand = candidate(42L, null, open(42L, 1L));
        when(queryPort.findCandidate(1L, new GoatId(42L))).thenReturn(Optional.of(cand));
        when(goatPort.findById(new GoatId(42L))).thenReturn(Optional.of(sampleGoat(42L, 1L, GoatStatus.ATIVO)));

        var business = new FarmGoatRegistryQueryBusiness(queryPort, goatPort);
        var result = business.findHistoricalDossierBasic(1L, new GoatId(42L));

        assertThat(result).isPresent();
        verify(queryPort).findCandidate(1L, new GoatId(42L));
        verify(queryPort, never()).findCandidates(anyLong());
    }

    @Test
    @DisplayName("Case 1: Creator only - included in Farm Registry with role CREATOR and disposition NONE")
    void case01_creatorOnly() {
        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of(
                candidate(1, CreatorReference.farm("TOD_A", 1L, "Capril A", CreatorSource.BIRTH, null, T))
        )), dummyGoatPort);

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
        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of(
                candidate(1, null, open(1, 1L))
        )), dummyGoatPort);

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
        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of(
                candidate(1, CreatorReference.farm("TOD_A", 1L, "Capril A", CreatorSource.BIRTH, null, T), open(1, 1L))
        )), dummyGoatPort);

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

        var businessA = new FarmGoatRegistryQueryBusiness(stubPort(List.of(candidate)), dummyGoatPort);
        var businessB = new FarmGoatRegistryQueryBusiness(stubPort(List.of(candidate)), dummyGoatPort);

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

        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of(candidate)), dummyGoatPort);

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
        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of(
                candidate(1, null, closed(1, 1L, T, T.plusSeconds(10), OwnershipExitType.EXTERNAL_SALE))
        )), dummyGoatPort);

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
        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of(
                candidate(1, null, closed(1, 1L, T, T.plusSeconds(10), OwnershipExitType.DONATION))
        )), dummyGoatPort);

        List<FarmGoatRegistryItem> items = business.findForFarm(1L);

        assertThat(items).hasSize(1);
        FarmGoatRegistryItem item = items.getFirst();
        assertThat(item.roles()).containsExactly(FarmGoatRegistryRole.FORMER_OWNER);
        assertThat(item.disposition()).isEqualTo(FarmGoatRegistryDisposition.DONATED);
    }

    @Test
    @DisplayName("Case 8: Retirement / discard from A - disposition is RETIRED")
    void case08_retirementFromA() {
        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of(
                candidate(1, null, closed(1, 1L, T, T.plusSeconds(10), OwnershipExitType.RETIREMENT))
        )), dummyGoatPort);

        List<FarmGoatRegistryItem> items = business.findForFarm(1L);

        assertThat(items).hasSize(1);
        FarmGoatRegistryItem item = items.getFirst();
        assertThat(item.roles()).containsExactly(FarmGoatRegistryRole.FORMER_OWNER);
        assertThat(item.disposition()).isEqualTo(FarmGoatRegistryDisposition.RETIRED);
    }

    @Test
    @DisplayName("Case 9: Death - for the farm owning the goat at death, disposition is DECEASED")
    void case09_deathInA() {
        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of(
                candidate(1, null, closed(1, 1L, T, T.plusSeconds(10), OwnershipExitType.DEATH))
        )), dummyGoatPort);

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

        var businessA = new FarmGoatRegistryQueryBusiness(stubPort(List.of(candidate)), dummyGoatPort);
        var businessB = new FarmGoatRegistryQueryBusiness(stubPort(List.of(candidate)), dummyGoatPort);

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

        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of(candidate)), dummyGoatPort);

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
        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of(
                candidate(1, null, open(1, 1L))
        )), dummyGoatPort);

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

        var businessA = new FarmGoatRegistryQueryBusiness(stubPort(List.of(acquiredGoat)), dummyGoatPort);
        List<FarmGoatRegistryItem> itemsA = businessA.findForFarm(1L);

        assertThat(itemsA).hasSize(1);
        assertThat(itemsA.getFirst().roles()).containsExactly(FarmGoatRegistryRole.CURRENT_OWNER);
        assertThat(itemsA.getFirst().creatorFarmId()).isEqualTo(2L);
        assertThat(itemsA.getFirst().creatorNameSnapshot()).isEqualTo("Capril B");

        // Sub-case b: Creator is Farm 2, NO ownership for Farm 1. Port returns empty for Farm 1.
        var businessAForNonOwned = new FarmGoatRegistryQueryBusiness(stubPort(List.of()), dummyGoatPort);
        assertThat(businessAForNonOwned.findForFarm(1L)).isEmpty();
    }

    @Test
    @DisplayName("Case 14: Projection-only relationship - goat must not appear in Farm A Registry without provenance/ownership")
    void case14_projectionOnlyRelationship_goatMustNotAppearInRegistry() {
        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of()), dummyGoatPort);

        List<FarmGoatRegistryItem> items = business.findForFarm(1L);

        assertThat(items).isEmpty();
    }

    @Test
    @DisplayName("Case 15: Projection drift - projection says Farm A, canonical OPEN ownership says Farm B")
    void case15_projectionDrift_projectionDoesNotOverrideCanonicalOwnership() {
        var candidate = candidate(1, null, open(1, 2L));

        var businessB = new FarmGoatRegistryQueryBusiness(stubPort(List.of(candidate)), dummyGoatPort);
        List<FarmGoatRegistryItem> itemsB = businessB.findForFarm(2L);
        assertThat(itemsB).hasSize(1);
        assertThat(itemsB.getFirst().roles()).containsExactly(FarmGoatRegistryRole.CURRENT_OWNER);
        assertThat(itemsB.getFirst().disposition()).isEqualTo(FarmGoatRegistryDisposition.CURRENT);
        assertThat(itemsB.getFirst().currentOwnerFarmId()).isEqualTo(2L);

        var businessA = new FarmGoatRegistryQueryBusiness(stubPort(List.of()), dummyGoatPort);
        assertThat(businessA.findForFarm(1L)).isEmpty();

        var driftedWithPriorPeriod = candidate(1, null,
                closed(1, 1L, T, T.plusSeconds(10), OwnershipExitType.TRANSFER_OUT),
                open(2, 2L, T.plusSeconds(10)));
        var businessAPrior = new FarmGoatRegistryQueryBusiness(stubPort(List.of(driftedWithPriorPeriod)), dummyGoatPort);
        List<FarmGoatRegistryItem> itemsAPrior = businessAPrior.findForFarm(1L);
        assertThat(itemsAPrior).hasSize(1);
        assertThat(itemsAPrior.getFirst().roles()).containsExactly(FarmGoatRegistryRole.FORMER_OWNER);
        assertThat(itemsAPrior.getFirst().disposition()).isEqualTo(FarmGoatRegistryDisposition.TRANSFERRED);
        assertThat(itemsAPrior.getFirst().currentOwnerFarmId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Invalid farmId throws IllegalArgumentException")
    void findForFarm_invalidFarmId_throwsIllegalArgumentException() {
        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of()), dummyGoatPort);

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
        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of(candidate)), dummyGoatPort);

        assertThatThrownBy(() -> business.findForFarm(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Overlapping closed periods in history fails closed with IllegalArgumentException")
    void overlappingClosedPeriods_failsClosed() {
        var candidate = candidate(1, null,
                closed(10, 1L, 1L, T, T.plusSeconds(20), OwnershipExitType.TRANSFER_OUT),
                closed(20, 1L, 1L, T.plusSeconds(10), T.plusSeconds(30), OwnershipExitType.TRANSFER_OUT));
        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of(candidate)), dummyGoatPort);

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
        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of(candidate)), dummyGoatPort);

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
        var businessA = new FarmGoatRegistryQueryBusiness(stubPort(List.of(candidate)), dummyGoatPort);
        var businessB = new FarmGoatRegistryQueryBusiness(stubPort(List.of(candidate)), dummyGoatPort);

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

        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of(candidate30, candidate10, candidate20)), dummyGoatPort);

        List<FarmGoatRegistryItem> items = business.findForFarm(1L);

        assertThat(items).extracting(item -> item.goatId().value())
                .containsExactly(10L, 20L, 30L);
    }

    @Test
    @DisplayName("Dossier: Exact current-owner lookup retrieves dossier basic item with CURRENT_OWNER and CURRENT")
    void dossier_currentOwner_success() {
        var cand = candidate(1L, null, open(1L, 1L));
        var goat = sampleGoat(1L, 1L, GoatStatus.ATIVO);
        GoatPersistencePort goatPort = mock(GoatPersistencePort.class);
        when(goatPort.findById(new GoatId(1L))).thenReturn(Optional.of(goat));

        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of(cand)), goatPort);

        var dossierOpt = business.findHistoricalDossierBasic(1L, new GoatId(1L));

        assertThat(dossierOpt).isPresent();
        var dossier = dossierOpt.get();
        assertThat(dossier.goatId()).isEqualTo(new GoatId(1L));
        assertThat(dossier.registrationNumber()).isEqualTo("TODA0001");
        assertThat(dossier.name()).isEqualTo("Goat 1");
        assertThat(dossier.globalStatus()).isEqualTo(GoatStatus.ATIVO);
        assertThat(dossier.gender()).isEqualTo(Gender.FEMEA);
        assertThat(dossier.breed()).isEqualTo(GoatBreed.SAANEN);
        assertThat(dossier.color()).isEqualTo("Branca");
        assertThat(dossier.birthDate()).isEqualTo(LocalDate.of(2023, 1, 1));
        assertThat(dossier.category()).isEqualTo(Category.PA);
        assertThat(dossier.tod()).isEqualTo("TODA");
        assertThat(dossier.toe()).isEqualTo("0001");
        assertThat(dossier.fatherName()).isEqualTo("Pai Local");
        assertThat(dossier.fatherRegistrationNumber()).isEqualTo("RG-PAI");
        assertThat(dossier.motherName()).isNull();
        assertThat(dossier.motherRegistrationNumber()).isEqualTo("RG-MAE-EXT");
        assertThat(dossier.roles()).containsExactly(FarmGoatRegistryRole.CURRENT_OWNER);
        assertThat(dossier.disposition()).isEqualTo(FarmGoatRegistryDisposition.CURRENT);
        assertThat(dossier.currentOwnerFarmId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Dossier: Exact former-owner lookup retrieves dossier with FORMER_OWNER and SOLD")
    void dossier_formerOwner_success() {
        var cand = candidate(1L, null, closed(1L, 1L, T, T.plusSeconds(10), OwnershipExitType.EXTERNAL_SALE));
        var goat = sampleGoat(1L, 1L, GoatStatus.VENDIDO);
        GoatPersistencePort goatPort = mock(GoatPersistencePort.class);
        when(goatPort.findById(new GoatId(1L))).thenReturn(Optional.of(goat));

        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of(cand)), goatPort);

        var dossierOpt = business.findHistoricalDossierBasic(1L, new GoatId(1L));

        assertThat(dossierOpt).isPresent();
        var dossier = dossierOpt.get();
        assertThat(dossier.roles()).containsExactly(FarmGoatRegistryRole.FORMER_OWNER);
        assertThat(dossier.disposition()).isEqualTo(FarmGoatRegistryDisposition.SOLD);
        assertThat(dossier.currentOwnerFarmId()).isNull();
    }

    @Test
    @DisplayName("Dossier: Exact creator-only lookup retrieves dossier with CREATOR and NONE")
    void dossier_creatorOnly_success() {
        var cand = candidate(1L, CreatorReference.farm("TOD_A", 1L, "Capril A", CreatorSource.BIRTH, null, T));
        var goat = sampleGoat(1L, 2L, GoatStatus.ATIVO);
        GoatPersistencePort goatPort = mock(GoatPersistencePort.class);
        when(goatPort.findById(new GoatId(1L))).thenReturn(Optional.of(goat));

        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of(cand)), goatPort);

        var dossierOpt = business.findHistoricalDossierBasic(1L, new GoatId(1L));

        assertThat(dossierOpt).isPresent();
        var dossier = dossierOpt.get();
        assertThat(dossier.roles()).containsExactly(FarmGoatRegistryRole.CREATOR);
        assertThat(dossier.disposition()).isEqualTo(FarmGoatRegistryDisposition.NONE);
        assertThat(dossier.creatorFarmId()).isEqualTo(1L);
        assertThat(dossier.creatorNameSnapshot()).isEqualTo("Capril A");
        assertThat(dossier.currentOwnerFarmId()).isNull();
    }

    @Test
    @DisplayName("Dossier: Unrelated goat returns empty")
    void dossier_unrelatedGoat_returnsEmpty() {
        GoatPersistencePort goatPort = mock(GoatPersistencePort.class);
        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of()), goatPort);

        var dossierOpt = business.findHistoricalDossierBasic(1L, new GoatId(999L));

        assertThat(dossierOpt).isEmpty();
    }

    @Test
    @DisplayName("Dossier: A -> B transfer - Farm A can still retrieve dossier with FORMER_OWNER / TRANSFERRED / currentOwnerFarmId = 2")
    void dossier_transferAToB_farmACanRetrieve() {
        var cand = candidate(1L, null,
                closed(1L, 1L, T, T.plusSeconds(10), OwnershipExitType.TRANSFER_OUT),
                open(2L, 2L, T.plusSeconds(10)));
        var goat = sampleGoat(1L, 2L, GoatStatus.ATIVO);
        GoatPersistencePort goatPort = mock(GoatPersistencePort.class);
        when(goatPort.findById(new GoatId(1L))).thenReturn(Optional.of(goat));

        var businessA = new FarmGoatRegistryQueryBusiness(stubPort(List.of(cand)), goatPort);

        var dossierOpt = businessA.findHistoricalDossierBasic(1L, new GoatId(1L));

        assertThat(dossierOpt).isPresent();
        var dossier = dossierOpt.get();
        assertThat(dossier.roles()).containsExactly(FarmGoatRegistryRole.FORMER_OWNER);
        assertThat(dossier.disposition()).isEqualTo(FarmGoatRegistryDisposition.TRANSFERRED);
        assertThat(dossier.currentOwnerFarmId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Dossier: CRITICAL CASE A -> B -> terminal exit at B - Farm A still retrieves basic profile with currentOwnerFarmId == null")
    void dossier_transferAToBThenTerminalExitAtB_farmARetrievesWithNullCurrentOwner() {
        var cand = new FarmGoatRegistryQueryPort.Candidate(
                new GoatId(1L), "RG1", "Goat 1", GoatStatus.FALECIDO, null,
                List.of(
                        closed(1L, 1L, 1L, T, T.plusSeconds(10), OwnershipExitType.TRANSFER_OUT),
                        closed(2L, 1L, 2L, T.plusSeconds(10), T.plusSeconds(20), OwnershipExitType.DEATH)
                )
        );
        var goat = sampleGoat(1L, 2L, GoatStatus.FALECIDO);
        GoatPersistencePort goatPort = mock(GoatPersistencePort.class);
        when(goatPort.findById(new GoatId(1L))).thenReturn(Optional.of(goat));

        var businessA = new FarmGoatRegistryQueryBusiness(stubPort(List.of(cand)), goatPort);

        var dossierOpt = businessA.findHistoricalDossierBasic(1L, new GoatId(1L));

        assertThat(dossierOpt).isPresent();
        var dossier = dossierOpt.get();
        assertThat(dossier.goatId()).isEqualTo(new GoatId(1L));
        assertThat(dossier.globalStatus()).isEqualTo(GoatStatus.FALECIDO);
        assertThat(dossier.roles()).containsExactly(FarmGoatRegistryRole.FORMER_OWNER);
        assertThat(dossier.disposition()).isEqualTo(FarmGoatRegistryDisposition.TRANSFERRED);
        assertThat(dossier.currentOwnerFarmId()).isNull();
    }

    @Test
    @DisplayName("Dossier: Retrieval does not require Goat projected farm == requested historical Farm")
    void dossier_retrievalDoesNotRequireGoatProjectedFarmEqualsRequestedFarm() {
        var cand = candidate(1L, null, closed(1L, 1L, T, T.plusSeconds(10), OwnershipExitType.TRANSFER_OUT), open(2L, 99L, T.plusSeconds(10)));
        var goat = sampleGoat(1L, 99L, GoatStatus.ATIVO);
        GoatPersistencePort goatPort = mock(GoatPersistencePort.class);
        when(goatPort.findById(new GoatId(1L))).thenReturn(Optional.of(goat));

        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of(cand)), goatPort);

        var dossierOpt = business.findHistoricalDossierBasic(1L, new GoatId(1L));

        assertThat(dossierOpt).isPresent();
        assertThat(dossierOpt.get().goatId()).isEqualTo(new GoatId(1L));
    }

    @Test
    @DisplayName("Dossier: Canonical ownership inconsistency still fails closed with IllegalArgumentException")
    void dossier_canonicalOwnershipInconsistency_failsClosed() {
        var cand = candidate(1L, null,
                closed(10L, 1L, 1L, T, T.plusSeconds(20), OwnershipExitType.TRANSFER_OUT),
                closed(20L, 1L, 1L, T.plusSeconds(10), T.plusSeconds(30), OwnershipExitType.TRANSFER_OUT));
        GoatPersistencePort goatPort = mock(GoatPersistencePort.class);
        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of(cand)), goatPort);

        assertThatThrownBy(() -> business.findHistoricalDossierBasic(1L, new GoatId(1L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ownership periods overlap for the same GoatId");
    }

    @Test
    @DisplayName("Dossier: Invalid farmId or null goatId throws IllegalArgumentException")
    void dossier_invalidArguments_throwsIllegalArgumentException() {
        GoatPersistencePort goatPort = mock(GoatPersistencePort.class);
        var business = new FarmGoatRegistryQueryBusiness(stubPort(List.of()), goatPort);

        assertThatThrownBy(() -> business.findHistoricalDossierBasic(0L, new GoatId(1L)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> business.findHistoricalDossierBasic(-1L, new GoatId(1L)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> business.findHistoricalDossierBasic(1L, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> business.findForFarmAndGoat(0L, new GoatId(1L)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> business.findForFarmAndGoat(1L, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static GoatPersistencePort dummyGoatPort() {
        return mock(GoatPersistencePort.class);
    }

    private static FarmGoatRegistryQueryPort stubPort(List<FarmGoatRegistryQueryPort.Candidate> candidates) {
        return new FarmGoatRegistryQueryPort() {
            @Override
            public List<Candidate> findCandidates(long farmId) {
                return candidates;
            }

            @Override
            public Optional<Candidate> findCandidate(long farmId, GoatId goatId) {
                return candidates.stream()
                        .filter(c -> c.goatId().equals(goatId))
                        .findFirst();
            }
        };
    }

    private static Goat sampleGoat(long id, long projectedFarmId, GoatStatus status) {
        String tod = "TODA";
        String toe = String.format("%04d", id);
        return Goat.rehydrate(
                new GoatId(id),
                RegistrationIdentity.of(tod + toe, tod, toe),
                "Goat " + id,
                Gender.FEMEA,
                GoatBreed.SAANEN,
                "Branca",
                LocalDate.of(2023, 1, 1),
                status,
                null, null, null,
                Category.PA,
                Goat.ParentReference.local(new GoatId(100L), "RG-PAI", "Pai Local"),
                Goat.ParentReference.external("RG-MAE-EXT"),
                projectedFarmId,
                1L,
                "Capril Projected",
                "Owner User"
        );
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
