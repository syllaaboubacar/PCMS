# Spring Data JPA Repositories – Introduction

**Version:** 1.0  
**Module:** 3 – Backend Development (Spring Boot)  
**Chapter:** 3.10.1 – Introduction to Spring Data JPA Repositories  
**Project:** Police Case Management System (PCMS)

---

# Purpose

This document introduces the Repository layer used in the Police Case Management System (PCMS).

It explains the role of **Spring Data JPA**, why repositories are essential in a layered architecture, and how they simplify database access by automatically providing CRUD operations.

The objective is to understand the Repository pattern before implementing the project's first repository.

---

# From Data Model to Data Access

During the previous chapter, the following JPA entities were created:

- Role
- Department
- User
- Case
- CaseAssignment
- Suspect
- Attachment
- CaseComment
- AuditLog

These entities describe the application's data model, but they do not provide a way to interact with the database.

Typical operations such as:

- creating a user;
- retrieving a case;
- updating a department;
- deleting a role;
- checking whether an email address already exists;

require a dedicated data access mechanism.

This responsibility belongs to the Repository layer.

---

# Before Spring Data JPA

Before Spring Data JPA became widely adopted, developers typically interacted with the database using the JPA `EntityManager`.

Example:

```java
@PersistenceContext
private EntityManager entityManager;

public User save(User user) {
    entityManager.persist(user);
    return user;
}

public User findById(Long id) {
    return entityManager.find(User.class, id);
}
```

Each entity required its own Data Access Object (DAO) containing methods such as:

- save()
- update()
- delete()
- findById()
- findAll()

Although functional, this approach resulted in a significant amount of repetitive code.

---

# The Problem

Imagine the PCMS project growing from:

- 9 entities today;
- 30 or more entities in the future.

Without Spring Data JPA, every entity would require a dedicated DAO implementation.

Example:

```text
RoleDao
DepartmentDao
UserDao
CaseFileDao
AttachmentDao
SuspectDao
...
```

Each DAO would implement nearly identical CRUD methods.

Most of this code would provide no business value while increasing maintenance costs.

---

# Spring Data JPA Solution

Spring Data JPA eliminates most of this boilerplate.

A repository can be declared with a single interface.

```java
public interface UserRepository
        extends JpaRepository<User, Long> {

}
```

No implementation is required.

At runtime, Spring automatically generates a fully functional repository.

Immediately available operations include:

- save()
- saveAll()
- findById()
- findAll()
- delete()
- deleteById()
- existsById()
- count()
- flush()
- paging
- sorting

Developers can therefore focus on business logic instead of persistence infrastructure.

---

# How Spring Data JPA Works

During application startup, Spring Boot scans the project for repository interfaces.

The process can be summarized as follows.

```text
Spring Boot
      │
      ▼
Scans Repository Interfaces
      │
      ▼
Creates Runtime Implementations
      │
      ▼
Registers Beans
      │
      ▼
Injects Repositories into Services
```

Although only an interface exists in the source code, Spring creates an implementation dynamically.

Conceptually:

```text
UserRepository
        │
        ▼
Generated Implementation
        │
        ▼
Hibernate
        │
        ▼
PostgreSQL
```

This implementation is generated automatically and should never be written manually.

---

# Repository Position in the Architecture

Repositories are responsible exclusively for data persistence.

Within the PCMS architecture, a typical request follows this path:

```text
HTTP Client
      │
      ▼
REST Controller
      │
      ▼
Service
      │
      ▼
Repository
      │
      ▼
Hibernate
      │
      ▼
PostgreSQL
```

Each layer has a clearly defined responsibility.

| Layer | Responsibility |
|--------|----------------|
| REST Controller | Handles HTTP requests and responses |
| Service | Implements business rules |
| Repository | Performs data access operations |
| Hibernate | Maps Java objects to SQL |
| PostgreSQL | Stores persistent data |

This separation of concerns improves maintainability and testability.

---

# Why Repositories Are Interfaces

Unlike services, repositories are usually declared as interfaces.

Example:

