package com.devmaster.goatfarm.milk.persistence.adapter;

import com.devmaster.goatfarm.milk.application.ports.out.LactationPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.persistence.entity.Lactation;
import com.devmaster.goatfarm.milk.persistence.projection.LactationDryOffAlertProjection;
import com.devmaster.goatfarm.milk.persistence.repository.LactationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class LactationPersistenceAdapter implements LactationPersistencePort {

    private final LactationRepository lactationRepository;
    private final GoatReferenceQueryPort goatReferenceQueryPort;

    public LactationPersistenceAdapter(LactationRepository lactationRepository) {
        this(lactationRepository, null);
    }

    @Autowired
    public LactationPersistenceAdapter(LactationRepository lactationRepository,
                                       GoatReferenceQueryPort goatReferenceQueryPort) {
        this.lactationRepository = lactationRepository;
        this.goatReferenceQueryPort = goatReferenceQueryPort;
    }

    @Override
    public Lactation save(Lactation lactation) {
        populateTechnicalIdentity(lactation);
        return lactationRepository.save(lactation);
    }

    @Override
    public Optional<Lactation> findActiveByFarmIdAndGoatId(Long farmId, String goatId) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<Lactation> technical = lactationRepository.findByFarmIdAndGoatTechnicalIdAndStatus(
                    farmId, technicalId.get(), LactationStatus.ACTIVE);
            if (technical.isPresent()) {
                return technical;
            }
        }
        return lactationRepository.findByFarmIdAndGoatIdAndStatus(farmId, goatId, LactationStatus.ACTIVE);
    }

    @Override
    public Optional<Lactation> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<Lactation> technical = lactationRepository.findByIdAndFarmIdAndGoatTechnicalId(id, farmId, technicalId.get());
            if (technical.isPresent()) {
                return technical;
            }
        }
        return lactationRepository.findByIdAndFarmIdAndGoatId(id, farmId, goatId);
    }

    @Override
    public Page<Lactation> findAllByFarmIdAndGoatId(Long farmId, String goatId, Pageable pageable) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Page<Lactation> technical = lactationRepository.findAllByFarmIdAndGoatTechnicalId(farmId, technicalId.get(), pageable);
            if (technical.hasContent()) {
                return technical;
            }
        }
        return lactationRepository.findAllByFarmIdAndGoatId(farmId, goatId, pageable);
    }

    @Override
    public Page<LactationDryOffAlertProjection> findDryOffAlerts(Long farmId, LocalDate referenceDate, int defaultDryDays, Pageable pageable) {
        return lactationRepository.findDryOffAlerts(farmId, referenceDate, defaultDryDays, pageable);
    }

    private Optional<Long> technicalId(Long farmId, String registrationNumber) {
        if (goatReferenceQueryPort == null || registrationNumber == null) {
            return Optional.empty();
        }
        return goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId(registrationNumber, farmId)
                .map(GoatReference::id)
                .map(id -> id.value());
    }

    private void populateTechnicalIdentity(Lactation entity) {
        if (entity.getGoatTechnicalId() == null) {
            technicalId(entity.getFarmId(), entity.getGoatId()).ifPresent(entity::setGoatTechnicalId);
        }
    }
}
