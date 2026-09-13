package com.devmaster.goatfarm.address.business;

import com.devmaster.goatfarm.address.application.ports.out.AddressPersistencePort;
import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressBusinessTest {
    @Mock AddressPersistencePort addressPort; @Mock FarmAuthorizationUseCase authorization; @Mock EntityFinder finder;
    AddressBusiness business;
    @BeforeEach void setUp() { business = new AddressBusiness(addressPort, authorization, finder); lenient().when(finder.findOrThrow(any(), anyString())).thenAnswer(i -> ((Optional<?>) ((java.util.function.Supplier<?>) i.getArgument(0)).get()).orElseThrow(() -> new ResourceNotFoundException("missing"))); }
    private AddressRequestVO valid() { return new AddressRequestVO(null,"Rua","Centro","Rio","RJ","12345678","Brasil"); }
    @Test void rejectsInvalidState() { var r=valid(); r.setState("FF"); assertThrows(BusinessRuleException.class,()->business.createAddress(1L,r)); verifyNoInteractions(addressPort); }
    @Test void createsCleanAddress() { AddressResponseVO saved=new AddressResponseVO(1L,"Rua","Rio","Centro","RJ","12345678","Brasil"); when(addressPort.save(any())).thenReturn(saved); assertSame(saved,business.createAddress(1L,valid())); verify(addressPort).save(any()); }
    @Test void updatesScopedAddress() { AddressResponseVO current=new AddressResponseVO(10L,"Old","Rio","Centro","RJ","12345678","Brasil"); AddressResponseVO updated=new AddressResponseVO(10L,"Rua","Rio","Centro","RJ","12345678","Brasil"); when(addressPort.findByIdAndFarmId(10L,1L)).thenReturn(Optional.of(current)); when(addressPort.save(current)).thenReturn(updated); assertSame(updated,business.updateAddress(1L,10L,valid())); }
    @Test void missingAddressFails() { when(addressPort.findByIdAndFarmId(10L,1L)).thenReturn(Optional.empty()); assertThrows(ResourceNotFoundException.class,()->business.findAddressById(1L,10L)); }
}
