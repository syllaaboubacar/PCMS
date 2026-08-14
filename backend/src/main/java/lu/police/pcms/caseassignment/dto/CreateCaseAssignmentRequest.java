package lu.police.pcms.caseassignment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * DTO utilisé pour créer une nouvelle affectation (POST).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCaseAssignmentRequest {

    /**
     * Identifiant du dossier à affecter.
     */
    @NotNull(message = "L'identifiant du dossier est obligatoire")
    private Long caseFileId;

    /**
     * Identifiant de l'utilisateur (enquêteur) à affecter.
     */
    @NotNull(message = "L'identifiant de l'utilisateur est obligatoire")
    private Long userId;

    /**
     * Date et heure de l'affectation (optionnelle).
     * Si non fournie, l'instant courant sera utilisé.
     */
    private Instant assignedAt;
}