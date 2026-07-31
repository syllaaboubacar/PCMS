# 03.06 - Flyway Introduction

---

| **Document**     | 03.06-Flyway-Introduction.md         |
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
2. What is Flyway?
3. Why Use Flyway?
4. How Flyway Works
5. Migration Directory Structure
6. Migration Naming Convention
7. The `flyway_schema_history` Table
8. First Migration
9. Flyway Execution Lifecycle
10. Migration Best Practices
11. Next Step
12. Revision History

---

# 1. Purpose

This document introduces **Flyway**, the database migration tool adopted by the Police Case Management System (PCMS).

Its purpose is to explain how database schema changes are versioned, executed, and tracked throughout the application's lifecycle.

Flyway ensures that every environment—development, testing, staging, and production—uses exactly the same database structure.

---

# 2. What is Flyway?

Flyway is an open-source database migration tool that manages the evolution of relational database schemas through **versioned SQL scripts**.

Instead of manually modifying a database, every structural change is stored in a migration file committed to the Git repository.

Each migration represents a single evolution of the database.

Examples:

```text
V1__init_database.sql
V2__create_users_table.sql
V3__create_departments_table.sql
V4__create_cases_table.sql
```

When the application starts, Flyway automatically executes any migrations that have not yet been applied.

---

# 3. Why Use Flyway?

Without a migration tool, database changes are often applied manually.

This approach leads to inconsistent environments, deployment issues, and difficult troubleshooting.

Flyway provides a controlled and repeatable process by:

* versioning every database change;
* executing migrations automatically;
* ensuring all environments share the same schema;
* integrating seamlessly with Spring Boot;
* supporting collaborative development.

For enterprise applications such as PCMS, this approach is essential.

---

# 4. How Flyway Works

At application startup, Flyway executes the following workflow:

```text
Spring Boot Application
          │
          ▼
      Start Flyway
          │
          ▼
Search migration scripts
          │
          ▼
Read migration history
          │
          ▼
Execute pending migrations
          │
          ▼
Update migration history
          │
          ▼
Start Hibernate validation
          │
          ▼
Application Ready
```

Only migrations that have never been executed are applied.

Previously executed migrations are skipped automatically.

---

# 5. Migration Directory Structure

By convention, Spring Boot automatically detects Flyway migrations stored in the following directory:

```text
backend/
└── src/
    └── main/
        └── resources/
            └── db/
                └── migration/
                    ├── V1__init_database.sql
                    ├── V2__create_users_table.sql
                    ├── V3__create_departments_table.sql
                    └── ...
```

No additional configuration is required when using this standard location.

---

# 6. Migration Naming Convention

Flyway follows a strict naming convention for versioned migrations.

```
V<version>__<description>.sql
```

Examples:

```text
V1__init_database.sql
V2__reference_data.sql
V3__create_indexes.sql
V4__create_cases_table.sql
```

### Naming Rules

* The filename must begin with **`V`**.
* A version number is mandatory.
* Two underscores (`__`) separate the version and description.
* Spaces are not allowed.
* Descriptions should clearly identify the purpose of the migration.

Consistent naming greatly improves project maintainability.

---

# 7. The `flyway_schema_history` Table

When Flyway executes its first migration, it automatically creates a metadata table named:

```text
flyway_schema_history
```

This table records every executed migration.

Typical information includes:

| Column       | Description           |
| ------------ | --------------------- |
| Version      | Migration version     |
| Description  | Migration description |
| Script       | Executed SQL file     |
| Installed On | Execution timestamp   |
| Success      | Execution status      |

Example:

| Version | Description        | Success |
| ------- | ------------------ | ------- |
| 1       | init_database      | ✅       |
| 2       | create_users_table | ✅       |
| 3       | create_indexes     | ✅       |

This table allows Flyway to determine which migrations have already been executed.

As a result, migrations are never executed twice.

---

# 8. First Migration

The first migration in the PCMS project is intentionally minimal.

File:

```text
src/main/resources/db/migration/V1__init_database.sql
```

Content:

```sql
/*
 * ============================================================================
 * PCMS - Police Case Management System
 * ----------------------------------------------------------------------------
 * Version     : V1
 * Description : Database initialization
 * Author      : Aboubacar Sylla
 * ============================================================================
 */

-- Database initialization.
-- Business tables will be created in future migrations.
```

This migration does not create any tables.

Its objective is simply to verify that:

* Flyway detects migration files;
* the migration executes successfully;
* the migration history table is created automatically.

This incremental approach validates the migration mechanism before introducing the application schema.

---

# 9. Flyway Execution Lifecycle

The database evolves progressively through successive migrations.

```text
Developer
      │
      ▼
Create a new migration
      │
      ▼
Commit to Git
      │
      ▼
Deploy application
      │
      ▼
Spring Boot starts
      │
      ▼
Flyway executes new migrations
      │
      ▼
Hibernate validates the schema
      │
      ▼
Application becomes available
```

Each migration is executed exactly once.

Future database changes are introduced by creating new migration files rather than modifying existing ones.

---

# 10. Migration Best Practices

The PCMS project follows the following migration principles:

* Create one logical change per migration.
* Never modify a migration that has already been executed.
* Always create a new migration for every database evolution.
* Use explicit and meaningful filenames.
* Keep all migration scripts under version control.
* Test migrations on an empty database before deployment.
* Keep migrations deterministic and repeatable.
* Allow Flyway—not Hibernate—to manage schema evolution.
* Configure Hibernate with `ddl-auto: validate` to verify schema consistency.

These practices are widely adopted in enterprise software development.

---

# 11. Next Step

The next chapter introduces the first database structures of the PCMS application.

Upcoming topics include:

* creating business tables;
* defining primary and foreign keys;
* implementing constraints;
* preparing JPA entity mappings;
* validating the schema through Flyway migrations.

This marks the beginning of the physical implementation of the database.

---

# 12. Revision History

| Version | Date       | Author            | Description     |
| ------- | ---------- | ----------------- | --------------- |
| 1.0.0   | 31/07/2026 | PCMS Project Team | Initial version |

