package com.devmaster.goatfarm.infrastructure.adapters.out.persistence;

import com.devmaster.goatfarm.application.ports.out.ArticlePersistencePort;
import com.devmaster.goatfarm.article.enums.ArticleCategory;
import com.devmaster.goatfarm.article.model.entity.Article;
import com.devmaster.goatfarm.article.model.repository.ArticleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ArticlePersistenceAdapter implements ArticlePersistencePort {

    private final ArticleRepository articleRepository;

    public ArticlePersistenceAdapter(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Override
    public Article save(Article article) {
        return articleRepository.save(article);
    }

    @Override
    public Optional<Article> findById(Long id) {
        return articleRepository.findById(id);
    }

    @Override
    public Optional<Article> findBySlug(String slug) {
        return articleRepository.findBySlug(slug);
    }

    @Override
    public Optional<Article> findBySlugAndPublishedTrue(String slug) {
        return articleRepository.findBySlugAndPublishedTrue(slug);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return articleRepository.existsBySlug(slug);
    }

    @Override
    public boolean existsBySlugAndIdNot(String slug, Long id) {
        return articleRepository.existsBySlugAndIdNot(slug, id);
    }

    @Override
    public Page<Article> findAll(Pageable pageable) {
        return articleRepository.findAll(pageable);
    }

    @Override
    public Page<Article> findPublished(ArticleCategory category, String q, Pageable pageable) {
        return articleRepository.findPublished(category, q, pageable);
    }

    @Override
    public List<Article> findTop3HighlightedPublished() {
        return articleRepository.findTop3ByPublishedTrueAndHighlightedTrueOrderByPublishedAtDesc();
    }

    @Override
    public Page<Article> findLatestPublished(Pageable pageable) {
        return articleRepository.findByPublishedTrue(pageable);
    }

    @Override
    public void deleteById(Long id) {
        articleRepository.deleteById(id);
    }
}
