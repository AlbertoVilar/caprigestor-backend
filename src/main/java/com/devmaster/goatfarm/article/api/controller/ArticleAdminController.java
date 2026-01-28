package com.devmaster.goatfarm.article.api.controller;

import com.devmaster.goatfarm.application.ports.in.ArticleCommandUseCase;
import com.devmaster.goatfarm.application.ports.in.ArticleQueryUseCase;
import com.devmaster.goatfarm.article.api.dto.ArticleHighlightRequestDTO;
import com.devmaster.goatfarm.article.api.dto.ArticlePublishRequestDTO;
import com.devmaster.goatfarm.article.api.dto.ArticleRequestDTO;
import com.devmaster.goatfarm.article.api.dto.ArticleResponseDTO;
import com.devmaster.goatfarm.article.business.bo.ArticleHighlightRequestVO;
import com.devmaster.goatfarm.article.business.bo.ArticlePublishRequestVO;
import com.devmaster.goatfarm.article.business.bo.ArticleRequestVO;
import com.devmaster.goatfarm.article.business.bo.ArticleResponseVO;
import com.devmaster.goatfarm.article.mapper.ArticleMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
@Tag(name = "Admin Articles", description = "Gerenciamento administrativo de artigos do blog")
public class ArticleAdminController {

    private static final String ADMIN_ONLY = "hasAuthority('ROLE_ADMIN')";

    private final ArticleCommandUseCase articleCommandUseCase;
    private final ArticleQueryUseCase articleQueryUseCase;
    private final ArticleMapper articleMapper;

    public ArticleAdminController(ArticleCommandUseCase articleCommandUseCase,
                                  ArticleQueryUseCase articleQueryUseCase,
                                  ArticleMapper articleMapper) {
        this.articleCommandUseCase = articleCommandUseCase;
        this.articleQueryUseCase = articleQueryUseCase;
        this.articleMapper = articleMapper;
    }

    @PreAuthorize(ADMIN_ONLY)
    @PostMapping
    @Operation(summary = "Cria um rascunho de artigo")
    public ResponseEntity<ArticleResponseDTO> createDraft(@Valid @RequestBody ArticleRequestDTO request) {
        ArticleRequestVO requestVO = articleMapper.toRequestVO(request);
        ArticleResponseVO responseVO = articleCommandUseCase.createDraft(requestVO);
        return ResponseEntity.status(HttpStatus.CREATED).body(articleMapper.toResponseDTO(responseVO));
    }

    @PreAuthorize(ADMIN_ONLY)
    @PutMapping("/{id}")
    @Operation(summary = "Atualiza conteúdo e metadados do artigo")
    public ResponseEntity<ArticleResponseDTO> updateArticle(
            @Parameter(description = "Identificador do artigo") @PathVariable Long id,
            @Valid @RequestBody ArticleRequestDTO request) {
        ArticleRequestVO requestVO = articleMapper.toRequestVO(request);
        ArticleResponseVO responseVO = articleCommandUseCase.updateArticle(id, requestVO);
        return ResponseEntity.ok(articleMapper.toResponseDTO(responseVO));
    }

    @PreAuthorize(ADMIN_ONLY)
    @GetMapping
    @Operation(summary = "Lista artigos (inclui rascunhos)")
    public ResponseEntity<Page<ArticleResponseDTO>> getAllArticles(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ArticleResponseVO> page = articleQueryUseCase.getAllArticles(pageable);
        Page<ArticleResponseDTO> dtoPage = page.map(articleMapper::toResponseDTO);
        return ResponseEntity.ok(dtoPage);
    }

    @PreAuthorize(ADMIN_ONLY)
    @GetMapping("/{id}")
    @Operation(summary = "Busca artigo por ID")
    public ResponseEntity<ArticleResponseDTO> getArticleById(
            @Parameter(description = "Identificador do artigo") @PathVariable Long id) {
        ArticleResponseVO responseVO = articleQueryUseCase.getArticleById(id);
        return ResponseEntity.ok(articleMapper.toResponseDTO(responseVO));
    }

    @PreAuthorize(ADMIN_ONLY)
    @PatchMapping("/{id}/publish")
    @Operation(summary = "Publica ou despublica um artigo")
    public ResponseEntity<ArticleResponseDTO> publishArticle(
            @Parameter(description = "Identificador do artigo") @PathVariable Long id,
            @Valid @RequestBody ArticlePublishRequestDTO request) {
        ArticlePublishRequestVO requestVO = articleMapper.toPublishRequestVO(request);
        ArticleResponseVO responseVO = articleCommandUseCase.publishArticle(id, requestVO);
        return ResponseEntity.ok(articleMapper.toResponseDTO(responseVO));
    }

    @PreAuthorize(ADMIN_ONLY)
    @PatchMapping("/{id}/highlight")
    @Operation(summary = "Destaca ou remove destaque de um artigo")
    public ResponseEntity<ArticleResponseDTO> highlightArticle(
            @Parameter(description = "Identificador do artigo") @PathVariable Long id,
            @Valid @RequestBody ArticleHighlightRequestDTO request) {
        ArticleHighlightRequestVO requestVO = articleMapper.toHighlightRequestVO(request);
        ArticleResponseVO responseVO = articleCommandUseCase.highlightArticle(id, requestVO);
        return ResponseEntity.ok(articleMapper.toResponseDTO(responseVO));
    }

    @PreAuthorize(ADMIN_ONLY)
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um artigo")
    public ResponseEntity<Void> deleteArticle(
            @Parameter(description = "Identificador do artigo") @PathVariable Long id) {
        articleCommandUseCase.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }
}
