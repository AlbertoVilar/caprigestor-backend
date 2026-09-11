package com.devmaster.goatfarm.milk.persistence.adapter;

import com.devmaster.goatfarm.milk.application.ports.out.LactationPersistencePort;
import com.devmaster.goatfarm.milk.application.model.LactationDryOffAlertSnapshot;
import com.devmaster.goatfarm.milk.domain.Lactation;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.persistence.entity.LactationEntity;
import com.devmaster.goatfarm.milk.persistence.projection.LactationDryOffAlertProjection;
import com.devmaster.goatfarm.milk.persistence.mapper.LactationPersistenceMapper;
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
    private final LactationPersistenceMapper lactationMapper;

    public LactationPersistenceAdapter(LactationRepository lactationRepository) {
        this(lactationRepository, null, new LactationPersistenceMapper());
    }

    @Autowired
    public LactationPersistenceAdapter(LactationRepository lactationRepository,
                                       GoatReferenceQueryPort goatReferenceQueryPort,
                                       LactationPersistenceMapper lactationMapper) {
        this.lactationRepository = lactationRepository;
        this.goatReferenceQueryPort = goatReferenceQueryPort;
        this.lactationMapper = lactationMapper;
    }

    @Override
    public Lactation save(Lactation lactation) {
        LactationEntity entity = lactationMapper.toEntity(lactation);
        populateTechnicalIdentity(entity);
        return lactationMapper.toDomain(lactationRepository.save(entity));
    }

    @Override
    public Optional<Lactation> findActiveByFarmIdAndGoatId(Long farmId, String goatId) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<LactationEntity> technical = lactationRepository.findByFarmIdAndGoatTechnicalIdAndStatus(
                    farmId, technicalId.get(), LactationStatus.ACTIVE);
            if (technical.isPresent()) {
                return technical.map(lactationMapper::toDomain);
            }
        }
        return lactationRepository.findByFarmIdAndGoatIdAndStatus(farmId, goatId, LactationStatus.ACTIVE)
                .map(lactationMapper::toDomain);
    }

    @Override
    public Optional<Lactation> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<LactationEntity> technical = lactationRepository.findByIdAndFarmIdAndGoatTechnicalId(id, farmId, technicalId.get());
            if (technical.isPresent()) {
                return technical.map(lactationMapper::toDomain);
            }
        }
        return lactationRepository.findByIdAndFarmIdAndGoatId(id, farmId, goatId)
                .map(lactationMapper::toDomain);
    }

    @Override
    public Page<Lactation> findAllByFarmIdAndGoatId(Long farmId, String goatId, Pageable pageable) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Page<LactationEntity> technical = lactationRepository.findAllByFarmIdAndGoatTechnicalId(farmId, technicalId.get(), pageable);
            if (technical.hasContent()) {
                return technical.map(lactationMapper::toDomain);
            }
        }
        return lactationRepository.findAllByFarmIdAndGoatId(farmId, goatId, pageable)
                .map(lactationMapper::toDomain);
    }

    @Override
    public Page<LactationDryOffAlertSnapshot> findDryOffAlerts(Long farmId, LocalDate referenceDate, int defaultDryDays, Pageable pageable) {
        return lactationRepository.findDryOffAlerts(farmId, referenceDate, defaultDryDays, pageable)
                .map(this::toDryOffSnapshot);
    }

    private Optional<Long> technicalId(Long farmId, String registrationNumber) {
        if (goatReferenceQueryPort == null || registrationNumber == null) {
            return Optional.empty();
        }
        return goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId(registrationNumber, farmId)
                .map(GoatReference::id)
                .map(id -> id.value());
    }

    private void populateTechnicalIdentity(LactationEntity entity) {
        if (entity.getGoatTechnicalId() == null) {
            technicalId(entity.getFarmId(), entity.getGoatId()).ifPresent(entity::setGoatTechnicalId);
        }
    }

    private LactationDryOffAlertSnapshot toDryOffSnapshot(LactationDryOffAlertProjection projection) {
        return new LactationDryOffAlertSnapshot(
                projection.getLactationId(), projection.getGoatTechnicalId(), projection.getGoatId(),
                projection.getDryAtPregnancyDays(), projection.getStartDatePregnancy(),
                projection.getBreedingDate(), projection.getConfirmDate(), projection.getDryOffDate()
        );
    }
}
