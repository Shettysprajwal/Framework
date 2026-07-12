package com.pqvcf.translation.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pqvcf.translation.api.RuleTranslationApplication;
import com.pqvcf.translation.application.port.in.TranslateRuleUseCase.TranslateRuleCommand;
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

@SpringBootTest(classes = RuleTranslationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RuleTranslationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should translate a raw CNL rule successfully and persist in H2 database")
    void shouldTranslateAndPersistRule() throws Exception {
        TranslateRuleCommand command = new TranslateRuleCommand(
                "GDPR",
                "Article 46",
                "1",
                "a controller may transfer personal data if appropriate safeguards are implemented"
        );

        String responseJson = mockMvc.perform(post("/api/v1/translation/translate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.regulationShortName", is("GDPR")))
                .andExpect(jsonPath("$.articleNumber", is("Article 46")))
                .andExpect(jsonPath("$.deonticOperator", is("PERMISSION")))
                .andExpect(jsonPath("$.subject", is("controller")))
                .andExpect(jsonPath("$.action", is("transfer")))
                .andExpect(jsonPath("$.target", is("personal data")))
                .andExpect(jsonPath("$.constraint", is("appropriate safeguards are implemented")))
                .andExpect(jsonPath("$.smtSpec").isNotEmpty())
                .andExpect(jsonPath("$.odrlPolicy").isNotEmpty())
                .andExpect(jsonPath("$.valid", is(true)))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(responseJson).get("id").asText();

        // Verify retrieving the saved rule
        mockMvc.perform(get("/api/v1/translation/rules/" + id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.regulationShortName", is("GDPR")))
                .andExpect(jsonPath("$.articleNumber", is("Article 46")));

        // Verify listing rules by regulation
        mockMvc.perform(get("/api/v1/translation/rules/regulation/GDPR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        // Clean up: delete the rule
        mockMvc.perform(delete("/api/v1/translation/rules/" + id))
                .andExpect(status().isNoContent());

        // Verify it was deleted
        mockMvc.perform(get("/api/v1/translation/rules/" + id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should detect conflict during sequential translation of contradicting rules")
    void shouldDetectConflictBetweenContradictoryRules() throws Exception {
        TranslateRuleCommand permitCommand = new TranslateRuleCommand(
                "DPDP",
                "Section 16",
                "1",
                "a fiduciary is permitted to transfer data"
        );

        TranslateRuleCommand prohibitCommand = new TranslateRuleCommand(
                "DPDP",
                "Section 16",
                "2",
                "a fiduciary is forbidden to transfer data"
        );

        // 1. Post the permission rule (should be valid)
        String permitResponse = mockMvc.perform(post("/api/v1/translation/translate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(permitCommand)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valid", is(true)))
                .andReturn().getResponse().getContentAsString();

        String permitId = objectMapper.readTree(permitResponse).get("id").asText();

        // 2. Post the prohibition rule (should detect conflict)
        String prohibitResponse = mockMvc.perform(post("/api/v1/translation/translate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prohibitCommand)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valid", is(false)))
                .andExpect(jsonPath("$.validationMessage").value(org.hamcrest.Matchers.containsString("Logical Conflict")))
                .andReturn().getResponse().getContentAsString();

        String prohibitId = objectMapper.readTree(prohibitResponse).get("id").asText();

        // Clean up
        mockMvc.perform(delete("/api/v1/translation/rules/" + permitId)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/translation/rules/" + prohibitId)).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should expose correct CORS response headers")
    void shouldVerifyCorsHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/translation/rules")
                .header("Origin", "http://localhost:5173")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
