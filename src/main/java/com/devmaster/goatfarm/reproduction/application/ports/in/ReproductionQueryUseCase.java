package com.devmaster.goatfarm.reproduction.application.ports.in;

import com.devmaster.goatfarm.reproduction.business.bo.PregnancyResponseVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyDiagnosisAlertVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyDueAlertVO;
import com.devmaster.goatfarm.reproduction.business.bo.ReproductiveEventResponseVO;
import com.devmaster.goatfarm.reproduction.business.bo.DiagnosisRecommendationResponseVO;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;

import java.time.LocalDate;

public interface ReproductionQueryUseCase {
    PregnancyResponseVO getActivePregnancy(Long farmId, String goatId);
    PregnancyResponseVO getPregnancyById(Long farmId, String goatId, Long pregnancyId);
    PageResult<PregnancyResponseVO> getPregnancies(Long farmId, String goatId, PageQuery pageQuery);
    PageResult<ReproductiveEventResponseVO> getReproductiveEvents(Long farmId, String goatId, PageQuery pageQuery);
    DiagnosisRecommendationResponseVO getDiagnosisRecommendation(Long farmId, String goatId, LocalDate referenceDate);
    PageResult<PregnancyDiagnosisAlertVO> getPendingPregnancyDiagnosisAlerts(Long farmId, LocalDate referenceDate, PageQuery pageQuery);
    PageResult<PregnancyDueAlertVO> getPendingBirthAlerts(Long farmId, LocalDate referenceDate, PageQuery pageQuery);
}
