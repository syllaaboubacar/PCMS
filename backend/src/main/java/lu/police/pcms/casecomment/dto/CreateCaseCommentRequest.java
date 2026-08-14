package lu.police.pcms.casecomment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO utilisé pour créer un nouveau commentaire (POST).
 *
 * <p>
 * Le commentaire est associé à un dossier et à un utilisateur.
 * Le contenu est obligatoire.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCaseCommentRequest {

    /**
     * Identifiant du dossier auquel le commentaire est rattaché.
     */
    @NotNull(message = "L'identifiant du dossier est obligatoire")
    private Long caseFileId;

    /**
     * Identifiant de l'utilisateur auteur du commentaire.
     */
    @NotNull(message = "L'identifiant de l'utilisateur est obligatoire")
    private Long userId;

    /**
     * Contenu du commentaire.
     */
    @NotBlank(message = "Le contenu du commentaire est obligatoire")
    private String content;
}

