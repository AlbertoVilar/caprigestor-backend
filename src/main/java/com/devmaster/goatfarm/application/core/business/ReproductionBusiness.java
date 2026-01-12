package com.devmaster.goatfarm.application.core.business;

import com.devmaster.goatfarm.application.ports.in.ReproductionCommandUseCase;
import com.devmaster.goatfarm.application.ports.in.ReproductionQueryUseCase;
import com.devmaster.goatfarm.application.ports.out.PregnancyPersistencePort;
import com.devmaster.goatfarm.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.reproduction.business.bo.*;
import com.devmaster.goatfarm.reproduction.mapper.ReproductionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReproductionBusiness implements ReproductionCommandUseCase, ReproductionQueryUseCase {

    private final PregnancyPersistencePort pregnancyPersistencePort;
    private final ReproductiveEventPersistencePort reproductiveEventPersistencePort;
    private final ReproductionMapper reproductionMapper;

    @Override
    public ReproductiveEventResponseVO registerBreeding(Long farmId, String goatId, BreedingRequestVO vo) {
        return null; // TODO: Implement
    }

    @Override
    public PregnancyResponseVO confirmPregnancy(Long farmId, String goatId, PregnancyConfirmRequestVO vo) {
        return null; // TODO: Implement
    }

    @Override
    public PregnancyResponseVO closePregnancy(Long farmId, String goatId, Long pregnancyId, PregnancyCloseRequestVO vo) {
        return null; // TODO: Implement
    }

    @Override
    @Transactional(readOnly = true)
    public PregnancyResponseVO getActivePregnancy(Long farmId, String goatId) {
        return null; // TODO: Implement
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PregnancyResponseVO> getPregnancies(Long farmId, String goatId, Pageable pageable) {
        return Page.empty(); // TODO: Implement
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReproductiveEventResponseVO> getReproductiveEvents(Long farmId, String goatId, Pageable pageable) {
        return Page.empty(); // TODO: Implement
    }
}
