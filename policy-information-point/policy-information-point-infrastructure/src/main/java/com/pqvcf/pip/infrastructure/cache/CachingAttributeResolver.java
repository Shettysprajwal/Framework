package com.pqvcf.pip.infrastructure.cache;

import com.pqvcf.pip.domain.model.AttributeCategory;
import com.pqvcf.pip.domain.model.ContextAttribute;
import com.pqvcf.pip.domain.resolver.AttributeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CachingAttributeResolver implements AttributeResolver {

    private static final Logger log = LoggerFactory.getLogger(CachingAttributeResolver.class);

    private final Map<String, List<ContextAttribute>> subjectCache = new ConcurrentHashMap<>();
    private final Map<String, List<ContextAttribute>> resourceCache = new ConcurrentHashMap<>();

    public CachingAttributeResolver() {
        prepopulateCaches();
    }

    private void prepopulateCaches() {
        // Pre-populate mock context attributes for research evaluations
        
        // 1. Subjects
        subjectCache.put("admin", List.of(
                new ContextAttribute(AttributeCategory.SUBJECT, "role", "administrator", "String"),
                new ContextAttribute(AttributeCategory.SUBJECT, "clearance", "top-secret", "String"),
                new ContextAttribute(AttributeCategory.SUBJECT, "mfa_enabled", "true", "Boolean")
        ));
        subjectCache.put("analyst", List.of(
                new ContextAttribute(AttributeCategory.SUBJECT, "role", "analyst", "String"),
                new ContextAttribute(AttributeCategory.SUBJECT, "clearance", "internal", "String"),
                new ContextAttribute(AttributeCategory.SUBJECT, "mfa_enabled", "true", "Boolean")
        ));
        subjectCache.put("external_user", List.of(
                new ContextAttribute(AttributeCategory.SUBJECT, "role", "contractor", "String"),
                new ContextAttribute(AttributeCategory.SUBJECT, "clearance", "public", "String"),
                new ContextAttribute(AttributeCategory.SUBJECT, "mfa_enabled", "false", "Boolean")
        ));

        // 2. Resources
        resourceCache.put("personal-data", List.of(
                new ContextAttribute(AttributeCategory.RESOURCE, "classification", "personal", "String"),
                new ContextAttribute(AttributeCategory.RESOURCE, "category", "privacy", "String"),
                new ContextAttribute(AttributeCategory.RESOURCE, "contains_pii", "true", "Boolean")
        ));
        resourceCache.put("health-records", List.of(
                new ContextAttribute(AttributeCategory.RESOURCE, "classification", "phii", "String"),
                new ContextAttribute(AttributeCategory.RESOURCE, "category", "medical", "String"),
                new ContextAttribute(AttributeCategory.RESOURCE, "contains_pii", "true", "Boolean")
        ));
        resourceCache.put("financial-data", List.of(
                new ContextAttribute(AttributeCategory.RESOURCE, "classification", "restricted", "String"),
                new ContextAttribute(AttributeCategory.RESOURCE, "category", "financial", "String"),
                new ContextAttribute(AttributeCategory.RESOURCE, "contains_pii", "false", "Boolean")
        ));
    }

    @Override
    public List<ContextAttribute> resolveSubjectAttributes(String subjectId) {
        log.info("Resolving subject attributes from cache for: {}", subjectId);
        String key = subjectId.toLowerCase().trim();
        
        if (subjectCache.containsKey(key)) {
            return subjectCache.get(key);
        }

        // Dynamic fallback fallback if unknown subject
        log.info("Subject {} not in cache. Resolving default guest attributes.", subjectId);
        List<ContextAttribute> defaultAttrs = List.of(
                new ContextAttribute(AttributeCategory.SUBJECT, "role", "guest", "String"),
                new ContextAttribute(AttributeCategory.SUBJECT, "clearance", "public", "String"),
                new ContextAttribute(AttributeCategory.SUBJECT, "mfa_enabled", "false", "Boolean")
        );
        subjectCache.put(key, defaultAttrs);
        return defaultAttrs;
    }

    @Override
    public List<ContextAttribute> resolveResourceAttributes(String resourceId) {
        log.info("Resolving resource attributes from cache for: {}", resourceId);
        String key = resourceId.toLowerCase().trim();

        if (resourceCache.containsKey(key)) {
            return resourceCache.get(key);
        }

        // Dynamic fallback if unknown resource
        log.info("Resource {} not in cache. Resolving default public attributes.", resourceId);
        List<ContextAttribute> defaultAttrs = List.of(
                new ContextAttribute(AttributeCategory.RESOURCE, "classification", "public", "String"),
                new ContextAttribute(AttributeCategory.RESOURCE, "category", "general", "String"),
                new ContextAttribute(AttributeCategory.RESOURCE, "contains_pii", "false", "Boolean")
        );
        resourceCache.put(key, defaultAttrs);
        return defaultAttrs;
    }

    /**
     * Expose admin method to dynamically register context attributes.
     */
    public void registerSubjectAttribute(String subjectId, String attrKey, String attrVal, String type) {
        String subKey = subjectId.toLowerCase().trim();
        List<ContextAttribute> list = new ArrayList<>(subjectCache.getOrDefault(subKey, List.of()));
        list.removeIf(attr -> attr.getKey().equalsIgnoreCase(attrKey.trim()));
        list.add(new ContextAttribute(AttributeCategory.SUBJECT, attrKey.trim(), attrVal, type));
        subjectCache.put(subKey, list);
    }

    public void registerResourceAttribute(String resourceId, String attrKey, String attrVal, String type) {
        String resKey = resourceId.toLowerCase().trim();
        List<ContextAttribute> list = new ArrayList<>(resourceCache.getOrDefault(resKey, List.of()));
        list.removeIf(attr -> attr.getKey().equalsIgnoreCase(attrKey.trim()));
        list.add(new ContextAttribute(AttributeCategory.RESOURCE, attrKey.trim(), attrVal, type));
        resourceCache.put(resKey, list);
    }
}
