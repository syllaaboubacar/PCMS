# 03.07 - Flyway Migration Strategy

---

| **Document**     | 03.07-Flyway-Migration-Strategy.md   |
| ---------------- | ------------------------------------ |
| **Module**       | Module 3 – Backend Development       |
| **Project**      | Police Case Management System (PCMS) |
| **Version**      | 1.0.0                                |
| **Status**       | Draft                                |
| **Author**       | PCMS Project Team                    |
| **Last Updated** | 31/07/2026                           |

---

# Table of Contents

1. Purpose
2. Position in the Project Lifecycle
3. Design Before Implementation
4. Migration Strategy
5. Flyway Responsibilities
6. Hibernate Responsibilities
7. Migration Roadmap
8. Migration Workflow
9. Best Practices
10. Next Step
11. Revision History

---

# 1. Purpose

This document defines the migration strategy adopted by the Police Case Management System (PCMS).

It explains how the database design produced during the **Database Design Module** is transformed into a physical PostgreSQL schema through **Flyway** versioned migrations.

Rather than redesigning the database, this phase focuses on implementing an already validated architecture.

---

# 2. Position in the Project Lifecycle

The PCMS project follows a structured development lifecycle.

```text
Business Analysis
        │
        ▼
Software Architecture
        │
        ▼
Database Design
        │
        ▼
Backend Development
        │
        ▼
Frontend Development
        │
        ▼
Deployment
```

The complete database design has already been finalized before backend implementation begins.

This approach reflects standard practices used in enterprise software development.

---

# 3. Design Before Implementation

The following design artifacts have already been completed and approved.

| Design Artifact                   | Status      |
| --------------------------------- | ----------- |
| Business Domain Model             | ✅ Completed |
| Conceptual Data Model (CDM)       | ✅ Completed |
| Logical Data Model (LDM)          | ✅ Completed |
| Entity Relationship Diagram (ERD) | ✅ Completed |
| PostgreSQL Schema                 | ✅ Completed |
| PostgreSQL Data Types             | ✅ Completed |
| Constraints                       | ✅ Completed |
| Index Strategy                    | ✅ Completed |
| Flyway Strategy                   | ✅ Completed |

These documents constitute the official reference for every database implementation.

Backend development must remain consistent with these specifications.

---

# 4. Migration Strategy

Database evolution is managed exclusively through **Flyway**.

Each structural change is introduced by a new versioned SQL migration.

Typical migration sequence:

```text
V1__init_database.sql
        │
        ▼
V2__create_reference_tables.sql
        │
        ▼
V3__create_business_tables.sql
        │
        ▼
V4__create_indexes.sql
        │
        ▼
V5__insert_reference_data.sql
```

Each migration represents a single functional evolution of the database schema.

---

# 5. Flyway Responsibilities

Flyway is responsible for:

* creating database objects;
* evolving the schema;
* executing SQL migrations in version order;
* recording migration history;
* ensuring identical schemas across all environments;
* supporting automated deployments.

Flyway is the single source of truth for schema evolution.

---

# 6. Hibernate Responsibilities

Hibernate does **not** create or modify the database schema.

Its responsibilities are limited to:

* validating entity mappings;
* checking table existence;
* verifying column definitions;
* detecting inconsistencies during application startup.

The application uses the following configuration:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

This separation of responsibilities ensures a predictable and maintainable persistence layer.

---

# 7. Migration Roadmap

The next implementation phases will follow this order.

| Step  | Description                                                      |
| ----- | ---------------------------------------------------------------- |
| 03.08 | Implement the physical PostgreSQL schema using Flyway migrations |
| 03.09 | Implement JPA entities                                           |
| 03.10 | Create Spring Data repositories                                  |
| 03.11 | Implement the service layer                                      |
| 03.12 | Expose REST controllers                                          |
| 03.13 | Secure the REST API                                              |
| 03.14 | Validate the backend through integration testing                 |

Each implementation step relies on the design decisions documented during the Database module.

---

# 8. Migration Workflow

Every database evolution follows the same lifecycle.

```text
Database Design
        │
        ▼
Create Flyway Migration
        │
        ▼
Commit to Git
        │
        ▼
Application Startup
        │
        ▼
Flyway Executes Migration
        │
        ▼
Hibernate Validates Schema
        │
        ▼
Application Ready
```

This workflow guarantees consistent deployments and complete traceability.

---

# 9. Best Practices

The PCMS project follows these migration principles:

* Design before implementation.
* One functional change per migration.
* Never modify an executed migration.
* Always create a new migration for every schema evolution.
* Keep migrations under version control.
* Let Flyway manage the schema.
* Configure Hibernate with `ddl-auto: validate`.
* Test migrations on a clean database before deployment.

These practices align with professional Spring Boot and PostgreSQL projects.

---

# 10. Next Step

The next chapter focuses on the physical implementation of the PostgreSQL schema.

Using the database design produced during the Database module, versioned Flyway migrations will be created to:

* implement reference tables;
* implement business tables;
* define constraints and indexes;
* prepare the persistence layer for JPA entity mapping.

No additional database design work is required, as the conceptual and logical models have already been completed.

---

# 11. Revision History

| Version | Date       | Author            | Description     |
| ------- | ---------- | ----------------- | --------------- |
| 1.0.0   | 31/07/2026 | PCMS Project Team | Initial version |

