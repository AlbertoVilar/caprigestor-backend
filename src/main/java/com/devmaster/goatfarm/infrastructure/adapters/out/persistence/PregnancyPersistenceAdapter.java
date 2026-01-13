package com.devmaster.goatfarm.infrastructure.adapters.out.persistence;

import com.devmaster.goatfarm.application.ports.out.PregnancyPersistencePort;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.model.entity.Pregnancy;
import com.devmaster.goatfarm.reproduction.model.repository.PregnancyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PregnancyPersistenceAdapter implements PregnancyPersistencePort {

    private final PregnancyRepository pregnancyRepository;

    @Override
    public Pregnancy save(Pregnancy entity) {
        return pregnancyRepository.save(entity);
    }

    @Override
    public Optional<Pregnancy> findActiveByFarmIdAndGoatId(Long farmId, String goatId) {
        // Assuming ACTIVE means CONFIRMED or SUSPECTED, but usually we just want the open one.
        // However, the prompt says PregnancyStatus (SUSPECTED, CONFIRMED, LOST, CLOSED).
        // A pregnancy is active if it is not LOST or CLOSED.
        // Since the repository method needs a specific status, and JpaRepository doesn't support "NOT IN" easily with method names unless strictly defined,
        // or we use @Query.
        // But for MVP skeleton, I will just call a repository method that returns Optional.empty() or throw UnsupportedOperationException if logic is complex.
        // The prompt says "SEM implementar lógica de negócio... métodos podem retornar null/Optional.empty".
        // But for Adapters, I should delegate to Repository if possible.
        // I will return Optional.empty() for now as logic "what is active" might be complex (SUSPECTED or CONFIRMED).
        return Optional.empty(); 
    }

    @Override
    public Optional<Pregnancy> findByIdAndFarmIdAndGoatId(Long pregnancyId, Long farmId, String goatId) {
        return pregnancyRepository.findByIdAndFarmIdAndGoatId(pregnancyId, farmId, goatId);
    }

    @Override
    public Page<Pregnancy> findAllByFarmIdAndGoatId(Long farmId, String goatId, Pageable pageable) {
        return pregnancyRepository.findAllByFarmIdAndGoatIdOrderByBreedingDateDesc(farmId, goatId, pageable);
    }
}
