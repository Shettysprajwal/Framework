package com.pqvcf.pip.domain.resolver;

import com.pqvcf.pip.domain.model.ContextAttribute;
import java.util.List;

/**
 * Port interface for resolving environment and subject attributes dynamically.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public interface AttributeResolver {
    
    /**
     * Resolves context attributes associated with a specific subject.
     */
    List<ContextAttribute> resolveSubjectAttributes(String subjectId);

    /**
     * Resolves context attributes associated with a resource and target action.
     */
    List<ContextAttribute> resolveResourceAttributes(String resourceId);
}
