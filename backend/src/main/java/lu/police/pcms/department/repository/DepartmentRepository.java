package lu.police.pcms.department.repository;

import lu.police.pcms.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository Spring Data JPA permettant de gérer
 * les opérations d'accès aux données des départements.
 *
 * <p>
 * Ce repository fournit les opérations CRUD standards
 * héritées de {@link JpaRepository} ainsi que des
 * méthodes de recherche spécifiques au Police Case
 * Management System (PCMS).
 * </p>
 */
public interface DepartmentRepository
        extends JpaRepository<Department, Long> {

    /*
     * ============================================================
     * Recherche métier
     * ============================================================
     */

    /**
     * Recherche un département par son code métier.
     *
     * @param code code du département
     * @return le département correspondant si trouvé
     */
    Optional<Department> findByCode(String code);

    /**
     * Recherche un département par son nom.
     *
     * @param name nom du département
     * @return le département correspondant si trouvé
     */
    Optional<Department> findByName(String name);

    /*
     * ============================================================
     * Vérifications métier
     * ============================================================
     */

    /**
     * Vérifie si un département possède déjà ce code.
     *
     * @param code code du département
     * @return true si le code existe
     */
    boolean existsByCode(String code);

    /**
     * Vérifie si un département possède déjà ce nom.
     *
     * @param name nom du département
     * @return true si le nom existe
     */
    boolean existsByName(String name);

    /*
     * ============================================================
     * Statistiques
     * ============================================================
     */

    /**
     * Retourne le nombre total de départements.
     *
     * <p>
     * Cette méthode est héritée de JpaRepository
     * et rappelée ici à titre documentaire.
     * </p>
     *
     * long count();
     */

    /*
     * ============================================================
     * Evolutions futures
     * ============================================================
     */

    /*
     * Pagination
     *
     * Page<Department> findAll(Pageable pageable);
     *
     * ------------------------------------------------------------
     *
     * Recherche textuelle
     *
     * List<Department> findByNameContainingIgnoreCase(String keyword);
     *
     * List<Department> findByCodeContainingIgnoreCase(String keyword);
     *
     * ------------------------------------------------------------
     *
     * Suppression logique
     *
     * List<Department> findByDeletedFalse();
     *
     * long countByDeletedFalse();
     *
     * ------------------------------------------------------------
     *
     * Recherche avancée
     *
     * JpaSpecificationExecutor<Department>
     *
     * ------------------------------------------------------------
     *
     * Cache
     *
     * @Cacheable("departments")
     * Optional<Department> findByCode(String code);
     *
     */

}