package com.devmaster.goatfarm.genealogy.dao;

import com.devmaster.goatfarm.genealogy.model.entity.Genealogy;
import com.devmaster.goatfarm.genealogy.model.repository.GenealogyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GenealogyDAO {

    private final GenealogyRepository repository;

    @Autowired
    public GenealogyDAO(GenealogyRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<Genealogy> findByGoatRegistration(String goatRegistration) {
        return repository.findByGoatRegistration(goatRegistration);
    }

    @Transactional(readOnly = true)
    public boolean existsByGoatRegistration(String goatRegistration) {
        return repository.existsByGoatRegistration(goatRegistration);
    }

    @Transactional
    public Genealogy save(Genealogy genealogy) {
        return repository.save(genealogy);
    }
}
