package lu.police.pcms.casefile.dto;

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
 * DTO utilisé pour une mise à jour partielle d'un dossier d'enquête (PATCH).
 *
 * <p>
 * Tous les champs sont optionnels. Seuls les champs non-nuls seront mis à jour.
 * Les champs non fournis (ou explicitement {@code null}) ne seront pas modifiés.
 * Cela permet au client de modifier un ou plusieurs champs en une seule requête,
 * sans avoir à envoyer l'intégralité de la ressource.
 * </p>
 *
 * <p>
 * Le numéro métier {@code caseNumber} n'est pas modifiable.
 * L'identité du dossier est conservée après sa création.
 * </p>
 *
 * @see UpdateCaseFileRequest
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatchCaseFileRequest {

    /**
     * Titre du dossier (optionnel).
     * Si fourni, le titre sera mis à jour.
     */
    @Size(
            max = 255,
            message = "Le titre ne peut pas dépasser 255 caractères"
    )
    private String title;

    /**
     * Description du dossier (optionnelle).
     * Si fournie, la description sera mise à jour.
     */
    private String description;

    /**
     * Nouveau statut du dossier (optionnel).
     * Si fourni, le statut sera mis à jour.
     */
    private CaseStatus status;

    /**
     * Nouvelle priorité du dossier (optionnelle).
     * Si fournie, la priorité sera mise à jour.
     */
    private CasePriority priority;

    /**
     * Date et heure de fermeture du dossier (optionnelle).
     * Si fournie, la date de fermeture sera mise à jour.
     * Peut être {@code null} pour indiquer que le dossier n'est pas encore fermé.
     */
    private Instant closedAt;

    /**
     * Date de l'incident (optionnelle).
     * Si fournie, la date sera mise à jour.
     */
    private LocalDate incidentDate;

    /**
     * Lieu de l'incident (optionnel).
     * Si fourni, le lieu sera mis à jour.
     */
    @Size(
            max = 255,
            message = "Le lieu ne peut pas dépasser 255 caractères"
    )
    private String location;
}