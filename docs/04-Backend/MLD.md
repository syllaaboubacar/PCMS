Excellent. 🚀

Nous arrivons à une étape charnière du projet.

Jusqu'à présent, nous avons conçu la base de données sur le papier :

✅ MCD
✅ MLD
✅ ERD
✅ Types SQL
✅ Contraintes
✅ Index

À partir de maintenant, nous allons produire un véritable schéma PostgreSQL professionnel, exactement comme le ferait une équipe de développement avant le début du codage.

Ce script SQL sera ensuite repris par Flyway sans pratiquement aucune modification.

MODULE 2 – Conception de la base de données
Chapitre 7 – Schéma PostgreSQL complet

Objectif : produire un schéma SQL professionnel, propre, maintenable et directement compatible avec Spring Boot, Hibernate et Flyway.

1. Notre philosophie

Nous allons respecter plusieurs règles.

Règle 1

Créer les tables dans le bon ordre.

Pourquoi ?

Parce que PostgreSQL ne peut pas créer une clé étrangère vers une table qui n'existe pas encore.

Notre ordre sera :

roles
↓
departments
↓
users
↓
cases
↓
case_assignments
↓
suspects
↓
attachments
↓
case_comments
↓
audit_logs
2. Création de la table roles
CREATE TABLE roles (

    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    name VARCHAR(50) NOT NULL,

    description VARCHAR(255),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),

    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(100),

    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_roles_name UNIQUE(name)

);

Pourquoi :

GENERATED ALWAYS AS IDENTITY

Parce que c'est la méthode moderne recommandée par PostgreSQL.

Nous n'utiliserons pas SERIAL.

3. Table departments
CREATE TABLE departments (

    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    code VARCHAR(20) NOT NULL,

    name VARCHAR(100) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),

    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(100),

    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_departments_code UNIQUE(code),
    CONSTRAINT uk_departments_name UNIQUE(name)

);
4. Table users
CREATE TABLE users (

    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    first_name VARCHAR(100) NOT NULL,

    last_name VARCHAR(100) NOT NULL,

    email VARCHAR(255) NOT NULL,

    password VARCHAR(255) NOT NULL,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    role_id BIGINT NOT NULL,

    department_id BIGINT NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),

    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(100),

    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_users_email UNIQUE(email),

    CONSTRAINT fk_users_role
        FOREIGN KEY(role_id)
        REFERENCES roles(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_users_department
        FOREIGN KEY(department_id)
        REFERENCES departments(id)
        ON DELETE RESTRICT

);
5. Table cases
CREATE TABLE cases (

    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    case_number VARCHAR(30) NOT NULL,

    title VARCHAR(255) NOT NULL,

    description TEXT NOT NULL,

    status VARCHAR(30) NOT NULL,

    priority VARCHAR(20) NOT NULL,

    opened_at TIMESTAMPTZ NOT NULL,

    closed_at TIMESTAMPTZ,

    incident_date DATE,

    location VARCHAR(255),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),

    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(100),

    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_cases_number UNIQUE(case_number),

    CONSTRAINT chk_case_status
        CHECK (
            status IN (
                'OPEN',
                'IN_PROGRESS',
                'ON_HOLD',
                'CLOSED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT chk_case_priority
        CHECK (
            priority IN (
                'LOW',
                'MEDIUM',
                'HIGH',
                'CRITICAL'
            )
        )

);
6. Table case_assignments
CREATE TABLE case_assignments (

    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    case_id BIGINT NOT NULL,

    user_id BIGINT NOT NULL,

    assigned_at TIMESTAMPTZ NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),

    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(100),

    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_assignment
        UNIQUE(case_id, user_id),

    CONSTRAINT fk_assignment_case
        FOREIGN KEY(case_id)
        REFERENCES cases(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_assignment_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT

);
7. Table suspects
CREATE TABLE suspects (

    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    case_id BIGINT NOT NULL,

    first_name VARCHAR(100) NOT NULL,

    last_name VARCHAR(100) NOT NULL,

    birth_date DATE,

    nationality VARCHAR(100),

    notes TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),

    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(100),

    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_suspects_case
        FOREIGN KEY(case_id)
        REFERENCES cases(id)
        ON DELETE RESTRICT

);
8. Table attachments
CREATE TABLE attachments (

    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    case_id BIGINT NOT NULL,

    filename VARCHAR(255) NOT NULL,

    original_filename VARCHAR(255) NOT NULL,

    mime_type VARCHAR(100) NOT NULL,

    file_size BIGINT NOT NULL,

    storage_path VARCHAR(500) NOT NULL,

    type VARCHAR(30) NOT NULL,

    uploaded_at TIMESTAMPTZ NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),

    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(100),

    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT chk_file_size
        CHECK(file_size >= 0),

    CONSTRAINT fk_attachment_case
        FOREIGN KEY(case_id)
        REFERENCES cases(id)
        ON DELETE RESTRICT

);
9. Table case_comments
CREATE TABLE case_comments (

    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    case_id BIGINT NOT NULL,

    user_id BIGINT NOT NULL,

    content TEXT NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),

    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(100),

    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_comment_case
        FOREIGN KEY(case_id)
        REFERENCES cases(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_comment_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT

);
10. Table audit_logs
CREATE TABLE audit_logs (

    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id BIGINT NOT NULL,

    action VARCHAR(100) NOT NULL,

    entity_name VARCHAR(100) NOT NULL,

    entity_id BIGINT NOT NULL,

    details TEXT,

    ip_address VARCHAR(45),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    created_by VARCHAR(100),

    CONSTRAINT fk_audit_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT

);
11. Création des index

