package com.pqvcf.pip.application.port.in;

import java.util.List;

public interface ResolveAttributesUseCase {

    ResolvedContextResponse resolve(ResolveAttributesQuery query);

    record ResolveAttributesQuery(
            String subjectId,
            String resourceId,
            String actionId,
            String sourceCountry,
            String targetCountry
    ) {}

    record AttributeResponse(
            String category,
            String key,
            String value,
            String dataType
    ) {}

    record ResolvedContextResponse(
            String subjectId,
            String resourceId,
            String actionId,
            List<AttributeResponse> attributes,
            boolean isTransitiveAdequate
    ) {}
}
