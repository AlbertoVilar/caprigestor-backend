package com.devmaster.goatfarm.milk.persistence.adapter;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalLactationItem;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalMilkProductionItem;
import com.devmaster.goatfarm.goatownership.application.ports.out.FarmGoatHistoricalMilkLactationQueryPort;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.persistence.entity.LactationEntity;
import com.devmaster.goatfarm.milk.persistence.entity.MilkProductionEntity;
import com.devmaster.goatfarm.milk.persistence.repository.LactationRepository;
import com.devmaster.goatfarm.milk.persistence.repository.MilkProductionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Milk-owned persistence adapter that queries historical lactation and milk production records.
 * Returns technology-neutral application models implementing the goatownership query port.
 */
@Component
public class FarmGoatHistoricalMilkLactationPersistenceAdapter implements FarmGoatHistoricalMilkLactationQueryPort {

    private final LactationRepository lactationRepository;
    private final MilkProductionRepository milkProductionRepository;

    public FarmGoatHistoricalMilkLactationPersistenceAdapter(
            LactationRepository lactationRepository,
            MilkProductionRepository milkProductionRepository
    ) {
        this.lactationRepository = Objects.requireNonNull(lactationRepository, "lactationRepository must not be null");
        this.milkProductionRepository = Objects.requireNonNull(milkProductionRepository, "milkProductionRepository must not be null");
    }

    @Override
    public List<FarmGoatHistoricalLactationItem> findLactationsByGoat(GoatId goatId, String registrationNumber) {
        if (goatId == null) {
            throw new IllegalArgumentException("goatId must not be null");
        }

        List<LactationEntity> entities = lactationRepository.findByGoatTechnicalIdOrRg(
                goatId.value(),
                registrationNumber
        );

        return entities.stream()
                .map(l -> new FarmGoatHistoricalLactationItem(
                        l.getId(),
                        goatId,
                        l.getFarmId(),
                        l.getStatus(),
                        l.getStartDate(),
                        l.getEndDate(),
                        l.getPregnancyStartDate(),
                        l.getDryStartDate(),
                        l.getDryAtPregnancyDays(),
                        l.getRestDays(),
                        l.getStatus() == LactationStatus.ACTIVE
                ))
                .toList();
    }

    @Override
    public List<FarmGoatHistoricalMilkProductionItem> findMilkProductionsByGoatAndFarm(
            long farmId,
            GoatId goatId,
            String registrationNumber
    ) {
        if (farmId <= 0) {
            throw new IllegalArgumentException("farmId must be positive");
        }
        if (goatId == null) {
            throw new IllegalArgumentException("goatId must not be null");
        }

        List<MilkProductionEntity> entities = milkProductionRepository.findHistoricalForDossier(
                farmId,
                goatId.value(),
                registrationNumber
        );

        return entities.stream()
                .map(p -> new FarmGoatHistoricalMilkProductionItem(
                        p.getId(),
                        goatId,
                        p.getLactation() != null ? p.getLactation().getId() : null,
                        p.getFarmId(),
                        p.getDate(),
                        p.getShift(),
                        p.getVolumeLiters(),
                        p.getStatus(),
                        p.getNotes(),
                        p.getCanceledAt(),
                        p.getCanceledReason(),
                        p.isRecordedDuringMilkWithdrawal(),
                        p.getMilkWithdrawalEventId(),
                        p.getMilkWithdrawalEndDate(),
                        p.getMilkWithdrawalSource()
                ))
                .toList();
    }
}