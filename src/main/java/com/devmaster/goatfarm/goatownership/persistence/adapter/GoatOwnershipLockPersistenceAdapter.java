package com.devmaster.goatfarm.goatownership.persistence.adapter;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.GoatOwnershipLockState;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipLockPort;
import com.devmaster.goatfarm.goatownership.persistence.repository.GoatOwnershipLockRepository;
import com.devmaster.goatfarm.goatownership.persistence.repository.GoatOwnershipPeriodRepository;
import com.devmaster.goatfarm.goatownership.persistence.mapper.OwnershipPersistenceMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Infrastructure implementation of the canonical lock order. The caller
 * must invoke this method inside an already active transaction.
 */
@Component
public class GoatOwnershipLockPersistenceAdapter implements GoatOwnershipLockPort {
    private final GoatOwnershipLockRepository goatLockRepository;
    private final GoatOwnershipPeriodRepository periodRepository;
    private final OwnershipPersistenceMapper mapper;

    public GoatOwnershipLockPersistenceAdapter(GoatOwnershipLockRepository goatLockRepository,
                                               GoatOwnershipPeriodRepository periodRepository,
                                               OwnershipPersistenceMapper mapper) {
        this.goatLockRepository = goatLockRepository;
        this.periodRepository = periodRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<GoatOwnershipLockState> lockGoatOwnership(GoatId goatId) {
        if (goatId == null) {
            return Optional.empty();
        }
        // Canonical order: cabras row first, then the open ownership period.
        Optional<Long> lockedGoat = goatLockRepository.lockGoatById(goatId.value());
        if (lockedGoat.isEmpty()) {
            return Optional.empty();
        }
        var openPeriod = periodRepository.findOpenForUpdateByGoatId(goatId.value()).map(mapper::toDomain);
        return Optional.of(new GoatOwnershipLockState(goatId, openPeriod));
    }
}
