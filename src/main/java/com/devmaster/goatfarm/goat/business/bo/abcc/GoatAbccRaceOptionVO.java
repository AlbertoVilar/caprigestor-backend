package com.devmaster.goatfarm.goat.business.bo.abcc;

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
public class GoatAbccRaceOptionVO {

    private Integer id;
    private String name;
    private GoatBreed normalizedBreed;
}
