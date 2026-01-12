package com.devmaster.goatfarm.reproduction.business.reproductionservice;

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
public class ReproductionBusiness implements ReproductionCommandUseCase, ReproductionQueryUseCase {

    private final PregnancyPersistencePort pregnancyPersistencePort;
    private final ReproductiveEventPersistencePort reproductiveEventPersistencePort;
    private final ReproductionMapper reproductionMapper;

    @Override
    @Transactional
    public ReproductiveEventResponseVO registerBreeding(Long farmId, String goatId, BreedingRequestVO vo) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Transactional
    public PregnancyResponseVO confirmPregnancy(Long farmId, String goatId, PregnancyConfirmRequestVO vo) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Transactional
    public PregnancyResponseVO closePregnancy(Long farmId, String goatId, Long pregnancyId, PregnancyCloseRequestVO vo) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public PregnancyResponseVO getActivePregnancy(Long farmId, String goatId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Page<PregnancyResponseVO> getPregnancies(Long farmId, String goatId, Pageable pageable) {
        return Page.empty();
    }

    @Override
    public Page<ReproductiveEventResponseVO> getReproductiveEvents(Long farmId, String goatId, Pageable pageable) {
        return Page.empty();
    }
}
