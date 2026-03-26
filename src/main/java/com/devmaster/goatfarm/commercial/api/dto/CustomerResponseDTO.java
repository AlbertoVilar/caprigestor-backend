package com.devmaster.goatfarm.commercial.api.dto;

public record CustomerResponseDTO(
        Long id,
        String name,
        String document,
        String phone,
        String email,
        String notes,
        boolean active
) {
}
