package com.devmaster.goatfarm.address.model.repository;

import com.devmaster.goatfarm.address.model.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("SELECT a FROM Address a WHERE " +
            "LOWER(a.street) = LOWER(:street) AND " +
            "LOWER(a.neighborhood) = LOWER(:neighborhood) AND " +
            "LOWER(a.city) = LOWER(:city) AND " +
            "LOWER(a.state) = LOWER(:state) AND " +
            "a.postalCode = :postalCode")
    Optional<Address> searchExactAddress(
            @Param("street") String street,
            @Param("neighborhood") String neighborhood,
            @Param("city") String city,
            @Param("state") String state,
            @Param("postalCode") String postalCode
    );
}
