package com.devmaster.goatfarm.application.ports.in;

import com.devmaster.goatfarm.article.business.bo.ArticlePublicDetailResponseVO;
import com.devmaster.goatfarm.article.business.bo.ArticlePublicListResponseVO;
import com.devmaster.goatfarm.article.business.bo.ArticleResponseVO;
import com.devmaster.goatfarm.article.enums.ArticleCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ArticleQueryUseCase {

    Page<ArticlePublicListResponseVO> getPublishedArticles(ArticleCategory category, String q, Pageable pageable);

    ArticlePublicDetailResponseVO getPublishedArticleBySlug(String slug);

    List<ArticlePublicListResponseVO> getHighlights();

    Page<ArticleResponseVO> getAllArticles(Pageable pageable);

    ArticleResponseVO getArticleById(Long id);
}
