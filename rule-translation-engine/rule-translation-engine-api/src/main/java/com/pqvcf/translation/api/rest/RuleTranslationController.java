package com.pqvcf.translation.api.rest;

import com.pqvcf.translation.application.port.in.GetTranslatedRuleUseCase;
import com.pqvcf.translation.application.port.in.TranslateRuleUseCase;
import com.pqvcf.translation.application.port.in.TranslateRuleUseCase.TranslateRuleCommand;
import com.pqvcf.translation.application.port.in.TranslateRuleUseCase.TranslationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/translation")
@Tag(name = "Rule Translation API", description = "Management endpoints for translating legal statutes into deontic and SMT-LIB2 specifications")
public class RuleTranslationController {

    private final TranslateRuleUseCase translateUseCase;
    private final GetTranslatedRuleUseCase getUseCase;

    public RuleTranslationController(TranslateRuleUseCase translateUseCase, GetTranslatedRuleUseCase getUseCase) {
        this.translateUseCase = translateUseCase;
        this.getUseCase = getUseCase;
    }

    @PostMapping("/translate")
    @Operation(summary = "Translate a Controlled Natural Language (CNL) policy statement into deontic formula, ODRL JSON and Z3 SMT specifications")
    public ResponseEntity<TranslationResponse> translate(@RequestBody TranslateRuleCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(translateUseCase.translate(command));
    }

    @GetMapping("/rules")
    @Operation(summary = "List all registered translated rules")
    public ResponseEntity<List<TranslationResponse>> listAll() {
        return ResponseEntity.ok(getUseCase.listAll());
    }

    @GetMapping("/rules/{id}")
    @Operation(summary = "Get translated rule details by ID")
    public ResponseEntity<TranslationResponse> getById(@PathVariable String id) {
        return getUseCase.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rules/regulation/{regulation}")
    @Operation(summary = "Get rules associated with a specific regulation")
    public ResponseEntity<List<TranslationResponse>> findByRegulation(
            @PathVariable String regulation,
            @RequestParam(required = false) String article) {
        if (article != null && !article.isBlank()) {
            return ResponseEntity.ok(getUseCase.findByRegulationAndArticle(regulation, article));
        }
        return ResponseEntity.ok(getUseCase.findByRegulation(regulation));
    }

    @DeleteMapping("/rules/{id}")
    @Operation(summary = "Delete a translated rule by ID")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        getUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
