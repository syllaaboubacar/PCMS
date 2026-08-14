# 03.11.00 – Tests Spring Data JPA (@DataJpaTest)

> **Module 3 — Développement du Backend Spring Boot**  
> **Chapitre 3.11 — Tests Spring Data JPA**  
> **Partie 0 — Introduction**

---

# Objectifs

À la fin de ce chapitre, nous serons capables de :

- comprendre le rôle des tests de persistance dans une application Spring Boot ;
- utiliser **@DataJpaTest** pour tester les repositories ;
- vérifier que les mappings JPA fonctionnent correctement ;
- valider les contraintes définies dans PostgreSQL ;
- tester les méthodes fournies par Spring Data JPA ;
- préparer une base solide pour les tests d'intégration avec PostgreSQL et Testcontainers.

---

# Pourquoi tester les Repositories ?

Dans une application Spring Boot, les repositories constituent la couche d'accès aux données.

Ils représentent le point de contact entre :

- les entités JPA ;
- Hibernate ;
- PostgreSQL.

Avant même de développer les Services ou les API REST, il est indispensable de vérifier que cette couche fonctionne correctement.

Tester les repositories permet de détecter très tôt :

- des erreurs de mapping JPA ;
- des relations incorrectes entre les entités ;
- des contraintes SQL mal définies ;
- des requêtes Spring Data incorrectes ;
- des migrations Flyway incomplètes.

En validant cette couche dès le début du projet, nous réduisons considérablement le risque d'erreurs dans les couches supérieures.

---

# Pourquoi ne pas attendre les tests des Services ?

Une erreur fréquente consiste à ne tester les repositories qu'indirectement à travers les Services.

Cette approche complique le diagnostic.

Lorsqu'un test échoue, il devient difficile de déterminer si le problème provient :

- du Service ;
- du Repository ;
- d'Hibernate ;
- de PostgreSQL.

En testant chaque couche indépendamment, les erreurs sont beaucoup plus faciles à identifier et à corriger.

---

# L'approche retenue pour PCMS

Dans le projet **PCMS**, nous suivrons une stratégie progressive.

Nous commencerons par tester uniquement la couche de persistance.

Notre objectif est de valider que :

- les entités sont correctement mappées ;
- Hibernate dialogue correctement avec PostgreSQL ;
- Flyway construit correctement le schéma de la base ;
- les méthodes des repositories produisent les résultats attendus.

Une fois cette étape validée, nous pourrons développer les Services avec davantage de confiance.

---

# Pourquoi utiliser @DataJpaTest ?

Spring Boot fournit plusieurs annotations destinées aux tests.

Pour les repositories, l'annotation officielle est :

```java
@DataJpaTest
```

Cette annotation démarre uniquement les composants nécessaires aux tests de persistance.

Contrairement à **@SpringBootTest**, elle ne charge pas l'ensemble de l'application.

Seuls les composants suivants sont initialisés :

- les entités JPA ;
- Hibernate ;
- les repositories ;
- la DataSource ;
- l'EntityManager ;
- les transactions.

En revanche, elle ne charge pas :

- les contrôleurs REST ;
- les services ;
- Spring Security ;
- les composants Web ;
- la couche MVC.

Les tests sont donc beaucoup plus rapides tout en restant parfaitement représentatifs du comportement réel de la couche de persistance.

---

# Les technologies utilisées

Tout au long de ce chapitre, nous utiliserons les composants suivants.

| Technologie | Rôle |
|-------------|------|
| JUnit 5 | Framework de tests |
| Spring Boot Test | Intégration avec Spring Boot |
| Spring Data JPA | Tests des repositories |
| Hibernate | Persistance ORM |
| AssertJ | Assertions lisibles |
| Flyway | Construction automatique du schéma |
| TestEntityManager | Manipulation simplifiée des entités pendant les tests |

Dans un second temps, nous introduirons également :

- Testcontainers ;
- PostgreSQL réel dans un conteneur Docker.

---

# Organisation des tests

Le projet suit une architecture **Package by Feature**.

Les tests respecteront exactement cette organisation.

