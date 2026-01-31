package com.devmaster.goatfarm.article.api.mapper;

import com.devmaster.goatfarm.article.api.dto.ArticleHighlightRequestDTO;
import com.devmaster.goatfarm.article.api.dto.ArticlePublishRequestDTO;
import com.devmaster.goatfarm.article.api.dto.ArticlePublicDetailResponseDTO;
import com.devmaster.goatfarm.article.api.dto.ArticlePublicListResponseDTO;
import com.devmaster.goatfarm.article.api.dto.ArticleRequestDTO;
import com.devmaster.goatfarm.article.api.dto.ArticleResponseDTO;
import com.devmaster.goatfarm.article.business.bo.ArticleHighlightRequestVO;
import com.devmaster.goatfarm.article.business.bo.ArticlePublishRequestVO;
import com.devmaster.goatfarm.article.business.bo.ArticlePublicDetailResponseVO;
import com.devmaster.goatfarm.article.business.bo.ArticlePublicListResponseVO;
import com.devmaster.goatfarm.article.business.bo.ArticleRequestVO;
import com.devmaster.goatfarm.article.business.bo.ArticleResponseVO;
import com.devmaster.goatfarm.article.persistence.entity.Article;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ArticleMapper {

    ArticleRequestVO toRequestVO(ArticleRequestDTO dto);

    ArticlePublishRequestVO toPublishRequestVO(ArticlePublishRequestDTO dto);

    ArticleHighlightRequestVO toHighlightRequestVO(ArticleHighlightRequestDTO dto);

    ArticleResponseDTO toResponseDTO(ArticleResponseVO vo);

    ArticlePublicListResponseDTO toPublicListResponseDTO(ArticlePublicListResponseVO vo);

    ArticlePublicDetailResponseDTO toPublicDetailResponseDTO(ArticlePublicDetailResponseVO vo);

    ArticleResponseVO toResponseVO(Article entity);

    ArticlePublicListResponseVO toPublicListResponseVO(Article entity);

    ArticlePublicDetailResponseVO toPublicDetailResponseVO(Article entity);
}
