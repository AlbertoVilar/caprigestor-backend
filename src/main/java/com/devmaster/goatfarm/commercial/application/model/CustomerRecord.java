package com.devmaster.goatfarm.commercial.application.model;

import java.time.LocalDateTime;

public record CustomerRecord(
        Long id,
        Long farmId,
        String name,
        String document,
        String phone,
        String email,
        String notes,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
