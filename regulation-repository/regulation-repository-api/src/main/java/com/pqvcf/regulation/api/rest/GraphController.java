package com.pqvcf.regulation.api.rest;

import com.pqvcf.regulation.domain.repository.JurisdictionGraphRepository;
import com.pqvcf.shared.types.JurisdictionCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/graph")
@Tag(name = "Graph Reasoning API", description = "Endpoints for managing the jurisdiction adequacy and conflict graph")
public class GraphController {

    private final JurisdictionGraphRepository graphRepository;

    public GraphController(JurisdictionGraphRepository graphRepository) {
        this.graphRepository = graphRepository;
    }

    @GetMapping
    @Operation(summary = "Get full serialization of nodes and edges for graph visualization")
    public ResponseEntity<Map<String, Object>> getGraph() {
        return ResponseEntity.ok(graphRepository.getGraphData());
    }

    @PostMapping("/adequacy")
    @Operation(summary = "Establish a direct adequacy decision relationship between source and target jurisdictions")
    public ResponseEntity<Void> addAdequacyDecision(@RequestBody AddAdequacyCommand command) {
        graphRepository.addAdequacyDecision(
                JurisdictionCode.of(command.source()),
                JurisdictionCode.of(command.target())
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/adequacy/check")
    @Operation(summary = "Check if target jurisdiction is adequate under source jurisdiction guidelines")
    public ResponseEntity<Map<String, Object>> checkAdequacy(
            @RequestParam String source,
            @RequestParam String target) {
        boolean adequate = graphRepository.isAdequate(
                JurisdictionCode.of(source),
                JurisdictionCode.of(target)
        );
        return ResponseEntity.ok(Map.of(
                "source", source.toUpperCase(),
                "target", target.toUpperCase(),
                "isAdequate", adequate
        ));
    }

    public record AddAdequacyCommand(String source, String target) {}
}
