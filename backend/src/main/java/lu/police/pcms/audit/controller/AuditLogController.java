package lu.police.pcms.audit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lu.police.pcms.audit.dto.AuditLogResponse;
import lu.police.pcms.audit.dto.CreateAuditLogRequest;
import lu.police.pcms.audit.service.AuditLogService;
import lu.police.pcms.common.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des journaux d'audit (AuditLog).
 *
 * <p>
 * Les logs d'audit sont immuables : seules les opérations de création
 * et de consultation sont autorisées.
 * </p>
 *
 * <p>
 * Tous les endpoints sont préfixés par {@code /api/audit-logs}.
 * </p>
 *
 * <p>
 * Les réponses sont encapsulées dans {@link ApiResponse}.
 * </p>
 *
 * @see AuditLogService
 * @see ApiResponse
 * @see CreateAuditLogRequest
 * @see AuditLogResponse
 */
@Slf4j
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Journalisation des actions (création et consultation uniquement)")
public class AuditLogController {

    private final AuditLogService auditLogService;

    // ============================================================
    // 1. CRÉATION D'UN LOG (POST)
    // ============================================================

    /**
     * Crée un nouveau journal d'audit.
     *
     * @param request DTO de création (userId, action, entityName, entityId,
     *                details (optionnel), ipAddress (optionnel))
     * @return Réponse HTTP 201 Created avec le log créé
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si l'utilisateur n'existe pas
     */
    @Operation(
            summary = "Créer un log d'audit",
            description = """
                    Enregistre une action effectuée par un utilisateur sur une entité métier.
                    
                    **Contraintes :**
                    - L'utilisateur doit exister.
                    - L'action, le nom de l'entité et l'identifiant de l'entité sont obligatoires.
                    - Les logs sont immuables : ils ne peuvent pas être modifiés ou supprimés.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Log d'audit créé avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requête invalide (validation échouée)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<AuditLogResponse>> createAuditLog(
            @Parameter(description = "Données du log d'audit à créer", required = true)
            @Valid @RequestBody CreateAuditLogRequest request) {

        log.info("Requête de création d'un log d'audit : action={}, entity={}, entityId={}",
                request.getAction(), request.getEntityName(), request.getEntityId());

        AuditLogResponse created = auditLogService.createAuditLog(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Log d'audit créé avec succès", created));
    }

    // ============================================================
    // 2. LISTE DE TOUS LES LOGS (GET) avec pagination
    // ============================================================

    /**
     * Récupère la liste paginée de tous les logs d'audit.
     *
     * @param pageable Objet de pagination (page, size, sort)
     * @return Réponse HTTP 200 OK avec la page de logs
     */
    @Operation(
            summary = "Récupérer tous les logs d'audit (paginé)",
            description = """
                    Retourne une page de logs d'audit triés par défaut par date de création décroissante.
                    
                    **Paramètres de pagination :**
                    - page : numéro de la page (défaut: 0)
                    - size : nombre d'éléments par page (défaut: 20)
                    - sort : champ de tri (ex: createdAt, action, entityName)
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logs récupérés avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAllAuditLogs(
            @Parameter(description = "Paramètres de pagination (page, size, sort)", hidden = true)
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Requête de récupération paginée des logs d'audit");

        Page<AuditLogResponse> page = auditLogService.getAuditLogsPaginated(pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Logs d'audit récupérés avec succès", page)
        );
    }

    // ============================================================
    // 3. DÉTAIL D'UN LOG (GET /{id})
    // ============================================================

    /**
     * Récupère un log d'audit par son identifiant.
     *
     * @param id Identifiant du log
     * @return Réponse HTTP 200 OK avec le log demandé
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si le log n'existe pas
     */
    @Operation(
            summary = "Récupérer un log d'audit par son ID",
            description = "Retourne les détails d'un log d'audit à partir de son identifiant technique."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Log trouvé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Log introuvable")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getAuditLogById(
            @Parameter(description = "Identifiant technique du log d'audit", required = true, example = "1")
            @PathVariable Long id) {

        log.debug("Requête de récupération du log d'audit ID : {}", id);

        AuditLogResponse log = auditLogService.getAuditLogById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Log d'audit récupéré avec succès", log)
        );
    }

    // ============================================================
    // 4. LOGS D'UN UTILISATEUR (GET /user/{userId})
    // ============================================================

