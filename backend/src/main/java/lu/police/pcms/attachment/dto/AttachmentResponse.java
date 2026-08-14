package lu.police.pcms.attachment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**

* DTO retourné par l'API REST pour représenter une pièce jointe.
  */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public class AttachmentResponse {

  /**

  * Identifiant de la pièce jointe.
    */
    private Long id;

  /**

  * Identifiant du dossier associé.
    */
    private Long caseFileId;

  /**

  * Nom interne du fichier généré par le système.
    */
    private String filename;

  /**

  * Nom original du fichier.
    */
    private String originalFilename;

  /**

  * Type MIME du fichier.
    */
    private String mimeType;

  /**

  * Taille du fichier en octets.
    */
    private Long fileSize;

  /**

  * Chemin de stockage du fichier.
    */
    private String storagePath;

  /**

  * Catégorie métier de la pièce jointe.
    */
    private String type;

  /**

  * Date et heure du téléversement.
    */
    @JsonFormat(
    shape = JsonFormat.Shape.STRING,
    pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
    timezone = "UTC"
    )
    private Instant uploadedAt;

  // ============================================================
  // CHAMPS D'AUDIT
  // ============================================================

  /**

  * Date de création de l'enregistrement.
    */
    @JsonFormat(
    shape = JsonFormat.Shape.STRING,
    pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
    timezone = "UTC"
    )
    private Instant createdAt;

  /**

  * Utilisateur ayant créé l'enregistrement.
    */
    private String createdBy;

  /**

  * Date de dernière modification.
    */
    @JsonFormat(
    shape = JsonFormat.Shape.STRING,
    pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
    timezone = "UTC"
    )
    private Instant updatedAt;

  /**

  * Utilisateur ayant effectué la dernière modification.
    */
    private String updatedBy;
    }
