package lu.police.pcms.casecomment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lu.police.pcms.casecomment.dto.CaseCommentResponse;
import lu.police.pcms.casecomment.dto.CreateCaseCommentRequest;
import lu.police.pcms.casecomment.dto.PatchCaseCommentRequest;
import lu.police.pcms.casecomment.dto.UpdateCaseCommentRequest;
import lu.police.pcms.casecomment.service.CaseCommentService;
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
 * Contrôleur REST pour la gestion des commentaires (CaseComment).
 *
 * <p>
 * Ce contrôleur expose une API CRUD complète pour gérer les commentaires
 * associés aux dossiers d'enquête.
 * </p>
 *
 * <p>
 * Tous les endpoints sont préfixés par {@code /api/comments}.
 * </p>
 *
 * <p>
 * Spécificités :
 * </p>
 * <ul>
 *     <li>Un commentaire est toujours associé à un dossier et à un utilisateur.</li>
 *     <li>Seul le champ {@code content} peut être modifié après la création.</li>
 *     <li>Les relations (dossier, utilisateur) sont immuables.</li>
 * </ul>
 *
 * @see CaseCommentService
 * @see ApiResponse
 * @see CreateCaseCommentRequest
 * @see UpdateCaseCommentRequest
 * @see PatchCaseCommentRequest
 * @see CaseCommentResponse
 */
@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CaseCommentController {

    private final CaseCommentService commentService;

    // ============================================================
    // 1. CRÉATION D'UN COMMENTAIRE (POST)
    // ============================================================

    /**
     * Crée un nouveau commentaire.
     *
     * @param request DTO de création (caseFileId, userId, content)
     * @return Réponse HTTP 201 Created avec le commentaire créé
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si le dossier ou l'utilisateur n'existe pas
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CaseCommentResponse>> createComment(
            @Valid @RequestBody CreateCaseCommentRequest request) {

        log.info("Requête de création d'un commentaire : dossier {}, utilisateur {}",
                request.getCaseFileId(), request.getUserId());

        CaseCommentResponse created = commentService.createComment(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Commentaire créé avec succès", created));
    }

    // ============================================================
    // 2. LISTE DE TOUS LES COMMENTAIRES (GET)
    // ============================================================

    /**
     * Récupère la liste de tous les commentaires non supprimés.
     *
     * @return Réponse HTTP 200 OK avec la liste des commentaires
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CaseCommentResponse>>> getAllComments() {

        log.debug("Requête de récupération de tous les commentaires");

        List<CaseCommentResponse> comments = commentService.getAllComments();

        return ResponseEntity.ok(
                ApiResponse.success("Commentaires récupérés avec succès", comments)
        );
    }

    // ============================================================
    // 3. COMMENTAIRES D'UN DOSSIER (GET /case/{caseFileId})
    // ============================================================

    /**
     * Récupère tous les commentaires d'un dossier.
     *
     * @param caseFileId Identifiant du dossier
     * @return Réponse HTTP 200 OK avec la liste des commentaires du dossier
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si le dossier n'existe pas
     */
    @GetMapping("/case/{caseFileId}")
    public ResponseEntity<ApiResponse<List<CaseCommentResponse>>> getCommentsByCaseFile(
            @PathVariable Long caseFileId) {

        log.debug("Requête de récupération des commentaires du dossier : {}", caseFileId);

        List<CaseCommentResponse> comments = commentService.getCommentsByCaseFile(caseFileId);

        return ResponseEntity.ok(
                ApiResponse.success("Commentaires du dossier récupérés avec succès", comments)
        );
    }

    // ============================================================
    // 4. COMMENTAIRES D'UN UTILISATEUR (GET /user/{userId})
    // ============================================================

    /**
     * Récupère tous les commentaires rédigés par un utilisateur.
     *
     * @param userId Identifiant de l'utilisateur
     * @return Réponse HTTP 200 OK avec la liste des commentaires de l'utilisateur
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si l'utilisateur n'existe pas
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<CaseCommentResponse>>> getCommentsByUser(
            @PathVariable Long userId) {

        log.debug("Requête de récupération des commentaires de l'utilisateur : {}", userId);

        List<CaseCommentResponse> comments = commentService.getCommentsByUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Commentaires de l'utilisateur récupérés avec succès", comments)
        );
    }

    // ============================================================
    // 5. DÉTAIL D'UN COMMENTAIRE (GET /{id})
    // ============================================================

    /**
     * Récupère un commentaire par son identifiant.
     *
     * @param id Identifiant du commentaire
     * @return Réponse HTTP 200 OK avec le commentaire demandé
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si le commentaire n'existe pas ou est supprimé
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseCommentResponse>> getCommentById(
            @PathVariable Long id) {

        log.debug("Requête de récupération du commentaire ID : {}", id);

        CaseCommentResponse comment = commentService.getCommentById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Commentaire récupéré avec succès", comment)
        );
    }

    // ============================================================
    // 6. MISE À JOUR COMPLÈTE (PUT /{id})
    // ============================================================

    /**
     * Remplace complètement un commentaire existant (PUT).
     *
     * <p>
     * Seul le champ {@code content} est modifiable.
     * Les relations (dossier, utilisateur) sont immuables.
     * </p>
     *
     * @param id      Identifiant du commentaire
     * @param request DTO de mise à jour complète (content)
     * @return Réponse HTTP 200 OK avec le commentaire mis à jour
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si le commentaire n'existe pas ou est supprimé
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseCommentResponse>> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCaseCommentRequest request) {

        log.info("Requête de mise à jour complète du commentaire ID : {}", id);

        CaseCommentResponse updated = commentService.updateComment(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Commentaire mis à jour avec succès", updated)
        );
    }

    // ============================================================
    // 7. MISE À JOUR PARTIELLE (PATCH /{id})
    // ============================================================

    /**
     * Met à jour partiellement un commentaire existant (PATCH).
     *
     * <p>
     * Seul le champ {@code content} est modifiable et optionnel.
     * </p>
     *
     * @param id      Identifiant du commentaire
     * @param request DTO de mise à jour partielle (content optionnel)
     * @return Réponse HTTP 200 OK avec le commentaire mis à jour
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si le commentaire n'existe pas ou est supprimé
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseCommentResponse>> patchComment(
            @PathVariable Long id,
            @Valid @RequestBody PatchCaseCommentRequest request) {

        log.info("Requête de mise à jour partielle du commentaire ID : {}", id);

        CaseCommentResponse patched = commentService.patchComment(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Commentaire partiellement mis à jour", patched)
        );
    }

    // ============================================================
    // 8. SUPPRESSION LOGIQUE (DELETE /{id})
    // ============================================================

    /**
     * Supprime logiquement un commentaire (marque deleted = true).
     *
     * @param id Identifiant du commentaire
     * @return Réponse HTTP 204 No Content
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si le commentaire n'existe pas
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id) {

        log.info("Requête de suppression logique du commentaire ID : {}", id);

        commentService.deleteComment(id);

        return ResponseEntity.noContent().build();
    }
}