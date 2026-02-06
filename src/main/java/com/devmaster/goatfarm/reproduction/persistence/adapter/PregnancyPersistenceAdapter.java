package com.devmaster.goatfarm.reproduction.persistence.adapter;

import com.devmaster.goatfarm.reproduction.application.ports.out.PregnancyPersistencePort;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.persistence.entity.Pregnancy;
import com.devmaster.goatfarm.reproduction.persistence.repository.PregnancyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

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
            throw new DuplicateEntityException("status", "Foram encontradas múltiplas gestações ativas para a mesma cabra na fazenda");
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
        return pregnancyRepository.findAllByFarmIdAndGoatIdOrderByBreedingDateDescIdDesc(farmId, goatId, pageable);
    }

    @Override
    public List<Pregnancy> findAllActiveByFarmIdAndGoatIdOrdered(Long farmId, String goatId) {
        return pregnancyRepository.findByFarmIdAndGoatIdAndStatusOrderByBreedingDateDescIdDesc(farmId, goatId, PregnancyStatus.ACTIVE);
    }
}
