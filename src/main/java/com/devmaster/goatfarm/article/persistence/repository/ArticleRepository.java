package com.devmaster.goatfarm.article.persistence.repository;

import com.devmaster.goatfarm.article.enums.ArticleCategory;
import com.devmaster.goatfarm.article.persistence.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    Optional<Article> findBySlug(String slug);

    Optional<Article> findBySlugAndPublishedTrue(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    @Query("""
        select a from Article a
        where a.published = true
          and (:category is null or a.category = :category)
          and (:q is null or lower(a.title) like :q
               or lower(a.excerpt) like :q)
        """)
    Page<Article> findPublished(
            @Param("category") ArticleCategory category,
            @Param("q") String q,
            Pageable pageable
    );

    List<Article> findTop3ByPublishedTrueAndHighlightedTrueOrderByPublishedAtDesc();

    Page<Article> findByPublishedTrue(Pageable pageable);
}
