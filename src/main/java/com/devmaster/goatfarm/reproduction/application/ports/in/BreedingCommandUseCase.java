package com.devmaster.goatfarm.reproduction.application.ports.in;

import com.devmaster.goatfarm.reproduction.business.bo.BreedingRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.CoverageCorrectionRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.ReproductiveEventResponseVO;

public interface BreedingCommandUseCase {
    ReproductiveEventResponseVO registerBreeding(Long farmId, String goatId, BreedingRequestVO vo);
    ReproductiveEventResponseVO correctCoverage(Long farmId, String goatId, Long coverageEventId, CoverageCorrectionRequestVO vo);
}
