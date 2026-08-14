package lu.police.pcms.attachment.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**

* DTO utilisé pour modifier partiellement les métadonnées
* d'une pièce jointe (PATCH).
*
* <p>
* Tous les champs sont optionnels.
* Seuls les champs fournis seront mis à jour.
* </p>
*
* <p>
* Le fichier physique et ses informations techniques
* ne peuvent pas être modifiés via cette opération.
* </p>

*/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatchAttachmentRequest {


/**
 * Type MIME du fichier.
 */
@Size(
    max = 100,
    message = "Le type MIME ne peut pas dépasser 100 caractères"
)
private String mimeType;

/**
 * Catégorie métier de la pièce jointe.
 */
@Size(
    max = 30,
    message = "Le type ne peut pas dépasser 30 caractères"
)
private String type;


}
