package lu.police.pcms.suspect.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lu.police.pcms.common.dto.ApiResponse;
import lu.police.pcms.suspect.dto.CreateSuspectRequest;
import lu.police.pcms.suspect.dto.PatchSuspectRequest;
import lu.police.pcms.suspect.dto.SuspectResponse;
import lu.police.pcms.suspect.dto.UpdateSuspectRequest;
import lu.police.pcms.suspect.service.SuspectService;
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
 * Contrôleur REST pour la gestion des suspects.
 *
 * <p>
 * Ce contrôleur expose une API CRUD complète pour gérer les suspects
 * associés aux dossiers d'enquête.
 * </p>
 *
 * <p>
 * Tous les endpoints sont préfixés par {@code /api/suspects}.
 * </p>
 *
 * <p>
 * Spécificités :
 * </p>
 * <ul>
 *     <li>Le couple (dossier, nom, prénom) doit être unique.</li>
 *     <li>Le dossier d'un suspect est immuable après la création.</li>
 *     <li>Les champs {@code birthDate}, {@code nationality} et {@code notes} sont optionnels.</li>
 * </ul>
 *
 * @see SuspectService
 * @see ApiResponse
 * @see CreateSuspectRequest
 * @see UpdateSuspectRequest
 * @see PatchSuspectRequest
 * @see SuspectResponse
 */
@Slf4j
@RestController
@RequestMapping("/api/suspects")
@RequiredArgsConstructor
public class SuspectController {

    private final SuspectService suspectService;

    // ============================================================
    // 1. CRÉATION D'UN SUSPECT (POST)
    // ============================================================

    /**
     * Crée un nouveau suspect.
     *
     * @param request DTO de création (caseFileId, firstName, lastName, etc.)
     * @return Réponse HTTP 201 Created avec le suspect créé
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException  Si le dossier n'existe pas
     * @throws lu.police.pcms.common.exception.DuplicateResourceException Si un suspect avec le même nom/prénom existe déjà dans le dossier
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SuspectResponse>> createSuspect(
            @Valid @RequestBody CreateSuspectRequest request) {

        log.info("Requête de création d'un suspect : {} {}, dossier {}",
                request.getFirstName(), request.getLastName(), request.getCaseFileId());

        SuspectResponse created = suspectService.createSuspect(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Suspect créé avec succès", created));
    }

    // ============================================================
    // 2. LISTE DE TOUS LES SUSPECTS (GET)
    // ============================================================

    /**
     * Récupère la liste de tous les suspects non supprimés.
     *
     * @return Réponse HTTP 200 OK avec la liste des suspects
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SuspectResponse>>> getAllSuspects() {

        log.debug("Requête de récupération de tous les suspects");

        List<SuspectResponse> suspects = suspectService.getAllSuspects();

        return ResponseEntity.ok(
                ApiResponse.success("Suspects récupérés avec succès", suspects)
        );
    }

    // ============================================================
    // 3. SUSPECTS D'UN DOSSIER (GET /case/{caseFileId})
    // ============================================================

    /**
     * Récupère tous les suspects d'un dossier.
     *
     * @param caseFileId Identifiant du dossier
     * @return Réponse HTTP 200 OK avec la liste des suspects du dossier
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si le dossier n'existe pas
     */
    @GetMapping("/case/{caseFileId}")
    public ResponseEntity<ApiResponse<List<SuspectResponse>>> getSuspectsByCaseFile(
            @PathVariable Long caseFileId) {

        log.debug("Requête de récupération des suspects du dossier : {}", caseFileId);

        List<SuspectResponse> suspects = suspectService.getSuspectsByCaseFile(caseFileId);

        return ResponseEntity.ok(
                ApiResponse.success("Suspects du dossier récupérés avec succès", suspects)
        );
    }

    // ============================================================
    // 4. DÉTAIL D'UN SUSPECT (GET /{id})
    // ============================================================

    /**
     * Récupère un suspect par son identifiant.
     *
     * @param id Identifiant du suspect
     * @return Réponse HTTP 200 OK avec le suspect demandé
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si le suspect n'existe pas ou est supprimé
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SuspectResponse>> getSuspectById(
            @PathVariable Long id) {

        log.debug("Requête de récupération du suspect ID : {}", id);

        SuspectResponse suspect = suspectService.getSuspectById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Suspect récupéré avec succès", suspect)
        );
    }

    // ============================================================
    // 5. MISE À JOUR COMPLÈTE (PUT /{id})
    // ============================================================

    /**
     * Remplace complètement un suspect existant (PUT).
     *
     * <p>
     * ⚠️ Le dossier (caseFile) est immuable et ne peut pas être modifié.
     * </p>
     *
     * @param id      Identifiant du suspect
     * @param request DTO de mise à jour complète (sans caseFileId)
     * @return Réponse HTTP 200 OK avec le suspect mis à jour
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException  Si le suspect n'existe pas ou est supprimé
     * @throws lu.police.pcms.common.exception.DuplicateResourceException Si les nouvelles valeurs violent la contrainte d'unicité
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SuspectResponse>> updateSuspect(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSuspectRequest request) {

        log.info("Requête de mise à jour complète du suspect ID : {}", id);

        SuspectResponse updated = suspectService.updateSuspect(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Suspect mis à jour avec succès", updated)
        );
    }

    // ============================================================
    // 6. MISE À JOUR PARTIELLE (PATCH /{id})
    // ============================================================

    /**
     * Met à jour partiellement un suspect existant (PATCH).
     *
     * <p>
     * Tous les champs sont optionnels.
     * ⚠️ Le dossier (caseFile) est immuable et ne peut pas être modifié.
     * </p>
     *
     * @param id      Identifiant du suspect
     * @param request DTO de mise à jour partielle (champs optionnels)
     * @return Réponse HTTP 200 OK avec le suspect mis à jour
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException  Si le suspect n'existe pas ou est supprimé
     * @throws lu.police.pcms.common.exception.DuplicateResourceException Si les nouvelles valeurs violent la contrainte d'unicité
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<SuspectResponse>> patchSuspect(
            @PathVariable Long id,
            @Valid @RequestBody PatchSuspectRequest request) {

        log.info("Requête de mise à jour partielle du suspect ID : {}", id);

        SuspectResponse patched = suspectService.patchSuspect(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Suspect partiellement mis à jour", patched)
        );
    }

    // ============================================================
    // 7. SUPPRESSION LOGIQUE (DELETE /{id})
    // ============================================================

    /**
     * Supprime logiquement un suspect (marque deleted = true).
     *
     * @param id Identifiant du suspect
     * @return Réponse HTTP 204 No Content
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si le suspect n'existe pas
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSuspect(
            @PathVariable Long id) {

        log.info("Requête de suppression logique du suspect ID : {}", id);

        suspectService.deleteSuspect(id);

        return ResponseEntity.noContent().build();
    }
}