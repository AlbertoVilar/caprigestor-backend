package com.devmaster.goatfarm.milk.persistence.adapter;

import com.devmaster.goatfarm.milk.application.ports.out.LactationPersistencePort;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.application.pagination.SortDirection;
import com.devmaster.goatfarm.milk.domain.Lactation;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.persistence.entity.LactationEntity;
import com.devmaster.goatfarm.milk.persistence.mapper.LactationPersistenceMapper;
import com.devmaster.goatfarm.milk.persistence.repository.LactationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

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
    public Optional<Lactation> findByIdAndGoatTechnicalId(Long id, GoatId goatId) {
        if (id == null || goatId == null) {
            return Optional.empty();
        }
        return lactationRepository.findByIdAndGoatTechnicalId(id, goatId.value())
                .map(lactationMapper::toDomain);
    }

    @Override
    public PageResult<Lactation> findAllByFarmIdAndGoatId(Long farmId, String goatId, PageQuery pageQuery) {
        Pageable pageable = toPageable(pageQuery);
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Page<LactationEntity> technical = lactationRepository.findAllByFarmIdAndGoatTechnicalId(farmId, technicalId.get(), pageable);
            if (technical.hasContent()) {
                return toPageResult(technical);
            }
        }
        return toPageResult(lactationRepository.findAllByFarmIdAndGoatId(farmId, goatId, pageable));
    }

    @Override
    public Optional<Lactation> findLatestByFarmIdAndGoatId(Long farmId, String goatId) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<LactationEntity> technical = lactationRepository
                    .findFirstByFarmIdAndGoatTechnicalIdOrderByStartDateDescIdDesc(farmId, technicalId.get());
            if (technical.isPresent()) {
                return technical.map(lactationMapper::toDomain);
            }
        }
        return lactationRepository.findFirstByFarmIdAndGoatIdOrderByStartDateDescIdDesc(farmId, goatId)
                .map(lactationMapper::toDomain);
    }

    @Override
    public Optional<Lactation> findActiveByGoatTechnicalId(GoatId goatId) {
        if (goatId == null) {
            return Optional.empty();
        }
        return lactationRepository.findByGoatTechnicalIdAndStatus(goatId.value(), LactationStatus.ACTIVE)
                .map(lactationMapper::toDomain);
    }

    @Override
    public Optional<Lactation> findLatestByGoatTechnicalId(GoatId goatId) {
        if (goatId == null) {
            return Optional.empty();
        }
        return lactationRepository.findFirstByGoatTechnicalIdOrderByStartDateDescIdDesc(goatId.value())
                .map(lactationMapper::toDomain);
    }

    @Override
    public List<Lactation> findAllActiveByFarmId(Long farmId) {
        return lactationRepository.findAllByFarmIdAndStatus(farmId, LactationStatus.ACTIVE)
                .stream().map(lactationMapper::toDomain).toList();
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

    private Pageable toPageable(PageQuery query) {
        var orders = query.sort().stream()
                .map(spec -> new Sort.Order(spec.direction() == SortDirection.ASC ? Sort.Direction.ASC : Sort.Direction.DESC, spec.field()))
                .toList();
        return PageRequest.of(query.page(), query.size(), Sort.by(orders));
    }

    private PageResult<Lactation> toPageResult(Page<LactationEntity> page) {
        return new PageResult<>(page.getContent().stream().map(lactationMapper::toDomain).toList(), page.getTotalElements(), page.getNumber(), page.getSize());
    }

}
