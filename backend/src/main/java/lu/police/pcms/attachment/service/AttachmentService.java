package lu.police.pcms.attachment.service;

import lu.police.pcms.attachment.dto.AttachmentResponse;
import lu.police.pcms.attachment.dto.CreateAttachmentRequest;
import lu.police.pcms.attachment.dto.PatchAttachmentRequest;
import lu.police.pcms.attachment.dto.UpdateAttachmentRequest;
import lu.police.pcms.attachment.entity.Attachment;
import lu.police.pcms.attachment.mapper.AttachmentMapper;
import lu.police.pcms.attachment.repository.AttachmentRepository;
import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.casefile.repository.CaseFileRepository;
import lu.police.pcms.common.exception.DuplicateResourceException;
import lu.police.pcms.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service de gestion des pièces jointes (Attachment).
 *
 * <p>
 * Une pièce jointe est un fichier associé à un dossier d'enquête (CaseFile).
 * Les champs techniques (filename, storagePath) sont générés automatiquement
 * par le service, tandis que uploadedAt peut être fourni par le client ou
 * défini à l'instant présent.
 * </p>
 *
 * <p>
 * Seuls les champs {@code mimeType} et {@code type} sont modifiables
 * après la création. Le fichier physique ne peut pas être remplacé ;
 * pour cela, il faut supprimer l'ancien et en créer un nouveau.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final CaseFileRepository caseFileRepository;
    private final AttachmentMapper attachmentMapper;

    /**
     * Crée une nouvelle pièce jointe.
     *
     * @param request DTO de création
     * @return DTO de réponse
     * @throws ResourceNotFoundException  si le dossier n'existe pas
     * @throws DuplicateResourceException si un fichier avec le même nom interne existe déjà (peu probable)
     */
    @Transactional
    public AttachmentResponse createAttachment(CreateAttachmentRequest request) {
        log.info("Création d'une pièce jointe pour le dossier : {}", request.getCaseFileId());

        // Chargement du dossier
        CaseFile caseFile = caseFileRepository.findById(request.getCaseFileId())
                .orElseThrow(() -> new ResourceNotFoundException("Dossier", request.getCaseFileId()));

        // Génération des champs techniques
        String filename = UUID.randomUUID() + "_" + request.getOriginalFilename();
        String storagePath = "/uploads/" + filename;

        // Vérification d'unicité (nom de fichier interne)
        if (attachmentRepository.existsByFilename(filename)) {
            throw new DuplicateResourceException("Pièce jointe", "filename", filename);
        }

        // Conversion DTO → Entité (sans les champs générés)
        Attachment attachment = attachmentMapper.toEntity(request);

        // Remplissage des champs techniques
        attachment.setCaseFile(caseFile);
        attachment.setFilename(filename);
        attachment.setStoragePath(storagePath);
        attachment.setUploadedAt(Instant.now());

        Attachment saved = attachmentRepository.save(attachment);
        log.info("Pièce jointe créée avec l'ID : {}", saved.getId());

        return attachmentMapper.toResponse(saved);
    }

    /**
     * Récupère une pièce jointe par son identifiant.
     *
     * @param id Identifiant de la pièce jointe
     * @return DTO de réponse
     * @throws ResourceNotFoundException si la pièce jointe n'existe pas ou est supprimée
     */
    @Transactional(readOnly = true)
    public AttachmentResponse getAttachmentById(Long id) {
        log.debug("Recherche de la pièce jointe par ID : {}", id);

        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pièce jointe", id));

        if (attachment.getDeleted()) {
            throw new ResourceNotFoundException("Pièce jointe", id);
        }

        return attachmentMapper.toResponse(attachment);
    }

    /**
     * Récupère toutes les pièces jointes non supprimées.
     *
     * @return Liste des DTO de réponse
     */
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAllAttachments() {
        log.debug("Récupération de toutes les pièces jointes actives");

        List<Attachment> attachments = attachmentRepository.findByDeletedFalse();
        return attachments.stream()
                .map(attachmentMapper::toResponse)
                .toList();
    }

    /**
     * Récupère toutes les pièces jointes d'un dossier.
     *
     * @param caseFileId Identifiant du dossier
     * @return Liste des DTO de réponse
     * @throws ResourceNotFoundException si le dossier n'existe pas
     */
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachmentsByCaseFile(Long caseFileId) {
        log.debug("Recherche des pièces jointes du dossier : {}", caseFileId);

        CaseFile caseFile = caseFileRepository.findById(caseFileId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier", caseFileId));

        List<Attachment> attachments = attachmentRepository.findByCaseFile(caseFile);
        return attachments.stream()
                .filter(a -> !a.getDeleted())
                .map(attachmentMapper::toResponse)
                .toList();
    }

    /**
     * Met à jour complètement une pièce jointe (PUT).
     * Seuls {@code mimeType} et {@code type} sont modifiables.
     *
     * @param id      Identifiant de la pièce jointe
     * @param request DTO de mise à jour complète
     * @return DTO de réponse mis à jour
     * @throws ResourceNotFoundException si la pièce jointe n'existe pas
     */
    @Transactional
    public AttachmentResponse updateAttachment(Long id, UpdateAttachmentRequest request) {
        log.info("Mise à jour complète de la pièce jointe ID : {}", id);

        Attachment existing = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pièce jointe", id));

        if (existing.getDeleted()) {
            throw new ResourceNotFoundException("Pièce jointe", id);
        }

        attachmentMapper.updateEntity(request, existing);

        Attachment updated = attachmentRepository.save(existing);
        log.info("Pièce jointe ID {} mise à jour avec succès", id);

        return attachmentMapper.toResponse(updated);
    }

    /**
     * Met à jour partiellement une pièce jointe (PATCH).
     * Seuls {@code mimeType} et {@code type} sont modifiables et optionnels.
     *
     * @param id      Identifiant de la pièce jointe
     * @param request DTO de mise à jour partielle
     * @return DTO de réponse mis à jour
     * @throws ResourceNotFoundException si la pièce jointe n'existe pas
     */
    @Transactional
    public AttachmentResponse patchAttachment(Long id, PatchAttachmentRequest request) {
        log.info("Mise à jour partielle de la pièce jointe ID : {}", id);

        Attachment existing = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pièce jointe", id));

        if (existing.getDeleted()) {
            throw new ResourceNotFoundException("Pièce jointe", id);
        }

        attachmentMapper.patchEntity(request, existing);

        Attachment updated = attachmentRepository.save(existing);
        log.info("Pièce jointe ID {} partiellement mise à jour", id);

        return attachmentMapper.toResponse(updated);
    }

    /**
     * Supprime logiquement une pièce jointe (marque deleted = true).
     *
     * @param id Identifiant de la pièce jointe
     * @throws ResourceNotFoundException si la pièce jointe n'existe pas
     */
    @Transactional
    public void deleteAttachment(Long id) {
        log.info("Suppression logique de la pièce jointe ID : {}", id);

        Attachment existing = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pièce jointe", id));

        if (existing.getDeleted()) {
            log.warn("Tentative de suppression d'une pièce jointe déjà supprimée : {}", id);
            return;
        }

        existing.setDeleted(true);
        attachmentRepository.save(existing);
        log.info("Pièce jointe ID {} marquée comme supprimée", id);
    }
}