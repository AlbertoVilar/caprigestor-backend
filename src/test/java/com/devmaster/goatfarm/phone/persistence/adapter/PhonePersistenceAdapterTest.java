package com.devmaster.goatfarm.phone.persistence.adapter;

import com.devmaster.goatfarm.application.exception.PersistenceConflictException;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.persistence.repository.PhoneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhonePersistenceAdapterTest {

    @Mock private PhoneRepository phoneRepository;
    @Mock private GoatFarmRepository farmRepository;

    @Test
    void translatesDatabaseConflictAtPhoneSaveBoundary() {
        GoatFarm farm = new GoatFarm();
        farm.setId(1L);
        when(farmRepository.findById(1L)).thenReturn(Optional.of(farm));
        when(phoneRepository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate phone"));
        PhonePersistenceAdapter adapter = new PhonePersistenceAdapter(phoneRepository, farmRepository);

        assertThrows(PersistenceConflictException.class, () -> adapter.save(
                1L, new PhoneRequestVO(null, "11", "999999999", null)));
    }
}
