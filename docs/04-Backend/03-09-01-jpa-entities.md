# JPA Entities – Introduction

**Version:** 1.0  
**Module:** 3 – Backend Development (Spring Boot)  
**Chapter:** 3.9.1 – Introduction to JPA Entities  
**Project:** Police Case Management System (PCMS)

---

# Purpose

This document introduces the Java Persistence API (JPA) entity model used by the Police Case Management System (PCMS).

Its purpose is to explain how the relational database designed during the **Database Design Module** is represented as Java objects within the Spring Boot application.

The database schema remains the single source of truth and is managed exclusively by **Flyway**. JPA entities simply map the existing database structure into Java classes, enabling developers to work with objects instead of raw SQL.

---

# Position in the Overall Architecture

The backend follows a layered architecture where each layer has a dedicated responsibility.

```text
Client
   │
   ▼
REST Controller
   │
   ▼
Service Layer
   │
   ▼
Repository
   │
   ▼
JPA Entity
   │
   ▼
Hibernate
   │
   ▼
PostgreSQL
```

Each layer depends only on the layer immediately below it.

---

# Why Use JPA?

Without JPA, interacting with the database would require writing SQL statements manually.

Example:

```sql
INSERT INTO users (
    first_name,
    last_name,
    email
)
VALUES (
    'John',
    'Doe',
    'john.doe@police.lu'
);
```

Using JPA, the same operation becomes object-oriented.

```java
User user = new User();

user.setFirstName("John");
user.setLastName("Doe");
user.setEmail("john.doe@police.lu");

userRepository.save(user);
```

Hibernate automatically generates the appropriate SQL statement.

This approach significantly reduces boilerplate code while improving readability and maintainability.

---

# JPA and Hibernate

Although often mentioned together, JPA and Hibernate are not the same thing.

## JPA

JPA (Jakarta Persistence API) is a Java specification.

It defines:

- entity mapping
- relationships
- persistence operations
- lifecycle management

JPA specifies **what** should be done, but not **how** it is implemented.

---

## Hibernate

Hibernate is the JPA implementation used by Spring Boot.

Its responsibilities include:

- mapping Java classes to database tables
- generating SQL statements
- managing entity persistence
- loading and updating objects
- handling relationships
- transaction synchronization

Architecture overview:

```text
Application
      │
      ▼
JPA Specification
      │
      ▼
Hibernate
      │
      ▼
PostgreSQL
```

---

# Mapping Between Database and Java

Each database object is represented by a Java class.

| PostgreSQL Table | Java Entity |
|------------------|------------|
| roles | Role |
| departments | Department |
| users | User |
| cases | Case |
| case_assignments | CaseAssignment |
| suspects | Suspect |
| attachments | Attachment |
| case_comments | CaseComment |
| audit_logs | AuditLog |

Table names remain plural.

Java class names remain singular.

This convention will be respected throughout the project.

---

# Entity Responsibilities

Each entity represents one business concept only.

Examples:

| Entity | Responsibility |
|----------|---------------|
| Role | Security role |
| Department | Police department |
| User | System user |
| Case | Investigation case |
| CaseAssignment | Investigator assignment |
| Suspect | Suspect involved in a case |
| Attachment | Investigation document |
| CaseComment | Investigation comment |
| AuditLog | Audit history |

This design follows the **Single Responsibility Principle (SRP)**.

---

# Entity Relationships

Relationships defined during database design are preserved by JPA.

```text
Role
 │
 └───────► User

Department
 │
 └───────► User

User
 │
 ├────────► CaseAssignment
 │
 ├────────► CaseComment
 │
 └────────► AuditLog

Case
 │
 ├────────► CaseAssignment
 ├────────► Suspect
 ├────────► Attachment
 └────────► CaseComment
```

These associations will be implemented using JPA annotations such as:

- `@OneToMany`
- `@ManyToOne`
- `@OneToOne`
- `@JoinColumn`

---

# Flyway Remains the Source of Truth

The database schema has already been designed and implemented through Flyway migrations.

Therefore:

```text
Flyway
      │
      ▼
PostgreSQL Schema
      │
      ▼
JPA Entities
      │
      ▼
Repositories
      │
      ▼
Services
      │
      ▼
REST Controllers
```

The entity model must always reflect the existing database schema.

Entities must **never** modify the schema automatically.

---

# Hibernate Validation

The project intentionally uses:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

This configuration ensures that:

- Flyway creates the database schema.
- Hibernate validates entity mappings.
- Hibernate never creates or updates database objects.

This is considered a production-ready approach.

---

# Shared Audit Information

Most entities contain common audit information:

- createdAt
- createdBy
- updatedAt
- updatedBy
- deleted

Instead of duplicating these fields across every entity, the project introduces a common superclass.

```text
BaseEntity
     ▲
     │
 ┌───┴─────────────────────────────┐
 │                                 │
Role                      Department
User                      Case
Attachment                Suspect
CaseComment               AuditLog
CaseAssignment
```

This design:

- eliminates duplicated code
- standardizes audit information
- simplifies maintenance
- improves consistency

The implementation of `BaseEntity` is covered in the next chapter.

---

# Entity Creation Order

Entities will be implemented following the same logical order used throughout the project.

| Order | Entity |
|--------|--------|
| 1 | BaseEntity |
| 2 | Role |
| 3 | Department |
| 4 | User |
| 5 | Case |
| 6 | CaseAssignment |
| 7 | Suspect |
| 8 | Attachment |
| 9 | CaseComment |
| 10 | AuditLog |

This sequence guarantees that dependencies are introduced progressively.

---

# Best Practices

The PCMS project follows the following JPA best practices:

- One entity per database table.
- One entity per business concept.
- Use singular class names.
- Keep business logic out of entities.
- Let Flyway manage the schema.
- Use Hibernate only for validation.
- Avoid duplicated audit fields through inheritance.
- Respect the ERD and Logical Data Model.
- Keep entities focused on persistence.

---

# Summary

At this stage of the project:

- The database schema is fully designed.
- Flyway manages schema creation and evolution.
- Hibernate validates the mappings.
- JPA entities provide the object representation of the database.
- Each entity corresponds directly to one PostgreSQL table.
- Common audit information will be centralized in `BaseEntity`.
- Entity implementation will follow the database design established during Module 2.

---

# Next Step

The next document introduces the common superclass shared by all persistent entities:

**03.09-02-BaseEntity.md**

It will define the audit model used consistently throughout the Police Case Management System.
