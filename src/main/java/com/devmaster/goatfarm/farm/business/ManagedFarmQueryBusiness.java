package com.devmaster.goatfarm.farm.business;

import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.farm.application.model.ManagedFarmRecord;
import com.devmaster.goatfarm.farm.application.ports.in.ManagedFarmQueryUseCase;
import com.devmaster.goatfarm.farm.application.ports.out.ManagedFarmQueryPort;
import com.devmaster.goatfarm.farm.business.bo.ManagedFarmSummaryVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for role-aware managed-farm discovery. */
@Service
public class ManagedFarmQueryBusiness implements ManagedFarmQueryUseCase {
    private final ManagedFarmQueryPort queryPort;
    private final CurrentPrincipalQueryUseCase principalQuery;

    public ManagedFarmQueryBusiness(ManagedFarmQueryPort queryPort,
                                    CurrentPrincipalQueryUseCase principalQuery) {
        this.queryPort = queryPort;
        this.principalQuery = principalQuery;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ManagedFarmSummaryVO> findManagedFarms(PageQuery pageQuery, String query) {
        AuthenticatedPrincipal principal = principalQuery.requireCurrent();
        String normalizedQuery = query == null ? "" : query.trim();
        PageResult<ManagedFarmRecord> page = principal.hasAuthority("ROLE_ADMIN")
                ? queryPort.findAll(pageQuery, normalizedQuery)
                : queryPort.findByUserId(principal.id(), pageQuery, normalizedQuery);
        return page.map(record -> new ManagedFarmSummaryVO(record.id(), record.name(), record.tod(), record.logoUrl()));
    }
}
