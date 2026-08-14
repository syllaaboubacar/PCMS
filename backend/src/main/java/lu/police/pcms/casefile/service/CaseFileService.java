package lu.police.pcms.casefile.service;

import lu.police.pcms.casefile.dto.CaseFileResponse;
import lu.police.pcms.casefile.dto.CreateCaseFileRequest;
import lu.police.pcms.casefile.dto.PatchCaseFileRequest;
import lu.police.pcms.casefile.dto.UpdateCaseFileRequest;
import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.casefile.mapper.CaseFileMapper;
import lu.police.pcms.casefile.repository.CaseFileRepository;
import lu.police.pcms.common.exception.DuplicateResourceException;
import lu.police.pcms.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de gestion des dossiers d'enquête (CaseFile).
 *
 * <p>
 * Ce service implémente la logique métier pour les opérations CRUD
 * sur les dossiers d'enquête : création, consultation, mise à jour
 * complète (PUT), mise à jour partielle (PATCH) et suppression logique.
 * </p>
 *
 * <p>
 * Le numéro de dossier (caseNumber) est immuable après la création.
 * La date d'ouverture (openedAt) n'est pas modifiable.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseFileService {

    private final CaseFileRepository caseFileRepository;
    private final CaseFileMapper caseFileMapper;

    /**
     * Crée un nouveau dossier d'enquête.
     *
     * @param request DTO de création
     * @return DTO de réponse du dossier créé
     * @throws DuplicateResourceException si le numéro de dossier existe déjà
     */
    @Transactional
    public CaseFileResponse createCaseFile(CreateCaseFileRequest request) {
        log.info("Création d'un nouveau dossier : {}", request.getCaseNumber());

        // Vérification de l'unicité du numéro de dossier
        if (caseFileRepository.existsByCaseNumber(request.getCaseNumber())) {
            throw new DuplicateResourceException("Dossier", "caseNumber", request.getCaseNumber());
        }

        // Conversion DTO → Entité
        CaseFile caseFile = caseFileMapper.toEntity(request);

        // Si la date d'ouverture n'est pas fournie, on utilise l'instant présent
        if (request.getOpenedAt() == null) {
            caseFile.setOpenedAt(java.time.Instant.now());
        }

        CaseFile saved = caseFileRepository.save(caseFile);
        log.info("Dossier créé avec l'ID : {}", saved.getId());

        return caseFileMapper.toResponse(saved);
    }

    /**
     * Récupère un dossier par son identifiant.
     *
     * @param id Identifiant du dossier
     * @return DTO de réponse
     * @throws ResourceNotFoundException si le dossier n'existe pas ou est supprimé
     */
    @Transactional(readOnly = true)
    public CaseFileResponse getCaseFileById(Long id) {
        log.debug("Recherche du dossier par ID : {}", id);

        CaseFile caseFile = caseFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier", id));

        if (caseFile.getDeleted()) {
            throw new ResourceNotFoundException("Dossier", id);
        }

        return caseFileMapper.toResponse(caseFile);
    }

    /**
     * Récupère un dossier par son numéro métier.
     *
     * @param caseNumber Numéro de dossier
     * @return DTO de réponse
     * @throws ResourceNotFoundException si le dossier n'existe pas ou est supprimé
     */
    @Transactional(readOnly = true)
    public CaseFileResponse getCaseFileByNumber(String caseNumber) {
        log.debug("Recherche du dossier par numéro : {}", caseNumber);

        CaseFile caseFile = caseFileRepository.findByCaseNumber(caseNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier", "caseNumber", caseNumber));

        if (caseFile.getDeleted()) {
            throw new ResourceNotFoundException("Dossier", "caseNumber", caseNumber);
        }

        return caseFileMapper.toResponse(caseFile);
    }

    /**
     * Récupère tous les dossiers non supprimés.
     *
     * @return Liste des DTO de réponse
     */
    @Transactional(readOnly = true)
    public List<CaseFileResponse> getAllCaseFiles() {
        log.debug("Récupération de tous les dossiers actifs");

        List<CaseFile> caseFiles = caseFileRepository.findByDeletedFalse();
        return caseFiles.stream()
                .map(caseFileMapper::toResponse)
                .toList();
    }

    /**
     * Met à jour complètement un dossier existant (PUT).
     *
     * @param id      Identifiant du dossier
     * @param request DTO de mise à jour complète
     * @return DTO de réponse mis à jour
     * @throws ResourceNotFoundException si le dossier n'existe pas
     */
    @Transactional
    public CaseFileResponse updateCaseFile(Long id, UpdateCaseFileRequest request) {
        log.info("Mise à jour complète du dossier ID : {}", id);

        CaseFile existing = caseFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier", id));

        if (existing.getDeleted()) {
            throw new ResourceNotFoundException("Dossier", id);
        }

        // Application des modifications (le caseNumber et openedAt sont ignorés par le mapper)
        caseFileMapper.updateEntity(request, existing);

        CaseFile updated = caseFileRepository.save(existing);
        log.info("Dossier ID {} mis à jour avec succès", id);

        return caseFileMapper.toResponse(updated);
    }

    /**
     * Met à jour partiellement un dossier existant (PATCH).
     *
     * @param id      Identifiant du dossier
     * @param request DTO de mise à jour partielle
     * @return DTO de réponse mis à jour
     * @throws ResourceNotFoundException si le dossier n'existe pas
     */
    @Transactional
    public CaseFileResponse patchCaseFile(Long id, PatchCaseFileRequest request) {
        log.info("Mise à jour partielle du dossier ID : {}", id);

        CaseFile existing = caseFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier", id));

        if (existing.getDeleted()) {
            throw new ResourceNotFoundException("Dossier", id);
        }

        // Application partielle (seuls les champs non-nuls sont copiés)
        caseFileMapper.patchEntity(request, existing);

        CaseFile updated = caseFileRepository.save(existing);
        log.info("Dossier ID {} partiellement mis à jour", id);

        return caseFileMapper.toResponse(updated);
    }

    /**
     * Supprime logiquement un dossier (marque deleted = true).
     *
     * @param id Identifiant du dossier
     * @throws ResourceNotFoundException si le dossier n'existe pas
     */
    @Transactional
    public void deleteCaseFile(Long id) {
        log.info("Suppression logique du dossier ID : {}", id);

        CaseFile existing = caseFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier", id));

        if (existing.getDeleted()) {
            log.warn("Tentative de suppression d'un dossier déjà supprimé : {}", id);
            return;
        }

        existing.setDeleted(true);
        caseFileRepository.save(existing);
        log.info("Dossier ID {} marqué comme supprimé", id);
    }
}