```java
public interface RoleRepository
        extends JpaRepository<Role, Long> {

}
```

Spring provides the implementation automatically.

This approach offers several advantages:

- less code to maintain;
- optimized persistence implementation;
- seamless Spring integration;
- easier unit testing;
- consistent programming model across all entities.

---

# Benefits of Spring Data JPA

Spring Data JPA provides numerous benefits for enterprise applications such as PCMS.

## Simplified Data Access

Most persistence operations are already implemented.

---

## Reduced Boilerplate

Developers no longer write repetitive CRUD code.

---

## Automatic Query Generation

Spring can generate queries directly from method names.

Example:

```java
findByEmail(String email)
```

No SQL or JPQL is required.

---

## Pagination Support

Large datasets can be retrieved efficiently.

```java
Page<User>
```

---

## Sorting Support

Repositories support dynamic sorting without additional SQL.

---

## Hibernate Integration

Repositories integrate seamlessly with Hibernate and the JPA persistence context.

---

## Maintainability

Business logic remains inside services instead of being mixed with persistence code.

---

# Repository Organization

PCMS follows a **Package by Feature** architecture.

Each business feature owns its repository.

```text
src/
└── main/
    └── java/
        └── lu/
            └── police/
                └── pcms/
                    ├── role/
                    │   └── repository/
                    │       └── RoleRepository.java
                    │
                    ├── department/
                    │   └── repository/
                    │       └── DepartmentRepository.java
                    │
                    ├── user/
                    │   └── repository/
                    │       └── UserRepository.java
                    │
                    ├── casefile/
                    │   └── repository/
                    │       └── CaseFileRepository.java
                    │
                    ├── caseassignment/
                    │   └── repository/
                    │       └── CaseAssignmentRepository.java
                    │
                    ├── suspect/
                    │   └── repository/
                    │       └── SuspectRepository.java
                    │
                    ├── attachment/
                    │   └── repository/
                    │       └── AttachmentRepository.java
                    │
                    ├── casecomment/
                    │   └── repository/
                    │       └── CaseCommentRepository.java
                    │
                    └── audit/
                        └── repository/
                            └── AuditLogRepository.java
```

Each repository belongs to the same feature package as its corresponding entity.

---

# Repository Responsibilities

Repositories are responsible only for persistence operations.

Typical responsibilities include:

- creating entities;
- updating entities;
- deleting entities;
- retrieving entities;
- executing database queries.

Repositories **must not** contain:

- business rules;
- authorization logic;
- validation logic;
- REST concerns.

Those responsibilities belong to the Service layer.

---

# Relationship Between Layers

The interaction between the main backend components is illustrated below.

```text
REST Controller
        │
        ▼
Service
        │
        ▼
Repository
        │
        ▼
Entity
        │
        ▼
Hibernate
        │
        ▼
PostgreSQL
```

Only the Service layer should communicate with repositories.

Controllers should never access repositories directly.

---

# Best Practices

The PCMS project follows these Repository best practices.

- One repository per aggregate/entity.
- Use interfaces instead of concrete implementations.
- Keep repositories focused on persistence.
- Place business logic in services.
- Prefer derived query methods whenever possible.
- Write custom queries only when necessary.
- Keep repositories inside their corresponding feature package.
- Let Spring generate implementations automatically.

---

# Key Takeaways

At the end of this chapter, the following concepts should be understood:

- A JPA entity represents a database table.
- A repository is the entry point for accessing persistent data.
- Spring Data JPA automatically generates repository implementations.
- Standard CRUD operations are available without writing SQL.
- Repositories belong to the persistence layer.
- Services are the only components that should interact with repositories.
- The Package by Feature architecture keeps repositories close to their business domain.

---

# Next Step

The next document explores the core interface used throughout the project:

**03.10.2-JpaRepository.md**

It explains:

- the generic declaration `JpaRepository<T, ID>`;
- inherited interfaces;
- built-in CRUD methods;
- pagination;
- sorting;
- counting;
- query capabilities.

This understanding will prepare the implementation of the project's first repository: **RoleRepository**.
