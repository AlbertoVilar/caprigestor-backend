package com.devmaster.goatfarm.goatownership.persistence.repository;

import com.devmaster.goatfarm.goatownership.persistence.entity.GoatOwnershipPeriodEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface GoatOwnershipPeriodRepository extends JpaRepository<GoatOwnershipPeriodEntity, Long> {
    Optional<GoatOwnershipPeriodEntity> findByGoatIdAndEndedAtIsNull(Long goatId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from GoatOwnershipPeriodEntity p where p.goatId = :goatId and p.endedAt is null")
    
    Optional<GoatOwnershipPeriodEntity> findOpenForUpdateByGoatId(Long goatId);

    List<GoatOwnershipPeriodEntity> findByGoatIdOrderByStartedAtAscIdAsc(Long goatId);
    List<GoatOwnershipPeriodEntity> findByFarmIdOrderByGoatIdAscStartedAtAscIdAsc(Long farmId);
    List<GoatOwnershipPeriodEntity> findByGoatIdInOrderByGoatIdAscStartedAtAscIdAsc(List<Long> goatIds);

    @Query("""
            select case when count(p) > 0 then true else false end
            from GoatOwnershipPeriodEntity p
            where p.goatId = :goatId
              and p.farmId = :farmId
              and p.startedAt <= :instant
              and (p.endedAt is null or :instant < p.endedAt)
            """)
    boolean existsOwnedByFarmAt(@Param("goatId") Long goatId,
                                @Param("farmId") Long farmId,
                                @Param("instant") Instant instant);
}
