package com.devmaster.goatfarm.farm.persistence.repository;

import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GoatFarmRepository extends JpaRepository<GoatFarm, Long> {

    boolean existsByName(String name);

    boolean existsByTod(String tod);

    @Query("SELECT gf FROM GoatFarm gf WHERE LOWER(gf.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<GoatFarm> searchGoatFarmByName(@Param("name") String name, Pageable pageable);

    // NOVO: Busca otimizada para verificação de posse
    Optional<GoatFarm> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT gf.user.id FROM GoatFarm gf WHERE gf.id = :id")
    Optional<Long> findOwnerIdById(@Param("id") Long id);

    Optional<GoatFarm> findByAddressId(Long addressId);

    @Query("SELECT gf FROM GoatFarm gf LEFT JOIN FETCH gf.address LEFT JOIN FETCH gf.phones WHERE gf.id = :id")
    Optional<GoatFarm> findByIdWithDetails(@Param("id") Long id);
}
