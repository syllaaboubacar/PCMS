# 03.13.00 – Introduction aux DTO (Data Transfer Objects)

> **Module 3 — Développement du Backend Spring Boot**  
> **Chapitre 3.13 — Les DTO (Data Transfer Objects)**  
> **Partie 0 — Introduction**

---

# Objectifs

À la fin de cette partie, nous comprendrons :

- le rôle des **DTO (Data Transfer Objects)** dans une application Spring Boot ;
- pourquoi les entités JPA ne doivent pas être exposées directement ;
- comment structurer les DTO dans une architecture **Package by Feature** ;
- les différents types de DTO utilisés selon les opérations métier ;
- les bonnes pratiques de conception d'une API REST professionnelle.

Cette partie constitue la passerelle entre la couche de persistance et la future couche REST du projet **PCMS**.

---

# Contexte

Jusqu'à présent, nous avons construit l'ensemble de la couche de persistance :

- Base de données PostgreSQL ;
- migrations Flyway ;
- entités JPA ;
- repositories Spring Data JPA ;
- tests de persistance.

Notre modèle métier est maintenant capable de stocker et de récupérer les données.

La prochaine étape consiste à exposer ces données à travers une API REST.

Avant de développer les contrôleurs et les services, il est indispensable d'introduire une couche intermédiaire destinée à échanger les données avec les clients.

Cette couche est constituée des **DTO**.

---

# Qu'est-ce qu'un DTO ?

DTO signifie **Data Transfer Object**.

Un DTO est un objet Java dont le seul rôle est de transporter des données entre deux couches de l'application.

Contrairement à une entité JPA, un DTO :

- ne représente pas une table de la base de données ;
- ne possède pas de comportement métier ;
- n'est pas géré par Hibernate ;
- n'est jamais persisté directement.

Il sert uniquement à échanger des informations entre le client et le serveur.

---

# Pourquoi utiliser des DTO ?

Une erreur fréquente consiste à exposer directement les entités JPA dans les API REST.

Par exemple :

```java
@GetMapping("/{id}")
public User getUser(@PathVariable Long id) {
    return userRepository.findById(id).orElseThrow();
}
```

Cette approche paraît simple, mais elle présente de nombreux inconvénients.

En exposant directement une entité :

- toutes les propriétés deviennent visibles ;
- des informations sensibles peuvent être divulguées ;
- les relations JPA peuvent provoquer des boucles infinies de sérialisation ;
- toute modification du modèle interne impacte immédiatement l'API.

L'application devient alors difficile à maintenir et à faire évoluer.

---

# Le rôle des DTO dans l'architecture

Les DTO assurent une séparation claire entre le modèle métier et l'API REST.

Le flux d'une requête devient :

```text
Client REST
      │
      ▼
Request DTO
      │
      ▼
Controller
      │
      ▼
Service
      │
      ▼
Entity
      │
      ▼
Repository
      │
      ▼
PostgreSQL
```

Pour la réponse :

```text
PostgreSQL
      │
      ▼
Repository
      │
      ▼
Entity
      │
      ▼
Service
      │
      ▼
Response DTO
      │
      ▼
Client REST
```

Les entités restent donc confinées à l'intérieur de l'application.

Le client ne manipule que des DTO.

---

# Les avantages des DTO

L'utilisation de DTO apporte de nombreux bénéfices.

## Sécurité

Les données sensibles restent internes à l'application.

Par exemple :

- mot de passe ;
- informations d'audit ;
- identifiants techniques ;
- relations internes.

Ces informations ne sont jamais envoyées au client.

---

## Validation des données

Les DTO permettent d'appliquer facilement les contraintes de validation.

Par exemple :

```java
@NotBlank
private String firstName;

@Email
private String email;
```

Les données sont ainsi validées avant même d'atteindre la logique métier.

---

## Stabilité de l'API

Le modèle interne peut évoluer sans modifier le contrat de l'API.

Par exemple, il est possible d'ajouter un nouvel attribut dans une entité sans impacter les applications clientes.

---

## Réduction des données échangées

Le client ne reçoit que les informations réellement utiles.

Cela réduit :

- la taille des réponses HTTP ;
- le temps de transfert ;
- la charge réseau.

---

## Maintenabilité

Chaque DTO possède une responsabilité clairement définie.

Les contrôleurs, les services et les entités restent découplés.

L'application est plus simple à maintenir et à faire évoluer.

---

# Architecture retenue pour PCMS

Le projet conserve l'organisation **Package by Feature**.

Chaque fonctionnalité possède son propre package `dto`.

```text
src/main/java
└── lu
    └── police
        └── pcms
            ├── role
            │   ├── dto
            │   ├── entity
            │   ├── repository
            │   └── ...
            │
            ├── department
            │   ├── dto
            │   ├── entity
            │   ├── repository
            │   └── ...
            │
            ├── user
            │   ├── dto
            │   ├── entity
            │   ├── repository
            │   └── ...
            │
            └── ...
```

Cette organisation garantit une excellente cohérence avec l'architecture globale du projet.

---

