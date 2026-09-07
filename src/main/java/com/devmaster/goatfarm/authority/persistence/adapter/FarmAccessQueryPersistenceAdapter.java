package com.devmaster.goatfarm.authority.persistence.adapter;

import com.devmaster.goatfarm.authority.application.ports.out.FarmAccessQueryPort;
import com.devmaster.goatfarm.authority.persistence.repository.FarmOperatorRepository;
import org.springframework.stereotype.Component;

@Component
public class FarmAccessQueryPersistenceAdapter implements FarmAccessQueryPort {

    private final FarmOperatorRepository farmOperatorRepository;

    public FarmAccessQueryPersistenceAdapter(FarmOperatorRepository farmOperatorRepository) {
        this.farmOperatorRepository = farmOperatorRepository;
    }

    @Override
    public boolean existsOperatorLink(Long farmId, Long userId) {
        return farmOperatorRepository.existsByFarmIdAndUserId(farmId, userId);
    }
}
