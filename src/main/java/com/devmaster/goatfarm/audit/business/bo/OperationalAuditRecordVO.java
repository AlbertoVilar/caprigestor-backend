package com.devmaster.goatfarm.audit.business.bo;

import com.devmaster.goatfarm.audit.enums.OperationalAuditActionType;

public record OperationalAuditRecordVO(
        Long farmId,
        String goatRegistrationNumber,
        OperationalAuditActionType actionType,
        String targetId,
        String description
) {
}
