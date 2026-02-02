package com.devmaster.goatfarm.health.business.healthservice;

import com.devmaster.goatfarm.health.application.ports.in.FarmHealthAlertsQueryUseCase;
import com.devmaster.goatfarm.health.application.ports.out.HealthEventPersistencePort;
import com.devmaster.goatfarm.health.business.bo.FarmHealthAlertItemVO;
import com.devmaster.goatfarm.health.business.bo.FarmHealthAlertsResponseVO;
import com.devmaster.goatfarm.health.business.bo.HealthEventResponseVO;
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
    private final HealthEventBusinessMapper mapper;

    public FarmHealthAlertsBusiness(HealthEventPersistencePort persistencePort, HealthEventBusinessMapper mapper) {
        this.persistencePort = persistencePort;
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

        return FarmHealthAlertsResponseVO.builder()
                .dueTodayCount((int) dueToday.getTotalElements())
                .upcomingCount((int) upcoming.getTotalElements())
                .overdueCount((int) overdue.getTotalElements())
                .dueTodayTop(toAlertItems(dueToday))
                .upcomingTop(toAlertItems(upcoming))
                .overdueTop(toAlertItems(overdue))
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

    private int safeWindow(Integer windowDays) {
        if (windowDays == null || windowDays < 1) {
            return DEFAULT_WINDOW_DAYS;
        }
        return Math.min(windowDays, MAX_WINDOW_DAYS);
    }
}
