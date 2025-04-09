package com.devmaster.goatfarm.phone.model.repository;

import com.devmaster.goatfarm.phone.model.entity.Phone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneRepository extends JpaRepository<Phone, Long> {
}
