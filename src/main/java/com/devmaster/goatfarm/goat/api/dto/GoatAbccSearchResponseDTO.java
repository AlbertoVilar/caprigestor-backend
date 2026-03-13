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
public class GoatAbccSearchResponseDTO {

    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;
    private List<GoatAbccSearchItemDTO> items;
}

