package com.devmaster.goatfarm.audit.application.model;

import com.devmaster.goatfarm.audit.enums.OperationalAuditActionType;

import java.time.LocalDateTime;

/**
 * Technology-neutral audit snapshot exchanged by the application and its
 * persistence adapter. It deliberately contains values captured at event time
 * rather than references to mutable domain or persistence objects.
 */
public record OperationalAuditRecord(
        Long id,
        Long farmId,
        Long goatTechnicalId,
        String goatRegistrationNumber,
        OperationalAuditActionType actionType,
        String targetId,
        Long actorUserId,
        String actorName,
        String actorEmail,
        String description,
        LocalDateTime createdAt
) {
}
