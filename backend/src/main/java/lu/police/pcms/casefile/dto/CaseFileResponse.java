package lu.police.pcms.casefile.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lu.police.pcms.casefile.enums.CasePriority;
import lu.police.pcms.casefile.enums.CaseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO retourné par l'API REST pour représenter un dossier d'enquête en réponse.
 *
 * <p>
 * Ce DTO expose toutes les informations du dossier, y compris les champs d'audit
 * hérités de {@code BaseEntity} et {@code BaseCreatedEntity}.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaseFileResponse {

    /**
     * Identifiant technique du dossier.
     */
    private Long id;

    /**
     * Numéro métier unique du dossier.
     */
    private String caseNumber;

    /**
     * Titre du dossier.
     */
    private String title;

    /**
     * Description du dossier.
     */
    private String description;

    /**
     * Statut actuel du dossier.
     */
    private CaseStatus status;

    /**
     * Priorité actuelle du dossier.
     */
    private CasePriority priority;

    /**
     * Date et heure d'ouverture (format ISO-8601).
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant openedAt;

    /**
     * Date et heure de fermeture (optionnelle, format ISO-8601).
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant closedAt;

    /**
     * Date de l'incident (optionnelle, format ISO-8601).
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate incidentDate;

    /**
     * Lieu de l'incident (optionnel).
     */
    private String location;

    /**
     * Date de création du dossier (format ISO-8601).
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;

    /**
     * Utilisateur ayant créé le dossier.
     */
    private String createdBy;

    /**
     * Date de dernière modification (format ISO-8601).
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant updatedAt;

    /**
     * Utilisateur ayant effectué la dernière modification.
     */
    private String updatedBy;

    /**
     * Indique si le dossier est marqué comme supprimé.
     *
     * <p>
     * Ce champ est optionnel et permet au client de savoir si le dossier
     * a été supprimé logiquement. Si vous ne souhaitez pas exposer cette
     * information, vous pouvez le retirer du DTO.
     * </p>
     */
    private Boolean deleted;
}