# 01 - Spring Boot Project Setup

---

| Élément                  | Valeur                               |
| ------------------------ | ------------------------------------ |
| **Document**             | 01-Spring-Boot-Project-Setup.md      |
| **Emplacement**          | `docs/backend/`                      |
| **Projet**               | Police Case Management System (PCMS) |
| **Version**              | 1.0.0                                |
| **Statut**               | Draft                                |
| **Auteur**               | Équipe Projet PCMS                   |
| **Dernière mise à jour** | 30/07/2026                           |

---

# Table des matières

1. Objectif
2. Vue d'ensemble
3. Prérequis
4. Structure du dépôt
5. Création du projet Spring Boot
6. Dépendances initiales
7. Intégration dans le dépôt
8. Ouverture dans VS Code
9. Premier démarrage
10. Premier commit Git
11. Bonnes pratiques
12. Résultat attendu
13. Documents associés
14. Historique des révisions

---

# 1. Objectif

Ce document décrit la mise en place du projet **Spring Boot** du **Police Case Management System (PCMS)**.

À l'issue de cette étape, le projet dispose :

* d'une structure Maven fonctionnelle ;
* d'un projet Spring Boot 3.x ;
* d'un environnement Java 21 opérationnel ;
* d'un dépôt Git initialisé ;
* d'un premier démarrage réussi.

Cette étape constitue la base de tout le développement backend.

---

# 2. Vue d'ensemble

Le backend du PCMS repose sur les technologies suivantes :

| Élément         | Version retenue               |
| --------------- | ----------------------------- |
| Java            | 21 (LTS)                      |
| Spring Boot     | 3.x (dernière version stable) |
| Build Tool      | Maven                         |
| Base de données | PostgreSQL                    |
| ORM             | Spring Data JPA / Hibernate   |
| Migration       | Flyway                        |
| IDE             | Visual Studio Code            |

---

# 3. Prérequis

Avant de générer le projet, vérifier que les outils suivants sont installés.

## Java

```bash
java --version
```

Résultat attendu :

```text
OpenJDK 21
```

---

## Maven

```bash
mvn -version
```

Résultat attendu :

```text
Apache Maven 3.9.x
Java version: 21
```

---

## Git

```bash
git --version
```

---

## Node.js

Node.js sera utilisé ultérieurement pour le développement du frontend Angular.

```bash
node --version
```

---

# 4. Structure du dépôt

Il est recommandé de créer un répertoire dédié aux projets.

```bash
mkdir -p ~/Development
cd ~/Development
mkdir PCMS
cd PCMS
```

Initialiser ensuite le dépôt Git.

```bash
git init
git branch -M main
```

Créer les principaux répertoires du projet.

```bash
mkdir backend
mkdir frontend
mkdir docs
mkdir diagrams
mkdir docker
mkdir scripts

touch README.md
touch LICENSE
touch .gitignore
```

La structure devient :

```text
PCMS/
├── backend/
├── frontend/
├── docs/
├── diagrams/
├── docker/
├── scripts/
├── README.md
├── LICENSE
└── .gitignore
```

---

# 5. Création du projet Spring Boot

Le projet est généré à l'aide de **Spring Initializr**.

Les paramètres retenus sont les suivants :

| Paramètre   | Valeur               |
| ----------- | -------------------- |
| Project     | Maven                |
| Language    | Java                 |
| Spring Boot | 3.x (version stable) |
| Java        | 21                   |
| Packaging   | Jar                  |
| Group       | `lu.police`          |
| Artifact    | `pcms-backend`       |
| Name        | `pcms-backend`       |
| Package     | `lu.police.pcms`     |

Le package racine du projet sera :

```text
lu.police.pcms
```

Cette convention reflète le contexte du projet :

* `lu` : Luxembourg ;
* `police` : domaine métier ;
* `pcms` : Police Case Management System.

---

# 6. Dépendances initiales

Afin de conserver une progression pédagogique et une architecture maîtrisée, seules les dépendances indispensables sont sélectionnées lors de la création du projet.

## Dépendances initiales

* Spring Web
* Spring Data JPA
* Validation
* PostgreSQL Driver
* Flyway Migration
* Lombok

## Dépendances prévues ultérieurement

Les dépendances suivantes seront ajoutées progressivement :

* Spring Security
* JWT
* MapStruct
* Spring Boot Actuator
* OpenAPI / Swagger
* Testcontainers
* Docker Compose

Cette approche permet d'introduire chaque technologie au moment où elle devient nécessaire.

---

# 7. Intégration dans le dépôt

Après téléchargement depuis Spring Initializr :

1. Décompresser l'archive.
2. Copier le contenu du projet dans le dossier :

```text
PCMS/backend/
```

Le dépôt Git conserve ainsi une séparation claire entre :

* le backend ;
* le frontend ;
* la documentation ;
* les ressources de déploiement.

---

# 8. Ouverture dans Visual Studio Code

Ouvrir le projet à la racine du dépôt :

```bash
cd ~/Development/PCMS
code .
```

Cette organisation permet de travailler dans un espace unique contenant :

* le backend Spring Boot ;
* le frontend Angular ;
* la documentation ;
* les diagrammes ;
* les scripts Docker.

---

# 9. Premier démarrage

Depuis le répertoire `backend` :

```bash
cd backend
./mvnw spring-boot:run
```

Un démarrage réussi affiche un message similaire à :

```text
Started PcmsBackendApplication
```

À ce stade, certaines fonctionnalités (connexion PostgreSQL, sécurité, migrations, etc.) ne sont pas encore configurées, ce qui est normal.

---

# 10. Premier commit Git

Une fois le projet généré et démarré avec succès :

```bash
git add .
git commit -m "Initial Spring Boot project"
```

Ce premier commit constitue un point de référence stable avant l'ajout des fonctionnalités métier.

---

# 11. Bonnes pratiques

Les règles suivantes seront appliquées dès le début du projet :

* versionner le projet dès sa création ;
* réaliser un commit par fonctionnalité ;
* utiliser des messages de commit explicites ;
* conserver une séparation claire entre backend et frontend ;
* ajouter les dépendances progressivement ;
* maintenir une architecture simple et cohérente.

---

# 12. Résultat attendu

À l'issue de ce chapitre, le projet dispose :

* d'un dépôt Git initialisé ;
* d'un projet Spring Boot opérationnel ;
* d'une structure Maven fonctionnelle ;
* d'un environnement Java 21 configuré ;
* d'un premier démarrage validé.

Le backend est désormais prêt à accueillir l'architecture applicative.

---

# 13. Documents associés

| Document                            | Description                     |
| ----------------------------------- | ------------------------------- |
| 02-Backend-Package-Architecture.md  | Organisation des packages       |
| 03-Application-Configuration.md     | Configuration `application.yml` |
| 04-Maven-Dependencies.md            | Dépendances Maven détaillées    |
| ../database/07-Flyway-Migrations.md | Stratégie de migrations Flyway  |

---

# 14. Historique des révisions

| Version | Date       | Auteur             | Description          |
| ------- | ---------- | ------------------ | -------------------- |
| 1.0.0   | 30/07/2026 | Équipe Projet PCMS | Création du document |

