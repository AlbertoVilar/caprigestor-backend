package com.devmaster.goatfarm.goat.application.ports.in;

import com.devmaster.goatfarm.goat.business.bo.GoatRegistrationHistoryResponseVO;
import com.devmaster.goatfarm.goat.business.bo.GoatRegistrationRectificationRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatRegistrationRectificationResponseVO;

import java.util.List;

/** Explicit application boundary for audited correction of a Goat's RG. */
public interface GoatRegistrationRectificationUseCase {

    GoatRegistrationRectificationResponseVO rectify(
            Long farmId,
            String goatRouteToken,
            GoatRegistrationRectificationRequestVO request
    );

    List<GoatRegistrationHistoryResponseVO> history(Long farmId, String goatRouteToken);
}
