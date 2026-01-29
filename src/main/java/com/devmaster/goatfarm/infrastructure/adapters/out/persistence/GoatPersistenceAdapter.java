package com.devmaster.goatfarm.infrastructure.adapters.out.persistence;

import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.application.ports.out.GoatGenealogyQueryPort;
import com.devmaster.goatfarm.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistência para cabras
 * Implementa a porta de saída GoatPersistencePort usando Spring Data JPA
 */
@Component
public class GoatPersistenceAdapter implements GoatPersistencePort, GoatGenealogyQueryPort {

    private final GoatRepository goatRepository;

    public GoatPersistenceAdapter(GoatRepository goatRepository) {
        this.goatRepository = goatRepository;
    }

    @Override
    public Goat save(Goat goat) {
        return goatRepository.save(goat);
    }

    @Override
    public Optional<Goat> findById(String registrationNumber) {
        return goatRepository.findById(registrationNumber);
    }

    @Override
    public Optional<Goat> findByRegistrationNumber(String registrationNumber) {
        return goatRepository.findByRegistrationNumber(registrationNumber);
    }

    @Override
    public List<Goat> findByGoatFarmId(Long goatFarmId) {
        // Preferir consulta direta ao repositório para evitar filtragem em memória
        return goatRepository.findAllByFarmId(goatFarmId, org.springframework.data.domain.Pageable.unpaged()).getContent();
    }

    @Override
    public Page<Goat> findAllByFarmId(Long goatFarmId, Pageable pageable) {
        return goatRepository.findAllByFarmId(goatFarmId, pageable);
    }

    @Override
    public Page<Goat> findByNameAndFarmId(Long goatFarmId, String name, Pageable pageable) {
        return goatRepository.findByNameAndFarmId(goatFarmId, name, pageable);
    }

    @Override
    public Optional<Goat> findByIdAndFarmId(String id, Long farmId) {
        return goatRepository.findByIdAndFarmId(id, farmId);
    }

    @Override
    public Optional<Goat> findByIdAndFarmIdWithFamilyGraph(String id, Long farmId) {
        return goatRepository.findByIdAndFarmIdWithFamilyGraph(id, farmId);
    }

    @Override
    public void deleteById(String registrationNumber) {
        goatRepository.deleteById(registrationNumber);
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