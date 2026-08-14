# Module 3 — Développement du Backend Spring Boot

# Partie 13 — Data Transfer Objects (DTO)

# Chapitre 13.1.1 — CreateRoleRequest

## Objectif du chapitre

Le **CreateRoleRequest** représente les données envoyées par le client lors de la création d'un nouveau rôle.

Il constitue le point d'entrée de l'API REST pour l'opération :

```http
POST /api/roles
```

Le contrôleur REST ne reçoit jamais directement une entité **Role**. Il reçoit uniquement une instance de **CreateRoleRequest**, qui sera ensuite validée puis transformée en entité métier.

Cette approche garantit une séparation claire entre la couche de présentation (API REST) et la couche de persistance.

---

# 1. Cycle de vie d'une requête de création

Lorsqu'un client souhaite créer un nouveau rôle, les données suivent le parcours suivant :

```text
                Client REST
                     │
                     │ POST /api/roles
                     ▼
           CreateRoleRequest
                     │
                     ▼
             RoleController
                     │
                     ▼
              RoleService
                     │
                     ▼
               RoleMapper
                     │
                     ▼
               Role Entity
                     │
                     ▼
            RoleRepository
                     │
                     ▼
                PostgreSQL
```

Chaque couche possède une responsabilité précise.

| Couche | Responsabilité |
|---------|----------------|
| Client REST | Envoie les données |
| CreateRoleRequest | Transporte et valide les données |
| Controller | Reçoit la requête HTTP |
| Service | Applique les règles métier |
| Mapper | Convertit le DTO en entité |
| Repository | Persiste les données |
| PostgreSQL | Stocke le rôle |

---

# 2. Pourquoi utiliser un DTO ?

Une entité JPA contient souvent des informations qui ne doivent jamais être fournies par un client.

Prenons l'exemple de l'entité **Role**.

Sans DTO, un client pourrait envoyer :

```json
{
    "id": 1,
    "name": "ROLE_ADMIN",
    "createdAt": "2026-01-01T08:00:00",
    "updatedAt": "2026-01-01T08:00:00",
    "deleted": false
}
```

Cette approche présente plusieurs problèmes.

- l'identifiant est généré par la base de données ;
- les champs d'audit sont gérés automatiquement par l'application ;
- certains champs techniques ne doivent jamais être modifiables par un client.

Le DTO limite volontairement les informations acceptées.

Le client ne peut fournir que les données réellement nécessaires à la création.

---

# 3. Les données attendues

Pour créer un rôle, une seule propriété métier est nécessaire :

```text
name
```

Exemple de requête :

```json
{
    "name": "ROLE_ADMIN"
}
```

Cette simplicité permet de limiter les risques d'erreur et de rendre l'API plus lisible.

---

# 4. Validation des données

Avant même l'exécution de la logique métier, les données sont validées grâce à **Jakarta Validation**.

Les contraintes suivantes sont appliquées.

| Annotation | Rôle |
|------------|------|
| `@NotBlank` | Interdit une valeur vide ou composée uniquement d'espaces |
| `@Size(max = 50)` | Limite la longueur du nom |
| `@Pattern(...)` | Impose le format `ROLE_XXX` |

Grâce à cette validation déclarative, les données invalides sont rejetées automatiquement par Spring Boot avant d'atteindre la couche Service.

---

# 5. Valeurs autorisées

Les exemples suivants illustrent le comportement attendu.

## Valeurs valides

| Valeur | Résultat |
|---------|----------|
| ROLE_ADMIN | ✅ |
| ROLE_USER | ✅ |
| ROLE_MANAGER | ✅ |
| ROLE_SUPERVISOR | ✅ |
| ROLE_MANAGER_2 | ✅ |

---

## Valeurs invalides

| Valeur | Raison |
|---------|--------|
| ADMIN | Préfixe `ROLE_` absent |
| role_admin | Lettres minuscules |
| roleUser | Format incorrect |
| ROLE ADMIN | Contient un espace |
| ROLE-ADMIN | Caractère `-` interdit |

---

# 6. Pourquoi utiliser un record ?

Depuis Java 16, les **records** permettent de représenter très simplement des objets destinés au transport de données.

Ils sont parfaitement adaptés aux DTO.

Par rapport à une classe classique, un record offre plusieurs avantages :

- immutabilité ;
- réduction importante du code ;
- meilleure lisibilité ;
- génération automatique des accesseurs ;
- génération automatique de `equals()`, `hashCode()` et `toString()` ;
- excellente intégration avec Spring Boot et Jackson.

Pour cette raison, tous les DTO du projet PCMS seront implémentés sous forme de **record**.

---

# 7. Implémentation

```java
package lu.police.pcms.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Représente les données nécessaires à la création
 * d'un rôle dans le Police Case Management System (PCMS).
 *
 * <p>
 * Ce DTO est utilisé par les requêtes HTTP POST.
 * Il contient uniquement les informations que le client
 * est autorisé à fournir.
 * </p>
 *
 * @param name nom du rôle au format ROLE_XXX
 */
public record CreateRoleRequest(

        @NotBlank(message = "Role name is required.")
        @Size(
                max = 50,
                message = "Role name must not exceed 50 characters."
        )
        @Pattern(
                regexp = "^ROLE_[A-Z0-9_]+$",
                message = "Role name must follow the format ROLE_XXX."
        )
        String name

) {
}
```

---

# 8. Comprendre l'expression régulière

La validation repose sur l'expression régulière suivante :

```text
^ROLE_[A-Z0-9_]+$
```

Décomposition :

| Élément | Signification |
|----------|---------------|
| `^` | Début de la chaîne |
| `ROLE_` | Préfixe obligatoire |
| `[A-Z0-9_]+` | Une ou plusieurs lettres majuscules, chiffres ou underscores |
| `$` | Fin de la chaîne |

Cette expression garantit une convention homogène pour tous les rôles du système.

---

# 9. Pourquoi valider dans le DTO ?

La validation est effectuée dès l'entrée de l'application.

Cette approche présente plusieurs avantages :

- les données invalides sont rejetées immédiatement ;
- le Service ne traite que des données conformes ;
- les règles de validation sont centralisées ;
- le code métier reste plus simple.

Le Service peut ainsi se concentrer exclusivement sur les règles fonctionnelles.

---

# 10. Bonnes pratiques appliquées

Le **CreateRoleRequest** respecte les bonnes pratiques adoptées pour l'ensemble du projet PCMS.

- ✅ DTO dédié exclusivement à la création.
- ✅ Objet immuable grâce au mot-clé `record`.
- ✅ Validation déclarative avec Jakarta Validation.
- ✅ Documentation Javadoc complète.
- ✅ Aucune dépendance envers JPA.
- ✅ Aucune logique métier.
- ✅ Structure compatible avec MapStruct.

---

# 11. Résultat

Le module **role** contient désormais son premier DTO.

```text
role
└── dto
    └── CreateRoleRequest.java
```

Ce DTO constitue le point d'entrée de l'API REST pour toutes les opérations de création de rôles.

---

# 12. Prochaine étape

Le prochain sous-chapitre sera consacré à **UpdateRoleRequest**.

Sa structure sera très proche de **CreateRoleRequest**, mais il sera destiné aux opérations de modification (`PUT` et `PATCH`) et préparera le module **Role** à la mise en œuvre complète des opérations CRUD.
