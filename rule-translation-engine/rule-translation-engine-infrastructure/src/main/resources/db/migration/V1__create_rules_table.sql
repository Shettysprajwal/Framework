-- Create translated rules table
CREATE TABLE translated_rules (
    id UUID PRIMARY KEY,
    regulation_short_name VARCHAR(50) NOT NULL,
    article_number VARCHAR(50) NOT NULL,
    clause_number VARCHAR(50),
    raw_source_text TEXT NOT NULL,
    deontic_operator VARCHAR(20) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    target VARCHAR(255) NOT NULL,
    constraint_text VARCHAR(500),
    smt_spec TEXT NOT NULL,
    odrl_policy TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Indexes for rule querying
CREATE INDEX idx_rules_regulation_article ON translated_rules(regulation_short_name, article_number);
