package com.devmaster.goatfarm.application.ports.in;

import com.devmaster.goatfarm.reproduction.business.bo.PregnancyResponseVO;
import com.devmaster.goatfarm.reproduction.business.bo.ReproductiveEventResponseVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReproductionQueryUseCase {
    PregnancyResponseVO getActivePregnancy(Long farmId, String goatId);
    PregnancyResponseVO getPregnancyById(Long farmId, Long pregnancyId);
    Page<PregnancyResponseVO> getPregnancies(Long farmId, String goatId, Pageable pageable);
    Page<ReproductiveEventResponseVO> getReproductiveEvents(Long farmId, String goatId, Pageable pageable);
}
