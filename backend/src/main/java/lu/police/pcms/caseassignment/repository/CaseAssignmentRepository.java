package lu.police.pcms.caseassignment.repository;

import lu.police.pcms.caseassignment.entity.CaseAssignment;
import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Spring Data JPA permettant de gérer les affectations
 * des enquêteurs aux dossiers.
 *
 * <p>
 * Cette entité représente l'association entre un utilisateur
 * (enquêteur) et un dossier d'enquête.
 *
 * Une affectation peut être :
 * <ul>
 *     <li>active ;</li>
 *     <li>désactivée (historique conservé).</li>
 * </ul>
 *
 * Ce repository fournit les opérations CRUD standards ainsi que
 * les principales recherches métier utilisées dans le projet PCMS.
 */
public interface CaseAssignmentRepository
        extends JpaRepository<CaseAssignment, Long> {

    /*
     * ============================================================
     * Recherche par dossier
     * ============================================================
     */

    /**
     * Retourne toutes les affectations d'un dossier.
     *
     * @param caseFile dossier concerné
     * @return liste des affectations
     */
    List<CaseAssignment> findByCaseFile(CaseFile caseFile);

    /**
     * Retourne uniquement les affectations actives d'un dossier.
     *
     * @param caseFile dossier concerné
     * @return liste des affectations actives
     */
    List<CaseAssignment> findByCaseFileAndActiveTrue(
            CaseFile caseFile
    );

    /*
     * ============================================================
     * Recherche par utilisateur
     * ============================================================
     */

    /**
     * Retourne toutes les affectations d'un utilisateur.
     *
     * @param user utilisateur concerné
     * @return liste des affectations
     */
    List<CaseAssignment> findByUser(User user);

    /**
     * Retourne uniquement les affectations actives d'un utilisateur.
     *
     * @param user utilisateur concerné
     * @return liste des affectations actives
     */
    List<CaseAssignment> findByUserAndActiveTrue(
            User user
    );

    /*
     * ============================================================
     * Recherche dossier + utilisateur
     * ============================================================
     */

    /**
     * Recherche une affectation précise.
     *
     * @param caseFile dossier
     * @param user enquêteur
     * @return affectation éventuelle
     */
    Optional<CaseAssignment> findByCaseFileAndUser(
            CaseFile caseFile,
            User user
    );

    /**
     * Vérifie si un utilisateur est affecté à un dossier.
     *
     * @param caseFile dossier
     * @param user utilisateur
     * @return true si l'affectation existe
     */
    boolean existsByCaseFileAndUser(
            CaseFile caseFile,
            User user
    );

    /*
     * ============================================================
     * Recherche par état
     * ============================================================
     */

    /**
     * Retourne toutes les affectations actives.
     *
     * @return liste des affectations actives
     */
    List<CaseAssignment> findByActiveTrue();

    /**
     * Retourne toutes les affectations inactives.
     *
     * @return liste des affectations inactives
     */
    List<CaseAssignment> findByActiveFalse();

    /*
     * ============================================================
     * Statistiques
     * ============================================================
     */

    /**
     * Compte le nombre d'affectations d'un dossier.
     *
     * @param caseFile dossier concerné
     * @return nombre d'affectations
     */
    long countByCaseFile(CaseFile caseFile);

    /**
     * Compte le nombre d'affectations actives d'un dossier.
     *
     * @param caseFile dossier concerné
     * @return nombre d'affectations actives
     */
    long countByCaseFileAndActiveTrue(
            CaseFile caseFile
    );

    /**
     * Compte le nombre d'affectations d'un utilisateur.
     *
     * @param user utilisateur
     * @return nombre d'affectations
     */
    long countByUser(User user);



    /**
     * Retourne toutes les affectations non supprimées logiquement.
     */
    List<CaseAssignment> findByDeletedFalse();


    /**
     * Vérifie si une affectation active existe pour un dossier et un utilisateur.
     */
    boolean existsByCaseFileAndUserAndActiveTrue(CaseFile caseFile, User user);

}