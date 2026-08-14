package lu.police.pcms.caseassignment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO utilisé pour modifier partiellement une affectation (PATCH).
 *
 * <p>
 * Seul le champ {@code active} est modifiable. Il permet d'activer ou
 * de désactiver une affectation. Les autres champs (caseFile, user, assignedAt)
 * sont considérés comme immuables après la création.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatchCaseAssignmentRequest {

    /**
     * Nouvel état de l'affectation (actif ou inactif).
     * Si non fourni, la valeur reste inchangée.
     */
    private Boolean active;
}