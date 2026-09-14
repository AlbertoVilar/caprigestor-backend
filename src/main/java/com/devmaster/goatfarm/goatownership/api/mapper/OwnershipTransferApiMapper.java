package com.devmaster.goatfarm.goatownership.api.mapper;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.api.dto.InternalOwnershipTransferRequestDTO;
import com.devmaster.goatfarm.goatownership.api.dto.OwnershipTransferResponseDTO;
import com.devmaster.goatfarm.goatownership.application.model.InternalOwnershipTransferRequest;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer;
import org.springframework.stereotype.Component;

/** Maps HTTP DTOs at the edge of the ownership application boundary. */
@Component
public class OwnershipTransferApiMapper {
    public InternalOwnershipTransferRequest toApplication(InternalOwnershipTransferRequestDTO dto) {
        return new InternalOwnershipTransferRequest(
                GoatId.of(dto.goatId()), dto.targetFarmId(), dto.reason(), dto.idempotencyKey());
    }

    public OwnershipTransferResponseDTO toResponse(OwnershipTransfer transfer) {
        return new OwnershipTransferResponseDTO(
                transfer.id(), transfer.goatId().value(), transfer.sourceFarmId(), transfer.targetFarmId(),
                transfer.kind(), transfer.status(), transfer.reason(), transfer.requestedAt(),
                transfer.acceptedAt(), transfer.effectiveAt(), transfer.completedAt(), transfer.cancelledAt());
    }
}
