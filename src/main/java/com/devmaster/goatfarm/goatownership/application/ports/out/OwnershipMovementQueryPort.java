package com.devmaster.goatfarm.goatownership.application.ports.out;

import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementDirection;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementItem;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementKind;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementPageQuery;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;

/** Persistence projection port for the ownership movement read model. */
public interface OwnershipMovementQueryPort {
    PageResult<OwnershipMovementItem> findForFarm(Long farmId,
                                                   OwnershipMovementDirection direction,
                                                   OwnershipTransferStatus status,
                                                   OwnershipMovementKind kind,
                                                   OwnershipMovementPageQuery pageQuery);
}
