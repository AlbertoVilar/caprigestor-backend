package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipTransferDirection;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipTransferPageQuery;
import com.devmaster.goatfarm.goatownership.application.ports.out.OwnershipTransferPersistencePort;
import com.devmaster.goatfarm.goatownership.application.ports.out.OwnershipTransferQueryPort;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
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
class GoatOwnershipTransferQueryBusinessTest {
    @Mock OwnershipTransferPersistencePort transferPersistence;
    @Mock OwnershipTransferQueryPort transferQuery;
    @Mock GoatFarmPersistencePort farmPersistence;
    @Mock FarmAuthorizationUseCase farmAuthorization;
    @Mock CurrentPrincipalQueryUseCase currentPrincipalQuery;

    private GoatOwnershipTransferQueryBusiness business;

    @BeforeEach
    void setUp() {
        business = new GoatOwnershipTransferQueryBusiness(transferPersistence, transferQuery,
                farmPersistence, farmAuthorization, currentPrincipalQuery);
    }

    @Test
    void adminCanReadAnyInternalTransfer() {
        var transfer = transfer(1L, 10L, 20L, OwnershipTransferStatus.REQUESTED);
        when(transferPersistence.findById(1L)).thenReturn(Optional.of(transfer));
        when(currentPrincipalQuery.requireCurrent()).thenReturn(principal("ROLE_ADMIN"));

        assertThat(business.findAuthorizedById(1L)).isSameAs(transfer);
        verify(farmAuthorization, never()).canAdministerFarm(any(Long.class));
    }

    @Test
    void sourceOrTargetOwnerCanReadButUnrelatedOwnerAndOperatorCannot() {
        var transfer = transfer(1L, 10L, 20L, OwnershipTransferStatus.REQUESTED);
        when(transferPersistence.findById(1L)).thenReturn(Optional.of(transfer));
        when(currentPrincipalQuery.requireCurrent()).thenReturn(principal("ROLE_FARM_OWNER"));
        when(farmAuthorization.canAdministerFarm(10L)).thenReturn(true);
        assertThat(business.findAuthorizedById(1L)).isSameAs(transfer);

        when(farmAuthorization.canAdministerFarm(10L)).thenReturn(false);
        when(farmAuthorization.canAdministerFarm(20L)).thenReturn(true);
        assertThat(business.findAuthorizedById(1L)).isSameAs(transfer);

        when(farmAuthorization.canAdministerFarm(20L)).thenReturn(false);
        assertThatThrownBy(() -> business.findAuthorizedById(1L))
                .isInstanceOf(AuthorizationDeniedException.class);

        when(currentPrincipalQuery.requireCurrent()).thenReturn(principal("ROLE_OPERATOR"));
        assertThatThrownBy(() -> business.findAuthorizedById(1L))
                .isInstanceOf(AuthorizationDeniedException.class);
    }

    @Test
    void listRequiresAdministrationAndDelegatesDirectionAndStatus() {
        when(farmAuthorization.canAdministerFarm(10L)).thenReturn(true);
        when(farmPersistence.findById(10L)).thenReturn(Optional.of(farm(10L)));
        var page = new PageResult<>(List.of(transfer(1L, 30L, 10L, OwnershipTransferStatus.REQUESTED)), 1, 0, 20);
        when(transferQuery.findForFarm(10L, OwnershipTransferDirection.INCOMING,
                OwnershipTransferStatus.REQUESTED, new OwnershipTransferPageQuery(0, 20))).thenReturn(page);

        assertThat(business.listForFarm(10L, OwnershipTransferDirection.INCOMING,
                OwnershipTransferStatus.REQUESTED, new OwnershipTransferPageQuery(0, 20))).isSameAs(page);
        verify(transferQuery).findForFarm(10L, OwnershipTransferDirection.INCOMING,
                OwnershipTransferStatus.REQUESTED, new OwnershipTransferPageQuery(0, 20));
    }

    @Test
    void invalidPaginationAndNonInternalTransferAreRejected() {
        assertThatThrownBy(() -> business.listForFarm(10L, OwnershipTransferDirection.OUTGOING,
                null, new OwnershipTransferPageQuery(0, 101)))
                .isInstanceOf(com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException.class);
        var sale = OwnershipTransfer.rehydrate(2L, GoatId.of(42L), 10L, 20L,
                OwnershipTransferKind.INTERNAL_SALE, OwnershipTransferStatus.REQUESTED,
                "sale", "key", Instant.parse("2026-01-01T00:00:00Z"),
                null, null, null, null, 7L, null, null, 8L);
        when(transferPersistence.findById(2L)).thenReturn(Optional.of(sale));
        assertThatThrownBy(() -> business.findAuthorizedById(2L))
                .isInstanceOf(com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException.class);
    }

    private FarmRecord farm(long id) {
        return new FarmRecord(id, "Farm " + id, "TOD" + id, null, null, null, List.of(), Instant.EPOCH, Instant.EPOCH, 0);
    }

    private OwnershipTransfer transfer(long id, long source, long target, OwnershipTransferStatus status) {
        return OwnershipTransfer.rehydrate(id, GoatId.of(42L), source, target,
                OwnershipTransferKind.INTERNAL_TRANSFER, status, "move", "key-" + id,
                Instant.parse("2026-01-01T00:00:00Z"), null, null, null, null, 7L, null, null, null);
    }

    private AuthenticatedPrincipal principal(String role) {
        return new AuthenticatedPrincipal(7L, "user@example.com", "User", Set.of(role));
    }
}
