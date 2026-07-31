CREATE TABLE case_assignments (

    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    case_id BIGINT NOT NULL,

    user_id BIGINT NOT NULL,

    assigned_at TIMESTAMPTZ NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),

    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(100),

    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_assignment
        UNIQUE(case_id, user_id),

    CONSTRAINT fk_assignment_case
        FOREIGN KEY(case_id)
        REFERENCES cases(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_assignment_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT

);
