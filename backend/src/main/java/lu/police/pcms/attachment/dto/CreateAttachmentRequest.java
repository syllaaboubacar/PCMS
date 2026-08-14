package lu.police.pcms.attachment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**

* DTO utilisé pour créer une nouvelle pièce jointe (POST).
*
* <p>
* La pièce jointe doit être associée à un dossier existant
* via son identifiant.
* </p>
*
* <p>
* Les champs {@code filename}, {@code storagePath} et
* {@code uploadedAt} sont générés ou gérés automatiquement
* par le backend et ne doivent pas être fournis par le client.
* </p>

*/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAttachmentRequest {


/**
 * Identifiant du dossier auquel la pièce jointe est rattachée.
 */
@NotNull(message = "L'identifiant du dossier est obligatoire")
private Long caseFileId;

/**
 * Nom original du fichier tel qu'envoyé par le client.
 */
@NotBlank(message = "Le nom original du fichier est obligatoire")
@Size(
    max = 255,
    message = "Le nom original ne peut pas dépasser 255 caractères"
)
private String originalFilename;

/**
 * Type MIME du fichier.
 *
 * <p>
 * Exemples : image/jpeg, application/pdf, video/mp4.
 * </p>
 */
@NotBlank(message = "Le type MIME est obligatoire")
@Size(
    max = 100,
    message = "Le type MIME ne peut pas dépasser 100 caractères"
)
private String mimeType;

/**
 * Taille du fichier en octets.
 */
@NotNull(message = "La taille du fichier est obligatoire")
@Min(
    value = 0,
    message = "La taille du fichier ne peut pas être négative"
)
private Long fileSize;

/**
 * Catégorie métier de la pièce jointe.
 *
 * <p>
 * Exemples : PHOTO, VIDEO, DOCUMENT.
 * </p>
 */
@NotBlank(message = "Le type de pièce jointe est obligatoire")
@Size(
    max = 30,
    message = "Le type ne peut pas dépasser 30 caractères"
)
private String type;


}
