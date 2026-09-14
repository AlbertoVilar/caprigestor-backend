package com.devmaster.goatfarm.goatownership.persistence.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import com.devmaster.goatfarm.goat.persistence.entity.GoatEntity;

import java.util.Optional;

/** Infrastructure-only row lock for the canonical Goat ownership lock order. */
public interface GoatOwnershipLockRepository extends Repository<GoatEntity, Long> {
    @Query(value = "select id from cabras where id = :goatId for update", nativeQuery = true)
    Optional<Long> lockGoatById(@Param("goatId") Long goatId);
}
