CREATE TABLE cases (

    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    case_number VARCHAR(30) NOT NULL,

    title VARCHAR(255) NOT NULL,

    description TEXT NOT NULL,

    status VARCHAR(30) NOT NULL,

    priority VARCHAR(20) NOT NULL,

    opened_at TIMESTAMPTZ NOT NULL,

    closed_at TIMESTAMPTZ,

    incident_date DATE,

    location VARCHAR(255),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),

    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(100),

    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_cases_number UNIQUE(case_number),

    CONSTRAINT chk_case_status
        CHECK (
            status IN (
                'OPEN',
                'IN_PROGRESS',
                'ON_HOLD',
                'CLOSED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT chk_case_priority
        CHECK (
            priority IN (
                'LOW',
                'MEDIUM',
                'HIGH',
                'CRITICAL'
            )
        )

);
