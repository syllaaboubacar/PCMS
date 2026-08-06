# 03.10.4 – RoleRepository

## Objectifs

À l'issue de cette partie, nous aurons :

- créé le premier repository Spring Data JPA du projet ;
- compris son rôle dans l'architecture du PCMS ;
- ajouté les premières méthodes de recherche métier ;
- appliqué les bonnes pratiques de Spring Data JPA.

---

# 1. Rôle de `RoleRepository`

Le `RoleRepository` est responsable de l'accès aux données de l'entité **Role**.

Il constitue l'unique point d'accès à la table :

```text
roles
```

Conformément à notre architecture, les règles métier seront implémentées dans la couche **Service**. Le repository se limite aux opérations de persistance.

Il sera notamment utilisé pour :

- la création des utilisateurs ;
- l'attribution des rôles ;
- l'authentification avec Spring Security ;
- l'administration des rôles ;
- la validation des données ;
- le chargement des données de référence.

---

# 2. Emplacement dans l'architecture

Avec l'approche **Package by Feature**, chaque fonctionnalité possède son propre repository.

```text
src/
└── main/
    └── java/
        └── lu/
            └── police/
                └── pcms/
                    └── role/
                        ├── entity/
                        │   └── Role.java
                        │
                        └── repository/
                            └── RoleRepository.java
```

Cette organisation favorise une meilleure cohésion fonctionnelle et facilite la maintenance lorsque le projet évolue.

---

# 3. Création du repository

Le repository est une simple interface qui hérite de `JpaRepository`.

```java
package lu.police.pcms.role.repository;

import lu.police.pcms.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

}
```

Aucune implémentation n'est nécessaire.

Au démarrage de l'application, Spring Data JPA génère automatiquement une implémentation concrète.

---

# 4. Les méthodes héritées automatiquement

Grâce à `JpaRepository<Role, Long>`, les opérations CRUD standards sont immédiatement disponibles.

Les principales méthodes sont :

| Méthode | Description |
|----------|-------------|
| `save(role)` | Crée ou met à jour un rôle |
| `findById(id)` | Recherche un rôle par identifiant |
| `findAll()` | Retourne tous les rôles |
| `delete(role)` | Supprime un rôle |
| `deleteById(id)` | Supprime un rôle par identifiant |
| `existsById(id)` | Vérifie l'existence d'un rôle |
| `count()` | Retourne le nombre total de rôles |

Ces méthodes couvrent déjà une grande partie des besoins courants.

---

# 5. Analyse des besoins métier

Avant d'ajouter de nouvelles méthodes, il est important d'identifier les cas d'utilisation réels du projet.

Dans le PCMS, un rôle sera principalement recherché :

- lors de la création d'un utilisateur ;
- lors de la modification d'un utilisateur ;
- pendant l'authentification ;
- lors des opérations d'administration ;
- lors de la validation des données.

Les critères de recherche réellement utiles sont :

- l'identifiant (`id`) ;
- le nom (`name`).

Il est inutile d'ajouter des méthodes qui ne correspondent à aucun besoin métier.

---

# 6. Ajout des méthodes dérivées

Nous enrichissons maintenant le repository avec deux méthodes spécifiques.

```java
Optional<Role> findByName(String name);

boolean existsByName(String name);
```

Ces méthodes sont automatiquement interprétées par Spring Data JPA.

Aucune requête SQL n'est nécessaire.

---

# 7. Implémentation complète

```java
package lu.police.pcms.role.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import lu.police.pcms.role.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Recherche un rôle par son nom.
     *
     * @param name le nom du rôle
     * @return le rôle correspondant, s'il existe
     */
    Optional<Role> findByName(String name);

    /**
     * Vérifie l'existence d'un rôle portant ce nom.
     *
     * @param name le nom du rôle
     * @return true si le rôle existe
     */
    boolean existsByName(String name);

}
```

