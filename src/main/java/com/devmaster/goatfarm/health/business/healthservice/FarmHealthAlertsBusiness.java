package com.devmaster.goatfarm.health.business.healthservice;

import com.devmaster.goatfarm.health.application.ports.in.FarmHealthAlertsQueryUseCase;
import com.devmaster.goatfarm.health.application.ports.in.HealthWithdrawalQueryUseCase;
import com.devmaster.goatfarm.health.application.ports.out.HealthEventPersistencePort;
import com.devmaster.goatfarm.health.business.bo.FarmHealthAlertItemVO;
import com.devmaster.goatfarm.health.business.bo.FarmHealthAlertsResponseVO;
import com.devmaster.goatfarm.health.business.bo.GoatWithdrawalStatusVO;
import com.devmaster.goatfarm.health.business.bo.HealthEventResponseVO;
import com.devmaster.goatfarm.health.business.bo.WithdrawalAlertItemVO;
import com.devmaster.goatfarm.health.business.mapper.HealthEventBusinessMapper;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

        Pageable topPageable = PageRequest.of(0, 5, Sort.by("scheduledDate").ascending());

        Page<HealthEventResponseVO> dueToday = persistencePort
                .findByFarmIdAndPeriod(farmId, today, today, null, HealthEventStatus.AGENDADO, topPageable)
                .map(mapper::toResponseVO);

        Page<HealthEventResponseVO> upcoming = persistencePort
                .findByFarmIdAndPeriod(farmId, fromUpcoming, upcomingTo, null, HealthEventStatus.AGENDADO, topPageable)
                .map(mapper::toResponseVO);

        Page<HealthEventResponseVO> overdue = persistencePort
                .findByFarmIdAndPeriod(farmId, null, toOverdue, null, HealthEventStatus.AGENDADO, topPageable)
                .map(mapper::toResponseVO);

        List<GoatWithdrawalStatusVO> activeWithdrawalStatuses = withdrawalQueryUseCase
                .listActiveWithdrawalStatuses(farmId, today);

        List<WithdrawalAlertItemVO> milkWithdrawalTop = activeWithdrawalStatuses.stream()
                .filter(GoatWithdrawalStatusVO::hasActiveMilkWithdrawal)
                .map(status -> toWithdrawalAlertItem(status.goatId(), status.milkWithdrawal(), today))
                .sorted(java.util.Comparator.comparing(WithdrawalAlertItemVO::withdrawalEndDate))
                .limit(5)
                .toList();

        List<WithdrawalAlertItemVO> meatWithdrawalTop = activeWithdrawalStatuses.stream()
                .filter(GoatWithdrawalStatusVO::hasActiveMeatWithdrawal)
                .map(status -> toWithdrawalAlertItem(status.goatId(), status.meatWithdrawal(), today))
                .sorted(java.util.Comparator.comparing(WithdrawalAlertItemVO::withdrawalEndDate))
                .limit(5)
                .toList();

        return FarmHealthAlertsResponseVO.builder()
                .dueTodayCount((int) dueToday.getTotalElements())
                .upcomingCount((int) upcoming.getTotalElements())
                .overdueCount((int) overdue.getTotalElements())
                .activeMilkWithdrawalCount((int) activeWithdrawalStatuses.stream().filter(GoatWithdrawalStatusVO::hasActiveMilkWithdrawal).count())
                .activeMeatWithdrawalCount((int) activeWithdrawalStatuses.stream().filter(GoatWithdrawalStatusVO::hasActiveMeatWithdrawal).count())
                .dueTodayTop(toAlertItems(dueToday))
                .upcomingTop(toAlertItems(upcoming))
                .overdueTop(toAlertItems(overdue))
                .milkWithdrawalTop(milkWithdrawalTop)
                .meatWithdrawalTop(meatWithdrawalTop)
                .windowDays(safeWindowDays)
                .build();
    }

    private List<FarmHealthAlertItemVO> toAlertItems(Page<HealthEventResponseVO> page) {
        return page.getContent().stream()
                .map(this::toAlertItem)
                .toList();
    }

    private FarmHealthAlertItemVO toAlertItem(HealthEventResponseVO vo) {
        return FarmHealthAlertItemVO.builder()
                .id(vo.id())
                .goatId(vo.goatId())
                .type(vo.type())
                .status(vo.status())
                .title(vo.title())
                .scheduledDate(vo.scheduledDate())
                .overdue(vo.overdue())
                .build();
    }

    private WithdrawalAlertItemVO toWithdrawalAlertItem(
            String goatId,
            com.devmaster.goatfarm.health.business.bo.HealthWithdrawalOriginVO origin,
            LocalDate referenceDate
    ) {
        return WithdrawalAlertItemVO.builder()
                .eventId(origin.eventId())
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
