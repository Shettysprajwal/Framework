package com.pqvcf.pdp.application.port.out;

import java.util.List;

public interface PipAttributeProvider {

    ResolvedContextDto resolveContext(
            String subjectId,
            String resourceId,
            String actionId,
            String sourceCountry,
            String targetCountry
    );

    record ResolvedContextDto(
            String subjectId,
            String resourceId,
            String actionId,
            List<AttributeDto> attributes,
            boolean isTransitiveAdequate
    ) {}

    record AttributeDto(
            String category,
            String key,
            String value,
            String dataType
    ) {}
}
