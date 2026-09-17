package com.devmaster.goatfarm.architecture;

import com.devmaster.goatfarm.goatownership.api.controller.FarmGoatRegistryController;
import com.devmaster.goatfarm.goatownership.business.FarmGoatHistoricalGenealogyQueryBusiness;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.assertj.core.api.Assertions.assertThat;

class W134A2ArchitectureGuardTest {

    @Test
    @DisplayName("Historical business must not depend directly on GenealogyAbccQueryPort")
    void historicalBusinessDoesNotDependOnGenealogyAbccQueryPort() {
        for (Field field : FarmGoatHistoricalGenealogyQueryBusiness.class.getDeclaredFields()) {
            assertThat(field.getType().getName())
                    .withFailMessage("FarmGoatHistoricalGenealogyQueryBusiness must not depend on output port %s", field.getType().getName())
                    .doesNotContain("GenealogyAbccQueryPort");
        }
    }

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
    @DisplayName("Historical API DTOs do not depend on genealogy.business.bo package")
    void historicalDtoDoesNotDependOnGenealogyBusinessBo() {
        Class<?>[] dtos = new Class<?>[]{
                com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalGenealogyResponseDTO.class,
                com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalGenealogyNodeDTO.class,
                com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalIntegrationDTO.class
        };

        for (Class<?> dto : dtos) {
            for (Field field : dto.getDeclaredFields()) {
                assertThat(field.getType().getPackageName())
                        .withFailMessage("DTO field %s in %s must not import from business.bo", field.getName(), dto.getSimpleName())
                        .doesNotContain("genealogy.business.bo");
            }
        }
    }

    @Test
    @DisplayName("Historical Use Case ports do not expose JPA types or API DTOs")
    void historicalUseCasePortCleanFromJpaAndDtos() {
        Class<?> useCase = com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatHistoricalGenealogyQueryUseCase.class;
        for (Method method : useCase.getDeclaredMethods()) {
            assertThat(method.getReturnType().getPackageName()).doesNotContain("jakarta.persistence");
            assertThat(method.getReturnType().getPackageName()).doesNotContain("dto");
            for (Parameter param : method.getParameters()) {
                assertThat(param.getType().getPackageName()).doesNotContain("jakarta.persistence");
                assertThat(param.getType().getPackageName()).doesNotContain("dto");
            }
        }
    }
}
