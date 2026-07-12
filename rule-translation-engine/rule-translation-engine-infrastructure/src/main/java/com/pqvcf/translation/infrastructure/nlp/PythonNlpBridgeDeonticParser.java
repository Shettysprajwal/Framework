package com.pqvcf.translation.infrastructure.nlp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pqvcf.translation.domain.model.DeonticFormula;
import com.pqvcf.translation.domain.model.DeonticOperator;
import com.pqvcf.translation.domain.parser.DeonticParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PythonNlpBridgeDeonticParser implements DeonticParser {

    private static final Logger log = LoggerFactory.getLogger(PythonNlpBridgeDeonticParser.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${pqvcf.nlp.url:http://localhost:5001/extract}")
    private String nlpUrl;

    public PythonNlpBridgeDeonticParser() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public DeonticFormula parse(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            throw new IllegalArgumentException("CNL raw text must not be empty");
        }

        try {
            log.info("Sending translation request to Python NLP bridge: {}", nlpUrl);
            Map<String, String> requestBody = Map.of("text", rawText);
            String jsonPayload = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(nlpUrl))
                    .timeout(Duration.ofSeconds(4))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String operatorStr = root.path("operator").asText("PERMISSION");
                String subject = root.path("subject").asText("actor");
                String action = root.path("action").asText("process");
                String target = root.path("target").asText("data");
                String constraint = root.path("constraint").asText("");

                return new DeonticFormula(
                        DeonticOperator.valueOf(operatorStr.toUpperCase()),
                        subject,
                        action,
                        target,
                        constraint
                );
            } else {
                log.warn("Python NLP service returned status: {}. Executing regex parsing fallback.", response.statusCode());
            }
        } catch (Exception e) {
            log.error("Failed to connect to Python NLP Bridge ({}). Executing regex parsing fallback.", e.getMessage());
        }

        return executeRegexFallback(rawText);
    }

    /**
     * Parse legal statements using deterministic regular expression checks if Python service is offline.
     * Matches typical CNL patterns: "Subject shall/may/must action target if constraint"
     */
    private DeonticFormula executeRegexFallback(String text) {
        log.info("Executing regex-based deterministic legal parsing fallback...");
        String normalized = text.toLowerCase().trim();

        // 1. Classify Operator
        DeonticOperator operator = DeonticOperator.PERMISSION;
        if (normalized.contains("shall not") || normalized.contains("must not") || normalized.contains("prohibited") || normalized.contains("forbidden") || normalized.contains("restrict")) {
            operator = DeonticOperator.PROHIBITION;
        } else if (normalized.contains("shall") || normalized.contains("must") || normalized.contains("obliged") || normalized.contains("required")) {
            operator = DeonticOperator.OBLIGATION;
        } else if (normalized.contains("exempt") || normalized.contains("unless") || normalized.contains("except")) {
            operator = DeonticOperator.EXEMPTION;
        }

        // 2. Extract Constraint (Text after if/unless/subject to)
        String constraint = "";
        Pattern constraintPattern = Pattern.compile("(?i)(?:if|unless|except when|subject to)\\s+(.+)");
        Matcher constraintMatcher = constraintPattern.matcher(text);
        if (constraintMatcher.find()) {
            constraint = constraintMatcher.group(1).trim();
            // strip trailing period if present
            if (constraint.endsWith(".")) {
                constraint = constraint.substring(0, constraint.length() - 1);
            }
        }

        // Strip constraint text from original string to isolate subject/action/target
        String baseSentence = text;
        if (constraintMatcher.find(0)) {
            baseSentence = text.substring(0, constraintMatcher.start()).trim();
        }

        // 3. Extract Subject, Action, Target
        String subject = "controller";
        String action = "transfer";
        String target = "personal_data";

        // Regex heuristic: "Subject modalVerb Action Target"
        // e.g. "a controller may transfer personal data"
        Pattern sentencePattern = Pattern.compile("(?i)^(.+?)\\s+(?:shall not|must not|shall|must|may|can|should|is obliged to|is permitted to)\\s+(.+?)\\s+(.+)$");
        Matcher sentenceMatcher = sentencePattern.matcher(baseSentence);
        if (sentenceMatcher.find()) {
            subject = sentenceMatcher.group(1).trim();
            action = sentenceMatcher.group(2).trim();
            target = sentenceMatcher.group(3).trim();
            if (target.endsWith(".")) {
                target = target.substring(0, target.length() - 1);
            }
        } else {
            // simpler search: match action word
            if (normalized.contains("transfer")) {
                action = "transfer";
            } else if (normalized.contains("process")) {
                action = "process";
            }
        }

        return new DeonticFormula(operator, subject, action, target, constraint);
    }
}
