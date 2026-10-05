package com.devmaster.goatfarm.goatownership.application.ports.in;

import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementDirection;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementItem;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementKind;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementPageQuery;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;

/** Authorized read boundary for farm-scoped ownership movement history. */
public interface OwnershipMovementQueryUseCase {
    PageResult<OwnershipMovementItem> listForFarm(Long farmId,
                                                   OwnershipMovementDirection direction,
                                                   OwnershipTransferStatus status,
                                                   OwnershipMovementKind kind,
                                                   OwnershipMovementPageQuery pageQuery);
}
