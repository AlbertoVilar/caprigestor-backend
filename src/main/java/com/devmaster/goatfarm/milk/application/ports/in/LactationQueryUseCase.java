package com.devmaster.goatfarm.milk.application.ports.in;

import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;
import com.devmaster.goatfarm.milk.business.bo.LactationSummaryResponseVO;
import com.devmaster.goatfarm.milk.business.bo.LactationDryOffAlertVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface LactationQueryUseCase {

    LactationResponseVO getActiveLactation(Long farmId, String goatId);

    LactationSummaryResponseVO getActiveLactationSummary(Long farmId, String goatId);

    LactationResponseVO getLactationById(Long farmId, String goatId, Long lactationId);

    LactationSummaryResponseVO getLactationSummary(Long farmId, String goatId, Long lactationId);

    Page<LactationResponseVO> getAllLactations(Long farmId, String goatId, Pageable pageable);

    Page<LactationDryOffAlertVO> getDryOffAlerts(Long farmId, LocalDate referenceDate, Pageable pageable);
}
