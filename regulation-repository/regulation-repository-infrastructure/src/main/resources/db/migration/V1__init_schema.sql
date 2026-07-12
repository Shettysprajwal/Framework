-- Create regulations table
CREATE TABLE regulations (
    id UUID PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    short_name VARCHAR(50) NOT NULL UNIQUE,
    primary_jurisdiction VARCHAR(20) NOT NULL,
    version VARCHAR(50) NOT NULL,
    effective_date DATE,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    formal_spec TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Create articles table
CREATE TABLE articles (
    id UUID PRIMARY KEY,
    regulation_id UUID NOT NULL REFERENCES regulations(id) ON DELETE CASCADE,
    article_number VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    deontic_formula TEXT,
    odrl_policy TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT unique_regulation_article UNIQUE(regulation_id, article_number)
);

-- Create clauses table
CREATE TABLE clauses (
    id UUID PRIMARY KEY,
    article_id UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    clause_number VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    clause_type VARCHAR(50) NOT NULL,
    CONSTRAINT unique_article_clause UNIQUE(article_id, clause_number)
);

-- Indexes for performance optimization
CREATE INDEX idx_regulations_jurisdiction ON regulations(primary_jurisdiction);
CREATE INDEX idx_regulations_status ON regulations(status);
CREATE INDEX idx_articles_regulation_id ON articles(regulation_id);
CREATE INDEX idx_clauses_article_id ON clauses(article_id);
