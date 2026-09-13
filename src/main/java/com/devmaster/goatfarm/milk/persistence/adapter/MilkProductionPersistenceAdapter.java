package com.devmaster.goatfarm.milk.persistence.adapter;

import com.devmaster.goatfarm.milk.application.ports.out.MilkProductionPersistencePort;
import com.devmaster.goatfarm.milk.application.ports.out.MilkProductionSummaryQueryPort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import com.devmaster.goatfarm.milk.domain.MilkProduction;
import com.devmaster.goatfarm.milk.persistence.entity.MilkProductionEntity;
import com.devmaster.goatfarm.milk.persistence.mapper.MilkProductionPersistenceMapper;
import com.devmaster.goatfarm.milk.persistence.repository.MilkProductionRepository;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.application.pagination.SortDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class MilkProductionPersistenceAdapter implements MilkProductionPersistencePort, MilkProductionSummaryQueryPort {

    private final MilkProductionRepository milkProductionRepository;
    private final GoatReferenceQueryPort goatReferenceQueryPort;
    private final MilkProductionPersistenceMapper mapper;

    public MilkProductionPersistenceAdapter(MilkProductionRepository milkProductionRepository) {
        this(milkProductionRepository, null, new MilkProductionPersistenceMapper());
    }

    public MilkProductionPersistenceAdapter(MilkProductionRepository milkProductionRepository,
                                            GoatReferenceQueryPort goatReferenceQueryPort) {
        this(milkProductionRepository, goatReferenceQueryPort, new MilkProductionPersistenceMapper());
    }

    @Autowired
    public MilkProductionPersistenceAdapter(MilkProductionRepository milkProductionRepository,
                                            GoatReferenceQueryPort goatReferenceQueryPort,
                                            MilkProductionPersistenceMapper mapper) {
        this.milkProductionRepository = milkProductionRepository;
        this.goatReferenceQueryPort = goatReferenceQueryPort;
        this.mapper = mapper;
    }

    @Override
    public MilkProduction save(MilkProduction milkProduction) {
        MilkProductionEntity entity = mapper.toEntity(milkProduction);
        populateTechnicalIdentity(entity);
        return mapper.toDomain(milkProductionRepository.save(entity));
    }

    @Override
    public void delete(MilkProduction milkProduction) {
        milkProductionRepository.delete(mapper.toEntity(milkProduction));
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
            Optional<MilkProductionEntity> technical = milkProductionRepository.findByIdAndFarmIdAndGoatTechnicalId(id, farmId, technicalId.get());
            if (technical.isPresent()) {
                return technical.map(mapper::toDomain);
            }
        }
        return milkProductionRepository.findByIdAndFarmIdAndGoatId(id, farmId, goatId).map(mapper::toDomain);
    }

    @Override
    public PageResult<MilkProduction> search(
            Long farmId,
            String goatId,
            LocalDate from,
            LocalDate to,
            PageQuery pageQuery,
            boolean includeCanceled
    ) {
        Pageable pageable = toPageable(pageQuery);
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Page<MilkProductionEntity> technical = milkProductionRepository.searchByTechnicalId(
                    farmId, technicalId.get(), from, to, pageable, includeCanceled);
            if (technical.hasContent()) {
                return toPageResult(technical);
            }
        }
        return toPageResult(milkProductionRepository.search(
                farmId,
                goatId,
                from,
                to,
                pageable,
                includeCanceled
        ));
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
            List<MilkProductionEntity> technical = milkProductionRepository.findByFarmIdAndGoatTechnicalIdAndDateBetween(
                    farmId, technicalId.get(), from, to);
            if (!technical.isEmpty()) {
                return technical.stream().map(mapper::toDomain).toList();
            }
        }
        return milkProductionRepository.findByFarmIdAndGoatIdAndDateBetween(farmId, goatId, from, to).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<MilkProductionSnapshot> findSummaryByFarmIdAndGoatIdAndDateBetween(
            Long farmId, String goatId, LocalDate from, LocalDate to) {
        return findByFarmIdAndGoatIdAndDateBetween(farmId, goatId, from, to).stream()
                .map(production -> new MilkProductionSnapshot(
                        production.getDate(), production.getVolumeLiters()))
                .toList();
    }

    private Optional<Long> technicalId(Long farmId, String registrationNumber) {
        if (goatReferenceQueryPort == null || registrationNumber == null) {
            return Optional.empty();
        }
        return goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId(registrationNumber, farmId)
                .map(GoatReference::id)
                .map(id -> id.value());
    }

    private void populateTechnicalIdentity(MilkProductionEntity entity) {
        if (entity.getGoatTechnicalId() == null) {
            technicalId(entity.getFarmId(), entity.getGoatId()).ifPresent(entity::setGoatTechnicalId);
        }
    }

    private Pageable toPageable(PageQuery query) {
        List<Sort.Order> orders = query.sort().stream()
                .map(spec -> new Sort.Order(
                        spec.direction() == SortDirection.ASC ? Sort.Direction.ASC : Sort.Direction.DESC,
                        spec.field()))
                .toList();
        Sort sort = orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
        return PageRequest.of(query.page(), query.size(), sort);
    }

    private PageResult<MilkProduction> toPageResult(Page<MilkProductionEntity> page) {
        return new PageResult<>(page.getContent().stream().map(mapper::toDomain).toList(),
                page.getTotalElements(), page.getNumber(), page.getSize());
    }

}
