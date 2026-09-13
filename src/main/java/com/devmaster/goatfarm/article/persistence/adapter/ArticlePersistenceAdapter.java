package com.devmaster.goatfarm.article.persistence.adapter;

import com.devmaster.goatfarm.article.application.ports.out.ArticlePersistencePort;
import com.devmaster.goatfarm.article.business.bo.ArticleResponseVO;
import com.devmaster.goatfarm.article.enums.ArticleCategory;
import com.devmaster.goatfarm.article.persistence.entity.Article;
import com.devmaster.goatfarm.article.persistence.mapper.ArticlePersistenceMapper;
import com.devmaster.goatfarm.article.persistence.repository.ArticleRepository;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.application.pagination.SortSpec;
import com.devmaster.goatfarm.application.pagination.SortDirection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public PageResult<ArticleResponseVO> findAll(PageQuery pageQuery) {
        var page = articleRepository.findAll(toPageable(pageQuery));
        return new PageResult<>(page.getContent().stream().map(mapper::toModel).toList(), page.getTotalElements(), page.getNumber(), page.getSize());
    }

    @Override
    public PageResult<ArticleResponseVO> findPublished(ArticleCategory category, String q, PageQuery pageQuery) {
        var page = articleRepository.findPublished(category, q, toPageable(pageQuery));
        return new PageResult<>(page.getContent().stream().map(mapper::toModel).toList(), page.getTotalElements(), page.getNumber(), page.getSize());
    }

    @Override
    public List<ArticleResponseVO> findTop3HighlightedPublished() {
        return articleRepository.findTop3ByPublishedTrueAndHighlightedTrueOrderByPublishedAtDesc()
                .stream().map(mapper::toModel).toList();
    }

    @Override
    public List<ArticleResponseVO> findLatestPublished(int limit) {
        if (limit <= 0) return List.of();
        return articleRepository.findByPublishedTrue(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedAt")))
                .getContent().stream().map(mapper::toModel).toList();
    }

    @Override
    public void deleteById(Long id) {
        articleRepository.deleteById(id);
    }

    private Pageable toPageable(PageQuery query) {
        Sort sort = Sort.by(query.sort().stream().map(this::toOrder).toList());
        return PageRequest.of(query.page(), query.size(), sort);
    }

    private Sort.Order toOrder(SortSpec spec) {
        return new Sort.Order(spec.direction() == SortDirection.ASC
                ? Sort.Direction.ASC : Sort.Direction.DESC, spec.field());
    }
}
