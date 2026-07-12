package com.pqvcf.ledger.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pqvcf.ledger.api.AuditingLedgerApplication;
import com.pqvcf.ledger.application.port.in.SealRecordUseCase.SealRecordCommand;
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

@SpringBootTest(classes = AuditingLedgerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditingLedgerControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should execute end-to-end ledger logging, validation, tampering detection workflow")
    void shouldExecuteLedgerWorkflow() throws Exception {
        // Clear database blocks first
        mockMvc.perform(post("/api/v1/ledger/reset")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 1. Seal Genesis Block
        SealRecordCommand genCommand = new SealRecordCommand("TRANSLATE", "SYSTEM", "GDPR-Art-46", "TRANSLATED");
        String record1 = mockMvc.perform(post("/api/v1/ledger/seal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(genCommand)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.previousHash", is("GENESIS_HASH")))
                .andExpect(jsonPath("$.currentHash").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String record1Hash = objectMapper.readTree(record1).get("currentHash").asText();

        // 2. Seal Child Block
        SealRecordCommand childCommand = new SealRecordCommand("EVALUATE", "ADMIN", "PDP-Request-101", "PERMIT");
        mockMvc.perform(post("/api/v1/ledger/seal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(childCommand)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.previousHash", is(record1Hash)))
                .andExpect(jsonPath("$.currentHash").isNotEmpty());

        // 3. Verify Chain Integrity (Should be valid)
        mockMvc.perform(get("/api/v1/ledger/verify")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(true)))
                .andExpect(jsonPath("$.tamperedIndex", is(-1)));

        // 4. List records
        mockMvc.perform(get("/api/v1/ledger/records")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // 5. Tamper with Genesis Block (break SHA-256 bindings)
        mockMvc.perform(post("/api/v1/ledger/tamper/0")
                .param("tamperedValue", "DENY")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 6. Verify Chain Integrity (Should detect tampered index 0)
        mockMvc.perform(get("/api/v1/ledger/verify")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(false)))
                .andExpect(jsonPath("$.tamperedIndex", is(0)))
                .andExpect(jsonPath("$.details").value(org.hamcrest.Matchers.containsString("Tampered data detected")));
    }

    @Test
    @DisplayName("Should verify correct CORS configuration headers")
    void shouldVerifyCorsHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/ledger/records")
                .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
