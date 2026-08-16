package lu.police.pcms.role.controller;

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
import lu.police.pcms.role.dto.CreateRoleRequest;
import lu.police.pcms.role.dto.PatchRoleRequest;
import lu.police.pcms.role.dto.RoleResponse;
import lu.police.pcms.role.dto.UpdateRoleRequest;
import lu.police.pcms.role.service.RoleService;
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
 * Contrôleur REST pour la gestion des rôles.
 *
 * <p>
 * Ce contrôleur expose une API CRUD complète pour gérer les rôles du système.
 * Il utilise {@link RoleService} pour la logique métier et renvoie des
 * réponses uniformisées via {@link ApiResponse}.
 * </p>
 *
 * <p>
 * Tous les endpoints sont préfixés par {@code /api/roles}.
 * </p>
 *
 * <p>
 * Les réponses de succès sont encapsulées dans {@link ApiResponse}.
 * Les erreurs sont gérées par {@link lu.police.pcms.common.handler.GlobalExceptionHandler}.
 * </p>
 *
 * @see RoleService
 * @see ApiResponse
 * @see CreateRoleRequest
 * @see UpdateRoleRequest
 * @see PatchRoleRequest
 * @see RoleResponse
 */
@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Rôles", description = "Gestion des rôles utilisateur (CRUD complet)")
public class RoleController {

    private final RoleService roleService;

    // ============================================================
    // 1. CRÉATION D'UN RÔLE (POST)
    // ============================================================

    /**
     * Crée un nouveau rôle.
     *
     * @param request DTO contenant les informations du rôle à créer
     * @return Réponse HTTP 201 Created avec le rôle créé
     */
    @Operation(
            summary = "Créer un nouveau rôle",
            description = """
                    Crée un rôle avec un nom unique au format 'ROLE_XXX'.
                    
                    **Contraintes :**
                    - Le nom doit commencer par 'ROLE_'
                    - Le nom doit être en majuscules
                    - Longueur : entre 6 et 50 caractères
                    - Le nom doit être unique dans le système
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Rôle créé avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide (validation échouée)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Un rôle avec ce nom existe déjà")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Parameter(description = "Données du rôle à créer", required = true)
            @Valid @RequestBody CreateRoleRequest request) {

        log.info("Requête de création d'un nouveau rôle : {}", request.getName());

        RoleResponse created = roleService.createRole(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Rôle créé avec succès", created));
    }

    // ============================================================
    // 2. LISTE DE TOUS LES RÔLES (GET)
    // ============================================================

    /**
     * Récupère la liste de tous les rôles non supprimés.
     *
     * @return Réponse HTTP 200 OK avec la liste des rôles
     */
    @Operation(
            summary = "Récupérer tous les rôles",
            description = "Retourne la liste de tous les rôles non supprimés (filtre automatique sur deleted=false)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {

        log.debug("Requête de récupération de tous les rôles");

        List<RoleResponse> roles = roleService.getAllRoles();

        return ResponseEntity.ok(
                ApiResponse.success("Rôles récupérés avec succès", roles)
        );
    }

    // ============================================================
    // 3. DÉTAIL D'UN RÔLE (GET /{id})
    // ============================================================

    /**
     * Récupère un rôle par son identifiant.
     *
     * @param id Identifiant du rôle
     * @return Réponse HTTP 200 OK avec le rôle demandé
     */
    @Operation(
            summary = "Récupérer un rôle par son ID",
            description = "Retourne les détails d'un rôle à partir de son identifiant technique."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rôle trouvé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Rôle introuvable ou supprimé")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(
            @Parameter(description = "Identifiant technique du rôle", required = true, example = "1")
            @PathVariable Long id) {

        log.debug("Requête de récupération du rôle ID : {}", id);

        RoleResponse role = roleService.getRoleById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Rôle récupéré avec succès", role)
        );
    }

    // ============================================================
    // 4. MISE À JOUR COMPLÈTE (PUT /{id})
    // ============================================================

    /**
     * Remplace complètement un rôle existant (PUT).
     *
     * @param id      Identifiant du rôle
     * @param request DTO contenant les nouvelles informations du rôle
     * @return Réponse HTTP 200 OK avec le rôle mis à jour
     */
    @Operation(
            summary = "Remplacer complètement un rôle (PUT)",
            description = """
                    Remplace toutes les données d'un rôle existant.
                    
                    ⚠️ Tous les champs sont obligatoires.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rôle mis à jour avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide (validation échouée)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Rôle introuvable ou supprimé"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Un autre rôle utilise déjà ce nom")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @Parameter(description = "Identifiant du rôle à modifier", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nouvelles données du rôle (tous les champs)", required = true)
            @Valid @RequestBody UpdateRoleRequest request) {

        log.info("Requête de mise à jour complète du rôle ID : {}", id);

        RoleResponse updated = roleService.updateRole(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Rôle mis à jour avec succès", updated)
        );
    }

    // ============================================================
    // 5. MISE À JOUR PARTIELLE (PATCH /{id})
    // ============================================================

    /**
     * Met à jour partiellement un rôle existant (PATCH).
     *
     * @param id      Identifiant du rôle
     * @param request DTO contenant les champs à modifier (optionnels)
     * @return Réponse HTTP 200 OK avec le rôle mis à jour
     */
    @Operation(
            summary = "Modifier partiellement un rôle (PATCH)",
            description = """
                    Modifie un ou plusieurs champs d'un rôle existant.
                    
                    ⚠️ Tous les champs sont optionnels. Seuls les champs fournis seront mis à jour.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rôle partiellement mis à jour",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Rôle introuvable ou supprimé"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Un autre rôle utilise déjà ce nom")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> patchRole(
            @Parameter(description = "Identifiant du rôle à modifier", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Champs à modifier (optionnels)", required = true)
            @Valid @RequestBody PatchRoleRequest request) {

        log.info("Requête de mise à jour partielle du rôle ID : {}", id);

        RoleResponse patched = roleService.patchRole(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Rôle partiellement mis à jour", patched)
        );
    }

    // ============================================================
    // 6. SUPPRESSION LOGIQUE (DELETE /{id})
    // ============================================================

    /**
     * Supprime logiquement un rôle (marque deleted = true).
     *
     * @param id Identifiant du rôle
     * @return Réponse HTTP 204 No Content
     */
    @Operation(
            summary = "Supprimer logiquement un rôle",
            description = """
                    Marque un rôle comme supprimé (deleted = true).
                    
                    ⚠️ Le rôle reste en base de données mais n'est plus accessible via les requêtes GET.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Rôle supprimé avec succès (aucun contenu retourné)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Rôle introuvable")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(
            @Parameter(description = "Identifiant du rôle à supprimer", required = true, example = "1")
            @PathVariable Long id) {

        log.info("Requête de suppression logique du rôle ID : {}", id);

        roleService.deleteRole(id);

        return ResponseEntity.noContent().build();
    }
}