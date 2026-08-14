package lu.police.pcms.user.repository;

import lu.police.pcms.department.entity.Department;
import lu.police.pcms.role.entity.Role;
import lu.police.pcms.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Spring Data JPA permettant de gérer
 * les opérations d'accès aux données des utilisateurs.
 *
 * <p>
 * Ce repository fournit les opérations CRUD standards
 * héritées de {@link JpaRepository} ainsi que des
 * méthodes de recherche spécifiques au Police Case
 * Management System (PCMS).
 * </p>
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /*
     * ============================================================
     * Recherche métier
     * ============================================================
     */

    /**
     * Recherche un utilisateur par son adresse e-mail.
     *
     * Utilisée notamment par Spring Security lors
     * de l'authentification.
     *
     * @param email adresse e-mail
     * @return utilisateur correspondant
     */
    Optional<User> findByEmail(String email);

    /**
     * Recherche tous les utilisateurs appartenant
     * à un rôle donné.
     *
     * @param role rôle recherché
     * @return liste des utilisateurs
     */
    List<User> findByRole(Role role);

    /**
     * Recherche tous les utilisateurs appartenant
     * à un département.
     *
     * @param department département recherché
     * @return liste des utilisateurs
     */
    List<User> findByDepartment(Department department);

    /*
     * ============================================================
     * Gestion des utilisateurs actifs
     * ============================================================
     */

    /**
     * Retourne tous les utilisateurs activés.
     *
     * @return liste des utilisateurs actifs
     */
    List<User> findByEnabledTrue();

    /**
     * Retourne tous les utilisateurs désactivés.
     *
     * @return liste des utilisateurs désactivés
     */
    List<User> findByEnabledFalse();

    /**
     * Retourne les utilisateurs actifs d'un département.
     *
     * @param department département concerné
     * @return liste des utilisateurs
     */
    List<User> findByDepartmentAndEnabledTrue(
            Department department
    );

    /**
     * Retourne les utilisateurs actifs possédant un rôle.
     *
     * @param role rôle concerné
     * @return liste des utilisateurs
     */
    List<User> findByRoleAndEnabledTrue(
            Role role
    );

    /*
     * ============================================================
     * Vérifications métier
     * ============================================================
     */

    /**
     * Vérifie si une adresse e-mail existe déjà.
     *
     * @param email adresse e-mail
     * @return true si elle existe
     */
    boolean existsByEmail(String email);

    /*
     * ============================================================
     * Statistiques
     * ============================================================
     */

    /**
     * Compte les utilisateurs activés.
     *
     * @return nombre d'utilisateurs actifs
     */
    long countByEnabledTrue();

    /**
     * Compte les utilisateurs désactivés.
     *
     * @return nombre d'utilisateurs désactivés
     */
    long countByEnabledFalse();

    /**
     * Compte les utilisateurs d'un département.
     *
     * @param department département concerné
     * @return nombre d'utilisateurs
     */
    long countByDepartment(Department department);

    /**
     * Compte les utilisateurs d'un rôle.
     *
     * @param role rôle concerné
     * @return nombre d'utilisateurs
     */
    long countByRole(Role role);

    /*
     * ============================================================
     * Evolutions futures
     * ============================================================
     */

    /*
     * Pagination
     *
     * Page<User> findAll(Pageable pageable);
     *
     * ------------------------------------------------------------
     *
     * Recherche textuelle
     *
     * List<User> findByLastNameContainingIgnoreCase(String keyword);
     *
     * List<User> findByFirstNameContainingIgnoreCase(String keyword);
     *
     * List<User> findByEmailContainingIgnoreCase(String keyword);
     *
     * ------------------------------------------------------------
     *
     * Recherche multicritère
     *
     * List<User> findByDepartmentAndRole(
     *      Department department,
     *      Role role
     * );
     *
     * ------------------------------------------------------------
     *
     * Recherche dynamique
     *
     * JpaSpecificationExecutor<User>
     *
     * ------------------------------------------------------------
     *
     * QueryDSL
     *
     * QuerydslPredicateExecutor<User>
     *
     * ------------------------------------------------------------
     *
     * Cache
     *
     * @Cacheable("users")
     * Optional<User> findByEmail(String email);
     *
     */

    /**
     * Retourne tous les utilisateurs non supprimés logiquement.
     * Utilisé pour n'exposer que les utilsateurs actifs.
     */
    List<User> findByDeletedFalse();

}