package lu.police.pcms.casecomment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO utilisé pour modifier partiellement un commentaire (PATCH).
 *
 * <p>
 * Seul le contenu du commentaire peut être modifié.
 * Le champ est optionnel :
 * </p>
 *
 * <ul>
 *     <li>{@code null} → aucune modification du contenu</li>
 *     <li>valeur non vide → remplacement du contenu</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatchCaseCommentRequest {

    /**
     * Nouveau contenu du commentaire.
     *
     * <p>
     * Le champ peut être {@code null} lorsqu'aucune modification
     * n'est demandée. En revanche, lorsqu'il est fourni, il ne
     * peut pas être vide ou composé uniquement d'espaces.
     * </p>
     */
    @NotBlank(message = "Le contenu du commentaire ne peut pas être vide")
    private String content;
}
