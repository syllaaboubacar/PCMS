# 03.05 - JPA & Hibernate Configuration

---

| **Document**     | 03.05-JPA-Hibernate-Configuration.md |
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
2. What is JPA?
3. What is Hibernate?
4. JPA vs Hibernate
5. JPA Configuration
6. Understanding `ddl-auto`
7. Why `validate`?
8. SQL Logging
9. SQL Formatting
10. Open Session in View
11. Current Configuration
12. Best Practices
13. Next Step
14. Revision History

---

# 1. Purpose

This document explains how **JPA** and **Hibernate** are configured in the Police Case Management System (PCMS).

At the end of this configuration:

* Spring Boot is connected to PostgreSQL.
* Hibernate validates the database schema.
* SQL queries can be displayed during development.
* SQL output is formatted for readability.
* The persistence layer is prepared for Flyway-managed database migrations.

---

# 2. What is JPA?

**Jakarta Persistence API (JPA)** is the Java specification used to map Java objects to relational database tables.

Instead of writing SQL directly:

```sql
SELECT *
FROM users;
```

Developers interact with repositories:

```java
userRepository.findAll();
```

The repository delegates the work to the JPA provider, which translates object operations into SQL statements.

JPA defines **what** a persistence framework should provide but does not implement it.

---

# 3. What is Hibernate?

Hibernate is the default JPA implementation used by Spring Boot.

It is an **Object-Relational Mapping (ORM)** framework responsible for converting Java entities into SQL operations.

```text
Java Entity
      │
      ▼
 Hibernate
      │
      ▼
 PostgreSQL
```

Hibernate automatically manages:

* entity persistence;
* object retrieval;
* updates;
* deletes;
* SQL generation;
* relationship mapping.

---

# 4. JPA vs Hibernate

| JPA                      | Hibernate           |
| ------------------------ | ------------------- |
| Specification            | Implementation      |
| Defines persistence APIs | Implements JPA APIs |
| Vendor independent       | Hibernate specific  |
| Standardized             | Feature-rich ORM    |

In the PCMS project:

* **JPA** provides the programming model.
* **Hibernate** provides the implementation.

---

# 5. JPA Configuration

The following configuration is added to `application.yml`:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate

    show-sql: true

    open-in-view: false

    properties:
      hibernate:
        format_sql: true
```

Each property has a specific responsibility.

---

# 6. Understanding `ddl-auto`

The `ddl-auto` property determines how Hibernate interacts with the database schema at application startup.

## Available values

| Value         | Description                                            | Production    |
| ------------- | ------------------------------------------------------ | ------------- |
| `none`        | No validation or schema generation                     | Rare          |
| `create`      | Drops and recreates the schema at startup              | ❌             |
| `create-drop` | Creates the schema at startup and drops it at shutdown | Testing only  |
| `update`      | Automatically updates the schema                       | ❌             |
| `validate`    | Verifies that the schema matches the entities          | ✅ Recommended |

---

## Why not `update`?

Although `update` appears convenient, it introduces several risks:

* schema changes are not versioned;
* environments may become inconsistent;
* changes cannot be reviewed before deployment;
* production databases become difficult to maintain.

Professional projects avoid automatic schema modifications.

---

# 7. Why `validate`?

PCMS adopts the following architecture:

```text
Flyway
      │
      ▼
Creates or updates the database schema
      │
      ▼
Hibernate
      │
      ▼
Validates entity mappings
```

Hibernate never creates or modifies database objects.

Its only responsibility is to verify that:

* every required table exists;
* columns match entity mappings;
* relationships are consistent.

If the schema is incorrect, application startup fails immediately.

This behavior prevents runtime errors and guarantees database consistency.

---

# 8. SQL Logging

```yaml
show-sql: true
```

When enabled, Hibernate displays generated SQL statements in the application logs.

Example:

```sql
select
    *
from
    users;
```

## Benefits

* easier debugging;
* repository verification;
* query analysis;
* learning Hibernate behavior.

This option is useful during development but should normally be disabled in production.

---

# 9. SQL Formatting

```yaml
properties:
  hibernate:
    format_sql: true
```

Without formatting:

```sql
select id,name,email from users where enabled=true
```

With formatting enabled:

```sql
select
    id,
    name,
    email
from
    users
where
    enabled = true;
```

Formatted SQL is significantly easier to read during debugging.

---

# 10. Open Session in View

```yaml
open-in-view: false
```

Spring Boot enables **Open Session in View (OSIV)** by default.

OSIV keeps the Hibernate session open until the HTTP response has been fully generated.

Although convenient, it may:

* trigger unexpected database queries;
* hide poor application design;
* introduce unnecessary database access during serialization;
* reduce performance.

For REST applications following a layered architecture, disabling OSIV is considered best practice.

PCMS therefore explicitly sets:

```yaml
open-in-view: false
```

---

# 11. Current Configuration

The current `application.yml` contains the following configuration:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/pcms
    username: pcms_app
    password: PcmsDev2026!
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate

    show-sql: true

    open-in-view: false

    properties:
      hibernate:
        format_sql: true
```

At this stage:

* PostgreSQL connectivity is configured.
* Hibernate validates the schema.
* SQL statements are displayed.
* SQL formatting is enabled.
* OSIV is disabled.

Flyway configuration will be added in the next chapter.

---

# 12. Best Practices

The PCMS project follows these persistence configuration principles:

* Use JPA as the standard persistence API.
* Use Hibernate as the JPA implementation.
* Manage database evolution exclusively with Flyway.
* Set `ddl-auto` to `validate`.
* Never use `update` in production.
* Enable SQL logging only in development.
* Disable Open Session in View.
* Keep persistence configuration simple and explicit.

These practices align with enterprise-grade Spring Boot applications.

---

# 13. Next Step

The next chapter introduces **Flyway**.

Topics include:

* versioned database migrations;
* migration naming conventions;
* migration directory structure;
* first migration script;
* Flyway integration with Spring Boot;
* database version management.

Once Flyway is configured, Hibernate will validate a schema that is entirely managed through version-controlled SQL migrations.

---

# 14. Revision History

| Version | Date       | Author            | Description     |
| ------- | ---------- | ----------------- | --------------- |
| 1.0.0   | 31/07/2026 | PCMS Project Team | Initial version |

