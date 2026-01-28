package com.devmaster.goatfarm.article.api.dto;

import com.devmaster.goatfarm.article.enums.ArticleCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticlePublicDetailResponseDTO {
    private String title;
    private String slug;
    private String excerpt;
    private ArticleCategory category;
    private String coverImageUrl;
    private String contentMarkdown;
    private LocalDateTime publishedAt;
}
