package com.pqvcf.monitor.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pqvcf.monitor.api.ComplianceMonitorApplication;
import com.pqvcf.monitor.application.port.in.IngestEventUseCase.IngestEventCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ComplianceMonitorApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ComplianceMonitorControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should execute continuous monitoring events ingestion, alert triggers, and SLA updates workflow")
    void shouldExecuteMonitoringWorkflow() throws Exception {
        // Clear database metrics first
        mockMvc.perform(post("/api/v1/monitor/reset")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 1. Ingest Compliant Event (DE -> IN, Approved)
        IngestEventCommand cmd1 = new IngestEventCommand("DE", "IN", "PERSONAL", 2048);
        mockMvc.perform(post("/api/v1/monitor/ingest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd1)))
                .andExpect(status().isOk());

        // 2. Ingest Blocked Event (RU -> DE, Critical Alert raised)
        IngestEventCommand cmd2 = new IngestEventCommand("RU", "DE", "PERSONAL", 1024);
        mockMvc.perform(post("/api/v1/monitor/ingest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd2)))
                .andExpect(status().isOk());

        // 3. Query SLA metrics (2 events, 1 critical violation -> 50% SLA)
        mockMvc.perform(get("/api/v1/monitor/metrics")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEvents", is(2)))
                .andExpect(jsonPath("$.violationCount", is(1)))
                .andExpect(jsonPath("$.complianceRate", is(50.0)));

        // 4. Query Events ledger
        mockMvc.perform(get("/api/v1/monitor/events")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // 5. Query Alarms ledger
        mockMvc.perform(get("/api/v1/monitor/violations")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].severity", is("CRITICAL")))
                .andExpect(jsonPath("$[0].violatedRule").value(org.hamcrest.Matchers.containsString("residency")));
    }

    @Test
    @DisplayName("Should verify correct CORS configuration headers")
    void shouldVerifyCorsHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/monitor/events")
                .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
