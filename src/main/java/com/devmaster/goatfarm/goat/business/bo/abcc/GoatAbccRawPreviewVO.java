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
public class GoatAbccRawPreviewVO {

    private String externalId;
    private String nome;
    private String registro;
    private String criador;
    private String proprietario;
    private String raca;
    private String pelagem;
    private String situacao;
    private String sexo;
    private String categoria;
    private String tod;
    private String toe;
    private String dataNascimento;
    private String paiNome;
    private String paiRegistro;
    private String maeNome;
    private String maeRegistro;
}

