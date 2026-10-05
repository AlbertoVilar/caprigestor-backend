package com.devmaster.goatfarm.goatownership.persistence.adapter;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.ports.out.CreatorReferencePersistencePort;
import com.devmaster.goatfarm.goatownership.domain.CreatorReference;
import com.devmaster.goatfarm.goatownership.persistence.entity.CreatorReferenceEntity;
import com.devmaster.goatfarm.goatownership.persistence.mapper.OwnershipPersistenceMapper;
import com.devmaster.goatfarm.goatownership.persistence.repository.CreatorReferenceRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CreatorReferencePersistenceAdapter implements CreatorReferencePersistencePort {
    private final CreatorReferenceRepository repository;
    private final OwnershipPersistenceMapper mapper;

    public CreatorReferencePersistenceAdapter(CreatorReferenceRepository repository,
                                              OwnershipPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public CreatorReference create(GoatId goatId, CreatorReference creatorReference) {
        if (goatId == null || creatorReference == null) {
            throw new IllegalArgumentException("goatId and creatorReference are required");
        }
        if (repository.existsById(goatId.value())) {
            throw new DataIntegrityViolationException("creator reference already exists for GoatId " + goatId.value());
        }
        CreatorReferenceEntity saved = repository.save(mapper.toEntity(goatId, creatorReference));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<CreatorReference> findByGoatId(GoatId goatId) {
        return goatId == null ? Optional.empty() : repository.findById(goatId.value()).map(mapper::toDomain);
    }
}
