package com.devmaster.goatfarm.address.business;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.dao.AddressDAO;
import com.devmaster.goatfarm.address.mapper.AddressMapper;
import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationException;
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

@ExtendWith(MockitoExtension.class)
public class AddressBusinessTest {

    @InjectMocks
    private AddressBusiness addressBusiness;

    @Mock
    private AddressDAO addressDAO;

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
        // arrange
        AddressRequestVO vo = validRequestVO();
        vo.setState("FF"); // UF inválida

        // act + assert
        assertThrows(ValidationException.class,
                () -> addressBusiness.findOrCreateAddressEntity(vo));

        // assert extra: domínio não toca infra
        verifyNoInteractions(addressDAO);
        verifyNoInteractions(addressMapper);
    }

    @Test
    void shouldReturnExistingAddressWhenExactMatchExists() {
        // arrange
        AddressRequestVO vo = validRequestVO();
        Address existing = new Address();

        when(addressDAO.searchExactAddress(
                eq(vo.getStreet()),
                eq(vo.getNeighborhood()),
                eq(vo.getCity()),
                eq(vo.getState()),
                eq(vo.getZipCode())
        )).thenReturn(Optional.of(existing));

        // act
        Address result = addressBusiness.findOrCreateAddressEntity(vo);

        // assert
        assertSame(existing, result);

        // não deve criar novo nem mapear VO -> Entity
        verify(addressDAO, never()).createAddress(any());
        verify(addressMapper, never()).toEntity(any(AddressRequestVO.class));
    }


}
