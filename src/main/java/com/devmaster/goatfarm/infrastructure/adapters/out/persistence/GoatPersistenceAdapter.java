package com.devmaster.goatfarm.infrastructure.adapters.out.persistence;

import com.devmaster.goatfarm.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistÃªncia para cabras
 * Implementa a porta de saÃ­da GoatPersistencePort usando Spring Data JPA
 */
@Component
public class GoatPersistenceAdapter implements GoatPersistencePort {

    private final GoatRepository goatRepository;

    public GoatPersistenceAdapter(GoatRepository goatRepository) {
        this.goatRepository = goatRepository;
    }

    @Override
    public Goat save(Goat goat) {
        return goatRepository.save(goat);
    }

    @Override
    public Optional<Goat> findById(Long id) {
                        return goatRepository.findById(String.valueOf(id));
    }

    @Override
    public Optional<Goat> findByRegistrationNumber(String registrationNumber) {
        return goatRepository.findByRegistrationNumber(registrationNumber);
    }

    @Override
    public List<Goat> findByGoatFarmId(Long goatFarmId) {
                        return goatRepository.findAll().stream()
                .filter(goat -> goat.getFarm() != null && goat.getFarm().getId().equals(goatFarmId))
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        goatRepository.deleteById(String.valueOf(id));
    }

    @Override
    public boolean existsByRegistrationNumber(String registrationNumber) {
        return goatRepository.existsById(registrationNumber);
    }

    @Override
    public void deleteGoatsFromOtherUsers(Long adminId) {
        goatRepository.deleteGoatsFromOtherUsers(adminId);
    }
}
