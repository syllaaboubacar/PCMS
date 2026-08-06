# Package Structure (Package by Feature)

**Version:** 1.0  
**Module:** 3 – Backend Development (Spring Boot)  
**Chapter:** 3.9.2 – Project Organization (Package by Feature)  
**Project:** Police Case Management System (PCMS)

---

# Purpose

This document defines the package organization adopted by the Police Case Management System (PCMS) backend.

The project follows a **Package by Feature** architecture, grouping classes by business domain rather than by technical layer.

This organization improves maintainability, scalability, and developer productivity while supporting long-term evolution of the application.

---

# Why Define the Package Structure Early?

Before implementing entities, repositories, services, and REST controllers, it is essential to establish a consistent project structure.

A well-organized package hierarchy:

- improves code readability;
- reduces coupling between business domains;
- simplifies feature development;
- makes navigation easier;
- supports collaborative development.

As the backend grows, a clear organization becomes a key factor in maintainability.

---

# Common Package Organization Approaches

Two major package organization strategies are commonly used in Spring Boot applications.

## Package by Layer

The traditional approach organizes classes according to their technical responsibility.

```text
entity/
repository/
service/
controller/
dto/
mapper/
config/
security/
```

Example:

```text
entity/
├── User.java
├── Role.java
├── Department.java
├── Case.java
└── ...

repository/
├── UserRepository.java
├── RoleRepository.java
└── ...

service/
├── UserService.java
├── RoleService.java
└── ...
```

### Advantages

- Easy to understand.
- Suitable for small applications.
- Common in tutorials and introductory projects.

### Limitations

As the application grows, packages become very large.

Example:

```text
entity/
    60 classes

repository/
    70 classes

service/
    75 classes

controller/
    65 classes
```

Developers must constantly navigate between multiple packages to work on a single business feature.

---

# Package by Feature

Package by Feature groups all classes belonging to the same business domain.

Instead of organizing by technical layer, the project is organized around functional modules.

Example:

```text
user/
role/
department/
casefile/
attachment/
suspect/
audit/
```

Each package contains everything required for its feature.

Example:

```text
user/

├── User.java
├── UserRepository.java
├── UserService.java
├── UserServiceImpl.java
├── UserController.java
├── UserMapper.java
├── UserDto.java
├── CreateUserRequest.java
└── UpdateUserRequest.java
```

Similarly:

```text
casefile/

├── Case.java
├── CaseRepository.java
├── CaseService.java
├── CaseController.java
├── CaseMapper.java
├── CaseDto.java
└── ...
```

All files related to the same business concept remain together.

---

# Why Package by Feature?

PCMS is designed as an enterprise application that will continue to evolve.

Package by Feature offers several important advantages.

## Better Business Cohesion

Each feature is self-contained.

Developers can work on a business domain without navigating through unrelated packages.

---

## Easier Navigation

When implementing or debugging a feature, all related classes are located in the same directory.

For example:

```text
user/
```

contains everything related to user management.

---

## Improved Maintainability

Adding or modifying a feature affects only one package.

This reduces the risk of unintended side effects elsewhere in the application.

---

## Better Team Collaboration

Different developers can work simultaneously on different modules with fewer merge conflicts.

Example:

- Developer A works on `casefile`
- Developer B works on `attachment`
- Developer C works on `user`

Each developer remains mostly isolated within their feature.

---

## Better Scalability

As the application grows, new modules can be added without affecting the existing package organization.

Examples:

```text
victim/
vehicle/
weapon/
evidence/
notification/
report/
```

The architecture naturally supports future extensions.

---

# Package Structure Adopted for PCMS

The backend follows the following organization.

```text
src/
└── main/
    └── java/
        └── lu/
            └── police/
                └── pcms/
                    │
                    ├── common/
                    │   ├── config/
                    │   ├── exception/
                    │   ├── model/
                    │   ├── security/
                    │   └── util/
                    │
                    ├── role/
                    ├── department/
                    ├── user/
                    ├── casefile/
                    ├── caseassignment/
                    ├── suspect/
                    ├── attachment/
                    ├── casecomment/
                    └── audit/
```

Each package represents one business domain of the Police Case Management System.

---

# Why `casefile` Instead of `case`?

The Java language reserves the keyword `case` for `switch` statements.

Using `case` as a package name can create confusion and reduce readability.

For this reason, the project adopts:

```text
casefile/
```

while keeping the entity name:

```java
Case
```

This naming convention is common in enterprise Java applications.

---

# Entity Placement

Each entity is stored within its corresponding business package.

| Entity | Package |
|---------|----------|
| Role | `role` |
| Department | `department` |
| User | `user` |
| Case | `casefile` |
| CaseAssignment | `caseassignment` |
| Suspect | `suspect` |
| Attachment | `attachment` |
| CaseComment | `casecomment` |
| AuditLog | `audit` |

Future classes belonging to the same feature will remain in the same package.

Example:

```text
user/

├── User.java
├── UserRepository.java
├── UserService.java
├── UserController.java
├── UserMapper.java
└── ...
```

---

# The Common Package

Some classes are shared across multiple business features.

Rather than duplicating them, they are centralized under the `common` package.

```text
common/

├── config/
├── exception/
├── model/
├── security/
└── util/
```

This package contains reusable infrastructure components.

---

# BaseEntity Location

Every persistent entity shares the same audit fields.

Instead of duplicating them, all entities inherit from a common superclass.

```text
common/
└── model/
    └── BaseEntity.java
```

This class will provide:

- createdAt
- createdBy
- updatedAt
- updatedBy
- deleted

The implementation of `BaseEntity` is covered in the next chapter.

---

# Benefits of the Common Package

Centralizing shared components provides several advantages.

- Eliminates duplicated code.
- Standardizes common behavior.
- Improves consistency.
- Simplifies maintenance.
- Facilitates future enhancements.

Only cross-cutting components belong in the `common` package.

Business logic remains inside feature packages.

---

# Package Evolution

As the project grows, each feature can evolve independently.

Example:

```text
casefile/

├── Case.java
├── CaseRepository.java
├── CaseService.java
├── CaseServiceImpl.java
├── CaseController.java
├── CaseMapper.java
├── dto/
├── validator/
└── specification/
```

The structure remains clear even as functionality expands.

---

# Design Principles

The package organization follows these principles.

- Group code by business feature.
- Keep related classes together.
- Minimize dependencies between modules.
- Centralize shared infrastructure.
- Promote readability and maintainability.
- Prepare the project for long-term evolution.

---

# Summary

The PCMS backend adopts a **Package by Feature** architecture.

This organization:

- groups code by business domain;
- improves navigation and maintainability;
- supports collaborative development;
- scales naturally as new features are added;
- aligns with modern enterprise practices such as Modular Monolith, Clean Architecture, and Domain-Driven Design (DDD Lite).

The `common` package contains reusable infrastructure, while each business feature owns its entities, repositories, services, controllers, DTOs, mappers, and validators.

---

# Next Step

The next document introduces the first shared persistence class:

**03.09-03-BaseEntity.md**

It explains why `BaseEntity` is implemented as a `@MappedSuperclass` and how it centralizes audit information for all persistent entities in the Police Case Management System.
