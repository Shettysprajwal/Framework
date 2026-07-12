package com.pqvcf.regulation.infrastructure.listener;

import com.pqvcf.regulation.domain.event.ArticleAddedEvent;
import com.pqvcf.regulation.domain.event.RegulationRegisteredEvent;
import com.pqvcf.regulation.domain.model.Article;
import com.pqvcf.regulation.domain.model.Regulation;
import com.pqvcf.regulation.domain.model.RegulationId;
import com.pqvcf.regulation.domain.repository.JurisdictionGraphRepository;
import com.pqvcf.regulation.domain.repository.RegulationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class RegulationGraphSyncListener {

    private static final Logger log = LoggerFactory.getLogger(RegulationGraphSyncListener.class);

    private final JurisdictionGraphRepository graphRepository;
    private final RegulationRepository regulationRepository;

    public RegulationGraphSyncListener(
            JurisdictionGraphRepository graphRepository,
            RegulationRepository regulationRepository) {
        this.graphRepository = graphRepository;
        this.regulationRepository = regulationRepository;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRegulationRegistered(RegulationRegisteredEvent event) {
        log.info("Received RegulationRegisteredEvent, syncing to graph: {}", event.shortName());
        graphRepository.addJurisdiction(event.jurisdiction());
        graphRepository.syncRegulation(
                event.regulationId().toString(),
                event.shortName(),
                event.jurisdiction()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleArticleAdded(ArticleAddedEvent event) {
        log.info("Received ArticleAddedEvent, syncing article to graph: {}", event.articleNumber());
        
        // Fetch full article details from regulation repository
        regulationRepository.findById(event.regulationId()).ifPresent(regulation -> {
            try {
                Article article = regulation.findArticle(event.articleNumber());
                graphRepository.syncArticle(
                        article.getId().toString(),
                        regulation.getId().toString(),
                        article.getArticleNumber(),
                        article.getTitle()
                );
                
                // Sync any clauses already added to the article
                article.getClauses().forEach(clause -> {
                    graphRepository.syncClause(
                            clause.getId().toString(),
                            article.getId().toString(),
                            clause.getClauseNumber(),
                            clause.getClauseType().name()
                    );
                });
            } catch (Exception e) {
                log.error("Failed to sync article to graph: {}", e.getMessage());
            }
        });
    }
}
