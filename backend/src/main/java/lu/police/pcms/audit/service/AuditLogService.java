package lu.police.pcms.audit.service;

import lu.police.pcms.audit.dto.AuditLogResponse;
import lu.police.pcms.audit.dto.CreateAuditLogRequest;
import lu.police.pcms.audit.entity.AuditLog;
import lu.police.pcms.audit.mapper.AuditLogMapper;
import lu.police.pcms.audit.repository.AuditLogRepository;
import lu.police.pcms.common.exception.ResourceNotFoundException;
import lu.police.pcms.user.entity.User;
import lu.police.pcms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Service de gestion des journaux d'audit (AuditLog).
 *
 * <p>
 * Les logs d'audit sont immuables : ils ne peuvent être que créés et consultés.
 * Ils permettent de tracer les actions des utilisateurs sur les entités métier.
 * </p>
 *
 * <p>
 * Ce service est généralement appelé par d'autres services pour enregistrer
 * automatiquement les opérations importantes (CRUD, authentification, etc.).
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AuditLogMapper auditLogMapper;

    /**
     * Crée un nouveau journal d'audit.
     *
     * @param request DTO de création
     * @return DTO de réponse
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas
     */
    @Transactional
    public AuditLogResponse createAuditLog(CreateAuditLogRequest request) {
        log.info("Création d'un log d'audit : action={}, entity={}, entityId={}",
                request.getAction(), request.getEntityName(), request.getEntityId());

        // Chargement de l'utilisateur
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", request.getUserId()));

        // Conversion DTO → Entité (sans relation)
        AuditLog auditLog = auditLogMapper.toEntity(request);

        // Association de l'utilisateur
        auditLog.setUser(user);

        AuditLog saved = auditLogRepository.save(auditLog);
        log.info("Log d'audit créé avec l'ID : {}", saved.getId());

        return auditLogMapper.toResponse(saved);
    }

    /**
     * Récupère un log d'audit par son identifiant.
     *
     * @param id Identifiant du log
     * @return DTO de réponse
     * @throws ResourceNotFoundException si le log n'existe pas
     */
    @Transactional(readOnly = true)
    public AuditLogResponse getAuditLogById(Long id) {
        log.debug("Recherche du log d'audit par ID : {}", id);

        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Log d'audit", id));

        return auditLogMapper.toResponse(auditLog);
    }

    /**
     * Récupère tous les logs d'audit (triés par date décroissante par défaut).
     *
     * @return Liste des DTO de réponse
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllAuditLogs() {
        log.debug("Récupération de tous les logs d'audit");

        // La méthode findAll() retourne les logs dans l'ordre d'insertion.
        // On pourrait ajouter un tri par createdAt desc via une méthode personnalisée,
        // mais on va simplement utiliser findAll() et trier si besoin.
        List<AuditLog> logs = auditLogRepository.findAll();
        return logs.stream()
                .map(auditLogMapper::toResponse)
                .toList();
    }

    /**
     * Récupère les logs d'audit avec pagination.
     *
     * @param pageable Objet de pagination
     * @return Page de DTO de réponse
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsPaginated(Pageable pageable) {
        log.debug("Récupération paginée des logs d'audit");

        Page<AuditLog> page = auditLogRepository.findAll(pageable);
        return page.map(auditLogMapper::toResponse);
    }

    /**
     * Récupère tous les logs d'audit d'un utilisateur donné.
     *
     * @param userId Identifiant de l'utilisateur
     * @return Liste des DTO de réponse
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByUser(Long userId) {
        log.debug("Recherche des logs d'audit de l'utilisateur : {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", userId));

        List<AuditLog> logs = auditLogRepository.findByUser(user);
        return logs.stream()
                .map(auditLogMapper::toResponse)
                .toList();
    }

    /**
     * Récupère les logs d'audit d'un utilisateur avec pagination.
     *
     * @param userId   Identifiant de l'utilisateur
     * @param pageable Objet de pagination
     * @return Page de DTO de réponse
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsByUser(Long userId, Pageable pageable) {
        log.debug("Recherche paginée des logs de l'utilisateur : {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", userId));

        Page<AuditLog> page = auditLogRepository.findByUser(user, pageable);
        return page.map(auditLogMapper::toResponse);
    }

    /**
     * Récupère les logs d'audit par type d'action.
     *
     * @param action Type d'action (ex: CREATE, UPDATE)
     * @return Liste des DTO de réponse
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByAction(String action) {
        log.debug("Recherche des logs d'audit par action : {}", action);

        List<AuditLog> logs = auditLogRepository.findByAction(action);
        return logs.stream()
                .map(auditLogMapper::toResponse)
                .toList();
    }

    /**
     * Récupère les logs d'audit paginés par type d'action.
     *
     * @param action   Type d'action
     * @param pageable Objet de pagination
     * @return Page de DTO de réponse
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsByAction(String action, Pageable pageable) {
        log.debug("Recherche paginée des logs d'audit par action : {}", action);

        Page<AuditLog> page = auditLogRepository.findByAction(action, pageable);
        return page.map(auditLogMapper::toResponse);
    }

    /**
     * Récupère les logs d'audit concernant une entité spécifique.
     *
     * @param entityName Nom de l'entité (ex: USER, ROLE)
     * @return Liste des DTO de réponse
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByEntityName(String entityName) {
        log.debug("Recherche des logs d'audit par entité : {}", entityName);

        List<AuditLog> logs = auditLogRepository.findByEntityName(entityName);
        return logs.stream()
                .map(auditLogMapper::toResponse)
                .toList();
    }

    /**
     * Récupère les logs d'audit concernant un enregistrement spécifique.
     *
     * @param entityName Nom de l'entité
     * @param entityId   Identifiant de l'enregistrement
     * @return Liste des DTO de réponse
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByEntityNameAndEntityId(String entityName, Long entityId) {
        log.debug("Recherche des logs d'audit pour entité {} et ID {}", entityName, entityId);

        List<AuditLog> logs = auditLogRepository.findByEntityNameAndEntityId(entityName, entityId);
        return logs.stream()
                .map(auditLogMapper::toResponse)
                .toList();
    }

    /**
     * Récupère les logs d'audit créés après une date donnée.
     *
     * @param date Date minimale
     * @return Liste des DTO de réponse
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsAfter(Instant date) {
        log.debug("Recherche des logs d'audit après : {}", date);

        List<AuditLog> logs = auditLogRepository.findByCreatedAtAfter(date);
        return logs.stream()
                .map(auditLogMapper::toResponse)
                .toList();
    }

    /**
     * Récupère les logs d'audit entre deux dates.
     *
     * @param start Date de début
     * @param end   Date de fin
     * @return Liste des DTO de réponse
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsBetween(Instant start, Instant end) {
        log.debug("Recherche des logs d'audit entre {} et {}", start, end);

        List<AuditLog> logs = auditLogRepository.findByCreatedAtBetween(start, end);
        return logs.stream()
                .map(auditLogMapper::toResponse)
                .toList();
    }

    /**
     * Compte le nombre de logs d'audit pour un utilisateur.
     *
     * @param userId Identifiant de l'utilisateur
     * @return Nombre de logs
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas
     */
    @Transactional(readOnly = true)
    public long countAuditLogsByUser(Long userId) {
        log.debug("Comptage des logs d'audit de l'utilisateur : {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", userId));

        return auditLogRepository.countByUser(user);
    }

    /**
     * Compte le nombre de logs d'audit par type d'action.
     *
     * @param action Type d'action
     * @return Nombre de logs
     */
    @Transactional(readOnly = true)
    public long countAuditLogsByAction(String action) {
        log.debug("Comptage des logs d'audit par action : {}", action);

        return auditLogRepository.countByAction(action);
    }
}