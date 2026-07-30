# 00 - Database Design Introduction

---

| Élément                  | Valeur                               |
| ------------------------ | ------------------------------------ |
| **Document**             | 00-Database-Design-Introduction.md   |
| **Emplacement**          | `docs/database/`                     |
| **Projet**               | Police Case Management System (PCMS) |
| **Version**              | 1.0.0                                |
| **Statut**               | Draft                                |
| **Auteur**               | Équipe Projet PCMS                   |
| **Dernière mise à jour** | 29/07/2026                           |

---

# Table des matières

1. Objectif
2. Vue d'ensemble
3. Objectifs du module
4. Méthodologie de conception
5. Les niveaux de modélisation
6. Pourquoi ne pas commencer par le SQL ?
7. Pourquoi PostgreSQL ?
8. Principes de conception
9. Objets principaux de la base de données
10. Correspondance avec le Domain Model
11. Résultat attendu
12. Documents associés
13. Historique des révisions

---

# 1. Objectif

Ce document présente la méthodologie de conception de la base de données du **Police Case Management System (PCMS)**.

Avant toute implémentation SQL, la conception suit une approche progressive permettant de transformer les besoins métier en un schéma relationnel robuste, cohérent et évolutif.

Cette démarche garantit que la structure de la base de données répond aux exigences fonctionnelles tout en restant indépendante des choix techniques dans les premières étapes de conception.

---

# 2. Vue d'ensemble

La base de données constitue le socle du système.

Sa conception repose sur une succession d'étapes permettant de passer progressivement :

* des besoins métier ;
* au modèle conceptuel ;
* au modèle relationnel ;
* puis à l'implémentation PostgreSQL.

Chaque étape dépend directement de la précédente et doit être validée avant de poursuivre.

---

# 3. Objectifs du module

À l'issue du module **Database Design**, le projet disposera des éléments suivants :

* un modèle conceptuel de données (CDM/MCD) ;
* un modèle logique de données (LDM/MLD) ;
* un diagramme Entité-Relation (ERD) ;
* un schéma PostgreSQL normalisé ;
* les contraintes d'intégrité ;
* les conventions de nommage ;
* les migrations Flyway ;
* une base prête à être utilisée par les entités JPA.

---

# 4. Méthodologie de conception

La conception suit systématiquement la chaîne de transformation suivante :

```text
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
        │
        ▼
JPA Entities
        │
        ▼
Repositories
```

Chaque étape enrichit la précédente sans remettre en cause les décisions métier déjà validées.

---

# 5. Les niveaux de modélisation

## 5.1 Conceptual Data Model (CDM)

Le modèle conceptuel décrit le métier.

Il définit :

* les entités ;
* les relations ;
* les cardinalités.

À ce niveau, aucune notion de table, de colonne ou de type SQL n'est introduite.

---

## 5.2 Logical Data Model (LDM)

Le modèle logique transforme les entités métier en structures relationnelles.

Les éléments suivants apparaissent progressivement :

* tables ;
* attributs ;
* clés primaires ;
* clés étrangères.

Le modèle reste indépendant d'un système de gestion de base de données particulier.

---

## 5.3 Physical Data Model

Le modèle physique correspond à l'implémentation réelle dans PostgreSQL.

Il introduit notamment :

* les instructions SQL ;
* les types de données ;
* les index ;
* les contraintes ;
* les optimisations spécifiques au SGBD.

---

# 6. Pourquoi ne pas commencer par le SQL ?

Commencer directement par la création des tables peut conduire à des modifications importantes lorsque les besoins métier évoluent.

Par exemple, si l'on découvre ultérieurement qu'un dossier peut être attribué à plusieurs enquêteurs, il faudra :

* modifier les tables ;
* migrer les données existantes ;
* adapter les entités JPA ;
* mettre à jour les API REST ;
* modifier le frontend Angular.

Une conception préalable permet d'éviter ces restructurations coûteuses.

---

# 7. Pourquoi PostgreSQL ?

Le projet utilise **PostgreSQL** comme système de gestion de base de données relationnelle.

Ce choix repose sur plusieurs critères :

