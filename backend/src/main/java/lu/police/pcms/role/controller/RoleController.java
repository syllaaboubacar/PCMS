package lu.police.pcms.role.controller;

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
     * @see CreateRoleRequest
     * @see RoleResponse
     * @see lu.police.pcms.common.exception.DuplicateResourceException Si un rôle avec le même nom existe déjà
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
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
     * @see RoleResponse
     */
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
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si le rôle n'existe pas ou est supprimé
     * @see RoleResponse
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(
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
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException   Si le rôle n'existe pas ou est supprimé
     * @throws lu.police.pcms.common.exception.DuplicateResourceException  Si le nouveau nom est déjà utilisé
     * @see UpdateRoleRequest
     * @see RoleResponse
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Long id,
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
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException   Si le rôle n'existe pas ou est supprimé
     * @throws lu.police.pcms.common.exception.DuplicateResourceException  Si le nouveau nom est déjà utilisé
     * @see PatchRoleRequest
     * @see RoleResponse
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> patchRole(
            @PathVariable Long id,
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
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si le rôle n'existe pas
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(
            @PathVariable Long id) {

        log.info("Requête de suppression logique du rôle ID : {}", id);

        roleService.deleteRole(id);

        // 204 No Content : succès mais pas de contenu à renvoyer
        return ResponseEntity.noContent().build();
    }
}