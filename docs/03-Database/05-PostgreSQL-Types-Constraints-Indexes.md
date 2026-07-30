# 05 - PostgreSQL Types, Constraints and Indexes

---

| Élément                  | Valeur                                     |
| ------------------------ | ------------------------------------------ |
| **Document**             | 05-PostgreSQL-Types-Constraints-Indexes.md |
| **Emplacement**          | `docs/database/`                           |
| **Projet**               | Police Case Management System (PCMS)       |
| **Version**              | 1.0.0                                      |
| **Statut**               | Draft                                      |
| **Auteur**               | Équipe Projet PCMS                         |
| **Dernière mise à jour** | 30/07/2026                                 |

---

# Table des matières

1. Objectif
2. Du modèle logique au modèle physique
3. Choix des types PostgreSQL
4. Conventions de nommage
5. Contraintes d'intégrité
6. Indexation
7. Principes d'optimisation
8. Exemples de modélisation
9. Préparation des entités JPA
10. Documents associés
11. Historique des révisions

---

# 1. Objectif

Ce document présente les conventions retenues pour le modèle physique PostgreSQL du **Police Case Management System (PCMS)**.

Il définit :

* les types de données PostgreSQL ;
* les contraintes d'intégrité ;
* les stratégies d'indexation ;
* les principes de performance.

Ces conventions seront appliquées uniformément à l'ensemble des tables du projet.

---

# 2. Du modèle logique au modèle physique

Le **Logical Data Model (LDM)** définit les tables et leurs relations.

Le modèle physique ajoute désormais :

* les types SQL ;
* les contraintes ;
* les index ;
* les règles de génération des identifiants.

```text id="ndk5v7"
Conceptual Data Model
          │
          ▼
Logical Data Model
          │
          ▼
Entity Relationship Diagram
          │
          ▼
PostgreSQL Physical Model
```

---

# 3. Choix des types PostgreSQL

Les types suivants seront utilisés de manière standard dans le projet.

| Usage         | Type PostgreSQL                                   | Exemple                        |
| ------------- | ------------------------------------------------- | ------------------------------ |
| Identifiant   | `BIGSERIAL` *(ou `BIGINT GENERATED AS IDENTITY`)* | `id`                           |
| Texte court   | `VARCHAR(n)`                                      | `first_name`, `email`          |
| Texte long    | `TEXT`                                            | `description`, `content`       |
| Booléen       | `BOOLEAN`                                         | `enabled`, `active`, `deleted` |
| Date          | `DATE`                                            | `birth_date`                   |
| Date et heure | `TIMESTAMP WITH TIME ZONE`                        | `created_at`, `updated_at`     |
| Enum métier   | `VARCHAR` *(ou ENUM PostgreSQL si retenu)*        | `status`, `priority`           |

> **Note**
>
> Les types `BIGINT GENERATED AS IDENTITY` sont recommandés dans les versions récentes de PostgreSQL. Ils seront évalués lors de l'implémentation avec JPA.

---

# 4. Conventions de nommage

Les conventions suivantes seront appliquées.

| Élément              | Convention                       |
| -------------------- | -------------------------------- |
| Tables               | pluriel en `snake_case`          |
| Colonnes             | `snake_case`                     |
| Clé primaire         | `id`                             |
| Clé étrangère        | `<entity>_id`                    |
| Tables d'association | nom composé (`case_assignments`) |
| Index                | `idx_<table>_<column>`           |
| Contraintes FK       | `fk_<table>_<reference>`         |
| Contraintes UNIQUE   | `uk_<table>_<column>`            |

Exemples :

```text id="g9xvsy"
users
cases
case_assignments
audit_logs

department_id
role_id
author_id
case_id
```

---

# 5. Contraintes d'intégrité

Le schéma PostgreSQL appliquera les contraintes suivantes.

## Primary Key

Chaque table possède une clé primaire technique.

```text id="z7a2lp"
PRIMARY KEY (id)
```

---

## Foreign Key

Toutes les relations métier sont protégées par des clés étrangères.

Exemples :

