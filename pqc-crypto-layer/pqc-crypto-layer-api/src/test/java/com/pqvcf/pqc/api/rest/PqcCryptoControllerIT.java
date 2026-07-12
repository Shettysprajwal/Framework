package com.pqvcf.pqc.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pqvcf.pqc.api.PqcCryptoApplication;
import com.pqvcf.pqc.application.port.in.GenerateKeyUseCase.GenerateKeyCommand;
import com.pqvcf.pqc.application.port.in.SignPayloadUseCase.SignCommand;
import com.pqvcf.pqc.application.port.in.SignPayloadUseCase.VerifyCommand;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PqcCryptoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PqcCryptoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should execute end-to-end PQC key generation, ML-DSA signing, and signature verification flow")
    void shouldExecutePqcSignatureWorkflow() throws Exception {
        GenerateKeyCommand genCommand = new GenerateKeyCommand("ML_DSA_65", "mldsa-test-key");

        // 1. Generate key
        mockMvc.perform(post("/api/v1/pqc/keys")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(genCommand)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.keyId", is("mldsa-test-key")))
                .andExpect(jsonPath("$.algorithm", is("ML_DSA_65")))
                .andExpect(jsonPath("$.publicKeyHex").isNotEmpty());

        // 2. Fetch keys list
        mockMvc.perform(get("/api/v1/pqc/keys")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // 3. Sign a payload hex (e.g. "hello world" encoded as hex)
        String payloadHex = "68656c6c6f20776f726c64";
        SignCommand signCommand = new SignCommand("mldsa-test-key", payloadHex);

        String resultJson = mockMvc.perform(post("/api/v1/pqc/sign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signCommand)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keyId", is("mldsa-test-key")))
                .andExpect(jsonPath("$.signatureHex").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String signatureHex = objectMapper.readTree(resultJson).get("signatureHex").asText();

        // 4. Verify valid signature
        VerifyCommand verifyCommand = new VerifyCommand("mldsa-test-key", payloadHex, signatureHex);
        mockMvc.perform(post("/api/v1/pqc/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyCommand)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(true)));

        // 5. Verify invalid signature (tampered signature payload)
        VerifyCommand tamperedCommand = new VerifyCommand("mldsa-test-key", "0011", signatureHex);
        mockMvc.perform(post("/api/v1/pqc/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tamperedCommand)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(false)));

        // 6. Delete key
        mockMvc.perform(delete("/api/v1/pqc/keys/mldsa-test-key"))
                .andExpect(status().isNoContent());

        // 7. Check key is deleted
        mockMvc.perform(get("/api/v1/pqc/keys/mldsa-test-key"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should verify correct CORS configuration headers")
    void shouldVerifyCorsHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/pqc/keys")
                .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
