CREATE TABLE users (

    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    first_name VARCHAR(100) NOT NULL,

    last_name VARCHAR(100) NOT NULL,

    email VARCHAR(255) NOT NULL,

    password VARCHAR(255) NOT NULL,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    role_id BIGINT NOT NULL,

    department_id BIGINT NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),

    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(100),

    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_users_email UNIQUE(email),

    CONSTRAINT fk_users_role
        FOREIGN KEY(role_id)
        REFERENCES roles(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_users_department
        FOREIGN KEY(department_id)
        REFERENCES departments(id)
        ON DELETE RESTRICT

);
