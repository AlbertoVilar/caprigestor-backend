package com.devmaster.goatfarm.milk.persistence.adapter;

import com.devmaster.goatfarm.milk.application.ports.out.MilkProductionPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import com.devmaster.goatfarm.milk.persistence.entity.MilkProduction;
import com.devmaster.goatfarm.milk.persistence.repository.MilkProductionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class MilkProductionPersistenceAdapter implements MilkProductionPersistencePort {

    private final MilkProductionRepository milkProductionRepository;
    private final GoatReferenceQueryPort goatReferenceQueryPort;

    public MilkProductionPersistenceAdapter(MilkProductionRepository milkProductionRepository) {
        this(milkProductionRepository, null);
    }

    @Autowired
    public MilkProductionPersistenceAdapter(MilkProductionRepository milkProductionRepository,
                                            GoatReferenceQueryPort goatReferenceQueryPort) {
        this.milkProductionRepository = milkProductionRepository;
        this.goatReferenceQueryPort = goatReferenceQueryPort;
    }

    @Override
    public MilkProduction save(MilkProduction milkProduction) {
        populateTechnicalIdentity(milkProduction);
        return milkProductionRepository.save(milkProduction);
    }

    @Override
    public void delete(MilkProduction milkProduction) {
        milkProductionRepository.delete(milkProduction);
    }

    @Override
    public boolean existsByFarmIdAndGoatIdAndDateAndShift(
            Long farmId,
            String goatId,
            LocalDate date,
            MilkingShift shift
    ) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            if (milkProductionRepository.existsByFarmIdAndGoatTechnicalIdAndDateAndShift(
                    farmId, technicalId.get(), date, shift)) {
                return true;
            }
        }
        return milkProductionRepository.existsByFarmIdAndGoatIdAndDateAndShift(
                farmId,
                goatId,
                date,
                shift
        );
    }

    @Override
    public Optional<MilkProduction> findById(Long farmId, String goatId, Long id) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<MilkProduction> technical = milkProductionRepository.findByIdAndFarmIdAndGoatTechnicalId(id, farmId, technicalId.get());
            if (technical.isPresent()) {
                return technical;
            }
        }
        return milkProductionRepository.findByIdAndFarmIdAndGoatId(id, farmId, goatId);
    }

    @Override
    public Page<MilkProduction> search(
            Long farmId,
            String goatId,
            LocalDate from,
            LocalDate to,
            Pageable pageable,
            boolean includeCanceled
    ) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Page<MilkProduction> technical = milkProductionRepository.searchByTechnicalId(
                    farmId, technicalId.get(), from, to, pageable, includeCanceled);
            if (technical.hasContent()) {
                return technical;
            }
        }
        return milkProductionRepository.search(
                farmId,
                goatId,
                from,
                to,
                pageable,
                includeCanceled
        );
    }

    @Override
    public List<MilkProduction> findByFarmIdAndGoatIdAndDateBetween(
            Long farmId,
            String goatId,
            LocalDate from,
            LocalDate to
    ) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            List<MilkProduction> technical = milkProductionRepository.findByFarmIdAndGoatTechnicalIdAndDateBetween(
                    farmId, technicalId.get(), from, to);
            if (!technical.isEmpty()) {
                return technical;
            }
        }
        return milkProductionRepository.findByFarmIdAndGoatIdAndDateBetween(farmId, goatId, from, to);
    }

    private Optional<Long> technicalId(Long farmId, String registrationNumber) {
        if (goatReferenceQueryPort == null || registrationNumber == null) {
            return Optional.empty();
        }
        return goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId(registrationNumber, farmId)
                .map(GoatReference::id)
                .map(id -> id.value());
    }

    private void populateTechnicalIdentity(MilkProduction entity) {
        if (entity.getGoatTechnicalId() == null) {
            technicalId(entity.getFarmId(), entity.getGoatId()).ifPresent(entity::setGoatTechnicalId);
        }
    }

}
