package com.devmaster.goatfarm.reproduction.application.ports.in;

import com.devmaster.goatfarm.reproduction.business.bo.PregnancyResponseVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyDiagnosisAlertVO;
import com.devmaster.goatfarm.reproduction.business.bo.ReproductiveEventResponseVO;
import com.devmaster.goatfarm.reproduction.business.bo.DiagnosisRecommendationResponseVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface ReproductionQueryUseCase {
    PregnancyResponseVO getActivePregnancy(Long farmId, String goatId);
    PregnancyResponseVO getPregnancyById(Long farmId, String goatId, Long pregnancyId);
    Page<PregnancyResponseVO> getPregnancies(Long farmId, String goatId, Pageable pageable);
    Page<ReproductiveEventResponseVO> getReproductiveEvents(Long farmId, String goatId, Pageable pageable);
    DiagnosisRecommendationResponseVO getDiagnosisRecommendation(Long farmId, String goatId, LocalDate referenceDate);
    Page<PregnancyDiagnosisAlertVO> getPendingPregnancyDiagnosisAlerts(Long farmId, LocalDate referenceDate, Pageable pageable);
}
