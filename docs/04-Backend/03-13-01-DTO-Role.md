# Module 3 — Développement du Backend Spring Boot

# Partie 13 — Data Transfer Objects (DTO)

# Chapitre 13.1 — Les DTO du module Role

## Objectif du chapitre

Le module **Role** constitue le point de départ de notre couche **DTO (Data Transfer Object)**.

Son objectif est de mettre en place une architecture de DTO simple, cohérente et réutilisable qui servira de modèle pour l'ensemble des autres fonctionnalités du **Police Case Management System (PCMS)**.

À l'issue de ce chapitre, le module **Role** disposera de tous les objets nécessaires aux échanges entre l'API REST et la couche métier.

---

# 1. Pourquoi utiliser des DTO ?

Une bonne pratique fondamentale dans une application Spring Boot consiste à **ne jamais exposer directement les entités JPA**.

Les entités représentent le modèle interne de l'application tandis que les DTO définissent le contrat d'échange avec les clients REST.

Le flux des données devient ainsi :

```text
                Client REST
                     │
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
               Role Entity
                     │
                     ▼
            RoleRepository
                     │
                     ▼
                PostgreSQL
```

Au retour :

```text
PostgreSQL
      │
      ▼
RoleRepository
      │
      ▼
 Role Entity
      │
      ▼
 RoleService
      │
      ▼
 RoleResponse
      │
      ▼
 Client REST
```

Cette séparation améliore :

- la sécurité ;
- la maintenabilité ;
- la stabilité de l'API ;
- l'évolutivité du modèle métier.

---

# 2. Les DTO du module Role

Le module **Role** est volontairement très simple.

Il ne possède qu'une seule propriété métier :

```java
name
```

Trois DTO suffisent donc à couvrir les besoins de l'API.

```text
role
└── dto
    ├── CreateRoleRequest.java
    ├── UpdateRoleRequest.java
    └── RoleResponse.java
```

Contrairement à d'autres modules du projet, aucun **SummaryResponse** n'est nécessaire.

---

# 3. Les responsabilités de chaque DTO

Chaque DTO répond à un cas d'utilisation précis.

| DTO | Utilisation |
|------|-------------|
| CreateRoleRequest | Création d'un rôle |
| UpdateRoleRequest | Modification d'un rôle |
| RoleResponse | Réponse envoyée au client |

Cette séparation évite de mélanger les responsabilités et permet de faire évoluer chaque DTO indépendamment.

---

# 4. CreateRoleRequest

## Rôle

Ce DTO est utilisé lors de la création d'un nouveau rôle.

Il représente les données envoyées par le client dans une requête **HTTP POST**.

Exemple :

```http
POST /api/roles
```

Corps de la requête :

```json
{
  "name": "ROLE_ADMIN"
}
```

Il ne contient que les informations nécessaires à la création.

L'identifiant n'est jamais fourni par le client.

---

# 5. UpdateRoleRequest

## Rôle

Ce DTO est utilisé lors de la modification d'un rôle existant.

Il est transmis dans une requête **PUT** ou **PATCH**.

Exemple :

```http
PUT /api/roles/1
```

```json
{
  "name": "ROLE_MANAGER"
}
```

Comme pour la création, seules les données modifiables sont présentes.

---

# 6. RoleResponse

## Rôle

Ce DTO représente les données renvoyées au client.

Exemple :

```json
{
  "id": 1,
  "name": "ROLE_ADMIN"
}
```

Il contient uniquement les informations utiles au consommateur de l'API.

Les détails internes de l'entité restent cachés.

---

# 7. Organisation du module

L'architecture **Package by Feature** est conservée.

```text
src
└── main
    └── java
        └── lu
            └── police
                └── pcms
                    └── role
                        ├── controller
                        │
                        ├── dto
                        │   ├── CreateRoleRequest.java
                        │   ├── UpdateRoleRequest.java
                        │   └── RoleResponse.java
                        │
                        ├── entity
                        │
                        ├── mapper
                        │
                        ├── repository
                        │
                        └── service
```

