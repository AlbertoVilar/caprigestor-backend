package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.domain.Goat;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.domain.RegistrationIdentity;
import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipHistory;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipHistoryPeriod;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipQueryPort;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipExitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoatOwnershipHistoryBusinessTest {
    private static final GoatId GOAT_ID = GoatId.of(42L);

    @Mock GoatPersistencePort goatPersistence;
    @Mock GoatOwnershipQueryPort ownershipQuery;
    @Mock FarmAuthorizationUseCase farmAuthorization;
    @Mock CurrentPrincipalQueryUseCase currentPrincipalQuery;

    private GoatOwnershipHistoryBusiness business;

    @BeforeEach
    void setUp() {
        business = new GoatOwnershipHistoryBusiness(goatPersistence, ownershipQuery,
                farmAuthorization, currentPrincipalQuery);
    }

    @Test
    void nullGoatIdIsRejected() {
        assertThatThrownBy(() -> business.findOwnershipHistory(null))
                .isInstanceOf(InvalidArgumentException.class);
        verify(goatPersistence, never()).findById(any());
    }

    @Test
    void nonexistentGoatIsRejectedBeforeAuthorization() {
        when(goatPersistence.findById(GOAT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> business.findOwnershipHistory(GOAT_ID))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(currentPrincipalQuery, never()).requireCurrent();
    }

    @Test
    void adminCanReadBaselineHistory() {
        givenGoatExists();
        when(currentPrincipalQuery.requireCurrent()).thenReturn(principal("ROLE_ADMIN"));
        var baseline = period(1L, 10L, "2026-01-01T00:00:00Z", null,
                OwnershipEntryType.MANUAL_IMPORT, null);
        when(ownershipQuery.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(baseline));

        OwnershipHistory result = business.findOwnershipHistory(GOAT_ID);

        assertThat(result.goatId()).isEqualTo(GOAT_ID);
        assertThat(result.periods()).containsExactly(new OwnershipHistoryPeriod(
                10L, baseline.startedAt(), null, OwnershipEntryType.MANUAL_IMPORT, null, true));
        verify(ownershipQuery, never()).findCurrentOwnerFarmId(any());
        verify(farmAuthorization, never()).canAdministerFarm(any());
    }

    @Test
    void adminCanReadClosedOnlyHistoryWithoutCurrentOwner() {
        givenGoatExists();
        when(currentPrincipalQuery.requireCurrent()).thenReturn(principal("ROLE_ADMIN"));
        var closed = period(1L, 10L, "2026-01-01T00:00:00Z", "2026-02-01T00:00:00Z",
                OwnershipEntryType.MANUAL_IMPORT, OwnershipExitType.EXTERNAL_SALE);
        when(ownershipQuery.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(closed));

        assertThat(business.findOwnershipHistory(GOAT_ID).periods().get(0).current()).isFalse();
        verify(ownershipQuery, never()).findCurrentOwnerFarmId(any());
    }

    @Test
    void currentFarmOwnerReceivesTheCompleteHistoricalChain() {
        givenGoatExists();
        when(currentPrincipalQuery.requireCurrent()).thenReturn(principal("ROLE_FARM_OWNER"));
        when(ownershipQuery.findCurrentOwnerFarmId(GOAT_ID)).thenReturn(Optional.of(20L));
        when(farmAuthorization.canAdministerFarm(20L)).thenReturn(true);
        var old = period(1L, 10L, "2026-01-01T00:00:00Z", "2026-03-01T00:00:00Z",
                OwnershipEntryType.MANUAL_IMPORT, OwnershipExitType.TRANSFER_OUT);
        var current = period(2L, 20L, "2026-03-01T00:00:00Z", null,
                OwnershipEntryType.TRANSFER_IN, null);
        when(ownershipQuery.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(old, current));

        OwnershipHistory result = business.findOwnershipHistory(GOAT_ID);

        assertThat(result.periods()).hasSize(2);
        assertThat(result.periods().get(0).farmId()).isEqualTo(10L);
        assertThat(result.periods().get(1).farmId()).isEqualTo(20L);
        assertThat(result.periods().get(1).current()).isTrue();
    }

    @Test
    void historicalOnlyFarmOwnerIsDenied() {
        givenGoatExists();
        when(currentPrincipalQuery.requireCurrent()).thenReturn(principal("ROLE_FARM_OWNER"));
        when(ownershipQuery.findCurrentOwnerFarmId(GOAT_ID)).thenReturn(Optional.of(20L));
        when(farmAuthorization.canAdministerFarm(20L)).thenReturn(false);

        assertThatThrownBy(() -> business.findOwnershipHistory(GOAT_ID))
                .isInstanceOf(AuthorizationDeniedException.class);
        verify(ownershipQuery, never()).findOwnershipHistory(any());
    }

    @Test
    void unrelatedFarmOwnerIsDenied() {
        givenGoatExists();
        when(currentPrincipalQuery.requireCurrent()).thenReturn(principal("ROLE_FARM_OWNER"));
        when(ownershipQuery.findCurrentOwnerFarmId(GOAT_ID)).thenReturn(Optional.of(30L));
        when(farmAuthorization.canAdministerFarm(30L)).thenReturn(false);

        assertThatThrownBy(() -> business.findOwnershipHistory(GOAT_ID))
                .isInstanceOf(AuthorizationDeniedException.class);
        verify(ownershipQuery, never()).findOwnershipHistory(any());
    }

    @Test
    void operatorIsDeniedEvenWhenCurrentFarmIsAdministrableByOwner() {
        givenGoatExists();
        when(currentPrincipalQuery.requireCurrent()).thenReturn(principal("ROLE_OPERATOR"));
        when(ownershipQuery.findCurrentOwnerFarmId(GOAT_ID)).thenReturn(Optional.of(20L));
        when(farmAuthorization.canAdministerFarm(20L)).thenReturn(false);

        assertThatThrownBy(() -> business.findOwnershipHistory(GOAT_ID))
                .isInstanceOf(AuthorizationDeniedException.class);
        verify(farmAuthorization).canAdministerFarm(20L);
        verify(ownershipQuery, never()).findOwnershipHistory(any());
    }

    @Test
    void operatorCannotUseOperationalFarmAccessToReadOwnershipHistory() {
        givenGoatExists();
        when(currentPrincipalQuery.requireCurrent()).thenReturn(principal("ROLE_OPERATOR"));
        when(ownershipQuery.findCurrentOwnerFarmId(GOAT_ID)).thenReturn(Optional.of(20L));
        when(farmAuthorization.canAdministerFarm(20L)).thenReturn(false);

        assertThatThrownBy(() -> business.findOwnershipHistory(GOAT_ID))
                .isInstanceOf(AuthorizationDeniedException.class);
        verify(ownershipQuery, never()).findOwnershipHistory(any());
    }

    @Test
    void structuralGoatIdRejectsNonPositiveValuesBeforeUseCase() {
        assertThatThrownBy(() -> GoatId.of(0L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> GoatId.of(-1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nonAdminWithoutCurrentOwnerIsDenied() {
        givenGoatExists();
        when(currentPrincipalQuery.requireCurrent()).thenReturn(principal("ROLE_FARM_OWNER"));
        when(ownershipQuery.findCurrentOwnerFarmId(GOAT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> business.findOwnershipHistory(GOAT_ID))
                .isInstanceOf(AuthorizationDeniedException.class);
        verify(ownershipQuery, never()).findOwnershipHistory(any());
    }

    @Test
    void suppliedCanonicalOrderIsPreservedAndCurrentIsDerivedOnlyFromEnd() {
        givenGoatExists();
        when(currentPrincipalQuery.requireCurrent()).thenReturn(principal("ROLE_ADMIN"));
        var first = period(1L, 10L, "2026-03-01T00:00:00Z", "2026-04-01T00:00:00Z",
                OwnershipEntryType.MANUAL_IMPORT, OwnershipExitType.TRANSFER_OUT);
        var second = period(2L, 20L, "2026-01-01T00:00:00Z", null,
                OwnershipEntryType.TRANSFER_IN, null);
        when(ownershipQuery.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(first, second));

        var periods = business.findOwnershipHistory(GOAT_ID).periods();

        assertThat(periods.get(0).farmId()).isEqualTo(10L);
        assertThat(periods.get(1).farmId()).isEqualTo(20L);
        assertThat(periods.get(0).current()).isFalse();
        assertThat(periods.get(1).current()).isTrue();
    }

    @Test
    void emptyCanonicalHistoryFailsClosedWithoutProjectionFallback() {
        givenGoatExists();
        when(currentPrincipalQuery.requireCurrent()).thenReturn(principal("ROLE_ADMIN"));
        when(ownershipQuery.findOwnershipHistory(GOAT_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> business.findOwnershipHistory(GOAT_ID))
                .isInstanceOf(BusinessRuleException.class);
        verify(ownershipQuery, never()).findCurrentOwnerFarmId(any());
    }

    @Test
    void readModelDoesNotExposePersistenceIdentityVersionOrSource() {
        assertThat(Arrays.stream(OwnershipHistoryPeriod.class.getRecordComponents())
                .map(RecordComponent::getName)
                .toList())
                .doesNotContain("id", "version", "source");
    }

    @Test
    void currentFarmAuthorizationIsEvaluatedAgainstCanonicalCurrentOwnerOnly() {
        givenGoatExists();
        when(currentPrincipalQuery.requireCurrent()).thenReturn(principal("ROLE_FARM_OWNER"));
        when(ownershipQuery.findCurrentOwnerFarmId(GOAT_ID)).thenReturn(Optional.of(20L));
        when(farmAuthorization.canAdministerFarm(20L)).thenReturn(true);
        when(ownershipQuery.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                period(1L, 10L, "2026-01-01T00:00:00Z", "2026-03-01T00:00:00Z",
                        OwnershipEntryType.MANUAL_IMPORT, OwnershipExitType.TRANSFER_OUT),
                period(2L, 20L, "2026-03-01T00:00:00Z", null,
                        OwnershipEntryType.TRANSFER_IN, null)));

        business.findOwnershipHistory(GOAT_ID);

        verify(farmAuthorization).canAdministerFarm(20L);
        verify(farmAuthorization, never()).canAdministerFarm(10L);
    }

    @Test
    void concurrentOwnershipChangeFailsClosedWhenHistoryOpenFarmDiffersFromAuthorizedFarm() {
        givenGoatExists();
        when(currentPrincipalQuery.requireCurrent()).thenReturn(principal("ROLE_FARM_OWNER"));
        when(ownershipQuery.findCurrentOwnerFarmId(GOAT_ID)).thenReturn(Optional.of(10L));
        when(farmAuthorization.canAdministerFarm(10L)).thenReturn(true);
        when(ownershipQuery.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                period(1L, 10L, "2026-01-01T00:00:00Z", "2026-03-01T00:00:00Z",
                        OwnershipEntryType.MANUAL_IMPORT, OwnershipExitType.TRANSFER_OUT),
                period(2L, 20L, "2026-03-01T00:00:00Z", null,
                        OwnershipEntryType.TRANSFER_IN, null)));

        assertThatThrownBy(() -> business.findOwnershipHistory(GOAT_ID))
                .isInstanceOf(AuthorizationDeniedException.class);
    }

    @Test
    void concurrentClosureFailsClosedWhenAuthorizedOwnerIsAbsentFromReturnedHistory() {
        givenGoatExists();
        when(currentPrincipalQuery.requireCurrent()).thenReturn(principal("ROLE_FARM_OWNER"));
        when(ownershipQuery.findCurrentOwnerFarmId(GOAT_ID)).thenReturn(Optional.of(10L));
        when(farmAuthorization.canAdministerFarm(10L)).thenReturn(true);
        when(ownershipQuery.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                period(1L, 10L, "2026-01-01T00:00:00Z", "2026-03-01T00:00:00Z",
                        OwnershipEntryType.MANUAL_IMPORT, OwnershipExitType.EXTERNAL_SALE)));

        assertThatThrownBy(() -> business.findOwnershipHistory(GOAT_ID))
                .isInstanceOf(AuthorizationDeniedException.class);
    }

    private void givenGoatExists() {
        when(goatPersistence.findById(GOAT_ID)).thenReturn(Optional.of(goat()));
    }

    private Goat goat() {
        return Goat.rehydrate(GOAT_ID, RegistrationIdentity.fromTodAndToe("12345", "00001"),
                "Cabra", Gender.FEMEA, GoatBreed.SAANEN, "Branca", LocalDate.of(2025, 1, 1),
                GoatStatus.ATIVO, null, null, null, Category.PO, null, null, 20L, 7L,
                "Farm", "User");
    }

    private GoatOwnershipPeriod period(Long id, long farmId, String startedAt, String endedAt,
                                       OwnershipEntryType entryType, OwnershipExitType exitType) {
        return GoatOwnershipPeriod.rehydrate(id, GOAT_ID, farmId, Instant.parse(startedAt),
                endedAt == null ? null : Instant.parse(endedAt), entryType, exitType, "TEST");
    }

    private AuthenticatedPrincipal principal(String role) {
        return new AuthenticatedPrincipal(7L, "user@example.com", "User", Set.of(role));
    }
}
