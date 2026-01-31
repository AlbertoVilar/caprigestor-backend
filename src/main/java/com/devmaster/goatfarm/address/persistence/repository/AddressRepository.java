package com.devmaster.goatfarm.address.persistence.repository;

import com.devmaster.goatfarm.address.persistence.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("SELECT a FROM Address a WHERE a.street = :street AND a.neighborhood = :neighborhood AND a.city = :city AND a.state = :state AND a.zipCode = :zipCode")
    Optional<Address> searchExactAddress(@Param("street") String street,
                                       @Param("neighborhood") String neighborhood,
                                       @Param("city") String city,
                                       @Param("state") String state,
                                       @Param("zipCode") String zipCode);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM endereco WHERE id IN (SELECT c.address_id FROM capril c WHERE c.user_id != :adminId AND c.address_id IS NOT NULL)")
    void deleteAddressesFromOtherUsers(@Param("adminId") Long adminId);

    // Busca endereço através da fazenda
    @Query("SELECT gf.address FROM GoatFarm gf WHERE gf.id = :farmId AND gf.address.id = :addressId")
    Optional<Address> findByIdAndFarmId(@Param("addressId") Long addressId, @Param("farmId") Long farmId);
}
