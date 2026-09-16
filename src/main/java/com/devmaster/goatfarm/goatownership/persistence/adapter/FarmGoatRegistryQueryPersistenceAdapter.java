package com.devmaster.goatfarm.goatownership.persistence.adapter;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.persistence.repository.GoatRepository;
import com.devmaster.goatfarm.goatownership.application.ports.out.FarmGoatRegistryQueryPort;
import com.devmaster.goatfarm.goatownership.persistence.entity.CreatorReferenceEntity;
import com.devmaster.goatfarm.goatownership.persistence.entity.GoatOwnershipPeriodEntity;
import com.devmaster.goatfarm.goatownership.persistence.mapper.OwnershipPersistenceMapper;
import com.devmaster.goatfarm.goatownership.persistence.repository.CreatorReferenceRepository;
import com.devmaster.goatfarm.goatownership.persistence.repository.GoatOwnershipPeriodRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FarmGoatRegistryQueryPersistenceAdapter implements FarmGoatRegistryQueryPort {

    private final CreatorReferenceRepository creators;
    private final GoatOwnershipPeriodRepository periods;
    private final GoatRepository goats;
    private final OwnershipPersistenceMapper mapper;

    public FarmGoatRegistryQueryPersistenceAdapter(
            CreatorReferenceRepository creators,
            GoatOwnershipPeriodRepository periods,
            GoatRepository goats,
            OwnershipPersistenceMapper mapper
    ) {
        this.creators = creators;
        this.periods = periods;
        this.goats = goats;
        this.mapper = mapper;
    }

    @Override
    public List<Candidate> findCandidates(long farmId) {
        List<CreatorReferenceEntity> createdByFarm = creators.findByCreatorFarmId(farmId);
        List<GoatOwnershipPeriodEntity> local = periods.findByFarmIdOrderByGoatIdAscStartedAtAscIdAsc(farmId);

        Set<Long> ids = new LinkedHashSet<>();
        createdByFarm.forEach(e -> ids.add(e.getGoatId()));
        local.forEach(p -> ids.add(p.getGoatId()));

        if (ids.isEmpty()) {
            return List.of();
        }

        Map<Long, CreatorReferenceEntity> creatorsByGoatId = creators.findAllById(ids).stream()
                .collect(Collectors.toMap(CreatorReferenceEntity::getGoatId, e -> e));
        Map<Long, List<GoatOwnershipPeriodEntity>> histories = periods.findByGoatIdInOrderByGoatIdAscStartedAtAscIdAsc(new ArrayList<>(ids)).stream()
                .collect(Collectors.groupingBy(GoatOwnershipPeriodEntity::getGoatId, LinkedHashMap::new, Collectors.toList()));

        return goats.findAllById(ids).stream().map(g -> new Candidate(
                new GoatId(g.getTechnicalId()),
                g.getRegistrationNumber(),
                g.getName(),
                g.getStatus(),
                Optional.ofNullable(creatorsByGoatId.get(g.getTechnicalId())).map(mapper::toDomain).orElse(null),
                histories.getOrDefault(g.getTechnicalId(), List.of()).stream().map(mapper::toDomain).toList()
        )).toList();
    }
}
