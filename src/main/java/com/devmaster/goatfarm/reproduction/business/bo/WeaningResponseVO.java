package com.devmaster.goatfarm.reproduction.business.bo;

import com.devmaster.goatfarm.goat.enums.GoatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeaningResponseVO {
    private String goatId;
    private LocalDate weaningDate;
    private GoatStatus previousStatus;
    private GoatStatus currentStatus;
    private ReproductiveEventResponseVO event;
}
