package lu.police.pcms.casecomment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO utilisé pour remplacer complètement un commentaire (PUT).
 *
 * <p>
 * Seul le contenu du commentaire est modifiable.
 * L'association au dossier et à l'utilisateur ne peut pas être modifiée.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCaseCommentRequest {

    /**
     * Nouveau contenu du commentaire.
     */
    @NotBlank(message = "Le contenu du commentaire est obligatoire")
    private String content;
}