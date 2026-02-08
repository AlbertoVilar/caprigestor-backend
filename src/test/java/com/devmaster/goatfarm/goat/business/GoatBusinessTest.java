package com.devmaster.goatfarm.goat.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.goat.business.GoatBusiness;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goat.business.mapper.GoatBusinessMapper;
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoatBusinessTest {

    @Mock private GoatPersistencePort goatPort;
    @Mock private GoatFarmPersistencePort goatFarmPort;
    @Mock private OwnershipService ownershipService;
    @Mock private GoatBusinessMapper goatBusinessMapper;
    @Mock private EntityFinder entityFinder;

    private GoatBusiness goatBusiness;

    private GoatRequestVO requestVO;
    private GoatResponseVO responseVO;
    private Goat goat;
    private GoatFarm goatFarm;
    private User currentUser;

    @BeforeEach
    void setUp() {
        goatBusiness = new GoatBusiness(goatPort, goatFarmPort, ownershipService, goatBusinessMapper, entityFinder);

        // Configure default EntityFinder behavior
        lenient().when(entityFinder.findOrThrow(any(), anyString())).thenAnswer(invocation -> {
             java.util.function.Supplier<Optional<?>> supplier = invocation.getArgument(0);
             String errorMsg = invocation.getArgument(1);
             return supplier.get().orElseThrow(() -> new com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException(errorMsg));
        });

        // Dados base
        goatFarm = new GoatFarm();
        goatFarm.setId(1L);

        currentUser = new User();
        currentUser.setId(1L);

        requestVO = new GoatRequestVO();
        requestVO.setRegistrationNumber("164322002");
        requestVO.setName("Xeque V Do Capril Vilar");
        requestVO.setGender(Gender.MACHO);
        requestVO.setBreed(GoatBreed.ALPINA);
        requestVO.setColor("Marrom");
        requestVO.setBirthDate(LocalDate.of(2025, 1, 1));
        requestVO.setStatus(GoatStatus.ATIVO);
        requestVO.setTod("16432");
        requestVO.setToe("22002");
        requestVO.setCategory(Category.PO);
        requestVO.setFarmId(1L);
        requestVO.setUserId(1L);

        goat = new Goat();
        goat.setRegistrationNumber(requestVO.getRegistrationNumber());
        goat.setName(requestVO.getName());
        goat.setGender(requestVO.getGender());
        goat.setBreed(requestVO.getBreed());
        goat.setColor(requestVO.getColor());
        goat.setBirthDate(requestVO.getBirthDate());
        goat.setStatus(requestVO.getStatus());
        goat.setTod(requestVO.getTod());
        goat.setToe(requestVO.getToe());
        goat.setCategory(requestVO.getCategory());
        goat.setFarm(goatFarm);
        goat.setUser(currentUser);
        goat.setFather(null);
        goat.setMother(null);

        responseVO = new GoatResponseVO();
        responseVO.setRegistrationNumber(goat.getRegistrationNumber());
        responseVO.setName(goat.getName());
        responseVO.setGender(goat.getGender());
        responseVO.setBreed(goat.getBreed());
        responseVO.setColor(goat.getColor());
        responseVO.setBirthDate(goat.getBirthDate());
        responseVO.setStatus(goat.getStatus());
        responseVO.setTod(goat.getTod());
        responseVO.setToe(goat.getToe());
        responseVO.setCategory(goat.getCategory());
        responseVO.setFarmId(goatFarm.getId());
    }

    @Test
    @DisplayName("Deve criar cabra com sucesso quando pai e mãe são nulos")
    void shouldCreateGoatSuccessfully() {
        // ===== Arrange =====
        doNothing().when(ownershipService).verifyFarmOwnership(1L);
        when(goatPort.existsByRegistrationNumber("164322002")).thenReturn(false);
        when(goatFarmPort.findById(1L)).thenReturn(java.util.Optional.of(goatFarm));
        when(goatBusinessMapper.toEntity(requestVO)).thenReturn(goat);
        when(ownershipService.getCurrentUser()).thenReturn(currentUser);
        when(goatPort.save(any(Goat.class))).thenReturn(goat);
        when(goatBusinessMapper.toResponseVO(goat)).thenReturn(responseVO);

        // ===== Act =====
        GoatResponseVO resultado = goatBusiness.createGoat(1L, requestVO);

        // ===== Assert =====
        assertThat(resultado).isNotNull();
        assertThat(resultado.getRegistrationNumber()).isEqualTo("164322002");
        assertThat(resultado.getName()).isEqualTo("Xeque V Do Capril Vilar");
        assertThat(resultado.getGender()).isEqualTo(Gender.MACHO);
        assertThat(resultado.getBreed()).isEqualTo(GoatBreed.ALPINA);
        assertThat(resultado.getColor()).isEqualTo("Marrom");
        assertThat(resultado.getBirthDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(resultado.getStatus()).isEqualTo(GoatStatus.ATIVO);
        assertThat(resultado.getTod()).isEqualTo("16432");
        assertThat(resultado.getToe()).isEqualTo("22002");
        assertThat(resultado.getCategory()).isEqualTo(Category.PO);
        assertThat(resultado.getFarmId()).isEqualTo(1L);

        // ===== Verify =====
        verify(ownershipService, times(1)).verifyFarmOwnership(1L);
        verify(goatPort, times(1)).existsByRegistrationNumber("164322002");
        verify(goatFarmPort, times(1)).findById(1L);
        verify(goatPort, never()).findByRegistrationNumber(any());
        verify(goatBusinessMapper, times(1)).toEntity(requestVO);
        verify(ownershipService, times(1)).getCurrentUser();
        verify(goatPort, times(1)).save(any(Goat.class));
        verify(goatBusinessMapper, times(1)).toResponseVO(goat);
    }

    @Test
    @DisplayName("Deve criar cabra com sucesso quando há pai e mãe")
    void shouldCreateGoatWithParents() {
        // ===== ARRANGE - Dados adicionais =====
        // Criar o pai
        Goat fatherGoat = new Goat();
        fatherGoat.setRegistrationNumber("164321001");
        fatherGoat.setName("Reprodutor Alpha");
        fatherGoat.setGender(Gender.MACHO);

        // Criar a mãe
        Goat motherGoat = new Goat();
        motherGoat.setRegistrationNumber("164321002");
        motherGoat.setName("Matriz Beta");
        motherGoat.setGender(Gender.FEMEA);

        // Atualizar o requestVO para incluir pai e mãe
        requestVO.setFatherRegistrationNumber("164321001");
        requestVO.setMotherRegistrationNumber("164321002");

        // Atualizar o goat esperado
        goat.setFather(fatherGoat);
        goat.setMother(motherGoat);

        // ===== ARRANGE - Mocks =====
        doNothing().when(ownershipService).verifyFarmOwnership(1L);
        when(goatPort.existsByRegistrationNumber("164322002")).thenReturn(false);
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(goatFarm));
        when(goatPort.findByRegistrationNumber("164321001")).thenReturn(Optional.of(fatherGoat));
        when(goatPort.findByRegistrationNumber("164321002")).thenReturn(Optional.of(motherGoat));
        when(goatBusinessMapper.toEntity(requestVO)).thenReturn(goat);
        when(ownershipService.getCurrentUser()).thenReturn(currentUser);
        when(goatPort.save(any(Goat.class))).thenReturn(goat);
        when(goatBusinessMapper.toResponseVO(goat)).thenReturn(responseVO);

        // ===== ACT =====
        GoatResponseVO resultado = goatBusiness.createGoat(1L, requestVO);

        // ===== ASSERT =====
        assertThat(resultado).isNotNull();
        assertThat(resultado.getRegistrationNumber()).isEqualTo("164322002");
        assertThat(resultado.getName()).isEqualTo("Xeque V Do Capril Vilar");

        // ===== VERIFY =====
        verify(ownershipService, times(1)).verifyFarmOwnership(1L);
        verify(goatPort, times(1)).existsByRegistrationNumber("164322002");
        verify(goatFarmPort, times(1)).findById(1L);
        verify(goatPort, times(1)).findByRegistrationNumber("164321001"); // Pai
        verify(goatPort, times(1)).findByRegistrationNumber("164321002"); // Mãe
        verify(goatBusinessMapper, times(1)).toEntity(requestVO);
        verify(ownershipService, times(1)).getCurrentUser();
        verify(goatPort, times(1)).save(any(Goat.class));
        verify(goatBusinessMapper, times(1)).toResponseVO(goat);
    }
}

