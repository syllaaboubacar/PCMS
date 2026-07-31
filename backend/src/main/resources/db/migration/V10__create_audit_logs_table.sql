CREATE TABLE audit_logs (

    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id BIGINT NOT NULL,

    action VARCHAR(100) NOT NULL,

    entity_name VARCHAR(100) NOT NULL,

    entity_id BIGINT NOT NULL,

    details TEXT,

    ip_address VARCHAR(45),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    created_by VARCHAR(100),

    CONSTRAINT fk_audit_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT

);
