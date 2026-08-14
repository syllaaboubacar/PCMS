package lu.police.pcms.suspect.service;

import lu.police.pcms.casefile.entity.CaseFile;
import lu.police.pcms.casefile.repository.CaseFileRepository;
import lu.police.pcms.common.exception.DuplicateResourceException;
import lu.police.pcms.common.exception.ResourceNotFoundException;
import lu.police.pcms.suspect.dto.CreateSuspectRequest;
import lu.police.pcms.suspect.dto.PatchSuspectRequest;
import lu.police.pcms.suspect.dto.SuspectResponse;
import lu.police.pcms.suspect.dto.UpdateSuspectRequest;
import lu.police.pcms.suspect.entity.Suspect;
import lu.police.pcms.suspect.mapper.SuspectMapper;
import lu.police.pcms.suspect.repository.SuspectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de gestion des suspects.
 *
 * <p>
 * Un suspect est associé à un dossier d'enquête (CaseFile).
 * La contrainte d'unicité métier est (caseFile, lastName, firstName) :
 * on ne peut pas avoir deux suspects identiques dans le même dossier.
 * </p>
 *
 * <p>
 * Le dossier d'un suspect est immuable après la création.
 * Tous les autres champs (firstName, lastName, birthDate, nationality, notes)
 * peuvent être modifiés via PUT ou PATCH.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SuspectService {

    private final SuspectRepository suspectRepository;
    private final CaseFileRepository caseFileRepository;
    private final SuspectMapper suspectMapper;

    /**
     * Crée un nouveau suspect.
     *
     * @param request DTO de création
     * @return DTO de réponse
     * @throws ResourceNotFoundException  si le dossier n'existe pas
     * @throws DuplicateResourceException si un suspect avec le même nom/prénom existe déjà dans le dossier
     */
    @Transactional
    public SuspectResponse createSuspect(CreateSuspectRequest request) {
        log.info("Création d'un suspect : {} {}, dossier {}",
                request.getFirstName(), request.getLastName(), request.getCaseFileId());

        // Chargement du dossier
        CaseFile caseFile = caseFileRepository.findById(request.getCaseFileId())
                .orElseThrow(() -> new ResourceNotFoundException("Dossier", request.getCaseFileId()));

        // Vérification de l'unicité (nom + prénom dans le même dossier)
        if (suspectRepository.existsByCaseFileIdAndLastNameAndFirstName(
                request.getCaseFileId(),
                request.getLastName(),
                request.getFirstName())) {
            throw new DuplicateResourceException(
                    "Suspect",
                    String.format("nom '%s' et prénom '%s' dans le dossier %d",
                            request.getLastName(), request.getFirstName(), request.getCaseFileId()),
                    ""
            );
        }

        // Conversion DTO → Entité (sans relation)
        Suspect suspect = suspectMapper.toEntity(request);

        // Association du dossier
        suspect.setCaseFile(caseFile);

        Suspect saved = suspectRepository.save(suspect);
        log.info("Suspect créé avec l'ID : {}", saved.getId());

        return suspectMapper.toResponse(saved);
    }

    /**
     * Récupère un suspect par son identifiant.
     *
     * @param id Identifiant du suspect
     * @return DTO de réponse
     * @throws ResourceNotFoundException si le suspect n'existe pas ou est supprimé
     */
    @Transactional(readOnly = true)
    public SuspectResponse getSuspectById(Long id) {
        log.debug("Recherche du suspect par ID : {}", id);

        Suspect suspect = suspectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suspect", id));

        if (suspect.getDeleted()) {
            throw new ResourceNotFoundException("Suspect", id);
        }

        return suspectMapper.toResponse(suspect);
    }

    /**
     * Récupère tous les suspects non supprimés.
     *
     * @return Liste des DTO de réponse
     */
    @Transactional(readOnly = true)
    public List<SuspectResponse> getAllSuspects() {
        log.debug("Récupération de tous les suspects actifs");

        List<Suspect> suspects = suspectRepository.findByDeletedFalse();
        return suspects.stream()
                .map(suspectMapper::toResponse)
                .toList();
    }

    /**
     * Récupère tous les suspects d'un dossier.
     *
     * @param caseFileId Identifiant du dossier
     * @return Liste des DTO de réponse
     * @throws ResourceNotFoundException si le dossier n'existe pas
     */
    @Transactional(readOnly = true)
    public List<SuspectResponse> getSuspectsByCaseFile(Long caseFileId) {
        log.debug("Recherche des suspects du dossier : {}", caseFileId);

        CaseFile caseFile = caseFileRepository.findById(caseFileId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier", caseFileId));

        List<Suspect> suspects = suspectRepository.findByCaseFile(caseFile);
        return suspects.stream()
                .filter(s -> !s.getDeleted())
                .map(suspectMapper::toResponse)
                .toList();
    }

    /**
     * Met à jour complètement un suspect existant (PUT).
     *
     * @param id      Identifiant du suspect
     * @param request DTO de mise à jour complète
     * @return DTO de réponse mis à jour
     * @throws ResourceNotFoundException  si le suspect n'existe pas
     * @throws DuplicateResourceException si les nouvelles valeurs violent la contrainte d'unicité
     */
    @Transactional
    public SuspectResponse updateSuspect(Long id, UpdateSuspectRequest request) {
        log.info("Mise à jour complète du suspect ID : {}", id);

        Suspect existing = suspectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suspect", id));

        if (existing.getDeleted()) {
            throw new ResourceNotFoundException("Suspect", id);
        }

        // Vérification de l'unicité (si le nom ou prénom change)
        if (!existing.getFirstName().equals(request.getFirstName())
                || !existing.getLastName().equals(request.getLastName())) {
            // On vérifie si un autre suspect avec le même nom/prénom existe dans le même dossier
            if (suspectRepository.existsByCaseFileIdAndLastNameAndFirstName(
                    existing.getCaseFile().getId(),
                    request.getLastName(),
                    request.getFirstName())) {
                throw new DuplicateResourceException(
                        "Suspect",
                        String.format("nom '%s' et prénom '%s' dans le dossier %d",
                                request.getLastName(), request.getFirstName(), existing.getCaseFile().getId()),
                        ""
                );
            }
        }

        // Application des modifications
        suspectMapper.updateEntity(request, existing);

        Suspect updated = suspectRepository.save(existing);
        log.info("Suspect ID {} mis à jour avec succès", id);

        return suspectMapper.toResponse(updated);
    }

    /**
     * Met à jour partiellement un suspect existant (PATCH).
     *
     * @param id      Identifiant du suspect
     * @param request DTO de mise à jour partielle
     * @return DTO de réponse mis à jour
     * @throws ResourceNotFoundException  si le suspect n'existe pas
     * @throws DuplicateResourceException si les nouvelles valeurs violent la contrainte d'unicité
     */
    @Transactional
    public SuspectResponse patchSuspect(Long id, PatchSuspectRequest request) {
        log.info("Mise à jour partielle du suspect ID : {}", id);

        Suspect existing = suspectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suspect", id));

        if (existing.getDeleted()) {
            throw new ResourceNotFoundException("Suspect", id);
        }

        // Vérification de l'unicité si firstName ou lastName est modifié
        String newFirstName = request.getFirstName() != null ? request.getFirstName() : existing.getFirstName();
        String newLastName = request.getLastName() != null ? request.getLastName() : existing.getLastName();

        if (request.getFirstName() != null || request.getLastName() != null) {
            // Vérifier si un autre suspect avec les nouvelles valeurs existe déjà dans le même dossier
            if (!newFirstName.equals(existing.getFirstName()) || !newLastName.equals(existing.getLastName())) {
                if (suspectRepository.existsByCaseFileIdAndLastNameAndFirstName(
                        existing.getCaseFile().getId(),
                        newLastName,
                        newFirstName)) {
                    throw new DuplicateResourceException(
                            "Suspect",
                            String.format("nom '%s' et prénom '%s' dans le dossier %d",
                                    newLastName, newFirstName, existing.getCaseFile().getId()),
                            ""
                    );
                }
            }
        }

        // Application partielle
        suspectMapper.patchEntity(request, existing);

        Suspect updated = suspectRepository.save(existing);
        log.info("Suspect ID {} partiellement mis à jour", id);

        return suspectMapper.toResponse(updated);
    }

    /**
     * Supprime logiquement un suspect (marque deleted = true).
     *
     * @param id Identifiant du suspect
     * @throws ResourceNotFoundException si le suspect n'existe pas
     */
    @Transactional
    public void deleteSuspect(Long id) {
        log.info("Suppression logique du suspect ID : {}", id);

        Suspect existing = suspectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suspect", id));

        if (existing.getDeleted()) {
            log.warn("Tentative de suppression d'un suspect déjà supprimé : {}", id);
            return;
        }

        existing.setDeleted(true);
        suspectRepository.save(existing);
        log.info("Suspect ID {} marqué comme supprimé", id);
    }
}