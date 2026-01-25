package com.devmaster.goatfarm.phone.business;

import com.devmaster.goatfarm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.application.ports.out.PhonePersistencePort;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.phone.business.business.PhoneBusiness;
import com.devmaster.goatfarm.phone.mapper.PhoneMapper;
import com.devmaster.goatfarm.phone.model.entity.Phone;
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
    private PhoneMapper phoneMapper;
    @Mock
    private GoatFarmPersistencePort goatFarmPort;
    @Mock
    private OwnershipService ownershipService;

    private PhoneBusiness phoneBusiness;

    @BeforeEach
    void setUp() {
        phoneBusiness = new PhoneBusiness(phonePort, phoneMapper, goatFarmPort, ownershipService);
    }

    @Test
    @DisplayName("Should fail when deleting the last phone of a farm")
    void deletePhone_shouldFailWhenLastPhone() {
        Long farmId = 1L;
        Long phoneId = 10L;

        when(phonePort.findByIdAndFarmId(phoneId, farmId)).thenReturn(Optional.of(new Phone()));
        when(phonePort.countByFarmId(farmId)).thenReturn(1L);

        assertThrows(ValidationException.class, () -> phoneBusiness.deletePhone(farmId, phoneId));

        verify(phonePort, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should delete phone when farm has more than one")
    void deletePhone_shouldDeleteWhenMoreThanOne() {
        Long farmId = 1L;
        Long phoneId = 10L;

        when(phonePort.findByIdAndFarmId(phoneId, farmId)).thenReturn(Optional.of(new Phone()));
        when(phonePort.countByFarmId(farmId)).thenReturn(2L);

        phoneBusiness.deletePhone(farmId, phoneId);

        verify(phonePort).deleteById(phoneId);
    }
}
