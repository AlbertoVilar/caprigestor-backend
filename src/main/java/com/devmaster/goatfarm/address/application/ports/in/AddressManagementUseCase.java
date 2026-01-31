package com.devmaster.goatfarm.address.application.ports.in;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;

/**
 * Porta de entrada (Use Case) para gerenciamento de endereços ligados à fazenda.
 */
public interface AddressManagementUseCase {

    AddressResponseVO createAddress(Long farmId, AddressRequestVO requestVO);

    AddressResponseVO updateAddress(Long farmId, Long addressId, AddressRequestVO requestVO);

    AddressResponseVO findAddressById(Long farmId, Long addressId);

    String deleteAddress(Long farmId, Long addressId);
}