package com.devmaster.goatfarm.health.api.dto;

import com.devmaster.goatfarm.health.domain.enums.AdministrationRoute;
import com.devmaster.goatfarm.health.domain.enums.DoseUnit;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record HealthEventUpdateRequestDTO(
    @NotNull(message = "O tipo do evento é obrigatório")
    HealthEventType type,

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 100, message = "O título deve ter no máximo 100 caracteres")
    String title,

    @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
    String description,

    @NotNull(message = "A data agendada é obrigatória")
    LocalDate scheduledDate,

    @Size(max = 1000, message = "As notas devem ter no máximo 1000 caracteres")
    String notes,

    @Size(max = 100, message = "O nome do produto deve ter no máximo 100 caracteres")
    String productName,

    @Size(max = 100, message = "O princípio ativo deve ter no máximo 100 caracteres")
    String activeIngredient,

    @Positive(message = "A dose deve ser maior que zero")
    @Digits(integer = 6, fraction = 3, message = "A dose deve ter no máximo 6 dígitos inteiros e 3 decimais")
    BigDecimal dose,

    DoseUnit doseUnit,

    AdministrationRoute route,

    @Size(max = 50, message = "O número do lote deve ter no máximo 50 caracteres")
    String batchNumber,

    @PositiveOrZero(message = "O período de carência (leite) não pode ser negativo")
    Integer withdrawalMilkDays,

    @PositiveOrZero(message = "O período de carência (carne) não pode ser negativo")
    Integer withdrawalMeatDays
) {}
