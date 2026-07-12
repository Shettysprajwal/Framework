package com.pqvcf.regulation.application.port.in;

import com.pqvcf.regulation.application.dto.ArticleResponse;
import java.util.List;

public interface AddArticleUseCase {
    ArticleResponse addArticle(AddArticleCommand command);

    record AddClauseCommand(
            String clauseNumber,
            String content,
            String clauseType
    ) {}

    record AddArticleCommand(
            String regulationId,
            String articleNumber,
            String title,
            String content,
            String deonticFormula,
            String odrlPolicy,
            List<AddClauseCommand> clauses
    ) {}
}
