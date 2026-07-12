-- Create policies table
CREATE TABLE policies (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    owner VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Create policy rule links mapping table
CREATE TABLE policy_rule_links (
    id UUID PRIMARY KEY,
    policy_id UUID NOT NULL REFERENCES policies(id) ON DELETE CASCADE,
    organizational_rule_name VARCHAR(255) NOT NULL,
    regulatory_rule_id UUID NOT NULL,
    description TEXT,
    CONSTRAINT unique_policy_rule UNIQUE(policy_id, organizational_rule_name)
);

-- Indexes for performance
CREATE INDEX idx_policies_status ON policies(status);
CREATE INDEX idx_policy_rule_links_policy ON policy_rule_links(policy_id);
