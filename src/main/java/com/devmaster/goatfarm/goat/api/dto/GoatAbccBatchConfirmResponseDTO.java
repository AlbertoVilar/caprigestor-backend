package com.devmaster.goatfarm.goat.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoatAbccBatchConfirmResponseDTO {

    private Integer totalSelected;
    private Integer totalImported;
    private Integer totalSkippedDuplicate;
    private Integer totalSkippedTodMismatch;
    private Integer totalError;
    private List<GoatAbccBatchConfirmItemResultDTO> results;
}
