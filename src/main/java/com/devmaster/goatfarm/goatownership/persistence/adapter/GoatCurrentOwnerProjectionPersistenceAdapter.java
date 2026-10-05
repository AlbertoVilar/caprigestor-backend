package com.devmaster.goatfarm.goatownership.persistence.adapter;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatCurrentOwnerProjectionPort;
import com.devmaster.goatfarm.goatownership.persistence.repository.GoatCurrentOwnerProjectionRepository;
import org.springframework.stereotype.Component;

/** Adapter for the transitional current-owner projection on cabras. */
@Component
public class GoatCurrentOwnerProjectionPersistenceAdapter implements GoatCurrentOwnerProjectionPort {
    private final GoatCurrentOwnerProjectionRepository repository;

    public GoatCurrentOwnerProjectionPersistenceAdapter(GoatCurrentOwnerProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean moveFromTo(GoatId goatId, long expectedSourceFarmId, long targetFarmId) {
        if (goatId == null || expectedSourceFarmId <= 0 || targetFarmId <= 0) {
            return false;
        }
        return repository.moveFromTo(goatId.value(), expectedSourceFarmId, targetFarmId) == 1;
    }
}
