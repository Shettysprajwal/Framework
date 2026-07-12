package com.pqvcf.pip.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pqvcf.pip.api.PolicyInformationPointApplication;
import com.pqvcf.pip.application.port.in.ResolveAttributesUseCase.ResolveAttributesQuery;
import com.pqvcf.pip.api.rest.PipController.RegisterAttributeRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PolicyInformationPointApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PipControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should dynamically resolve subject and resource attributes, support admin overrides, and check transitively")
    void shouldExecutePipResolutionFlow() throws Exception {
        ResolveAttributesQuery query = new ResolveAttributesQuery(
                "analyst",
                "health-records",
                "read",
                "IN",
                "EU"
        );

        // 1. Resolve Attributes (Before Override)
        mockMvc.perform(post("/api/v1/pip/resolve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectId", is("analyst")))
                .andExpect(jsonPath("$.resourceId", is("health-records")))
                .andExpect(jsonPath("$.attributes", hasSize(6))) // 3 subject + 3 resource attributes
                .andExpect(jsonPath("$.transitiveAdequate", is(true))); // IN -> EU fallback is true

        // 2. Override Subject Attribute
        RegisterAttributeRequest overrideRequest = new RegisterAttributeRequest(
                "analyst",
                "clearance",
                "restricted-access",
                "String"
        );

        mockMvc.perform(post("/api/v1/pip/attributes/subject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(overrideRequest)))
                .andExpect(status().isCreated());

        // 3. Resolve Attributes (After Override)
        mockMvc.perform(post("/api/v1/pip/resolve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attributes[1].key", is("clearance")))
                .andExpect(jsonPath("$.attributes[1].value", is("restricted-access")));
    }

    @Test
    @DisplayName("Should expose correct CORS response headers")
    void shouldVerifyCorsHeaders() throws Exception {
        ResolveAttributesQuery query = new ResolveAttributesQuery("guest", "public-data", "read", "", "");

        mockMvc.perform(post("/api/v1/pip/resolve")
                .header("Origin", "http://localhost:5173")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
