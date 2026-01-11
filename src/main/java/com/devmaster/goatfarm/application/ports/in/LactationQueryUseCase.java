package com.devmaster.goatfarm.application.ports.in;

import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LactationQueryUseCase {

    LactationResponseVO getActiveLactation(Long farmId, String goatId);

    LactationResponseVO getLactationById(Long farmId, String goatId, Long lactationId);

    Page<LactationResponseVO> getAllLactations(Long farmId, String goatId, Pageable pageable);
}
