package com.devmaster.goatfarm.reproduction.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PregnancyDiagnosisAlertResponseDTO {
    private long totalPending;
    private List<PregnancyDiagnosisAlertItemDTO> alerts;
}
