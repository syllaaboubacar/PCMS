package lu.police.pcms.attachment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**

* DTO utilisé pour modifier les métadonnées d'une pièce jointe (PUT).
*
* <p>
* Seuls les champs {@code mimeType} et {@code type} sont modifiables.
* </p>
*
* <p>
* Le fichier lui-même ne peut pas être remplacé via cette opération.
* Les champs {@code originalFilename}, {@code filename},
* {@code fileSize}, {@code storagePath} et {@code uploadedAt}
* ne sont pas modifiables.
* </p>
*
* <p>
* Pour remplacer le fichier physique, il faut supprimer l'ancienne
* pièce jointe puis créer une nouvelle pièce jointe.
* </p>

*/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAttachmentRequest {


/**
 * Type MIME du fichier.
 */
@NotBlank(message = "Le type MIME est obligatoire")
@Size(
    max = 100,
    message = "Le type MIME ne peut pas dépasser 100 caractères"
)
private String mimeType;

/**
 * Catégorie métier de la pièce jointe.
 */
@NotBlank(message = "Le type de pièce jointe est obligatoire")
@Size(
    max = 30,
    message = "Le type ne peut pas dépasser 30 caractères"
)
private String type;


}
