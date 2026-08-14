package lu.police.pcms.casefile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lu.police.pcms.casefile.enums.CasePriority;
import lu.police.pcms.casefile.enums.CaseStatus;

import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO utilisé pour créer un nouveau dossier d'enquête.
 *
 * <p>
 * Ce DTO représente les données reçues par l'API
 * lors de la création d'un CaseFile.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCaseFileRequest {

    /**
     * Numéro métier unique du dossier.
     */
    @NotBlank(message = "Le numéro de dossier est obligatoire")
    @Size(
        max = 30,
        message = "Le numéro de dossier ne peut pas dépasser 30 caractères"
    )
    @Pattern(
            regexp = "^PCMS_[A-Z][A-Z0-9_]*$",
            message = "Le numéro de dossier doit commencer par 'PCMS_' et contenir uniquement des lettres majuscules, des chiffres et des underscores."
    )
    private String caseNumber;

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
     * Statut initial du dossier.
     */
    @NotNull(message = "Le statut est obligatoire")
    private CaseStatus status;

    /**
     * Priorité du dossier.
     */
    @NotNull(message = "La priorité est obligatoire")
    private CasePriority priority;

    /**
     * Date et heure d'ouverture du dossier.
     */
    @NotNull(message = "La date d'ouverture est obligatoire")
    private Instant openedAt;

    /**
     * Date de l'incident (optionnelle).
     * Si non fournie, la valeur reste {@code null}.
     */
    private LocalDate incidentDate;

    /**
     * Lieu de l'incident (optionnel).
     * Si non fourni, la valeur reste {@code null}.
     */
    @Size(max = 255, message = "Le lieu ne peut pas dépasser 255 caractères")
    private String location;
}