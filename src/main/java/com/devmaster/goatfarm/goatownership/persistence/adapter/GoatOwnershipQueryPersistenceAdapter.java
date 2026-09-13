package com.devmaster.goatfarm.goatownership.persistence.adapter;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipQueryPort;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import com.devmaster.goatfarm.goatownership.persistence.mapper.OwnershipPersistenceMapper;
import com.devmaster.goatfarm.goatownership.persistence.repository.GoatOwnershipPeriodRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class GoatOwnershipQueryPersistenceAdapter implements GoatOwnershipQueryPort {
    private final GoatOwnershipPeriodRepository repository;
    private final OwnershipPersistenceMapper mapper;

    public GoatOwnershipQueryPersistenceAdapter(GoatOwnershipPeriodRepository repository,
                                                OwnershipPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Long> findCurrentOwnerFarmId(GoatId goatId) {
        return goatId == null ? Optional.empty()
                : repository.findByGoatIdAndEndedAtIsNull(goatId.value()).map(entity -> entity.getFarmId());
    }

    @Override
    public boolean isOwnedByFarmAt(GoatId goatId, Long farmId, Instant instant) {
        if (goatId == null || farmId == null || instant == null) {
            return false;
        }
        return repository.existsOwnedByFarmAt(goatId.value(), farmId, instant);
    }

    @Override
    public List<GoatOwnershipPeriod> findOwnershipHistory(GoatId goatId) {
        return goatId == null ? List.of()
                : repository.findByGoatIdOrderByStartedAtAscIdAsc(goatId.value()).stream().map(mapper::toDomain).toList();
    }
}
