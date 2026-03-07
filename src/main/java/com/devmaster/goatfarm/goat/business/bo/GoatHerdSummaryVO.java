package com.devmaster.goatfarm.goat.business.bo;

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
public class GoatHerdSummaryVO {

    private long total;
    private long males;
    private long females;
    private long active;
    private long inactive;
    private long sold;
    private long deceased;
    private List<GoatBreedSummaryVO> breeds;
}
