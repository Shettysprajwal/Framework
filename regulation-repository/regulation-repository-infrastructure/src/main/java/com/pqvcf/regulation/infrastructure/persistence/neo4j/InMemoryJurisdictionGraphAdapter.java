package com.pqvcf.regulation.infrastructure.persistence.neo4j;

import com.pqvcf.regulation.domain.repository.JurisdictionGraphRepository;
import com.pqvcf.shared.types.JurisdictionCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "pqvcf.neo4j.enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryJurisdictionGraphAdapter implements JurisdictionGraphRepository {

    private static final Logger log = LoggerFactory.getLogger(InMemoryJurisdictionGraphAdapter.class);

    private final Set<String> jurisdictions = new HashSet<>();
    private final Map<String, Set<String>> adequacyDecisions = new HashMap<>();
    private final Map<String, Map<String, Object>> nodes = new HashMap<>();
    private final List<Map<String, Object>> edges = new ArrayList<>();
    private long nodeCounter = 0;
    private long edgeCounter = 0;

    public InMemoryJurisdictionGraphAdapter() {
        log.info("Initialized In-Memory (Fallback) Jurisdiction Graph Adapter");
    }

    @Override
    public synchronized void addJurisdiction(JurisdictionCode code) {
        jurisdictions.add(code.getCode());
        String nodeKey = "Jurisdiction:" + code.getCode();
        if (!nodes.containsKey(nodeKey)) {
            Map<String, Object> props = new HashMap<>();
            props.put("code", code.getCode());
            addNode(nodeKey, List.of("Jurisdiction"), props);
        }
    }

    @Override
    public synchronized void addAdequacyDecision(JurisdictionCode source, JurisdictionCode target) {
        addJurisdiction(source);
        addJurisdiction(target);
        adequacyDecisions.computeIfAbsent(source.getCode(), k -> new HashSet<>()).add(target.getCode());

        String sourceKey = "Jurisdiction:" + source.getCode();
        String targetKey = "Jurisdiction:" + target.getCode();
        addEdge("IS_ADEQUATE_FOR", sourceKey, targetKey);
    }

    @Override
    public synchronized boolean isAdequate(JurisdictionCode source, JurisdictionCode target) {
        if (source.equals(target)) return true;
        Set<String> visited = new HashSet<>();
        return dfsAdequacy(source.getCode(), target.getCode(), visited);
    }

    private boolean dfsAdequacy(String current, String target, Set<String> visited) {
        if (current.equals(target)) return true;
        if (visited.contains(current)) return false;
        visited.add(current);

        Set<String> targets = adequacyDecisions.get(current);
        if (targets != null) {
            for (String t : targets) {
                if (dfsAdequacy(t, target, visited)) return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void syncRegulation(String regulationId, String shortName, JurisdictionCode jurisdiction) {
        addJurisdiction(jurisdiction);
        String regKey = "Regulation:" + regulationId;
        Map<String, Object> props = new HashMap<>();
        props.put("id", regulationId);
        props.put("shortName", shortName.toUpperCase());
        addNode(regKey, List.of("Regulation"), props);

        addEdge("APPLIES_IN", regKey, "Jurisdiction:" + jurisdiction.getCode());
    }

    @Override
    public synchronized void syncArticle(String articleId, String regulationId, String articleNumber, String title) {
        String artKey = "Article:" + articleId;
        Map<String, Object> props = new HashMap<>();
        props.put("id", articleId);
        props.put("articleNumber", articleNumber);
        props.put("title", title);
        addNode(artKey, List.of("Article"), props);

        addEdge("HAS_ARTICLE", "Regulation:" + regulationId, artKey);
    }

    @Override
    public synchronized void syncClause(String clauseId, String articleId, String clauseNumber, String clauseType) {
        String clsKey = "Clause:" + clauseId;
        Map<String, Object> props = new HashMap<>();
        props.put("id", clauseId);
        props.put("clauseNumber", clauseNumber);
        props.put("clauseType", clauseType);
        addNode(clsKey, List.of("Clause"), props);

        addEdge("HAS_CLAUSE", "Article:" + articleId, clsKey);
    }

    @Override
    public synchronized Map<String, Object> getGraphData() {
        Map<String, Object> graph = new HashMap<>();
        List<Map<String, Object>> nodesList = new ArrayList<>();
        
        // Map string keys to serial numbers for visualization
        Map<String, String> keyToIdMap = new HashMap<>();
        for (var entry : nodes.entrySet()) {
            keyToIdMap.put(entry.getKey(), (String) entry.getValue().get("id"));
            nodesList.add(entry.getValue());
        }

        List<Map<String, Object>> edgesList = new ArrayList<>();
        for (var edge : edges) {
            Map<String, Object> serialEdge = new HashMap<>(edge);
            serialEdge.put("source", keyToIdMap.getOrDefault(edge.get("source"), "0"));
            serialEdge.put("target", keyToIdMap.getOrDefault(edge.get("target"), "0"));
            edgesList.add(serialEdge);
        }

        graph.put("nodes", nodesList);
        graph.put("edges", edgesList);
        return graph;
    }

    private void addNode(String key, List<String> labels, Map<String, Object> props) {
        if (!nodes.containsKey(key)) {
            nodeCounter++;
            Map<String, Object> node = new HashMap<>();
            node.put("id", String.valueOf(nodeCounter));
            node.put("labels", labels);
            node.put("properties", props);
            nodes.put(key, node);
        }
    }

    private void addEdge(String type, String sourceKey, String targetKey) {
        if (!nodes.containsKey(sourceKey) || !nodes.containsKey(targetKey)) {
            return;
        }
        // Check for duplicates
        boolean exists = edges.stream()
                .anyMatch(e -> e.get("type").equals(type) &&
                               e.get("source").equals(sourceKey) &&
                               e.get("target").equals(targetKey));
        if (!exists) {
            edgeCounter++;
            Map<String, Object> edge = new HashMap<>();
            edge.put("id", String.valueOf(edgeCounter));
            edge.put("type", type);
            edge.put("source", sourceKey);
            edge.put("target", targetKey);
            edges.add(edge);
        }
    }
}
