package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementDirection;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementItem;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementKind;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementPageQuery;
import com.devmaster.goatfarm.goatownership.application.ports.out.OwnershipMovementQueryPort;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnershipMovementQueryBusinessTest {
    @Mock OwnershipMovementQueryPort movementQuery;
    @Mock GoatFarmPersistencePort farmPersistence;
    @Mock FarmAuthorizationUseCase farmAuthorization;

    private OwnershipMovementQueryBusiness business;

    @BeforeEach
    void setUp() {
        business = new OwnershipMovementQueryBusiness(movementQuery, farmPersistence, farmAuthorization);
    }

    @Test
    void missingFarmIsRejectedBeforeAuthorizationOrQuery() {
        when(farmPersistence.findById(19L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> business.listForFarm(19L, OwnershipMovementDirection.OUTGOING,
                null, null, new OwnershipMovementPageQuery(0, 20)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(farmAuthorization, never()).canAdministerFarm(19L);
        verify(movementQuery, never()).findForFarm(19L, OwnershipMovementDirection.OUTGOING,
                null, null, new OwnershipMovementPageQuery(0, 20));
    }

    @Test
    void unauthorizedOperatorCannotReadMovementOrSalePaymentDetails() {
        when(farmPersistence.findById(19L)).thenReturn(Optional.of(farm(19L)));
        when(farmAuthorization.canAdministerFarm(19L)).thenReturn(false);

        assertThatThrownBy(() -> business.listForFarm(19L, OwnershipMovementDirection.OUTGOING,
                null, null, new OwnershipMovementPageQuery(0, 20)))
                .isInstanceOf(AuthorizationDeniedException.class);

        verify(movementQuery, never()).findForFarm(19L, OwnershipMovementDirection.OUTGOING,
                null, null, new OwnershipMovementPageQuery(0, 20));
    }

    @Test
    void authorizedFarmAdministratorReceivesFilteredReadProjection() {
        var pageQuery = new OwnershipMovementPageQuery(0, 20);
        when(farmPersistence.findById(19L)).thenReturn(Optional.of(farm(19L)));
        when(farmAuthorization.canAdministerFarm(19L)).thenReturn(true);
        when(movementQuery.findForFarm(19L, OwnershipMovementDirection.OUTGOING,
                OwnershipTransferStatus.COMPLETED, OwnershipMovementKind.INTERNAL_SALE, pageQuery))
                .thenReturn(new PageResult<OwnershipMovementItem>(java.util.List.of(), 0, 0, 20));

        business.listForFarm(19L, OwnershipMovementDirection.OUTGOING,
                OwnershipTransferStatus.COMPLETED, OwnershipMovementKind.INTERNAL_SALE, pageQuery);

        verify(movementQuery).findForFarm(19L, OwnershipMovementDirection.OUTGOING,
                OwnershipTransferStatus.COMPLETED, OwnershipMovementKind.INTERNAL_SALE, pageQuery);
    }

    private FarmRecord farm(long id) {
        return new FarmRecord(id, "Test Farm", "TEST", null, null, null, java.util.List.of(), null, null, 0);
    }
}
