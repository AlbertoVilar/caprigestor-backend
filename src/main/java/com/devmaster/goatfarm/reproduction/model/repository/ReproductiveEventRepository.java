package com.devmaster.goatfarm.reproduction.model.repository;

import com.devmaster.goatfarm.reproduction.model.entity.ReproductiveEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReproductiveEventRepository extends JpaRepository<ReproductiveEvent, Long> {
    Page<ReproductiveEvent> findAllByFarmIdAndGoatId(Long farmId, String goatId, Pageable pageable);
}