Après les tables, nous créons les index.

CREATE UNIQUE INDEX idx_users_email
ON users(email);

CREATE UNIQUE INDEX idx_cases_number
ON cases(case_number);

CREATE INDEX idx_cases_status
ON cases(status);

CREATE INDEX idx_cases_priority
ON cases(priority);

CREATE INDEX idx_case_assignments_case
ON case_assignments(case_id);

CREATE INDEX idx_case_assignments_user
ON case_assignments(user_id);

CREATE INDEX idx_comments_case
ON case_comments(case_id);

CREATE INDEX idx_attachments_case
ON attachments(case_id);

CREATE INDEX idx_audit_user
ON audit_logs(user_id);
12. Pourquoi créer les index à la fin ?

Parce que :

les tables existent déjà ;
la création est plus rapide ;
Flyway exécutera les scripts dans un ordre logique.
13. Ce que nous n'avons volontairement pas ajouté

Afin de garder le MVP simple, nous n'avons pas encore modélisé :

victims
witnesses
vehicles
weapons
evidence
addresses
organizations

Notre architecture permet de les ajouter sans remettre en cause le modèle existant.

14. Vérification finale

Notre schéma répond aux exigences du poste :

Exigence	Couverture
PostgreSQL	✅
SQL	✅
Architecture modulaire	✅
Maintenance	✅
Documentation	✅
Audit	✅
Sécurité	✅
Extensibilité	✅
Questions d'entretien
Pourquoi utiliser GENERATED ALWAYS AS IDENTITY plutôt que SERIAL ?

Réponse :

Parce qu'il s'agit de la méthode moderne recommandée par PostgreSQL. Elle est conforme au standard SQL et s'intègre mieux avec les outils récents.

Pourquoi choisir ON DELETE RESTRICT ?

Réponse :

Pour préserver l'intégrité des données. Dans une application de gestion d'enquêtes, il ne doit pas être possible de supprimer un rôle, un utilisateur ou un dossier encore référencé.

Pourquoi créer les index après les tables ?

Réponse :

Les index dépendent des tables. Les créer à la fin rend le script plus lisible et facilite son exécution par Flyway.

📄 Documentation GitHub

Créer le fichier :

