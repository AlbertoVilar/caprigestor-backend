package com.devmaster.goatfarm.goatownership.persistence.repository;

import com.devmaster.goatfarm.goat.persistence.entity.GoatEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Infrastructure-only compare-and-set update for the legacy farm projection. */
public interface GoatCurrentOwnerProjectionRepository extends Repository<GoatEntity, Long> {
    @Modifying
    @Query(value = """
            update cabras
               set capril_id = :targetFarmId
             where id = :goatId
               and capril_id = :expectedSourceFarmId
            """, nativeQuery = true)
    int moveFromTo(@Param("goatId") Long goatId,
                   @Param("expectedSourceFarmId") Long expectedSourceFarmId,
                   @Param("targetFarmId") Long targetFarmId);
}
