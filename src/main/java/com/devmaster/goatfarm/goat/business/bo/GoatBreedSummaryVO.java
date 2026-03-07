package com.devmaster.goatfarm.goat.business.bo;

import com.devmaster.goatfarm.goat.enums.GoatBreed;
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
public class GoatBreedSummaryVO {

    private GoatBreed breed;
    private String label;
    private long count;
}
