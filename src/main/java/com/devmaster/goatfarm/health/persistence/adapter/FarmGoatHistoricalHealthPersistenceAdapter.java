package com.devmaster.goatfarm.health.persistence.adapter;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.HistoricalHealthEventItem;
import com.devmaster.goatfarm.goatownership.application.ports.out.FarmGoatHistoricalHealthQueryPort;
import com.devmaster.goatfarm.health.persistence.entity.HealthEvent;
import com.devmaster.goatfarm.health.persistence.repository.HealthEventRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Health-owned persistence adapter that queries historical health event records.
 * Returns technology-neutral application models implementing the goatownership query port.
 */
@Component
public class FarmGoatHistoricalHealthPersistenceAdapter implements FarmGoatHistoricalHealthQueryPort {

    private final HealthEventRepository healthEventRepository;

    public FarmGoatHistoricalHealthPersistenceAdapter(HealthEventRepository healthEventRepository) {
        this.healthEventRepository = Objects.requireNonNull(healthEventRepository, "healthEventRepository must not be null");
    }

    @Override
    public List<HistoricalHealthEventItem> findHistoricalHealthEvents(GoatId goatId, long farmId) {
        if (goatId == null || farmId <= 0) {
            return List.of();
        }

        List<HealthEvent> entities = healthEventRepository.findHistoricalHealthEvents(goatId.value(), farmId);
        return entities.stream()
                .map(this::toItem)
                .toList();
    }

    private HistoricalHealthEventItem toItem(HealthEvent entity) {
        Long technicalId = entity.getGoatTechnicalId();
        GoatId goatId = technicalId != null ? GoatId.of(technicalId) : null;

        return new HistoricalHealthEventItem(
                entity.getId(),
                goatId,
                entity.getFarmId(),
                entity.getType(),
                entity.getStatus(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getScheduledDate(),
                entity.getPerformedAt(),
                entity.getResponsible(),
                entity.getNotes(),
                entity.getProductName(),
                entity.getActiveIngredient(),
                entity.getDose(),
                entity.getDoseUnit(),
                entity.getRoute(),
                entity.getBatchNumber(),
                entity.getWithdrawalMilkDays(),
                entity.getWithdrawalMeatDays()
        );
    }
}
