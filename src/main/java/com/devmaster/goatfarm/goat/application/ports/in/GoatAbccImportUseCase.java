package com.devmaster.goatfarm.goat.application.ports.in;

import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccBatchConfirmItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccBatchConfirmResponseVO;

import java.util.List;

public interface GoatAbccImportUseCase {

    GoatResponseVO confirm(Long farmId, String externalId, GoatRequestVO goatRequestVO);

    GoatAbccBatchConfirmResponseVO confirmBatch(Long farmId, List<GoatAbccBatchConfirmItemVO> items);
}
