package com.devmaster.goatfarm.commercial.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnimalSaleReversalRequestDTO(
        @NotBlank @Size(max = 500) String reason
) {
}
