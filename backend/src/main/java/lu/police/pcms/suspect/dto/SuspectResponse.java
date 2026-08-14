package lu.police.pcms.suspect.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO retourné par l'API REST pour représenter un suspect.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SuspectResponse {

    /**
     * Identifiant du suspect.
     */
    private Long id;

    /**
     * Identifiant du dossier auquel le suspect est associé.
     */
    private Long caseFileId;

    /**
     * Prénom du suspect.
     */
    private String firstName;

    /**
     * Nom du suspect.
     */
    private String lastName;

    /**
     * Date de naissance du suspect.
     */
    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd"
    )
    private LocalDate birthDate;

    /**
     * Nationalité du suspect.
     */
    private String nationality;

    /**
     * Notes complémentaires concernant le suspect.
     */
    private String notes;

    // ============================================================
    // AUDIT
    // ============================================================

    /**
     * Date de création du suspect.
     */
    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
        timezone = "UTC"
    )
    private Instant createdAt;

    /**
     * Utilisateur ayant créé le suspect.
     */
    private String createdBy;

    /**
     * Date de dernière modification du suspect.
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
}