Chaque fonctionnalité regroupe ainsi tous les composants qui lui sont propres.

---

# 8. Validation des données

Les DTO de requête utilisent **Jakarta Validation**.

Le champ métier du module Role est :

```java
private String name;
```

Plusieurs contraintes seront appliquées.

| Annotation | Objectif |
|------------|----------|
| @NotBlank | Interdire une valeur vide ou composée uniquement d'espaces |
| @Size(max = 50) | Limiter la longueur du nom |
| @Pattern(...) | Imposer le format `ROLE_XXX` |

Grâce à ces validations, les données incorrectes sont rejetées avant d'atteindre la couche métier.

---

# 9. Pourquoi utiliser les records Java ?

Tous les DTO du projet utiliseront les **records**, introduits avec Java 16 et pleinement supportés en Java 17.

Exemple :

```java
public record RoleResponse(
        Long id,
        String name
) {
}
```

Les records offrent plusieurs avantages :

- moins de code ;
- objets immuables ;
- meilleure lisibilité ;
- génération automatique des accesseurs ;
- génération automatique de `equals()`, `hashCode()` et `toString()`.

Ils sont particulièrement adaptés aux objets de transport de données.

---

# 10. Conventions du projet

Tous les DTO du PCMS respecteront les conventions suivantes.

## Convention de nommage

### Création

```text
CreateRoleRequest
```

### Mise à jour

```text
UpdateRoleRequest
```

### Réponse

```text
RoleResponse
```

Les noms sont explicites et décrivent précisément leur rôle.

---

## Documentation

Chaque DTO comportera une documentation **Javadoc**.

Elle précisera :

- son objectif ;
- son contexte d'utilisation ;
- les paramètres transportés.

---

## Validation

Les validations seront portées par les DTO de requête.

La logique métier restera exclusivement dans les Services.

---

## Immutabilité

Tous les DTO seront déclarés sous forme de **record**.

Ils seront donc immuables par conception.

---

# 11. Préparation pour MapStruct

Les DTO créés dans ce chapitre seront utilisés dans le prochain module consacré à **MapStruct**.

Les conversions suivront le schéma suivant :

```text
CreateRoleRequest
        │
        ▼
     Role Entity
        │
        ▼
  RoleResponse
```

Le mapping sera entièrement automatisé.

---

# 12. Pourquoi commencer par le module Role ?

Le module **Role** constitue un excellent point de départ.

Il présente plusieurs avantages :

- une seule propriété métier ;
- aucune relation JPA ;
- aucune collection ;
- aucune hiérarchie complexe ;
- aucun objet imbriqué.

Il permet donc de comprendre les principes fondamentaux des DTO avant d'aborder des modules plus riches comme :

- User ;
- CaseFile ;
- Suspect ;
- Attachment.

---

# 13. Bonnes pratiques retenues

Pour l'ensemble du projet PCMS, nous appliquerons les principes suivants :

- ne jamais exposer directement les entités JPA ;
- utiliser un DTO spécialisé pour chaque cas d'utilisation ;
- séparer les DTO de création, de mise à jour et de réponse ;
- utiliser des records Java ;
- appliquer Jakarta Validation sur les DTO de requête ;
- conserver des DTO simples, sans logique métier ;
- préparer les DTO pour une intégration avec MapStruct.

---

# 14. Bilan

À l'issue de ce chapitre, le module **Role** dispose d'une architecture DTO claire et cohérente.

Les bases de la couche d'échange entre l'API REST et le domaine métier sont désormais établies.

Cette organisation sera réutilisée dans l'ensemble des autres fonctionnalités du PCMS.

---

# 15. Prochaine étape

Le prochain chapitre sera consacré à la création du premier DTO du projet :

**CreateRoleRequest**.

Nous y définirons :

- la structure du record ;
- les annotations Jakarta Validation ;
- les contraintes de validation ;
- la documentation Javadoc ;
- les bonnes pratiques de conception d'un DTO de création dans une application Spring Boot professionnelle.