# Convention de nommage

Afin de conserver une API homogène, les DTO respecteront une convention stricte.

## DTO de création

Utilisés pour les requêtes **POST**.

Exemples :

```text
CreateRoleRequest

CreateDepartmentRequest

CreateUserRequest

CreateCaseFileRequest
```

Ils contiennent uniquement les informations nécessaires à la création d'une ressource.

---

## DTO de mise à jour

Utilisés pour les requêtes **PUT** ou **PATCH**.

Exemples :

```text
UpdateRoleRequest

UpdateDepartmentRequest

UpdateUserRequest

UpdateCaseFileRequest
```

Ils ne contiennent que les champs pouvant être modifiés.

---

## DTO de réponse

Renvoyés au client après une opération.

Exemples :

```text
RoleResponse

DepartmentResponse

UserResponse

CaseFileResponse
```

Ils représentent le contrat de sortie de l'API.

---

## DTO de résumé

Dans certaines situations, il n'est pas nécessaire de retourner un objet complet.

Nous utiliserons alors des DTO plus légers.

Exemples :

```text
UserSummaryResponse

CaseFileSummaryResponse

SuspectSummaryResponse
```

Ils sont particulièrement adaptés :

- aux listes ;
- aux recherches ;
- aux références entre objets.

---

# Organisation par fonctionnalité

Prenons l'exemple du module **User**.

```text
user
├── dto
│   ├── CreateUserRequest.java
│   ├── UpdateUserRequest.java
│   ├── UserResponse.java
│   └── UserSummaryResponse.java
│
├── entity
├── repository
├── service
├── mapper
└── controller
```

Chaque fonctionnalité adoptera exactement la même structure.

---

# Les modules concernés

Tous les domaines métier disposeront de leurs propres DTO.

| Module | Create | Update | Response | Summary |
|---------|:------:|:------:|:--------:|:-------:|
| Role | ✅ | ✅ | ✅ | — |
| Department | ✅ | ✅ | ✅ | — |
| User | ✅ | ✅ | ✅ | ✅ |
| CaseFile | ✅ | ✅ | ✅ | ✅ |
| CaseAssignment | ✅ | ✅ | ✅ | — |
| Suspect | ✅ | ✅ | ✅ | ✅ |
| Attachment | ✅ | ✅ | ✅ | ✅ |
| CaseComment | ✅ | ✅ | ✅ | — |
| AuditLog | — | — | ✅ | ✅ |

Cette organisation répond aux besoins actuels du projet tout en restant évolutive.

---

# Pourquoi plusieurs DTO pour une même entité ?

Prenons l'exemple d'un utilisateur.

L'entité `User` contient des informations qui ne doivent jamais être exposées directement :

- mot de passe ;
- informations d'audit ;
- identifiant technique ;
- relations JPA.

Selon le contexte, les besoins sont différents.

Pour créer un utilisateur :

```text
CreateUserRequest
```

Pour modifier un utilisateur :

```text
UpdateUserRequest
```

Pour retourner un utilisateur au client :

```text
UserResponse
```

Pour afficher uniquement une liste :

```text
UserSummaryResponse
```

Chaque DTO possède donc une responsabilité unique.

---

# Préparation à MapStruct

Dans les chapitres suivants, nous introduirons **MapStruct**.

Son rôle sera de convertir automatiquement :

```text
CreateUserRequest
        │
        ▼
      User
```

ou encore :

```text
User
      │
      ▼
UserResponse
```

Grâce à cette séparation, le code de conversion restera simple, lisible et facilement maintenable.

---

# Bonnes pratiques

Pour le projet **PCMS**, nous appliquerons les principes suivants :

- ne jamais exposer directement une entité JPA ;
- créer des DTO spécialisés selon les cas d'utilisation ;
- limiter chaque DTO aux données réellement nécessaires ;
- appliquer les validations Jakarta Validation sur les DTO d'entrée ;
- conserver une convention de nommage homogène ;
- préparer l'intégration de MapStruct pour automatiser les conversions.

---

# Ce qu'il faut retenir

À l'issue de cette introduction, il faut retenir les points essentiels suivants :

- Un DTO est un objet destiné au transport des données entre le client et le serveur.
- Les entités JPA ne doivent jamais être exposées directement dans une API REST.
- Les DTO améliorent la sécurité, la maintenabilité et la stabilité de l'application.
- Chaque fonctionnalité possède son propre package `dto`, conformément à l'architecture **Package by Feature**.
- Plusieurs types de DTO sont utilisés selon le contexte : création, mise à jour, réponse ou résumé.
- Cette organisation prépare naturellement l'utilisation de **MapStruct** dans les chapitres suivants.

---

# Prochaine partie

Nous commencerons la mise en œuvre des DTO avec le module **Role**, qui servira de référence pour l'ensemble du projet.

Nous créerons notamment :

- `CreateRoleRequest`
- `UpdateRoleRequest`
- `RoleResponse`

et nous appliquerons les premières validations avec **Jakarta Validation**, tout en préparant leur conversion vers les entités grâce à **MapStruct**.
