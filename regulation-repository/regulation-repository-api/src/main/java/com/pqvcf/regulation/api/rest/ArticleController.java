package com.pqvcf.regulation.api.rest;

import com.pqvcf.regulation.application.dto.ArticleResponse;
import com.pqvcf.regulation.application.port.in.AddArticleUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/regulations/articles")
@Tag(name = "Articles API", description = "Endpoints for managing articles and clauses under regulations")
public class ArticleController {

    private final AddArticleUseCase addArticleUseCase;

    public ArticleController(AddArticleUseCase addArticleUseCase) {
        this.addArticleUseCase = addArticleUseCase;
    }

    @PostMapping
    @Operation(summary = "Add a new article (and optional clauses) to an existing regulation")
    public ResponseEntity<ArticleResponse> addArticle(@RequestBody AddArticleUseCase.AddArticleCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addArticleUseCase.addArticle(command));
    }
}
