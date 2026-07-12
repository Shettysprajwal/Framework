package com.pqvcf.pap.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pqvcf.pap.api.PolicyAdministrationApplication;
import com.pqvcf.pap.application.port.in.CreatePolicyUseCase.CreatePolicyCommand;
import com.pqvcf.pap.api.rest.PolicyController.LinkRuleCommandRequestBody;
import com.pqvcf.pap.domain.model.PolicyStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PolicyAdministrationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PolicyControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should successfully create, link rules, retrieve, activate, and deprecate policies")
    void shouldExecutePolicyLifecycle() throws Exception {
        CreatePolicyCommand command = new CreatePolicyCommand(
                "Enterprise Access Policy",
                "Internal IT Team",
                "Specifies strict corporate encryption mandates"
        );

        // 1. Create Policy
        String responseJson = mockMvc.perform(post("/api/v1/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Enterprise Access Policy")))
                .andExpect(jsonPath("$.status", is(PolicyStatus.DRAFT.name())))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(responseJson).get("id").asText();

        // 2. Link Regulatory Rule
        UUID regRuleId = UUID.randomUUID();
        LinkRuleCommandRequestBody linkRequest = new LinkRuleCommandRequestBody(
                "Corporate Health Check Mapping",
                regRuleId.toString(),
                "Binds access controls to HIPAA standards"
        );

        mockMvc.perform(post("/api/v1/policies/" + id + "/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(linkRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ruleLinks", hasSize(1)))
                .andExpect(jsonPath("$.ruleLinks[0].organizationalRuleName", is("Corporate Health Check Mapping")))
                .andExpect(jsonPath("$.ruleLinks[0].regulatoryRuleId", is(regRuleId.toString())));

        // 3. Get Details
        mockMvc.perform(get("/api/v1/policies/" + id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Enterprise Access Policy")))
                .andExpect(jsonPath("$.ruleLinks", hasSize(1)));

        // 4. Activate Policy
        mockMvc.perform(post("/api/v1/policies/" + id + "/activate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(PolicyStatus.ACTIVE.name())));

        // 5. Deprecate Policy
        mockMvc.perform(post("/api/v1/policies/" + id + "/deprecate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(PolicyStatus.DEPRECATED.name())));
    }

    @Test
    @DisplayName("Should expose correct CORS response headers")
    void shouldVerifyCorsHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/policies")
                .header("Origin", "http://localhost:5173")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
