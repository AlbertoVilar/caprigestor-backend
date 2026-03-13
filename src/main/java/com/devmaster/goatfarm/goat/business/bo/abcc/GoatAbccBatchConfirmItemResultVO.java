package com.devmaster.goatfarm.goat.business.bo.abcc;

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
public class GoatAbccBatchConfirmItemResultVO {

    private String externalId;
    private String registrationNumber;
    private String name;
    private String status;
    private String message;
}
