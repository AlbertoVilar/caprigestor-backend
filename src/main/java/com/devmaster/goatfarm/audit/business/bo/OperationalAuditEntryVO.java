package com.devmaster.goatfarm.audit.business.bo;

import com.devmaster.goatfarm.audit.enums.OperationalAuditActionType;

import java.time.LocalDateTime;

public record OperationalAuditEntryVO(
        Long id,
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
}
