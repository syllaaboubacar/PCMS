# 03 - Logical Data Model (LDM)

---

| Élément                  | Valeur                               |
| ------------------------ | ------------------------------------ |
| **Document**             | 03-Logical-Data-Model.md             |
| **Emplacement**          | `docs/database/`                     |
| **Projet**               | Police Case Management System (PCMS) |
| **Version**              | 1.0.0                                |
| **Statut**               | Draft                                |
| **Auteur**               | Équipe Projet PCMS                   |
| **Dernière mise à jour** | 30/07/2026                           |

---

# Table des matières

1. Objectif
2. Qu'est-ce qu'un Logical Data Model ?
3. Transformation du CDM vers le LDM
4. Tables du modèle logique
5. Relations et clés
6. Modèle logique
7. Règles de conception
8. Préparation du modèle physique
9. Documents associés
10. Historique des révisions

---

# 1. Objectif

Ce document présente le **Logical Data Model (LDM)** du **Police Case Management System (PCMS)**.

Le LDM transforme le **Conceptual Data Model (CDM)** en un modèle relationnel prêt à être implémenté dans un système de gestion de base de données relationnelle.

À ce niveau apparaissent notamment :

* les tables ;
* les clés primaires ;
* les clés étrangères ;
* les relations entre tables.

Les types SQL et les optimisations spécifiques à PostgreSQL ne sont pas encore définis.

---

# 2. Qu'est-ce qu'un Logical Data Model ?

Le modèle logique constitue l'étape intermédiaire entre le métier et la base de données.

Il conserve les concepts du CDM tout en les traduisant sous une forme relationnelle.

Le LDM répond aux questions suivantes :

* Quelles tables seront créées ?
* Comment seront-elles reliées ?
* Quelles clés permettront d'assurer l'intégrité des relations ?

---

# 3. Transformation du CDM vers le LDM

La conception suit la progression suivante :

```text id="s9eqxa"
Business Requirements
        │
        ▼
Business Domain Model
        │
        ▼
Conceptual Data Model (CDM)
        │
        ▼
Logical Data Model (LDM)
        │
        ▼
Entity Relationship Diagram (ERD)
        │
        ▼
PostgreSQL Schema
```

Le LDM conserve les entités métier tout en introduisant les éléments relationnels nécessaires à leur implémentation.

---

# 4. Tables du modèle logique

Le modèle logique comporte les tables suivantes.

| Table              | Clé primaire | Description                 |
| ------------------ | ------------ | --------------------------- |
| `roles`            | id           | Rôles fonctionnels          |
| `departments`      | id           | Départements de police      |
| `users`            | id           | Utilisateurs                |
| `cases`            | id           | Dossiers d'enquête          |
| `case_assignments` | id           | Affectations des enquêteurs |
| `suspects`         | id           | Suspects liés à une enquête |
| `attachments`      | id           | Pièces jointes              |
| `case_comments`    | id           | Commentaires                |
| `audit_logs`       | id           | Journal d'audit             |

Chaque table représente une responsabilité métier unique.

---

# 5. Relations et clés

Les relations conceptuelles sont traduites en clés étrangères.

| Table              | Clé étrangère | Référence   |
| ------------------ | ------------- | ----------- |
| `users`            | role_id       | roles       |
| `users`            | department_id | departments |
| `case_assignments` | case_id       | cases       |
| `case_assignments` | user_id       | users       |
| `suspects`         | case_id       | cases       |
| `attachments`      | case_id       | cases       |
| `case_comments`    | case_id       | cases       |
| `case_comments`    | author_id     | users       |
| `audit_logs`       | user_id       | users       |

Cette organisation garantit la cohérence des relations entre les données.

---

# 6. Modèle logique

```text id="jp0c0k"
roles
-----
PK id

departments
-----------
PK id

users
-----
PK id
FK role_id
FK department_id

cases
-----
PK id

case_assignments
----------------
PK id
FK case_id
FK user_id

suspects
---------
PK id
FK case_id

attachments
-----------
PK id
FK case_id

case_comments
-------------
PK id
FK case_id
FK author_id

audit_logs
----------
PK id
FK user_id
```

Ce modèle reste volontairement indépendant des types de données SQL.

---

# 7. Règles de conception

Le modèle logique respecte les principes suivants :

## Une table par responsabilité

Chaque table représente un seul concept métier.

---

## Clés primaires techniques

Toutes les tables possèdent une clé primaire unique (`id`).

Les modalités de génération seront définies dans le modèle physique.

---

## Relations explicites

Toutes les associations sont représentées par des clés étrangères.

Aucune relation implicite n'est utilisée.

---

## Entité d'association

La relation entre les utilisateurs et les dossiers est modélisée par la table `case_assignments`.

Cette approche permet :

* plusieurs enquêteurs par dossier ;
* conservation de l'historique ;
* stockage des informations propres à l'affectation (`assignedAt`, `active`).

---

## Préparation à la normalisation

Le modèle est conçu pour respecter les principes de normalisation qui seront détaillés dans le chapitre suivant.

---

# 8. Préparation du modèle physique

Le modèle logique servira directement à produire :

* le diagramme Entité-Relation (ERD) ;
* le schéma PostgreSQL ;
* les entités JPA ;
* les repositories Spring Data ;
* les migrations Flyway.

Les éléments suivants seront introduits dans les prochains chapitres :

* types SQL ;
* contraintes `NOT NULL` ;
* contraintes `UNIQUE` ;
* contraintes `CHECK` ;
* index ;
* stratégies de génération des identifiants.

---

# 9. Documents associés

| Document                           | Description                           |
| ---------------------------------- | ------------------------------------- |
| 00-Database-Design-Introduction.md | Introduction à la conception          |
| 01-Conceptual-Data-Model.md        | Modèle conceptuel                     |
| 02-Base-Entity-and-Audit.md        | Entité de base et audit               |
| 04-Entity-Relationship-Diagram.md  | Diagramme Entité-Relation *(à venir)* |
| 05-Normalization.md                | Normalisation *(à venir)*             |
| 06-Constraints-and-Indexes.md      | Contraintes et index *(à venir)*      |

---

# 10. Historique des révisions

| Version | Date       | Auteur             | Description          |
| ------- | ---------- | ------------------ | -------------------- |
| 1.0.0   | 30/07/2026 | Équipe Projet PCMS | Création du document |

