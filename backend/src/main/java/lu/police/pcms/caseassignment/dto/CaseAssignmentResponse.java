package lu.police.pcms.caseassignment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * DTO retourné par l'API REST pour représenter une affectation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaseAssignmentResponse {

    /**
     * Identifiant technique de l'affectation.
     */
    private Long id;

    /**
     * Identifiant du dossier affecté.
     */
    private Long caseFileId;

    /**
     * Identifiant de l'utilisateur affecté.
     */
    private Long userId;

    /**
     * Date et heure de l'affectation.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant assignedAt;

    /**
     * Indique si l'affectation est active.
     */
    private Boolean active;

    // ===== Champs d'audit =====

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;

    private String createdBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant updatedAt;

    private String updatedBy;

    /**
     * Indique si l'affectation est supprimée logiquement.
     * (Optionnel : à conserver si le client en a besoin)
     */
    private Boolean deleted;
}