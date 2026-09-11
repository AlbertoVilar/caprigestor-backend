package com.devmaster.goatfarm.goat.persistence.adapter;

import com.devmaster.goatfarm.goat.application.ports.out.GoatGenealogyQueryPort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatGenealogySnapshot;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.application.pagination.GoatPage;
import com.devmaster.goatfarm.goat.application.pagination.GoatPageQuery;
import com.devmaster.goatfarm.goat.application.ports.out.GoatHerdSnapshot;
import com.devmaster.goatfarm.goat.application.ports.out.GoatBreedCount;
import com.devmaster.goatfarm.goat.application.ports.out.GoatValidationQueryPort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goat.persistence.entity.GoatEntity;
import com.devmaster.goatfarm.goat.persistence.repository.GoatRepository;
import com.devmaster.goatfarm.goat.persistence.mapper.GoatPersistenceMapper;
import com.devmaster.goatfarm.goat.domain.Goat;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.application.routing.GoatRouteIdentifier;
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
 * <p>The domain-facing {@link GoatPersistencePort} and narrow read ports are
 * the only application contracts exposed by this adapter. JPA entities and
 * repository projections remain private to the persistence boundary.</p>
 */
@Component
public class GoatPersistenceAdapter implements GoatPersistencePort, GoatGenealogyQueryPort,
        GoatReferenceQueryPort, GoatValidationQueryPort {

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
    public Goat save(Goat goat) {
        GoatEntity existing = goat.id() == null
                ? goatRepository.findByRegistrationNumber(goat.registrationNumber()).orElse(null)
                : goatRepository.findByTechnicalId(goat.id().value()).orElse(null);
        if (existing == null) {
            GoatFarm farm = goat.farmId() == null ? null : reference(GoatFarm.class, goat.farmId());
            User user = goat.userId() == null ? null : reference(User.class, goat.userId());
            existing = mapper.toNewEntity(goat, farm, user);
        } else {
            mapper.toEntity(goat, existing);
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
        return findLegacyToken(registrationNumber, farmId).map(mapper::toDomain);
    }

    @Override
    public Optional<GoatReference> findReferenceByRegistrationNumber(String registrationNumber) {
        return registrationNumber == null
                ? Optional.empty()
                : goatRepository.findByRegistrationNumber(registrationNumber).map(this::toReference);
    }

    @Override
    public Optional<GoatReference> findReferenceByRegistrationNumberAndFarmId(
            String registrationNumber,
            Long farmId
    ) {
        return findLegacyToken(registrationNumber, farmId).map(this::toReference);
    }

    @Override
    public Optional<GoatReference> findReferenceByTechnicalIdAndFarmId(GoatId goatId, Long farmId) {
        return goatId == null ? Optional.empty()
                : goatRepository.findByTechnicalIdAndFarmId(goatId.value(), farmId).map(this::toReference);
    }

    @Override
    public GoatPage<Goat> findAllByFarmId(Long farmId, GoatPageQuery query) {
        return toDomainPage(goatRepository.findAllByFarmId(farmId, toPageable(query)));
    }

    @Override
    public GoatPage<Goat> findAllByFarmIdAndBreed(Long farmId, GoatBreed breed, GoatPageQuery query) {
        return toDomainPage(goatRepository.findAllByFarmIdAndBreed(farmId, breed, toPageable(query)));
    }

    @Override
    public GoatPage<Goat> findByNameAndFarmId(Long farmId, String name, GoatPageQuery query) {
        return toDomainPage(goatRepository.findByNameAndFarmId(farmId, name, toPageable(query)));
    }

    @Override
    public GoatPage<Goat> findByNameAndFarmIdAndBreed(Long farmId, String name, GoatBreed breed, GoatPageQuery query) {
        return toDomainPage(goatRepository.findByNameAndFarmIdAndBreed(farmId, name, breed, toPageable(query)));
    }

    @Override
    public List<Goat> findOffspringByParentId(Long farmId, GoatId parentId) {
        return parentId == null ? List.of() : goatRepository.findOffspringByParentTechnicalId(farmId, parentId.value())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<GoatValidationSnapshot> findForValidation(String registrationNumber, Long farmId) {
        return findLegacyToken(registrationNumber, farmId)
                .map(goat -> new GoatValidationSnapshot(
                        goat.getRegistrationNumber(),
                        goat.getGender(),
                        goat.getStatus()
                ));
    }

    @Override
    public Optional<GoatGenealogySnapshot> findGenealogyByRegistrationNumberAndFarmId(
            String registrationNumber,
            Long farmId
    ) {
        return findLegacyToken(registrationNumber, farmId)
                .flatMap(goat -> goatRepository.findByRegistrationNumberAndFarmIdWithTechnicalFamilyGraph(
                        goat.getRegistrationNumber(), farmId))
                .map(goat -> toGenealogySnapshot(goat, 3));
    }

    @Override
    public void deleteById(GoatId id) {
        goatRepository.findByTechnicalId(id.value()).ifPresent(goatRepository::delete);
    }

    @Override
    public boolean existsByRegistrationNumber(String registrationNumber) {
        return goatRepository.existsByRegistrationNumber(registrationNumber);
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

    private GoatGenealogySnapshot toGenealogySnapshot(GoatEntity goat, int remainingGenerations) {
        return new GoatGenealogySnapshot(
                GoatId.of(goat.getTechnicalId()),
                goat.getRegistrationNumber(),
                goat.getName(),
                goat.getBreed(),
                goat.getColor(),
                goat.getStatus(),
                goat.getGender(),
                goat.getCategory(),
                goat.getTod(),
                goat.getToe(),
                goat.getBirthDate(),
                goat.getUser() == null ? null : goat.getUser().getName(),
                goat.getFarm() == null || goat.getFarm().getUser() == null ? null : goat.getFarm().getUser().getName(),
                remainingGenerations > 0 ? toGenealogyParent(goat.getTechnicalFather(),
                        goat.getExternalFatherRegistrationNumber(), remainingGenerations) : null,
                remainingGenerations > 0 ? toGenealogyParent(goat.getTechnicalMother(),
                        goat.getExternalMotherRegistrationNumber(), remainingGenerations) : null
        );
    }

    private GoatGenealogySnapshot.ParentReference toGenealogyParent(
            GoatEntity technicalParent,
            String externalRegistrationNumber,
            int remainingGenerations
    ) {
        GoatEntity localParent = technicalParent;
        if (localParent != null) {
            return GoatGenealogySnapshot.ParentReference.local(
                    toGenealogySnapshot(localParent, remainingGenerations - 1)
            );
        }
        if (externalRegistrationNumber != null && !externalRegistrationNumber.isBlank()) {
            return GoatGenealogySnapshot.ParentReference.external(externalRegistrationNumber);
        }
        return null;
    }

    private GoatReference toReference(GoatEntity goat) {
        return new GoatReference(
                GoatId.of(goat.getTechnicalId()),
                goat.getFarm() == null ? null : goat.getFarm().getId(),
                goat.getRegistrationNumber(),
                goat.getName(),
                goat.getGender()
        );
    }

    private Optional<GoatEntity> findLegacyToken(String token, Long farmId) {
        if (token == null || farmId == null) return Optional.empty();
        return GoatRouteIdentifier.technicalId(token)
                .flatMap(id -> goatRepository.findByTechnicalIdAndFarmId(id.value(), farmId))
                .or(() -> goatRepository.findByRegistrationNumberAndFarmId(token, farmId));
    }
}
