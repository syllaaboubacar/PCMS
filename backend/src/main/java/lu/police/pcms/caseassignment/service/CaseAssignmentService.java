package lu.police.pcms.caseassignment.service;

import lu.police.pcms.caseassignment.dto.CaseAssignmentResponse;
import lu.police.pcms.caseassignment.dto.CreateCaseAssignmentRequest;
import lu.police.pcms.caseassignment.dto.PatchCaseAssignmentRequest;
import lu.police.pcms.caseassignment.dto.UpdateCaseAssignmentRequest;
import lu.police.pcms.caseassignment.entity.CaseAssignment;
import lu.police.pcms.caseassignment.mapper.CaseAssignmentMapper;
import lu.police.pcms.caseassignment.repository.CaseAssignmentRepository;
import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.casefile.repository.CaseFileRepository;
import lu.police.pcms.common.exception.DuplicateResourceException;
import lu.police.pcms.common.exception.ResourceNotFoundException;
import lu.police.pcms.user.entity.User;
import lu.police.pcms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Service de gestion des affectations (CaseAssignment).
 *
 * <p>
 * Une affectation associe un enquêteur (User) à un dossier (CaseFile).
 * La contrainte d'unicité (caseFile, user) est vérifiée à la création.
 * L'affectation peut être active ou inactive (désactivée).
 * </p>
 *
 * <p>
 * La date d'affectation (assignedAt) est immuable après la création.
 * Seul le champ {@code active} peut être modifié via PUT ou PATCH.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseAssignmentService {

    private final CaseAssignmentRepository assignmentRepository;
    private final CaseFileRepository caseFileRepository;
    private final UserRepository userRepository;
    private final CaseAssignmentMapper assignmentMapper;

    /**
     * Crée une nouvelle affectation.
     *
     * @param request DTO de création
     * @return DTO de réponse
     * @throws ResourceNotFoundException  si le dossier ou l'utilisateur n'existe pas
     * @throws DuplicateResourceException si une affectation existe déjà pour ce couple (dossier, utilisateur)
     */
    @Transactional
    public CaseAssignmentResponse createAssignment(CreateCaseAssignmentRequest request) {
        log.info("Création d'une affectation : dossier={}, utilisateur={}",
                request.getCaseFileId(), request.getUserId());

        // Chargement des entités associées
        CaseFile caseFile = caseFileRepository.findById(request.getCaseFileId())
                .orElseThrow(() -> new ResourceNotFoundException("Dossier", request.getCaseFileId()));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", request.getUserId()));

        // Vérification de l'unicité du couple (caseFile, user) même si supprimé ?
        // On vérifie l'existence d'une affectation active ou non, mais on autorise la création si l'ancienne est supprimée ?
        // Selon la contrainte unique en base, on ne peut pas avoir deux enregistrements avec le même couple.
        // On vérifie donc l'existence d'une affectation (même supprimée) pour éviter la violation de contrainte.
        if (assignmentRepository.existsByCaseFileAndUser(caseFile, user)) {
            throw new DuplicateResourceException(
                    "Affectation",
                    String.format("dossier %d et utilisateur %d", request.getCaseFileId(), request.getUserId()),
                    ""
            );
        }

        // Conversion DTO → Entité (sans les relations)
        CaseAssignment assignment = assignmentMapper.toEntity(request);

        // Définition des relations et de la date d'affectation
        assignment.setCaseFile(caseFile);
        assignment.setUser(user);
        assignment.setAssignedAt(request.getAssignedAt() != null ? request.getAssignedAt() : Instant.now());

        // active est initialisé à true par la base (DEFAULT TRUE), mais on peut le définir explicitement
        assignment.setActive(true);

        CaseAssignment saved = assignmentRepository.save(assignment);
        log.info("Affectation créée avec l'ID : {}", saved.getId());

        return assignmentMapper.toResponse(saved);
    }

    /**
     * Récupère une affectation par son identifiant.
     *
     * @param id Identifiant de l'affectation
     * @return DTO de réponse
     * @throws ResourceNotFoundException si l'affectation n'existe pas ou est supprimée
     */
    @Transactional(readOnly = true)
    public CaseAssignmentResponse getAssignmentById(Long id) {
        log.debug("Recherche de l'affectation par ID : {}", id);

        CaseAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation", id));

        if (assignment.getDeleted()) {
            throw new ResourceNotFoundException("Affectation", id);
        }

        return assignmentMapper.toResponse(assignment);
    }

    /**
     * Récupère toutes les affectations non supprimées.
     *
     * @return Liste des DTO de réponse
     */
    @Transactional(readOnly = true)
    public List<CaseAssignmentResponse> getAllAssignments() {
        log.debug("Récupération de toutes les affectations actives");

        List<CaseAssignment> assignments = assignmentRepository.findByDeletedFalse();
        return assignments.stream()
                .map(assignmentMapper::toResponse)
                .toList();
    }

    /**
     * Récupère toutes les affectations d'un dossier.
     *
     * @param caseFileId Identifiant du dossier
     * @return Liste des DTO de réponse
     * @throws ResourceNotFoundException si le dossier n'existe pas
     */
    @Transactional(readOnly = true)
    public List<CaseAssignmentResponse> getAssignmentsByCaseFile(Long caseFileId) {
        log.debug("Recherche des affectations du dossier : {}", caseFileId);

        CaseFile caseFile = caseFileRepository.findById(caseFileId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier", caseFileId));

        List<CaseAssignment> assignments = assignmentRepository.findByCaseFile(caseFile);
        return assignments.stream()
                .filter(a -> !a.getDeleted())
                .map(assignmentMapper::toResponse)
                .toList();
    }

    /**
     * Récupère toutes les affectations d'un utilisateur.
     *
     * @param userId Identifiant de l'utilisateur
     * @return Liste des DTO de réponse
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas
     */
    @Transactional(readOnly = true)
    public List<CaseAssignmentResponse> getAssignmentsByUser(Long userId) {
        log.debug("Recherche des affectations de l'utilisateur : {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", userId));

        List<CaseAssignment> assignments = assignmentRepository.findByUser(user);
        return assignments.stream()
                .filter(a -> !a.getDeleted())
                .map(assignmentMapper::toResponse)
                .toList();
    }

    /**
     * Met à jour complètement une affectation (PUT).
     * Seul le champ {@code active} est modifiable.
     *
     * @param id      Identifiant de l'affectation
     * @param request DTO de mise à jour complète
     * @return DTO de réponse mis à jour
     * @throws ResourceNotFoundException si l'affectation n'existe pas
     */
    @Transactional
    public CaseAssignmentResponse updateAssignment(Long id, UpdateCaseAssignmentRequest request) {
        log.info("Mise à jour complète de l'affectation ID : {}", id);

        CaseAssignment existing = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation", id));

        if (existing.getDeleted()) {
            throw new ResourceNotFoundException("Affectation", id);
        }

        // Application de la mise à jour (active est le seul champ modifiable)
        assignmentMapper.updateEntity(request, existing);

        CaseAssignment updated = assignmentRepository.save(existing);
        log.info("Affectation ID {} mise à jour avec succès", id);

        return assignmentMapper.toResponse(updated);
    }

    /**
     * Met à jour partiellement une affectation (PATCH).
     * Seul le champ {@code active} est modifiable et optionnel.
     *
     * @param id      Identifiant de l'affectation
     * @param request DTO de mise à jour partielle
     * @return DTO de réponse mis à jour
     * @throws ResourceNotFoundException si l'affectation n'existe pas
     */
    @Transactional
    public CaseAssignmentResponse patchAssignment(Long id, PatchCaseAssignmentRequest request) {
        log.info("Mise à jour partielle de l'affectation ID : {}", id);

        CaseAssignment existing = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation", id));

        if (existing.getDeleted()) {
            throw new ResourceNotFoundException("Affectation", id);
        }

        // Application partielle (seul active peut être modifié)
        assignmentMapper.patchEntity(request, existing);

        CaseAssignment updated = assignmentRepository.save(existing);
        log.info("Affectation ID {} partiellement mise à jour", id);

        return assignmentMapper.toResponse(updated);
    }

    /**
     * Supprime logiquement une affectation (marque deleted = true).
     *
     * @param id Identifiant de l'affectation
     * @throws ResourceNotFoundException si l'affectation n'existe pas
     */
    @Transactional
    public void deleteAssignment(Long id) {
        log.info("Suppression logique de l'affectation ID : {}", id);

        CaseAssignment existing = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation", id));

        if (existing.getDeleted()) {
            log.warn("Tentative de suppression d'une affectation déjà supprimée : {}", id);
            return;
        }

        existing.setDeleted(true);
        assignmentRepository.save(existing);
        log.info("Affectation ID {} marquée comme supprimée", id);
    }
}