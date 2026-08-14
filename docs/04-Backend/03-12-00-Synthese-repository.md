# Module 3 — Développement du Backend Spring Boot

# Chapitre 3.12 — Synthèse de la couche Repository Spring Data JPA

## Objectif du chapitre

Ce chapitre conclut la mise en place de la couche de persistance du **Police Case Management System (PCMS)**.

Après avoir conçu le modèle de données, créé les migrations Flyway, implémenté les entités JPA et développé les Repository Spring Data JPA, nous disposons désormais d'une architecture de persistance complète, robuste et prête à être utilisée par la couche métier.

L'objectif de cette synthèse est de récapituler les choix techniques réalisés, les bonnes pratiques appliquées et les perspectives d'évolution de cette couche essentielle de l'application.

---

# 1. Vue d'ensemble de la couche de persistance

La couche de persistance assure la communication entre l'application Java et la base de données PostgreSQL.

Son architecture est organisée selon le schéma suivant :

```text
                  Client REST
                       │
                       ▼
               Services Métier
                       │
                       ▼
          Repository Spring Data JPA
                       │
                       ▼
                Entités JPA (Hibernate)
                       │
                       ▼
            Base PostgreSQL (Flyway)
```

Chaque niveau possède une responsabilité clairement définie.

| Couche | Responsabilité |
|---------|----------------|
| PostgreSQL | Stockage des données |
| Flyway | Gestion des migrations versionnées |
| Entités JPA | Représentation objet des tables |
| Repository | Accès aux données |
| Services | Logique métier |
| Contrôleurs REST | Exposition de l'API |

Cette séparation des responsabilités constitue l'un des fondements d'une architecture Spring Boot professionnelle.

---

# 2. Architecture Package by Feature

Le projet PCMS adopte une organisation **Package by Feature**.

Chaque domaine métier regroupe l'ensemble de ses composants.

```text
lu.police.pcms

├── role
│   ├── entity
│   ├── repository
│   └── dto
│
├── department
│   ├── entity
│   ├── repository
│   └── dto
│
├── user
│   ├── entity
│   ├── repository
│   └── dto
│
├── casefile
├── caseassignment
├── suspect
├── attachment
├── casecomment
└── audit
```

Cette organisation facilite :

- la navigation dans le code ;
- la séparation des domaines métier ;
- l'évolutivité du projet ;
- le travail en équipe.

---

# 3. Les Repository développés

Au cours de ce chapitre, un Repository Spring Data JPA a été créé pour chacune des entités métier.

| Entité | Repository |
|---------|------------|
| Role | RoleRepository |
| Department | DepartmentRepository |
| User | UserRepository |
| CaseFile | CaseFileRepository |
| CaseAssignment | CaseAssignmentRepository |
| Suspect | SuspectRepository |
| Attachment | AttachmentRepository |
| CaseComment | CaseCommentRepository |
| AuditLog | AuditLogRepository |

Chaque Repository hérite de :

```java
JpaRepository<Entity, Long>
```

Ils bénéficient ainsi automatiquement de toutes les fonctionnalités offertes par Spring Data JPA.

---

# 4. Les fonctionnalités fournies automatiquement

L'héritage de `JpaRepository` met immédiatement à disposition un ensemble complet d'opérations CRUD.

## Création et mise à jour

- save()
- saveAll()

## Lecture

- findById()
- findAll()
- findAllById()

## Suppression

- delete()
- deleteById()
- deleteAll()

## Vérification

- existsById()

## Comptage

- count()

## Tri

- findAll(Sort)

## Pagination

- findAll(Pageable)

Aucune implémentation spécifique n'a été nécessaire pour bénéficier de ces fonctionnalités.

---

# 5. Les méthodes métier développées

Au-delà des opérations CRUD, chaque Repository a été enrichi avec des méthodes adaptées aux besoins du PCMS.

## Recherches

Exemples :

```java
findByEmail(...)

findByName(...)

findByCode(...)

findByRole(...)

findByDepartment(...)

findByStatus(...)

findByPriority(...)

findByCaseFile(...)
```

Spring Data JPA génère automatiquement les requêtes SQL correspondantes à partir du nom des méthodes.

---

## Vérification d'existence

Afin de garantir l'intégrité des données avant leur enregistrement, plusieurs méthodes de vérification ont été ajoutées.

Exemples :

```java
existsByEmail(...)

existsByName(...)

existsByCode(...)

existsByFilename(...)

existsByCaseFileAndUser(...)
```

Ces méthodes permettent notamment de prévenir les doublons avant que les contraintes SQL ne soient violées.

---

## Comptage

Des méthodes de statistiques simples ont également été prévues.

Exemples :

```java
countByRole(...)

countByDepartment(...)

countByStatus(...)

countByPriority(...)

countByCaseFile(...)

countByUser(...)
```

Elles seront utiles pour les tableaux de bord et les indicateurs de suivi.

---

# 6. Bonnes pratiques appliquées

