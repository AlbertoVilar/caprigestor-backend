package com.devmaster.goatfarm.reproduction.persistence.repository;

import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEvent;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ReproductiveEventRepository extends JpaRepository<ReproductiveEvent, Long> {

    Page<ReproductiveEvent> findAllByFarmIdAndGoatIdOrderByEventDateDescIdDesc(Long farmId, String goatId, Pageable pageable);

    Optional<ReproductiveEvent> findTopByFarmIdAndGoatIdAndEventTypeAndEventDateLessThanEqualOrderByEventDateDescIdDesc(
            Long farmId,
            String goatId,
            ReproductiveEventType eventType,
            LocalDate eventDate
    );


}
