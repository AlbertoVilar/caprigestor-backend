package com.devmaster.goatfarm.reproduction.persistence.repository;

import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.persistence.entity.PregnancyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PregnancyRepository extends JpaRepository<PregnancyEntity, Long> {
    List<PregnancyEntity> findByFarmIdAndGoatIdAndStatusOrderByBreedingDateDescIdDesc(Long farmId, String goatId, PregnancyStatus status);
    List<PregnancyEntity> findByFarmIdAndGoatTechnicalIdAndStatusOrderByBreedingDateDescIdDesc(Long farmId, Long goatTechnicalId, PregnancyStatus status);
    Optional<PregnancyEntity> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId);
    Optional<PregnancyEntity> findByIdAndFarmIdAndGoatTechnicalId(Long id, Long farmId, Long goatTechnicalId);
    Optional<PregnancyEntity> findByFarmIdAndId(Long farmId, Long id);
    Optional<PregnancyEntity> findByFarmIdAndCoverageEventId(Long farmId, Long coverageEventId);
    boolean existsByFarmIdAndCoverageEventId(Long farmId, Long coverageEventId);
    Page<PregnancyEntity> findAllByFarmIdAndGoatIdOrderByBreedingDateDescIdDesc(Long farmId, String goatId, Pageable pageable);
    Page<PregnancyEntity> findAllByFarmIdAndGoatTechnicalIdOrderByBreedingDateDescIdDesc(Long farmId, Long goatTechnicalId, Pageable pageable);
    Page<PregnancyEntity> findByFarmIdAndStatusAndExpectedDueDateIsNotNullAndExpectedDueDateLessThanEqualOrderByExpectedDueDateAscIdAsc(
            Long farmId,
            PregnancyStatus status,
            LocalDate referenceDate,
            Pageable pageable
    );

    @Query("""
            select max(p.closedAt)
            from Pregnancy p
            where p.farmId = :farmId
              and p.goatId = :goatId
              and p.closeReason = com.devmaster.goatfarm.reproduction.enums.PregnancyCloseReason.BIRTH
              and p.closedAt is not null
            """)
    Optional<LocalDate> findLatestBirthCloseDate(
            @Param("farmId") Long farmId,
            @Param("goatId") String goatId
    );

    @Query("""
            select max(p.closedAt)
            from Pregnancy p
            where p.farmId = :farmId
              and p.goatTechnicalId = :goatTechnicalId
              and p.closeReason = com.devmaster.goatfarm.reproduction.enums.PregnancyCloseReason.BIRTH
              and p.closedAt is not null
            """)
    Optional<LocalDate> findLatestBirthCloseDateByTechnicalId(
            @Param("farmId") Long farmId,
            @Param("goatTechnicalId") Long goatTechnicalId
    );
}
