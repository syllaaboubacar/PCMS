# JpaRepository in Spring Data JPA

## Purpose

This document explains the role of `JpaRepository` in the Police Case Management System (PCMS), how it fits into the application architecture, and why it is the preferred abstraction for data access in Spring Boot applications.

---

# What is JpaRepository?

`JpaRepository` is a Spring Data JPA interface that provides a complete set of CRUD (Create, Read, Update, Delete) operations for JPA entities.

Instead of writing SQL statements or manually implementing DAO classes, developers only need to declare an interface extending `JpaRepository`.

Example:

```java
public interface UserRepository extends JpaRepository<User, Long> {
}
```

Spring Boot automatically generates the implementation at runtime.

---

# Architecture Overview

```
REST Controller
        │
        ▼
Service Layer
        │
        ▼
Repository (JpaRepository)
        │
        ▼
Hibernate (JPA Provider)
        │
        ▼
PostgreSQL
```

Each layer has a single responsibility:

| Layer | Responsibility |
|--------|----------------|
| Controller | Handle HTTP requests |
| Service | Business logic |
| Repository | Data access |
| Hibernate | Object/Relational Mapping |
| PostgreSQL | Data persistence |

---

# Understanding JpaRepository<T, ID>

The declaration uses Java Generics.

```java
JpaRepository<T, ID>
```

## Generic Parameters

| Parameter | Description |
|-----------|-------------|
| `T` | Entity class |
| `ID` | Primary key type |

Example:

```java
public interface RoleRepository
        extends JpaRepository<Role, Long> {
}
```

Meaning:

- Entity = `Role`
- Primary key = `Long`

Another example:

```java
public interface UserRepository
        extends JpaRepository<User, Long> {
}
```

---

# JpaRepository Inheritance

`JpaRepository` extends several Spring Data interfaces.

```
Repository
      │
      ▼
CrudRepository
      │
      ▼
PagingAndSortingRepository
      │
      ▼
ListCrudRepository
      │
      ▼
JpaRepository
```

Each level adds new capabilities.

---

# CRUD Operations

The following methods are immediately available.

## Save

```java
roleRepository.save(role);
```

Creates or updates an entity.

---

## Find by ID

```java
Optional<Role> role = roleRepository.findById(id);
```

Returns an `Optional`.

---

## Find All

```java
List<Role> roles = roleRepository.findAll();
```

Retrieves every record.

---

## Delete by ID

```java
roleRepository.deleteById(id);
```

Deletes an entity using its identifier.

---

## Delete Entity

```java
roleRepository.delete(role);
```

Deletes the specified entity.

---

## Exists

```java
boolean exists = roleRepository.existsById(id);
```

Checks whether an entity exists.

---

## Count

```java
long total = roleRepository.count();
```

Returns the number of rows.

---

# Pagination

Spring Data JPA provides built-in pagination.

Example:

```java
Page<User> users =
        userRepository.findAll(PageRequest.of(0, 20));
```

Meaning:

- Page 1
- 20 records

This avoids loading an entire table into memory.

---

# Sorting

Sorting is also built in.

Example:

```java
List<Role> roles =
        roleRepository.findAll(
                Sort.by("name")
        );
```

Descending order:

```java
Sort.by("createdAt").descending()
```

Multiple criteria:

```java
Sort.by("department")
    .ascending()
    .and(
        Sort.by("lastName")
    );
```

---

# Pagination with Sorting

The two mechanisms can be combined.

Example:

```java
PageRequest.of(
        0,
        20,
        Sort.by("lastName")
);
```

---

# Query Methods

Spring Data JPA can generate SQL automatically from method names.

Example:

```java
Optional<User> findByEmail(String email);
```

Generated SQL is conceptually similar to:

```sql
SELECT *
FROM users
WHERE email = ?;
```

Another example:

```java
List<User> findByEnabledTrue();
```

Equivalent SQL:

```sql
SELECT *
FROM users
WHERE enabled = true;
```

More examples:

```java
findByLastName(String lastName)

findByDepartment(Department department)

findByRole(Role role)

findByStatus(CaseStatus status)

findByDeletedFalse()
```

No SQL implementation is required.

---

# Custom Queries

When method names become too complex, JPQL can be used.

Example:

```java
@Query("""
       SELECT u
       FROM User u
       WHERE u.enabled = true
       """)
List<User> findEnabledUsers();
```

Native SQL is also supported when necessary.

```java
@Query(
    value = """
            SELECT *
            FROM users
            WHERE enabled = true
            """,
    nativeQuery = true
)
List<User> findEnabledUsersNative();
```

Native queries should remain exceptional.

---

# Transaction Management

Repositories participate in Spring transaction management.

Read operations are typically executed within read-only transactions.

Write operations should generally be invoked from the Service layer using:

```java
@Transactional
```

Repositories themselves should remain focused on persistence operations.

---

# Repository Responsibilities

A repository should only:

- Persist entities.
- Retrieve entities.
- Delete entities.
- Execute database queries.

A repository must **not**:

- Contain business rules.
- Perform validation.
- Send emails.
- Call external services.
- Manage security.

These responsibilities belong to the Service layer.

---

# Repository Package Organization

PCMS follows a Package by Feature architecture.

```
role/
└── repository/
    └── RoleRepository.java

department/
└── repository/
    └── DepartmentRepository.java

user/
└── repository/
    └── UserRepository.java

casefile/
└── repository/
    └── CaseFileRepository.java

caseassignment/
└── repository/
    └── CaseAssignmentRepository.java

suspect/
└── repository/
    └── SuspectRepository.java

attachment/
└── repository/
    └── AttachmentRepository.java

casecomment/
└── repository/
    └── CaseCommentRepository.java

audit/
└── repository/
    └── AuditLogRepository.java
```

Each feature owns its repository.

---

# Best Practices

- One repository per aggregate root.
- Keep repositories small and focused.
- Prefer derived query methods.
- Use JPQL only when necessary.
- Use native SQL only for database-specific requirements.
- Never place business logic inside repositories.
- Access repositories only through the Service layer.
- Return `Optional<T>` for nullable single-result queries.
- Use pagination for large datasets.

---

# Interview Questions

## Why use JpaRepository instead of EntityManager?

Because `JpaRepository` eliminates repetitive CRUD code, improves productivity, integrates seamlessly with Spring Boot, and provides advanced features such as pagination, sorting, and query derivation.

---

## Why is a Repository declared as an interface?

Spring Data JPA generates the implementation automatically at runtime, allowing developers to focus on business logic rather than persistence infrastructure.

---

## What is the purpose of `JpaRepository<T, ID>`?

It defines the entity type (`T`) and the type of its primary key (`ID`), enabling Spring Data JPA to generate type-safe persistence operations.

---

## Should Controllers access repositories directly?

No.

Controllers communicate with Services.

Services encapsulate business rules and coordinate transactions.

Repositories are responsible only for data access.

---

# Summary

At this stage of the project:

- The PostgreSQL schema is versioned with Flyway.
- JPA entities map the database tables.
- `JpaRepository` provides the persistence layer.
- CRUD operations are generated automatically.
- Pagination and sorting are built in.
- Query methods can be generated from method names.
- Repositories remain focused exclusively on data access.

The next step is to implement the first repository of the project: `RoleRepository`.
