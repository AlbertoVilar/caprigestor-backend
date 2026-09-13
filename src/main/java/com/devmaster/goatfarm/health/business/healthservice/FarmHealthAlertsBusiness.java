package com.devmaster.goatfarm.health.business.healthservice;

import com.devmaster.goatfarm.health.application.ports.in.FarmHealthAlertsQueryUseCase;
import com.devmaster.goatfarm.health.application.ports.in.HealthWithdrawalQueryUseCase;
import com.devmaster.goatfarm.health.application.ports.out.HealthEventPersistencePort;
import com.devmaster.goatfarm.health.application.model.HealthEventWindow;
import com.devmaster.goatfarm.health.business.bo.FarmHealthAlertItemVO;
import com.devmaster.goatfarm.health.business.bo.FarmHealthAlertsResponseVO;
import com.devmaster.goatfarm.health.business.bo.GoatWithdrawalStatusVO;
import com.devmaster.goatfarm.health.business.bo.HealthEventResponseVO;
import com.devmaster.goatfarm.health.business.bo.WithdrawalAlertItemVO;
import com.devmaster.goatfarm.health.business.mapper.HealthEventBusinessMapper;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class FarmHealthAlertsBusiness implements FarmHealthAlertsQueryUseCase {

    private static final int DEFAULT_WINDOW_DAYS = 7;
    private static final int MAX_WINDOW_DAYS = 30;

    private final HealthEventPersistencePort persistencePort;
    private final HealthWithdrawalQueryUseCase withdrawalQueryUseCase;
    private final HealthEventBusinessMapper mapper;

    public FarmHealthAlertsBusiness(
            HealthEventPersistencePort persistencePort,
            HealthWithdrawalQueryUseCase withdrawalQueryUseCase,
            HealthEventBusinessMapper mapper
    ) {
        this.persistencePort = persistencePort;
        this.withdrawalQueryUseCase = withdrawalQueryUseCase;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public FarmHealthAlertsResponseVO getAlerts(Long farmId, Integer windowDays) {
        int safeWindowDays = safeWindow(windowDays);
        LocalDate today = LocalDate.now();
        LocalDate upcomingTo = today.plusDays(safeWindowDays);
        LocalDate fromUpcoming = today.plusDays(1);
        LocalDate toOverdue = today.minusDays(1);

        HealthEventWindow dueToday = persistencePort
                .findNextScheduledEvents(farmId, today, today, null, HealthEventStatus.AGENDADO, 5);

        HealthEventWindow upcoming = persistencePort
                .findNextScheduledEvents(farmId, fromUpcoming, upcomingTo, null, HealthEventStatus.AGENDADO, 5);

        HealthEventWindow overdue = persistencePort
                .findNextScheduledEvents(farmId, null, toOverdue, null, HealthEventStatus.AGENDADO, 5);

        var dueTodayResponses = dueToday.content().stream().map(mapper::toResponseVO).toList();
        var upcomingResponses = upcoming.content().stream().map(mapper::toResponseVO).toList();
        var overdueResponses = overdue.content().stream().map(mapper::toResponseVO).toList();

        List<GoatWithdrawalStatusVO> activeWithdrawalStatuses = withdrawalQueryUseCase
                .listActiveWithdrawalStatuses(farmId, today);

        List<WithdrawalAlertItemVO> milkWithdrawalTop = activeWithdrawalStatuses.stream()
                .filter(GoatWithdrawalStatusVO::hasActiveMilkWithdrawal)
                .map(status -> toWithdrawalAlertItem(status.goatTechnicalId(), status.goatId(), status.milkWithdrawal(), today))
                .sorted(java.util.Comparator.comparing(WithdrawalAlertItemVO::withdrawalEndDate))
                .limit(5)
                .toList();

        List<WithdrawalAlertItemVO> meatWithdrawalTop = activeWithdrawalStatuses.stream()
                .filter(GoatWithdrawalStatusVO::hasActiveMeatWithdrawal)
                .map(status -> toWithdrawalAlertItem(status.goatTechnicalId(), status.goatId(), status.meatWithdrawal(), today))
                .sorted(java.util.Comparator.comparing(WithdrawalAlertItemVO::withdrawalEndDate))
                .limit(5)
                .toList();

        return FarmHealthAlertsResponseVO.builder()
                .dueTodayCount((int) dueToday.totalElements())
                .upcomingCount((int) upcoming.totalElements())
                .overdueCount((int) overdue.totalElements())
                .activeMilkWithdrawalCount((int) activeWithdrawalStatuses.stream().filter(GoatWithdrawalStatusVO::hasActiveMilkWithdrawal).count())
                .activeMeatWithdrawalCount((int) activeWithdrawalStatuses.stream().filter(GoatWithdrawalStatusVO::hasActiveMeatWithdrawal).count())
                .dueTodayTop(toAlertItems(dueTodayResponses))
                .upcomingTop(toAlertItems(upcomingResponses))
                .overdueTop(toAlertItems(overdueResponses))
                .milkWithdrawalTop(milkWithdrawalTop)
                .meatWithdrawalTop(meatWithdrawalTop)
                .windowDays(safeWindowDays)
                .build();
    }

    private List<FarmHealthAlertItemVO> toAlertItems(List<HealthEventResponseVO> events) {
        return events.stream()
                .map(this::toAlertItem)
                .toList();
    }

    private FarmHealthAlertItemVO toAlertItem(HealthEventResponseVO vo) {
        return FarmHealthAlertItemVO.builder()
                .id(vo.id())
                .goatTechnicalId(vo.goatTechnicalId())
                .goatId(vo.goatId())
                .type(vo.type())
                .status(vo.status())
                .title(vo.title())
                .scheduledDate(vo.scheduledDate())
                .overdue(vo.overdue())
                .build();
    }

    private WithdrawalAlertItemVO toWithdrawalAlertItem(
            Long goatTechnicalId,
            String goatId,
            com.devmaster.goatfarm.health.business.bo.HealthWithdrawalOriginVO origin,
            LocalDate referenceDate
    ) {
        return WithdrawalAlertItemVO.builder()
                .eventId(origin.eventId())
                .goatTechnicalId(goatTechnicalId)
                .goatId(goatId)
                .title(origin.title())
                .productName(origin.productName())
                .activeIngredient(origin.activeIngredient())
                .performedDate(origin.performedDate())
                .withdrawalEndDate(origin.withdrawalEndDate())
                .daysRemaining((int) java.time.temporal.ChronoUnit.DAYS.between(referenceDate, origin.withdrawalEndDate()))
                .build();
    }

    private int safeWindow(Integer windowDays) {
        if (windowDays == null || windowDays < 1) {
            return DEFAULT_WINDOW_DAYS;
        }
        return Math.min(windowDays, MAX_WINDOW_DAYS);
    }
}
