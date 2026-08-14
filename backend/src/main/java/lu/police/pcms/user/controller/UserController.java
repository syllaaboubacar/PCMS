package lu.police.pcms.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lu.police.pcms.common.dto.ApiResponse;
import lu.police.pcms.user.dto.CreateUserRequest;
import lu.police.pcms.user.dto.PatchUserRequest;
import lu.police.pcms.user.dto.UpdateUserRequest;
import lu.police.pcms.user.dto.UserResponse;
import lu.police.pcms.user.service.UserService;
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
 * Contrôleur REST pour la gestion des utilisateurs.
 *
 * <p>
 * Ce contrôleur expose une API CRUD complète pour gérer les utilisateurs.
 * Il utilise {@link UserService} pour la logique métier et renvoie
 * des réponses uniformisées via {@link ApiResponse}.
 * </p>
 *
 * <p>
 * Tous les endpoints sont préfixés par {@code /api/users}.
 * </p>
 *
 * <p>
 * Spécificités du module User :
 * </p>
 * <ul>
 *     <li>Le mot de passe est présent uniquement dans la création (POST).</li>
 *     <li>Les mises à jour (PUT/PATCH) ne modifient pas le mot de passe.</li>
 *     <li>La réponse inclut les détails du rôle et du département.</li>
 * </ul>
 *
 * @see UserService
 * @see ApiResponse
 * @see CreateUserRequest
 * @see UpdateUserRequest
 * @see PatchUserRequest
 * @see UserResponse
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ============================================================
    // 1. CRÉATION D'UN UTILISATEUR (POST)
    // ============================================================

    /**
     * Crée un nouvel utilisateur.
     *
     * @param request DTO contenant les informations de l'utilisateur
     *                (incluant le mot de passe, roleId et departmentId)
     * @return Réponse HTTP 201 Created avec l'utilisateur créé
     * @throws lu.police.pcms.common.exception.DuplicateResourceException Si l'email existe déjà
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException  Si le rôle ou le département n'existe pas
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        log.info("Requête de création d'un utilisateur : {}", request.getEmail());

        UserResponse created = userService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Utilisateur créé avec succès", created));
    }

    // ============================================================
    // 2. LISTE DE TOUS LES UTILISATEURS (GET)
    // ============================================================

    /**
     * Récupère la liste de tous les utilisateurs non supprimés.
     *
     * @return Réponse HTTP 200 OK avec la liste des utilisateurs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {

        log.debug("Requête de récupération de tous les utilisateurs");

        List<UserResponse> users = userService.getAllUsers();

        return ResponseEntity.ok(
                ApiResponse.success("Utilisateurs récupérés avec succès", users)
        );
    }

    // ============================================================
    // 3. DÉTAIL D'UN UTILISATEUR (GET /{id})
    // ============================================================

    /**
     * Récupère un utilisateur par son identifiant.
     *
     * @param id Identifiant de l'utilisateur
     * @return Réponse HTTP 200 OK avec l'utilisateur demandé
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si l'utilisateur n'existe pas ou est supprimé
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long id) {

        log.debug("Requête de récupération de l'utilisateur ID : {}", id);

        UserResponse user = userService.getUserById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Utilisateur récupéré avec succès", user)
        );
    }

    // ============================================================
    // 4. MISE À JOUR COMPLÈTE (PUT /{id})
    // ============================================================

    /**
     * Remplace complètement un utilisateur existant (PUT).
     *
     * <p>
     * ⚠️ Le mot de passe n'est pas modifiable via cette opération.
     * Seuls les champs métier sont mis à jour.
     * </p>
     *
     * @param id      Identifiant de l'utilisateur
     * @param request DTO contenant les nouvelles informations (sans mot de passe)
     * @return Réponse HTTP 200 OK avec l'utilisateur mis à jour
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException   Si l'utilisateur, le rôle ou le département n'existe pas
     * @throws lu.police.pcms.common.exception.DuplicateResourceException  Si le nouvel email est déjà utilisé
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        log.info("Requête de mise à jour complète de l'utilisateur ID : {}", id);

        UserResponse updated = userService.updateUser(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Utilisateur mis à jour avec succès", updated)
        );
    }

    // ============================================================
    // 5. MISE À JOUR PARTIELLE (PATCH /{id})
    // ============================================================

    /**
     * Met à jour partiellement un utilisateur existant (PATCH).
     *
     * <p>
     * Tous les champs sont optionnels.
     * ⚠️ Le mot de passe n'est pas modifiable via cette opération.
     * </p>
     *
     * @param id      Identifiant de l'utilisateur
     * @param request DTO contenant les champs à modifier (optionnels)
     * @return Réponse HTTP 200 OK avec l'utilisateur mis à jour
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException   Si l'utilisateur, le rôle ou le département n'existe pas
     * @throws lu.police.pcms.common.exception.DuplicateResourceException  Si le nouvel email est déjà utilisé
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> patchUser(
            @PathVariable Long id,
            @Valid @RequestBody PatchUserRequest request) {

        log.info("Requête de mise à jour partielle de l'utilisateur ID : {}", id);

        UserResponse patched = userService.patchUser(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Utilisateur partiellement mis à jour", patched)
        );
    }

    // ============================================================
    // 6. SUPPRESSION LOGIQUE (DELETE /{id})
    // ============================================================

    /**
     * Supprime logiquement un utilisateur (marque deleted = true).
     *
     * @param id Identifiant de l'utilisateur
     * @return Réponse HTTP 204 No Content
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si l'utilisateur n'existe pas
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id) {

        log.info("Requête de suppression logique de l'utilisateur ID : {}", id);

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}