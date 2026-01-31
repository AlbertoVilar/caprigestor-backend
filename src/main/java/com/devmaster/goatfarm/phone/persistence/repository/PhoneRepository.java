package com.devmaster.goatfarm.phone.persistence.repository;

import com.devmaster.goatfarm.phone.persistence.entity.Phone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PhoneRepository extends JpaRepository<Phone, Long> {
    boolean existsByDddAndNumber(String ddd, String number);

    Optional<Phone> findByDddAndNumber(String ddd, String number);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM telefone WHERE goat_farm_id IN (SELECT c.id FROM capril c WHERE c.user_id != :adminId)")
    void deletePhonesFromOtherUsers(@Param("adminId") Long adminId);

    // NOVO: Busca por ID do telefone e ID da fazenda
    Optional<Phone> findByIdAndGoatFarmId(Long id, Long goatFarmId);

    // NOVO: Busca todos os telefones de uma fazenda
    List<Phone> findAllByGoatFarmId(Long goatFarmId);

    long countByGoatFarmId(Long goatFarmId);
}
