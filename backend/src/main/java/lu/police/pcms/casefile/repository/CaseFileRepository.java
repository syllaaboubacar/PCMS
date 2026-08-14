package lu.police.pcms.casefile.repository;

import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.casefile.enums.CasePriority;
import lu.police.pcms.casefile.enums.CaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository Spring Data JPA permettant de gérer
 * les opérations d'accès aux données des dossiers.
 *
 * <p>
 * Ce repository fournit les opérations CRUD standards
 * héritées de {@link JpaRepository} ainsi que les
 * recherches métier nécessaires au Police Case
 * Management System (PCMS).
 * </p>
 */
public interface CaseFileRepository extends JpaRepository<CaseFile, Long> {

    /*
     * ============================================================
     * Recherche par numéro de dossier
     * ============================================================
     */

    /**
     * Recherche un dossier par son numéro métier.
     *
     * @param caseNumber numéro du dossier
     * @return dossier correspondant
     */
    Optional<CaseFile> findByCaseNumber(String caseNumber);

    /**
     * Vérifie si un numéro de dossier existe déjà.
     *
     * @param caseNumber numéro du dossier
     * @return true si le numéro existe
     */
    boolean existsByCaseNumber(String caseNumber);

    /*
     * ============================================================
     * Recherche par statut
     * ============================================================
     */

    /**
     * Recherche tous les dossiers ayant un statut donné.
     *
     * @param status statut recherché
     * @return liste des dossiers
     */
    List<CaseFile> findByStatus(CaseStatus status);

    /**
     * Recherche paginée des dossiers par statut.
     *
     * @param status statut recherché
     * @param pageable pagination
     * @return page de résultats
     */
    Page<CaseFile> findByStatus(
            CaseStatus status,
            Pageable pageable
    );

    /*
     * ============================================================
     * Recherche par priorité
     * ============================================================
     */

    /**
     * Recherche tous les dossiers ayant une priorité donnée.
     *
     * @param priority priorité recherchée
     * @return liste des dossiers
     */
    List<CaseFile> findByPriority(CasePriority priority);

    /**
     * Recherche paginée des dossiers par priorité.
     *
     * @param priority priorité recherchée
     * @param pageable pagination
     * @return page de résultats
     */
    Page<CaseFile> findByPriority(
            CasePriority priority,
            Pageable pageable
    );

    /*
     * ============================================================
     * Recherche par date
     * ============================================================
     */

    /**
     * Recherche les dossiers ouverts après une date.
     *
     * @param date date minimale
     * @return liste des dossiers
     */
    List<CaseFile> findByOpenedAtAfter(Instant date);

    /**
     * Recherche les dossiers ouverts entre deux dates.
     *
     * @param start date de début
     * @param end date de fin
     * @return liste des dossiers
     */
    List<CaseFile> findByOpenedAtBetween(
            Instant start,
            Instant end
    );

    /*
     * ============================================================
     * Recherche multicritère simple
     * ============================================================
     */

    /**
     * Recherche paginée par statut et priorité.
     *
     * @param status statut
     * @param priority priorité
     * @param pageable pagination
     * @return page des dossiers
     */
    Page<CaseFile> findByStatusAndPriority(
            CaseStatus status,
            CasePriority priority,
            Pageable pageable
    );

    /*
     * ============================================================
     * Statistiques
     * ============================================================
     */

    /**
     * Compte les dossiers ayant un statut donné.
     *
     * @param status statut
     * @return nombre de dossiers
     */
    long countByStatus(CaseStatus status);

    /**
     * Compte les dossiers ayant une priorité donnée.
     *
     * @param priority priorité
     * @return nombre de dossiers
     */
    long countByPriority(CasePriority priority);

    /*
     * ============================================================
     * Evolutions futures
     * ============================================================
     */

    /*
     * Recherche plein texte
     *
     * List<CaseFile> findByTitleContainingIgnoreCase(...)
     *
     * List<CaseFile> findByDescriptionContainingIgnoreCase(...)
     *
     * ------------------------------------------------------------
     *
     * Recherche par lieu
     *
     * findByLocation(...)
     *
     * ------------------------------------------------------------
     *
     * Recherche par date d'incident
     *
     * findByIncidentDate(...)
     *
     * findByIncidentDateBetween(...)
     *
     * ------------------------------------------------------------
     *
     * Pagination globale
     *
     * Page<CaseFile> findAll(Pageable pageable)
     *
     * ------------------------------------------------------------
     *
     * Tri dynamique
     *
     * Sort
     *
     * ------------------------------------------------------------
     *
     * Recherche avancée
     *
     * JpaSpecificationExecutor<CaseFile>
     *
     * ------------------------------------------------------------
     *
     * QueryDSL
     *
     * QuerydslPredicateExecutor<CaseFile>
     *
     * ------------------------------------------------------------
     *
     * Requêtes JPQL personnalisées
     *
     * @Query(...)
     *
     * ------------------------------------------------------------
     *
     * Dashboard
     *
     * statistiques
     * agrégats
     * projections DTO
     *
     */

    /**
     * Retourne tous les dossiers non supprimés logiquement.
     */
    List<CaseFile> findByDeletedFalse();

}