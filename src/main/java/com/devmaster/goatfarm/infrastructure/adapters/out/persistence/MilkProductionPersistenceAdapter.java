package com.devmaster.goatfarm.infrastructure.adapters.out.persistence;

import com.devmaster.goatfarm.application.ports.out.MilkProductionPersistencePort;
import com.devmaster.goatfarm.milk.model.repository.MilkProductionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MilkProductionPersistenceAdapter implements MilkProductionPersistencePort {

    private final MilkProductionRepository milkProductionRepository;
}
