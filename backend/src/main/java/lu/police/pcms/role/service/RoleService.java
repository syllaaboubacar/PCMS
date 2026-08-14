package lu.police.pcms.role.service;

import lu.police.pcms.common.exception.DuplicateResourceException;
import lu.police.pcms.common.exception.ResourceNotFoundException;
import lu.police.pcms.role.dto.CreateRoleRequest;
import lu.police.pcms.role.dto.PatchRoleRequest;
import lu.police.pcms.role.dto.RoleResponse;
import lu.police.pcms.role.dto.UpdateRoleRequest;
import lu.police.pcms.role.entity.Role;
import lu.police.pcms.role.mapper.RoleMapper;
import lu.police.pcms.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de gestion des rôles.
 *
 * <p>
 * Ce service implémente toute la logique métier autour des rôles :
 * création, consultation, mise à jour complète (PUT), mise à jour partielle (PATCH)
 * et suppression logique.
 * </p>
 *
 * <p>
 * Les opérations de modification sont transactionnelles et vérifient les
 * contraintes d'unicité avant de persister.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    /**
     * Crée un nouveau rôle.
     *
     * @param request DTO de création (nom + description)
     * @return DTO de réponse du rôle créé
     * @throws DuplicateResourceException si un rôle avec le même nom existe déjà
     */
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        log.info("Création d'un nouveau rôle : {}", request.getName());

        // Vérification de l'unicité du nom
        if (roleRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Rôle", "nom", request.getName());
        }

        // Conversion DTO → Entité
        Role role = roleMapper.toRole(request);

        // Persistance
        Role saved = roleRepository.save(role);
        log.info("Rôle créé avec l'ID : {}", saved.getId());

        // Conversion Entité → DTO de réponse
        return roleMapper.toResponse(saved);
    }

    /**
     * Récupère un rôle par son identifiant.
     *
     * @param id Identifiant du rôle
     * @return DTO de réponse
     * @throws ResourceNotFoundException si le rôle n'existe pas ou est supprimé
     */
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        log.debug("Recherche du rôle par ID : {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle", id));

        // Vérification que le rôle n'est pas supprimé logiquement
        if (role.getDeleted()) {
            throw new ResourceNotFoundException("Rôle", id);
        }

        return roleMapper.toResponse(role);
    }

    /**
     * Récupère tous les rôles non supprimés.
     *
     * @return Liste des DTO de réponse
     */
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        log.debug("Récupération de tous les rôles actifs");

        List<Role> roles = roleRepository.findByDeletedFalse();
        return roles.stream()
                .map(roleMapper::toResponse)
                .toList();
    }

    /**
     * Met à jour complètement un rôle existant (PUT).
     *
     * @param id      Identifiant du rôle
     * @param request DTO de mise à jour complète
     * @return DTO de réponse mis à jour
     * @throws ResourceNotFoundException   si le rôle n'existe pas
     * @throws DuplicateResourceException  si le nouveau nom est déjà utilisé par un autre rôle
     */
    @Transactional
    public RoleResponse updateRole(Long id, UpdateRoleRequest request) {
        log.info("Mise à jour complète du rôle ID : {}", id);

        Role existing = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle", id));

        if (existing.getDeleted()) {
            throw new ResourceNotFoundException("Rôle", id);
        }

        // Vérification de l'unicité du nom (si modifié)
        if (!existing.getName().equals(request.getName())
                && roleRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Rôle", "nom", request.getName());
        }

        // Application des modifications
        roleMapper.updateRole(request, existing);

        // Sauvegarde (les champs d'audit sont mis à jour automatiquement)
        Role updated = roleRepository.save(existing);
        log.info("Rôle ID {} mis à jour avec succès", id);

        return roleMapper.toResponse(updated);
    }

    /**
     * Met à jour partiellement un rôle existant (PATCH).
     *
     * @param id      Identifiant du rôle
     * @param request DTO de mise à jour partielle
     * @return DTO de réponse mis à jour
     * @throws ResourceNotFoundException   si le rôle n'existe pas
     * @throws DuplicateResourceException  si le nouveau nom est déjà utilisé par un autre rôle
     */
    @Transactional
    public RoleResponse patchRole(Long id, PatchRoleRequest request) {
        log.info("Mise à jour partielle du rôle ID : {}", id);

        Role existing = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle", id));

        if (existing.getDeleted()) {
            throw new ResourceNotFoundException("Rôle", id);
        }

        // Vérification de l'unicité du nom (si fourni et modifié)
        if (request.getName() != null
                && !existing.getName().equals(request.getName())
                && roleRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Rôle", "nom", request.getName());
        }

        // Application partielle (seuls les champs non-nuls sont copiés)
        roleMapper.patchRole(request, existing);

        Role updated = roleRepository.save(existing);
        log.info("Rôle ID {} partiellement mis à jour", id);

        return roleMapper.toResponse(updated);
    }

    /**
     * Supprime logiquement un rôle (marque deleted = true).
     *
     * @param id Identifiant du rôle
     * @throws ResourceNotFoundException si le rôle n'existe pas
     */
    @Transactional
    public void deleteRole(Long id) {
        log.info("Suppression logique du rôle ID : {}", id);

        Role existing = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle", id));

        // Si déjà supprimé, on ne fait rien (ou on peut lever une exception)
        if (existing.getDeleted()) {
            log.warn("Tentative de suppression d'un rôle déjà supprimé : {}", id);
            return;
        }

        existing.setDeleted(true);
        roleRepository.save(existing);
        log.info("Rôle ID {} marqué comme supprimé", id);
    }
}