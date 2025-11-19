package com.devmaster.goatfarm.infrastructure.adapters.out.persistence;

import com.devmaster.goatfarm.application.ports.out.GenealogyPersistencePort;
import com.devmaster.goatfarm.genealogy.model.entity.Genealogy;
import com.devmaster.goatfarm.genealogy.model.repository.GenealogyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GenealogyPersistenceAdapter implements GenealogyPersistencePort {

    private final GenealogyRepository repository;

    @Autowired
    public GenealogyPersistenceAdapter(GenealogyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Genealogy> findByGoatRegistration(String goatRegistration) {
        return repository.findByGoatRegistration(goatRegistration);
    }

    @Override
    public Optional<Genealogy> findByGoatRegistrationAndGoatFarmId(String goatRegistrationNumber, Long farmId) {
        return repository.findByGoatRegistrationAndGoatFarmId(goatRegistrationNumber, farmId);
    }

    @Override
    public boolean existsByGoatRegistration(String goatRegistration) {
        return repository.existsByGoatRegistration(goatRegistration);
    }

    @Override
    public Genealogy save(Genealogy genealogy) {
        return repository.save(genealogy);
    }
}