package com.pqvcf.pip.application.service;

import com.pqvcf.pip.application.port.in.ResolveAttributesUseCase.ResolveAttributesQuery;
import com.pqvcf.pip.application.port.in.ResolveAttributesUseCase.ResolvedContextResponse;
import com.pqvcf.pip.domain.model.AttributeCategory;
import com.pqvcf.pip.domain.model.ContextAttribute;
import com.pqvcf.pip.domain.resolver.AdequacyPathResolver;
import com.pqvcf.pip.domain.resolver.AttributeResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttributeResolutionServiceTest {

    @Mock
    private AttributeResolver attributeResolver;

    @Mock
    private AdequacyPathResolver adequacyPathResolver;

    private AttributeResolutionService service;

    @BeforeEach
    void setUp() {
        service = new AttributeResolutionService(attributeResolver, adequacyPathResolver);
    }

    @Test
    @DisplayName("Should compile subject and resource attributes and resolve geo transitivity path")
    void shouldResolveAttributes() {
        ResolveAttributesQuery query = new ResolveAttributesQuery(
                "admin",
                "personal-data",
                "transfer",
                "IN",
                "EU"
        );

        List<ContextAttribute> subAttrs = List.of(
                new ContextAttribute(AttributeCategory.SUBJECT, "role", "administrator", "String"),
                new ContextAttribute(AttributeCategory.SUBJECT, "clearance", "top-secret", "String")
        );

        List<ContextAttribute> resAttrs = List.of(
                new ContextAttribute(AttributeCategory.RESOURCE, "classification", "personal", "String")
        );

        when(attributeResolver.resolveSubjectAttributes("admin")).thenReturn(subAttrs);
        when(attributeResolver.resolveResourceAttributes("personal-data")).thenReturn(resAttrs);
        when(adequacyPathResolver.isAdequate("IN", "EU")).thenReturn(true);

        ResolvedContextResponse response = service.resolve(query);

        assertThat(response).isNotNull();
        assertThat(response.subjectId()).isEqualTo("admin");
        assertThat(response.resourceId()).isEqualTo("personal-data");
        assertThat(response.isTransitiveAdequate()).isTrue();
        assertThat(response.attributes()).hasSize(3);

        verify(attributeResolver).resolveSubjectAttributes("admin");
        verify(attributeResolver).resolveResourceAttributes("personal-data");
        verify(adequacyPathResolver).isAdequate("IN", "EU");
    }
}
