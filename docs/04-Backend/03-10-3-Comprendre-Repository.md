# Chapitre 3.10 — Création des Repository Spring Data JPA
## Partie 2 — Comprendre JpaRepository

## Objectif

Dans la partie précédente, nous avons découvert que **Spring Data JPA** génère automatiquement les implémentations des repositories.

Avant de créer nos propres repositories, il est indispensable de comprendre l'interface que nous allons utiliser dans l'ensemble du projet :

```java
JpaRepository<T, ID>
```

À la fin de cette partie, tu comprendras :

- le rôle de `JpaRepository` ;
- la signification des paramètres génériques `T` et `ID` ;
- les interfaces dont elle hérite ;
- les principales méthodes qu'elle met à disposition ;
- pourquoi elle constitue la base de la couche de persistance de notre application.

---

# 1. Qu'est-ce que JpaRepository ?

`JpaRepository` est une interface fournie par **Spring Data JPA**.

Elle permet d'effectuer les opérations classiques de persistance sur une entité JPA sans écrire de code SQL ni implémenter manuellement une classe DAO.

Il suffit de déclarer une interface comme celle-ci :

```java
public interface UserRepository
        extends JpaRepository<User, Long> {
}
```

Au démarrage de l'application, Spring Boot génère automatiquement une implémentation complète.

---

# 2. La déclaration de JpaRepository

La définition simplifiée est la suivante :

```java
public interface JpaRepository<T, ID>
        extends ListCrudRepository<T, ID>,
                ListPagingAndSortingRepository<T, ID>,
                QueryByExampleExecutor<T> {
}
```

Dans la pratique, nous retiendrons surtout les deux paramètres génériques :

- `T`
- `ID`

---

# 3. Le paramètre T

Le premier paramètre représente **le type de l'entité gérée par le repository**.

Exemple :

```java
public interface UserRepository
        extends JpaRepository<User, Long> {
}
```

Ici :

```
T = User
```

Toutes les méthodes manipuleront donc des objets `User`.

Exemples :

```java
User user = repository.save(user);

Optional<User> user =
        repository.findById(1L);

List<User> users =
        repository.findAll();
```

Le repository est entièrement spécialisé pour l'entité `User`.

---

# 4. Le paramètre ID

Le second paramètre représente **le type de la clé primaire**.

Dans notre projet PCMS, toutes les tables utilisent :

```sql
BIGINT GENERATED ALWAYS AS IDENTITY
```

En Java, cela correspond au type :

```java
Long
```

Nous écrivons donc :

```java
JpaRepository<User, Long>
```

Si une entité utilisait un identifiant UUID, nous écririons :

```java
JpaRepository<User, UUID>
```

Le type `ID` doit toujours correspondre au type du champ annoté `@Id`.

---

# 5. Application au projet PCMS

Toutes nos entités héritent de `BaseEntity`.

Cette classe définit un identifiant technique :

```java
private Long id;
```

Par conséquent, tous les repositories auront la même structure.

| Entité | Repository |
|---------|------------|
| Role | `JpaRepository<Role, Long>` |
| Department | `JpaRepository<Department, Long>` |
| User | `JpaRepository<User, Long>` |
| CaseFile | `JpaRepository<CaseFile, Long>` |
| CaseAssignment | `JpaRepository<CaseAssignment, Long>` |
| Suspect | `JpaRepository<Suspect, Long>` |
| Attachment | `JpaRepository<Attachment, Long>` |
| CaseComment | `JpaRepository<CaseComment, Long>` |
| AuditLog | `JpaRepository<AuditLog, Long>` |

Cette homogénéité simplifie le développement et la maintenance.

---

# 6. Les interfaces héritées

`JpaRepository` hérite de plusieurs interfaces spécialisées.

```
Repository
      │
      ▼
CrudRepository
      │
      ▼
ListCrudRepository
      │
      ▼
PagingAndSortingRepository
      │
      ▼
ListPagingAndSortingRepository
      │
      ▼
JpaRepository
```

Chaque niveau apporte de nouvelles fonctionnalités :

| Interface | Rôle |
|-----------|------|
| Repository | Interface marqueur |
| CrudRepository | Opérations CRUD de base |
| ListCrudRepository | Retour de collections sous forme de `List` |
| PagingAndSortingRepository | Pagination et tri |
| ListPagingAndSortingRepository | Pagination avec retour sous forme de `List` |
| JpaRepository | Fonctionnalités JPA avancées |

---

# 7. Les principales méthodes fournies

Sans écrire la moindre implémentation, nous disposons immédiatement de nombreuses méthodes.

