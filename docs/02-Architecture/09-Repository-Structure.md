# 09 - Repository Structure

---

| Élément                  | Valeur                               |
| ------------------------ | ------------------------------------ |
| **Document**             | 09-Repository-Structure.md           |
| **Projet**               | Police Case Management System (PCMS) |
| **Version**              | 1.0.0                                |
| **Statut**               | Draft                                |
| **Auteur**               | Équipe Projet PCMS                   |
| **Dernière mise à jour** | 29/07/2026                           |

---

# Table des matières

1. Objectif
2. Vue d'ensemble du dépôt
3. Choix du Monorepo
4. Organisation du dépôt
5. Architecture Backend – Package by Feature
6. Domaines métier
7. Packages transverses
8. Règles architecturales
9. Conventions de nommage
10. Organisation des tests
11. Bénéfices de l'architecture
12. Documents associés
13. Historique des révisions

---

# 1. Objectif

Ce document définit l'organisation officielle du dépôt Git du **Police Case Management System (PCMS)**.

Il constitue la référence pour :

* l'organisation du code ;
* la structure des projets ;
* les conventions de développement ;
* l'intégration des nouvelles fonctionnalités.

L'objectif est de garantir une architecture cohérente, lisible et maintenable tout au long du projet.

---

# 2. Vue d'ensemble du dépôt

Le projet adopte une architecture **Monorepo**.

L'ensemble du produit (Frontend, Backend, documentation et infrastructure) est regroupé dans un dépôt Git unique.

Cette organisation facilite :

* la gestion des versions ;
* la documentation ;
* l'intégration continue (CI/CD) ;
* les revues de code ;
* la collaboration entre développeurs.

---

# 3. Choix du Monorepo

Le choix d'un **Monorepo** repose sur le fait que le frontend, le backend et la documentation appartiennent au même produit métier.

Les principaux avantages sont :

* un dépôt Git unique ;
* une documentation centralisée ;
* une gestion unifiée des versions ;
* une pipeline CI/CD simplifiée ;
* une meilleure visibilité sur l'ensemble du projet.

---

# 4. Organisation du dépôt

La structure cible du dépôt est la suivante :

```text
pcms/
├── frontend/
├── backend/
├── docs/
├── scripts/
├── docker/
├── .github/
├── README.md
├── LICENSE
└── .gitignore
```

## Description des dossiers

| Dossier     | Description                                  |
| ----------- | -------------------------------------------- |
| `frontend/` | Application Angular                          |
| `backend/`  | API Spring Boot                              |
| `docs/`     | Documentation du projet                      |
| `scripts/`  | Scripts d'administration et d'automatisation |
| `docker/`   | Fichiers Docker et Docker Compose            |
| `.github/`  | Configuration GitHub Actions et workflows    |

### Scripts

Le dossier `scripts/` regroupe notamment :

* `create-db.sh`
* `backup-db.sh`
* `seed-data.sh`
* `clean.sh`

### Docker

Le dossier `docker/` contient :

* `Dockerfile.backend`
* `Dockerfile.frontend`
* `docker-compose.yml`

### GitHub

Le dossier `.github/workflows/` héberge les workflows CI/CD, par exemple :

* `ci.yml`

---

# 5. Architecture Backend – Package by Feature

Le backend adopte une organisation **Package by Feature**.

Contrairement à une organisation par couches techniques (`controller`, `service`, `repository`, etc.), chaque domaine métier regroupe l'ensemble des classes nécessaires à son fonctionnement.

## Structure racine

```text
backend/
└── src/
    └── main/
        └── java/
            └── lu/
                └── police/
                    └── pcms/
                        ├── auth/
                        ├── case/
                        ├── user/
                        ├── dashboard/
                        ├── attachment/
                        ├── audit/
                        ├── common/
                        ├── config/
                        └── security/
```

Cette organisation facilite la compréhension et limite les dépendances entre domaines métiers.

---

# 6. Domaines métier

Chaque domaine métier regroupe toutes les classes nécessaires à une fonctionnalité.

## Auth

