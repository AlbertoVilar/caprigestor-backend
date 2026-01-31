package com.devmaster.goatfarm.article.api.controller;

import com.devmaster.goatfarm.article.application.ports.in.ArticleQueryUseCase;
import com.devmaster.goatfarm.article.api.dto.ArticlePublicDetailResponseDTO;
import com.devmaster.goatfarm.article.api.dto.ArticlePublicListResponseDTO;
import com.devmaster.goatfarm.article.enums.ArticleCategory;
import com.devmaster.goatfarm.article.api.mapper.ArticleMapper;
import com.devmaster.goatfarm.article.business.bo.ArticlePublicDetailResponseVO;
import com.devmaster.goatfarm.article.business.bo.ArticlePublicListResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/articles")
@Tag(name = "Public Articles", description = "Leitura pública de artigos do blog")
public class PublicArticleController {

    private final ArticleQueryUseCase articleQueryUseCase;
    private final ArticleMapper articleMapper;

    public PublicArticleController(ArticleQueryUseCase articleQueryUseCase, ArticleMapper articleMapper) {
        this.articleQueryUseCase = articleQueryUseCase;
        this.articleMapper = articleMapper;
    }

    @GetMapping
    @Operation(summary = "Lista artigos publicados")
    public ResponseEntity<Page<ArticlePublicListResponseDTO>> getPublishedArticles(
            @Parameter(description = "Categoria do artigo") @RequestParam(required = false) ArticleCategory category,
            @Parameter(description = "Busca por título ou resumo") @RequestParam(required = false) String q,
            @PageableDefault(sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ArticlePublicListResponseVO> page = articleQueryUseCase.getPublishedArticles(category, q, pageable);
        Page<ArticlePublicListResponseDTO> dtoPage = page.map(articleMapper::toPublicListResponseDTO);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/highlights")
    @Operation(summary = "Lista destaques da home (3 artigos)")
    public ResponseEntity<List<ArticlePublicListResponseDTO>> getHighlights() {
        List<ArticlePublicListResponseVO> highlights = articleQueryUseCase.getHighlights();
        List<ArticlePublicListResponseDTO> response = highlights.stream()
                .map(articleMapper::toPublicListResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Busca artigo publicado por slug")
    public ResponseEntity<ArticlePublicDetailResponseDTO> getArticleBySlug(
            @Parameter(description = "Slug do artigo") @PathVariable String slug) {
        ArticlePublicDetailResponseVO responseVO = articleQueryUseCase.getPublishedArticleBySlug(slug);
        return ResponseEntity.ok(articleMapper.toPublicDetailResponseDTO(responseVO));
    }
}
