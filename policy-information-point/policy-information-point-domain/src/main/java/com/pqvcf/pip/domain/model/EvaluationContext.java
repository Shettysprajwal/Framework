package com.pqvcf.pip.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Collection representing the compiled attributes resolving during policy authorization.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public class EvaluationContext {

    private final String subjectId;
    private final String resourceId;
    private final String actionId;
    private final List<ContextAttribute> attributes;

    public EvaluationContext(String subjectId, String resourceId, String actionId) {
        this.subjectId = subjectId != null ? subjectId.trim() : "anonymous";
        this.resourceId = resourceId != null ? resourceId.trim() : "";
        this.actionId = actionId != null ? actionId.trim() : "";
        this.attributes = new ArrayList<>();
    }

    public void addAttribute(ContextAttribute attribute) {
        if (attribute == null) return;
        
        // Remove existing attribute under category/key if already resolved
        attributes.removeIf(attr -> attr.getCategory() == attribute.getCategory() &&
                attr.getKey().equalsIgnoreCase(attribute.getKey()));

        attributes.add(attribute);
    }

    public Optional<ContextAttribute> getAttribute(AttributeCategory category, String key) {
        return attributes.stream()
                .filter(attr -> attr.getCategory() == category && attr.getKey().equalsIgnoreCase(key))
                .findFirst();
    }

    public String getSubjectId() { return subjectId; }
    public String getResourceId() { return resourceId; }
    public String getActionId() { return actionId; }
    public List<ContextAttribute> getAttributes() { return Collections.unmodifiableList(attributes); }
}
