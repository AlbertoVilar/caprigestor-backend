package com.devmaster.goatfarm.address.application.ports.out;

import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;

import java.util.Optional;

/**
 * Porta de saída para persistência de endereços
 */
public interface AddressPersistencePort {

    AddressResponseVO save(AddressResponseVO address);

    Optional<AddressResponseVO> findById(Long id);

    Optional<AddressResponseVO> findByIdAndFarmId(Long addressId, Long farmId);

    void deleteById(Long id);

    Optional<AddressResponseVO> searchExactAddress(String street,
                                         String neighborhood,
                                         String city,
                                         String state,
                                         String zipCode);

}
