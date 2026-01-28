package com.devmaster.goatfarm.article.api.dto;

import com.devmaster.goatfarm.article.enums.ArticleCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ArticleRequestDTO {

    @NotBlank(message = "Título é obrigatório")
    private String title;

    @NotBlank(message = "Resumo é obrigatório")
    private String excerpt;

    @NotBlank(message = "Conteúdo é obrigatório")
    private String contentMarkdown;

    @NotNull(message = "Categoria é obrigatória")
    private ArticleCategory category;

    private String coverImageUrl;
}
