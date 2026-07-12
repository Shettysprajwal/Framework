package com.pqvcf.regulation.application.dto;

import java.time.Instant;
import java.util.List;

public record ArticleResponse(
        String id,
        String regulationId,
        String articleNumber,
        String title,
        String content,
        String deonticFormula,
        String odrlPolicy,
        List<ClauseResponse> clauses,
        Instant createdAt,
        Instant updatedAt
) {}
