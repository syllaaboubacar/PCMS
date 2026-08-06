package lu.police.pcms.casecomment.repository;

import lu.police.pcms.casecomment.entity.CaseComment;
import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

/**
 * Repository Spring Data JPA dédié à la gestion des commentaires
 * des dossiers d'enquête.
 *
 * <p>
 * Ce repository fournit les opérations CRUD standards héritées de
 * {@link JpaRepository} ainsi que plusieurs méthodes de recherche
 * spécifiques au Police Case Management System (PCMS).
 * </p>
 *
 * <p>
 * Les méthodes proposées permettent notamment :
 * </p>
 *
 * <ul>
 *     <li>de consulter les commentaires d'un dossier ;</li>
 *     <li>de retrouver les commentaires rédigés par un utilisateur ;</li>
 *     <li>de consulter les commentaires sur une période donnée ;</li>
 *     <li>de produire des statistiques utiles aux tableaux de bord.</li>
 * </ul>
 */
public interface CaseCommentRepository
        extends JpaRepository<CaseComment, Long> {

    /*
     * ============================================================
     * Recherche par dossier
     * ============================================================
     */

    /**
     * Retourne tous les commentaires
     * associés à un dossier.
     *
     * @param caseFile dossier concerné
     * @return liste des commentaires
     */
    List<CaseComment> findByCaseFile(CaseFile caseFile);

    /**
     * Retourne tous les commentaires
     * d'un dossier via son identifiant.
     *
     * @param caseId identifiant du dossier
     * @return liste des commentaires
     */
    List<CaseComment> findByCaseFileId(Long caseId);

    /*
     * ============================================================
     * Recherche par auteur
     * ============================================================
     */

    /**
     * Retourne tous les commentaires
     * rédigés par un utilisateur.
     *
     * @param user auteur
     * @return liste des commentaires
     */
    List<CaseComment> findByUser(User user);

    /**
     * Retourne tous les commentaires
     * d'un utilisateur via son identifiant.
     *
     * @param userId identifiant utilisateur
     * @return liste des commentaires
     */
    List<CaseComment> findByUserId(Long userId);

    /*
     * ============================================================
     * Recherche par date
     * ============================================================
     */

    /**
     * Retourne les commentaires créés
     * après une date donnée.
     *
     * @param createdAt date minimale
     * @return liste des commentaires
     */
    List<CaseComment> findByCreatedAtAfter(Instant createdAt);

    /**
     * Retourne les commentaires créés
     * entre deux dates.
     *
     * @param start début de période
     * @param end fin de période
     * @return liste des commentaires
     */
    List<CaseComment> findByCreatedAtBetween(
            Instant start,
            Instant end
    );

    /*
     * ============================================================
     * Recherche sur l'état de suppression
     * ============================================================
     */

    /**
     * Retourne tous les commentaires
     * non supprimés.
     *
     * @return liste des commentaires actifs
     */
    List<CaseComment> findByDeletedFalse();

    /**
     * Retourne tous les commentaires
     * supprimés logiquement.
     *
     * @return liste des commentaires supprimés
     */
    List<CaseComment> findByDeletedTrue();

    /*
     * ============================================================
     * Statistiques
     * ============================================================
     */

    /**
     * Compte le nombre de commentaires
     * d'un dossier.
     *
     * @param caseId identifiant du dossier
     * @return nombre de commentaires
     */
    long countByCaseFileId(Long caseId);

    /**
     * Compte le nombre de commentaires
     * rédigés par un utilisateur.
     *
     * @param userId identifiant utilisateur
     * @return nombre de commentaires
     */
    long countByUserId(Long userId);

}