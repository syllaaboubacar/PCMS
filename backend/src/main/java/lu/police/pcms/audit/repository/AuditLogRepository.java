package lu.police.pcms.audit.repository;

import lu.police.pcms.audit.entity.AuditLog;
import lu.police.pcms.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

/**
 * Repository Spring Data JPA permettant de gérer les journaux d'audit.
 *
 * <p>
 * Cette entité permet de tracer toutes les opérations importantes
 * réalisées dans le système PCMS.
 * </p>
 *
 * <p>
 * Elle est principalement utilisée pour :
 * <ul>
 *     <li>la traçabilité des actions utilisateurs ;</li>
 *     <li>les audits de sécurité ;</li>
 *     <li>l'investigation d'incidents ;</li>
 *     <li>les tableaux de bord administrateur ;</li>
 *     <li>les statistiques d'utilisation.</li>
 * </ul>
 * </p>
 */
public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    /*
     * ============================================================
     * Recherche par utilisateur
     * ============================================================
     */

    /**
     * Recherche toutes les opérations effectuées par un utilisateur.
     *
     * @param user utilisateur concerné
     * @return liste des opérations
     */
    List<AuditLog> findByUser(User user);

    /**
     * Recherche paginée des opérations d'un utilisateur.
     *
     * @param user utilisateur concerné
     * @param pageable pagination
     * @return page des opérations
     */
    Page<AuditLog> findByUser(
            User user,
            Pageable pageable
    );

    /*
     * ============================================================
     * Recherche par type d'action
     * ============================================================
     */

    /**
     * Recherche toutes les opérations d'un type donné.
     *
     * Exemple :
     * CREATE
     * UPDATE
     * DELETE
     * LOGIN
     * LOGOUT
     *
     * @param action action recherchée
     * @return liste des opérations
     */
    List<AuditLog> findByAction(String action);

    /**
     * Recherche paginée par type d'action.
     *
     * @param action action recherchée
     * @param pageable pagination
     * @return page des opérations
     */
    Page<AuditLog> findByAction(
            String action,
            Pageable pageable
    );

    /*
     * ============================================================
     * Recherche par entité métier
     * ============================================================
     */

    /**
     * Recherche toutes les opérations concernant une entité.
     *
     * Exemple :
     * USER
     * CASE
     * ROLE
     *
     * @param entityName nom de l'entité
     * @return liste des opérations
     */
    List<AuditLog> findByEntityName(String entityName);

    /**
     * Recherche toutes les opérations concernant un enregistrement précis.
     *
     * @param entityName nom de l'entité
     * @param entityId identifiant métier
     * @return liste des opérations
     */
    List<AuditLog> findByEntityNameAndEntityId(
            String entityName,
            Long entityId
    );

    /*
     * ============================================================
     * Recherche temporelle
     * ============================================================
     */

    /**
     * Recherche les opérations effectuées après une date.
     *
     * @param date date minimale
     * @return liste des opérations
     */
    List<AuditLog> findByCreatedAtAfter(
            Instant date
    );

    /**
     * Recherche les opérations comprises entre deux dates.
     *
     * @param start début
     * @param end fin
     * @return liste des opérations
     */
    List<AuditLog> findByCreatedAtBetween(
            Instant start,
            Instant end
    );

    /*
     * ============================================================
     * Statistiques
     * ============================================================
     */

    /**
     * Compte le nombre total d'opérations réalisées
     * par un utilisateur.
     *
     * @param user utilisateur
     * @return nombre d'opérations
     */
    long countByUser(User user);

    /**
     * Compte le nombre d'opérations d'un type donné.
     *
     * @param action action
     * @return nombre d'opérations
     */
    long countByAction(String action);

}