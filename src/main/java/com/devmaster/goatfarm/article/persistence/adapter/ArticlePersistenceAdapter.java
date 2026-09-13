package com.devmaster.goatfarm.article.persistence.adapter;

import com.devmaster.goatfarm.article.application.ports.out.ArticlePersistencePort;
import com.devmaster.goatfarm.article.business.bo.ArticleResponseVO;
import com.devmaster.goatfarm.article.enums.ArticleCategory;
import com.devmaster.goatfarm.article.persistence.entity.Article;
import com.devmaster.goatfarm.article.persistence.mapper.ArticlePersistenceMapper;
import com.devmaster.goatfarm.article.persistence.repository.ArticleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ArticlePersistenceAdapter implements ArticlePersistencePort {

    private final ArticleRepository articleRepository;
    private final ArticlePersistenceMapper mapper;

    public ArticlePersistenceAdapter(ArticleRepository articleRepository, ArticlePersistenceMapper mapper) {
        this.articleRepository = articleRepository;
        this.mapper = mapper;
    }

    @Override
    public ArticleResponseVO save(ArticleResponseVO article) {
        return mapper.toModel(articleRepository.save(mapper.toEntity(article)));
    }

    @Override
    public Optional<ArticleResponseVO> findById(Long id) {
        return articleRepository.findById(id).map(mapper::toModel);
    }

    @Override
    public Optional<ArticleResponseVO> findBySlug(String slug) {
        return articleRepository.findBySlug(slug).map(mapper::toModel);
    }

    @Override
    public Optional<ArticleResponseVO> findBySlugAndPublishedTrue(String slug) {
        return articleRepository.findBySlugAndPublishedTrue(slug).map(mapper::toModel);
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
    public Page<ArticleResponseVO> findAll(Pageable pageable) {
        return articleRepository.findAll(pageable).map(mapper::toModel);
    }

    @Override
    public Page<ArticleResponseVO> findPublished(ArticleCategory category, String q, Pageable pageable) {
        return articleRepository.findPublished(category, q, pageable).map(mapper::toModel);
    }

    @Override
    public List<ArticleResponseVO> findTop3HighlightedPublished() {
        return articleRepository.findTop3ByPublishedTrueAndHighlightedTrueOrderByPublishedAtDesc()
                .stream().map(mapper::toModel).toList();
    }

    @Override
    public Page<ArticleResponseVO> findLatestPublished(Pageable pageable) {
        return articleRepository.findByPublishedTrue(pageable).map(mapper::toModel);
    }

    @Override
    public void deleteById(Long id) {
        articleRepository.deleteById(id);
    }
}
