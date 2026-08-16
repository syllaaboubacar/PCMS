package lu.police.pcms.caseassignment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lu.police.pcms.caseassignment.dto.CaseAssignmentResponse;
import lu.police.pcms.caseassignment.dto.CreateCaseAssignmentRequest;
import lu.police.pcms.caseassignment.dto.PatchCaseAssignmentRequest;
import lu.police.pcms.caseassignment.dto.UpdateCaseAssignmentRequest;
import lu.police.pcms.caseassignment.service.CaseAssignmentService;
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
 * Contrôleur REST pour la gestion des affectations (CaseAssignment).
 *
 * <p>
 * Ce contrôleur expose une API CRUD complète pour gérer les affectations
 * des enquêteurs aux dossiers d'enquête.
 * </p>
 *
 * <p>
 * Tous les endpoints sont préfixés par {@code /api/assignments}.
 * </p>
 *
 * <p>
 * Spécificités :
 * </p>
 * <ul>
 *     <li>Le couple (dossier, utilisateur) doit être unique.</li>
 *     <li>La date d'affectation {@code assignedAt} est immuable.</li>
 *     <li>Seul le champ {@code active} peut être modifié après la création.</li>
 * </ul>
 *
 * @see CaseAssignmentService
 * @see ApiResponse
 * @see CreateCaseAssignmentRequest
 * @see UpdateCaseAssignmentRequest
 * @see PatchCaseAssignmentRequest
 * @see CaseAssignmentResponse
 */
@Slf4j
@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@Tag(name = "Affectations", description = "Gestion des affectations des enquêteurs aux dossiers (CRUD complet)")
public class CaseAssignmentController {

    private final CaseAssignmentService assignmentService;

    // ============================================================
    // 1. CRÉATION D'UNE AFFECTATION (POST)
    // ============================================================

