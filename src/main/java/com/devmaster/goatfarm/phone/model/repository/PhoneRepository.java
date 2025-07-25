package com.devmaster.goatfarm.phone.model.repository;

import com.devmaster.goatfarm.phone.model.entity.Phone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PhoneRepository extends JpaRepository<Phone, Long> {

    boolean existsByDddAndNumber(String ddd, String number);

    @Query("SELECT p FROM Phone p WHERE p.ddd = :ddd AND p.number = :number")
    Optional<Phone> findByDddAndNumber(@Param("ddd") String ddd, @Param("number") String number);

}
