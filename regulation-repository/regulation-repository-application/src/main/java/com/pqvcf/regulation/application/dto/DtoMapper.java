package com.pqvcf.regulation.application.dto;

import com.pqvcf.regulation.domain.model.Article;
import com.pqvcf.regulation.domain.model.Clause;
import com.pqvcf.regulation.domain.model.Regulation;

import java.util.List;
import java.util.stream.Collectors;

public final class DtoMapper {

    private DtoMapper() {}

    public static ClauseResponse toResponse(Clause clause) {
        if (clause == null) return null;
        return new ClauseResponse(
                clause.getId().toString(),
                clause.getArticleId().toString(),
                clause.getClauseNumber(),
                clause.getContent(),
                clause.getClauseType().name()
        );
    }

    public static ArticleResponse toResponse(Article article) {
        if (article == null) return null;
        List<ClauseResponse> clauseResponses = article.getClauses().stream()
                .map(DtoMapper::toResponse)
                .collect(Collectors.toList());
        return new ArticleResponse(
                article.getId().toString(),
                article.getRegulationId().toString(),
                article.getArticleNumber(),
                article.getTitle(),
                article.getContent(),
                article.getDeonticFormula(),
                article.getOdrlPolicy(),
                clauseResponses,
                article.getCreatedAt(),
                article.getUpdatedAt()
        );
    }

    public static RegulationResponse toResponse(Regulation regulation) {
        if (regulation == null) return null;
        List<ArticleResponse> articleResponses = regulation.getArticles().stream()
                .map(DtoMapper::toResponse)
                .collect(Collectors.toList());
        return new RegulationResponse(
                regulation.getId().toString(),
                regulation.getName(),
                regulation.getShortName(),
                regulation.getPrimaryJurisdiction().getCode(),
                regulation.getVersion(),
                regulation.getEffectiveDate(),
                regulation.getDescription(),
                regulation.getStatus().name(),
                regulation.getFormalSpec(),
                articleResponses,
                regulation.getCreatedAt(),
                regulation.getUpdatedAt()
        );
    }
}
