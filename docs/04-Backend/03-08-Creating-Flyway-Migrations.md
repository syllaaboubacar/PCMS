# 03.08 - Creating Flyway Migrations

---

| **Document**     | 03.08-Creating-Flyway-Migrations.md  |
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
2. From Design to Implementation
3. Migration Strategy
4. Migration Roadmap
5. Migration Directory Structure
6. Migration Naming Convention
7. Migration Execution Order
8. Migration Content Guidelines
9. Migration Workflow
10. Best Practices
11. Next Step
12. Revision History

---

# 1. Purpose

This document explains how the database design produced during the **Database Design Module** is implemented through versioned Flyway SQL migrations.

The objective is to transform the approved conceptual, logical and physical database models into an executable PostgreSQL schema while maintaining complete version control.

---

# 2. From Design to Implementation

The database has already been fully designed.

The following documents constitute the reference architecture:

| Document                          | Status      |
| --------------------------------- | ----------- |
| Business Domain Model             | ✅ Completed |
| Conceptual Data Model (CDM)       | ✅ Completed |
| Logical Data Model (LDM)          | ✅ Completed |
| Entity Relationship Diagram (ERD) | ✅ Completed |
| PostgreSQL Schema                 | ✅ Completed |
| PostgreSQL Types                  | ✅ Completed |
| Constraints & Indexes             | ✅ Completed |
| Flyway Strategy                   | ✅ Completed |

Backend development now focuses exclusively on implementing this validated design.

No structural decisions should be introduced during migration development.

---

# 3. Migration Strategy

PCMS adopts a fully versioned migration strategy.

Every structural modification of the database is introduced through a dedicated SQL migration.

The schema evolves incrementally.

```text
Database Design
        │
        ▼
Flyway Migration
        │
        ▼
Git Repository
        │
        ▼
Application Startup
        │
        ▼
PostgreSQL Schema Updated
```

Each migration represents a single functional evolution.

---

# 4. Migration Roadmap

The database schema will be implemented progressively.

| Version | Purpose                                                                                  |
| ------- | ---------------------------------------------------------------------------------------- |
| V1      | Database initialization                                                                  |
| V2      | Reference tables (roles, departments)                                                    |
| V3      | Business tables (users, cases, assignments, suspects, attachments, comments, audit logs) |
| V4      | Constraints and foreign keys                                                             |
| V5      | Performance indexes                                                                      |
| V6      | Reference data                                                                           |
| V7      | Initial administrator                                                                    |
| V8      | Sample data (optional)                                                                   |

The exact number of migrations may evolve during the project, but each migration must remain focused on a single responsibility.

---

# 5. Migration Directory Structure

All migrations are stored in the standard Flyway directory.

```text
backend/
└── src/
    └── main/
        └── resources/
            └── db/
                └── migration/
                    ├── V1__init_database.sql
                    ├── V2__create_reference_tables.sql
                    ├── V3__create_business_tables.sql
                    ├── V4__add_constraints.sql
                    ├── V5__create_indexes.sql
                    ├── V6__reference_data.sql
                    ├── V7__initial_admin.sql
                    └── V8__sample_data.sql
```

This location is detected automatically by Spring Boot and Flyway.

---

# 6. Migration Naming Convention

Every migration must follow the Flyway naming convention.

```text
V<version>__<description>.sql
```

Examples:

```text
V1__init_database.sql
V2__create_reference_tables.sql
V3__create_business_tables.sql
V4__add_constraints.sql
```

Rules:

* Start with `V`.
* Use sequential version numbers.
* Separate version and description with two underscores (`__`).
* Use lowercase words separated by underscores.
* Choose explicit, business-oriented names.

---

# 7. Migration Execution Order

Flyway executes migrations in ascending version order.

```text
V1
 │
 ▼
V2
 │
 ▼
V3
 │
 ▼
V4
 │
 ▼
V5
 │
 ▼
...
```

Each migration is executed only once.

Executed migrations are recorded in the `flyway_schema_history` table.

---

# 8. Migration Content Guidelines

Each migration should contain a single logical change.

Typical responsibilities include:

* creating tables;
* adding constraints;
* creating indexes;
* inserting reference data;
* introducing new columns;
* renaming objects;
* updating data structures.

A migration should never mix unrelated changes.

Keeping migrations small and focused simplifies testing, debugging and maintenance.

---

# 9. Migration Workflow

Every database evolution follows the same lifecycle.

```text
Requirement
      │
      ▼
Database Design
      │
      ▼
Create New Flyway Migration
      │
      ▼
Commit to Git
      │
      ▼
Deploy Application
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

This process guarantees reproducible deployments across development, testing and production environments.

---

# 10. Best Practices

The PCMS project follows these migration principles:

* Design before implementation.
* One migration for one logical evolution.
* Never modify an executed migration.
* Always create a new migration for every schema change.
* Keep migrations under version control.
* Test migrations on a clean database.
* Let Flyway manage schema evolution.
* Configure Hibernate with `ddl-auto: validate`.
* Keep SQL scripts readable and well documented.

Following these practices ensures long-term maintainability and deployment safety.

---

# 11. Next Step

The next chapter introduces the implementation of the JPA persistence model.

Topics include:

* creating the `BaseEntity`;
* implementing entity classes;
* mapping relationships;
* using Jakarta Persistence annotations;
* preparing Spring Data repositories.

The entities will be mapped directly from the database design already documented during the Database module.

---

# 12. Revision History

| Version | Date       | Author            | Description     |
| ------- | ---------- | ----------------- | --------------- |
| 1.0.0   | 31/07/2026 | PCMS Project Team | Initial version |

