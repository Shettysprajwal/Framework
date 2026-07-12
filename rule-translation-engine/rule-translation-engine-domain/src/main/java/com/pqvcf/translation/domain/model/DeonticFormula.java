package com.pqvcf.translation.domain.model;

import com.pqvcf.shared.domain.ValueObject;
import java.util.Objects;

/**
 * Value Object representing a single Deontic logic rule formula.
 * Expresses a normative relation: Operator(Subject, Action, Target, Constraint).
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class DeonticFormula extends ValueObject {

    private final DeonticOperator operator;
    private final String subject;
    private final String action;
    private final String target;
    private final String constraint;

    public DeonticFormula(DeonticOperator operator, String subject, String action, String target, String constraint) {
        this.operator = requireNonNull(operator, "Deontic operator");
        this.subject = requireNonBlank(subject, "Subject identifier");
        this.action = requireNonBlank(action, "Action identifier");
        this.target = requireNonBlank(target, "Target resource");
        this.constraint = constraint != null ? constraint.trim() : "";
    }

    public DeonticOperator getOperator() { return operator; }
    public String getSubject() { return subject; }
    public String getAction() { return action; }
    public String getTarget() { return target; }
    public String getConstraint() { return constraint; }

    /**
     * Checks if this formula represents a prohibition.
     */
    public boolean isProhibition() {
        return operator == DeonticOperator.PROHIBITION;
    }

    /**
     * Checks if this formula has a conditional constraint.
     */
    public boolean hasConstraint() {
        return !constraint.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeonticFormula that)) return false;
        return operator == that.operator &&
               Objects.equals(subject, that.subject) &&
               Objects.equals(action, that.action) &&
               Objects.equals(target, that.target) &&
               Objects.equals(constraint, that.constraint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, subject, action, target, constraint);
    }

    @Override
    public String toString() {
        return String.format("%s(%s, %s, %s, {%s})", operator.getSymbol(), subject, action, target, constraint);
    }
}
