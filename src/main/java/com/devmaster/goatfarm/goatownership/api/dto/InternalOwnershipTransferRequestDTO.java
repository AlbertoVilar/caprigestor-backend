package com.devmaster.goatfarm.goatownership.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** HTTP request for an internal transfer; source ownership is never client supplied. */
public record InternalOwnershipTransferRequestDTO(
        @NotNull @Positive Long goatId,
        @NotNull @Positive Long targetFarmId,
        @NotBlank @Size(max = 1000) String reason,
        @NotBlank @Size(max = 255) String idempotencyKey
) {
}