* solution Open Source ;
* excellentes performances ;
* forte adoption dans les applications professionnelles ;
* intégration native avec Spring Boot ;
* compatibilité avec Docker.

La méthodologie de conception reste néanmoins compatible avec d'autres bases relationnelles, notamment Oracle, car elle repose sur les principes généraux du modèle relationnel.

---

# 8. Principes de conception

La base de données du PCMS respecte les principes suivants.

## Une responsabilité par table

Chaque table représente un seul concept métier.

Exemple :

* `users` contient uniquement les utilisateurs ;
* `cases` contient uniquement les dossiers d'enquête.

---

## Éviter les duplications

Les informations ne doivent être stockées qu'une seule fois.

Les relations entre entités sont privilégiées afin de limiter les redondances et de faciliter les mises à jour.

---

## Clés techniques

Chaque table possède une clé primaire technique.

Le projet utilisera des identifiants générés automatiquement (`BIGSERIAL` ou stratégie `IDENTITY` avec JPA).

---

## Relations explicites

Les liens entre les entités sont représentés au moyen de clés étrangères.

Les listes de valeurs stockées dans une seule colonne sont proscrites.

Chaque relation métier est modélisée par une table ou une entité dédiée lorsque cela est nécessaire.

---

## Normalisation

Le modèle relationnel sera progressivement normalisé afin de limiter les redondances et de garantir la cohérence des données.

L'objectif final est de respecter la **Troisième Forme Normale (3NF)**.

---

# 9. Objets principaux de la base de données

À partir du modèle métier actuel, les principales tables prévues sont les suivantes :

| Table              | Responsabilité              |
| ------------------ | --------------------------- |
| `users`            | Utilisateurs                |
| `roles`            | Rôles                       |
| `departments`      | Départements                |
| `cases`            | Dossiers d'enquête          |
| `case_assignments` | Affectations des enquêteurs |
| `suspects`         | Suspects                    |
| `attachments`      | Pièces jointes              |
| `case_comments`    | Commentaires                |
| `audit_logs`       | Journal d'audit             |

Cette liste pourra être complétée au fur et à mesure de l'avancement de la conception.

---

# 10. Correspondance avec le Business Domain Model

| Entité métier  | Table PostgreSQL   |
| -------------- | ------------------ |
| User           | `users`            |
| Role           | `roles`            |
| Department     | `departments`      |
| Case           | `cases`            |
| CaseAssignment | `case_assignments` |
| Suspect        | `suspects`         |
| Attachment     | `attachments`      |
| CaseComment    | `case_comments`    |
| AuditLog       | `audit_logs`       |

Conformément aux conventions du projet :

* les **classes Java** sont nommées au singulier (`User`, `Case`, `Role`) ;
* les **tables PostgreSQL** sont nommées au pluriel (`users`, `cases`, `roles`).

---

# 11. Résultat attendu

À la fin du module, le projet disposera :

* d'un modèle conceptuel validé ;
* d'un modèle logique complet ;
* d'un diagramme ERD officiel ;
* d'un schéma PostgreSQL prêt à être implémenté ;
* d'une base compatible avec les entités JPA et les migrations Flyway.

Ces éléments constitueront la référence pour le développement du backend Spring Boot.

---

# 12. Documents associés

| Document                          | Description                              |
| --------------------------------- | ---------------------------------------- |
| 02-Business-Requirements.md       | Exigences métier                         |
| 03-Business-Rules.md              | Règles métier                            |
| 05-System-Architecture.md         | Architecture générale                    |
| 09-Repository-Structure.md        | Organisation du dépôt                    |
| 10-Business-Domain-Model.md       | Modèle métier                            |
| 01-Conceptual-Data-Model.md       | Modèle Conceptuel de Données *(à venir)* |
| 02-Logical-Data-Model.md          | Modèle Logique de Données *(à venir)*    |
| 03-Entity-Relationship-Diagram.md | Diagramme ERD *(à venir)*                |

---

# 13. Historique des révisions

| Version | Date       | Auteur             | Description          |
| ------- | ---------- | ------------------ | -------------------- |
| 1.0.0   | 29/07/2026 | Équipe Projet PCMS | Création du document |

