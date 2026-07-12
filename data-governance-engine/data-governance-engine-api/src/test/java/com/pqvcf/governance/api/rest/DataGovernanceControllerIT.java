package com.pqvcf.governance.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pqvcf.governance.api.DataGovernanceApplication;
import com.pqvcf.governance.application.port.in.EvaluateFlowUseCase.EvaluateFlowCommand;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DataGovernanceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DataGovernanceControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should execute end-to-end data transfer legality evaluation workflow")
    void shouldExecuteGovernanceWorkflow() throws Exception {
        // 1. Evaluate Whitelisted Adequacy Transfer (DE -> IN, Approved)
        EvaluateFlowCommand approveCommand = new EvaluateFlowCommand("DE", "IN", "PERSONAL", "BACKUP");

        String resultJson = mockMvc.perform(post("/api/v1/governance/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approveCommand)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.decision", is("APPROVED")))
                .andExpect(jsonPath("$.reasoning").value(org.hamcrest.Matchers.containsString("Approved")))
                .andExpect(jsonPath("$.citations").value(org.hamcrest.Matchers.hasItem("GDPR Article 45 Adequacy Decision")))
                .andReturn().getResponse().getContentAsString();

        String decisionId = objectMapper.readTree(resultJson).get("decisionId").asText();

        // 2. Evaluate Blocked Localization Transfer (RU -> DE, Blocked)
        EvaluateFlowCommand blockCommand = new EvaluateFlowCommand("RU", "DE", "PERSONAL", "BACKUP");

        mockMvc.perform(post("/api/v1/governance/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(blockCommand)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.decision", is("BLOCKED")))
                .andExpect(jsonPath("$.citations").value(org.hamcrest.Matchers.hasItem("Russia FFDL No. 242-FZ")));

        // 3. Evaluate Standard Safeguards Transfer (US -> DE, Conditional)
        EvaluateFlowCommand condCommand = new EvaluateFlowCommand("US", "DE", "PERSONAL", "BACKUP");

        mockMvc.perform(post("/api/v1/governance/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(condCommand)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.decision", is("CONDITIONAL")))
                .andExpect(jsonPath("$.citations").value(org.hamcrest.Matchers.hasItem("GDPR Article 46 Transfer Safeguards")));

        // 4. List Decisions
        mockMvc.perform(get("/api/v1/governance/decisions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));

        // 5. Delete decision
        mockMvc.perform(delete("/api/v1/governance/decisions/" + decisionId))
                .andExpect(status().isNoContent());

        // 6. Check decision is deleted
        mockMvc.perform(get("/api/v1/governance/decisions/" + decisionId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should verify correct CORS configuration headers")
    void shouldVerifyCorsHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/governance/decisions")
                .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
