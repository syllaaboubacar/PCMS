package lu.police.pcms.suspect.repository;

import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.suspect.entity.Suspect;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository Spring Data JPA dédié à la gestion des suspects.
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
 *     <li>de rechercher les suspects d'un dossier ;</li>
 *     <li>de retrouver un suspect par son identité ;</li>
 *     <li>de vérifier les doublons dans une enquête ;</li>
 *     <li>de préparer les futurs modules de recherche criminelle
 *     et de statistiques.</li>
 * </ul>
 */
public interface SuspectRepository extends JpaRepository<Suspect, Long> {

    /*
     * ============================================================
     * Recherche par dossier
     * ============================================================
     */

    /**
     * Retourne tous les suspects associés à un dossier.
     *
     * @param caseFile dossier concerné
     * @return liste des suspects
     */
    List<Suspect> findByCaseFile(CaseFile caseFile);

    /**
     * Retourne tous les suspects associés
     * à un dossier via son identifiant.
     *
     * @param caseId identifiant du dossier
     * @return liste des suspects
     */
    List<Suspect> findByCaseFileId(Long caseId);

    /*
     * ============================================================
     * Recherche par identité
     * ============================================================
     */

    /**
     * Recherche tous les suspects portant
     * un nom donné.
     *
     * @param lastName nom recherché
     * @return liste des suspects
     */
    List<Suspect> findByLastName(String lastName);

    /**
     * Recherche tous les suspects portant
     * un prénom donné.
     *
     * @param firstName prénom recherché
     * @return liste des suspects
     */
    List<Suspect> findByFirstName(String firstName);

    /**
     * Recherche les suspects correspondant
     * à un nom et un prénom.
     *
     * @param lastName nom
     * @param firstName prénom
     * @return liste des suspects
     */
    List<Suspect> findByLastNameAndFirstName(
            String lastName,
            String firstName
    );

    /**
     * Recherche les suspects par date de naissance.
     *
     * @param birthDate date de naissance
     * @return liste des suspects
     */
    List<Suspect> findByBirthDate(LocalDate birthDate);

    /**
     * Recherche les suspects par nationalité.
     *
     * @param nationality nationalité recherchée
     * @return liste des suspects
     */
    List<Suspect> findByNationality(String nationality);

    /*
     * ============================================================
     * Vérifications
     * ============================================================
     */

    /**
     * Vérifie si un suspect est déjà présent
     * dans un dossier.
     *
     * @param caseId identifiant du dossier
     * @param lastName nom
     * @param firstName prénom
     * @return true si le suspect existe déjà
     */
    boolean existsByCaseFileIdAndLastNameAndFirstName(
            Long caseId,
            String lastName,
            String firstName
    );

    /*
     * ============================================================
     * Statistiques
     * ============================================================
     */

    /**
     * Compte le nombre de suspects
     * associés à un dossier.
     *
     * @param caseId identifiant du dossier
     * @return nombre de suspects
     */
    long countByCaseFileId(Long caseId);


    /**
     * Retourne tous les suspects non supprimés logiquement.
     */
    List<Suspect> findByDeletedFalse();

}