package com.devmaster.goatfarm.goat.application.ports.out;

import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawPreviewVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawSearchResultVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchRequestVO;

public interface GoatAbccPublicQueryPort {

    GoatAbccRawSearchResultVO search(GoatAbccSearchRequestVO requestVO);

    GoatAbccRawPreviewVO preview(String externalId);
}

