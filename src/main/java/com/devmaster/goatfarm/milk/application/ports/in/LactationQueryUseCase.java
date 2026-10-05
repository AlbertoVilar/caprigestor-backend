package com.devmaster.goatfarm.milk.application.ports.in;

import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;
import com.devmaster.goatfarm.milk.business.bo.LactationSummaryResponseVO;
import com.devmaster.goatfarm.milk.business.bo.LactationDryOffAlertVO;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;

import java.time.LocalDate;

public interface LactationQueryUseCase {

    LactationResponseVO getActiveLactation(Long farmId, String goatId);

    LactationSummaryResponseVO getActiveLactationSummary(Long farmId, String goatId);

    LactationResponseVO getLactationById(Long farmId, String goatId, Long lactationId);

    LactationSummaryResponseVO getLactationSummary(Long farmId, String goatId, Long lactationId);

    PageResult<LactationResponseVO> getAllLactations(Long farmId, String goatId, PageQuery pageQuery);

    PageResult<LactationDryOffAlertVO> getDryOffAlerts(Long farmId, LocalDate referenceDate, PageQuery pageQuery);
}
