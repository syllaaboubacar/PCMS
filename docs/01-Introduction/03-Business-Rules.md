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

1. Objectif du document
2. Définition
3. Principes métier
4. Catalogue des règles métier
5. Traçabilité
6. Contraintes métier
7. Documents associés
8. Historique des révisions

---

# 1. Objectif du document

Ce document recense les règles métier (Business Rules) applicables au **Police Case Management System (PCMS)**.

Les règles métier définissent les contraintes et comportements que le système doit respecter, indépendamment de leur implémentation technique.

Elles constituent une référence pour :

* les cas d'utilisation ;
* le développement ;
* les tests fonctionnels ;
* les validations métier.

---

# 2. Définition

Une règle métier exprime une contrainte ou un comportement imposé par le domaine fonctionnel.

Elle répond généralement à une question du type :

* Qui peut effectuer une action ?
* Dans quelles conditions ?
* Quelles informations doivent être conservées ?
* Quel comportement est attendu du système ?

Les règles sont identifiées par un identifiant unique de la forme **BRU-XXX**.

---

# 3. Principes métier

Le système repose sur les principes suivants :

* seules les personnes autorisées peuvent accéder à l'application ;
* les actions réalisées doivent être traçables ;
* les données doivent être centralisées ;
* les utilisateurs disposent de droits adaptés à leur rôle ;
* les informations doivent pouvoir être recherchées rapidement.

---

# 4. Catalogue des règles métier

## BRU-001 — Authentification obligatoire

Tout utilisateur doit être authentifié avant d'accéder aux fonctionnalités du système.

Cette authentification constitue le point d'entrée de l'application.

---

## BRU-002 — Contrôle des accès par rôle

Les autorisations sont déterminées par le rôle attribué à l'utilisateur.

Les rôles identifiés sont :

* ADMIN
* OFFICER
* VIEWER

Chaque rôle dispose d'un niveau d'autorisation spécifique.

---

## BRU-003 — Gestion des dossiers

Le système doit permettre les opérations suivantes sur les dossiers :

* création ;
* consultation ;
* modification ;
* suppression ;
* recherche ;
* filtrage.

Toute opération est réalisée par un utilisateur autorisé.

---

## BRU-004 — Gestion des policiers

Le système doit permettre la gestion complète des policiers (CRUD).

---

## BRU-005 — Gestion des suspects

Le système doit permettre la gestion complète des suspects (CRUD).

---

## BRU-006 — Historisation des modifications

Chaque modification réalisée dans l'application doit générer automatiquement une entrée dans l'historique.

Cette entrée permet d'assurer la traçabilité des opérations.

---

## BRU-007 — Informations enregistrées dans l'historique

Pour chaque événement enregistré, le système doit conserver au minimum :

* l'utilisateur ayant réalisé l'action ;
* l'action effectuée ;
* l'élément concerné ;
* la date de l'opération.

Exemple :

> John Doe a créé le dossier 10245 le 02/07/2026.

---

## BRU-008 — Tableau de bord

Le tableau de bord doit présenter une synthèse de l'activité du système.

Les informations disponibles comprennent notamment :

* le nombre de dossiers ;
* le nombre de policiers ;
* le nombre de suspects ;
* les dernières activités ;
* des indicateurs graphiques.

---

## BRU-009 — Recherche d'informations

Le système doit permettre la recherche des dossiers et des informations disponibles afin de faciliter leur consultation.

---

## BRU-010 — API REST

Toutes les interactions entre le frontend et le backend passent par une API REST respectant les bonnes pratiques.

---

## BRU-011 — Pièces jointes

Les pièces jointes associées aux dossiers sont destinées à être stockées dans un système de stockage distinct de la base de données relationnelle.

Cette règle répond à un objectif d'évolutivité de l'architecture.

---

## BRU-012 — Évolutivité de l'authentification

La première version du projet met en œuvre une authentification JWT.

L'architecture prévoit une évolution vers une authentification centralisée reposant sur un fournisseur IAM / SSO compatible OpenID Connect, sans remise en cause de l'architecture globale.

---

# 5. Traçabilité

| Règle métier | Exigence associée |
| ------------ | ----------------- |
| BRU-001      | BR-001            |
| BRU-002      | BR-002            |
| BRU-003      | BR-004            |
| BRU-004      | BR-005            |
| BRU-005      | BR-006            |
| BRU-006      | BR-007            |
| BRU-007      | BR-007            |
| BRU-008      | BR-003            |
| BRU-009      | BR-008            |
| BRU-010      | BR-009            |
| BRU-011      | BR-009            |
| BRU-012      | BR-001            |

---

# 6. Contraintes métier

Les contraintes identifiées à ce stade sont les suivantes :

* les utilisateurs doivent être authentifiés ;
* les droits d'accès sont liés au rôle de l'utilisateur ;
* toutes les opérations importantes sont historisées ;
* les échanges entre les composants applicatifs s'effectuent via une API REST ;
* l'architecture doit rester suffisamment modulaire pour permettre l'intégration future de services externes (IAM, stockage de fichiers, messagerie et journalisation centralisée).

---

# 7. Documents associés

| Document                    | Description              |
| --------------------------- | ------------------------ |
| README.md                   | Présentation générale    |
| 02-Business-Requirements.md | Exigences métier         |
| 04-Use-Cases.md             | Cas d'utilisation        |
| 05-System-Architecture.md   | Architecture logicielle  |
| 12-Security.md              | Architecture de sécurité |

---

# 8. Historique des révisions

| Version | Date       | Auteur             | Description          |
| ------- | ---------- | ------------------ | -------------------- |
| 1.0.0   | 29/07/2026 | Équipe Projet PCMS | Création du document |

