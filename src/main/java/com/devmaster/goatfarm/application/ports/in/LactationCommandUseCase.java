package com.devmaster.goatfarm.application.ports.in;

import com.devmaster.goatfarm.milk.business.bo.LactationRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;
import com.devmaster.goatfarm.milk.business.bo.LactationDryRequestVO;

public interface LactationCommandUseCase {

    LactationResponseVO openLactation(Long farmId, String goatId, LactationRequestVO vo);

    LactationResponseVO dryLactation(Long farmId, String goatId, Long lactationId, LactationDryRequestVO vo);
}
