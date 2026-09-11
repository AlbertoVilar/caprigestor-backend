package com.devmaster.goatfarm.audit.business.bo;

import com.devmaster.goatfarm.audit.enums.OperationalAuditActionType;

public record OperationalAuditRecordVO(
        Long farmId,
        Long goatTechnicalId,
        String goatRegistrationNumber,
        OperationalAuditActionType actionType,
        String targetId,
        String description
) {
    public OperationalAuditRecordVO(Long farmId, String goatRegistrationNumber,
                                    OperationalAuditActionType actionType, String targetId,
                                    String description) {
        this(farmId, null, goatRegistrationNumber, actionType, targetId, description);
    }
}
