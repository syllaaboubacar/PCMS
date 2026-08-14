package lu.police.pcms.department.controller;

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
    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
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
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(
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
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable Long id,
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
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> patchDepartment(
            @PathVariable Long id,
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(
            @PathVariable Long id) {

        log.info("Requête de suppression logique du département ID : {}", id);

        departmentService.deleteDepartment(id);

        return ResponseEntity.noContent().build();
    }
}