* user → role
* user → department
* case_assignment → case
* case_assignment → user

---

## NOT NULL

Les attributs obligatoires seront protégés par des contraintes `NOT NULL`.

Exemples :

* title
* email
* status
* priority
* created_at

---

## UNIQUE

Les données devant rester uniques utiliseront une contrainte `UNIQUE`.

Exemples :

| Colonne            | Justification                                         |
| ------------------ | ----------------------------------------------------- |
| `users.email`      | Un utilisateur possède une seule adresse électronique |
| `departments.code` | Code métier unique                                    |
| `roles.name`       | Nom de rôle unique                                    |

---

## CHECK

Les contraintes `CHECK` seront utilisées lorsque la règle métier peut être validée directement par PostgreSQL.

Exemples :

* priorité autorisée ;
* statut valide ;
* valeurs booléennes cohérentes.

Les règles métier complexes continueront d'être validées dans les services Spring Boot.

---

# 6. Indexation

Les index permettront d'améliorer les performances des recherches.

Les colonnes suivantes seront indexées.

| Table            | Colonnes indexées |
| ---------------- | ----------------- |
| users            | email             |
| users            | department_id     |
| users            | role_id           |
| cases            | status            |
| cases            | priority          |
| cases            | created_at        |
| case_assignments | case_id           |
| case_assignments | user_id           |
| attachments      | case_id           |
| case_comments    | case_id           |
| audit_logs       | user_id           |
| audit_logs       | timestamp         |

Des index composites pourront être ajoutés après analyse des performances.

---

# 7. Principes d'optimisation

Le modèle physique respecte les principes suivants :

* clés primaires numériques ;
* index sur les colonnes fréquemment recherchées ;
* limitation des doublons ;
* intégrité référentielle systématique ;
* suppression logique privilégiée lorsque nécessaire ;
* normalisation jusqu'à la Troisième Forme Normale (3NF).

---

# 8. Exemples de modélisation

## Table `users`

| Colonne       | Type         | Contraintes      |
| ------------- | ------------ | ---------------- |
| id            | BIGSERIAL    | PK               |
| email         | VARCHAR(255) | NOT NULL, UNIQUE |
| first_name    | VARCHAR(100) | NOT NULL         |
| last_name     | VARCHAR(100) | NOT NULL         |
| enabled       | BOOLEAN      | NOT NULL         |
| department_id | BIGINT       | FK               |
| role_id       | BIGINT       | FK               |

---

## Table `cases`

| Colonne     | Type                     | Contraintes |
| ----------- | ------------------------ | ----------- |
| id          | BIGSERIAL                | PK          |
| title       | VARCHAR(255)             | NOT NULL    |
| description | TEXT                     | NOT NULL    |
| status      | VARCHAR(30)              | NOT NULL    |
| priority    | VARCHAR(30)              | NOT NULL    |
| created_at  | TIMESTAMP WITH TIME ZONE | NOT NULL    |

---

# 9. Préparation des entités JPA

Les choix réalisés dans ce document faciliteront la génération des futures entités Spring Boot.

Ils permettront notamment :

* une correspondance directe entre les tables PostgreSQL et les entités JPA ;
* une gestion uniforme des identifiants ;
* une implémentation simplifiée des relations (`@OneToMany`, `@ManyToOne`) ;
* une intégration naturelle avec Flyway.

---

# 10. Documents associés

| Document                          | Description                   |
| --------------------------------- | ----------------------------- |
| 01-Conceptual-Data-Model.md       | Modèle conceptuel             |
| 02-Base-Entity-and-Audit.md       | Entité de base et audit       |
| 03-Logical-Data-Model.md          | Modèle logique                |
| 04-Entity-Relationship-Diagram.md | Diagramme ERD                 |
| 06-Flyway-Migrations.md           | Migrations Flyway *(à venir)* |

---

# 11. Historique des révisions

| Version | Date       | Auteur             | Description          |
| ------- | ---------- | ------------------ | -------------------- |
| 1.0.0   | 30/07/2026 | Équipe Projet PCMS | Création du document |

