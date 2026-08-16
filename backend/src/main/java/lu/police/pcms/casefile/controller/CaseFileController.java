package lu.police.pcms.casefile.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lu.police.pcms.casefile.dto.CaseFileResponse;
import lu.police.pcms.casefile.dto.CreateCaseFileRequest;
import lu.police.pcms.casefile.dto.PatchCaseFileRequest;
import lu.police.pcms.casefile.dto.UpdateCaseFileRequest;
import lu.police.pcms.casefile.service.CaseFileService;
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
 * Contrôleur REST pour la gestion des dossiers d'enquête (CaseFile).
 *
 * <p>
 * Ce contrôleur expose une API CRUD complète pour gérer les dossiers.
 * Il utilise {@link CaseFileService} pour la logique métier et renvoie
 * des réponses uniformisées via {@link ApiResponse}.
 * </p>
 *
 * <p>
 * Tous les endpoints sont préfixés par {@code /api/cases}.
 * </p>
 *
 * <p>
 * Spécificités :
 * </p>
 * <ul>
 *     <li>Le {@code caseNumber} est immuable après la création.</li>
 *     <li>La date d'ouverture {@code openedAt} est définie à la création et non modifiable.</li>
 *     <li>Les champs {@code closedAt}, {@code incidentDate} et {@code location} sont optionnels.</li>
 * </ul>
 *
 * @see CaseFileService
 * @see ApiResponse
 * @see CreateCaseFileRequest
 * @see UpdateCaseFileRequest
 * @see PatchCaseFileRequest
 * @see CaseFileResponse
 */
@Slf4j
@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
@Tag(name = "Dossiers d'enquête", description = "Gestion des dossiers d'enquête (CRUD complet)")
public class CaseFileController {

    private final CaseFileService caseFileService;

    // ============================================================
    // 1. CRÉATION D'UN DOSSIER (POST)
    // ============================================================

