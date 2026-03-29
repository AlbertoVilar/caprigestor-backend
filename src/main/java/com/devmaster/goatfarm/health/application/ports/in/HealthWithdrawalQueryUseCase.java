package com.devmaster.goatfarm.health.application.ports.in;

import com.devmaster.goatfarm.health.business.bo.GoatWithdrawalStatusVO;

import java.time.LocalDate;
import java.util.List;

public interface HealthWithdrawalQueryUseCase {

    GoatWithdrawalStatusVO getGoatWithdrawalStatus(Long farmId, String goatId, LocalDate referenceDate);

    List<GoatWithdrawalStatusVO> listActiveWithdrawalStatuses(Long farmId, LocalDate referenceDate);
}
