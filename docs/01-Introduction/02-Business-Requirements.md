# 02 - Business Requirements

---

| Élément                  | Valeur                               |
| ------------------------ | ------------------------------------ |
| **Document**             | 02-Business-Requirements.md          |
| **Projet**               | Police Case Management System (PCMS) |
| **Version**              | 1.0.0                                |
| **Statut**               | Draft                                |
| **Auteur**               | Équipe Projet PCMS                   |
| **Dernière mise à jour** | 29/07/2026                           |

---

# Table des matières

1. Objectif du document
2. Contexte métier
3. Vision métier
4. Parties prenantes
5. Exigences métier
6. Exigences non fonctionnelles
7. Hors périmètre
8. Traçabilité
9. Documents associés
10. Historique des révisions

---

# 1. Objectif du document

Ce document décrit les besoins métier identifiés pour le **Police Case Management System (PCMS)**.

Il constitue la référence fonctionnelle de haut niveau sur laquelle s'appuieront les règles métier, les cas d'utilisation et l'architecture du système.

---

# 2. Contexte métier

Les services de police manipulent quotidiennement un volume important d'informations liées aux enquêtes.

Ces informations doivent être :

* centralisées ;
* sécurisées ;
* facilement consultables ;
* historisées ;
* accessibles selon les droits des utilisateurs.

Le projet PCMS répond à ce besoin en proposant une application web permettant la gestion des dossiers de police dans un environnement moderne et sécurisé.

---

# 3. Vision métier

Le système doit permettre aux différents utilisateurs autorisés de gérer efficacement les dossiers de police tout au long de leur cycle de vie.

La solution doit offrir :

* une interface moderne ;
* une gestion centralisée des informations ;
* une traçabilité complète des opérations ;
* une architecture évolutive ;
* une sécurité adaptée aux données manipulées.

---

# 4. Parties prenantes

| Partie prenante | Rôle                                                                   |
| --------------- | ---------------------------------------------------------------------- |
| Policier        | Crée, consulte et met à jour les dossiers.                             |
| Chef de service | Supervise les dossiers et consulte les tableaux de bord.               |
| Administrateur  | Gère les utilisateurs, les rôles et la configuration de l'application. |

Ces acteurs constituent les principaux utilisateurs identifiés à ce stade du projet.

---

# 5. Exigences métier

## BR-001 — Authentification des utilisateurs

Le système doit permettre aux utilisateurs autorisés de s'authentifier afin d'accéder aux fonctionnalités correspondant à leur rôle.

---

## BR-002 — Gestion des rôles

Le système doit prendre en charge plusieurs profils utilisateurs afin de contrôler les autorisations d'accès.

Les rôles identifiés sont :

* ADMIN
* OFFICER
* VIEWER

---

## BR-003 — Tableau de bord

Le système doit proposer un tableau de bord présentant des indicateurs permettant de suivre l'activité.

Les indicateurs identifiés sont notamment :

* nombre de dossiers ;
* nombre de policiers ;
* nombre de suspects ;
* dernières activités ;
* graphiques.

---

## BR-004 — Gestion des dossiers

Le système doit permettre de gérer les dossiers de police.

Les opérations attendues sont :

* créer un dossier ;
* consulter un dossier ;
* modifier un dossier ;
* supprimer un dossier ;
* rechercher un dossier ;
* filtrer les dossiers.

---

## BR-005 — Gestion des policiers

Le système doit permettre la gestion des policiers au travers d'opérations CRUD.

---

## BR-006 — Gestion des suspects

Le système doit permettre la gestion des suspects au travers d'opérations CRUD.

---

## BR-007 — Historique des actions

Chaque modification effectuée dans le système doit être enregistrée afin d'assurer une traçabilité complète des opérations.

L'historique doit permettre d'identifier :

* l'utilisateur ;
* l'action réalisée ;
* l'élément concerné ;
* la date de l'opération.

---

## BR-008 — Recherche

Le système doit permettre aux utilisateurs de rechercher rapidement les informations disponibles.

---

## BR-009 — API REST

Le backend doit exposer une API REST respectant les bonnes pratiques afin de permettre les échanges avec le frontend.

---

## BR-010 — Documentation

Le projet doit être accompagné d'une documentation technique et fonctionnelle couvrant l'ensemble de la solution.

---

# 6. Exigences non fonctionnelles

Les exigences non fonctionnelles explicitement mentionnées sont les suivantes :

| Identifiant | Exigence                                   |
| ----------- | ------------------------------------------ |
| NFR-001     | Architecture modulaire.                    |
| NFR-002     | API REST conforme aux bonnes pratiques.    |
| NFR-003     | Sécurité basée sur Spring Security et JWT. |
| NFR-004     | Validation des données.                    |
| NFR-005     | Gestion centralisée des exceptions.        |
| NFR-006     | Utilisation de DTO et de MapStruct.        |
| NFR-007     | Journalisation des événements.             |
| NFR-008     | Tests unitaires et d'intégration.          |
| NFR-009     | Documentation OpenAPI / Swagger.           |
| NFR-010     | Déploiement avec Docker.                   |

---

# 7. Hors périmètre

À ce stade du projet, les éléments suivants ne sont pas décrits comme faisant partie de la première version :

* intégration avec un fournisseur IAM/SSO ;
* stockage externe des pièces jointes ;
* journalisation centralisée ;
* services de messagerie externes.

Ces capacités sont identifiées comme des évolutions possibles de l'architecture.

---

# 8. Traçabilité

| Exigence              | Documents associés                                    |
| --------------------- | ----------------------------------------------------- |
| Authentification      | 03-Business-Rules.md, 04-Use-Cases.md, 12-Security.md |
| Gestion des dossiers  | 03-Business-Rules.md, 04-Use-Cases.md, 09-Database.md |
| Gestion des policiers | 04-Use-Cases.md, 09-Database.md                       |
| Gestion des suspects  | 04-Use-Cases.md, 09-Database.md                       |
| Historique            | 03-Business-Rules.md, 09-Database.md                  |
| Tableau de bord       | 11-Frontend.md                                        |

---

# 9. Documents associés

* README.md
* 01-Introduction.md
* 03-Business-Rules.md
* 04-Use-Cases.md
* 05-System-Architecture.md

---

# 10. Historique des révisions

| Version | Date       | Auteur             | Description          |
| ------- | ---------- | ------------------ | -------------------- |
| 1.0.0   | 29/07/2026 | Équipe Projet PCMS | Création du document |

