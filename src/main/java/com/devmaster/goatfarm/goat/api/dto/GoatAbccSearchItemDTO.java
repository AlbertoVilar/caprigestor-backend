package com.devmaster.goatfarm.goat.api.dto;

import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
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
public class GoatAbccSearchItemDTO {

    private String externalSource;
    private String externalId;
    private String nome;
    private String situacao;
    private String dna;
    private String tod;
    private String toe;
    private String criador;
    private String afixo;
    private String dataNascimento;
    private String sexo;
    private String raca;
    private String pelagem;
    private Gender normalizedGender;
    private GoatBreed normalizedBreed;
    private GoatStatus normalizedStatus;
    private List<String> normalizationWarnings;
}

