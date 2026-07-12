package com.pqvcf.regulation.application.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record RegulationResponse(
        String id,
        String name,
        String shortName,
        String primaryJurisdiction,
        String version,
        LocalDate effectiveDate,
        String description,
        String status,
        String formalSpec,
        List<ArticleResponse> articles,
        Instant createdAt,
        Instant updatedAt
) {}
