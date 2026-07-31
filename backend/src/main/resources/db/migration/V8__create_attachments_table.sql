CREATE TABLE attachments (

    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    case_id BIGINT NOT NULL,

    filename VARCHAR(255) NOT NULL,

    original_filename VARCHAR(255) NOT NULL,

    mime_type VARCHAR(100) NOT NULL,

    file_size BIGINT NOT NULL,

    storage_path VARCHAR(500) NOT NULL,

    type VARCHAR(30) NOT NULL,

    uploaded_at TIMESTAMPTZ NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),

    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(100),

    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT chk_file_size
        CHECK(file_size >= 0),

    CONSTRAINT fk_attachment_case
        FOREIGN KEY(case_id)
        REFERENCES cases(id)
        ON DELETE RESTRICT

);
