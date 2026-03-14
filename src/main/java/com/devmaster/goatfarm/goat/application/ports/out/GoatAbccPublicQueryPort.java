package com.devmaster.goatfarm.goat.application.ports.out;

import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRaceOptionVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawPreviewVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawSearchResultVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchRequestVO;

import java.util.List;

public interface GoatAbccPublicQueryPort {

    List<GoatAbccRaceOptionVO> listRaces();

    GoatAbccRawSearchResultVO search(GoatAbccSearchRequestVO requestVO);

    GoatAbccRawPreviewVO preview(String externalId);
}
