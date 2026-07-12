package com.pqvcf.regulation.infrastructure.persistence.neo4j;

import com.pqvcf.regulation.domain.repository.JurisdictionGraphRepository;
import com.pqvcf.shared.types.JurisdictionCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "pqvcf.neo4j.enabled", havingValue = "true", matchIfMissing = false)
public class Neo4jRegulationGraphAdapter implements JurisdictionGraphRepository {

    private static final Logger log = LoggerFactory.getLogger(Neo4jRegulationGraphAdapter.class);

    private final Neo4jClient neo4jClient;

    public Neo4jRegulationGraphAdapter(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
        log.info("Initialized Neo4j Regulation Graph Adapter");
    }

    @Override
    public void addJurisdiction(JurisdictionCode code) {
        try {
            neo4jClient.query("MERGE (j:Jurisdiction {code: $code})")
                    .bind(code.getCode()).to("code")
                    .run();
        } catch (Exception e) {
            log.error("Failed to add jurisdiction to Neo4j: {}", e.getMessage());
        }
    }

    @Override
    public void addAdequacyDecision(JurisdictionCode source, JurisdictionCode target) {
        try {
            neo4jClient.query(
                    "MERGE (s:Jurisdiction {code: $source}) " +
                    "MERGE (t:Jurisdiction {code: $target}) " +
                    "MERGE (s)-[:IS_ADEQUATE_FOR]->(t)"
            )
            .bind(source.getCode()).to("source")
            .bind(target.getCode()).to("target")
            .run();
        } catch (Exception e) {
            log.error("Failed to add adequacy decision to Neo4j: {}", e.getMessage());
        }
    }

    @Override
    public boolean isAdequate(JurisdictionCode source, JurisdictionCode target) {
        try {
            return neo4jClient.query(
                    "MATCH p=(s:Jurisdiction {code: $source})-[:IS_ADEQUATE_FOR*0..3]->(t:Jurisdiction {code: $target}) " +
                    "RETURN count(p) > 0 AS adequate"
            )
            .bind(source.getCode()).to("source")
            .bind(target.getCode()).to("target")
            .fetchAs(Boolean.class)
            .one()
            .orElse(false);
        } catch (Exception e) {
            log.error("Failed to check adequacy in Neo4j: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void syncRegulation(String regulationId, String shortName, JurisdictionCode jurisdiction) {
        try {
            neo4jClient.query(
                    "MERGE (r:Regulation {id: $id}) " +
                    "SET r.shortName = $shortName " +
                    "MERGE (j:Jurisdiction {code: $jurisdiction}) " +
                    "MERGE (r)-[:APPLIES_IN]->(j)"
            )
            .bind(regulationId).to("id")
            .bind(shortName.toUpperCase()).to("shortName")
            .bind(jurisdiction.getCode()).to("jurisdiction")
            .run();
        } catch (Exception e) {
            log.error("Failed to sync regulation to Neo4j: {}", e.getMessage());
        }
    }

    @Override
    public void syncArticle(String articleId, String regulationId, String articleNumber, String title) {
        try {
            neo4jClient.query(
                    "MERGE (a:Article {id: $id}) " +
                    "SET a.articleNumber = $articleNumber, a.title = $title " +
                    "MERGE (r:Regulation {id: $regulationId}) " +
                    "MERGE (r)-[:HAS_ARTICLE]->(a)"
            )
            .bind(articleId).to("id")
            .bind(regulationId).to("regulationId")
            .bind(articleNumber).to("articleNumber")
            .bind(title).to("title")
            .run();
        } catch (Exception e) {
            log.error("Failed to sync article to Neo4j: {}", e.getMessage());
        }
    }

    @Override
    public void syncClause(String clauseId, String articleId, String clauseNumber, String clauseType) {
        try {
            neo4jClient.query(
                    "MERGE (c:Clause {id: $id}) " +
                    "SET c.clauseNumber = $clauseNumber, c.clauseType = $clauseType " +
                    "MERGE (a:Article {id: $articleId}) " +
                    "MERGE (a)-[:HAS_CLAUSE]->(c)"
            )
            .bind(clauseId).to("id")
            .bind(articleId).to("articleId")
            .bind(clauseNumber).to("clauseNumber")
            .bind(clauseType).to("clauseType")
            .run();
        } catch (Exception e) {
            log.error("Failed to sync clause to Neo4j: {}", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getGraphData() {
        Map<String, Object> graph = new HashMap<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();

        try {
            // Get all nodes
            neo4jClient.query(
                    "MATCH (n) RETURN id(n) as rawId, labels(n) as labels, properties(n) as props"
            )
            .fetch().all().forEach(row -> {
                Map<String, Object> node = new HashMap<>();
                node.put("id", row.get("rawId").toString());
                node.put("labels", row.get("labels"));
                node.put("properties", row.get("props"));
                nodes.add(node);
            });

            // Get all relationships
            neo4jClient.query(
                    "MATCH (n)-[r]->(m) RETURN id(r) as relId, type(r) as type, id(n) as source, id(m) as target"
            )
            .fetch().all().forEach(row -> {
                Map<String, Object> edge = new HashMap<>();
                edge.put("id", row.get("relId").toString());
                edge.put("type", row.get("type"));
                edge.put("source", row.get("source").toString());
                edge.put("target", row.get("target").toString());
                edges.add(edge);
            });

        } catch (Exception e) {
            log.error("Failed to get graph data from Neo4j: {}", e.getMessage());
        }

        graph.put("nodes", nodes);
        graph.put("edges", edges);
        return graph;
    }
}
