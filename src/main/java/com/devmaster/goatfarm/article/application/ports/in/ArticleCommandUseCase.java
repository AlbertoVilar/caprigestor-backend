package com.devmaster.goatfarm.article.application.ports.in;

import com.devmaster.goatfarm.article.business.bo.ArticleHighlightRequestVO;
import com.devmaster.goatfarm.article.business.bo.ArticlePublishRequestVO;
import com.devmaster.goatfarm.article.business.bo.ArticleRequestVO;
import com.devmaster.goatfarm.article.business.bo.ArticleResponseVO;

public interface ArticleCommandUseCase {

    ArticleResponseVO createDraft(ArticleRequestVO requestVO);

    ArticleResponseVO updateArticle(Long id, ArticleRequestVO requestVO);

    ArticleResponseVO publishArticle(Long id, ArticlePublishRequestVO requestVO);

    ArticleResponseVO highlightArticle(Long id, ArticleHighlightRequestVO requestVO);

    void deleteArticle(Long id);
}