    /**
     * Récupère tous les logs d'audit d'un utilisateur.
     *
     * @param userId Identifiant de l'utilisateur
     * @return Réponse HTTP 200 OK avec la liste des logs
     * @throws lu.police.pcms.common.exception.ResourceNotFoundException Si l'utilisateur n'existe pas
     */
    @Operation(
            summary = "Récupérer les logs d'un utilisateur",
            description = "Retourne la liste de tous les logs d'audit associés à un utilisateur donné."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logs de l'utilisateur récupérés avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsByUser(
            @Parameter(description = "Identifiant de l'utilisateur", required = true, example = "1")
            @PathVariable Long userId) {

        log.debug("Requête de récupération des logs de l'utilisateur : {}", userId);

        List<AuditLogResponse> logs = auditLogService.getAuditLogsByUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Logs de l'utilisateur récupérés avec succès", logs)
        );
    }

    // ============================================================
    // 5. LOGS PAR ACTION (GET /action/{action})
    // ============================================================

    /**
     * Récupère les logs d'audit par type d'action.
     *
     * @param action Type d'action (ex: CREATE, UPDATE, DELETE)
     * @return Réponse HTTP 200 OK avec la liste des logs
     */
    @Operation(
            summary = "Récupérer les logs par type d'action",
            description = """
                    Retourne la liste de tous les logs d'audit correspondant à un type d'action donné.
                    
                    **Actions courantes :** CREATE, UPDATE, DELETE, LOGIN, LOGOUT
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logs par action récupérés avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/action/{action}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsByAction(
            @Parameter(description = "Type d'action (ex: CREATE, UPDATE, DELETE)", required = true, example = "CREATE")
            @PathVariable String action) {

        log.debug("Requête de récupération des logs par action : {}", action);

        List<AuditLogResponse> logs = auditLogService.getAuditLogsByAction(action);

        return ResponseEntity.ok(
                ApiResponse.success("Logs par action récupérés avec succès", logs)
        );
    }

    // ============================================================
    // 6. LOGS PAR ENTITÉ (GET /entity/{entityName})
    // ============================================================

    /**
     * Récupère les logs d'audit concernant une entité spécifique.
     *
     * @param entityName Nom de l'entité (ex: USER, CASE, ROLE)
     * @return Réponse HTTP 200 OK avec la liste des logs
     */
    @Operation(
            summary = "Récupérer les logs par nom d'entité",
            description = """
                    Retourne la liste de tous les logs d'audit concernant une entité métier donnée.
                    
                    **Entités courantes :** USER, ROLE, DEPARTMENT, CASE, ASSIGNMENT, SUSPECT, ATTACHMENT, COMMENT
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logs par entité récupérés avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/entity/{entityName}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsByEntityName(
            @Parameter(description = "Nom de l'entité (ex: USER, CASE, ROLE)", required = true, example = "USER")
            @PathVariable String entityName) {

        log.debug("Requête de récupération des logs par entité : {}", entityName);

        List<AuditLogResponse> logs = auditLogService.getAuditLogsByEntityName(entityName);

        return ResponseEntity.ok(
                ApiResponse.success("Logs par entité récupérés avec succès", logs)
        );
    }

    // ============================================================
    // 7. LOGS POUR UN ENREGISTREMENT SPÉCIFIQUE (GET /entity/{entityName}/{entityId})
    // ============================================================

    /**
     * Récupère les logs d'audit concernant un enregistrement précis.
     *
     * @param entityName Nom de l'entité
     * @param entityId   Identifiant de l'enregistrement
     * @return Réponse HTTP 200 OK avec la liste des logs
     */
    @Operation(
            summary = "Récupérer les logs pour un enregistrement spécifique",
            description = "Retourne la liste de tous les logs d'audit concernant un enregistrement précis (entité + ID)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logs pour l'enregistrement récupérés avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/entity/{entityName}/{entityId}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsByEntityNameAndEntityId(
            @Parameter(description = "Nom de l'entité", required = true, example = "ROLE")
            @PathVariable String entityName,
            @Parameter(description = "Identifiant de l'enregistrement", required = true, example = "1")
            @PathVariable Long entityId) {

        log.debug("Requête de récupération des logs pour entité {} et ID {}", entityName, entityId);

        List<AuditLogResponse> logs = auditLogService.getAuditLogsByEntityNameAndEntityId(entityName, entityId);

        return ResponseEntity.ok(
                ApiResponse.success("Logs pour l'enregistrement récupérés avec succès", logs)
        );
    }

    // ============================================================
    // 8. LOGS APRÈS UNE DATE (GET /after/{date})
    // ============================================================

    /**
     * Récupère les logs d'audit créés après une date donnée.
     *
     * @param date Date au format ISO-8601 (ex: 2026-08-11T10:00:00Z)
     * @return Réponse HTTP 200 OK avec la liste des logs
     */
    @Operation(
            summary = "Récupérer les logs après une date",
            description = "Retourne la liste de tous les logs d'audit créés après une date donnée (format ISO-8601)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logs après la date récupérés avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/after/{date}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsAfter(
            @Parameter(description = "Date minimale (format ISO-8601, ex: 2026-08-11T10:00:00Z)", required = true)
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date) {

        log.debug("Requête de récupération des logs après : {}", date);

        List<AuditLogResponse> logs = auditLogService.getAuditLogsAfter(date);

        return ResponseEntity.ok(
                ApiResponse.success("Logs après la date récupérés avec succès", logs)
        );
    }

    // ============================================================
    // 9. LOGS ENTRE DEUX DATES (GET /between?start=...&end=...)
    // ============================================================

    /**
     * Récupère les logs d'audit entre deux dates.
     *
     * @param start Date de début (format ISO-8601)
     * @param end   Date de fin (format ISO-8601)
     * @return Réponse HTTP 200 OK avec la liste des logs
     */
    @Operation(
            summary = "Récupérer les logs entre deux dates",
            description = "Retourne la liste de tous les logs d'audit créés entre deux dates (format ISO-8601)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logs entre les dates récupérés avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/between")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsBetween(
            @Parameter(description = "Date de début (format ISO-8601)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @Parameter(description = "Date de fin (format ISO-8601)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {

        log.debug("Requête de récupération des logs entre {} et {}", start, end);

        List<AuditLogResponse> logs = auditLogService.getAuditLogsBetween(start, end);

        return ResponseEntity.ok(
                ApiResponse.success("Logs entre les dates récupérés avec succès", logs)
        );
    }
}