# 03 - Business Rules

---

| Élément                  | Valeur                               |
| ------------------------ | ------------------------------------ |
| **Document**             | 03-Business-Rules.md                 |
| **Projet**               | Police Case Management System (PCMS) |
| **Version**              | 1.0.0                                |
| **Statut**               | Draft                                |
| **Auteur**               | Équipe Projet PCMS                   |
| **Dernière mise à jour** | 29/07/2026                           |

---

# Table des matières

1. Objectif
2. Définition d'une règle métier
3. Pourquoi documenter les règles métier ?
4. Acteurs concernés
5. Classification des règles métier
6. Cycle de vie d'un dossier
7. Gestion des affectations
8. Gestion des commentaires
9. Gestion des pièces jointes
10. Gestion des utilisateurs
11. Audit
12. Sécurité
13. Validation métier
14. Implémentation des règles métier
15. Traçabilité
16. Documents associés
17. Historique des révisions

---

# 1. Objectif

Ce document définit l'ensemble des **règles métier (Business Rules)** qui gouvernent le fonctionnement du **Police Case Management System (PCMS)**.

Ces règles décrivent le comportement attendu du système indépendamment de toute technologie.

Elles constituent la référence officielle pour :

* les spécifications fonctionnelles ;
* l'implémentation backend ;
* les validations côté frontend ;
* les contraintes de persistance ;
* les tests fonctionnels et d'intégration.

---

# 2. Définition d'une règle métier

Une règle métier est une contrainte imposée par le métier et non par la technologie.

### Exemple de règle technique

> La colonne `CASE_ID` est une clé primaire.

Cette règle concerne l'implémentation technique.

### Exemple de règle métier

> Un dossier fermé ne peut plus être modifié.

Cette règle exprime une contrainte fonctionnelle applicable quel que soit le langage ou la base de données utilisée.

---

# 3. Pourquoi documenter les règles métier ?

Les règles métier sont définies avant le développement afin de garantir un comportement cohérent de l'application.

Elles servent de référence pour :

* le développement Angular ;
* le développement Spring Boot ;
* les contraintes de la base PostgreSQL ;
* les scénarios de tests ;
* la validation avec les utilisateurs métier.

---

# 4. Acteurs concernés

Le système distingue trois rôles principaux.

| Rôle           | Description                                 |
| -------------- | ------------------------------------------- |
| **OFFICER**    | Gère les enquêtes auxquelles il est affecté |
| **SUPERVISOR** | Supervise les enquêtes de son département   |
| **ADMIN**      | Administre l'application                    |

---

# 5. Classification des règles métier

| Domaine                 | Identifiants    |
| ----------------------- | --------------- |
| Cycle de vie du dossier | BR-001 → BR-003 |
| Affectations            | BR-004 → BR-006 |
| Commentaires            | BR-007 → BR-009 |
| Pièces jointes          | BR-010 → BR-012 |
| Utilisateurs            | BR-013 → BR-015 |
| Audit                   | BR-016 → BR-017 |
| Sécurité                | BR-018 → BR-020 |
| Validation              | BR-021 → BR-025 |

Cette numérotation facilite les échanges entre les analystes, les développeurs, les testeurs et les utilisateurs métier.

---

# 6. Cycle de vie d'un dossier

Le statut d'un dossier suit le cycle de vie suivant :

```text
OPEN
   │
   ▼
IN_PROGRESS
   │
   ▼
ON_HOLD
   │
   ▼
IN_PROGRESS
   │
   ▼
CLOSED
   │
   ▼
ARCHIVED
```

Un dossier ne peut pas passer directement d'un état à un autre si cette transition n'est pas autorisée.

## Règles

| ID         | Règle                                                             |
| ---------- | ----------------------------------------------------------------- |
| **BR-001** | Un nouveau dossier est toujours créé avec le statut `OPEN`.       |
| **BR-002** | Un dossier ayant le statut `CLOSED` ne peut plus être modifié.    |
| **BR-003** | Un dossier ayant le statut `ARCHIVED` est uniquement consultable. |

---

# 7. Gestion des affectations

Le système utilise l'entité **CaseAssignment** afin de permettre plusieurs enquêteurs sur un même dossier.

## Règles

| ID         | Règle                                                                                              |
| ---------- | -------------------------------------------------------------------------------------------------- |
| **BR-004** | Chaque dossier doit toujours posséder au moins un enquêteur actif.                                 |
| **BR-005** | Un utilisateur ne peut pas être affecté deux fois au même dossier.                                 |
| **BR-006** | Une affectation désactivée est conservée dans l'historique et n'est jamais supprimée physiquement. |

