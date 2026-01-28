package com.devmaster.goatfarm.application.ports.out;

import com.devmaster.goatfarm.article.enums.ArticleCategory;
import com.devmaster.goatfarm.article.model.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ArticlePersistencePort {

    Article save(Article article);

    Optional<Article> findById(Long id);

    Optional<Article> findBySlug(String slug);

    Optional<Article> findBySlugAndPublishedTrue(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    Page<Article> findAll(Pageable pageable);

    Page<Article> findPublished(ArticleCategory category, String q, Pageable pageable);

    List<Article> findTop3HighlightedPublished();

    Page<Article> findLatestPublished(Pageable pageable);

    void deleteById(Long id);
}
