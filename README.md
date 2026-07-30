# Police Case Management System (PCMS)

> **Documentation officielle du projet**

---

| Élément                  | Valeur                               |
| ------------------------ | ------------------------------------ |
| **Projet**               | Police Case Management System (PCMS) |
| **Version**              | 1.0.0                                |
| **Statut**               | Draft                                |
| **Auteur**               | Équipe Projet PCMS                   |
| **Dernière mise à jour** | 29/07/2026                           |

---

# Table des matières

* Présentation
* Contexte
* Objectifs
* Fonctionnalités
* Architecture générale
* Stack technique
* Organisation du dépôt
* Documentation
* Feuille de route
* Bonnes pratiques
* Références
* Historique des révisions

---

# Présentation

Le **Police Case Management System (PCMS)** est un projet de démonstration développé dans le cadre d'une démarche d'Analyste-Développeur.

Il s'inspire de l'environnement de travail d'un service de police moderne et a pour objectif de démontrer la capacité à analyser un besoin, concevoir une architecture logicielle, développer une application full-stack, documenter les choix techniques, mettre en œuvre les bonnes pratiques de développement et préparer un déploiement complet.

Le projet est volontairement limité en taille afin de mettre l'accent sur la qualité de l'architecture, la maintenabilité, la documentation et les pratiques professionnelles.

---

# Contexte

La gestion des dossiers constitue une activité centrale des services de police.

Le système PCMS vise à centraliser les informations relatives aux dossiers d'enquête tout en offrant une architecture moderne, évolutive et sécurisée.

Le projet est conçu selon une approche proche des standards utilisés en entreprise afin de démontrer des compétences en :

* analyse des besoins ;
* conception logicielle ;
* développement Frontend et Backend ;
* modélisation de base de données ;
* sécurité ;
* documentation technique ;
* déploiement ;
* intégration continue.

---

# Objectifs du projet

Les principaux objectifs sont les suivants :

* Centraliser la gestion des dossiers.
* Gérer les policiers et les suspects.
* Assurer la traçabilité des actions.
* Sécuriser l'accès aux données.
* Fournir une API REST moderne.
* Faciliter la maintenance du système.
* Démontrer une architecture professionnelle.
* Produire une documentation complète.

---

# Fonctionnalités principales

La première version du projet couvre notamment :

## Authentification

* Connexion utilisateur
* Déconnexion
* JWT
* Refresh Token
* Gestion des rôles

## Tableau de bord

* Nombre de dossiers
* Nombre de policiers
* Nombre de suspects
* Dernières activités
* Statistiques

## Gestion des dossiers

* Création
* Consultation
* Modification
* Suppression
* Recherche
* Filtres

## Gestion des policiers

* CRUD complet

## Gestion des suspects

* CRUD complet

## Historique

Chaque modification réalisée dans le système est historisée afin d'assurer la traçabilité des opérations.

---

# Architecture générale

Le système repose sur une architecture Web moderne.

```text
                        Internet
                            │
                            ▼
                 Frontend Angular 20
                       (Vercel)
                            │
                    REST API HTTPS
                            │
                            ▼
             Backend Spring Boot 3
                            │
          ┌─────────────────┴─────────────────┐
          │                                   │
     PostgreSQL                        (Évolutions futures)
          │                         Stockage externe / Cache
          │
      Flyway
          │
 Docker Compose
```

Cette architecture favorise :

* la séparation des responsabilités ;
* la modularité ;
* la maintenabilité ;
* l'évolutivité ;
* la sécurité.

---

# Stack technique

## Frontend

* Angular 20
* TypeScript
* Angular Material
* RxJS
* Signals
* Angular Router
* Standalone Components
* SCSS

## Backend

* Java 21
* Spring Boot 3
* Spring Security
* Spring Data JPA
* Bean Validation
* MapStruct
* Lombok
* Flyway
* OpenAPI (Swagger)

## Base de données

* PostgreSQL

## Outils

* Git
* GitHub
* Docker
* Docker Compose
* GitHub Actions
* Postman
* IntelliJ IDEA
* Visual Studio Code

---

# Organisation du dépôt

```text
docs/
│
├── README.md
├── 01-Introduction.md
├── 02-Business-Requirements.md
├── 03-Business-Rules.md
├── 04-Use-Cases.md
├── 05-System-Architecture.md
├── 06-C4-Context.md
├── 07-C4-Containers.md
├── 08-C4-Components.md
├── 09-Database.md
├── 10-Backend.md
├── 11-Frontend.md
├── 12-Security.md
├── 13-Deployment.md
├── 14-Testing.md
├── 15-Coding-Standards.md
├── 16-Git-Workflow.md
│
├── adr/
├── diagrams/
└── meeting-notes/
```

---

# Documentation

La documentation est organisée de manière progressive.

Elle couvre :

* les besoins métier ;
* les règles métier ;
* les cas d'utilisation ;
* l'architecture logicielle ;
* les diagrammes C4 ;
* la base de données ;
* le backend ;
* le frontend ;
* la sécurité ;
* le déploiement ;
* les tests ;
* les conventions de développement ;
* les décisions d'architecture (ADR).

---

# Feuille de route

Le développement est organisé en plusieurs phases.

## Phase 0

* Analyse
* Conception
* Cas d'utilisation
* Modèle de données
* Architecture

## Phase 1

* Initialisation Git
* Backend Spring Boot
* Frontend Angular
* Docker
* PostgreSQL
* Flyway
* CI/CD

## Phase 2

* Authentification
* JWT
* Gestion des rôles
* Protection des routes

## Phase 3

* Dashboard
* Gestion des dossiers
* Gestion des policiers
* Gestion des suspects
* Historique
* Recherche

## Phase 4

* Validation
* Gestion des exceptions
* Documentation OpenAPI
* Tests
* Journalisation

## Phase 5

* Déploiement Backend
* Déploiement Frontend
* Déploiement Base de données
* Validation de bout en bout

---

# Bonnes pratiques

Le projet applique notamment :

* Architecture modulaire
* API REST
* Clean Architecture (objectif d'évolution)
* Documentation continue
* Git Flow
* Intégration Continue
* Docker
* Flyway
* OpenAPI
* Journalisation
* Tests automatisés

---

# Références

Les documents complémentaires sont disponibles dans le dossier `docs/`.

Les décisions d'architecture sont documentées dans `docs/adr/`.

Les diagrammes (C4, UML, Mermaid et ERD) sont regroupés dans `docs/diagrams/`.

---

# Historique des révisions

| Version | Date       | Auteur             | Description          |
| ------- | ---------- | ------------------ | -------------------- |
| 1.0.0   | 29/07/2026 | Équipe Projet PCMS | Création du document |

