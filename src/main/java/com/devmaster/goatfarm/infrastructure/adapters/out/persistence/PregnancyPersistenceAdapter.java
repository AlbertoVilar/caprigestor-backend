package com.devmaster.goatfarm.infrastructure.adapters.out.persistence;

import com.devmaster.goatfarm.application.ports.out.PregnancyPersistencePort;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationError;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationException;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.model.entity.Pregnancy;
import com.devmaster.goatfarm.reproduction.model.repository.PregnancyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class PregnancyPersistenceAdapter implements PregnancyPersistencePort {

    private final PregnancyRepository pregnancyRepository;

    public PregnancyPersistenceAdapter(PregnancyRepository pregnancyRepository) {
        this.pregnancyRepository = pregnancyRepository;
    }

    @Override
    public Pregnancy save(Pregnancy entity) {
        return pregnancyRepository.save(entity);
    }

    @Override
    public Optional<Pregnancy> findActiveByFarmIdAndGoatId(Long farmId, String goatId) {
        List<Pregnancy> pregnancies = findAllActiveByFarmIdAndGoatIdOrdered(farmId, goatId);
        if (pregnancies.isEmpty()) {
            return Optional.empty();
        }
        if (pregnancies.size() > 1) {
            ValidationError error = new ValidationError(Instant.now(), HttpStatus.CONFLICT.value(), "Erro de integridade de dados");
            error.addError("status", "Foram encontradas múltiplas gestações ativas para a mesma cabra na fazenda");
            throw new ValidationException(error);
        }
        return Optional.of(pregnancies.get(0));
    }

    @Override
    public Optional<Pregnancy> findByIdAndFarmIdAndGoatId(Long pregnancyId, Long farmId, String goatId) {
        return pregnancyRepository.findByIdAndFarmIdAndGoatId(pregnancyId, farmId, goatId);
    }

    @Override
    public Optional<Pregnancy> findByFarmIdAndId(Long farmId, Long pregnancyId) {
        return pregnancyRepository.findByFarmIdAndId(farmId, pregnancyId);
    }

    @Override
    public Page<Pregnancy> findAllByFarmIdAndGoatId(Long farmId, String goatId, Pageable pageable) {
        return pregnancyRepository.findAllByFarmIdAndGoatIdOrderByBreedingDateDesc(farmId, goatId, pageable);
    }

    @Override
    public List<Pregnancy> findAllActiveByFarmIdAndGoatIdOrdered(Long farmId, String goatId) {
        return pregnancyRepository.findByFarmIdAndGoatIdAndStatusOrderByBreedingDateDesc(farmId, goatId, PregnancyStatus.ACTIVE);
    }
}
