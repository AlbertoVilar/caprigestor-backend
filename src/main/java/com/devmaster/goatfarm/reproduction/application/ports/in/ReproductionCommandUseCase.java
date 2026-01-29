package com.devmaster.goatfarm.reproduction.application.ports.in;

import com.devmaster.goatfarm.reproduction.business.bo.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReproductionCommandUseCase {
    ReproductiveEventResponseVO registerBreeding(Long farmId, String goatId, BreedingRequestVO vo);
    PregnancyResponseVO confirmPregnancy(Long farmId, String goatId, PregnancyConfirmRequestVO vo);
    PregnancyResponseVO closePregnancy(Long farmId, String goatId, Long pregnancyId, PregnancyCloseRequestVO vo);
}
