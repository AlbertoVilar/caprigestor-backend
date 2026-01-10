package com.devmaster.goatfarm.application.ports.in;

import com.devmaster.goatfarm.milk.business.bo.LactationRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;

public interface LactationCommandUseCase {

    LactationResponseVO openLactation(Long farmId, String goatId, LactationRequestVO vo);

    void dryLactation(Long farmId, String goatId, Long lactationId);
}
