package lu.police.pcms.user.service;

import lu.police.pcms.common.exception.DuplicateResourceException;
import lu.police.pcms.common.exception.ResourceNotFoundException;
import lu.police.pcms.department.entity.Department;
import lu.police.pcms.department.repository.DepartmentRepository;
import lu.police.pcms.role.entity.Role;
import lu.police.pcms.role.repository.RoleRepository;
import lu.police.pcms.user.dto.CreateUserRequest;
import lu.police.pcms.user.dto.PatchUserRequest;
import lu.police.pcms.user.dto.UpdateUserRequest;
import lu.police.pcms.user.dto.UserResponse;
import lu.police.pcms.user.entity.User;
import lu.police.pcms.user.mapper.UserMapper;
import lu.police.pcms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de gestion des utilisateurs.
 *
 * <p>
 * Ce service implémente toute la logique métier autour des utilisateurs :
 * création, consultation, mise à jour complète (PUT), mise à jour partielle (PATCH)
 * et suppression logique.
 * </p>
 *
 * <p>
 * Le mot de passe est encodé avec BCrypt lors de la création.
 * Il ne peut pas être modifié via les opérations PUT/PATCH ; un endpoint dédié
 * devra être prévu pour le changement de mot de passe.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crée un nouvel utilisateur.
     *
     * @param request DTO de création
     * @return DTO de réponse de l'utilisateur créé
     * @throws DuplicateResourceException si l'email existe déjà
     * @throws ResourceNotFoundException  si le rôle ou le département n'existe pas
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Création d'un nouvel utilisateur : {}", request.getEmail());

        // Vérification de l'unicité de l'email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Utilisateur", "email", request.getEmail());
        }

        // Récupération du rôle et du département
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Rôle", request.getRoleId()));
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Département", request.getDepartmentId()));

        // Conversion DTO → Entité (sans les relations)
        User user = userMapper.toEntity(request);

        // Encodage du mot de passe
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Association des relations
        user.setRole(role);
        user.setDepartment(department);

        User saved = userRepository.save(user);
        log.info("Utilisateur créé avec l'ID : {}", saved.getId());

        return userMapper.toResponse(saved);
    }

    /**
     * Récupère un utilisateur par son identifiant.
     *
     * @param id Identifiant de l'utilisateur
     * @return DTO de réponse
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas ou est supprimé
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Recherche de l'utilisateur par ID : {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));

        if (user.getDeleted()) {
            throw new ResourceNotFoundException("Utilisateur", id);
        }

        return userMapper.toResponse(user);
    }

    /**
     * Récupère un utilisateur par son email.
     *
     * @param email Adresse email
     * @return DTO de réponse
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas ou est supprimé
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.debug("Recherche de l'utilisateur par email : {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", email));

        if (user.getDeleted()) {
            throw new ResourceNotFoundException("Utilisateur", "email", email);
        }

        return userMapper.toResponse(user);
    }

    /**
     * Récupère tous les utilisateurs non supprimés.
     *
     * @return Liste des DTO de réponse
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Récupération de tous les utilisateurs actifs");

        List<User> users = userRepository.findByDeletedFalse();
        return users.stream()
                .map(userMapper::toResponse)
                .toList();
    }

    /**
     * Met à jour complètement un utilisateur existant (PUT).
     *
     * @param id      Identifiant de l'utilisateur
     * @param request DTO de mise à jour complète
     * @return DTO de réponse mis à jour
     * @throws ResourceNotFoundException  si l'utilisateur, le rôle ou le département n'existe pas
     * @throws DuplicateResourceException si le nouvel email est déjà utilisé par un autre utilisateur
     */
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Mise à jour complète de l'utilisateur ID : {}", id);

        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));

        if (existing.getDeleted()) {
            throw new ResourceNotFoundException("Utilisateur", id);
        }

        // Vérification de l'unicité de l'email (si modifié)
        if (!existing.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Utilisateur", "email", request.getEmail());
        }

        // Récupération du rôle et du département
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Rôle", request.getRoleId()));
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Département", request.getDepartmentId()));

        // Application des modifications (le mot de passe est ignoré)
        userMapper.updateEntity(request, existing);

        // Mise à jour des relations
        existing.setRole(role);
        existing.setDepartment(department);

        User updated = userRepository.save(existing);
        log.info("Utilisateur ID {} mis à jour avec succès", id);

        return userMapper.toResponse(updated);
    }

    /**
     * Met à jour partiellement un utilisateur existant (PATCH).
     *
     * @param id      Identifiant de l'utilisateur
     * @param request DTO de mise à jour partielle
     * @return DTO de réponse mis à jour
     * @throws ResourceNotFoundException  si l'utilisateur, le rôle ou le département n'existe pas
     * @throws DuplicateResourceException si le nouvel email est déjà utilisé par un autre utilisateur
     */
    @Transactional
    public UserResponse patchUser(Long id, PatchUserRequest request) {
        log.info("Mise à jour partielle de l'utilisateur ID : {}", id);

        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));

        if (existing.getDeleted()) {
            throw new ResourceNotFoundException("Utilisateur", id);
        }

        // Vérification de l'unicité de l'email (si fourni et modifié)
        if (request.getEmail() != null
                && !existing.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Utilisateur", "email", request.getEmail());
        }

        // Récupération du rôle et du département si les IDs sont fournis
        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Rôle", request.getRoleId()));
            existing.setRole(role);
        }
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Département", request.getDepartmentId()));
            existing.setDepartment(department);
        }

        // Application partielle (seuls les champs non-nuls sont copiés)
        userMapper.patchEntity(request, existing);

        User updated = userRepository.save(existing);
        log.info("Utilisateur ID {} partiellement mis à jour", id);

        return userMapper.toResponse(updated);
    }

    /**
     * Supprime logiquement un utilisateur (marque deleted = true).
     *
     * @param id Identifiant de l'utilisateur
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Suppression logique de l'utilisateur ID : {}", id);

        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));

        if (existing.getDeleted()) {
            log.warn("Tentative de suppression d'un utilisateur déjà supprimé : {}", id);
            return;
        }

        existing.setDeleted(true);
        userRepository.save(existing);
        log.info("Utilisateur ID {} marqué comme supprimé", id);
    }
}