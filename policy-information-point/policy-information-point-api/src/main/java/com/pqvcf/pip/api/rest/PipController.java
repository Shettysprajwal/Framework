package com.pqvcf.pip.api.rest;

import com.pqvcf.pip.application.port.in.ResolveAttributesUseCase;
import com.pqvcf.pip.application.port.in.ResolveAttributesUseCase.ResolveAttributesQuery;
import com.pqvcf.pip.application.port.in.ResolveAttributesUseCase.ResolvedContextResponse;
import com.pqvcf.pip.infrastructure.cache.CachingAttributeResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pip")
@Tag(name = "Policy Information Point API", description = "Management endpoints for dynamic subject/resource attribute resolution and transitivity pathways checking")
public class PipController {

    private final ResolveAttributesUseCase resolveUseCase;
    private final CachingAttributeResolver cacheResolver;

    public PipController(
            ResolveAttributesUseCase resolveUseCase,
            CachingAttributeResolver cacheResolver) {
        this.resolveUseCase = resolveUseCase;
        this.cacheResolver = cacheResolver;
    }

    @PostMapping("/resolve")
    @Operation(summary = "Resolve subject, resource, action, and transitive geo-adequacy status")
    public ResponseEntity<ResolvedContextResponse> resolve(@RequestBody ResolveAttributesQuery query) {
        return ResponseEntity.ok(resolveUseCase.resolve(query));
    }

    @PostMapping("/attributes/subject")
    @Operation(summary = "Register or overwrite subject attribute dynamically inside PIP local cache")
    public ResponseEntity<Void> registerSubjectAttribute(@RequestBody RegisterAttributeRequest request) {
        cacheResolver.registerSubjectAttribute(
                request.id(),
                request.key(),
                request.value(),
                request.dataType()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/attributes/resource")
    @Operation(summary = "Register or overwrite resource attribute dynamically inside PIP local cache")
    public ResponseEntity<Void> registerResourceAttribute(@RequestBody RegisterAttributeRequest request) {
        cacheResolver.registerResourceAttribute(
                request.id(),
                request.key(),
                request.value(),
                request.dataType()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public record RegisterAttributeRequest(
            String id,
            String key,
            String value,
            String dataType
    ) {}
}
