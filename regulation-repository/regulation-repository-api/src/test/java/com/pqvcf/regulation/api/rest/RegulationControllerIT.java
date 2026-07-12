package com.pqvcf.regulation.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pqvcf.regulation.api.RegulationRepositoryApplication;
import com.pqvcf.regulation.application.port.in.RegisterRegulationUseCase.RegisterRegulationCommand;
import com.pqvcf.regulation.domain.model.RegulationStatus;
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

@SpringBootTest(classes = RegulationRepositoryApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegulationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should fetch all seeded regulations (GDPR, DPDP, HIPAA)")
    void shouldFetchSeededRegulations() throws Exception {
        mockMvc.perform(get("/api/v1/regulations")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[0].shortName", is("DPDP")))
                .andExpect(jsonPath("$[1].shortName", is("GDPR")))
                .andExpect(jsonPath("$[2].shortName", is("HIPAA")));
    }

    @Test
    @DisplayName("Should fetch GDPR details by short name, including articles and clauses")
    void shouldFetchRegulationByShortName() throws Exception {
        mockMvc.perform(get("/api/v1/regulations/short/GDPR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortName", is("GDPR")))
                .andExpect(jsonPath("$.primaryJurisdiction", is("EU")))
                .andExpect(jsonPath("$.articles", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$.articles[0].articleNumber", is("Article 44")))
                .andExpect(jsonPath("$.articles[0].clauses", hasSize(1)))
                .andExpect(jsonPath("$.articles[0].clauses[0].clauseNumber", is("44")))
                .andExpect(jsonPath("$.articles[0].clauses[0].clauseType", is("PROHIBITION")));
    }

    @Test
    @DisplayName("Should successfully create, retrieve, and activate a new custom regulation")
    void shouldCreateAndActivateRegulation() throws Exception {
        RegisterRegulationCommand command = new RegisterRegulationCommand(
                "California Consumer Privacy Act",
                "CCPA",
                "US_CA",
                "2018",
                "California data protection framework"
        );

        // 1. Create CCPA
        String responseJson = mockMvc.perform(post("/api/v1/regulations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortName", is("CCPA")))
                .andExpect(jsonPath("$.status", is(RegulationStatus.DRAFT.name())))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(responseJson).get("id").asText();

        // 2. Fetch CCPA by ID
        mockMvc.perform(get("/api/v1/regulations/" + id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortName", is("CCPA")))
                .andExpect(jsonPath("$.status", is(RegulationStatus.DRAFT.name())));

        // 3. Activate CCPA
        mockMvc.perform(post("/api/v1/regulations/" + id + "/activate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(RegulationStatus.ACTIVE.name())))
                .andExpect(jsonPath("$.effectiveDate").isNotEmpty());
    }

    @Test
    @DisplayName("Should expose correct CORS response headers for browser client requests")
    void shouldVerifyCorsHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/regulations")
                .header("Origin", "http://localhost:5173")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
