package com.devmaster.goatfarm.article.business.bo;

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
public class ArticlePublicDetailResponseVO {
    private String title;
    private String slug;
    private String excerpt;
    private ArticleCategory category;
    private String coverImageUrl;
    private String contentMarkdown;
    private LocalDateTime publishedAt;
}
