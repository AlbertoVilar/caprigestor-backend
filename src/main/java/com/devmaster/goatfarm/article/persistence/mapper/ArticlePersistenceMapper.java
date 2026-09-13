package com.devmaster.goatfarm.article.persistence.mapper;

import com.devmaster.goatfarm.article.business.bo.ArticleResponseVO;
import com.devmaster.goatfarm.article.persistence.entity.Article;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ArticlePersistenceMapper {

    ArticleResponseVO toModel(Article entity);

    Article toEntity(ArticleResponseVO model);
}
