package com.devmaster.goatfarm.article.application.ports.in;

import com.devmaster.goatfarm.article.business.bo.ArticlePublicDetailResponseVO;
import com.devmaster.goatfarm.article.business.bo.ArticlePublicListResponseVO;
import com.devmaster.goatfarm.article.business.bo.ArticleResponseVO;
import com.devmaster.goatfarm.article.enums.ArticleCategory;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;

import java.util.List;

public interface ArticleQueryUseCase {

    PageResult<ArticlePublicListResponseVO> getPublishedArticles(ArticleCategory category, String q, PageQuery pageQuery);

    ArticlePublicDetailResponseVO getPublishedArticleBySlug(String slug);

    List<ArticlePublicListResponseVO> getHighlights();

    PageResult<ArticleResponseVO> getAllArticles(PageQuery pageQuery);

    ArticleResponseVO getArticleById(Long id);
}
