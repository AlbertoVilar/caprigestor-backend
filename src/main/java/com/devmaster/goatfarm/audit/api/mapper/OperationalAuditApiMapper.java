package com.devmaster.goatfarm.audit.api.mapper;

import com.devmaster.goatfarm.audit.api.dto.OperationalAuditEntryDTO;
import com.devmaster.goatfarm.audit.business.bo.OperationalAuditEntryVO;
import org.springframework.stereotype.Component;

@Component
public class OperationalAuditApiMapper {

    public OperationalAuditEntryDTO toDTO(OperationalAuditEntryVO vo) {
        return new OperationalAuditEntryDTO(
                vo.id(),
                vo.goatRegistrationNumber(),
                vo.actionType().name(),
                vo.actionLabel(),
                vo.targetId(),
                vo.description(),
                vo.actorUserId(),
                vo.actorName(),
                vo.actorEmail(),
                vo.createdAt()
        );
    }
}