Cette interface est directement exploitable dans les futurs services.

---

# 8. Pourquoi utiliser `Optional<Role>` ?

Un rôle peut ne pas exister.

Par exemple :

```text
ROLE_SUPER_ADMIN
```

Si aucun rôle ne correspond, retourner `null` obligerait le développeur à effectuer des vérifications systématiques.

Avec `Optional<Role>`, l'absence de résultat est explicite.

Exemple :

```java
Role role = roleRepository.findByName("ROLE_ADMIN")
        .orElseThrow(() ->
                new RoleNotFoundException("Role not found."));
```

Cette approche réduit le risque de `NullPointerException` et améliore la lisibilité du code.

---

# 9. Génération automatique des requêtes

Spring Data JPA analyse le nom des méthodes.

Ainsi :

```java
findByName(String name)
```

est automatiquement traduit en une requête équivalente à :

```sql
SELECT *
FROM roles
WHERE name = ?;
```

De même,

```java
existsByName(String name)
```

correspond approximativement à :

```sql
SELECT EXISTS (
    SELECT 1
    FROM roles
    WHERE name = ?
);
```

Le développeur n'a donc pas besoin d'écrire le SQL.

---

# 10. Pourquoi ne pas ajouter davantage de méthodes ?

Il serait possible d'ajouter des méthodes telles que :

```java
findAllByOrderByNameAsc();

findByDescription(String description);

findAllByDeletedFalse();
```

Cependant, ces méthodes ne répondent actuellement à aucun besoin métier identifié.

Dans le PCMS :

- le tri pourra être réalisé avec `Sort` ;
- la recherche par description n'est pas utilisée ;
- la gestion de la suppression logique sera traitée globalement dans un chapitre dédié.

Nous appliquons donc le principe suivant :

> **Ne créer que les méthodes réellement nécessaires.**

---

# 11. Exemple d'utilisation future

Dans la couche Service, le repository sera utilisé de manière similaire à l'exemple suivant.

```java
Role role = roleRepository.findByName("ROLE_OFFICER")
        .orElseThrow(() ->
                new RoleNotFoundException("Role not found."));
```

Ou encore :

```java
if (roleRepository.existsByName(roleName)) {
    throw new DuplicateRoleException(
            "Role already exists.");
}
```

Le repository reste concentré sur l'accès aux données.

La logique métier appartient exclusivement à la couche Service.

---

# 12. Bonnes pratiques

Pour l'ensemble du projet PCMS, nous appliquerons les principes suivants :

- créer un repository par entité ;
- hériter systématiquement de `JpaRepository<Entité, Long>` ;
- privilégier les méthodes dérivées (`findBy...`, `existsBy...`) lorsque cela suffit ;
- éviter les annotations `@Query` tant qu'une méthode dérivée répond au besoin ;
- ne créer que des méthodes correspondant à un véritable cas d'utilisation métier ;
- conserver les repositories simples et centrés sur l'accès aux données.

---

# Résumé

Dans cette partie, nous avons :

- créé notre premier repository Spring Data JPA ;
- compris son rôle dans l'architecture du projet ;
- ajouté deux méthodes métier (`findByName` et `existsByName`) ;
- vu comment Spring Data JPA génère automatiquement les requêtes SQL ;
- appliqué les bonnes pratiques de conception des repositories.

Le `RoleRepository` constitue désormais une base solide qui sera utilisée par les futurs services, contrôleurs REST et mécanismes d'authentification du PCMS.

---

## Prochaine étape

Dans la partie suivante, nous créerons le **DepartmentRepository**.

Nous y appliquerons les mêmes principes en ajoutant les méthodes métier adaptées à la gestion des départements, notamment :

- `findByCode(...)`
- `findByName(...)`
- `existsByCode(...)`
- `existsByName(...)`

Ces méthodes seront utilisées lors de l'administration des départements et de l'affectation des utilisateurs.
