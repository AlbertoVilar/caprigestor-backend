package com.devmaster.goatfarm.farm.application.ports.out;

import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.farm.application.model.ManagedFarmRecord;

/** Neutral persistence boundary for canonical managed-farm discovery. */
public interface ManagedFarmQueryPort {
    PageResult<ManagedFarmRecord> findAll(PageQuery pageQuery, String query);

    PageResult<ManagedFarmRecord> findByUserId(Long userId, PageQuery pageQuery, String query);
}
