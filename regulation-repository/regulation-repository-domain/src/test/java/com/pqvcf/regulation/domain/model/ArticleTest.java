package com.pqvcf.regulation.domain.model;

import com.pqvcf.shared.types.JurisdictionCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArticleTest {

    @Test
    @DisplayName("Should successfully append articles and clauses to a regulation aggregate")
    void shouldAppendArticlesAndClauses() {
        Regulation regulation = Regulation.create(
                "General Data Protection Regulation",
                "GDPR",
                JurisdictionCode.EU,
                "2016/679",
                "EU Privacy Framework"
        );

        Article article = regulation.addArticle("Article 46", "Appropriate Safeguards", "In the absence of adequacy decision...");
        
        assertThat(article).isNotNull();
        assertThat(article.getArticleNumber()).isEqualTo("Article 46");
        assertThat(regulation.getArticles()).hasSize(1);

        Clause clause = article.addClause("46(1)", "A controller or processor may transfer personal data...", ClauseType.PERMISSION);
        
        assertThat(clause).isNotNull();
        assertThat(clause.getClauseNumber()).isEqualTo("46(1)");
        assertThat(clause.getClauseType()).isEqualTo(ClauseType.PERMISSION);
        assertThat(article.getClauses()).hasSize(1);
    }

    @Test
    @DisplayName("Should prevent duplicate article numbers in a regulation aggregate")
    void shouldPreventDuplicateArticles() {
        Regulation regulation = Regulation.create(
                "General Data Protection Regulation",
                "GDPR",
                JurisdictionCode.EU,
                "2016/679",
                "EU Privacy Framework"
        );

        regulation.addArticle("Article 44", "General principle", "Text");

        assertThatThrownBy(() -> regulation.addArticle("Article 44", "Duplicate Article", "Text"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists in regulation");
    }
}
