package com.pqvcf.translation.application.translator;

import com.pqvcf.translation.domain.model.DeonticFormula;

/**
 * Utility to generate W3C ODRL compliance policies (JSON-LD format) from DeonticFormulas.
 * See: https://www.w3.org/TR/odrl-model/
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class OdrlPolicyGenerator {

    private OdrlPolicyGenerator() {}

    /**
     * Translates the given DeonticFormula to ODRL JSON-LD.
     */
    public static String generate(DeonticFormula formula) {
        String relationType;
        switch (formula.getOperator()) {
            case PERMISSION -> relationType = "permission";
            case OBLIGATION -> relationType = "obligation";
            case PROHIBITION -> relationType = "prohibition";
            case EXEMPTION -> relationType = "exemption";
            default -> relationType = "permission";
        }

        String constraintJson = "";
        if (formula.hasConstraint()) {
            constraintJson = String.format(
                ", \"constraint\": [{ \"leftOperand\": \"processing_condition\", \"operator\": \"eq\", \"rightOperand\": \"%s\" }]",
                formula.getConstraint()
            );
        }

        return String.format(
            "{\n" +
            "  \"@context\": \"http://www.w3.org/ns/odrl.jsonld\",\n" +
            "  \"@type\": \"Policy\",\n" +
            "  \"%s\": [{\n" +
            "    \"action\": \"%s\",\n" +
            "    \"target\": \"%s\",\n" +
            "    \"assignee\": \"%s\"%s\n" +
            "  }]\n" +
            "}",
            relationType,
            formula.getAction(),
            formula.getTarget(),
            formula.getSubject(),
            constraintJson
        );
    }
}
