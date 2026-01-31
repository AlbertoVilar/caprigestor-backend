package com.devmaster.goatfarm.health.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthEventDoneRequestDTO {
    private LocalDateTime performedAt;
    private String responsible;
    private String notes;
}
