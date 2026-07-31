# 03-03 - PostgreSQL Setup

---

| Élément                  | Valeur                                   |
| ------------------------ | ---------------------------------------- |
| **Document**             | 03-03-PostgreSQL-Setup.md                |
| **Emplacement**          | `docs/backend/03-Backend-Configuration/` |
| **Projet**               | Police Case Management System (PCMS)     |
| **Version**              | 1.0.0                                    |
| **Statut**               | Draft                                    |
| **Auteur**               | Équipe Projet PCMS                       |
| **Dernière mise à jour** | 31/07/2026                               |

---

# Table des matières

1. Objectif
2. Pourquoi PostgreSQL ?
3. Version retenue
4. Installation
5. Vérification de l'installation
6. Démarrage du service
7. Connexion à PostgreSQL
8. Création de l'utilisateur PCMS
9. Création de la base de données
10. Vérification de la configuration
11. Structure de la base
12. Bonnes pratiques
13. Résultat attendu
14. Documents associés
15. Historique des révisions

---

# 1. Objectif

Ce document décrit la préparation de PostgreSQL pour le projet **Police Case Management System (PCMS)**.

L'objectif est de disposer d'une base de données dédiée, prête à être utilisée par Spring Boot et Flyway.

À ce stade, aucune table métier n'est encore créée.

---

# 2. Pourquoi PostgreSQL ?

Le PCMS utilise PostgreSQL comme système de gestion de base de données relationnelle.

Ce choix repose sur plusieurs critères :

* logiciel Open Source ;
* excellente stabilité ;
* très bonnes performances ;
* conformité au standard SQL ;
* intégration native avec Spring Boot ;
* compatibilité avec Docker ;
* utilisation fréquente dans les projets professionnels.

Le modèle de données restera compatible avec d'autres SGBD relationnels (Oracle, SQL Server), mais PostgreSQL constitue la plateforme de référence du projet.

---

# 3. Version retenue

| Composant     | Version |
| ------------- | ------- |
| PostgreSQL    | 17      |
| Client `psql` | 17      |

Toutes les équipes de développement doivent utiliser une version compatible afin de garantir la reproductibilité des migrations Flyway.

---

# 4. Installation

Sous Ubuntu :

```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
```

Vérifier ensuite que PostgreSQL est correctement installé.

---

# 5. Vérification de l'installation

Afficher la version installée :

```bash
psql --version
```

Exemple :

```text
psql (PostgreSQL) 17.x
```

Vérifier également que le serveur est présent :

```bash
postgres --version
```

---

# 6. Démarrage du service

Vérifier l'état du service PostgreSQL :

```bash
sudo systemctl status postgresql
```

Démarrer le service si nécessaire :

```bash
sudo systemctl start postgresql
```

Activer le démarrage automatique :

```bash
sudo systemctl enable postgresql
```

Vérifier une nouvelle fois son état :

```bash
sudo systemctl status postgresql
```

Le service doit être indiqué comme **active (running)**.

---

# 7. Connexion à PostgreSQL

Se connecter avec l'utilisateur système PostgreSQL :

```bash
sudo -u postgres psql
```

Une fois connecté :

```sql
SELECT version();
```

Quitter ensuite le client :

```sql
\q
```

---

# 8. Création de l'utilisateur PCMS

Créer un utilisateur dédié au projet.

```sql
CREATE ROLE pcms
LOGIN
PASSWORD 'ChangeMe'
CREATEDB;
```

Contrôler les rôles existants :

```sql
\du
```

L'utilisateur `pcms` doit apparaître dans la liste.

> **Important**
>
> Le mot de passe utilisé dans cet exemple est uniquement destiné au développement local.
> En production, les identifiants seront gérés via des variables d'environnement ou un gestionnaire de secrets.

---

# 9. Création de la base de données

Créer la base dédiée au projet :

```sql
CREATE DATABASE pcms
OWNER pcms;
```

Lister les bases disponibles :

```sql
\l
```

Se connecter à la nouvelle base :

```sql
\c pcms
```

Vérifier la connexion :

```sql
SELECT current_database();
```

Résultat attendu :

```text
pcms
```

---

# 10. Vérification de la configuration

À ce stade :

* PostgreSQL est installé ;
* le service est démarré ;
* l'utilisateur `pcms` existe ;
* la base `pcms` est créée ;
* la connexion fonctionne correctement.

Aucune table métier n'est encore présente.

Cette étape sera réalisée automatiquement par Flyway.

---

# 11. Structure de la base

Après la création, la base est volontairement vide.

Les objets suivants seront créés lors des prochaines étapes :

* tables métier ;
* contraintes ;
* index ;
* séquences ;
* données de référence ;
* historique Flyway (`flyway_schema_history`).

Toutes ces opérations seront versionnées par Flyway.

---

# 12. Bonnes pratiques

Les règles suivantes s'appliquent au projet :

* utiliser un utilisateur dédié à l'application ;
* ne jamais utiliser l'utilisateur `postgres` dans Spring Boot ;
* créer une base dédiée au projet ;
* limiter les privilèges au strict nécessaire ;
* ne jamais stocker les mots de passe dans le code source ;
* laisser Flyway gérer toutes les évolutions du schéma.

---

# 13. Résultat attendu

À l'issue de cette étape :

* PostgreSQL est opérationnel ;
* la base `pcms` est disponible ;
* l'utilisateur `pcms` peut s'y connecter ;
* le serveur est prêt à recevoir les migrations Flyway.

Le backend peut désormais être configuré pour utiliser cette base de données.

---

# 14. Documents associés

| Document                               | Description                        |
| -------------------------------------- | ---------------------------------- |
| 03-01-Backend-Configuration.md         | Vue d'ensemble de la configuration |
| 03-02-Backend-Environment.md           | Préparation de l'environnement     |
| 03-04-Application-Yml.md               | Configuration Spring Boot          |
| ../../database/06-PostgreSQL-Schema.md | Schéma PostgreSQL                  |
| ../../database/07-Flyway-Migrations.md | Stratégie de migrations            |

---

# 15. Historique des révisions

| Version | Date       | Auteur             | Description          |
| ------- | ---------- | ------------------ | -------------------- |
| 1.0.0   | 31/07/2026 | Équipe Projet PCMS | Création du document |

