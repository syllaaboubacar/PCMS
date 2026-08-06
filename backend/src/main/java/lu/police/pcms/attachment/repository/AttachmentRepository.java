package lu.police.pcms.attachment.repository;

import lu.police.pcms.attachment.entity.Attachment;
import lu.police.pcms.casefile.entity.CaseFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository Spring Data JPA permettant de gérer
 * les pièces jointes des dossiers d'enquête.
 *
 * <p>
 * Les pièces jointes représentent les preuves numériques
 * ou documents associés à un dossier.
 * </p>
 */
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    /**
     * Recherche toutes les pièces jointes
     * d'un dossier.
     *
     * @param caseFile dossier concerné
     * @return liste des pièces jointes
     */
    List<Attachment> findByCaseFile(CaseFile caseFile);

    /**
     * Recherche toutes les pièces jointes
     * d'un dossier via son identifiant.
     *
     * @param caseId identifiant du dossier
     * @return liste des pièces jointes
     */
    List<Attachment> findByCaseFileId(Long caseId);

    /**
     * Recherche toutes les pièces jointes
     * d'un type donné.
     *
     * Exemple :
     * PHOTO
     * VIDEO
     * REPORT
     * AUDIO
     *
     * @param type type métier
     * @return liste des pièces jointes
     */
    List<Attachment> findByType(String type);

    /**
     * Recherche toutes les pièces jointes
     * possédant un type MIME donné.
     *
     * Exemple :
     * image/jpeg
     * application/pdf
     *
     * @param mimeType type MIME
     * @return liste des pièces jointes
     */
    List<Attachment> findByMimeType(String mimeType);

    /**
     * Recherche une pièce jointe
     * à partir de son chemin de stockage.
     *
     * @param storagePath chemin de stockage
     * @return pièce jointe éventuelle
     */
    Optional<Attachment> findByStoragePath(String storagePath);

    /**
     * Recherche les pièces jointes
     * envoyées après une date donnée.
     *
     * @param uploadedAt date minimale
     * @return liste des pièces jointes
     */
    List<Attachment> findByUploadedAtAfter(Instant uploadedAt);

    /**
     * Recherche les pièces jointes
     * envoyées entre deux dates.
     *
     * @param start début de période
     * @param end fin de période
     * @return liste des pièces jointes
     */
    List<Attachment> findByUploadedAtBetween(
            Instant start,
            Instant end
    );

    /**
     * Recherche toutes les pièces jointes
     * non supprimées.
     *
     * @return liste des pièces jointes actives
     */
    List<Attachment> findByDeletedFalse();

    /**
     * Recherche toutes les pièces jointes
     * supprimées logiquement.
     *
     * @return liste des pièces jointes supprimées
     */
    List<Attachment> findByDeletedTrue();

    /**
     * Compte le nombre de pièces jointes
     * d'un dossier.
     *
     * @param caseId identifiant du dossier
     * @return nombre de pièces jointes
     */
    long countByCaseFileId(Long caseId);

    /**
     * Recherche une pièce jointe
     * par son nom interne.
     *
     * @param filename nom interne du fichier
     * @return pièce jointe éventuelle
     */
    Optional<Attachment> findByFilename(String filename);

    /**
     * Recherche toutes les pièces jointes
     * portant un nom d'origine donné.
     *
     * @param originalFilename nom d'origine
     * @return liste des pièces jointes
     */
    List<Attachment> findByOriginalFilename(String originalFilename);

    /**
     * Vérifie si un nom de fichier interne
     * existe déjà.
     *
     * @param filename nom interne
     * @return true si le fichier existe
     */
    boolean existsByFilename(String filename);

}