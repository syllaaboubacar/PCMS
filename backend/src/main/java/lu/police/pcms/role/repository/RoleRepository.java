package lu.police.pcms.role.repository;

import lu.police.pcms.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository Spring Data JPA permettant de gérer
 * les opérations d'accès aux données des rôles.
 *
 * <p>
 * Ce repository fournit les opérations CRUD standards
 * héritées de {@link JpaRepository} ainsi que des
 * méthodes de recherche spécifiques au Police Case
 * Management System (PCMS).
 * </p>
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /*
     * ============================================================
     * Recherche métier
     * ============================================================
     */

    /**
     * Recherche un rôle par son nom.
     *
     * @param name nom du rôle
     * @return le rôle correspondant si trouvé
     */
    Optional<Role> findByName(String name);

    /*
     * ============================================================
     * Vérifications métier
     * ============================================================
     */

    /**
     * Vérifie si un rôle existe déjà.
     *
     * @param name nom du rôle
     * @return true si le rôle existe
     */
    boolean existsByName(String name);

    /*
     * ============================================================
     * Statistiques
     * ============================================================
     */

    /**
     * Retourne le nombre total de rôles.
     *
     * <p>
     * Cette méthode est déjà héritée de JpaRepository,
     * elle est rappelée ici uniquement à titre documentaire.
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
     * Page<Role> findAll(Pageable pageable);
     *
     * List<Role> findByNameContainingIgnoreCase(String keyword);
     *
     * List<Role> findByDeletedFalse();
     *
     * long countByDeletedFalse();
     *
     * Ces méthodes pourront être ajoutées lorsque
     * le projet intégrera :
     *
     * - la pagination REST ;
     * - la recherche multicritère ;
     * - la suppression logique des rôles
     *   (si elle devient nécessaire).
     */

}