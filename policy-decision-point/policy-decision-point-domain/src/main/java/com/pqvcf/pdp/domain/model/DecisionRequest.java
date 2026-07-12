package com.pqvcf.pdp.domain.model;

import com.pqvcf.shared.domain.ValueObject;
import java.util.Objects;

/**
 * Value Object representing the variables submitted for authorization evaluation.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class DecisionRequest extends ValueObject {

    private final String subjectId;
    private final String resourceId;
    private final String actionId;
    private final String sourceCountry;
    private final String targetCountry;
    private final String policyName;

    public DecisionRequest(
            String subjectId,
            String resourceId,
            String actionId,
            String sourceCountry,
            String targetCountry,
            String policyName) {
        this.subjectId = requireNonBlank(subjectId, "Subject ID");
        this.resourceId = requireNonBlank(resourceId, "Resource ID");
        this.actionId = requireNonBlank(actionId, "Action ID");
        this.sourceCountry = sourceCountry != null ? sourceCountry.trim() : "";
        this.targetCountry = targetCountry != null ? targetCountry.trim() : "";
        this.policyName = policyName != null ? policyName.trim() : "";
    }

    private static String requireNonBlank(String val, String field) {
        if (val == null || val.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
        return val.trim();
    }

    public String getSubjectId() { return subjectId; }
    public String getResourceId() { return resourceId; }
    public String getActionId() { return actionId; }
    public String getSourceCountry() { return sourceCountry; }
    public String getTargetCountry() { return targetCountry; }
    public String getPolicyName() { return policyName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DecisionRequest that)) return false;
        return Objects.equals(subjectId, that.subjectId) &&
                Objects.equals(resourceId, that.resourceId) &&
                Objects.equals(actionId, that.actionId) &&
                Objects.equals(sourceCountry, that.sourceCountry) &&
                Objects.equals(targetCountry, that.targetCountry) &&
                Objects.equals(policyName, that.policyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subjectId, resourceId, actionId, sourceCountry, targetCountry, policyName);
    }
}
