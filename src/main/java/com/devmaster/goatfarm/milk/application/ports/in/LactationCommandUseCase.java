package com.devmaster.goatfarm.milk.application.ports.in;

import com.devmaster.goatfarm.milk.business.bo.LactationRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;
import com.devmaster.goatfarm.milk.business.bo.LactationDryRequestVO;
import com.devmaster.goatfarm.goat.domain.GoatId;

import java.time.Instant;

public interface LactationCommandUseCase {

    LactationResponseVO openLactation(Long farmId, String goatId, LactationRequestVO vo);

    LactationResponseVO dryLactation(Long farmId, String goatId, Long lactationId, LactationDryRequestVO vo);

    LactationResponseVO resumeLactation(Long farmId, String goatId, Long lactationId);

    void closeActiveForOwnershipTransfer(GoatId goatId, long sourceFarmId, Instant effectiveAt);
}
