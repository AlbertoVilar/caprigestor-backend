package com.devmaster.goatfarm.article.business.mapper;

import com.devmaster.goatfarm.article.business.bo.ArticlePublicDetailResponseVO;
import com.devmaster.goatfarm.article.business.bo.ArticlePublicListResponseVO;
import com.devmaster.goatfarm.article.business.bo.ArticleResponseVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ArticleBusinessMapper {

    default ArticleResponseVO toResponseVO(ArticleResponseVO article) {
        return article;
    }

    ArticlePublicListResponseVO toPublicListResponseVO(ArticleResponseVO article);

    ArticlePublicDetailResponseVO toPublicDetailResponseVO(ArticleResponseVO article);
}
