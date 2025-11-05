package com.devmaster.goatfarm.phone.model.repository;

import com.devmaster.goatfarm.phone.model.entity.Phone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PhoneRepository extends JpaRepository<Phone, Long> {
    boolean existsByDddAndNumber(String ddd, String number);

    Optional<Phone> findByDddAndNumber(String ddd, String number);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM telefone WHERE goat_farm_id IN (SELECT c.id FROM capril c WHERE c.user_id != :adminId)")
    void deletePhonesFromOtherUsers(@Param("adminId") Long adminId);
}
