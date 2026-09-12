package com.devmaster.goatfarm.reproduction.application.ports.in;

import com.devmaster.goatfarm.reproduction.business.bo.WeaningRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.WeaningResponseVO;

public interface WeaningCommandUseCase {
    WeaningResponseVO registerWeaning(Long farmId, String goatId, WeaningRequestVO vo);
}