Responsabilités :

* authentification ;
* JWT ;
* rafraîchissement des tokens ;
* gestion des rôles.

---

## User

Gestion des utilisateurs :

* création ;
* modification ;
* activation ;
* désactivation.

---

## Case

Le cœur du projet.

Responsabilités :

* gestion des dossiers ;
* statuts ;
* affectations ;
* commentaires.

Exemple :

```text
case/
├── CaseController.java
├── CaseService.java
├── CaseRepository.java
├── CaseEntity.java
├── CaseMapper.java
├── CreateCaseRequest.java
├── CaseResponse.java
├── CaseSpecification.java
├── CaseValidator.java
└── CaseServiceTest.java
```

---

## Attachment

Gestion des pièces jointes :

* photographies ;
* rapports ;
* documents PDF.

---

## Dashboard

Statistiques et tableaux de bord.

---

## Audit

Historique des actions :

* utilisateur ;
* date ;
* opération réalisée.

---

# 7. Packages transverses

Certains packages ne sont pas liés à un domaine métier spécifique.

## config/

Contient la configuration globale de Spring :

* CORS ;
* OpenAPI ;
* Jackson ;
* Beans.

---

## security/

Regroupe les composants de sécurité :

* `SecurityConfig`
* `JwtFilter`
* `AuthenticationProvider`
* `PasswordEncoder`

---

## common/

Contient le code partagé entre plusieurs domaines.

Exemples :

* `ApiResponse`
* `ErrorResponse`
* `Pagination`
* `Constants`
* `Enums`
* `BaseEntity`

---

# 8. Règles architecturales

Le projet respecte les règles suivantes :

* Les **Controllers** appellent uniquement les **Services**.
* Les **Services** appellent uniquement les **Repositories**.
* Les **Repositories** accèdent uniquement à la base de données.
* Les domaines métiers restent indépendants autant que possible.
* Le code partagé est placé dans le package `common`.

Cette séparation garantit une architecture claire et facilite les évolutions.

---

# 9. Conventions de nommage

Les composants utilisent des noms explicites.

| Élément      | Convention          |
| ------------ | ------------------- |
| Controller   | `CaseController`    |
| Service      | `CaseService`       |
| Repository   | `CaseRepository`    |
| Entity       | `CaseEntity`        |
| Mapper       | `CaseMapper`        |
| Request DTO  | `CreateCaseRequest` |
| Response DTO | `CaseResponse`      |

Les abréviations sont évitées afin d'améliorer la lisibilité du code.

---

# 10. Organisation des tests

Chaque domaine métier possède ses propres tests.

Exemple :

```text
case/
├── CaseServiceTest.java
├── CaseControllerTest.java
└── CaseRepositoryTest.java
```

Cette approche facilite :

* la maintenance ;
* la compréhension ;
* l'évolution des fonctionnalités.

---

# 11. Bénéfices de l'architecture

L'organisation retenue apporte plusieurs avantages :

* meilleure lisibilité ;
* forte maintenabilité ;
* évolutivité du projet ;
* réduction du couplage ;
* meilleure répartition du travail entre développeurs ;
* intégration simplifiée de nouvelles fonctionnalités ;
* prise en main plus rapide pour les nouveaux membres de l'équipe.

---

# 12. Documents associés

| Document                  | Description                                    |
| ------------------------- | ---------------------------------------------- |
| README.md                 | Présentation générale                          |
| 05-System-Architecture.md | Architecture générale                          |
| 07-C4-Containers.md       | Architecture des conteneurs                    |
| 08-C4-Components.md       | Architecture des composants                    |
| 10-Domain-Model.md        | Modèle de domaine *(à venir)*                  |
| 11-Database.md            | Architecture de la base de données *(à venir)* |
| 16-Git-Workflow.md        | Workflow Git *(à venir)*                       |

---

# 13. Historique des révisions

| Version | Date       | Auteur             | Description          |
| ------- | ---------- | ------------------ | -------------------- |
| 1.0.0   | 29/07/2026 | Équipe Projet PCMS | Création du document |

