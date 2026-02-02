package com.devmaster.goatfarm.health.business.bo;

import java.util.List;
import lombok.Builder;

@Builder
public record FarmHealthAlertsResponseVO(
        int dueTodayCount,
        int upcomingCount,
        int overdueCount,
        List<FarmHealthAlertItemVO> dueTodayTop,
        List<FarmHealthAlertItemVO> upcomingTop,
        List<FarmHealthAlertItemVO> overdueTop,
        Integer windowDays
) {
}
