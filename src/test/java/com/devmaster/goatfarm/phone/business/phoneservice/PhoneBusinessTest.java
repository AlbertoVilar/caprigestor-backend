package com.devmaster.goatfarm.phone.business.phoneservice;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.phone.application.ports.out.PhonePersistencePort;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.phone.business.mapper.PhoneBusinessMapper;
import com.devmaster.goatfarm.phone.persistence.entity.Phone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhoneBusinessTest {

    @Mock
    private PhonePersistencePort phonePort;
    @Mock
    private PhoneBusinessMapper phoneMapper;
    @Mock
    private GoatFarmPersistencePort goatFarmPort;
    @Mock
    private OwnershipService ownershipService;
    @Mock
    private EntityFinder entityFinder;

    private PhoneBusiness phoneBusiness;

    @BeforeEach
    void setUp() {
        phoneBusiness = new PhoneBusiness(phonePort, phoneMapper, goatFarmPort, ownershipService, entityFinder);
    }

    @Test
    @DisplayName("Should fail when deleting the last phone of a farm")
    void deletePhone_shouldFailWhenLastPhone() {
        Long farmId = 1L;
        Long phoneId = 10L;

        when(phonePort.findByIdAndFarmId(phoneId, farmId)).thenReturn(Optional.of(new Phone()));
        when(phonePort.countByFarmId(farmId)).thenReturn(1L);
        // Mock EntityFinder to return the phone
        when(entityFinder.findOrThrow(any(), anyString())).thenAnswer(invocation -> {
             java.util.function.Supplier<Optional<Phone>> supplier = invocation.getArgument(0);
             return supplier.get().orElseThrow(() -> new com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException("Mocked Ex"));
        });

        assertThrows(BusinessRuleException.class, () -> phoneBusiness.deletePhone(farmId, phoneId));

        verify(phonePort, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should delete phone when farm has more than one")
    void deletePhone_shouldDeleteWhenMoreThanOne() {
        Long farmId = 1L;
        Long phoneId = 10L;

        when(phonePort.findByIdAndFarmId(phoneId, farmId)).thenReturn(Optional.of(new Phone()));
        when(phonePort.countByFarmId(farmId)).thenReturn(2L);
        // Mock EntityFinder to return the phone
        when(entityFinder.findOrThrow(any(), anyString())).thenAnswer(invocation -> {
             java.util.function.Supplier<Optional<Phone>> supplier = invocation.getArgument(0);
             return supplier.get().orElseThrow(() -> new com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException("Mocked Ex"));
        });

        phoneBusiness.deletePhone(farmId, phoneId);

        verify(phonePort).deleteById(phoneId);
    }
}
