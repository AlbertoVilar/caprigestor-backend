package com.devmaster.goatfarm.farm.model.repository;

import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface GoatFarmRepository extends JpaRepository<GoatFarm, Long> {

    boolean existsByName(String name);

    boolean existsByTod(String tod);

    @Query("SELECT gf FROM GoatFarm gf WHERE LOWER(gf.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<GoatFarm> searchGoatFarmByName(@Param("name") String name, Pageable pageable);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM capril WHERE user_id != :adminId")
    void deleteGoatFarmsFromOtherUsers(@Param("adminId") Long adminId);

    // NOVO: Busca otimizada para verificação de posse
    Optional<GoatFarm> findByIdAndUserId(Long id, Long userId);

    Optional<GoatFarm> findByAddressId(Long addressId);
}
