package com.devmaster.goatfarm.farm.application.ports.in;

import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.farm.business.bo.ManagedFarmSummaryVO;

/** Resolves the farms the current authenticated user can manage. */
public interface ManagedFarmQueryUseCase {
    PageResult<ManagedFarmSummaryVO> findManagedFarms(PageQuery pageQuery, String query);
}
