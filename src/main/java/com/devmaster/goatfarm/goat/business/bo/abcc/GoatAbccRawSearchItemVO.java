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
public class GoatAbccRawSearchItemVO {

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
}

