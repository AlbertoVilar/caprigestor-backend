package com.devmaster.goatfarm.application.ports.in;

import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;

public interface LactationQueryUseCase {

    LactationResponseVO getActiveLactation(Long farmId, String goatId);
}