```text
src
└── test
    ├── java
    │
    └── lu
        └── police
            └── pcms
                ├── role
                │   └── repository
                │       └── RoleRepositoryTest.java
                │
                ├── department
                │   └── repository
                │       └── DepartmentRepositoryTest.java
                │
                ├── user
                │   └── repository
                │       └── UserRepositoryTest.java
                │
                ├── casefile
                │   └── repository
                │       └── CaseFileRepositoryTest.java
                │
                ├── caseassignment
                │   └── repository
                │       └── CaseAssignmentRepositoryTest.java
                │
                ├── suspect
                │   └── repository
                │       └── SuspectRepositoryTest.java
                │
                ├── attachment
                │   └── repository
                │       └── AttachmentRepositoryTest.java
                │
                ├── casecomment
                │   └── repository
                │       └── CaseCommentRepositoryTest.java
                │
                ├── audit
                │   └── repository
                │       └── AuditLogRepositoryTest.java
                │
                └── common
                    └── TestDataFactory.java
    │
    └── resources
        └── application-test.yml
```

Chaque fonctionnalité possède ainsi ses propres tests, ce qui garantit une excellente cohérence avec l'architecture du projet.

---

# Dépendances Maven

Les tests de persistance reposent sur la dépendance suivante :

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

Cette dépendance fournit notamment :

- JUnit 5 ;
- AssertJ ;
- Mockito ;
- Spring Test ;
- Spring Boot Test.

Plus tard, nous ajouterons également :

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

afin d'exécuter les tests sur une véritable instance PostgreSQL.

---

# Configuration dédiée aux tests

Les tests utiliseront un profil Spring spécifique.

Le fichier sera placé dans :

```text
src/test/resources/application-test.yml
```

Ce profil permettra d'isoler complètement la configuration des tests de celle utilisée pour le développement.

Dans un premier temps, ce fichier restera volontairement minimal, puis il sera enrichi progressivement au fur et à mesure du projet.

---

# Convention de nommage

Toutes les classes de tests suivront une convention identique.

Exemple :

```java
@DataJpaTest
class RoleRepositoryTest {

}
```

Les méthodes de test adopteront une nomenclature explicite.

Par exemple :

```text
shouldSaveRole()

shouldFindRoleByName()

shouldReturnEmptyWhenRoleDoesNotExist()

shouldCheckRoleExists()

shouldDeleteRole()

shouldCountRoles()
```

Cette convention facilite la lecture des rapports de tests et améliore la maintenabilité du projet.

---

# Ce que nous allons tester

Chaque repository sera testé de manière systématique.

Les opérations communes comprendront notamment :

- sauvegarde d'une entité ;
- recherche par identifiant ;
- recherche par méthode métier ;
- vérification d'existence ;
- comptage ;
- suppression.

Les repositories disposant de méthodes spécifiques feront également l'objet de tests adaptés.

Par exemple :

- recherche d'un utilisateur par e-mail ;
- recherche d'un dossier par numéro ;
- vérification d'une relation entre deux entités ;
- statistiques métier.

---

# Ordre de réalisation

Nous conserverons le même ordre que celui utilisé pour les entités et les repositories.

1. RoleRepositoryTest
2. DepartmentRepositoryTest
3. UserRepositoryTest
4. CaseFileRepositoryTest
5. CaseAssignmentRepositoryTest
6. SuspectRepositoryTest
7. AttachmentRepositoryTest
8. CaseCommentRepositoryTest
9. AuditLogRepositoryTest

Cette progression facilite la compréhension du projet et respecte les dépendances entre les différentes entités.

---

# Bonnes pratiques

Pour le projet **PCMS**, nous appliquerons les règles suivantes :

- un fichier de test par repository ;
- une responsabilité clairement définie par classe de test ;
- une méthode de test par comportement attendu ;
- des noms de tests explicites ;
- des données de test réutilisables via une fabrique commune ;
- un profil Spring dédié aux tests ;
- une évolution progressive vers des tests d'intégration avec PostgreSQL réel.

---

# Ce qu'il faut retenir

À l'issue de cette introduction, il faut retenir les points essentiels suivants :

- Les repositories doivent être testés indépendamment des Services.
- **@DataJpaTest** est l'annotation officielle de Spring Boot pour tester la couche de persistance.
- Les tests chargent uniquement les composants nécessaires à JPA, ce qui les rend rapides et fiables.
- Chaque fonctionnalité possède son propre ensemble de tests conformément à l'architecture **Package by Feature**.
- Les tests constituent une étape indispensable avant le développement des Services et des API REST.

---

# Prochaine partie

Dans la partie suivante, nous découvrirons en détail l'annotation **@DataJpaTest**.

Nous étudierons notamment :

- son fonctionnement interne ;
- les composants qu'elle charge ;
- ceux qu'elle exclut volontairement ;
- son cycle d'exécution ;
- les raisons pour lesquelles elle est recommandée par Spring Boot pour les tests des repositories.

Cette compréhension nous permettra ensuite de créer sereinement notre premier test : **RoleRepositoryTest**.
