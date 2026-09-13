package com.devmaster.goatfarm.reproduction.application.ports.out;

import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.domain.Pregnancy;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface PregnancyPersistencePort {
    Pregnancy save(Pregnancy entity);
    Optional<Pregnancy> findActiveByFarmIdAndGoatId(Long farmId, String goatId);
    Optional<Pregnancy> findByIdAndFarmIdAndGoatId(Long pregnancyId, Long farmId, String goatId);
    Optional<Pregnancy> findByFarmIdAndId(Long farmId, Long pregnancyId);
    Optional<Pregnancy> findByFarmIdAndCoverageEventId(Long farmId, Long coverageEventId);
    boolean existsByFarmIdAndCoverageEventId(Long farmId, Long coverageEventId);
    Optional<LocalDate> findLatestBirthCloseDate(Long farmId, String goatId);
    PageResult<Pregnancy> findAllByFarmIdAndGoatId(Long farmId, String goatId, PageQuery pageQuery);
    List<Pregnancy> findAllActiveByFarmIdAndGoatIdOrdered(Long farmId, String goatId);
    PageResult<Pregnancy> findActiveWithDueDateOnOrBefore(Long farmId, LocalDate referenceDate, PageQuery pageQuery);
}
