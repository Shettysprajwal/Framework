package com.pqvcf.pip.application.service;

import com.pqvcf.pip.application.port.in.ResolveAttributesUseCase;
import com.pqvcf.pip.domain.model.ContextAttribute;
import com.pqvcf.pip.domain.model.EvaluationContext;
import com.pqvcf.pip.domain.resolver.AdequacyPathResolver;
import com.pqvcf.pip.domain.resolver.AttributeResolver;

import java.util.List;
import java.util.stream.Collectors;

public class AttributeResolutionService implements ResolveAttributesUseCase {

    private final AttributeResolver attributeResolver;
    private final AdequacyPathResolver adequacyPathResolver;

    public AttributeResolutionService(
            AttributeResolver attributeResolver,
            AdequacyPathResolver adequacyPathResolver) {
        this.attributeResolver = attributeResolver;
        this.adequacyPathResolver = adequacyPathResolver;
    }

    @Override
    public ResolvedContextResponse resolve(ResolveAttributesQuery query) {
        EvaluationContext context = new EvaluationContext(
                query.subjectId(),
                query.resourceId(),
                query.actionId()
        );

        // 1. Resolve subject context attributes
        List<ContextAttribute> subjectAttrs = attributeResolver.resolveSubjectAttributes(query.subjectId());
        subjectAttrs.forEach(context::addAttribute);

        // 2. Resolve resource/action context attributes
        List<ContextAttribute> resourceAttrs = attributeResolver.resolveResourceAttributes(query.resourceId());
        resourceAttrs.forEach(context::addAttribute);

        // 3. Resolve environmental transit adequacy
        boolean isTransitiveAdequate = false;
        if (query.sourceCountry() != null && !query.sourceCountry().isBlank() &&
                query.targetCountry() != null && !query.targetCountry().isBlank()) {
            isTransitiveAdequate = adequacyPathResolver.isAdequate(query.sourceCountry(), query.targetCountry());
        }

        // Map list to DTO responses
        List<AttributeResponse> attributeResponses = context.getAttributes().stream()
                .map(attr -> new AttributeResponse(
                        attr.getCategory().name(),
                        attr.getKey(),
                        attr.getValue(),
                        attr.getDataType()
                )).collect(Collectors.toList());

        return new ResolvedContextResponse(
                context.getSubjectId(),
                context.getResourceId(),
                context.getActionId(),
                attributeResponses,
                isTransitiveAdequate
        );
    }
}
