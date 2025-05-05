package com.devmaster.goatfarm.address.model.repository;

import com.devmaster.goatfarm.address.model.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
}
