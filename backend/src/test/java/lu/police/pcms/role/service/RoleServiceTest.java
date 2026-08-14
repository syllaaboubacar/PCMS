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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service RoleService")
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleService roleService;

    // ========== DONNÉES DE TEST ==========

    private static final Long ROLE_ID = 1L;
    private static final String ROLE_NAME = "ROLE_ADMIN";
    private static final String ROLE_DESC = "Administrateur";
    private static final String NEW_NAME = "ROLE_SUPER_ADMIN";

    private Role mockRole(boolean deleted) {
        Role role = new Role();
        role.setId(ROLE_ID);
        role.setName(ROLE_NAME);
        role.setDescription(ROLE_DESC);
        role.setDeleted(deleted);
        role.setCreatedAt(Instant.now());
        return role;
    }

    private RoleResponse mockResponse(Role role) {
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setName(role.getName());
        response.setDescription(role.getDescription());
        response.setDeleted(role.getDeleted());
        return response;
    }

    // ========== TESTS ==========

    @Test
    @DisplayName("Création d'un rôle avec succès")
    void shouldCreateRoleSuccessfully() {
        // Arrange
        CreateRoleRequest request = new CreateRoleRequest(ROLE_NAME, ROLE_DESC);
        Role roleToSave = new Role();
        Role savedRole = mockRole(false);
        RoleResponse expectedResponse = mockResponse(savedRole);

        when(roleRepository.existsByName(ROLE_NAME)).thenReturn(false);
        when(roleMapper.toRole(request)).thenReturn(roleToSave);
        when(roleRepository.save(roleToSave)).thenReturn(savedRole);
        when(roleMapper.toResponse(savedRole)).thenReturn(expectedResponse);

        // Act
        RoleResponse actual = roleService.createRole(request);

        // Assert
        assertThat(actual).isEqualTo(expectedResponse);
        verify(roleRepository).existsByName(ROLE_NAME);
        verify(roleRepository).save(roleToSave);
    }

    @Test
    @DisplayName("Création d'un rôle avec nom existant → exception")
    void shouldThrowExceptionWhenNameAlreadyExists() {
        // Arrange
        CreateRoleRequest request = new CreateRoleRequest(ROLE_NAME, ROLE_DESC);
        when(roleRepository.existsByName(ROLE_NAME)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> roleService.createRole(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("existe déjà");

        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Récupération d'un rôle existant")
    void shouldGetRoleByIdSuccessfully() {
        // Arrange
        Role role = mockRole(false);
        RoleResponse expected = mockResponse(role);
        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(role));
        when(roleMapper.toResponse(role)).thenReturn(expected);

        // Act
        RoleResponse actual = roleService.getRoleById(ROLE_ID);

        // Assert
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Récupération d'un rôle inexistant → exception")
    void shouldThrowExceptionWhenRoleNotFound() {
        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.getRoleById(ROLE_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("introuvable");
    }

    @Test
    @DisplayName("Récupération d'un rôle supprimé → exception")
    void shouldThrowExceptionWhenRoleIsDeleted() {
        Role role = mockRole(true);
        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(role));

        assertThatThrownBy(() -> roleService.getRoleById(ROLE_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("introuvable");
    }

    @Test
    @DisplayName("Récupération de tous les rôles actifs")
    void shouldGetAllActiveRoles() {
        Role role1 = mockRole(false);
        Role role2 = mockRole(false);
        role2.setId(2L);
        role2.setName("ROLE_USER");
        List<Role> roles = List.of(role1, role2);

        when(roleRepository.findByDeletedFalse()).thenReturn(roles);
        when(roleMapper.toResponse(any(Role.class))).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        List<RoleResponse> responses = roleService.getAllRoles();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(RoleResponse::getName)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    @DisplayName("Mise à jour complète (PUT) avec succès")
    void shouldUpdateRoleSuccessfully() {
        // Arrange
        Role existing = mockRole(false);
        UpdateRoleRequest request = new UpdateRoleRequest(NEW_NAME, "Nouvelle description");

        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(existing));
        when(roleRepository.existsByName(NEW_NAME)).thenReturn(false);
        doAnswer(inv -> {
            Role r = inv.getArgument(1);
            r.setName(request.getName());
            r.setDescription(request.getDescription());
            return null;
        }).when(roleMapper).updateRole(request, existing);

        when(roleRepository.save(existing)).thenReturn(existing);

        // 🔥 Correction : utiliser thenAnswer pour obtenir la réponse dynamique
        when(roleMapper.toResponse(existing)).thenAnswer(inv -> {
            Role r = inv.getArgument(0);
            return mockResponse(r);
        });

        // Act
        RoleResponse actual = roleService.updateRole(ROLE_ID, request);

        // Assert
        assertThat(actual.getName()).isEqualTo(NEW_NAME);
        verify(roleRepository).save(existing);
    }

    @Test
    @DisplayName("PUT avec nom déjà utilisé par un autre rôle → exception")
    void shouldThrowExceptionWhenUpdatingToExistingName() {
        Role existing = mockRole(false);
        UpdateRoleRequest request = new UpdateRoleRequest(NEW_NAME, "Desc");

        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(existing));
        when(roleRepository.existsByName(NEW_NAME)).thenReturn(true);

        assertThatThrownBy(() -> roleService.updateRole(ROLE_ID, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("existe déjà");
    }

    @Test
    @DisplayName("Mise à jour partielle (PATCH) avec succès")
    void shouldPatchRoleSuccessfully() {
        // Arrange
        Role existing = mockRole(false);
        PatchRoleRequest request = new PatchRoleRequest(NEW_NAME, null); // seul le nom change

        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(existing));
        when(roleRepository.existsByName(NEW_NAME)).thenReturn(false);
        doAnswer(inv -> {
            Role r = inv.getArgument(1);
            r.setName(NEW_NAME);
            return null;
        }).when(roleMapper).patchRole(request, existing);

        when(roleRepository.save(existing)).thenReturn(existing);

        // 🔥 Correction : utiliser thenAnswer pour obtenir la réponse dynamique
        when(roleMapper.toResponse(existing)).thenAnswer(inv -> {
            Role r = inv.getArgument(0);
            return mockResponse(r);
        });

        // Act
        RoleResponse actual = roleService.patchRole(ROLE_ID, request);

        // Assert
        assertThat(actual.getName()).isEqualTo(NEW_NAME);
        verify(roleRepository).save(existing);
    }

    @Test
    @DisplayName("Suppression logique avec succès")
    void shouldDeleteRoleSuccessfully() {
        Role existing = mockRole(false);
        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(existing));

        roleService.deleteRole(ROLE_ID);

        assertThat(existing.getDeleted()).isTrue();
        verify(roleRepository).save(existing);
    }

    @Test
    @DisplayName("Suppression d'un rôle déjà supprimé ne fait rien")
    void shouldDoNothingWhenDeletingAlreadyDeletedRole() {
        Role existing = mockRole(true);
        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(existing));

        roleService.deleteRole(ROLE_ID);

        verify(roleRepository, never()).save(existing); // car on a un return avant
        // Dans l'implémentation, on a un return si déjà supprimé, donc save n'est pas appelé
        // Mais on peut aussi sauvegarder, peu importe. On vérifie qu'aucune exception n'est levée.
    }
}