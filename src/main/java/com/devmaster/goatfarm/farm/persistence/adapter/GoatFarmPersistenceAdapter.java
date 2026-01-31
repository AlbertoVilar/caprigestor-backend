package com.devmaster.goatfarm.farm.persistence.adapter;

import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GoatFarmPersistenceAdapter implements GoatFarmPersistencePort {

    private final GoatFarmRepository repository;

    public GoatFarmPersistenceAdapter(GoatFarmRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<GoatFarm> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<GoatFarm> findByIdAndUserId(Long id, Long userId) {
        return repository.findByIdAndUserId(id, userId);
    }

    @Override
    public Optional<GoatFarm> findByAddressId(Long addressId) {
        return repository.findByAddressId(addressId);
    }

    @Override
    public Optional<GoatFarm> findByIdWithDetails(Long id) {
        return repository.findByIdWithDetails(id);
    }

    @Override
    public Page<GoatFarm> searchByName(String name, Pageable pageable) {
        return repository.searchGoatFarmByName(name, pageable);
    }

    @Override
    public Page<GoatFarm> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public boolean existsByName(String name) {
        return repository.existsByName(name);
    }

    @Override
    public boolean existsByTod(String tod) {
        return repository.existsByTod(tod);
    }

    @Override
    public GoatFarm save(GoatFarm goatFarm) {
        return repository.save(goatFarm);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteGoatFarmsFromOtherUsers(Long adminId) {
        repository.deleteGoatFarmsFromOtherUsers(adminId);
    }
}