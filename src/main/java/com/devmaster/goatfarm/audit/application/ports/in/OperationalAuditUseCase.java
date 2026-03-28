package com.devmaster.goatfarm.audit.application.ports.in;

import com.devmaster.goatfarm.audit.business.bo.OperationalAuditEntryVO;
import com.devmaster.goatfarm.audit.business.bo.OperationalAuditRecordVO;

import java.util.List;

public interface OperationalAuditUseCase {

    void record(OperationalAuditRecordVO recordVO);

    List<OperationalAuditEntryVO> listEntries(Long farmId, String goatId, int limit);
}
