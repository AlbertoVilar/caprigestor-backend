package com.devmaster.goatfarm.infrastructure.adapters.out.persistence;

import com.devmaster.goatfarm.application.ports.out.LactationPersistencePort;
import com.devmaster.goatfarm.milk.model.repository.LactationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LactationPersistenceAdapter implements LactationPersistencePort {

    private final LactationRepository lactationRepository;
}
