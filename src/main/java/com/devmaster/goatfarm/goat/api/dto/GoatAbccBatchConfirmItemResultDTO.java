package com.devmaster.goatfarm.goat.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoatAbccBatchConfirmItemResultDTO {

    private String externalId;
    private String registrationNumber;
    private String name;
    private String status;
    private String message;
}
