package com.devmaster.goatfarm.farm.persistence.adapter;

import com.devmaster.goatfarm.address.persistence.repository.AddressRepository;
import com.devmaster.goatfarm.application.exception.PersistenceConflictException;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import com.devmaster.goatfarm.farm.application.model.FarmPersistenceCommand;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoatFarmPersistenceAdapterTest {

    @Mock private GoatFarmRepository repository;
    @Mock private UserRepository userRepository;
    @Mock private AddressRepository addressRepository;

    @Test
    void translatesDatabaseConflictAtFarmSaveBoundary() {
        when(repository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate farm"));
        GoatFarmPersistenceAdapter adapter = new GoatFarmPersistenceAdapter(repository, userRepository, addressRepository);

        assertThrows(PersistenceConflictException.class, () -> adapter.save(
                new FarmPersistenceCommand(null, "Farm", "12345", null, null, null, null)));
    }
}
