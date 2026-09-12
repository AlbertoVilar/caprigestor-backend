package com.devmaster.goatfarm.reproduction.application.ports.in;

import com.devmaster.goatfarm.reproduction.business.bo.BirthRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.BirthResponseVO;

public interface BirthCommandUseCase {
    BirthResponseVO registerBirth(Long farmId, String goatId, Long pregnancyId, BirthRequestVO vo);
}
