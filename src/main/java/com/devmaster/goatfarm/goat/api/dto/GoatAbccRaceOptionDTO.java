package com.devmaster.goatfarm.goat.api.dto;

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
public class GoatAbccRaceOptionDTO {

    private Integer id;
    private String name;
    private GoatBreed normalizedBreed;
}
