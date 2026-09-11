package com.devmaster.goatfarm.goat.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.audit.application.ports.in.OperationalAuditUseCase;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.goat.application.ports.out.GoatParentagePort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.business.bo.*;
import com.devmaster.goatfarm.goat.domain.Goat;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.domain.RegistrationIdentity;
import com.devmaster.goatfarm.goat.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoatBusinessTest {
    @Mock private GoatPersistencePort goatPort;
    @Mock private GoatFarmPersistencePort goatFarmPort;
    @Mock private OwnershipService ownershipService;
    @Mock private EntityFinder entityFinder;
    @Mock private OperationalAuditUseCase audit;
    @Mock private GoatParentagePort parentage;

    private GoatBusiness business;
    private GoatRequestVO request;
    private Goat goat;

    @BeforeEach
    void setUp() {
        business = new GoatBusiness(goatPort, goatFarmPort, ownershipService, entityFinder, audit, parentage);
        request = new GoatRequestVO();
        request.setRegistrationNumber("164322002"); request.setName("Xeque"); request.setGender(Gender.MACHO);
        request.setBreed(GoatBreed.ALPINA); request.setBirthDate(LocalDate.of(2025, 1, 1));
        request.setStatus(GoatStatus.ATIVO); request.setCategory(Category.PA); request.setTod("16432"); request.setToe("22002");
        request.setFarmId(1L); request.setUserId(1L);
        goat = Goat.rehydrate(new GoatId(77), RegistrationIdentity.of(request.getRegistrationNumber(), request.getTod(), request.getToe()),
                request.getName(), request.getGender(), request.getBreed(), request.getColor(), request.getBirthDate(), request.getStatus(),
                null, null, null, request.getCategory(), null, null, 1L, 1L, "Capril", "Alberto");
        lenient().when(parentage.resolve(any(), any(), any(), any())).thenReturn(new GoatParentagePort.ResolvedParentage(null, null));
        lenient().when(entityFinder.findOrThrow(any(), anyString())).thenAnswer(inv -> ((java.util.function.Supplier<?>) inv.getArgument(0)).get());
    }

    @Test
    void createsUsingDomainPort() {
        GoatFarm farm = new GoatFarm(); farm.setId(1L);
        User user = new User(); user.setId(1L);
        doNothing().when(ownershipService).verifyFarmManagement(1L);
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(farm));
        when(ownershipService.getCurrentUser()).thenReturn(user);
        when(goatPort.existsByRegistrationNumber("164322002")).thenReturn(false);
        when(goatPort.save(any(Goat.class))).thenAnswer(inv -> inv.getArgument(0));

        GoatResponseVO result = business.createGoat(1L, request);

        assertThat(result.getRegistrationNumber()).isEqualTo("164322002");
        verify(goatPort).save(any(Goat.class));
    }

    @Test
    void updatesByRegistrationNumberWithoutReinterpretingANumericRgAsTechnicalId() {
        doNothing().when(ownershipService).verifyFarmOwnership(1L);
        request.setRegistrationNumber("77");
        goat = Goat.rehydrate(new GoatId(99), RegistrationIdentity.of("77", request.getTod(), request.getToe()),
                request.getName(), request.getGender(), request.getBreed(), request.getColor(), request.getBirthDate(), request.getStatus(),
                null, null, null, request.getCategory(), null, null, 1L, 1L, "Capril", "Alberto");
        when(goatPort.findByRegistrationNumberAndFarmId("77", 1L)).thenReturn(Optional.of(goat));
        when(goatPort.save(any(Goat.class))).thenAnswer(inv -> inv.getArgument(0));

        request.setName("Xeque atualizado");
        GoatResponseVO result = business.updateGoat(1L, "77", request);

        assertThat(result.getName()).isEqualTo("Xeque atualizado");
        verify(goatPort).findByRegistrationNumberAndFarmId("77", 1L);
        verify(goatPort, never()).findByIdAndFarmId(any(), any());
    }

    @Test
    void rejectsExitForInactiveGoat() {
        Goat inactive = Goat.rehydrate(new GoatId(77), goat.registrationIdentity(), goat.name(), goat.gender(), goat.breed(), goat.color(),
                goat.birthDate(), GoatStatus.INATIVO, null, null, null, goat.category(), null, null, 1L, 1L, "Capril", "Alberto");
        when(goatPort.findByRegistrationNumberAndFarmId("77", 1L)).thenReturn(Optional.of(inactive));
        GoatExitRequestVO exit = GoatExitRequestVO.builder().exitType(GoatExitType.VENDA).exitDate(LocalDate.now()).build();

        assertThatThrownBy(() -> business.exitGoat(1L, "77", exit)).isInstanceOf(com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException.class);
        verify(goatPort, never()).save(any(Goat.class));
    }

    @Test
    void returnsCleanPageAndSummary() {
        when(goatPort.findAllByFarmId(1L, new com.devmaster.goatfarm.goat.application.ports.out.GoatPageQuery(0, 12, "")))
                .thenReturn(new com.devmaster.goatfarm.goat.application.ports.out.GoatPage<>(List.of(goat), 1, 0, 12));
        Page<GoatResponseVO> page = business.findAllGoatsByFarm(1L, PageRequest.of(0, 12));
        assertThat(page.getTotalElements()).isEqualTo(1);

        when(goatPort.getHerdSummary(1L)).thenReturn(new com.devmaster.goatfarm.goat.application.ports.out.GoatHerdSnapshot(1, 1, 0, 1, 0, 0, 0, List.of(), 1));
        assertThat(business.getGoatHerdSummary(1L).getTotal()).isEqualTo(1);
    }
}
