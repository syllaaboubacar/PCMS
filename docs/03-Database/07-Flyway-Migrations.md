# 07 - Flyway Migrations

---

| Élément                  | Valeur                               |
| ------------------------ | ------------------------------------ |
| **Document**             | 07-Flyway-Migrations.md              |
| **Emplacement**          | `docs/database/`                     |
| **Projet**               | Police Case Management System (PCMS) |
| **Version**              | 1.0.0                                |
| **Statut**               | Draft                                |
| **Auteur**               | Équipe Projet PCMS                   |
| **Dernière mise à jour** | 30/07/2026                           |

---

# Table des matières

1. Objectif
2. Pourquoi utiliser Flyway ?
3. Principe de fonctionnement
4. Organisation des migrations
5. Convention de nommage
6. Cycle de vie d'une migration
7. Intégration avec Spring Boot
8. Hibernate et Flyway
9. Évolution du schéma
10. Données de référence
11. Jeu de données de démonstration
12. Bonnes pratiques
13. Documents associés
14. Historique des révisions

---

# 1. Objectif

Ce document décrit la stratégie de gestion des migrations de la base de données du **Police Case Management System (PCMS)** à l'aide de **Flyway**.

Flyway permet de gérer l'évolution du schéma de manière :

* versionnée ;
* reproductible ;
* automatisée ;
* sécurisée.

Toutes les modifications du schéma PostgreSQL seront réalisées au moyen de migrations versionnées.

---

# 2. Pourquoi utiliser Flyway ?

Une application évolue continuellement.

Au fil des versions, de nouvelles tables, colonnes ou contraintes sont ajoutées.

Sans outil de migration, chaque environnement devrait être mis à jour manuellement, ce qui augmente considérablement les risques d'erreur.

Flyway garantit que tous les environnements (développement, intégration, recette et production) appliquent exactement les mêmes évolutions, dans le même ordre.

---

# 3. Principe de fonctionnement

Chaque évolution du schéma est représentée par un fichier SQL versionné.

Exemple :

```text id="yq3r6x"
V1__Initial_schema.sql
        │
        ▼
V2__Reference_data.sql
        │
        ▼
V3__Indexes.sql
        │
        ▼
V4__Initial_admin.sql
        │
        ▼
V5__Sample_cases.sql
```

Chaque migration est exécutée **une seule fois**.

Lors du premier démarrage, Flyway crée automatiquement la table :

```text id="8hnvwi"
flyway_schema_history
```

Cette table enregistre notamment :

* la version ;
* la description ;
* la date d'exécution ;
* le résultat de l'exécution.

Flyway utilise cet historique pour déterminer quelles migrations doivent encore être appliquées.

---

# 4. Organisation des migrations

Les migrations seront stockées dans l'arborescence standard reconnue par Flyway.

```text id="jvydx5"
backend/
└── src/
    └── main/
        └── resources/
            ├── application.yml
            └── db/
                └── migration/
                    ├── V1__Initial_schema.sql
                    ├── V2__Reference_data.sql
                    ├── V3__Indexes.sql
                    ├── V4__Initial_admin.sql
                    └── V5__Sample_cases.sql
```

Cette structure est automatiquement détectée par Spring Boot.

---

# 5. Convention de nommage

Toutes les migrations respecteront la convention suivante :

```text id="kqvdr0"
V<version>__Description.sql
```

Exemples :

* `V1__Initial_schema.sql`
* `V2__Reference_data.sql`
* `V3__Indexes.sql`
* `V4__Initial_admin.sql`
* `V5__Sample_cases.sql`
* `V6__Add_phone_number_to_users.sql`

Les noms doivent être explicites afin de faciliter la lecture de l'historique du projet.

Les noms génériques tels que :

* `script.sql`
* `database.sql`
* `migration.sql`
* `new.sql`

sont à proscrire.

---

# 6. Cycle de vie d'une migration

Une migration suit le processus suivant :

