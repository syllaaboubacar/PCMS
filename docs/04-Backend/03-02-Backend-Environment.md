# 03-02 - Backend Environment

---

| Élément                  | Valeur                                   |
| ------------------------ | ---------------------------------------- |
| **Document**             | 03-02-Backend-Environment.md             |
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
3. Environnement cible
4. Logiciels requis
5. Versions retenues
6. Variables d'environnement
7. Vérification de l'installation
8. Arborescence de travail
9. Bonnes pratiques
10. Résultat attendu
11. Documents associés
12. Historique des révisions

---

# 1. Objectif

Ce document décrit l'environnement de développement nécessaire au backend du **Police Case Management System (PCMS)**.

Avant toute configuration de Spring Boot ou de PostgreSQL, il est indispensable que chaque développeur dispose d'un environnement identique afin de garantir la reproductibilité des développements et des tests.

---

# 2. Présentation

Le backend du PCMS est développé avec une stack moderne basée sur Java et Spring Boot.

L'environnement doit permettre de :

* développer l'application ;
* exécuter les tests ;
* lancer les migrations Flyway ;
* communiquer avec PostgreSQL ;
* préparer le déploiement via Docker.

Tous les membres de l'équipe doivent utiliser des versions compatibles des outils afin de limiter les problèmes liés aux différences d'environnement.

---

# 3. Environnement cible

Le projet est développé principalement sous Linux.

| Élément                           | Valeur                                       |
| --------------------------------- | -------------------------------------------- |
| Système d'exploitation recommandé | Ubuntu 24.04 LTS                             |
| Compatible                        | Ubuntu 22.04+, Debian, Windows (WSL2), macOS |
| Architecture                      | x86_64                                       |
| Encodage                          | UTF-8                                        |

L'utilisation de WSL2 est recommandée pour les développeurs travaillant sous Windows.

---

# 4. Logiciels requis

Les outils suivants sont nécessaires avant de commencer le développement.

| Logiciel                       | Utilisation                    |
| ------------------------------ | ------------------------------ |
| Git                            | Gestion du code source         |
| Java 21 (JDK)                  | Développement Spring Boot      |
| Maven 3.9+                     | Gestion des dépendances        |
| PostgreSQL                     | Base de données                |
| Docker Desktop / Docker Engine | Conteneurisation               |
| Visual Studio Code             | Environnement de développement |
| Postman ou Bruno               | Tests des API REST             |

---

# 5. Versions retenues

Les versions suivantes sont utilisées dans le projet.

| Composant          | Version                        |
| ------------------ | ------------------------------ |
| Java               | 21 LTS                         |
| Spring Boot        | 3.x                            |
| Maven              | 3.9+                           |
| PostgreSQL         | 17                             |
| Flyway             | Version intégrée à Spring Boot |
| Docker             | Dernière version stable        |
| Git                | Dernière version stable        |
| Visual Studio Code | Dernière version stable        |

Ces versions doivent être harmonisées entre tous les développeurs.

---

# 6. Variables d'environnement

Certaines variables système doivent être configurées.

## JAVA_HOME

```text
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
```

## PATH

Le répertoire contenant Java et Maven doit être présent dans le `PATH`.

Exemple :

```text
$JAVA_HOME/bin
```

Les informations sensibles (identifiants PostgreSQL, clés JWT, etc.) ne doivent jamais être stockées directement dans le code source. Elles seront externalisées via les profils Spring Boot ou des variables d'environnement.

---

# 7. Vérification de l'installation

Les commandes suivantes permettent de vérifier que l'environnement est correctement installé.

## Java

```bash
java --version
```

## Maven

```bash
mvn --version
```

## Git

```bash
git --version
```

## Docker

```bash
docker --version
```

## PostgreSQL

```bash
psql --version
```

Chaque commande doit retourner la version installée sans erreur.

---

# 8. Arborescence de travail

Le dépôt du projet est organisé de la manière suivante :

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
├── .gitignore
├── .editorconfig
└── .gitattributes
```

Le développement backend est réalisé exclusivement dans le dossier `backend/`.

---

# 9. Bonnes pratiques

Les règles suivantes s'appliquent à l'ensemble de l'équipe :

* utiliser les mêmes versions des outils ;
* privilégier les versions LTS ;
* conserver un environnement reproductible ;
* ne jamais stocker de secrets dans Git ;
* maintenir les outils à jour selon les versions validées par le projet.

---

# 10. Résultat attendu

À l'issue de cette étape :

* tous les outils sont installés ;
* les versions sont compatibles avec le projet ;
* les commandes principales fonctionnent correctement ;
* chaque développeur dispose d'un environnement identique.

Le projet est désormais prêt pour la configuration de PostgreSQL et de Spring Boot.

---

# 11. Documents associés

| Document                          | Description                                 |
| --------------------------------- | ------------------------------------------- |
| 03-01-Backend-Configuration.md    | Introduction à la configuration du backend  |
| 03-03-PostgreSQL-Configuration.md | Installation et configuration de PostgreSQL |
| 03-04-Application-Yml.md          | Configuration de Spring Boot                |
| 01-Spring-Boot-Project-Setup.md   | Création du projet Spring Boot              |

---

# 12. Historique des révisions

| Version | Date       | Auteur             | Description          |
| ------- | ---------- | ------------------ | -------------------- |
| 1.0.0   | 31/07/2026 | Équipe Projet PCMS | Création du document |

