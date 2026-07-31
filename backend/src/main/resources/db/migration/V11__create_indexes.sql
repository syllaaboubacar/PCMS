CREATE UNIQUE INDEX idx_users_email
ON users(email);

CREATE UNIQUE INDEX idx_cases_number
ON cases(case_number);

CREATE INDEX idx_cases_status
ON cases(status);

CREATE INDEX idx_cases_priority
ON cases(priority);

CREATE INDEX idx_case_assignments_case
ON case_assignments(case_id);

CREATE INDEX idx_case_assignments_user
ON case_assignments(user_id);

CREATE INDEX idx_comments_case
ON case_comments(case_id);

CREATE INDEX idx_attachments_case
ON attachments(case_id);

CREATE INDEX idx_audit_user
ON audit_logs(user_id);
