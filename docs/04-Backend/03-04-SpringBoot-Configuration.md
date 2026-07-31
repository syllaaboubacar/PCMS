# 03-04 - Spring Boot Configuration

---

| Élément                  | Valeur                                   |
| ------------------------ | ---------------------------------------- |
| **Document**             | 03-04-SpringBoot-Configuration.md        |
| **Emplacement**          | `docs/backend/03-Backend-Configuration/` |
| **Projet**               | Police Case Management System (PCMS)     |
| **Version**              | 1.0.0                                    |
| **Statut**               | Draft                                    |
| **Auteur**               | Équipe Projet PCMS                       |
| **Dernière mise à jour** | 31/07/2026                               |

---

# Table des matières

1. Objectif
2. Le rôle de `application.yml`
3. Pourquoi choisir YAML ?
4. Organisation du fichier
5. Configuration du serveur HTTP
6. Configuration de la DataSource
7. Explication détaillée des propriétés
8. Bonnes pratiques de sécurité
9. Configuration actuelle
10. Résultat attendu
11. Documents associés
12. Historique des révisions

---

# 1. Objectif

Ce document décrit la première configuration du fichier `application.yml` du **Police Case Management System (PCMS)**.

L'objectif est de fournir une configuration minimale, claire et fonctionnelle permettant à Spring Boot de :

* démarrer correctement ;
* préparer la connexion à PostgreSQL ;
* initialiser la `DataSource` ;
* servir de base aux futures configurations JPA, Hibernate et Flyway.

À ce stade, seules les propriétés indispensables sont définies.

---

# 2. Le rôle de `application.yml`

Le fichier :

```text
backend/
└── src/
    └── main/
        └── resources/
            └── application.yml
```

est le fichier principal de configuration de Spring Boot.

Il permet de centraliser l'ensemble des paramètres de l'application :

* configuration HTTP ;
* connexion à la base de données ;
* propriétés Spring ;
* profils d'exécution ;
* journalisation ;
* gestion des migrations ;
* paramètres de production.

Spring Boot charge automatiquement ce fichier au démarrage.

---

# 3. Pourquoi choisir YAML ?

Spring Boot accepte deux formats :

* `application.properties`
* `application.yml`

Le projet PCMS utilise **YAML**.

## Comparaison

### Format Properties

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/pcms
spring.datasource.username=pcms_app
spring.datasource.password=********
```

### Format YAML

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/pcms
    username: pcms_app
    password: ********
```

## Avantages du format YAML

* structure hiérarchique ;
* meilleure lisibilité ;
* moins de répétitions ;
* plus simple à maintenir ;
* particulièrement adapté aux projets Spring Boot de grande taille.

Pour ces raisons, YAML est retenu comme standard de configuration du projet.

---

# 4. Organisation du fichier

À terme, le fichier `application.yml` sera organisé autour des blocs suivants :

```yaml
server:

spring:

logging:

management:
```

Chaque section possède une responsabilité unique :

| Section      | Rôle                                   |
| ------------ | -------------------------------------- |
| `server`     | Configuration du serveur HTTP embarqué |
| `spring`     | Configuration des composants Spring    |
| `logging`    | Gestion des journaux d'application     |
| `management` | Actuator et supervision                |

Dans ce chapitre, seuls les blocs `server` et `spring.datasource` sont abordés.

---

# 5. Configuration du serveur HTTP

Le serveur embarqué Tomcat écoute sur le port **8080**.

```yaml
server:
  port: 8080
```

## Pourquoi le port 8080 ?

Le projet utilise les ports suivants :

| Service     | Port |
| ----------- | ---: |
| Spring Boot | 8080 |
| Angular     | 4200 |
| PostgreSQL  | 5432 |

Cette répartition évite les conflits et facilite le développement local.

---

# 6. Configuration de la DataSource

La DataSource permet à Spring Boot d'établir une connexion avec PostgreSQL.

Configuration retenue :

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/pcms
    username: pcms_app
    password: PcmsDev2026!
    driver-class-name: org.postgresql.Driver
```

Cette configuration est volontairement minimale afin de faciliter les premiers tests de démarrage.

---

# 7. Explication détaillée des propriétés

## `url`

```yaml
url: jdbc:postgresql://localhost:5432/pcms
```

Décomposition :

| Élément      | Signification                              |
| ------------ | ------------------------------------------ |
| `jdbc`       | Java Database Connectivity                 |
| `postgresql` | Type de base de données                    |
| `localhost`  | Serveur local                              |
| `5432`       | Port PostgreSQL                            |
| `pcms`       | Base de données utilisée par l'application |

---

## `username`

```yaml
username: pcms_app
```

Compte dédié à l'application.

L'utilisation d'un utilisateur spécifique permet :

* d'isoler les droits ;
* de limiter les privilèges ;
* d'améliorer la sécurité.

---

## `password`

```yaml
password: PcmsDev2026!
```

Mot de passe associé au compte applicatif.

Dans l'environnement de développement, cette valeur peut être définie directement dans le fichier de configuration.

Cette approche sera remplacée en production par un mécanisme sécurisé.

---

## `driver-class-name`

```yaml
driver-class-name: org.postgresql.Driver
```

Cette propriété indique explicitement le pilote JDBC utilisé.

Bien que Spring Boot puisse généralement le détecter automatiquement, sa déclaration présente plusieurs avantages :

* configuration explicite ;
* meilleure lisibilité ;
* diagnostic facilité en cas d'erreur ;
* comportement identique sur tous les environnements.

---

# 8. Bonnes pratiques de sécurité

La configuration présentée est adaptée au développement local.

En revanche, les bonnes pratiques imposent qu'en production :

* aucun mot de passe ne soit stocké dans Git ;
* les identifiants proviennent de variables d'environnement ;
* les secrets soient externalisés (Vault, Kubernetes Secrets, etc.) ;
* les profils Spring (`dev`, `test`, `prod`) utilisent des configurations distinctes.

Cette stratégie sera mise en place dans les chapitres consacrés aux profils d'exécution et au déploiement.

---

# 9. Configuration actuelle

Le contenu complet du fichier `application.yml` est actuellement le suivant :

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/pcms
    username: pcms_app
    password: PcmsDev2026!
    driver-class-name: org.postgresql.Driver
```

À ce stade, aucune autre propriété n'est ajoutée.

Les configurations JPA, Hibernate, Flyway, Logging et Actuator seront introduites progressivement dans les chapitres suivants.

---

# 10. Résultat attendu

À l'issue de cette étape :

* Spring Boot connaît le port HTTP à utiliser ;
* la connexion PostgreSQL est définie ;
* le pilote JDBC est explicitement déclaré ;
* la configuration reste simple, lisible et évolutive.

Le projet est désormais prêt à intégrer la configuration de JPA, Hibernate et Flyway.

---

# 11. Documents associés

| Document                             | Description                                |
| ------------------------------------ | ------------------------------------------ |
| 03-01-Backend-Configuration.md       | Introduction à la configuration            |
| 03-02-Backend-Environment.md         | Préparation de l'environnement             |
| 03-03-PostgreSQL-Setup.md            | Installation de PostgreSQL                 |
| 03-05-JPA-Hibernate-Configuration.md | Configuration JPA et Hibernate *(à venir)* |
| 03-06-Flyway-Integration.md          | Intégration de Flyway *(à venir)*          |

---

# 12. Historique des révisions

| Version | Date       | Auteur             | Description          |
| ------- | ---------- | ------------------ | -------------------- |
| 1.0.0   | 31/07/2026 | Équipe Projet PCMS | Création du document |

