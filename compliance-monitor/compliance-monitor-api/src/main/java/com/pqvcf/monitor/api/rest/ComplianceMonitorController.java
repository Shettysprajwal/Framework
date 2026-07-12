package com.pqvcf.monitor.api.rest;

import com.pqvcf.monitor.application.port.in.IngestEventUseCase;
import com.pqvcf.monitor.application.port.in.IngestEventUseCase.EventDto;
import com.pqvcf.monitor.application.port.in.IngestEventUseCase.IngestEventCommand;
import com.pqvcf.monitor.application.port.in.IngestEventUseCase.SlaMetricsDto;
import com.pqvcf.monitor.application.port.in.IngestEventUseCase.ViolationDto;
import com.pqvcf.monitor.application.port.in.MonitorQueryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/monitor")
@Tag(name = "Continuous Compliance Monitor API", description = "Management endpoints for ingesting cloud traffic events, listing alerts, and checking SLA health status")
public class ComplianceMonitorController {

    private final IngestEventUseCase ingestUseCase;
    private final MonitorQueryUseCase queryUseCase;

    public ComplianceMonitorController(
            IngestEventUseCase ingestUseCase,
            MonitorQueryUseCase queryUseCase) {
        this.ingestUseCase = ingestUseCase;
        this.queryUseCase = queryUseCase;
    }

    @PostMapping("/ingest")
    @Operation(summary = "Ingest a simulated cloud data movement event and check compliance rules")
    public ResponseEntity<Void> ingest(@RequestBody IngestEventCommand command) {
        ingestUseCase.ingest(command);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/events")
    @Operation(summary = "List recent cloud data movement events")
    public ResponseEntity<List<EventDto>> listRecentEvents() {
        return ResponseEntity.ok(queryUseCase.listRecentEvents());
    }

    @GetMapping("/violations")
    @Operation(summary = "List compliance policy violations and warnings")
    public ResponseEntity<List<ViolationDto>> listViolations() {
        return ResponseEntity.ok(queryUseCase.listViolations());
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get SLA compliance rate statistics")
    public ResponseEntity<SlaMetricsDto> getCurrentMetrics() {
        return ResponseEntity.ok(queryUseCase.getCurrentMetrics());
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset SLA metrics and clear logged events history")
    public ResponseEntity<Void> reset() {
        queryUseCase.reset();
        return ResponseEntity.ok().build();
    }
}