```text id="zn5j9m"
Developer
      │
      ▼
Create new migration
      │
      ▼
Commit to Git
      │
      ▼
CI/CD Pipeline
      │
      ▼
Application Startup
      │
      ▼
Flyway Migration
      │
      ▼
Updated Database
```

Chaque nouvelle évolution du schéma correspond à une nouvelle migration.

Une migration déjà appliquée ne doit jamais être modifiée.

---

# 7. Intégration avec Spring Boot

Flyway est intégré directement au démarrage de l'application.

Configuration recommandée :

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration

  jpa:
    hibernate:
      ddl-auto: validate
```

Au démarrage de l'application :

1. Spring Boot initialise le contexte.
2. Flyway consulte `flyway_schema_history`.
3. Les nouvelles migrations sont exécutées.
4. Hibernate valide la correspondance entre les entités JPA et le schéma PostgreSQL.
5. L'application devient disponible.

---

# 8. Hibernate et Flyway

Dans le projet PCMS, Flyway est responsable de la création et de l'évolution de la base de données.

Hibernate ne doit pas modifier automatiquement le schéma.

La configuration suivante est donc retenue :

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

Cette approche présente plusieurs avantages :

* schéma entièrement versionné ;
* déploiements reproductibles ;
* historique complet des évolutions ;
* maîtrise des changements en production.

L'utilisation de `ddl-auto: update` est évitée dans les environnements de production, car elle ne fournit ni historique ni contrôle précis des évolutions.

---

# 9. Évolution du schéma

Lorsqu'une nouvelle exigence apparaît, une nouvelle migration est créée.

Exemple :

Nouvelle fonctionnalité :

> Ajouter un numéro de téléphone aux utilisateurs.

Nouvelle migration :

```text id="jwgjpl"
V6__Add_phone_number_to_users.sql
```

Exemple de contenu :

```sql
ALTER TABLE users
ADD COLUMN phone_number VARCHAR(30);
```

Les migrations précédentes restent inchangées.

---

# 10. Données de référence

Certaines données sont indispensables au fonctionnement de l'application.

Elles seront insérées dans une migration dédiée.

Exemple :

```text id="g2a9ps"
V2__Reference_data.sql
```

Cette migration pourra notamment créer :

## Rôles

* ROLE_ADMIN
* ROLE_MANAGER
* ROLE_OFFICER

## Départements

* CYBER
* TRAFFIC
* JUDICIAL
* HOMICIDE

Ces données sont relativement stables et adaptées à une migration de référence.

---

# 11. Jeu de données de démonstration

Une migration optionnelle pourra être utilisée pour alimenter la base avec des données réalistes.

Exemple :

```text id="r5xjcn"
V5__Sample_cases.sql
```

Elle pourra créer notamment :

* 5 utilisateurs ;
* 10 dossiers ;
* 15 suspects ;
* 20 commentaires ;
* 30 pièces jointes.

Ces données faciliteront :

* les démonstrations ;
* les tests fonctionnels ;
* les présentations du projet ;
* les entretiens techniques.

---

# 12. Bonnes pratiques

Les règles suivantes seront appliquées dans tout le projet.

* Une migration correspond à une évolution fonctionnelle.
* Une migration déjà exécutée n'est jamais modifiée.
* Les fichiers portent des noms explicites.
* Les migrations sont testées sur une base vierge.
* Toutes les migrations sont versionnées dans Git.
* Les données de référence sont séparées du schéma.
* Les données de démonstration restent optionnelles.

---

# 13. Documents associés

| Document                                   | Description                          |
| ------------------------------------------ | ------------------------------------ |
| 05-PostgreSQL-Types-Constraints-Indexes.md | Types SQL, contraintes et index      |
| 06-PostgreSQL-Schema.md                    | Schéma PostgreSQL                    |
| 10-Backend.md                              | Architecture backend *(à venir)*     |
| 12-Security.md                             | Architecture de sécurité *(à venir)* |

---

# 14. Historique des révisions

| Version | Date       | Auteur             | Description          |
| ------- | ---------- | ------------------ | -------------------- |
| 1.0.0   | 30/07/2026 | Équipe Projet PCMS | Création du document |