## Création ou mise à jour

```java
save(entity)
```

Enregistre ou met à jour une entité.

---

## Recherche par identifiant

```java
findById(id)
```

Retourne un :

```java
Optional<T>
```

---

## Recherche de toutes les entités

```java
findAll()
```

Retourne toutes les lignes de la table.

---

## Suppression

```java
delete(entity)

deleteById(id)
```

Supprime une entité.

---

## Vérification

```java
existsById(id)
```

Indique si une entité existe.

---

## Comptage

```java
count()
```

Retourne le nombre total d'enregistrements.

---

# 8. Pourquoi findById() retourne un Optional ?

La signature de la méthode est :

```java
Optional<T> findById(ID id);
```

Pourquoi ne retourne-t-elle pas directement une entité ?

Parce que l'identifiant recherché peut ne pas exister.

Exemple :

```java
Optional<User> user =
        repository.findById(100L);
```

Si aucun utilisateur ne possède cet identifiant :

- aucune exception n'est levée automatiquement ;
- aucune valeur `null` n'est renvoyée.

Le développeur est invité à traiter explicitement ce cas.

Par exemple :

```java
User user = repository.findById(id)
        .orElseThrow(
            () -> new EntityNotFoundException()
        );
```

Cette approche limite les erreurs de type `NullPointerException`.

---

# 9. Les grandes catégories de méthodes

Les méthodes proposées par `JpaRepository` peuvent être regroupées selon leur rôle.

| Catégorie | Exemples |
|-----------|----------|
| Création | `save()` |
| Lecture | `findById()`, `findAll()` |
| Suppression | `delete()`, `deleteById()` |
| Vérification | `existsById()` |
| Comptage | `count()` |
| Pagination | `findAll(Pageable)` |
| Tri | `findAll(Sort)` |

Ces méthodes couvrent la majorité des besoins d'une application métier.

---

# 10. Pourquoi utiliser JpaRepository ?

Sans Spring Data JPA, il faudrait implémenter ces opérations pour chaque entité.

Par exemple :

```
RoleDao

DepartmentDao

UserDao

CaseFileDao

...
```

Chaque classe contiendrait des méthodes similaires :

- save()
- findById()
- findAll()
- delete()

Avec neuf entités, cela représenterait déjà plusieurs centaines de lignes de code répétitif.

`JpaRepository` élimine cette duplication et permet au développeur de se concentrer sur la logique métier.

---

# 11. Ce que nous utiliserons dans PCMS

Dans les prochains chapitres, nous exploiterons principalement les méthodes suivantes :

```java
save()

findById()

findAll()

deleteById()

existsById()

count()
```

Nous ajouterons ensuite des méthodes dérivées comme :

```java
findByEmail(String email);

existsByEmail(String email);

findByCaseNumber(String caseNumber);

findByRole(Role role);

findByDepartment(Department department);
```

Enfin, lorsque les besoins deviendront plus spécifiques, nous utiliserons des requêtes personnalisées avec `@Query`.

---

# 12. Les bonnes pratiques

Pour le projet PCMS, nous appliquerons les règles suivantes :

- Un repository par agrégat métier.
- Les repositories restent focalisés sur l'accès aux données.
- Aucune logique métier dans les repositories.
- Les contrôleurs n'accèdent jamais directement aux repositories.
- Les repositories sont utilisés exclusivement par les services.
- Privilégier les méthodes dérivées avant d'écrire des requêtes personnalisées.
- Utiliser `Optional<T>` pour les recherches pouvant ne retourner aucun résultat.

---

# Ce qu'il faut retenir

À l'issue de cette partie :

- `JpaRepository` est une interface générique de Spring Data JPA.
- Le paramètre `T` représente l'entité manipulée.
- Le paramètre `ID` représente le type de la clé primaire.
- Spring génère automatiquement l'implémentation des repositories.
- Les opérations CRUD sont disponibles sans écrire de SQL.
- `Optional` permet de gérer proprement l'absence de résultat.
- Les méthodes spécifiques seront ajoutées directement dans nos interfaces de repository.

---

# Conclusion

Nous comprenons désormais le fonctionnement de `JpaRepository` et la signification de ses paramètres génériques.

Nous sommes prêts à créer notre premier repository du projet.

La prochaine étape sera l'implémentation de **RoleRepository**, qui nous permettra de découvrir la structure d'un repository Spring Data JPA et d'ajouter nos premières méthodes métier telles que :

- `findByName(String name)`
- `existsByName(String name)`

Ces méthodes seront utilisées par la couche **Service** lors de la gestion des rôles dans le Police Case Management System (PCMS).
