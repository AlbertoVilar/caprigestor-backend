package com.devmaster.goatfarm.audit.business.bo;

import com.devmaster.goatfarm.audit.enums.OperationalAuditActionType;

import java.time.LocalDateTime;

public record OperationalAuditEntryVO(
        Long id,
        Long goatTechnicalId,
        String goatRegistrationNumber,
        OperationalAuditActionType actionType,
        String actionLabel,
        String targetId,
        String description,
        Long actorUserId,
        String actorName,
        String actorEmail,
        LocalDateTime createdAt
) {
    public OperationalAuditEntryVO(Long id, String goatRegistrationNumber,
                                   OperationalAuditActionType actionType, String actionLabel,
                                   String targetId, String description, Long actorUserId,
                                   String actorName, String actorEmail, LocalDateTime createdAt) {
        this(id, null, goatRegistrationNumber, actionType, actionLabel, targetId, description,
                actorUserId, actorName, actorEmail, createdAt);
    }
}
