package com.pqvcf.translation.domain.validator;

import com.pqvcf.translation.domain.model.DeonticFormula;
import com.pqvcf.translation.domain.model.DeonticOperator;
import com.pqvcf.translation.domain.model.LegalRule;
import java.util.List;
import java.util.Optional;

/**
 * Domain Service for verifying the logical consistency of legal rules.
 * Identifies direct deontic logic contradictions (e.g. Permission vs Prohibition).
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public class RuleValidator {

    public record ValidationResult(boolean isValid, String message) {}

    /**
     * Checks if a candidate rule contradicts any existing rules.
     *
     * @param existingRules the current rules in force
     * @param candidate     the newly translated rule candidate
     * @return result stating if consistent or details of the contradiction found
     */
    public ValidationResult validateConsistency(List<LegalRule> existingRules, DeonticFormula candidate) {
        for (LegalRule rule : existingRules) {
            DeonticFormula formula = rule.getDeonticFormula();
            
            // Check structural equivalence of target, subject and action
            if (ObjectsEqualsIgnoreCase(formula.getSubject(), candidate.getSubject()) &&
                ObjectsEqualsIgnoreCase(formula.getAction(), candidate.getAction()) &&
                ObjectsEqualsIgnoreCase(formula.getTarget(), candidate.getTarget())) {
                
                // 1. Contradiction: Permission vs Prohibition
                if (isContradictory(formula.getOperator(), candidate.getOperator())) {
                    // Check if constraints are similar / overlapping (or if one has no constraint, making it absolute)
                    boolean constraintOverlap = checkConstraintOverlap(formula.getConstraint(), candidate.getConstraint());
                    if (constraintOverlap) {
                        return new ValidationResult(false, String.format(
                                "Logical Conflict: Deontic contradiction detected with %s (%s). " +
                                "Rule 1 asserts '%s', while Rule 2 asserts '%s' on target '%s'.",
                                rule.getArticleNumber(),
                                rule.getRegulationShortName(),
                                formula.getOperator(),
                                candidate.getOperator(),
                                candidate.getTarget()
                        ));
                    }
                }
            }
        }
        return new ValidationResult(true, "Rule consistency validated. No logical conflicts detected.");
    }

    private boolean isContradictory(DeonticOperator op1, DeonticOperator op2) {
        if (op1 == DeonticOperator.PROHIBITION && op2 == DeonticOperator.PERMISSION) return true;
        if (op1 == DeonticOperator.PERMISSION && op2 == DeonticOperator.PROHIBITION) return true;
        if (op1 == DeonticOperator.PROHIBITION && op2 == DeonticOperator.EXEMPTION) return true;
        if (op1 == DeonticOperator.EXEMPTION && op2 == DeonticOperator.PROHIBITION) return true;
        return false;
    }

    private boolean checkConstraintOverlap(String c1, String c2) {
        // If either constraint is absolute (blank/empty), it covers everything, so they overlap.
        if (c1 == null || c1.isBlank() || c2 == null || c2.isBlank()) return true;
        // Simple string containment check as a heuristic for AST match
        return c1.equalsIgnoreCase(c2) || c1.contains(c2) || c2.contains(c1);
    }

    private boolean ObjectsEqualsIgnoreCase(String s1, String s2) {
        if (s1 == null || s2 == null) return false;
        return s1.trim().equalsIgnoreCase(s2.trim());
    }
}
