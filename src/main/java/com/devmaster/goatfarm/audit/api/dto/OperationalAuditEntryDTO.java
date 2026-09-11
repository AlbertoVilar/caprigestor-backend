package com.devmaster.goatfarm.audit.api.dto;

import java.time.LocalDateTime;

public record OperationalAuditEntryDTO(
        Long id,
        Long goatTechnicalId,
        String goatRegistrationNumber,
        String actionType,
        String actionLabel,
        String targetId,
        String description,
        Long actorUserId,
        String actorName,
        String actorEmail,
        LocalDateTime createdAt
) {
}
