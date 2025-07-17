package com.devmaster.goatfarm.phone.model.repository;

import com.devmaster.goatfarm.phone.model.entity.Phone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhoneRepository extends JpaRepository<Phone, Long> {

    boolean existsByDddAndNumber(String ddd, String number);

}
