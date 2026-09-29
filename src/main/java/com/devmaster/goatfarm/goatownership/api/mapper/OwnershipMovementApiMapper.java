package com.devmaster.goatfarm.goatownership.api.mapper;

import com.devmaster.goatfarm.goatownership.api.dto.OwnershipMovementResponseDTO;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementItem;
import org.springframework.stereotype.Component;

/** Maps the neutral ownership movement projection to its HTTP representation. */
@Component
public class OwnershipMovementApiMapper {
    public OwnershipMovementResponseDTO toResponse(OwnershipMovementItem item) {
        return new OwnershipMovementResponseDTO(item.movementId(), item.goatId(), item.sourceFarmId(),
                item.targetFarmId(), item.movementKind(), item.status(), item.direction(), item.reason(),
                item.requestedAt(), item.acceptedAt(), item.effectiveAt(), item.completedAt(),
                item.cancelledAt(), item.realized(), item.saleId(), item.saleDate(), item.amount(),
                item.paymentStatus(), item.paymentDate());
    }
}
