package com.devmaster.goatfarm.goat.persistence.adapter;

import com.devmaster.goatfarm.goat.application.ports.out.GoatGenealogyQueryPort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPage;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPageQuery;
import com.devmaster.goatfarm.goat.application.ports.out.GoatHerdSnapshot;
import com.devmaster.goatfarm.goat.application.ports.out.GoatBreedCount;
import com.devmaster.goatfarm.goat.application.ports.out.LegacyGoatPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatValidationQueryPort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goat.persistence.entity.GoatEntity;
import com.devmaster.goatfarm.goat.persistence.repository.GoatBreedCountProjection;
import com.devmaster.goatfarm.goat.persistence.repository.GoatRepository;
import com.devmaster.goatfarm.goat.persistence.mapper.GoatPersistenceMapper;
import com.devmaster.goatfarm.goat.domain.Goat;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for the Goat aggregate.
 *
 * <p>The domain-facing {@link GoatPersistencePort} is the primary contract;
 * {@link LegacyGoatPersistencePort} and the genealogy port remain compatibility
 * views for modules that have not yet migrated from JPA/RG references.</p>
 */
@Component
public class GoatPersistenceAdapter implements GoatPersistencePort, LegacyGoatPersistencePort, GoatGenealogyQueryPort, GoatValidationQueryPort {

    private final GoatRepository goatRepository;
    private final GoatPersistenceMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public GoatPersistenceAdapter(GoatRepository goatRepository, GoatPersistenceMapper mapper) {
        this.goatRepository = goatRepository;
        this.mapper = mapper;
    }

    /** Compatibility constructor for lightweight legacy unit tests. */
    public GoatPersistenceAdapter(GoatRepository goatRepository) {
        this(goatRepository, new GoatPersistenceMapper());
    }

    @Override
    public GoatEntity save(GoatEntity goat) {
        return goatRepository.save(goat);
    }

    @Override
    public Goat save(Goat goat) {
        GoatEntity existing = goat.id() == null
                ? goatRepository.findByRegistrationNumber(goat.registrationNumber()).orElse(null)
                : goatRepository.findByTechnicalId(goat.id().value()).orElse(null);
        if (existing == null) {
            GoatFarm farm = goat.farmId() == null ? null : reference(GoatFarm.class, goat.farmId());
            User user = goat.userId() == null ? null : reference(User.class, goat.userId());
            existing = mapper.toNewEntity(goat, farm, user,
                    localParent(goat.father()), localParent(goat.mother()));
        } else {
            mapper.toEntity(goat, existing, localParent(goat.father()), localParent(goat.mother()));
        }
        GoatEntity saved = goatRepository.save(existing);
        if (saved.getTechnicalId() == null && entityManager != null) {
            entityManager.flush();
            saved = goatRepository.findByRegistrationNumber(saved.getRegistrationNumber()).orElse(saved);
        }
        return mapper.toDomain(saved);
    }

    private <T> T reference(Class<T> type, Long id) {
        if (entityManager == null) {
            return null;
        }
        return entityManager.getReference(type, id);
    }

    private GoatEntity localParent(Goat.ParentReference parent) {
        if (parent == null || !parent.isLocal() || parent.registrationNumber() == null) {
            return null;
        }
        return goatRepository.findByRegistrationNumber(parent.registrationNumber()).orElse(null);
    }

    @Override
    public Optional<GoatEntity> findById(String registrationNumber) {
        return goatRepository.findById(registrationNumber);
    }

    @Override
    public Optional<GoatEntity> findByRegistrationNumber(String registrationNumber) {
        return goatRepository.findByRegistrationNumber(registrationNumber);
    }