    /**
     * Crée une nouvelle affectation.
     *
     * @param request DTO de création (caseFileId, userId, assignedAt optionnel)
     * @return Réponse HTTP 201 Created avec l'affectation créée
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException  Si le dossier ou l'utilisateur n'existe pas
     * @throws lu.police.pcms.common.exception.DuplicateResourceException Si une affectation existe déjà pour ce couple
     */
    @Operation(
            summary = "Créer une nouvelle affectation",
            description = """
                    Assigne un enquêteur (utilisateur) à un dossier d'enquête.
                    
                    **Contraintes :**
                    - Le couple (dossier, utilisateur) doit être unique.
                    - La date d'affectation est optionnelle (par défaut, l'instant courant).
                    - L'affectation est active par défaut (active = true).
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Affectation créée avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide (validation échouée)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dossier ou utilisateur introuvable"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Une affectation existe déjà pour ce couple")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CaseAssignmentResponse>> createAssignment(
            @Parameter(description = "Données de l'affectation à créer", required = true)
            @Valid @RequestBody CreateCaseAssignmentRequest request) {

        log.info("Requête de création d'une affectation : dossier={}, utilisateur={}",
                request.getCaseFileId(), request.getUserId());

        CaseAssignmentResponse created = assignmentService.createAssignment(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Affectation créée avec succès", created));
    }

    // ============================================================
    // 2. LISTE DE TOUTES LES AFFECTATIONS (GET)
    // ============================================================

    /**
     * Récupère la liste de toutes les affectations non supprimées.
     *
     * @return Réponse HTTP 200 OK avec la liste des affectations
     */
    @Operation(
            summary = "Récupérer toutes les affectations",
            description = "Retourne la liste de toutes les affectations non supprimées (filtre automatique sur deleted=false)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<CaseAssignmentResponse>>> getAllAssignments() {

        log.debug("Requête de récupération de toutes les affectations");

        List<CaseAssignmentResponse> assignments = assignmentService.getAllAssignments();

        return ResponseEntity.ok(
                ApiResponse.success("Affectations récupérées avec succès", assignments)
        );
    }

    // ============================================================
    // 3. AFFECTATIONS D'UN DOSSIER (GET /case/{caseFileId})
    // ============================================================

    /**
     * Récupère toutes les affectations d'un dossier.
     *
     * @param caseFileId Identifiant du dossier
     * @return Réponse HTTP 200 OK avec la liste des affectations du dossier
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si le dossier n'existe pas
     */
    @Operation(
            summary = "Récupérer les affectations d'un dossier",
            description = "Retourne la liste de toutes les affectations (actives ou non) associées à un dossier donné."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Affectations du dossier récupérées avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dossier introuvable")
    })
    @GetMapping("/case/{caseFileId}")
    public ResponseEntity<ApiResponse<List<CaseAssignmentResponse>>> getAssignmentsByCaseFile(
            @Parameter(description = "Identifiant du dossier", required = true, example = "1")
            @PathVariable Long caseFileId) {

        log.debug("Requête de récupération des affectations du dossier : {}", caseFileId);

        List<CaseAssignmentResponse> assignments = assignmentService.getAssignmentsByCaseFile(caseFileId);

        return ResponseEntity.ok(
                ApiResponse.success("Affectations du dossier récupérées avec succès", assignments)
        );
    }

    // ============================================================
    // 4. AFFECTATIONS D'UN UTILISATEUR (GET /user/{userId})
    // ============================================================

    /**
     * Récupère toutes les affectations d'un utilisateur.
     *
     * @param userId Identifiant de l'utilisateur
     * @return Réponse HTTP 200 OK avec la liste des affectations de l'utilisateur
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si l'utilisateur n'existe pas
     */
    @Operation(
            summary = "Récupérer les affectations d'un utilisateur",
            description = "Retourne la liste de toutes les affectations (actives ou non) d'un enquêteur donné."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Affectations de l'utilisateur récupérées avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<CaseAssignmentResponse>>> getAssignmentsByUser(
            @Parameter(description = "Identifiant de l'utilisateur (enquêteur)", required = true, example = "1")
            @PathVariable Long userId) {

        log.debug("Requête de récupération des affectations de l'utilisateur : {}", userId);

        List<CaseAssignmentResponse> assignments = assignmentService.getAssignmentsByUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Affectations de l'utilisateur récupérées avec succès", assignments)
        );
    }

    // ============================================================
    // 5. DÉTAIL D'UNE AFFECTATION (GET /{id})
    // ============================================================

    /**
     * Récupère une affectation par son identifiant.
     *
     * @param id Identifiant de l'affectation
     * @return Réponse HTTP 200 OK avec l'affectation demandée
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si l'affectation n'existe pas ou est supprimée
     */
    @Operation(
            summary = "Récupérer une affectation par son ID",
            description = "Retourne les détails d'une affectation à partir de son identifiant technique."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Affectation trouvée",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Affectation introuvable ou supprimée")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseAssignmentResponse>> getAssignmentById(
            @Parameter(description = "Identifiant technique de l'affectation", required = true, example = "1")
            @PathVariable Long id) {

        log.debug("Requête de récupération de l'affectation ID : {}", id);

        CaseAssignmentResponse assignment = assignmentService.getAssignmentById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Affectation récupérée avec succès", assignment)
        );
    }

    // ============================================================
    // 6. MISE À JOUR COMPLÈTE (PUT /{id})
    // ============================================================

    /**
     * Remplace complètement une affectation existante (PUT).
     *
     * <p>
     * ⚠️ Seul le champ {@code active} est modifiable.
     * Les relations (dossier, utilisateur) et {@code assignedAt} sont immuables.
     * </p>
     *
     * @param id      Identifiant de l'affectation
     * @param request DTO de mise à jour complète (seul active est modifiable)
     * @return Réponse HTTP 200 OK avec l'affectation mise à jour
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si l'affectation n'existe pas ou est supprimée
     */
    @Operation(
            summary = "Remplacer complètement une affectation (PUT)",
            description = """
                    Met à jour le statut d'une affectation existante.
                    
                    ⚠️ Seul le champ 'active' peut être modifié.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Affectation mise à jour avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide (validation échouée)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Affectation introuvable ou supprimée")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseAssignmentResponse>> updateAssignment(
            @Parameter(description = "Identifiant de l'affectation à modifier", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nouveau statut de l'affectation (active ou inactive)", required = true)
            @Valid @RequestBody UpdateCaseAssignmentRequest request) {

        log.info("Requête de mise à jour complète de l'affectation ID : {}", id);

        CaseAssignmentResponse updated = assignmentService.updateAssignment(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Affectation mise à jour avec succès", updated)
        );
    }

    // ============================================================
    // 7. MISE À JOUR PARTIELLE (PATCH /{id})
    // ============================================================

    /**
     * Met à jour partiellement une affectation existante (PATCH).
     *
     * <p>
     * Seul le champ {@code active} est modifiable et optionnel.
     * Les relations (dossier, utilisateur) et {@code assignedAt} sont immuables.
     * </p>
     *
     * @param id      Identifiant de l'affectation
     * @param request DTO de mise à jour partielle (active optionnel)
     * @return Réponse HTTP 200 OK avec l'affectation mise à jour
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si l'affectation n'existe pas ou est supprimée
     */
    @Operation(
            summary = "Modifier partiellement une affectation (PATCH)",
            description = """
                    Modifie le statut d'une affectation existante.
                    
                    ⚠️ Seul le champ 'active' peut être modifié. Il est optionnel.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Affectation partiellement mise à jour",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Affectation introuvable ou supprimée")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseAssignmentResponse>> patchAssignment(
            @Parameter(description = "Identifiant de l'affectation à modifier", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nouveau statut de l'affectation (optionnel)", required = true)
            @Valid @RequestBody PatchCaseAssignmentRequest request) {

        log.info("Requête de mise à jour partielle de l'affectation ID : {}", id);

        CaseAssignmentResponse patched = assignmentService.patchAssignment(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Affectation partiellement mise à jour", patched)
        );
    }

    // ============================================================
    // 8. SUPPRESSION LOGIQUE (DELETE /{id})
    // ============================================================

    /**
     * Supprime logiquement une affectation (marque deleted = true).
     *
     * @param id Identifiant de l'affectation
     * @return Réponse HTTP 204 No Content
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si l'affectation n'existe pas
     */
    @Operation(
            summary = "Supprimer logiquement une affectation",
            description = """
                    Marque une affectation comme supprimée (deleted = true).
                    
                    ⚠️ L'affectation reste en base de données mais n'est plus accessible via les requêtes GET.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Affectation supprimée avec succès (aucun contenu retourné)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Affectation introuvable")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(
            @Parameter(description = "Identifiant de l'affectation à supprimer", required = true, example = "1")
            @PathVariable Long id) {

        log.info("Requête de suppression logique de l'affectation ID : {}", id);

        assignmentService.deleteAssignment(id);

        return ResponseEntity.noContent().build();
    }
}