# 02 - Base Entity and Audit Model

---

| Élément                  | Valeur                               |
| ------------------------ | ------------------------------------ |
| **Document**             | 02-Base-Entity-and-Audit.md          |
| **Emplacement**          | `docs/database/`                     |
| **Projet**               | Police Case Management System (PCMS) |
| **Version**              | 1.0.0                                |
| **Statut**               | Draft                                |
| **Auteur**               | Équipe Projet PCMS                   |
| **Dernière mise à jour** | 30/07/2026                           |

---

# Table des matières

1. Objectif
2. Pourquoi une Base Entity ?
3. Audit des données
4. Attributs communs
5. Modèle conceptuel
6. Héritage des entités
7. Soft Delete
8. Principes de conception
9. Préparation du modèle logique
10. Documents associés
11. Historique des révisions

---

# 1. Objectif

Ce document définit les attributs techniques communs qui seront partagés par la majorité des entités du **Police Case Management System (PCMS)**.

L'objectif est de :

* centraliser les informations d'audit ;
* uniformiser le modèle de données ;
* simplifier les futures entités JPA ;
* préparer le schéma PostgreSQL ;
* faciliter les migrations Flyway.

---

# 2. Pourquoi une Base Entity ?

Dans une application d'entreprise, la plupart des tables possèdent des informations identiques :

* date de création ;
* auteur de la création ;
* date de modification ;
* auteur de la modification ;
* indicateur de suppression logique.

Plutôt que de redéfinir ces colonnes dans chaque entité, elles sont regroupées dans un modèle conceptuel commun appelé **Base Entity**.

Cette approche favorise :

* la réutilisabilité ;
* la cohérence ;
* la maintenabilité.

---

# 3. Audit des données

Le système doit assurer la traçabilité des principales opérations métier.

Les informations d'audit permettent notamment de répondre aux questions suivantes :

* Qui a créé cet enregistrement ?
* Quand a-t-il été créé ?
* Qui l'a modifié ?
* Quand a eu lieu la dernière modification ?
* L'enregistrement est-il supprimé logiquement ?

Ces informations complètent le journal d'audit (`AuditLog`) en fournissant un historique minimal directement sur chaque entité.

---

# 4. Attributs communs

Les attributs suivants seront hérités par les principales entités métier.

| Attribut    | Description                                         |
| ----------- | --------------------------------------------------- |
| `createdAt` | Date de création de l'enregistrement                |
| `createdBy` | Utilisateur ayant créé l'enregistrement             |
| `updatedAt` | Date de la dernière modification                    |
| `updatedBy` | Utilisateur ayant effectué la dernière modification |
| `deleted`   | Indicateur de suppression logique                   |

Ces attributs restent indépendants de toute implémentation technique à ce stade.

Les types SQL seront définis lors de la conception du modèle physique.

---

# 5. Modèle conceptuel

```text id="o6u7pv"
                BaseEntity
                ───────────
                createdAt
                createdBy
                updatedAt
                updatedBy
                deleted
                     ▲
                     │
      ┌──────────────┼──────────────┐
      │              │              │
     User          Case        Department
      │              │              │
      ├──────────────┼──────────────┤
      │              │              │
 Attachment   CaseComment   CaseAssignment
      │
   AuditLog*
```

> **Remarque**
>
> `AuditLog` possède également ses propres informations métier (`action`, `entityName`, `entityId`, etc.). Les attributs d'audit communs permettent néanmoins de conserver une structure homogène dans l'ensemble du modèle.

---

# 6. Héritage des entités

Les principales entités du modèle hériteront conceptuellement de la **Base Entity** :

* Role
* Department
* User
* Case
* CaseAssignment
* Suspect
* Attachment
* CaseComment
* AuditLog

Ce mécanisme sera implémenté côté backend à l'aide d'une classe abstraite lors du développement Spring Boot.

---

# 7. Soft Delete

Le projet privilégie la **suppression logique** (*Soft Delete*) plutôt que la suppression physique.

Le champ `deleted` permet :

* de conserver l'historique ;
* d'éviter les pertes de données ;
* de préserver les relations entre entités ;
* de répondre aux exigences d'audit.

Les enregistrements supprimés restent présents dans la base mais sont ignorés par les traitements métier.

---

# 8. Principes de conception

La Base Entity respecte les principes suivants :

* informations techniques séparées des données métier ;
* structure uniforme entre les entités ;
* audit systématique ;
* compatibilité avec la suppression logique ;
* préparation aux mécanismes automatiques de persistance.

---

# 9. Préparation du modèle logique

L'introduction de la Base Entity simplifiera les étapes suivantes :

* définition des colonnes communes dans le MLD ;
* création des tables PostgreSQL ;
* génération des entités JPA ;
* mise en œuvre des migrations Flyway ;
* implémentation de l'audit automatique.

Le chapitre suivant introduira le **Logical Data Model (LDM)**, dans lequel ces attributs seront traduits en colonnes relationnelles.

---

# 10. Documents associés

| Document                           | Description                  |
| ---------------------------------- | ---------------------------- |
| 00-Database-Design-Introduction.md | Introduction à la conception |
| 01-Conceptual-Data-Model.md        | Modèle conceptuel            |
| 03-Business-Rules.md               | Règles métier                |
| 03-Logical-Data-Model.md           | Modèle logique *(à venir)*   |
| 04-Entity-Relationship-Diagram.md  | Diagramme ERD *(à venir)*    |

---

# 11. Historique des révisions

| Version | Date       | Auteur             | Description          |
| ------- | ---------- | ------------------ | -------------------- |
| 1.0.0   | 30/07/2026 | Équipe Projet PCMS | Création du document |

