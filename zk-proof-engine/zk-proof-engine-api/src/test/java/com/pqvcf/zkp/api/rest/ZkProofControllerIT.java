package com.pqvcf.zkp.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pqvcf.zkp.api.ZkProofApplication;
import com.pqvcf.zkp.application.port.in.GenerateProofUseCase.GenerateProofCommand;
import com.pqvcf.zkp.application.port.in.VerifyProofUseCase.VerifyProofCommand;
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

@SpringBootTest(classes = ZkProofApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ZkProofControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should execute end-to-end ZK proof generation, Pedersen commitments verification, and Sigma provers checking flow")
    void shouldExecuteZkVerificationWorkflow() throws Exception {
        GenerateProofCommand genCommand = new GenerateProofCommand(
                "DATA_RESIDENCY",
                101, // secret server code (witness)
                "{\"required_zone\": \"EU\"}" // public parameters
        );

        // 1. Generate ZK Proof
        String responseContent = mockMvc.perform(post("/api/v1/zkp/prove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(genCommand)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.proofType", is("DATA_RESIDENCY")))
                .andExpect(jsonPath("$.commitmentHex").isNotEmpty())
                .andExpect(jsonPath("$.challengeHex").isNotEmpty())
                .andExpect(jsonPath("$.responseHex").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String proofId = objectMapper.readTree(responseContent).get("proofId").asText();
        String commitmentHex = objectMapper.readTree(responseContent).get("commitmentHex").asText();
        String challengeHex = objectMapper.readTree(responseContent).get("challengeHex").asText();
        String responseHex = objectMapper.readTree(responseContent).get("responseHex").asText();

        // 2. List ZK Proofs
        mockMvc.perform(get("/api/v1/zkp/proofs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // 3. Verify valid ZK Proof
        VerifyProofCommand verifyCommand = new VerifyProofCommand(
                proofId,
                challengeHex,
                responseHex,
                commitmentHex
        );

        mockMvc.perform(post("/api/v1/zkp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyCommand)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(true)));

        // 4. Verify invalid ZK Proof (tampered commitment hex)
        String fakeCommitmentHex = commitmentHex.substring(2) + "00"; // alter bytes
        VerifyProofCommand tamperedCommand = new VerifyProofCommand(
                proofId,
                challengeHex,
                responseHex,
                fakeCommitmentHex
        );

        mockMvc.perform(post("/api/v1/zkp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tamperedCommand)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(false)));

        // 5. Delete proof
        mockMvc.perform(delete("/api/v1/zkp/proofs/" + proofId))
                .andExpect(status().isNoContent());

        // 6. Check proof is deleted
        mockMvc.perform(get("/api/v1/zkp/proofs/" + proofId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should verify correct CORS configuration headers")
    void shouldVerifyCorsHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/zkp/proofs")
                .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
