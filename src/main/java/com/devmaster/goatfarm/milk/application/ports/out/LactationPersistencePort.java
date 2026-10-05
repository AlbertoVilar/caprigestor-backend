package com.devmaster.goatfarm.milk.application.ports.out;

import com.devmaster.goatfarm.milk.domain.Lactation;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface LactationPersistencePort {

    Lactation save(Lactation lactation);

    Optional<Lactation> findActiveByFarmIdAndGoatId(Long farmId, String goatId);

    Optional<Lactation> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId);

    Optional<Lactation> findByIdAndGoatTechnicalId(Long id, GoatId goatId);

    PageResult<Lactation> findAllByFarmIdAndGoatId(Long farmId, String goatId, PageQuery pageQuery);

    Optional<Lactation> findLatestByFarmIdAndGoatId(Long farmId, String goatId);

    Optional<Lactation> findActiveByGoatTechnicalId(GoatId goatId);

    Optional<Lactation> findLatestByGoatTechnicalId(GoatId goatId);

    List<Lactation> findAllActiveByFarmId(Long farmId);
}
