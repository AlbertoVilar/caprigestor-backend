package com.devmaster.goatfarm.architecture;

import com.devmaster.goatfarm.goatownership.api.controller.FarmGoatRegistryController;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalLactationDTO;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalMilkLactationResponseDTO;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalMilkProductionDTO;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatHistoricalMilkLactationQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.out.FarmGoatHistoricalMilkLactationQueryPort;
import com.devmaster.goatfarm.goatownership.business.FarmGoatHistoricalMilkLactationQueryBusiness;
import com.devmaster.goatfarm.milk.persistence.adapter.FarmGoatHistoricalMilkLactationPersistenceAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.assertj.core.api.Assertions.assertThat;

class W134BArchitectureGuardTest {

    @Test
    @DisplayName("FarmGoatRegistryController has no repository dependency")
    void controllerHasNoRepositoryDependency() {
        for (Field field : FarmGoatRegistryController.class.getDeclaredFields()) {
            assertThat(field.getType().getName())
                    .withFailMessage("Controller field %s must not be a Repository", field.getName())
                    .doesNotContain("Repository");
        }
    }

    @Test
    @DisplayName("Historical Milk/Lactation Use Case port does not expose JPA types or API DTOs")
    void historicalUseCasePortCleanFromJpaAndDtos() {
        Class<?> useCase = FarmGoatHistoricalMilkLactationQueryUseCase.class;
        for (Method method : useCase.getDeclaredMethods()) {
            assertThat(method.getReturnType().getPackageName()).doesNotContain("jakarta.persistence");
            assertThat(method.getReturnType().getPackageName()).doesNotContain("dto");
            for (Parameter param : method.getParameters()) {
                assertThat(param.getType().getPackageName()).doesNotContain("jakarta.persistence");
                assertThat(param.getType().getPackageName()).doesNotContain("dto");
            }
        }
    }

    @Test
    @DisplayName("Historical Milk/Lactation Query Port does not expose JPA types or API DTOs")
    void historicalQueryPortCleanFromJpaAndDtos() {
        Class<?> port = FarmGoatHistoricalMilkLactationQueryPort.class;
        for (Method method : port.getDeclaredMethods()) {
            assertThat(method.getReturnType().getPackageName()).doesNotContain("jakarta.persistence");
            assertThat(method.getReturnType().getPackageName()).doesNotContain("dto");
            for (Parameter param : method.getParameters()) {
                assertThat(param.getType().getPackageName()).doesNotContain("jakarta.persistence");
                assertThat(param.getType().getPackageName()).doesNotContain("dto");
            }
        }
    }

    @Test
    @DisplayName("Historical API DTOs do not depend on persistence entities")
    void historicalDtosDoNotDependOnPersistenceEntities() {
        Class<?>[] dtos = new Class<?>[]{
                FarmGoatHistoricalMilkLactationResponseDTO.class,
                FarmGoatHistoricalLactationDTO.class,
                FarmGoatHistoricalMilkProductionDTO.class
        };

        for (Class<?> dto : dtos) {
            for (Field field : dto.getDeclaredFields()) {
                assertThat(field.getType().getPackageName())
                        .withFailMessage("DTO field %s in %s must not import from persistence.entity", field.getName(), dto.getSimpleName())
                        .doesNotContain("persistence.entity");
            }
        }
    }

    @Test
    @DisplayName("Historical business depends only on application ports and use cases")
    void historicalBusinessDependsOnlyOnPorts() {
        for (Field field : FarmGoatHistoricalMilkLactationQueryBusiness.class.getDeclaredFields()) {
            assertThat(field.getType().getName())
                    .withFailMessage("Business field %s must not be a Repository or Entity", field.getName())
                    .doesNotContain("Repository")
                    .doesNotContain("Entity");
        }
    }

    @Test
    @DisplayName("Persistence adapter belongs to milk module and implements goatownership port")
    void persistenceAdapterOwnedByMilkModule() {
        Class<?> adapterClass = FarmGoatHistoricalMilkLactationPersistenceAdapter.class;
        assertThat(adapterClass.getPackageName()).isEqualTo("com.devmaster.goatfarm.milk.persistence.adapter");
        assertThat(FarmGoatHistoricalMilkLactationQueryPort.class.isAssignableFrom(adapterClass)).isTrue();

        for (Field field : adapterClass.getDeclaredFields()) {
            assertThat(field.getType().getName())
                    .withFailMessage("Adapter field %s must not depend on goatownership persistence", field.getName())
                    .doesNotContain("GoatOwnershipPeriodRepository")
                    .doesNotContain("com.devmaster.goatfarm.goatownership.persistence");
        }
    }
}