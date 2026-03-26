package com.devmaster.goatfarm.commercial.persistence.repository;

import com.devmaster.goatfarm.commercial.persistence.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByIdAndFarm_Id(Long id, Long farmId);

    List<Customer> findByFarm_IdOrderByNameAsc(Long farmId);

    long countByFarm_Id(Long farmId);
}
