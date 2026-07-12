package com.pqvcf.pdp.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pqvcf.pdp.api.PolicyDecisionApplication;
import com.pqvcf.pdp.application.port.in.EvaluateRequestUseCase.EvaluateCommand;
import com.pqvcf.pdp.domain.model.DecisionEffect;
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

@SpringBootTest(classes = PolicyDecisionApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PdpControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should successfully evaluate compliance access requests, trace mathematical proof, and retrieve audit trails")
    void shouldExecutePdpEvaluationFlow() throws Exception {
        EvaluateCommand command = new EvaluateCommand(
                "analyst",
                "health-records",
                "transfer",
                "IN",
                "EU",
                "Global Privacy Transfer Policy"
        );

        // 1. Evaluate compliant request (IN -> EU adequacy transitivity is true in fallback)
        mockMvc.perform(post("/api/v1/pdp/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.effect", is(DecisionEffect.PERMIT.name())))
                .andExpect(jsonPath("$.proofTrace").isNotEmpty())
                .andExpect(jsonPath("$.validationLog").value(org.hamcrest.Matchers.containsString("Permit")));

        // 2. Evaluate non-compliant request (US -> IN adequacy transitivity is false in fallback)
        EvaluateCommand nonCompliantCommand = new EvaluateCommand(
                "analyst",
                "health-records",
                "transfer",
                "US",
                "IN",
                "Global Privacy Transfer Policy"
        );

        mockMvc.perform(post("/api/v1/pdp/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nonCompliantCommand)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.effect", is(DecisionEffect.DENY.name())))
                .andExpect(jsonPath("$.validationLog").value(org.hamcrest.Matchers.containsString("Deny")));

        // 3. Fetch past audits logs
        mockMvc.perform(get("/api/v1/pdp/audits")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("Should expose correct CORS response headers")
    void shouldVerifyCorsHeaders() throws Exception {
        EvaluateCommand command = new EvaluateCommand("guest", "public", "read", "", "", "");

        mockMvc.perform(post("/api/v1/pdp/evaluate")
                .header("Origin", "http://localhost:5173")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
