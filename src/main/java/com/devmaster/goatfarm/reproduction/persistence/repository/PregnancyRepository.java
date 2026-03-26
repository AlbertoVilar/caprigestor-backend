package com.devmaster.goatfarm.reproduction.persistence.repository;

import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.persistence.entity.Pregnancy;
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
public interface PregnancyRepository extends JpaRepository<Pregnancy, Long> {
    List<Pregnancy> findByFarmIdAndGoatIdAndStatusOrderByBreedingDateDescIdDesc(Long farmId, String goatId, PregnancyStatus status);
    Optional<Pregnancy> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId);
    Optional<Pregnancy> findByFarmIdAndId(Long farmId, Long id);
    Optional<Pregnancy> findByFarmIdAndCoverageEventId(Long farmId, Long coverageEventId);
    boolean existsByFarmIdAndCoverageEventId(Long farmId, Long coverageEventId);
    Page<Pregnancy> findAllByFarmIdAndGoatIdOrderByBreedingDateDescIdDesc(Long farmId, String goatId, Pageable pageable);

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
}
