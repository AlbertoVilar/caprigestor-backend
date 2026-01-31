package com.devmaster.goatfarm.address.business;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.application.ports.out.AddressPersistencePort;
import com.devmaster.goatfarm.address.api.mapper.AddressMapper;
import com.devmaster.goatfarm.address.persistence.entity.Address;
import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * AddressBusinessTest (Unit Test com Mockito)
 */
@ExtendWith(MockitoExtension.class)
public class AddressBusinessTest {

    private AddressBusiness addressBusiness;

    @Mock
    private AddressPersistencePort addressPort;

    @Mock
    private AddressMapper addressMapper;

    @Mock
    private OwnershipService ownershipService;

    @Mock
    private EntityFinder entityFinder;

    @BeforeEach
    void setUp() {
        addressBusiness = new AddressBusiness(addressPort, addressMapper, ownershipService, entityFinder);

        lenient().when(entityFinder.findOrThrow(any(), anyString())).thenAnswer(invocation -> {
            java.util.function.Supplier<Optional<?>> supplier = invocation.getArgument(0);
            String errorMsg = invocation.getArgument(1);
            return supplier.get().orElseThrow(() -> new com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException(errorMsg));
        });
    }

    private AddressRequestVO validRequestVO() {
        AddressRequestVO vo = new AddressRequestVO();
        vo.setStreet("Rua das Flores");
        vo.setNeighborhood("Centro");
        vo.setCity("Rio de Janeiro");
        vo.setState("RJ");
        vo.setZipCode("12345678");
        vo.setCountry("Brasil");
        return vo;
    }

    @Test
    void shouldThrowValidationExceptionWhenStateIsInvalid() {
        AddressRequestVO vo = validRequestVO();
        vo.setState("FF");

        assertThrows(BusinessRuleException.class,
                () -> addressBusiness.createAddress(1L, vo));

        verifyNoInteractions(addressPort);
        verifyNoInteractions(addressMapper);
    }

    @Test
    void shouldCreateAddressWhenDataIsValid() {
        Long farmId = 1L;
        AddressRequestVO vo = validRequestVO();

        Address entity = new Address();
        Address saved = new Address();
        AddressResponseVO response = new AddressResponseVO();

        when(addressMapper.toEntity(vo)).thenReturn(entity);
        when(addressPort.save(entity)).thenReturn(saved);
        when(addressMapper.toResponseVO(saved)).thenReturn(response);

        AddressResponseVO result = addressBusiness.createAddress(farmId, vo);

        assertSame(response, result);

        verify(ownershipService).verifyFarmOwnership(farmId);
        verify(addressMapper).toEntity(vo);
        verify(addressPort).save(entity);
        verify(addressMapper).toResponseVO(saved);
    }
    
    @Test
    void shouldThrowValidationExceptionOnCreateAddressWhenStateIsInvalid() {
        Long farmId = 1L;
        AddressRequestVO vo = validRequestVO();
        vo.setState("FF");

        assertThrows(BusinessRuleException.class,
                () -> addressBusiness.createAddress(farmId, vo));

        verify(ownershipService).verifyFarmOwnership(farmId);
        verifyNoInteractions(addressPort);
    }

    @Test
    void shouldFindAddressByIdWhenExists() {
        Long farmId = 1L;
        Long addressId = 10L;

        Address entity = new Address();
        AddressResponseVO response = new AddressResponseVO();

        when(addressPort.findByIdAndFarmId(addressId, farmId)).thenReturn(Optional.of(entity));
        when(addressMapper.toResponseVO(entity)).thenReturn(response);

        AddressResponseVO result = addressBusiness.findAddressById(farmId, addressId);

        assertSame(response, result);

        verify(ownershipService).verifyFarmOwnership(farmId);
        verify(addressPort).findByIdAndFarmId(addressId, farmId);
        verify(addressMapper).toResponseVO(entity);
    }

    @Test
    void shouldThrowResourceNotFoundWhenFindAddressByIdDoesNotExist() {
        Long farmId = 1L;
        Long addressId = 10L;

        when(addressPort.findByIdAndFarmId(addressId, farmId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> addressBusiness.findAddressById(farmId, addressId));

        verify(ownershipService).verifyFarmOwnership(farmId);
        verify(addressPort).findByIdAndFarmId(addressId, farmId);
        verifyNoInteractions(addressMapper);
    }

    @Test
    void shouldUpdateAddressWhenExistsAndDataIsValid() {
        Long farmId = 1L;
        Long addressId = 10L;
        AddressRequestVO vo = validRequestVO();

        Address current = new Address();
        Address updated = new Address();
        AddressResponseVO response = new AddressResponseVO();

        when(addressPort.findByIdAndFarmId(addressId, farmId)).thenReturn(Optional.of(current));
        when(addressPort.save(current)).thenReturn(updated); // save updates in JPA
        when(addressMapper.toResponseVO(updated)).thenReturn(response);

        AddressResponseVO result = addressBusiness.updateAddress(farmId, addressId, vo);

        assertSame(response, result);

        verify(ownershipService).verifyFarmOwnership(farmId);
        verify(addressPort).findByIdAndFarmId(addressId, farmId);
        verify(addressMapper).toEntity(current, vo);
        verify(addressPort).save(current);
        verify(addressMapper).toResponseVO(updated);
    }

    @Test
    void shouldThrowResourceNotFoundWhenUpdateAddressDoesNotExist() {
        Long farmId = 1L;
        Long addressId = 10L;
        AddressRequestVO vo = validRequestVO();

        when(addressPort.findByIdAndFarmId(addressId, farmId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> addressBusiness.updateAddress(farmId, addressId, vo));

        verify(ownershipService).verifyFarmOwnership(farmId);
        verify(addressPort).findByIdAndFarmId(addressId, farmId);
        verifyNoInteractions(addressMapper);
        verify(addressPort, never()).save(any());
    }

    @Test
    void shouldDeleteAddressWhenExists() {
        Long farmId = 1L;
        Long addressId = 10L;

        Address existing = new Address();

        when(addressPort.findByIdAndFarmId(addressId, farmId)).thenReturn(Optional.of(existing));

        addressBusiness.deleteAddress(farmId, addressId);

        verify(ownershipService).verifyFarmOwnership(farmId);
        verify(addressPort).findByIdAndFarmId(addressId, farmId);
        verify(addressPort).deleteById(addressId);
    }
}