docs/database/06-PostgreSQL-Schema.md

Puis ajouter une présentation du schéma comprenant :

la liste des tables ;
les conventions de nommage ;
les contraintes principales ;
la stratégie des clés étrangères ;
la liste des index ;
les choix de conception (IDENTITY, TIMESTAMPTZ, ON DELETE RESTRICT).

Le code SQL complet sera stocké dans un fichier dédié, par exemple :

backend/
└── src/
    └── main/
        └── resources/
            └── db/
                └── migration/
                    └── V1__Initial_schema.sql

Nous n'y placerons pas le script immédiatement : il sera généré lors du chapitre consacré à Flyway, afin que le dépôt soit organisé comme un véritable projet Spring Boot professionnel.

🎯 Ce qui nous attend

Nous avons maintenant terminé la conception logique et physique de la base de données.

Le prochain chapitre sera consacré à Flyway, l'outil de migration de base de données utilisé dans de nombreux projets Spring Boot.

Nous verrons :

pourquoi versionner la base de données comme le code ;
comment organiser les fichiers V1__, V2__, V3__ ;
comment intégrer Flyway à Spring Boot ;
comment préparer un pipeline CI/CD où chaque déploiement met automatiquement la base à jour.

À partir de là, nous entrerons progressivement dans la phase de développement du backend.

Structure du projet PCMS

src/
└── main/
    └── java/
        └── lu/
            └── police/
                └── pcms/
                    ├── common/
                    │   ├── config/
                    │   ├── exception/
                    │   ├── security/
                    │   ├── util/
                    │   └── entity/
                    │		├── BaseCreatedEntity.java
                    │		└── BaseEntity.java
                    │
                    ├── role/
                    │   ├── controller/
                    │   ├── dto/
                    │   ├── entity/
                    │	│	└── Role.java
                    │   ├── repository/
                    │   ├── validation/
                    │   └── service/
                    │
                    ├── department/
                    │   ├── controller/
                    │   ├── dto/
                    │   ├── entity/
                    │	│	└── Department.java
                    │   ├── repository/
                    │   ├── validation/
                    │   └── service/
                    │
                    ├── user/
                    │   ├── controller/
                    │   ├── dto/
                    │   ├── entity/
                    │	│	└── User.java
                    │   ├── repository/
                    │   ├── validation/
                    │   └── service/
                    │
                    ├── casefile/
                    │   ├── controller/
                    │   ├── dto/
                    │   ├── entity/
                    │	│	└── CaseFile.java
                    │   ├── enums/
                    │	│	├── CasePriority.java
                    │	│	└── CaseStatus.java
                    │   ├── repository/
                    │   ├── validation/
                    │   └── service/
                    │
                    ├── caseassignment/
                    │   ├── controller/
                    │   ├── dto/
                    │   ├── entity/
                    │	│	└── CaseAssignment.java
                    │   ├── repository/
                    │   ├── validation/
                    │   └── service/
                    │
                    ├── suspect/
                    │   ├── controller/
                    │   ├── dto/
                    │   ├── entity/
                    │	│	└── Suspect.java
                    │   ├── repository/
                    │   ├── validation/
                    │   └── service/
                    │
                    ├── attachment/
                    │   ├── controller/
                    │   ├── dto/
                    │   ├── entity/
                    │	│	└── Attachment.java
                    │   ├── repository/
                    │   ├── validation/
                    │   └── service/
                    │
                    ├── casecomment/
                    │   ├── controller/
                    │   ├── dto/
                    │   ├── entity/
                    │	│	└── Casecomment.java
                    │   ├── repository/
                    │   ├── validation/
                    │   └── service/
                    │
                    │── audit/
                    │   ├── controller/
                    │   ├── dto/
                    │   ├── entity/
                    │	│	└── AuditLog.java
                    │   ├── repository/
                    │   ├── validation/
                    │   └── service/
                    │
                    └── PcmsBackendApplication.java