    @Override
    public Optional<Goat> findById(GoatId id) {
        return id == null ? Optional.empty() : goatRepository.findByTechnicalId(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Goat> findByIdAndFarmId(GoatId id, Long farmId) {
        return id == null ? Optional.empty() : goatRepository.findByTechnicalIdAndFarmId(id.value(), farmId).map(mapper::toDomain);
    }

    @Override
    public Optional<Goat> findDomainByRegistrationNumber(String registrationNumber) {
        return goatRepository.findByRegistrationNumber(registrationNumber).map(mapper::toDomain);
    }

    @Override
    public Optional<Goat> findByRegistrationNumberAndFarmId(String registrationNumber, Long farmId) {
        return goatRepository.findByIdAndFarmId(registrationNumber, farmId).map(mapper::toDomain);
    }

    @Override
    public List<GoatEntity> findByGoatFarmId(Long goatFarmId) {
        // Preferir consulta direta ao repositório para evitar filtragem em memória
        return goatRepository.findAllByFarmId(goatFarmId, org.springframework.data.domain.Pageable.unpaged()).getContent();
    }

    @Override
    public long countByFarmId(Long goatFarmId) {
        return goatRepository.countByFarmId(goatFarmId);
    }

    @Override
    public long countByFarmIdAndGender(Long goatFarmId, Gender gender) {
        return goatRepository.countByFarmIdAndGender(goatFarmId, gender);
    }

    @Override
    public long countByFarmIdAndStatus(Long goatFarmId, GoatStatus status) {
        return goatRepository.countByFarmIdAndStatus(goatFarmId, status);
    }

    @Override
    public long countByFarmIdWithoutBreed(Long goatFarmId) {
        return goatRepository.countByFarmIdWithoutBreed(goatFarmId);
    }

    @Override
    public List<GoatBreedCountProjection> countBreedsByFarmId(Long goatFarmId) {
        return goatRepository.countBreedsByFarmId(goatFarmId);
    }

    @Override
    public Page<GoatEntity> findAllByFarmId(Long goatFarmId, Pageable pageable) {
        return goatRepository.findAllByFarmId(goatFarmId, pageable);
    }

    @Override
    public GoatPage<Goat> findAllByFarmId(Long farmId, GoatPageQuery query) {
        return toDomainPage(goatRepository.findAllByFarmId(farmId, toPageable(query)));
    }

    @Override
    public Page<GoatEntity> findAllByFarmIdAndBreed(Long goatFarmId, GoatBreed breed, Pageable pageable) {
        return goatRepository.findAllByFarmIdAndBreed(goatFarmId, breed, pageable);
    }

    @Override
    public GoatPage<Goat> findAllByFarmIdAndBreed(Long farmId, GoatBreed breed, GoatPageQuery query) {
        return toDomainPage(goatRepository.findAllByFarmIdAndBreed(farmId, breed, toPageable(query)));
    }

    @Override
    public Page<GoatEntity> findByNameAndFarmId(Long goatFarmId, String name, Pageable pageable) {
        return goatRepository.findByNameAndFarmId(goatFarmId, name, pageable);
    }

    @Override
    public GoatPage<Goat> findByNameAndFarmId(Long farmId, String name, GoatPageQuery query) {
        return toDomainPage(goatRepository.findByNameAndFarmId(farmId, name, toPageable(query)));
    }

    @Override
    public Page<GoatEntity> findByNameAndFarmIdAndBreed(Long goatFarmId, String name, GoatBreed breed, Pageable pageable) {
        return goatRepository.findByNameAndFarmIdAndBreed(goatFarmId, name, breed, pageable);
    }

    @Override
    public GoatPage<Goat> findByNameAndFarmIdAndBreed(Long farmId, String name, GoatBreed breed, GoatPageQuery query) {
        return toDomainPage(goatRepository.findByNameAndFarmIdAndBreed(farmId, name, breed, toPageable(query)));
    }

    @Override
    public List<GoatEntity> findOffspringByParentRegistration(Long goatFarmId, String parentRegistrationNumber) {
        return goatRepository.findOffspringByParentRegistration(goatFarmId, parentRegistrationNumber);
    }

    @Override
    public List<Goat> findOffspringByParentId(Long farmId, GoatId parentId) {
        return parentId == null ? List.of() : goatRepository.findOffspringByParentTechnicalId(farmId, parentId.value())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<GoatEntity> findByIdAndFarmId(String id, Long farmId) {
        return goatRepository.findByIdAndFarmId(id, farmId);
    }

    @Override
    public Optional<GoatValidationSnapshot> findForValidation(String registrationNumber, Long farmId) {
        return goatRepository.findByIdAndFarmId(registrationNumber, farmId)
                .map(goat -> new GoatValidationSnapshot(
                        goat.getRegistrationNumber(),
                        goat.getGender(),
                        goat.getStatus()
                ));
    }

    @Override
    public Optional<GoatEntity> findByIdAndFarmIdWithFamilyGraph(String id, Long farmId) {
        return goatRepository.findByIdAndFarmIdWithFamilyGraph(id, farmId);
    }

    @Override
    public void deleteById(String registrationNumber) {
        goatRepository.deleteById(registrationNumber);
    }

    @Override
    public void deleteById(GoatId id) {
        goatRepository.findByTechnicalId(id.value()).ifPresent(entity -> goatRepository.deleteById(entity.getRegistrationNumber()));
    }

    @Override
    public boolean existsByRegistrationNumber(String registrationNumber) {
        return goatRepository.existsById(registrationNumber);
    }

    @Override
    public void deleteGoatsFromOtherUsers(Long adminId) {
        goatRepository.deleteGoatsFromOtherUsers(adminId);
    }

    @Override
    public GoatHerdSnapshot getHerdSummary(Long farmId) {
        List<GoatBreedCount> breeds = goatRepository.countBreedsByFarmId(farmId).stream()
                .map(p -> new GoatBreedCount(p.getBreed(), p.getTotal())).toList();
        return new GoatHerdSnapshot(
                goatRepository.countByFarmId(farmId),
                goatRepository.countByFarmIdAndGender(farmId, Gender.MACHO),
                goatRepository.countByFarmIdAndGender(farmId, Gender.FEMEA),
                goatRepository.countByFarmIdAndStatus(farmId, GoatStatus.ATIVO),
                goatRepository.countByFarmIdAndStatus(farmId, GoatStatus.INATIVO),
                goatRepository.countByFarmIdAndStatus(farmId, GoatStatus.VENDIDO),
                goatRepository.countByFarmIdAndStatus(farmId, GoatStatus.FALECIDO),
                breeds,
                goatRepository.countByFarmIdWithoutBreed(farmId));
    }

    private GoatPage<Goat> toDomainPage(Page<GoatEntity> page) {
        return new GoatPage<>(page.getContent().stream().map(mapper::toDomain).toList(),
                page.getTotalElements(), page.getNumber(), page.getSize());
    }

    private Pageable toPageable(GoatPageQuery query) {
        if (query.sort() == null || query.sort().isBlank()) {
            return org.springframework.data.domain.PageRequest.of(query.page(), query.size());
        }
        String[] parts = query.sort().split(",", 2);
        org.springframework.data.domain.Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;
        return org.springframework.data.domain.PageRequest.of(query.page(), query.size(),
                org.springframework.data.domain.Sort.by(direction, parts[0]));
    }
}
