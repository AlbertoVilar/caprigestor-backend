package com.devmaster.goatfarm.commercial.persistence.repository;

import com.devmaster.goatfarm.commercial.persistence.entity.AnimalSaleReversal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AnimalSaleReversalRepository extends JpaRepository<AnimalSaleReversal, Long> {
    Optional<AnimalSaleReversal> findBySale_Id(Long saleId);
    List<AnimalSaleReversal> findBySale_IdIn(Collection<Long> saleIds);
}
