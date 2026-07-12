-- Create decision audit logs table
CREATE TABLE decision_audit_logs (
    id UUID PRIMARY KEY,
    subject_id VARCHAR(255) NOT NULL,
    resource_id VARCHAR(255) NOT NULL,
    action_id VARCHAR(255) NOT NULL,
    source_country VARCHAR(50),
    target_country VARCHAR(50),
    policy_name VARCHAR(255),
    effect VARCHAR(50) NOT NULL,
    proof_trace TEXT,
    validation_log TEXT,
    solved_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Index for log querying
CREATE INDEX idx_audit_logs_solved_at ON decision_audit_logs(solved_at DESC);
CREATE INDEX idx_audit_logs_subject_resource ON decision_audit_logs(subject_id, resource_id);
