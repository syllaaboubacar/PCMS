package lu.police.pcms.caseassignment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO utilisé pour remplacer complètement une affectation (PUT).
 *
 * <p>
 * Tous les champs modifiables doivent être fournis.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCaseAssignmentRequest {

    @NotNull(message = "Le statut actif est obligatoire")
    private Boolean active;

    // On pourrait ajouter d'autres champs modifiables plus tard (ex: assignedAt)
    // private Instant assignedAt;
}