---

# 8. Gestion des commentaires

Les commentaires sont représentés par l'entité **CaseComment**.

## Règles

| ID         | Règle                                                                |
| ---------- | -------------------------------------------------------------------- |
| **BR-007** | Seul l'auteur d'un commentaire peut le modifier.                     |
| **BR-008** | Un commentaire ne peut jamais être supprimé.                         |
| **BR-009** | Chaque commentaire est horodaté automatiquement lors de sa création. |

---

# 9. Gestion des pièces jointes

Les pièces jointes représentent les preuves ou documents associés à une enquête.

## Règles

| ID         | Règle                                                                 |
| ---------- | --------------------------------------------------------------------- |
| **BR-010** | Une pièce jointe appartient à un seul dossier.                        |
| **BR-011** | Les types autorisés sont `PHOTO`, `VIDEO`, `PDF` et `REPORT`.         |
| **BR-012** | Les pièces jointes utilisent une suppression logique (*Soft Delete*). |

---

# 10. Gestion des utilisateurs

## Règles

| ID         | Règle                                                             |
| ---------- | ----------------------------------------------------------------- |
| **BR-013** | Deux utilisateurs ne peuvent pas partager la même adresse e-mail. |
| **BR-014** | Un utilisateur désactivé ne peut plus s'authentifier.             |
| **BR-015** | Chaque utilisateur appartient obligatoirement à un département.   |

---

# 11. Audit

Le système assure la traçabilité des opérations importantes.

Les événements concernés comprennent notamment :

* création ;
* modification ;
* changement de statut ;
* affectation ;
* suppression logique.

## Règles

| ID         | Règle                                                                   |
| ---------- | ----------------------------------------------------------------------- |
| **BR-016** | Toute opération importante est enregistrée dans le journal d'audit.     |
| **BR-017** | Les journaux d'audit sont immuables et ne peuvent jamais être modifiés. |

---

# 12. Sécurité

Les autorisations dépendent du rôle de l'utilisateur.

## Règles

| ID         | Règle                                                                                     |
| ---------- | ----------------------------------------------------------------------------------------- |
| **BR-018** | L'accès aux dossiers dépend du rôle de l'utilisateur et de ses affectations.              |
| **BR-019** | Seuls les utilisateurs ayant le rôle `SUPERVISOR` ou `ADMIN` peuvent clôturer un dossier. |
| **BR-020** | Seuls les utilisateurs ayant le rôle `ADMIN` peuvent gérer les utilisateurs.              |

---

# 13. Validation métier

Les données minimales d'un dossier doivent respecter les contraintes suivantes.

| ID         | Règle                                               |
| ---------- | --------------------------------------------------- |
| **BR-021** | Le titre d'un dossier est obligatoire.              |
| **BR-022** | La description d'un dossier est obligatoire.        |
| **BR-023** | La priorité est obligatoire.                        |
| **BR-024** | Le statut est toujours défini.                      |
| **BR-025** | La date de création est renseignée automatiquement. |

---

# 14. Implémentation des règles métier

Les règles métier ne doivent jamais dépendre du frontend.

Le frontend Angular peut empêcher certaines actions afin d'améliorer l'expérience utilisateur.

Cependant, **le backend reste l'unique garant des règles métier**.

Les règles sont implémentées exclusivement dans la couche **Service**.

```text
Controller
      │
      ▼
Service
      │
      ▼
Repository
      │
      ▼
PostgreSQL
```

Les composants **Controller** et **Repository** ne doivent contenir aucune logique métier.

---

# 15. Traçabilité

L'utilisation des identifiants **BR-001** à **BR-025** permet :

* de référencer facilement une règle dans la documentation ;
* d'établir un lien avec les cas d'utilisation ;
* de faciliter les échanges avec les utilisateurs métier ;
* d'identifier les règles couvertes par les tests ;
* de référencer les règles dans les tickets de développement.

---

# 16. Documents associés

| Document                    | Description                 |
| --------------------------- | --------------------------- |
| 02-Business-Requirements.md | Exigences métier            |
| 04-Use-Cases.md             | Cas d'utilisation           |
| 05-System-Architecture.md   | Architecture générale       |
| 08-C4-Components.md         | Architecture des composants |
| 10-Business-Domain-Model.md | Modèle métier               |
| 12-Security.md              | Architecture de sécurité    |
| 14-Testing.md               | Stratégie de tests          |

---

# 17. Historique des révisions

| Version | Date       | Auteur             | Description          |
| ------- | ---------- | ------------------ | -------------------- |
| 1.0.0   | 29/07/2026 | Équipe Projet PCMS | Création du document |

