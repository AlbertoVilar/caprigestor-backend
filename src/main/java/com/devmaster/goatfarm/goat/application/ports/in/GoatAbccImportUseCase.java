package com.devmaster.goatfarm.goat.application.ports.in;

import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccBatchConfirmItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccBatchConfirmResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRaceOptionVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchResponseVO;

import java.util.List;

public interface GoatAbccImportUseCase {

    List<GoatAbccRaceOptionVO> listRaces(Long farmId);

    GoatAbccSearchResponseVO search(Long farmId, GoatAbccSearchRequestVO requestVO);

    GoatAbccPreviewResponseVO preview(Long farmId, GoatAbccPreviewRequestVO requestVO);

    GoatResponseVO confirm(Long farmId, String externalId, GoatRequestVO goatRequestVO);

    GoatAbccBatchConfirmResponseVO confirmBatch(Long farmId, List<GoatAbccBatchConfirmItemVO> items);
}
