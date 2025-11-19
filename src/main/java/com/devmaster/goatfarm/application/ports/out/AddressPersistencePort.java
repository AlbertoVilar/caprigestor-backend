package com.devmaster.goatfarm.application.ports.out;

import com.devmaster.goatfarm.address.model.entity.Address;

import java.util.Optional;

/**
 * Porta de saída para persistência de endereços
 */
public interface AddressPersistencePort {

    Address save(Address address);

    Optional<Address> findById(Long id);

    Optional<Address> findByIdAndFarmId(Long addressId, Long farmId);

    void deleteById(Long id);

    Optional<Address> searchExactAddress(String street,
                                         String neighborhood,
                                         String city,
                                         String state,
                                         String zipCode);

    void deleteAddressesFromOtherUsers(Long adminId);
}