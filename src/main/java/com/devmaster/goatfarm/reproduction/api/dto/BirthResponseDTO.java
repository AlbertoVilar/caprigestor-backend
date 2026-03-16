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
public class BirthResponseDTO {
    private PregnancyResponseDTO pregnancy;
    private ReproductiveEventResponseDTO closeEvent;
    private List<BirthKidResponseDTO> kids;
}
