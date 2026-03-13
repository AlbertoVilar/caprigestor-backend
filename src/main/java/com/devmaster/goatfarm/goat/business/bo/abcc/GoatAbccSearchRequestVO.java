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
public class GoatAbccSearchRequestVO {

    private Integer raceId;
    private String affix;
    private Integer page;
    private String sex;
    private String tod;
    private String toe;
    private String name;
    private String dna;
}

