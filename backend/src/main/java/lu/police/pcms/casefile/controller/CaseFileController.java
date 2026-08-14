package lu.police.pcms.casefile.controller;

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
    @PostMapping
    public ResponseEntity<ApiResponse<CaseFileResponse>> createCaseFile(
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
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseFileResponse>> getCaseFileById(
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
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseFileResponse>> updateCaseFile(
            @PathVariable Long id,
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
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseFileResponse>> patchCaseFile(
            @PathVariable Long id,
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCaseFile(
            @PathVariable Long id) {

        log.info("Requête de suppression logique du dossier ID : {}", id);

        caseFileService.deleteCaseFile(id);

        return ResponseEntity.noContent().build();
    }
}