    /**
     * Crée un nouveau dossier d'enquête.
     *
     * @param request DTO de création (inclut le numéro de dossier, titre, description, statut, priorité, etc.)
     * @return Réponse HTTP 201 Created avec le dossier créé
     * @throws lu.police.pcms.common.exception.DuplicateResourceException Si le numéro de dossier existe déjà
     */
    @Operation(
            summary = "Créer un nouveau dossier d'enquête",
            description = """
                    Crée un dossier avec un numéro unique, un titre, une description, un statut et une priorité.
                    
                    **Contraintes :**
                    - Le numéro de dossier doit commencer par 'PCMS_' et être unique.
                    - Le statut doit être l'une des valeurs : OPEN, IN_PROGRESS, ON_HOLD, CLOSED, ARCHIVED.
                    - La priorité doit être l'une des valeurs : LOW, MEDIUM, HIGH, CRITICAL.
                    - Les champs incidentDate et location sont optionnels.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Dossier créé avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide (validation échouée)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Un dossier avec ce numéro existe déjà")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CaseFileResponse>> createCaseFile(
            @Parameter(description = "Données du dossier à créer", required = true)
            @Valid @RequestBody CreateCaseFileRequest request) {

        log.info("Requête de création d'un dossier : {}", request.getCaseNumber());

        CaseFileResponse created = caseFileService.createCaseFile(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dossier créé avec succès", created));
    }

    // ============================================================
    // 2. LISTE DE TOUS LES DOSSIERS (GET)
    // ============================================================

    /**
     * Récupère la liste de tous les dossiers non supprimés.
     *
     * @return Réponse HTTP 200 OK avec la liste des dossiers
     */
    @Operation(
            summary = "Récupérer tous les dossiers",
            description = "Retourne la liste de tous les dossiers non supprimés (filtre automatique sur deleted=false)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<CaseFileResponse>>> getAllCaseFiles() {

        log.debug("Requête de récupération de tous les dossiers");

        List<CaseFileResponse> caseFiles = caseFileService.getAllCaseFiles();

        return ResponseEntity.ok(
                ApiResponse.success("Dossiers récupérés avec succès", caseFiles)
        );
    }

    // ============================================================
    // 3. DÉTAIL D'UN DOSSIER (GET /{id})
    // ============================================================

    /**
     * Récupère un dossier par son identifiant.
     *
     * @param id Identifiant du dossier
     * @return Réponse HTTP 200 OK avec le dossier demandé
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si le dossier n'existe pas ou est supprimé
     */
    @Operation(
            summary = "Récupérer un dossier par son ID",
            description = "Retourne les détails d'un dossier à partir de son identifiant technique."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dossier trouvé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dossier introuvable ou supprimé")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseFileResponse>> getCaseFileById(
            @Parameter(description = "Identifiant technique du dossier", required = true, example = "1")
            @PathVariable Long id) {

        log.debug("Requête de récupération du dossier ID : {}", id);

        CaseFileResponse caseFile = caseFileService.getCaseFileById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Dossier récupéré avec succès", caseFile)
        );
    }

    // ============================================================
    // 4. MISE À JOUR COMPLÈTE (PUT /{id})
    // ============================================================

    /**
     * Remplace complètement un dossier existant (PUT).
     *
     * <p>
     * ⚠️ Le numéro de dossier (caseNumber) et la date d'ouverture (openedAt)
     * ne sont pas modifiables et sont donc absents de la requête.
     * </p>
     *
     * @param id      Identifiant du dossier
     * @param request DTO de mise à jour complète
     * @return Réponse HTTP 200 OK avec le dossier mis à jour
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si le dossier n'existe pas ou est supprimé
     */
    @Operation(
            summary = "Remplacer complètement un dossier (PUT)",
            description = """
                    Remplace toutes les données d'un dossier existant.
                    
                    ⚠️ Tous les champs sont obligatoires.
                    Le numéro de dossier et la date d'ouverture sont immuables.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dossier mis à jour avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide (validation échouée)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dossier introuvable ou supprimé")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseFileResponse>> updateCaseFile(
            @Parameter(description = "Identifiant du dossier à modifier", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nouvelles données du dossier (tous les champs sauf caseNumber et openedAt)", required = true)
            @Valid @RequestBody UpdateCaseFileRequest request) {

        log.info("Requête de mise à jour complète du dossier ID : {}", id);

        CaseFileResponse updated = caseFileService.updateCaseFile(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Dossier mis à jour avec succès", updated)
        );
    }

    // ============================================================
    // 5. MISE À JOUR PARTIELLE (PATCH /{id})
    // ============================================================

    /**
     * Met à jour partiellement un dossier existant (PATCH).
     *
     * <p>
     * Tous les champs sont optionnels.
     * ⚠️ Le numéro de dossier (caseNumber) et la date d'ouverture (openedAt)
     * ne peuvent pas être modifiés.
     * </p>
     *
     * @param id      Identifiant du dossier
     * @param request DTO de mise à jour partielle (champs optionnels)
     * @return Réponse HTTP 200 OK avec le dossier mis à jour
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si le dossier n'existe pas ou est supprimé
     */
    @Operation(
            summary = "Modifier partiellement un dossier (PATCH)",
            description = """
                    Modifie un ou plusieurs champs d'un dossier existant.
                    
                    ⚠️ Tous les champs sont optionnels. Seuls les champs fournis seront mis à jour.
                    Le numéro de dossier et la date d'ouverture sont immuables.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dossier partiellement mis à jour",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dossier introuvable ou supprimé")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseFileResponse>> patchCaseFile(
            @Parameter(description = "Identifiant du dossier à modifier", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Champs à modifier (optionnels)", required = true)
            @Valid @RequestBody PatchCaseFileRequest request) {

        log.info("Requête de mise à jour partielle du dossier ID : {}", id);

        CaseFileResponse patched = caseFileService.patchCaseFile(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Dossier partiellement mis à jour", patched)
        );
    }

    // ============================================================
    // 6. SUPPRESSION LOGIQUE (DELETE /{id})
    // ============================================================

    /**
     * Supprime logiquement un dossier (marque deleted = true).
     *
     * @param id Identifiant du dossier
     * @return Réponse HTTP 204 No Content
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si le dossier n'existe pas
     */
    @Operation(
            summary = "Supprimer logiquement un dossier",
            description = """
                    Marque un dossier comme supprimé (deleted = true).
                    
                    ⚠️ Le dossier reste en base de données mais n'est plus accessible via les requêtes GET.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Dossier supprimé avec succès (aucun contenu retourné)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dossier introuvable")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCaseFile(
            @Parameter(description = "Identifiant du dossier à supprimer", required = true, example = "1")
            @PathVariable Long id) {

        log.info("Requête de suppression logique du dossier ID : {}", id);

        caseFileService.deleteCaseFile(id);

        return ResponseEntity.noContent().build();
    }
}