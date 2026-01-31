package com.devmaster.goatfarm.address.business;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.application.ports.out.AddressPersistencePort;
import com.devmaster.goatfarm.address.api.mapper.AddressMapper;
import com.devmaster.goatfarm.address.persistence.entity.Address;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * AddressBusinessTest (Unit Test com Mockito)
 */
@ExtendWith(MockitoExtension.class)
public class AddressBusinessTest {

    @InjectMocks
    private AddressBusiness addressBusiness;

    @Mock
    private AddressPersistencePort addressPort;

    @Mock
    private AddressMapper addressMapper;

    @Mock
    private OwnershipService ownershipService;

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
                () -> addressBusiness.createAddress(1L, vo)); // Changed from findOrCreateAddressEntity to createAddress because findOrCreate is private or not main entry point here

        verifyNoInteractions(addressPort);
        verifyNoInteractions(addressMapper);
    }

    // Note: createAddress calls validateAddressData internally. 
    // findOrCreateAddressEntity was removed or made private/internal in AddressBusiness refactoring?
    // Checking AddressBusiness.java... It has createAddress. It doesn't seem to expose findOrCreateAddressEntity publicly.
    // However, the test code from feature branch tested findOrCreateAddressEntity.
    // Let's assume createAddress is the main entry point now.

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

        // Validation is likely before ownership check or right after.
        // In AddressBusiness.java: 
        // ownershipService.verifyFarmOwnership(farmId);
        // validateAddressData(requestVO);
        
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
        
        String result = addressBusiness.deleteAddress(farmId, addressId);

        // AddressBusiness.java returns a string message
        
        verify(ownershipService).verifyFarmOwnership(farmId);
        verify(addressPort).findByIdAndFarmId(addressId, farmId);
        verify(addressPort).deleteById(addressId);
    }

    @Test
    void shouldThrowResourceNotFoundWhenDeleteAddressDoesNotExist() {
        Long farmId = 1L;
        Long addressId = 10L;

        when(addressPort.findByIdAndFarmId(addressId, farmId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> addressBusiness.deleteAddress(farmId, addressId));

        verify(ownershipService).verifyFarmOwnership(farmId);
        verify(addressPort).findByIdAndFarmId(addressId, farmId);
        verify(addressPort, never()).deleteById(any());
    }
}