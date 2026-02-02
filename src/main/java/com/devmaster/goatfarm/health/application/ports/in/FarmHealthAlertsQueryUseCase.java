package com.devmaster.goatfarm.health.application.ports.in;

import com.devmaster.goatfarm.health.business.bo.FarmHealthAlertsResponseVO;

public interface FarmHealthAlertsQueryUseCase {
    FarmHealthAlertsResponseVO getAlerts(Long farmId, Integer windowDays);
}
