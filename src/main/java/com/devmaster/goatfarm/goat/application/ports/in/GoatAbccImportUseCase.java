package com.devmaster.goatfarm.goat.application.ports.in;

import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchResponseVO;

public interface GoatAbccImportUseCase {

    GoatAbccSearchResponseVO search(Long farmId, GoatAbccSearchRequestVO requestVO);

    GoatAbccPreviewResponseVO preview(Long farmId, GoatAbccPreviewRequestVO requestVO);

    GoatResponseVO confirm(Long farmId, String externalId, GoatRequestVO goatRequestVO);
}