Plusieurs principes ont guidé la conception des Repository.

## Un Repository par entité

Chaque entité possède son propre Repository.

Cela garantit une responsabilité unique et un code facilement maintenable.

---

## Utilisation des méthodes dérivées

Les méthodes sont générées automatiquement par Spring Data JPA dès que cela est possible.

Exemples :

```java
findBy...

existsBy...

countBy...
```

Cette approche évite l'écriture de SQL inutile.

---

## Requêtes personnalisées uniquement lorsque nécessaire

Les annotations telles que :

```java
@Query
```

ne seront introduites que lorsqu'une méthode dérivée ne permet plus de répondre au besoin métier.

---

## Responsabilité limitée

Le Repository ne contient jamais de logique métier.

Son unique responsabilité est l'accès aux données.

Toute règle métier sera implémentée dans les Services.

---

# 7. Validation de la couche Repository

La fiabilité de cette couche repose également sur des tests d'intégration dédiés.

Chaque Repository dispose de sa propre classe de test utilisant :

- JUnit 5 ;
- Spring Boot Test ;
- AssertJ ;
- @DataJpaTest.

Les tests vérifient notamment :

- la sauvegarde des entités ;
- les recherches métier ;
- les méthodes d'existence ;
- les méthodes de comptage ;
- les suppressions ;
- le bon fonctionnement des relations JPA.

Cette stratégie garantit que la couche de persistance fonctionne correctement avant même l'implémentation de la logique métier.

---

# 8. Intégration avec Flyway et Hibernate

La couche Repository s'appuie directement sur deux composants essentiels.

## Flyway

Flyway est responsable :

- de la création du schéma PostgreSQL ;
- des migrations versionnées ;
- de la cohérence entre les environnements.

---

## Hibernate

Hibernate est responsable :

- du mapping Objet ↔ Base de données ;
- de la traduction des opérations Repository en SQL.

La configuration :

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

garantit que les entités correspondent exactement au schéma créé par Flyway, sans jamais modifier la base de données.

---

# 9. Évolutions prévues

La couche Repository pourra être enrichie progressivement selon les besoins du projet.

Les principales évolutions envisagées sont :

- Pagination avancée (`Pageable`) ;
- Tri (`Sort`) ;
- `JpaSpecificationExecutor` ;
- QueryDSL ;
- requêtes JPQL ;
- requêtes SQL natives ;
- projections DTO ;
- `@EntityGraph` pour optimiser le chargement des relations ;
- mise en cache avec `@Cacheable`.

Ces fonctionnalités seront introduites au moment où elles apporteront une réelle valeur métier.

---

# 10. Compétences acquises

À l'issue de ce chapitre, vous maîtrisez les concepts suivants :

- le rôle d'un Repository Spring Data JPA ;
- l'utilisation de `JpaRepository<T, ID>` ;
- les méthodes CRUD automatiques ;
- les méthodes dérivées (`findBy...`, `existsBy...`, `countBy...`) ;
- l'organisation d'un projet en **Package by Feature** ;
- la séparation entre Repository, Service et Controller ;
- les tests d'intégration avec `@DataJpaTest` ;
- l'intégration entre Flyway, Hibernate et PostgreSQL.

Ces compétences constituent une base solide pour développer des applications Spring Boot professionnelles.

---

# 11. Bilan du chapitre

La couche de persistance du **Police Case Management System (PCMS)** est désormais entièrement opérationnelle.

Elle offre :

- une architecture claire et évolutive ;
- une séparation stricte des responsabilités ;
- une gestion fiable des données grâce à Spring Data JPA ;
- un schéma de base de données versionné avec Flyway ;
- une validation du mapping JPA via Hibernate ;
- une couverture de tests dédiée.

Cette fondation permettra de construire sereinement les couches supérieures de l'application.

---

# 12. Suite du parcours

La prochaine étape consiste à construire la couche d'échange entre le backend et les clients REST.

L'ordre recommandé est le suivant :

```text
Partie 13 — DTO (Data Transfer Objects)
        │
        ▼
Partie 14 — Mappers (MapStruct)
        │
        ▼
Partie 15 — Validation (Jakarta Validation)
        │
        ▼
Partie 16 — Services
        │
        ▼
Partie 17 — Gestion des exceptions
        │
        ▼
Partie 18 — Contrôleurs REST
        │
        ▼
Partie 19 — Spring Security
        │
        ▼
Partie 20 — Documentation OpenAPI / Swagger
        │
        ▼
Partie 21 — Tests des Services
        │
        ▼
Partie 22 — Tests des Contrôleurs
        │
        ▼
Partie 23 — Intégration complète
```

La prochaine partie sera donc consacrée aux **DTO (Data Transfer Objects)**. Ils définiront le contrat d'échange de l'API REST et permettront de découpler les entités JPA des données exposées aux clients, tout en préparant l'intégration de **MapStruct** et de **Jakarta Validation**.
