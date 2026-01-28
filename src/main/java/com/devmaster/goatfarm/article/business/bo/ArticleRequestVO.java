package com.devmaster.goatfarm.article.business.bo;

import com.devmaster.goatfarm.article.enums.ArticleCategory;
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
public class ArticleRequestVO {
    private String title;
    private String excerpt;
    private String contentMarkdown;
    private ArticleCategory category;
    private String coverImageUrl;
}
