package com.devmaster.goatfarm.reproduction.application.ports.in;

import com.devmaster.goatfarm.reproduction.business.bo.BreedingRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.CoverageCorrectionRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyCloseRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyConfirmRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyResponseVO;
import com.devmaster.goatfarm.reproduction.business.bo.ReproductiveEventResponseVO;

public interface ReproductionCommandUseCase {
    ReproductiveEventResponseVO registerBreeding(Long farmId, String goatId, BreedingRequestVO vo);
    PregnancyResponseVO confirmPregnancy(Long farmId, String goatId, PregnancyConfirmRequestVO vo);
    PregnancyResponseVO closePregnancy(Long farmId, String goatId, Long pregnancyId, PregnancyCloseRequestVO vo);
    ReproductiveEventResponseVO correctCoverage(Long farmId, String goatId, Long coverageEventId, CoverageCorrectionRequestVO vo);
}
