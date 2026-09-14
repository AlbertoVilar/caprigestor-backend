package com.devmaster.goatfarm.goatownership.persistence.adapter;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipPeriodPersistencePort;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import com.devmaster.goatfarm.goatownership.persistence.entity.GoatOwnershipPeriodEntity;
import com.devmaster.goatfarm.goatownership.persistence.mapper.OwnershipPersistenceMapper;
import com.devmaster.goatfarm.goatownership.persistence.repository.GoatOwnershipPeriodRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class GoatOwnershipPeriodPersistenceAdapter implements GoatOwnershipPeriodPersistencePort {
    private final GoatOwnershipPeriodRepository repository;
    private final OwnershipPersistenceMapper mapper;

    public GoatOwnershipPeriodPersistenceAdapter(GoatOwnershipPeriodRepository repository,
                                                 OwnershipPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public GoatOwnershipPeriod save(GoatOwnershipPeriod period) {
        if (period == null) {
            throw new IllegalArgumentException("period is required");
        }
        GoatOwnershipPeriodEntity entity;
        if (period.id() == null) {
            entity = mapper.toEntity(period);
        } else {
            entity = repository.findById(period.id())
                    .orElseThrow(() -> new IllegalArgumentException("ownership period does not exist: " + period.id()));
            mapper.update(entity, period);
        }
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<GoatOwnershipPeriod> findOpenByGoatId(GoatId goatId) {
        return goatId == null ? Optional.empty()
                : repository.findByGoatIdAndEndedAtIsNull(goatId.value()).map(mapper::toDomain);
    }

    @Override
    public List<GoatOwnershipPeriod> findByGoatIdOrderByStartedAt(GoatId goatId) {
        return goatId == null ? List.of()
                : repository.findByGoatIdOrderByStartedAtAscIdAsc(goatId.value()).stream().map(mapper::toDomain).toList();
    }
}
