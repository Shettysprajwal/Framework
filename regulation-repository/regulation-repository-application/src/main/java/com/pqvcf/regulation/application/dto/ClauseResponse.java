package com.pqvcf.regulation.application.dto;

import java.util.List;

public record ClauseResponse(
        String id,
        String articleId,
        String clauseNumber,
        String content,
        String clauseType
) {}
