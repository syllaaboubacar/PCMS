# 03-01 - Backend Configuration

---

| Élément                  | Valeur                                   |
| ------------------------ | ---------------------------------------- |
| **Document**             | 03-01-Backend-Configuration.md           |
| **Emplacement**          | `docs/backend/03-Backend-Configuration/` |
| **Projet**               | Police Case Management System (PCMS)     |
| **Version**              | 1.0.0                                    |
| **Statut**               | Draft                                    |
| **Auteur**               | Équipe Projet PCMS                       |
| **Dernière mise à jour** | 31/07/2026                               |

---

# Table des matières

1. Objectif
2. Présentation
3. Objectifs de configuration
4. Architecture retenue
5. Composants de configuration
6. Ordre de mise en œuvre
7. Résultat attendu
8. Documents associés
9. Historique des révisions

---

# 1. Objectif

Ce document introduit la configuration technique du backend du **Police Case Management System (PCMS)**.

Avant toute implémentation métier, il est indispensable de disposer d'un environnement d'exécution stable permettant au backend Spring Boot de démarrer correctement et de communiquer avec la base de données PostgreSQL.

Cette étape constitue le socle de toutes les fonctionnalités qui seront développées par la suite.

---

# 2. Présentation

Le backend PCMS repose sur une architecture moderne utilisant :

* Java 21 ;
* Spring Boot 3 ;
* Spring Data JPA ;
* Hibernate ;
* PostgreSQL ;
* Flyway.

Avant de développer les premières entités métier, plusieurs éléments doivent être configurés :

* la connexion à la base de données ;
* les propriétés de l'application ;
* les dépendances Maven ;
* les migrations Flyway ;
* le premier démarrage de l'application.

Chaque sujet est traité dans un document dédié afin de conserver une documentation claire et modulaire.

---

# 3. Objectifs de configuration

À l'issue de cette phase, le backend devra être capable de :

* démarrer sans erreur ;
* se connecter à PostgreSQL ;
* appliquer automatiquement les migrations Flyway ;
* valider le schéma de la base avec Hibernate ;
* préparer l'implémentation des entités JPA.

Cette configuration constitue la base de tout le développement backend.

---

# 4. Architecture retenue

Le projet adopte les choix techniques suivants :

| Composant                  | Choix retenu      |
| -------------------------- | ----------------- |
| Base de données            | PostgreSQL 17     |
| ORM                        | Hibernate         |
| Couche d'accès aux données | Spring Data JPA   |
| Gestion des migrations     | Flyway            |
| Gestion des dépendances    | Maven             |
| Configuration              | `application.yml` |

Cette architecture est conforme aux bonnes pratiques des applications Spring Boot professionnelles.

---

# 5. Composants de configuration

La configuration du backend est décomposée en plusieurs sous-chapitres.

| Document                           | Description                                 |
| ---------------------------------- | ------------------------------------------- |
| 03-02-PostgreSQL-Configuration.md  | Installation et configuration de PostgreSQL |
| 03-03-Application-Yml.md           | Configuration de Spring Boot                |
| 03-04-Maven-Dependencies.md        | Dépendances Maven du projet                 |
| 03-05-Flyway-Integration.md        | Intégration de Flyway                       |
| 03-06-First-Application-Startup.md | Premier démarrage de l'application          |

Chaque document pourra être consulté indépendamment.

---

# 6. Ordre de mise en œuvre

Les étapes seront réalisées dans l'ordre suivant :

```text
PostgreSQL Installation
        │
        ▼
Database Creation
        │
        ▼
Application Configuration
        │
        ▼
Maven Dependencies
        │
        ▼
Flyway Configuration
        │
        ▼
First Migration
        │
        ▼
Spring Boot Startup
```

Chaque étape dépend de la précédente et doit être validée avant de poursuivre.

---

# 7. Résultat attendu

À la fin de cette phase de configuration :

* PostgreSQL est opérationnel ;
* une base de données dédiée au projet est disponible ;
* Spring Boot se connecte correctement à cette base ;
* Flyway applique automatiquement les migrations ;
* Hibernate valide le schéma existant ;
* le backend démarre sans erreur.

Le projet est alors prêt à accueillir les premières entités JPA et la couche métier.

---

# 8. Documents associés

| Document                            | Description                    |
| ----------------------------------- | ------------------------------ |
| 01-Spring-Boot-Project-Setup.md     | Création du projet Spring Boot |
| 02-Git-Repository-Setup.md          | Préparation du dépôt Git       |
| ../database/07-Flyway-Migrations.md | Stratégie de migrations Flyway |
| 03-02-PostgreSQL-Configuration.md   | Configuration PostgreSQL       |

---

# 9. Historique des révisions

| Version | Date       | Auteur             | Description          |
| ------- | ---------- | ------------------ | -------------------- |
| 1.0.0   | 31/07/2026 | Équipe Projet PCMS | Création du document |

