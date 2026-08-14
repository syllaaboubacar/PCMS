package lu.police.pcms.casefile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lu.police.pcms.casefile.enums.CasePriority;
import lu.police.pcms.casefile.enums.CaseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO utilisé pour modifier un dossier d'enquête existant.
 *
 * <p>
 * Le numéro métier {@code caseNumber} n'est pas modifiable.
 * L'identité du dossier est conservée après sa création.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCaseFileRequest {

    /**
     * Titre du dossier.
     */
    @NotBlank(message = "Le titre du dossier est obligatoire")
    @Size(
        max = 255,
        message = "Le titre ne peut pas dépasser 255 caractères"
    )
    private String title;

    /**
     * Description du dossier.
     */
    @NotBlank(message = "La description du dossier est obligatoire")
    private String description;

    /**
     * Nouveau statut du dossier.
     */
    @NotNull(message = "Le statut est obligatoire")
    private CaseStatus status;

    /**
     * Nouvelle priorité du dossier.
     */
    @NotNull(message = "La priorité est obligatoire")
    private CasePriority priority;

    /**
     * Date et heure de fermeture du dossier.
     *
     * <p>
     * Peut être {@code null} lorsqu'un dossier n'est pas encore fermé.
     * </p>
     */
    private Instant closedAt;

    /**
     * Date de l'incident.
     */
    private LocalDate incidentDate;

    /**
     * Lieu de l'incident.
     */
    @Size(
        max = 255,
        message = "Le lieu ne peut pas dépasser 255 caractères"
    )
    private String location;
}