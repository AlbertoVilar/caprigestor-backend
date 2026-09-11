package com.devmaster.goatfarm.goat.persistence.adapter;

import com.devmaster.goatfarm.goat.application.ports.out.GoatRegistrationHistoryPersistencePort;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.domain.GoatRegistrationHistory;
import com.devmaster.goatfarm.goat.domain.RegistrationIdentity;
import com.devmaster.goatfarm.goat.persistence.entity.GoatRegistrationHistoryEntity;
import com.devmaster.goatfarm.goat.persistence.repository.GoatRegistrationHistoryRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GoatRegistrationHistoryPersistenceAdapter implements GoatRegistrationHistoryPersistencePort {

    private final GoatRegistrationHistoryRepository repository;

    public GoatRegistrationHistoryPersistenceAdapter(GoatRegistrationHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public GoatRegistrationHistory save(GoatRegistrationHistory history) {
        GoatRegistrationHistoryEntity saved = repository.save(toEntity(history));
        return toDomain(saved);
    }

    @Override
    public List<GoatRegistrationHistory> findByFarmIdAndGoatId(Long farmId, GoatId goatId) {
        if (farmId == null || goatId == null) {
            return List.of();
        }
        return repository.findByFarmIdAndGoatIdOrderByCreatedAtDescIdDesc(farmId, goatId.value())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private GoatRegistrationHistoryEntity toEntity(GoatRegistrationHistory history) {
        return GoatRegistrationHistoryEntity.builder()
                .id(history.id())
                .goatId(history.goatId().value())
                .farmId(history.farmId())
                .oldRegistrationNumber(history.oldIdentity().registrationNumber())
                .oldTod(history.oldIdentity().tod())
                .oldToe(history.oldIdentity().toe())
                .newRegistrationNumber(history.newIdentity().registrationNumber())
                .newTod(history.newIdentity().tod())
                .newToe(history.newIdentity().toe())
                .source(history.source())
                .evidenceReference(history.evidenceReference())
                .reason(history.reason())
                .actorUserId(history.actorUserId())
                .createdAt(history.createdAt())
                .build();
    }

    private GoatRegistrationHistory toDomain(GoatRegistrationHistoryEntity entity) {
        return new GoatRegistrationHistory(
                entity.getId(),
                GoatId.of(entity.getGoatId()),
                entity.getFarmId(),
                RegistrationIdentity.of(entity.getOldRegistrationNumber(), entity.getOldTod(), entity.getOldToe()),
                RegistrationIdentity.of(entity.getNewRegistrationNumber(), entity.getNewTod(), entity.getNewToe()),
                entity.getSource(),
                entity.getEvidenceReference(),
                entity.getReason(),
                entity.getActorUserId(),
                entity.getCreatedAt()
        );
    }
}
