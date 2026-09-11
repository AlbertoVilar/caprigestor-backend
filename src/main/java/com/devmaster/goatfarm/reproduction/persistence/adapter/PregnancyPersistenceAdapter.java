package com.devmaster.goatfarm.reproduction.persistence.adapter;

import com.devmaster.goatfarm.reproduction.application.ports.out.PregnancyPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.persistence.entity.Pregnancy;
import com.devmaster.goatfarm.reproduction.persistence.repository.PregnancyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class PregnancyPersistenceAdapter implements PregnancyPersistencePort {

    private final PregnancyRepository pregnancyRepository;
    private final GoatReferenceQueryPort goatReferenceQueryPort;

    public PregnancyPersistenceAdapter(PregnancyRepository pregnancyRepository) {
        this(pregnancyRepository, null);
    }

    @Autowired
    public PregnancyPersistenceAdapter(PregnancyRepository pregnancyRepository,
                                       GoatReferenceQueryPort goatReferenceQueryPort) {
        this.pregnancyRepository = pregnancyRepository;
        this.goatReferenceQueryPort = goatReferenceQueryPort;
    }

    @Override
    public Pregnancy save(Pregnancy entity) {
        populateTechnicalIdentity(entity);
        return pregnancyRepository.save(entity);
    }

    @Override
    public Optional<Pregnancy> findActiveByFarmIdAndGoatId(Long farmId, String goatId) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<Pregnancy> technical = findActiveByTechnicalId(farmId, technicalId.get());
            if (technical.isPresent()) {
                return technical;
            }
        }
        List<Pregnancy> pregnancies = findAllActiveByFarmIdAndGoatIdOrdered(farmId, goatId);
        if (pregnancies.isEmpty()) {
            return Optional.empty();
        }
        if (pregnancies.size() > 1) {
            throw new DuplicateEntityException("status", "Foram encontradas múltiplas gestações ativas para a mesma cabra na fazenda");
        }
        return Optional.of(pregnancies.get(0));
    }

    @Override
    public Optional<Pregnancy> findByIdAndFarmIdAndGoatId(Long pregnancyId, Long farmId, String goatId) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<Pregnancy> technical = pregnancyRepository.findByIdAndFarmIdAndGoatTechnicalId(pregnancyId, farmId, technicalId.get());
            if (technical.isPresent()) {
                return technical;
            }
        }
        return pregnancyRepository.findByIdAndFarmIdAndGoatId(pregnancyId, farmId, goatId);
    }

    @Override
    public Optional<Pregnancy> findByFarmIdAndId(Long farmId, Long pregnancyId) {
        return pregnancyRepository.findByFarmIdAndId(farmId, pregnancyId);
    }

    @Override
    public Optional<Pregnancy> findByFarmIdAndCoverageEventId(Long farmId, Long coverageEventId) {
        return pregnancyRepository.findByFarmIdAndCoverageEventId(farmId, coverageEventId);
    }

    @Override
    public boolean existsByFarmIdAndCoverageEventId(Long farmId, Long coverageEventId) {
        return pregnancyRepository.existsByFarmIdAndCoverageEventId(farmId, coverageEventId);
    }

    @Override
    public Optional<LocalDate> findLatestBirthCloseDate(Long farmId, String goatId) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<LocalDate> technical = pregnancyRepository.findLatestBirthCloseDateByTechnicalId(farmId, technicalId.get());
            if (technical.isPresent()) {
                return technical;
            }
        }
        return pregnancyRepository.findLatestBirthCloseDate(farmId, goatId);
    }

    @Override
    public Page<Pregnancy> findAllByFarmIdAndGoatId(Long farmId, String goatId, Pageable pageable) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Page<Pregnancy> technical = pregnancyRepository.findAllByFarmIdAndGoatTechnicalIdOrderByBreedingDateDescIdDesc(
                    farmId, technicalId.get(), pageable);
            if (technical.hasContent()) {
                return technical;
            }
        }
        return pregnancyRepository.findAllByFarmIdAndGoatIdOrderByBreedingDateDescIdDesc(farmId, goatId, pageable);
    }

    @Override
    public List<Pregnancy> findAllActiveByFarmIdAndGoatIdOrdered(Long farmId, String goatId) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            List<Pregnancy> technical = pregnancyRepository.findByFarmIdAndGoatTechnicalIdAndStatusOrderByBreedingDateDescIdDesc(
                    farmId, technicalId.get(), PregnancyStatus.ACTIVE);
            if (!technical.isEmpty()) {
                return technical;
            }
        }
        return pregnancyRepository.findByFarmIdAndGoatIdAndStatusOrderByBreedingDateDescIdDesc(farmId, goatId, PregnancyStatus.ACTIVE);
    }

    @Override
    public Page<Pregnancy> findActiveWithDueDateOnOrBefore(Long farmId, LocalDate referenceDate, Pageable pageable) {
        return pregnancyRepository
                .findByFarmIdAndStatusAndExpectedDueDateIsNotNullAndExpectedDueDateLessThanEqualOrderByExpectedDueDateAscIdAsc(
                        farmId,
                        PregnancyStatus.ACTIVE,
                        referenceDate,
                        pageable
                );
    }

    private Optional<Long> technicalId(Long farmId, String registrationNumber) {
        if (goatReferenceQueryPort == null || registrationNumber == null) {
            return Optional.empty();
        }
        return goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId(registrationNumber, farmId)
                .map(GoatReference::id)
                .map(id -> id.value());
    }

    private Optional<Pregnancy> findActiveByTechnicalId(Long farmId, Long technicalId) {
        List<Pregnancy> pregnancies = pregnancyRepository
                .findByFarmIdAndGoatTechnicalIdAndStatusOrderByBreedingDateDescIdDesc(
                        farmId, technicalId, PregnancyStatus.ACTIVE);
        if (pregnancies.isEmpty()) {
            return Optional.empty();
        }
        if (pregnancies.size() > 1) {
            throw new DuplicateEntityException("status", "Foram encontradas múltiplas gestações ativas para a mesma cabra na fazenda");
        }
        return Optional.of(pregnancies.get(0));
    }

    private void populateTechnicalIdentity(Pregnancy entity) {
        if (entity.getGoatTechnicalId() == null) {
            technicalId(entity.getFarmId(), entity.getGoatId()).ifPresent(entity::setGoatTechnicalId);
        }
    }
}
