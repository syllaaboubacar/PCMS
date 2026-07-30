# 01 - Introduction

---

| Élément                  | Valeur                               |
| ------------------------ | ------------------------------------ |
| **Document**             | 01-Introduction.md                   |
| **Projet**               | Police Case Management System (PCMS) |
| **Version**              | 1.0.0                                |
| **Statut**               | Draft                                |
| **Auteur**               | Équipe Projet PCMS                   |
| **Dernière mise à jour** | 29/07/2026                           |

---

# Table des matières

1. Introduction
2. Contexte du projet
3. Présentation du projet
4. Vision
5. Objectifs
6. Public cible
7. Périmètre du projet
8. Principes directeurs
9. Documents associés
10. Historique des révisions

---

# 1. Introduction

Le **Police Case Management System (PCMS)** est un projet de gestion de dossiers de police conçu dans une démarche d'Analyste-Développeur.

Au-delà du développement d'une application web, ce projet a pour objectif de démontrer la maîtrise des différentes étapes du cycle de vie d'un logiciel professionnel :

* analyse des besoins ;
* conception fonctionnelle ;
* conception technique ;
* développement Full Stack ;
* documentation ;
* déploiement ;
* maintenance.

Le projet est volontairement dimensionné pour rester accessible tout en mettant en œuvre les pratiques et les technologies couramment utilisées dans les projets d'entreprise.

---

# 2. Contexte du projet

La gestion des dossiers constitue une activité essentielle au sein des services de police.

Les informations relatives aux enquêtes, aux policiers, aux suspects et à l'historique des actions doivent être centralisées, sécurisées et facilement consultables.

Dans ce contexte, le projet **PCMS** propose la réalisation d'une application web moderne permettant de gérer ces informations au travers d'une architecture logicielle modulaire et évolutive.

---

# 3. Présentation du projet

Le projet consiste à développer une plateforme permettant notamment de :

* authentifier les utilisateurs ;
* gérer les dossiers de police ;
* gérer les policiers ;
* gérer les suspects ;
* consulter un tableau de bord ;
* rechercher des informations ;
* historiser les modifications réalisées sur les dossiers.

L'application repose sur une architecture client/serveur utilisant un frontend Angular, un backend Spring Boot et une base de données PostgreSQL.

---

# 4. Vision

La vision du projet est de produire une application démontrant les compétences attendues d'un Analyste-Développeur.

Le projet met particulièrement l'accent sur :

* une architecture claire ;
* un code maintenable ;
* une documentation complète ;
* une séparation des responsabilités ;
* l'application des bonnes pratiques de développement ;
* une architecture facilement extensible.

Cette approche permet de préparer l'évolution du système sans remettre en cause son architecture globale.

---

# 5. Objectifs

Les principaux objectifs du projet sont les suivants :

## Objectifs fonctionnels

* Centraliser la gestion des dossiers.
* Simplifier le suivi des enquêtes.
* Gérer les policiers.
* Gérer les suspects.
* Consulter des tableaux de bord.
* Assurer la traçabilité des actions.

## Objectifs techniques

* Concevoir une architecture modulaire.
* Développer une API REST sécurisée.
* Mettre en œuvre une authentification JWT.
* Utiliser PostgreSQL comme base de données.
* Industrialiser le projet avec Docker.
* Documenter l'ensemble de la solution.

---

# 6. Public cible

La documentation s'adresse principalement aux :

* chefs de projet ;
* analystes fonctionnels ;
* architectes logiciels ;
* développeurs Frontend ;
* développeurs Backend ;
* administrateurs système ;
* ingénieurs DevOps ;
* testeurs ;
* futurs mainteneurs de l'application.

---

# 7. Périmètre du projet

La première version du projet couvre les domaines suivants :

* authentification ;
* tableau de bord ;
* gestion des dossiers ;
* gestion des policiers ;
* gestion des suspects ;
* historique des actions ;
* recherche d'informations.

Les intégrations avec des services externes (gestion d'identité centralisée, service de messagerie, stockage de fichiers ou journalisation centralisée) sont envisagées comme des évolutions futures de l'architecture et seront documentées dans les chapitres dédiés.

---

# 8. Principes directeurs

Le projet est guidé par les principes suivants :

* simplicité de conception ;
* séparation claire des responsabilités ;
* modularité ;
* évolutivité ;
* maintenabilité ;
* sécurité ;
* documentation continue.

La documentation est élaborée parallèlement au développement afin d'assurer la traçabilité des choix fonctionnels et techniques tout au long du projet.

---

# 9. Documents associés

Les documents suivants complètent cette introduction :

| Document                    | Description                           |
| --------------------------- | ------------------------------------- |
| README.md                   | Présentation générale du projet       |
| 02-Business-Requirements.md | Besoins métier                        |
| 03-Business-Rules.md        | Règles métier                         |
| 04-Use-Cases.md             | Cas d'utilisation                     |
| 05-System-Architecture.md   | Architecture logicielle               |
| 06-C4-Context.md            | Diagramme de contexte (C4 - Niveau 1) |

---

# 10. Historique des révisions

| Version | Date       | Auteur             | Description          |
| ------- | ---------- | ------------------ | -------------------- |
| 1.0.0   | 29/07/2026 | Équipe Projet PCMS | Création du document |

