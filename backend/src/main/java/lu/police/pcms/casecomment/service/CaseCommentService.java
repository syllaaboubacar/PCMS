package lu.police.pcms.casecomment.service;

import lu.police.pcms.casecomment.dto.CaseCommentResponse;
import lu.police.pcms.casecomment.dto.CreateCaseCommentRequest;
import lu.police.pcms.casecomment.dto.PatchCaseCommentRequest;
import lu.police.pcms.casecomment.dto.UpdateCaseCommentRequest;
import lu.police.pcms.casecomment.entity.CaseComment;
import lu.police.pcms.casecomment.mapper.CaseCommentMapper;
import lu.police.pcms.casecomment.repository.CaseCommentRepository;
import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.casefile.repository.CaseFileRepository;
import lu.police.pcms.common.exception.ResourceNotFoundException;
import lu.police.pcms.user.entity.User;
import lu.police.pcms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de gestion des commentaires (CaseComment).
 *
 * <p>
 * Un commentaire est associé à un dossier (CaseFile) et à un utilisateur (User).
 * Seul le champ {@code content} est modifiable après la création.
 * Les relations dossier/utilisateur sont immuables.
 * </p>
 *
 * <p>
 * La suppression est logique : le champ {@code deleted} passe à {@code true}.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseCommentService {

    private final CaseCommentRepository commentRepository;
    private final CaseFileRepository caseFileRepository;
    private final UserRepository userRepository;
    private final CaseCommentMapper commentMapper;

    /**
     * Crée un nouveau commentaire.
     *
     * @param request DTO de création
     * @return DTO de réponse
     * @throws ResourceNotFoundException si le dossier ou l'utilisateur n'existe pas
     */
    @Transactional
    public CaseCommentResponse createComment(CreateCaseCommentRequest request) {
        log.info("Création d'un commentaire pour le dossier {} par l'utilisateur {}",
                request.getCaseFileId(), request.getUserId());

        // Chargement des entités associées
        CaseFile caseFile = caseFileRepository.findById(request.getCaseFileId())
                .orElseThrow(() -> new ResourceNotFoundException("Dossier", request.getCaseFileId()));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", request.getUserId()));

        // Conversion DTO → Entité (sans relations)
        CaseComment comment = commentMapper.toEntity(request);

        // Association des relations
        comment.setCaseFile(caseFile);
        comment.setUser(user);

        CaseComment saved = commentRepository.save(comment);
        log.info("Commentaire créé avec l'ID : {}", saved.getId());

        return commentMapper.toResponse(saved);
    }

    /**
     * Récupère un commentaire par son identifiant.
     *
     * @param id Identifiant du commentaire
     * @return DTO de réponse
     * @throws ResourceNotFoundException si le commentaire n'existe pas ou est supprimé
     */
    @Transactional(readOnly = true)
    public CaseCommentResponse getCommentById(Long id) {
        log.debug("Recherche du commentaire par ID : {}", id);

        CaseComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commentaire", id));

        if (comment.getDeleted()) {
            throw new ResourceNotFoundException("Commentaire", id);
        }

        return commentMapper.toResponse(comment);
    }

    /**
     * Récupère tous les commentaires non supprimés.
     *
     * @return Liste des DTO de réponse
     */
    @Transactional(readOnly = true)
    public List<CaseCommentResponse> getAllComments() {
        log.debug("Récupération de tous les commentaires actifs");

        List<CaseComment> comments = commentRepository.findByDeletedFalse();
        return comments.stream()
                .map(commentMapper::toResponse)
                .toList();
    }

    /**
     * Récupère tous les commentaires d'un dossier.
     *
     * @param caseFileId Identifiant du dossier
     * @return Liste des DTO de réponse
     * @throws ResourceNotFoundException si le dossier n'existe pas
     */
    @Transactional(readOnly = true)
    public List<CaseCommentResponse> getCommentsByCaseFile(Long caseFileId) {
        log.debug("Recherche des commentaires du dossier : {}", caseFileId);

        CaseFile caseFile = caseFileRepository.findById(caseFileId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier", caseFileId));

        List<CaseComment> comments = commentRepository.findByCaseFile(caseFile);
        return comments.stream()
                .filter(c -> !c.getDeleted())
                .map(commentMapper::toResponse)
                .toList();
    }

    /**
     * Récupère tous les commentaires rédigés par un utilisateur.
     *
     * @param userId Identifiant de l'utilisateur
     * @return Liste des DTO de réponse
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas
     */
    @Transactional(readOnly = true)
    public List<CaseCommentResponse> getCommentsByUser(Long userId) {
        log.debug("Recherche des commentaires de l'utilisateur : {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", userId));

        List<CaseComment> comments = commentRepository.findByUser(user);
        return comments.stream()
                .filter(c -> !c.getDeleted())
                .map(commentMapper::toResponse)
                .toList();
    }

    /**
     * Met à jour complètement un commentaire (PUT).
     * Seul le champ {@code content} est modifiable.
     *
     * @param id      Identifiant du commentaire
     * @param request DTO de mise à jour complète
     * @return DTO de réponse mis à jour
     * @throws ResourceNotFoundException si le commentaire n'existe pas
     */
    @Transactional
    public CaseCommentResponse updateComment(Long id, UpdateCaseCommentRequest request) {
        log.info("Mise à jour complète du commentaire ID : {}", id);

        CaseComment existing = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commentaire", id));

        if (existing.getDeleted()) {
            throw new ResourceNotFoundException("Commentaire", id);
        }

        commentMapper.updateEntity(request, existing);

        CaseComment updated = commentRepository.save(existing);
        log.info("Commentaire ID {} mis à jour avec succès", id);

        return commentMapper.toResponse(updated);
    }

    /**
     * Met à jour partiellement un commentaire (PATCH).
     * Seul le champ {@code content} est modifiable et optionnel.
     *
     * @param id      Identifiant du commentaire
     * @param request DTO de mise à jour partielle
     * @return DTO de réponse mis à jour
     * @throws ResourceNotFoundException si le commentaire n'existe pas
     */
    @Transactional
    public CaseCommentResponse patchComment(Long id, PatchCaseCommentRequest request) {
        log.info("Mise à jour partielle du commentaire ID : {}", id);

        CaseComment existing = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commentaire", id));

        if (existing.getDeleted()) {
            throw new ResourceNotFoundException("Commentaire", id);
        }

        commentMapper.patchEntity(request, existing);

        CaseComment updated = commentRepository.save(existing);
        log.info("Commentaire ID {} partiellement mis à jour", id);

        return commentMapper.toResponse(updated);
    }

    /**
     * Supprime logiquement un commentaire (marque deleted = true).
     *
     * @param id Identifiant du commentaire
     * @throws ResourceNotFoundException si le commentaire n'existe pas
     */
    @Transactional
    public void deleteComment(Long id) {
        log.info("Suppression logique du commentaire ID : {}", id);

        CaseComment existing = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commentaire", id));

        if (existing.getDeleted()) {
            log.warn("Tentative de suppression d'un commentaire déjà supprimé : {}", id);
            return;
        }

        existing.setDeleted(true);
        commentRepository.save(existing);
        log.info("Commentaire ID {} marqué comme supprimé", id);
    }
}