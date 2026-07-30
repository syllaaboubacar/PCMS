# 06 - PostgreSQL Schema

---

| Élément                  | Valeur                               |
| ------------------------ | ------------------------------------ |
| **Document**             | 06-PostgreSQL-Schema.md              |
| **Emplacement**          | `docs/database/`                     |
| **Projet**               | Police Case Management System (PCMS) |
| **Version**              | 1.0.0                                |
| **Statut**               | Draft                                |
| **Auteur**               | Équipe Projet PCMS                   |
| **Dernière mise à jour** | 30/07/2026                           |

---

# Table des matières

1. Objectif
2. Vue d'ensemble
3. Schéma relationnel
4. Description des tables
5. Relations entre les tables
6. Colonnes d'audit
7. Conventions du schéma
8. Évolutions futures
9. Documents associés
10. Historique des révisions

---

# 1. Objectif

Ce document présente le **schéma PostgreSQL** du **Police Case Management System (PCMS)**.

Il constitue la représentation physique de la base de données issue des étapes de conception précédentes :

* Conceptual Data Model (CDM) ;
* Logical Data Model (LDM) ;
* Entity Relationship Diagram (ERD).

Le schéma décrit les tables, leurs principales colonnes, les relations et les conventions retenues pour l'implémentation.

---

# 2. Vue d'ensemble

Le schéma PostgreSQL repose sur les tables suivantes :

| Table              | Description                 |
| ------------------ | --------------------------- |
| `roles`            | Rôles fonctionnels          |
| `departments`      | Départements de police      |
| `users`            | Utilisateurs                |
| `cases`            | Dossiers d'enquête          |
| `case_assignments` | Affectations des enquêteurs |
| `suspects`         | Suspects liés aux enquêtes  |
| `attachments`      | Pièces jointes              |
| `case_comments`    | Commentaires                |
| `audit_logs`       | Journal d'audit             |

Toutes les tables utilisent une clé primaire technique (`id`) et appliquent les conventions définies dans le document **05-PostgreSQL-Types-Constraints-Indexes.md**.

---

# 3. Schéma relationnel

```text id="8v2x1m"
roles
 └── id (PK)

departments
 └── id (PK)

users
 ├── id (PK)
 ├── role_id (FK → roles.id)
 └── department_id (FK → departments.id)

cases
 └── id (PK)

case_assignments
 ├── id (PK)
 ├── case_id (FK → cases.id)
 └── user_id (FK → users.id)

suspects
 ├── id (PK)
 └── case_id (FK → cases.id)

attachments
 ├── id (PK)
 └── case_id (FK → cases.id)

case_comments
 ├── id (PK)
 ├── case_id (FK → cases.id)
 └── author_id (FK → users.id)

audit_logs
 ├── id (PK)
 └── user_id (FK → users.id)
```

---

# 4. Description des tables

## roles

Référence les rôles applicatifs.

Principales colonnes :

* id
* name
* description

---

## departments

Référence les départements ou unités de police.

Principales colonnes :

* id
* code
* name

---

## users

Stocke les utilisateurs du système.

Principales colonnes :

* id
* first_name
* last_name
* email
* password
* enabled
* role_id
* department_id

---

## cases

Représente les dossiers d'enquête.

Principales colonnes :

* id
* title
* description
* status
* priority
* created_at
* updated_at

Le champ `case_number` sera ajouté ultérieurement afin de gérer une numérotation métier indépendante de l'identifiant technique.

---

## case_assignments

Table d'association entre les utilisateurs et les dossiers.

Principales colonnes :

* id
* case_id
* user_id
* assigned_at
* active

Cette table permet de gérer plusieurs enquêteurs sur un même dossier tout en conservant l'historique des affectations.

---

## suspects

Représente les suspects associés à une enquête.

Principales colonnes :

* id
* first_name
* last_name
* birth_date
* case_id

---

## attachments

Stocke les documents associés à une enquête.

Principales colonnes :

* id
* filename
* type
* uploaded_at
* case_id

---

## case_comments

Conserve les commentaires liés aux enquêtes.

Principales colonnes :

* id
* content
* created_at
* author_id
* case_id

---

## audit_logs

Historise les opérations importantes réalisées dans le système.

Principales colonnes :

* id
* action
* timestamp
* user_id

---

# 5. Relations entre les tables

| Table source | Relation | Table cible      |
| ------------ | -------- | ---------------- |
| roles        | 1 → N    | users            |
| departments  | 1 → N    | users            |
| users        | 1 → N    | case_assignments |
| cases        | 1 → N    | case_assignments |
| cases        | 1 → N    | suspects         |
| cases        | 1 → N    | attachments      |
| cases        | 1 → N    | case_comments    |
| users        | 1 → N    | case_comments    |
| users        | 1 → N    | audit_logs       |

Ces relations assurent la cohérence du modèle et reflètent les règles métier définies dans la phase d'analyse.

---

# 6. Colonnes d'audit

La majorité des tables héritera conceptuellement des attributs définis dans **Base Entity**.

| Colonne    | Description                       |
| ---------- | --------------------------------- |
| created_at | Date de création                  |
| created_by | Utilisateur créateur              |
| updated_at | Date de dernière modification     |
| updated_by | Dernier utilisateur ayant modifié |
| deleted    | Suppression logique               |

Ces colonnes permettront d'uniformiser la gestion des données et de faciliter l'audit.

---

# 7. Conventions du schéma

Le schéma PostgreSQL respecte les conventions suivantes :

* noms des tables au pluriel ;
* noms des colonnes en `snake_case` ;
* clé primaire nommée `id` ;
* clés étrangères nommées `<entity>_id` ;
* suppression logique privilégiée ;
* intégrité référentielle assurée par les contraintes de clés étrangères.

Ces conventions garantissent une structure homogène et facilement maintenable.

---

# 8. Évolutions futures

Le schéma est conçu pour accueillir de nouvelles tables sans remettre en cause les fondations existantes.

Les évolutions envisagées comprennent notamment :

* victims ;
* witnesses ;
* evidences ;
* vehicles ;
* weapons ;
* locations ;
* organizations.

Ces extensions seront intégrées progressivement au fil des prochaines versions du projet.

---

# 9. Documents associés

| Document                                   | Description                         |
| ------------------------------------------ | ----------------------------------- |
| 01-Conceptual-Data-Model.md                | Modèle conceptuel                   |
| 02-Base-Entity-and-Audit.md                | Entité de base et audit             |
| 03-Logical-Data-Model.md                   | Modèle logique                      |
| 04-Entity-Relationship-Diagram.md          | Diagramme ERD                       |
| 05-PostgreSQL-Types-Constraints-Indexes.md | Types, contraintes et index         |
| 07-Flyway-Migrations.md                    | Stratégie de migrations *(à venir)* |

---

# 10. Historique des révisions

| Version | Date       | Auteur             | Description          |
| ------- | ---------- | ------------------ | -------------------- |
| 1.0.0   | 30/07/2026 | Équipe Projet PCMS | Création du document |

