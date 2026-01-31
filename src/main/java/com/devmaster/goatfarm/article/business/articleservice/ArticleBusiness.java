package com.devmaster.goatfarm.article.business.articleservice;

import com.devmaster.goatfarm.article.application.ports.in.ArticleCommandUseCase;
import com.devmaster.goatfarm.article.application.ports.in.ArticleQueryUseCase;
import com.devmaster.goatfarm.article.application.ports.out.ArticlePersistencePort;
import com.devmaster.goatfarm.article.business.bo.ArticleHighlightRequestVO;
import com.devmaster.goatfarm.article.business.bo.ArticlePublishRequestVO;
import com.devmaster.goatfarm.article.business.bo.ArticlePublicDetailResponseVO;
import com.devmaster.goatfarm.article.business.bo.ArticlePublicListResponseVO;
import com.devmaster.goatfarm.article.business.bo.ArticleRequestVO;
import com.devmaster.goatfarm.article.business.bo.ArticleResponseVO;
import com.devmaster.goatfarm.article.enums.ArticleCategory;
import com.devmaster.goatfarm.article.api.mapper.ArticleMapper;
import com.devmaster.goatfarm.article.persistence.entity.Article;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ArticleBusiness implements ArticleCommandUseCase, ArticleQueryUseCase {

    private static final int HIGHLIGHT_COUNT = 3;

    private final ArticlePersistencePort articlePersistencePort;
    private final ArticleMapper articleMapper;

    public ArticleBusiness(ArticlePersistencePort articlePersistencePort, ArticleMapper articleMapper) {
        this.articlePersistencePort = articlePersistencePort;
        this.articleMapper = articleMapper;
    }

    @Override
    public ArticleResponseVO createDraft(ArticleRequestVO requestVO) {
        validateArticleContent(requestVO);

        String slug = generateSlug(requestVO.getTitle());
        if (articlePersistencePort.existsBySlug(slug)) {
            throw new DuplicateEntityException("Já existe um artigo com este slug.");
        }

        Article article = Article.builder()
                .title(requestVO.getTitle())
                .slug(slug)
                .excerpt(requestVO.getExcerpt())
                .contentMarkdown(requestVO.getContentMarkdown())
                .category(requestVO.getCategory())
                .coverImageUrl(requestVO.getCoverImageUrl())
                .published(false)
                .publishedAt(null)
                .highlighted(false)
                .build();

        Article saved = articlePersistencePort.save(article);
        return articleMapper.toResponseVO(saved);
    }

    @Override
    public ArticleResponseVO updateArticle(Long id, ArticleRequestVO requestVO) {
        validateArticleContent(requestVO);

        Article article = articlePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artigo não encontrado."));

        String slug = generateSlug(requestVO.getTitle());
        if (articlePersistencePort.existsBySlugAndIdNot(slug, id)) {
            throw new DuplicateEntityException("Já existe um artigo com este slug.");
        }

        article.setTitle(requestVO.getTitle());
        article.setSlug(slug);
        article.setExcerpt(requestVO.getExcerpt());
        article.setContentMarkdown(requestVO.getContentMarkdown());
        article.setCategory(requestVO.getCategory());
        article.setCoverImageUrl(requestVO.getCoverImageUrl());

        Article saved = articlePersistencePort.save(article);
        return articleMapper.toResponseVO(saved);
    }

    @Override
    public ArticleResponseVO publishArticle(Long id, ArticlePublishRequestVO requestVO) {
        Article article = articlePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artigo não encontrado."));

        if (requestVO.isPublished()) {
            validateArticleContent(article);
            article.setPublished(true);
            article.setPublishedAt(LocalDateTime.now());
        } else {
            article.setPublished(false);
            article.setPublishedAt(null);
        }

        Article saved = articlePersistencePort.save(article);
        return articleMapper.toResponseVO(saved);
    }

    @Override
    public ArticleResponseVO highlightArticle(Long id, ArticleHighlightRequestVO requestVO) {
        Article article = articlePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artigo não encontrado."));

        article.setHighlighted(requestVO.isHighlighted());
        Article saved = articlePersistencePort.save(article);
        return articleMapper.toResponseVO(saved);
    }

    @Override
    public void deleteArticle(Long id) {
        Article article = articlePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artigo não encontrado."));
        articlePersistencePort.deleteById(article.getId());
    }

    @Override
    public Page<ArticlePublicListResponseVO> getPublishedArticles(ArticleCategory category, String q, Pageable pageable) {
        String query = normalizeQuery(q);
        return articlePersistencePort.findPublished(category, query, pageable)
                .map(articleMapper::toPublicListResponseVO);
    }

    @Override
    public ArticlePublicDetailResponseVO getPublishedArticleBySlug(String slug) {
        Article article = articlePersistencePort.findBySlugAndPublishedTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Artigo não encontrado."));
        return articleMapper.toPublicDetailResponseVO(article);
    }

    @Override
    public List<ArticlePublicListResponseVO> getHighlights() {
        List<Article> highlighted = articlePersistencePort.findTop3HighlightedPublished();
        List<Article> result = new ArrayList<>(highlighted);

        if (result.size() < HIGHLIGHT_COUNT) {
            int remaining = HIGHLIGHT_COUNT - result.size();
            Pageable pageable = PageRequest.of(0, remaining + HIGHLIGHT_COUNT, Sort.by(Sort.Direction.DESC, "publishedAt"));
            List<Article> latest = articlePersistencePort.findLatestPublished(pageable).getContent();
            Set<Long> existingIds = new HashSet<>();
            result.forEach(article -> existingIds.add(article.getId()));
            for (Article article : latest) {
                if (existingIds.contains(article.getId())) {
                    continue;
                }
                result.add(article);
                existingIds.add(article.getId());
                if (result.size() == HIGHLIGHT_COUNT) {
                    break;
                }
            }
        }

        return result.stream().limit(HIGHLIGHT_COUNT).map(articleMapper::toPublicListResponseVO).toList();
    }

    @Override
    public Page<ArticleResponseVO> getAllArticles(Pageable pageable) {
        return articlePersistencePort.findAll(pageable).map(articleMapper::toResponseVO);
    }

    @Override
    public ArticleResponseVO getArticleById(Long id) {
        Article article = articlePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artigo não encontrado."));
        return articleMapper.toResponseVO(article);
    }

    private void validateArticleContent(ArticleRequestVO requestVO) {
        if (requestVO.getTitle() == null || requestVO.getTitle().isBlank()) {
            throw new BusinessRuleException("title", "Título é obrigatório");
        }
        if (requestVO.getExcerpt() == null || requestVO.getExcerpt().isBlank()) {
            throw new BusinessRuleException("excerpt", "Resumo é obrigatório");
        }
        if (requestVO.getContentMarkdown() == null || requestVO.getContentMarkdown().isBlank()) {
            throw new BusinessRuleException("contentMarkdown", "Conteúdo é obrigatório");
        }
        if (requestVO.getCategory() == null) {
            throw new BusinessRuleException("category", "Categoria é obrigatória");
        }
    }

    private void validateArticleContent(Article article) {
        ArticleRequestVO requestVO = ArticleRequestVO.builder()
                .title(article.getTitle())
                .excerpt(article.getExcerpt())
                .contentMarkdown(article.getContentMarkdown())
                .category(article.getCategory())
                .coverImageUrl(article.getCoverImageUrl())
                .build();
        validateArticleContent(requestVO);
    }

    private String normalizeQuery(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        return "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private String generateSlug(String title) {
        if (title == null) {
            return "";
        }
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String slug = normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return slug.isBlank() ? "artigo" : slug;
    }
}
