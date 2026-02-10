package com.devmaster.goatfarm.milk.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LactationDryOffAlertResponseDTO {
    private long totalPending;
    private List<LactationDryOffAlertItemDTO> alerts;
}
