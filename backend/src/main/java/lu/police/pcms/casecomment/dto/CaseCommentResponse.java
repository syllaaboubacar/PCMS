package lu.police.pcms.casecomment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * DTO retourné par l'API REST pour représenter un commentaire.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaseCommentResponse {

    /**
     * Identifiant du commentaire.
     */
    private Long id;

    /**
     * Identifiant du dossier associé.
     */
    private Long caseFileId;

    /**
     * Identifiant de l'utilisateur auteur du commentaire.
     */
    private Long userId;

    /**
     * Contenu du commentaire.
     */
    private String content;

    // ============================================================
    // CHAMPS D'AUDIT
    // Hérités de BaseEntity
    // ============================================================

    /**
     * Date et heure de création.
     */
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
            timezone = "UTC"
    )
    private Instant createdAt;

    /**
     * Utilisateur ayant créé le commentaire.
     */
    private String createdBy;

    /**
     * Date et heure de dernière modification.
     */
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
            timezone = "UTC"
    )
    private Instant updatedAt;

    /**
     * Utilisateur ayant effectué la dernière modification.
     */
    private String updatedBy;

    /**
     * Indique si le commentaire est marqué comme supprimé.
     */
    private Boolean deleted;
}