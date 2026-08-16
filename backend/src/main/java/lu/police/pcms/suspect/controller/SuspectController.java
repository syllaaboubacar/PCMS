package lu.police.pcms.suspect.controller;

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
@Tag(name = "Suspects", description = "Gestion des suspects (CRUD complet)")
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
    @Operation(
            summary = "Créer un nouveau suspect",
            description = """
                    Ajoute un suspect à un dossier d'enquête.
                    
                    **Contraintes :**
                    - Le couple (dossier, nom, prénom) doit être unique.
                    - Le nom et le prénom sont obligatoires.
                    - La date de naissance, la nationalité et les notes sont optionnelles.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Suspect créé avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide (validation échouée)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dossier introuvable"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Un suspect avec ce nom/prénom existe déjà dans ce dossier")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<SuspectResponse>> createSuspect(
            @Parameter(description = "Données du suspect à créer", required = true)
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
    @Operation(
            summary = "Récupérer tous les suspects",
            description = "Retourne la liste de tous les suspects non supprimés (filtre automatique sur deleted=false)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)))
    })
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
    @Operation(
            summary = "Récupérer les suspects d'un dossier",
            description = "Retourne la liste de tous les suspects associés à un dossier donné."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Suspects du dossier récupérés avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dossier introuvable")
    })
    @GetMapping("/case/{caseFileId}")
    public ResponseEntity<ApiResponse<List<SuspectResponse>>> getSuspectsByCaseFile(
            @Parameter(description = "Identifiant du dossier", required = true, example = "1")
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
    @Operation(
            summary = "Récupérer un suspect par son ID",
            description = "Retourne les détails d'un suspect à partir de son identifiant technique."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Suspect trouvé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Suspect introuvable ou supprimé")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SuspectResponse>> getSuspectById(
            @Parameter(description = "Identifiant technique du suspect", required = true, example = "1")
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
    @Operation(
            summary = "Remplacer complètement un suspect (PUT)",
            description = """
                    Remplace toutes les données d'un suspect existant.
                    
                    ⚠️ Le dossier d'origine ne peut pas être modifié.
                    Tous les champs sont obligatoires.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Suspect mis à jour avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide (validation échouée)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Suspect introuvable ou supprimé"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Un autre suspect avec ce nom/prénom existe déjà dans ce dossier")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SuspectResponse>> updateSuspect(
            @Parameter(description = "Identifiant du suspect à modifier", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nouvelles données du suspect (tous les champs sauf caseFileId)", required = true)
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
    @Operation(
            summary = "Modifier partiellement un suspect (PATCH)",
            description = """
                    Modifie un ou plusieurs champs d'un suspect existant.
                    
                    ⚠️ Tous les champs sont optionnels. Seuls les champs fournis seront mis à jour.
                    Le dossier d'origine ne peut pas être modifié.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Suspect partiellement mis à jour",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Suspect introuvable ou supprimé"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Un autre suspect avec ce nom/prénom existe déjà dans ce dossier")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<SuspectResponse>> patchSuspect(
            @Parameter(description = "Identifiant du suspect à modifier", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Champs à modifier (optionnels)", required = true)
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
    @Operation(
            summary = "Supprimer logiquement un suspect",
            description = """
                    Marque un suspect comme supprimé (deleted = true).
                    
                    ⚠️ Le suspect reste en base de données mais n'est plus accessible via les requêtes GET.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Suspect supprimé avec succès (aucun contenu retourné)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Suspect introuvable")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSuspect(
            @Parameter(description = "Identifiant du suspect à supprimer", required = true, example = "1")
            @PathVariable Long id) {

        log.info("Requête de suppression logique du suspect ID : {}", id);

        suspectService.deleteSuspect(id);

        return ResponseEntity.noContent().build();
    }
}