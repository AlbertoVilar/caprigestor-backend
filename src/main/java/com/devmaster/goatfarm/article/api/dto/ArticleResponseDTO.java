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
public class ArticleResponseDTO {
    private Long id;
    private String title;
    private String slug;
    private String excerpt;
    private String contentMarkdown;
    private ArticleCategory category;
    private String coverImageUrl;
    private boolean published;
    private LocalDateTime publishedAt;
    private boolean highlighted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
