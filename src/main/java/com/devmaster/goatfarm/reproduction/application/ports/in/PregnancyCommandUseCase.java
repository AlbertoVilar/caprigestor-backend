package com.devmaster.goatfarm.reproduction.application.ports.in;

import com.devmaster.goatfarm.reproduction.business.bo.PregnancyCheckRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyCloseRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyConfirmRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyResponseVO;
import com.devmaster.goatfarm.reproduction.business.bo.ReproductiveEventResponseVO;

public interface PregnancyCommandUseCase {
    PregnancyResponseVO confirmPregnancy(Long farmId, String goatId, PregnancyConfirmRequestVO vo);
    ReproductiveEventResponseVO registerPregnancyCheck(Long farmId, String goatId, PregnancyCheckRequestVO vo);
    PregnancyResponseVO closePregnancy(Long farmId, String goatId, Long pregnancyId, PregnancyCloseRequestVO vo);
}
