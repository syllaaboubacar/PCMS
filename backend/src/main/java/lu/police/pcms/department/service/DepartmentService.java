package lu.police.pcms.department.service;

import lu.police.pcms.common.exception.DuplicateResourceException;
import lu.police.pcms.common.exception.ResourceNotFoundException;
import lu.police.pcms.department.dto.CreateDepartmentRequest;
import lu.police.pcms.department.dto.DepartmentResponse;
import lu.police.pcms.department.dto.PatchDepartmentRequest;
import lu.police.pcms.department.dto.UpdateDepartmentRequest;
import lu.police.pcms.department.entity.Department;
import lu.police.pcms.department.mapper.DepartmentMapper;
import lu.police.pcms.department.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de gestion des départements.
 *
 * <p>
 * Ce service implémente toute la logique métier autour des départements :
 * création, consultation, mise à jour complète (PUT), mise à jour partielle (PATCH)
 * et suppression logique.
 * </p>
 *
 * <p>
 * Les contraintes d'unicité sur {@code code} et {@code name} sont vérifiées
 * avant toute modification.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    /**
     * Crée un nouveau département.
     *
     * @param request DTO de création
     * @return DTO de réponse du département créé
     * @throws DuplicateResourceException si le code ou le nom existe déjà
     */
    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        log.info("Création d'un nouveau département : code={}, name={}", request.getCode(), request.getName());

        // Vérification de l'unicité du code
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Département", "code", request.getCode());
        }
        // Vérification de l'unicité du nom
        if (departmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Département", "nom", request.getName());
        }

        Department department = departmentMapper.toEntity(request);
        Department saved = departmentRepository.save(department);
        log.info("Département créé avec l'ID : {}", saved.getId());

        return departmentMapper.toResponse(saved);
    }

    /**
     * Récupère un département par son identifiant.
     *
     * @param id Identifiant du département
     * @return DTO de réponse
     * @throws ResourceNotFoundException si le département n'existe pas ou est supprimé
     */
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        log.debug("Recherche du département par ID : {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Département", id));

        if (department.getDeleted()) {
            throw new ResourceNotFoundException("Département", id);
        }

        return departmentMapper.toResponse(department);
    }

    /**
     * Récupère tous les départements non supprimés.
     *
     * @return Liste des DTO de réponse
     */
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        log.debug("Récupération de tous les départements actifs");

        List<Department> departments = departmentRepository.findByDeletedFalse();
        return departments.stream()
                .map(departmentMapper::toResponse)
                .toList();
    }

    /**
     * Met à jour complètement un département existant (PUT).
     *
     * @param id      Identifiant du département
     * @param request DTO de mise à jour complète
     * @return DTO de réponse mis à jour
     * @throws ResourceNotFoundException   si le département n'existe pas
     * @throws DuplicateResourceException  si le nouveau code ou nom est déjà utilisé par un autre département
     */
    @Transactional
    public DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request) {
        log.info("Mise à jour complète du département ID : {}", id);

        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Département", id));

        if (existing.getDeleted()) {
            throw new ResourceNotFoundException("Département", id);
        }

        // Vérification de l'unicité du code (si modifié)
        if (!existing.getCode().equals(request.getCode())
                && departmentRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Département", "code", request.getCode());
        }
        // Vérification de l'unicité du nom (si modifié)
        if (!existing.getName().equals(request.getName())
                && departmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Département", "nom", request.getName());
        }

        departmentMapper.updateEntity(request, existing);
        Department updated = departmentRepository.save(existing);
        log.info("Département ID {} mis à jour avec succès", id);

        return departmentMapper.toResponse(updated);
    }

    /**
     * Met à jour partiellement un département existant (PATCH).
     *
     * @param id      Identifiant du département
     * @param request DTO de mise à jour partielle
     * @return DTO de réponse mis à jour
     * @throws ResourceNotFoundException   si le département n'existe pas
     * @throws DuplicateResourceException  si le nouveau code ou nom est déjà utilisé par un autre département
     */
    @Transactional
    public DepartmentResponse patchDepartment(Long id, PatchDepartmentRequest request) {
        log.info("Mise à jour partielle du département ID : {}", id);

        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Département", id));

        if (existing.getDeleted()) {
            throw new ResourceNotFoundException("Département", id);
        }

        // Vérification de l'unicité du code (si fourni et modifié)
        if (request.getCode() != null
                && !existing.getCode().equals(request.getCode())
                && departmentRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Département", "code", request.getCode());
        }
        // Vérification de l'unicité du nom (si fourni et modifié)
        if (request.getName() != null
                && !existing.getName().equals(request.getName())
                && departmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Département", "nom", request.getName());
        }

        departmentMapper.patchEntity(request, existing);
        Department updated = departmentRepository.save(existing);
        log.info("Département ID {} partiellement mis à jour", id);

        return departmentMapper.toResponse(updated);
    }

    /**
     * Supprime logiquement un département (marque deleted = true).
     *
     * @param id Identifiant du département
     * @throws ResourceNotFoundException si le département n'existe pas
     */
    @Transactional
    public void deleteDepartment(Long id) {
        log.info("Suppression logique du département ID : {}", id);

        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Département", id));

        if (existing.getDeleted()) {
            log.warn("Tentative de suppression d'un département déjà supprimé : {}", id);
            return;
        }

        existing.setDeleted(true);
        departmentRepository.save(existing);
        log.info("Département ID {} marqué comme supprimé", id);
    }
}