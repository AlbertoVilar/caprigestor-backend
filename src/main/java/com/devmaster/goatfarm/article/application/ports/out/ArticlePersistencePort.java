package com.devmaster.goatfarm.article.application.ports.out;

import com.devmaster.goatfarm.article.enums.ArticleCategory;
import com.devmaster.goatfarm.article.business.bo.ArticleResponseVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ArticlePersistencePort {

    ArticleResponseVO save(ArticleResponseVO article);

    Optional<ArticleResponseVO> findById(Long id);

    Optional<ArticleResponseVO> findBySlug(String slug);

    Optional<ArticleResponseVO> findBySlugAndPublishedTrue(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    Page<ArticleResponseVO> findAll(Pageable pageable);

    Page<ArticleResponseVO> findPublished(ArticleCategory category, String q, Pageable pageable);

    List<ArticleResponseVO> findTop3HighlightedPublished();

    Page<ArticleResponseVO> findLatestPublished(Pageable pageable);

    void deleteById(Long id);
}
