package com.devmaster.goatfarm.article.application.ports.out;

import com.devmaster.goatfarm.article.enums.ArticleCategory;
import com.devmaster.goatfarm.article.business.bo.ArticleResponseVO;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;

import java.util.List;
import java.util.Optional;

public interface ArticlePersistencePort {

    ArticleResponseVO save(ArticleResponseVO article);

    Optional<ArticleResponseVO> findById(Long id);

    Optional<ArticleResponseVO> findBySlug(String slug);

    Optional<ArticleResponseVO> findBySlugAndPublishedTrue(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    PageResult<ArticleResponseVO> findAll(PageQuery pageQuery);

    PageResult<ArticleResponseVO> findPublished(ArticleCategory category, String q, PageQuery pageQuery);

    List<ArticleResponseVO> findTop3HighlightedPublished();

    List<ArticleResponseVO> findLatestPublished(int limit);

    void deleteById(Long id);
}
