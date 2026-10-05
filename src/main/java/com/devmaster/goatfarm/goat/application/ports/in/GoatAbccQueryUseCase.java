package com.devmaster.goatfarm.goat.application.ports.in;

import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRaceOptionVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRegistrationLookupRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRegistrationLookupResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchResponseVO;

import java.util.List;

public interface GoatAbccQueryUseCase {

    List<GoatAbccRaceOptionVO> listRaces(Long farmId);

    GoatAbccSearchResponseVO search(Long farmId, GoatAbccSearchRequestVO requestVO);

    GoatAbccPreviewResponseVO preview(Long farmId, GoatAbccPreviewRequestVO requestVO);

    GoatAbccRegistrationLookupResponseVO lookupByRegistration(
            Long farmId,
            GoatAbccRegistrationLookupRequestVO requestVO
    );
}
