# 02 - Git Repository Setup

---

| Élément                  | Valeur                               |
| ------------------------ | ------------------------------------ |
| **Document**             | 02-Git-Repository-Setup.md           |
| **Emplacement**          | `docs/backend/`                      |
| **Projet**               | Police Case Management System (PCMS) |
| **Version**              | 1.0.0                                |
| **Statut**               | Draft                                |
| **Auteur**               | Équipe Projet PCMS                   |
| **Dernière mise à jour** | 31/07/2026                           |

---

# Table des matières

1. Objectif
2. Pourquoi commencer par Git ?
3. Structure du dépôt
4. Initialisation du dépôt local
5. Configuration de l'identité Git
6. Création du dépôt GitHub
7. Configuration de l'authentification SSH
8. Association du dépôt distant
9. Premier commit
10. Bonnes pratiques Git
11. Résultat attendu
12. Documents associés
13. Historique des révisions

---

# 1. Objectif

Ce document décrit la préparation du dépôt Git du **Police Case Management System (PCMS)** avant le début du développement.

L'objectif est de disposer d'un dépôt :

* propre ;
* versionné dès le premier jour ;
* prêt pour le développement collaboratif ;
* compatible avec les futures intégrations CI/CD.

Cette approche garantit une traçabilité complète de l'évolution du projet.

---

# 2. Pourquoi commencer par Git ?

Dans un projet professionnel, le dépôt Git est créé avant toute implémentation.

Le processus retenu pour PCMS est le suivant :

```text
Create Git Repository
        │
        ▼
Configure Git
        │
        ▼
Create Project Structure
        │
        ▼
Generate Spring Boot Project
        │
        ▼
First Commit
```

Cette démarche permet de conserver l'historique complet du projet dès sa création et facilite la collaboration entre les membres de l'équipe.

---

# 3. Structure du dépôt

À ce stade, l'arborescence du projet est la suivante :

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

Cette organisation sépare clairement :

* le backend Spring Boot ;
* le frontend Angular ;
* la documentation ;
* les diagrammes d'architecture ;
* les ressources de déploiement.

Une structure modulaire facilite la maintenance et les évolutions futures.

---

# 4. Initialisation du dépôt local

Depuis le répertoire racine du projet :

```bash
cd ~/MyProjects/PCMS
```

Vérifier le répertoire courant :

```bash
pwd
```

Si Git n'est pas encore initialisé :

```bash
git init
git branch -M main
```

Vérifier ensuite l'état du dépôt :

```bash
git status
```

Résultat attendu :

```text
On branch main
No commits yet
```

---

# 5. Configuration de l'identité Git

Configurer une identité Git cohérente est indispensable afin que chaque commit soit correctement attribué.

Vérification :

```bash
git config --global user.name
git config --global user.email
```

Configuration si nécessaire :

```bash
git config --global user.name "Aboubacar Sylla"
git config --global user.email "your.email@example.com"
```

> **Bonne pratique**
>
> Utiliser la même adresse électronique que celle associée au compte GitHub afin d'assurer le lien entre les commits et le profil GitHub.

---

# 6. Création du dépôt GitHub

Créer un nouveau dépôt sur GitHub avec les paramètres suivants :

| Paramètre       | Valeur recommandée              |
| --------------- | ------------------------------- |
| Repository Name | `PCMS`                          |
| Description     | `Police Case Management System` |
| Visibility      | Public                          |

Ne pas demander à GitHub de créer :

* `README.md`
* `.gitignore`
* `LICENSE`

Ces fichiers sont déjà présents dans le dépôt local.

---

# 7. Configuration de l'authentification SSH

L'utilisation de SSH est recommandée pour éviter la saisie répétée des identifiants GitHub.

## Vérifier l'existence d'une clé SSH

```bash
ls ~/.ssh
```

Si les fichiers suivants existent :

```text
id_ed25519
id_ed25519.pub
```

une clé est déjà disponible.

Sinon, générer une nouvelle paire de clés :

```bash
ssh-keygen -t ed25519 -C "your.email@example.com"
```

Afficher ensuite la clé publique :

```bash
cat ~/.ssh/id_ed25519.pub
```

Ajouter cette clé dans GitHub :

```text
GitHub
└── Settings
    └── SSH and GPG keys
        └── New SSH key
```

Tester enfin la connexion :

```bash
ssh -T git@github.com
```

Exemple de résultat :

```text
Hi <username>!
You've successfully authenticated.
```

---

# 8. Association du dépôt distant

Ajouter le dépôt GitHub comme dépôt distant :

```bash
git remote add origin git@github.com:<username>/PCMS.git
```

Vérifier la configuration :

```bash
git remote -v
```

Résultat attendu :

```text
origin    git@github.com:<username>/PCMS.git (fetch)
origin    git@github.com:<username>/PCMS.git (push)
```

---

# 9. Premier commit

Une fois le dépôt prêt :

```bash
git add .
git commit -m "Initialize PCMS repository"
```

Publier ensuite la branche principale :

```bash
git push -u origin main
```

Ce premier commit constitue le point de départ officiel du projet.

---

# 10. Bonnes pratiques Git

Les règles suivantes seront appliquées pendant tout le développement du PCMS.

* Initialiser Git avant de développer.
* Utiliser la branche `main` comme branche stable.
* Réaliser un commit par fonctionnalité.
* Écrire des messages de commit explicites.
* Utiliser l'authentification SSH.
* Versionner la documentation au même titre que le code.
* Préparer le dépôt pour une intégration continue (CI/CD).

Les fichiers suivants seront ajoutés dans les chapitres suivants afin d'harmoniser l'environnement de développement :

* `.gitignore`
* `.editorconfig`
* `.gitattributes`

Le répertoire `.github/workflows/` sera également créé afin d'accueillir les futures pipelines GitHub Actions.

---

# 11. Résultat attendu

À l'issue de ce chapitre, le projet dispose :

* d'un dépôt Git initialisé ;
* d'un dépôt GitHub associé ;
* d'une authentification SSH opérationnelle ;
* d'une branche principale `main` ;
* d'un premier commit versionné.

Le dépôt est désormais prêt à accueillir le développement du backend, du frontend et de la documentation.

---

# 12. Documents associés

| Document                        | Description                                                   |
| ------------------------------- | ------------------------------------------------------------- |
| 01-Spring-Boot-Project-Setup.md | Mise en place du projet Spring Boot                           |
| 03-Git-Configuration-Files.md   | `.gitignore`, `.editorconfig` et `.gitattributes` *(à venir)* |
| ../16-Git-Workflow.md           | Stratégie Git du projet                                       |
| ../13-Deployment.md             | Déploiement et CI/CD *(à venir)*                              |

---

# 13. Historique des révisions

| Version | Date       | Auteur             | Description          |
| ------- | ---------- | ------------------ | -------------------- |
| 1.0.0   | 31/07/2026 | Équipe Projet PCMS | Création du document |

