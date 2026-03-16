package com.devmaster.goatfarm.goat.business.bo;

import com.devmaster.goatfarm.goat.enums.GoatExitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoatExitRequestVO {
    private GoatExitType exitType;
    private LocalDate exitDate;
    private String notes;
}
