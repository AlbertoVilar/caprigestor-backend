package com.devmaster.goatfarm.health.business.healthservice;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.health.application.ports.in.HealthWithdrawalQueryUseCase;
import com.devmaster.goatfarm.health.application.ports.out.HealthEventPersistencePort;
import com.devmaster.goatfarm.health.business.bo.GoatWithdrawalStatusVO;
import com.devmaster.goatfarm.health.business.bo.HealthWithdrawalOriginVO;
import com.devmaster.goatfarm.health.persistence.entity.HealthEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class HealthWithdrawalBusiness implements HealthWithdrawalQueryUseCase {

    private final HealthEventPersistencePort healthEventPersistencePort;
    private final GoatPersistencePort goatPersistencePort;
    private final EntityFinder entityFinder;

    public HealthWithdrawalBusiness(
            HealthEventPersistencePort healthEventPersistencePort,
            GoatPersistencePort goatPersistencePort,
            EntityFinder entityFinder
    ) {
        this.healthEventPersistencePort = healthEventPersistencePort;
        this.goatPersistencePort = goatPersistencePort;
        this.entityFinder = entityFinder;
    }

    @Override
    public GoatWithdrawalStatusVO getGoatWithdrawalStatus(Long farmId, String goatId, LocalDate referenceDate) {
        entityFinder.findOrThrow(
                () -> goatPersistencePort.findByIdAndFarmId(goatId, farmId),
                "Cabra nao encontrada no capril informado. goatId=" + goatId + ", farmId=" + farmId
        );

        return buildStatus(
                goatId,
                healthEventPersistencePort.findPerformedWithWithdrawalByFarmIdAndGoatId(farmId, goatId),
                safeReferenceDate(referenceDate)
        );
    }

    @Override
    public List<GoatWithdrawalStatusVO> listActiveWithdrawalStatuses(Long farmId, LocalDate referenceDate) {
        LocalDate effectiveReferenceDate = safeReferenceDate(referenceDate);
        return healthEventPersistencePort.findPerformedWithWithdrawalByFarmId(farmId).stream()
                .collect(java.util.stream.Collectors.groupingBy(HealthEvent::getGoatId))
                .entrySet().stream()
                .map(entry -> buildStatus(entry.getKey(), entry.getValue(), effectiveReferenceDate))
                .filter(status -> status.hasActiveMilkWithdrawal() || status.hasActiveMeatWithdrawal())
                .sorted(Comparator.comparing(GoatWithdrawalStatusVO::goatId))
                .toList();
    }

    private GoatWithdrawalStatusVO buildStatus(String goatId, List<HealthEvent> events, LocalDate referenceDate) {
        Optional<HealthWithdrawalOriginVO> milkWithdrawal = events.stream()
                .map(event -> toOrigin(event, event.getWithdrawalMilkDays()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(origin -> isActive(origin.withdrawalEndDate(), referenceDate))
                .max(Comparator.comparing(HealthWithdrawalOriginVO::withdrawalEndDate));

        Optional<HealthWithdrawalOriginVO> meatWithdrawal = events.stream()
                .map(event -> toOrigin(event, event.getWithdrawalMeatDays()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(origin -> isActive(origin.withdrawalEndDate(), referenceDate))
                .max(Comparator.comparing(HealthWithdrawalOriginVO::withdrawalEndDate));

        return GoatWithdrawalStatusVO.builder()
                .goatId(goatId)
                .referenceDate(referenceDate)
                .hasActiveMilkWithdrawal(milkWithdrawal.isPresent())
                .hasActiveMeatWithdrawal(meatWithdrawal.isPresent())
                .milkWithdrawal(milkWithdrawal.orElse(null))
                .meatWithdrawal(meatWithdrawal.orElse(null))
                .build();
    }

    private Optional<HealthWithdrawalOriginVO> toOrigin(HealthEvent event, Integer withdrawalDays) {
        if (event.getPerformedAt() == null || withdrawalDays == null || withdrawalDays <= 0) {
            return Optional.empty();
        }

        LocalDate performedDate = event.getPerformedAt().toLocalDate();
        LocalDate withdrawalEndDate = performedDate.plusDays(withdrawalDays);

        return Optional.of(HealthWithdrawalOriginVO.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .productName(event.getProductName())
                .activeIngredient(event.getActiveIngredient())
                .batchNumber(event.getBatchNumber())
                .performedDate(performedDate)
                .withdrawalEndDate(withdrawalEndDate)
                .build());
    }

    private LocalDate safeReferenceDate(LocalDate referenceDate) {
        return referenceDate != null ? referenceDate : LocalDate.now();
    }

    private boolean isActive(LocalDate withdrawalEndDate, LocalDate referenceDate) {
        return withdrawalEndDate != null && !referenceDate.isAfter(withdrawalEndDate);
    }
}
