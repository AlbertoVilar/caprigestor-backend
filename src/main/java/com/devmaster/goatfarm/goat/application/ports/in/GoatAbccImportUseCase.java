package com.devmaster.goatfarm.goat.application.ports.in;

import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccBatchConfirmItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccBatchConfirmResponseVO;
import com.devmaster.goatfarm.goat.enums.GoatStatus;

import java.util.List;

public interface GoatAbccImportUseCase {

    GoatResponseVO confirm(Long farmId, String externalId, GoatRequestVO goatRequestVO);

    GoatAbccBatchConfirmResponseVO confirmBatch(Long farmId, GoatStatus localStatus, List<GoatAbccBatchConfirmItemVO> items);
}
