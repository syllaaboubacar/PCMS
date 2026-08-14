package lu.police.pcms.audit.controller;

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
    @PostMapping
    public ResponseEntity<ApiResponse<AuditLogResponse>> createAuditLog(
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
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAllAuditLogs(
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
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getAuditLogById(
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
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsByUser(
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
    @GetMapping("/action/{action}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsByAction(
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
    @GetMapping("/entity/{entityName}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsByEntityName(
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
    @GetMapping("/entity/{entityName}/{entityId}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsByEntityNameAndEntityId(
            @PathVariable String entityName,
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
    @GetMapping("/after/{date}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsAfter(
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
    @GetMapping("/between")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {

        log.debug("Requête de récupération des logs entre {} et {}", start, end);

        List<AuditLogResponse> logs = auditLogService.getAuditLogsBetween(start, end);

        return ResponseEntity.ok(
                ApiResponse.success("Logs entre les dates récupérés avec succès", logs)
        );
    }
}