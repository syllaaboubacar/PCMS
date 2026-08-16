package lu.police.pcms.casecomment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Commentaires", description = "Gestion des commentaires des dossiers d'enquête (CRUD complet)")
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
    @Operation(
            summary = "Créer un nouveau commentaire",
            description = """
                    Ajoute un commentaire à un dossier d'enquête.
                    
                    **Contraintes :**
                    - Le commentaire est associé à un dossier et à un utilisateur.
                    - Le contenu est obligatoire.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Commentaire créé avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide (validation échouée)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dossier ou utilisateur introuvable")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CaseCommentResponse>> createComment(
            @Parameter(description = "Données du commentaire à créer", required = true)
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
    @Operation(
            summary = "Récupérer tous les commentaires",
            description = "Retourne la liste de tous les commentaires non supprimés (filtre automatique sur deleted=false)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)))
    })
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
    @Operation(
            summary = "Récupérer les commentaires d'un dossier",
            description = "Retourne la liste de tous les commentaires associés à un dossier donné."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Commentaires du dossier récupérés avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dossier introuvable")
    })
    @GetMapping("/case/{caseFileId}")
    public ResponseEntity<ApiResponse<List<CaseCommentResponse>>> getCommentsByCaseFile(
            @Parameter(description = "Identifiant du dossier", required = true, example = "1")
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
    @Operation(
            summary = "Récupérer les commentaires d'un utilisateur",
            description = "Retourne la liste de tous les commentaires rédigés par un utilisateur donné."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Commentaires de l'utilisateur récupérés avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<CaseCommentResponse>>> getCommentsByUser(
            @Parameter(description = "Identifiant de l'utilisateur", required = true, example = "1")
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
    @Operation(
            summary = "Récupérer un commentaire par son ID",
            description = "Retourne les détails d'un commentaire à partir de son identifiant technique."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Commentaire trouvé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Commentaire introuvable ou supprimé")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseCommentResponse>> getCommentById(
            @Parameter(description = "Identifiant technique du commentaire", required = true, example = "1")
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
    @Operation(
            summary = "Remplacer complètement un commentaire (PUT)",
            description = """
                    Remplace le contenu d'un commentaire existant.
                    
                    ⚠️ Seul le champ 'content' peut être modifié.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Commentaire mis à jour avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide (validation échouée)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Commentaire introuvable ou supprimé")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseCommentResponse>> updateComment(
            @Parameter(description = "Identifiant du commentaire à modifier", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nouveau contenu du commentaire", required = true)
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
    @Operation(
            summary = "Modifier partiellement un commentaire (PATCH)",
            description = """
                    Modifie le contenu d'un commentaire existant.
                    
                    ⚠️ Seul le champ 'content' peut être modifié. Il est optionnel.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Commentaire partiellement mis à jour",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Commentaire introuvable ou supprimé")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseCommentResponse>> patchComment(
            @Parameter(description = "Identifiant du commentaire à modifier", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nouveau contenu du commentaire (optionnel)", required = true)
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
    @Operation(
            summary = "Supprimer logiquement un commentaire",
            description = """
                    Marque un commentaire comme supprimé (deleted = true).
                    
                    ⚠️ Le commentaire reste en base de données mais n'est plus accessible via les requêtes GET.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Commentaire supprimé avec succès (aucun contenu retourné)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Commentaire introuvable")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "Identifiant du commentaire à supprimer", required = true, example = "1")
            @PathVariable Long id) {

        log.info("Requête de suppression logique du commentaire ID : {}", id);

        commentService.deleteComment(id);

        return ResponseEntity.noContent().build();
    }
}