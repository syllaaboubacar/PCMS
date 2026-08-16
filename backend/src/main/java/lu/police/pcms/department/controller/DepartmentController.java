package lu.police.pcms.department.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lu.police.pcms.common.dto.ApiResponse;
import lu.police.pcms.department.dto.CreateDepartmentRequest;
import lu.police.pcms.department.dto.DepartmentResponse;
import lu.police.pcms.department.dto.PatchDepartmentRequest;
import lu.police.pcms.department.dto.UpdateDepartmentRequest;
import lu.police.pcms.department.service.DepartmentService;
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
 * Contrôleur REST pour la gestion des départements.
 *
 * <p>
 * Ce contrôleur expose une API CRUD complète pour gérer les départements.
 * Il utilise {@link DepartmentService} pour la logique métier et renvoie
 * des réponses uniformisées via {@link ApiResponse}.
 * </p>
 *
 * <p>
 * Tous les endpoints sont préfixés par {@code /api/departments}.
 * </p>
 *
 * @see DepartmentService
 * @see ApiResponse
 * @see CreateDepartmentRequest
 * @see UpdateDepartmentRequest
 * @see PatchDepartmentRequest
 * @see DepartmentResponse
 */
@Slf4j
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "Départements", description = "Gestion des départements (CRUD complet)")
public class DepartmentController {

    private final DepartmentService departmentService;

    // ============================================================
    // 1. CRÉATION D'UN DÉPARTEMENT (POST)
    // ============================================================

    /**
     * Crée un nouveau département.
     *
     * @param request DTO contenant le code et le nom du département
     * @return Réponse HTTP 201 Created avec le département créé
     * @throws lu.police.pcms.common.exception.DuplicateResourceException Si le code ou le nom existe déjà
     */
    @Operation(
            summary = "Créer un nouveau département",
            description = """
                    Crée un département avec un code unique et un nom unique.
                    
                    **Contraintes :**
                    - Le code doit être en majuscules, sans espaces, et ne contenir que des lettres majuscules, des chiffres, des tirets (-) et des underscores (_).
                    - Le code ne doit pas dépasser 20 caractères.
                    - Le nom ne doit pas dépasser 100 caractères.
                    - Le code et le nom doivent être uniques dans le système.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Département créé avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide (validation échouée)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Le code ou le nom existe déjà")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @Parameter(description = "Données du département à créer", required = true)
            @Valid @RequestBody CreateDepartmentRequest request) {

        log.info("Requête de création d'un département : code={}, name={}",
                request.getCode(), request.getName());

        DepartmentResponse created = departmentService.createDepartment(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Département créé avec succès", created));
    }

    // ============================================================
    // 2. LISTE DE TOUS LES DÉPARTEMENTS (GET)
    // ============================================================

    /**
     * Récupère la liste de tous les départements non supprimés.
     *
     * @return Réponse HTTP 200 OK avec la liste des départements
     */
    @Operation(
            summary = "Récupérer tous les départements",
            description = "Retourne la liste de tous les départements non supprimés (filtre automatique sur deleted=false)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments() {

        log.debug("Requête de récupération de tous les départements");

        List<DepartmentResponse> departments = departmentService.getAllDepartments();

        return ResponseEntity.ok(
                ApiResponse.success("Départements récupérés avec succès", departments)
        );
    }

    // ============================================================
    // 3. DÉTAIL D'UN DÉPARTEMENT (GET /{id})
    // ============================================================

    /**
     * Récupère un département par son identifiant.
     *
     * @param id Identifiant du département
     * @return Réponse HTTP 200 OK avec le département demandé
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si le département n'existe pas ou est supprimé
     */
    @Operation(
            summary = "Récupérer un département par son ID",
            description = "Retourne les détails d'un département à partir de son identifiant technique."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Département trouvé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Département introuvable ou supprimé")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(
            @Parameter(description = "Identifiant technique du département", required = true, example = "1")
            @PathVariable Long id) {

        log.debug("Requête de récupération du département ID : {}", id);

        DepartmentResponse department = departmentService.getDepartmentById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Département récupéré avec succès", department)
        );
    }

    // ============================================================
    // 4. MISE À JOUR COMPLÈTE (PUT /{id})
    // ============================================================

    /**
     * Remplace complètement un département existant (PUT).
     *
     * @param id      Identifiant du département
     * @param request DTO contenant les nouvelles informations
     * @return Réponse HTTP 200 OK avec le département mis à jour
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException   Si le département n'existe pas ou est supprimé
     * @throws lu.police.pcms.common.exception.DuplicateResourceException  Si le nouveau code ou nom est déjà utilisé
     */
    @Operation(
            summary = "Remplacer complètement un département (PUT)",
            description = """
                    Remplace toutes les données d'un département existant.
                    
                    ⚠️ Tous les champs sont obligatoires.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Département mis à jour avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide (validation échouée)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Département introuvable ou supprimé"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Un autre département utilise déjà ce code ou ce nom")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @Parameter(description = "Identifiant du département à modifier", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nouvelles données du département (tous les champs)", required = true)
            @Valid @RequestBody UpdateDepartmentRequest request) {

        log.info("Requête de mise à jour complète du département ID : {}", id);

        DepartmentResponse updated = departmentService.updateDepartment(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Département mis à jour avec succès", updated)
        );
    }

    // ============================================================
    // 5. MISE À JOUR PARTIELLE (PATCH /{id})
    // ============================================================

    /**
     * Met à jour partiellement un département existant (PATCH).
     *
     * @param id      Identifiant du département
     * @param request DTO contenant les champs à modifier (optionnels)
     * @return Réponse HTTP 200 OK avec le département mis à jour
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException   Si le département n'existe pas ou est supprimé
     * @throws lu.police.pcms.common.exception.DuplicateResourceException  Si le nouveau code ou nom est déjà utilisé
     */
    @Operation(
            summary = "Modifier partiellement un département (PATCH)",
            description = """
                    Modifie un ou plusieurs champs d'un département existant.
                    
                    ⚠️ Tous les champs sont optionnels. Seuls les champs fournis seront mis à jour.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Département partiellement mis à jour",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Département introuvable ou supprimé"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Un autre département utilise déjà ce code ou ce nom")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> patchDepartment(
            @Parameter(description = "Identifiant du département à modifier", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Champs à modifier (optionnels)", required = true)
            @Valid @RequestBody PatchDepartmentRequest request) {

        log.info("Requête de mise à jour partielle du département ID : {}", id);

        DepartmentResponse patched = departmentService.patchDepartment(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Département partiellement mis à jour", patched)
        );
    }

    // ============================================================
    // 6. SUPPRESSION LOGIQUE (DELETE /{id})
    // ============================================================

    /**
     * Supprime logiquement un département (marque deleted = true).
     *
     * @param id Identifiant du département
     * @return Réponse HTTP 204 No Content
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si le département n'existe pas
     */
    @Operation(
            summary = "Supprimer logiquement un département",
            description = """
                    Marque un département comme supprimé (deleted = true).
                    
                    ⚠️ Le département reste en base de données mais n'est plus accessible via les requêtes GET.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Département supprimé avec succès (aucun contenu retourné)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Département introuvable")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(
            @Parameter(description = "Identifiant du département à supprimer", required = true, example = "1")
            @PathVariable Long id) {

        log.info("Requête de suppression logique du département ID : {}", id);

        departmentService.deleteDepartment(id);

        return ResponseEntity.noContent().build();
    }
}