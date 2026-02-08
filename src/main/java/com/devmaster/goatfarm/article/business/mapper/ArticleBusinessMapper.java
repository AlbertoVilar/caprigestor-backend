package com.devmaster.goatfarm.article.business.mapper;

import com.devmaster.goatfarm.article.business.bo.ArticlePublicDetailResponseVO;
import com.devmaster.goatfarm.article.business.bo.ArticlePublicListResponseVO;
import com.devmaster.goatfarm.article.business.bo.ArticleResponseVO;
import com.devmaster.goatfarm.article.persistence.entity.Article;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ArticleBusinessMapper {

    ArticleResponseVO toResponseVO(Article entity);

    ArticlePublicListResponseVO toPublicListResponseVO(Article entity);

    ArticlePublicDetailResponseVO toPublicDetailResponseVO(Article entity);
}
