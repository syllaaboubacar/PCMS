package lu.police.pcms.attachment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lu.police.pcms.attachment.dto.AttachmentResponse;
import lu.police.pcms.attachment.dto.CreateAttachmentRequest;
import lu.police.pcms.attachment.dto.PatchAttachmentRequest;
import lu.police.pcms.attachment.dto.UpdateAttachmentRequest;
import lu.police.pcms.attachment.service.AttachmentService;
import lu.police.pcms.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des pièces jointes (Attachment).
 *
 * <p>
 * Ce contrôleur expose une API CRUD complète pour gérer les pièces jointes
 * associées aux dossiers d'enquête.
 * </p>
 *
 * <p>
 * Tous les endpoints sont préfixés par {@code /api/attachments}.
 * </p>
 *
 * <p>
 * Spécificités :
 * </p>
 * <ul>
 *     <li>Les champs {@code filename} et {@code storagePath} sont générés automatiquement par le service.</li>
 *     <li>Seuls {@code mimeType} et {@code type} sont modifiables après la création.</li>
 *     <li>Le fichier physique ne peut pas être remplacé ; il faut supprimer l'ancien et en créer un nouveau.</li>
 * </ul>
 *
 * @see AttachmentService
 * @see ApiResponse
 * @see CreateAttachmentRequest
 * @see UpdateAttachmentRequest
 * @see PatchAttachmentRequest
 * @see AttachmentResponse
 */
@Slf4j
@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    // ============================================================
    // 1. CRÉATION D'UNE PIÈCE JOINTE (POST)
    // ============================================================

    /**
     * Crée une nouvelle pièce jointe.
     *
     * @param request DTO de création (caseFileId, originalFilename, mimeType, fileSize, type)
     * @return Réponse HTTP 201 Created avec la pièce jointe créée
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException  Si le dossier n'existe pas
     * @throws lu.police.pcms.common.exception.DuplicateResourceException Si un nom de fichier interne existe déjà (peu probable)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AttachmentResponse>> createAttachment(
            @Valid @RequestBody CreateAttachmentRequest request) {

        log.info("Requête de création d'une pièce jointe pour le dossier : {}", request.getCaseFileId());

        AttachmentResponse created = attachmentService.createAttachment(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pièce jointe créée avec succès", created));
    }

    // ============================================================
    // 2. LISTE DE TOUTES LES PIÈCES JOINTES (GET)
    // ============================================================

    /**
     * Récupère la liste de toutes les pièces jointes non supprimées.
     *
     * @return Réponse HTTP 200 OK avec la liste des pièces jointes
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getAllAttachments() {

        log.debug("Requête de récupération de toutes les pièces jointes");

        List<AttachmentResponse> attachments = attachmentService.getAllAttachments();

        return ResponseEntity.ok(
                ApiResponse.success("Pièces jointes récupérées avec succès", attachments)
        );
    }

    // ============================================================
    // 3. PIÈCES JOINTES D'UN DOSSIER (GET /case/{caseFileId})
    // ============================================================

    /**
     * Récupère toutes les pièces jointes d'un dossier.
     *
     * @param caseFileId Identifiant du dossier
     * @return Réponse HTTP 200 OK avec la liste des pièces jointes du dossier
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si le dossier n'existe pas
     */
    @GetMapping("/case/{caseFileId}")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getAttachmentsByCaseFile(
            @PathVariable Long caseFileId) {

        log.debug("Requête de récupération des pièces jointes du dossier : {}", caseFileId);

        List<AttachmentResponse> attachments = attachmentService.getAttachmentsByCaseFile(caseFileId);

        return ResponseEntity.ok(
                ApiResponse.success("Pièces jointes du dossier récupérées avec succès", attachments)
        );
    }

    // ============================================================
    // 4. DÉTAIL D'UNE PIÈCE JOINTE (GET /{id})
    // ============================================================

    /**
     * Récupère une pièce jointe par son identifiant.
     *
     * @param id Identifiant de la pièce jointe
     * @return Réponse HTTP 200 OK avec la pièce jointe demandée
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si la pièce jointe n'existe pas ou est supprimée
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttachmentResponse>> getAttachmentById(
            @PathVariable Long id) {

        log.debug("Requête de récupération de la pièce jointe ID : {}", id);

        AttachmentResponse attachment = attachmentService.getAttachmentById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Pièce jointe récupérée avec succès", attachment)
        );
    }

    // ============================================================
    // 5. MISE À JOUR COMPLÈTE (PUT /{id})
    // ============================================================

    /**
     * Remplace complètement les métadonnées d'une pièce jointe existante (PUT).
     *
     * <p>
     * ⚠️ Seuls les champs {@code mimeType} et {@code type} sont modifiables.
     * Les autres champs (filename, originalFilename, fileSize, storagePath, uploadedAt)
     * sont immuables.
     * </p>
     *
     * @param id      Identifiant de la pièce jointe
     * @param request DTO de mise à jour complète (mimeType, type)
     * @return Réponse HTTP 200 OK avec la pièce jointe mise à jour
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si la pièce jointe n'existe pas ou est supprimée
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AttachmentResponse>> updateAttachment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAttachmentRequest request) {

        log.info("Requête de mise à jour complète de la pièce jointe ID : {}", id);

        AttachmentResponse updated = attachmentService.updateAttachment(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Pièce jointe mise à jour avec succès", updated)
        );
    }

    // ============================================================
    // 6. MISE À JOUR PARTIELLE (PATCH /{id})
    // ============================================================

    /**
     * Met à jour partiellement les métadonnées d'une pièce jointe (PATCH).
     *
     * <p>
     * Tous les champs sont optionnels.
     * ⚠️ Seuls {@code mimeType} et {@code type} peuvent être modifiés.
     * </p>
     *
     * @param id      Identifiant de la pièce jointe
     * @param request DTO de mise à jour partielle (champs optionnels)
     * @return Réponse HTTP 200 OK avec la pièce jointe mise à jour
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si la pièce jointe n'existe pas ou est supprimée
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AttachmentResponse>> patchAttachment(
            @PathVariable Long id,
            @Valid @RequestBody PatchAttachmentRequest request) {

        log.info("Requête de mise à jour partielle de la pièce jointe ID : {}", id);

        AttachmentResponse patched = attachmentService.patchAttachment(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Pièce jointe partiellement mise à jour", patched)
        );
    }

    // ============================================================
    // 7. SUPPRESSION LOGIQUE (DELETE /{id})
    // ============================================================

    /**
     * Supprime logiquement une pièce jointe (marque deleted = true).
     *
     * @param id Identifiant de la pièce jointe
     * @return Réponse HTTP 204 No Content
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si la pièce jointe n'existe pas
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long id) {

        log.info("Requête de suppression logique de la pièce jointe ID : {}", id);

        attachmentService.deleteAttachment(id);

        return ResponseEntity.noContent().build();
    }
}