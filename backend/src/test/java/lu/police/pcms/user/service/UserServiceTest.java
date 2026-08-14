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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // ========== DONNÉES DE TEST ==========

    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "john.doe@pcms.lu";
    private static final String PASSWORD_RAW = "password123";
    private static final String PASSWORD_ENCODED = "encoded_password";

    private Role mockRole(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return role;
    }

    private Department mockDepartment(Long id, String code, String name) {
        Department dept = new Department();
        dept.setId(id);
        dept.setCode(code);
        dept.setName(name);
        return dept;
    }

    private User mockUser(boolean deleted) {
        User user = new User();
        user.setId(USER_ID);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail(USER_EMAIL);
        user.setPassword(PASSWORD_ENCODED);
        user.setEnabled(true);
        user.setDeleted(deleted);
        user.setCreatedAt(Instant.now());
        return user;
    }

    /**
     * Génère un UserResponse à partir d'un User.
     * ✅ Inclut désormais les informations du rôle et du département.
     */
    private UserResponse mockResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setEnabled(user.getEnabled());
        response.setDeleted(user.getDeleted());

        if (user.getRole() != null) {
            response.setRoleId(user.getRole().getId());
            response.setRoleName(user.getRole().getName());
        }
        if (user.getDepartment() != null) {
            response.setDepartmentId(user.getDepartment().getId());
            response.setDepartmentCode(user.getDepartment().getCode());
            response.setDepartmentName(user.getDepartment().getName());
        }
        return response;
    }

    // ========== TESTS ==========

    @Test
    @DisplayName("Création d'un utilisateur avec succès")
    void shouldCreateUserSuccessfully() {
        CreateUserRequest request = new CreateUserRequest(
                "John", "Doe", USER_EMAIL, PASSWORD_RAW, true, 1L, 1L
        );
        Role role = mockRole(1L, "ROLE_USER");
        Department department = mockDepartment(1L, "IT", "Informatique");
        User entity = new User();
        User saved = mockUser(false);
        UserResponse expected = mockResponse(saved);

        when(userRepository.existsByEmail(USER_EMAIL)).thenReturn(false);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(userMapper.toEntity(request)).thenReturn(entity);
        when(passwordEncoder.encode(PASSWORD_RAW)).thenReturn(PASSWORD_ENCODED);
        when(userRepository.save(entity)).thenReturn(saved);
        when(userMapper.toResponse(saved)).thenReturn(expected);

        UserResponse actual = userService.createUser(request);

        assertThat(actual).isEqualTo(expected);
        assertThat(entity.getPassword()).isEqualTo(PASSWORD_ENCODED);
        assertThat(entity.getRole()).isEqualTo(role);
        assertThat(entity.getDepartment()).isEqualTo(department);
        verify(userRepository).save(entity);
    }

    @Test
    @DisplayName("Création avec email existant → exception")
    void shouldThrowExceptionWhenEmailExists() {
        CreateUserRequest request = new CreateUserRequest(
                "John", "Doe", USER_EMAIL, PASSWORD_RAW, true, 1L, 1L
        );
        when(userRepository.existsByEmail(USER_EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Création avec rôle inexistant → exception")
    void shouldThrowExceptionWhenRoleNotFound() {
        CreateUserRequest request = new CreateUserRequest(
                "John", "Doe", USER_EMAIL, PASSWORD_RAW, true, 1L, 1L
        );
        when(userRepository.existsByEmail(USER_EMAIL)).thenReturn(false);
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Rôle");
    }

    @Test
    @DisplayName("Récupération d'un utilisateur par ID avec succès")
    void shouldGetUserByIdSuccessfully() {
        User user = mockUser(false);
        UserResponse expected = mockResponse(user);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expected);

        UserResponse actual = userService.getUserById(USER_ID);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Récupération d'un utilisateur supprimé → exception")
    void shouldThrowExceptionWhenUserDeleted() {
        User user = mockUser(true);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.getUserById(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("introuvable");
    }

    @Test
    @DisplayName("Récupération par email avec succès")
    void shouldGetUserByEmailSuccessfully() {
        User user = mockUser(false);
        UserResponse expected = mockResponse(user);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expected);

        UserResponse actual = userService.getUserByEmail(USER_EMAIL);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Récupération de tous les utilisateurs actifs")
    void shouldGetAllUsers() {
        User user1 = mockUser(false);
        User user2 = mockUser(false);
        user2.setId(2L);
        user2.setEmail("alice@pcms.lu");
        List<User> list = List.of(user1, user2);

        when(userRepository.findByDeletedFalse()).thenReturn(list);
        when(userMapper.toResponse(any(User.class))).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        List<UserResponse> responses = userService.getAllUsers();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(UserResponse::getEmail)
                .containsExactlyInAnyOrder(USER_EMAIL, "alice@pcms.lu");
    }

    @Test
    @DisplayName("Mise à jour complète (PUT) avec succès")
    void shouldUpdateUserSuccessfully() {
        User existing = mockUser(false);
        UpdateUserRequest request = new UpdateUserRequest(
                "Jane", "Smith", "jane.smith@pcms.lu", true, 2L, 2L
        );
        Role newRole = mockRole(2L, "ROLE_ADMIN");
        Department newDept = mockDepartment(2L, "HR", "Ressources Humaines");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("jane.smith@pcms.lu")).thenReturn(false);
        when(roleRepository.findById(2L)).thenReturn(Optional.of(newRole));
        when(departmentRepository.findById(2L)).thenReturn(Optional.of(newDept));

        doAnswer(inv -> {
            User u = inv.getArgument(1);
            u.setFirstName("Jane");
            u.setLastName("Smith");
            u.setEmail("jane.smith@pcms.lu");
            u.setEnabled(true);
            return null;
        }).when(userMapper).updateEntity(request, existing);

        when(userRepository.save(existing)).thenReturn(existing);
        when(userMapper.toResponse(existing)).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        UserResponse actual = userService.updateUser(USER_ID, request);

        assertThat(actual.getFirstName()).isEqualTo("Jane");
        assertThat(actual.getLastName()).isEqualTo("Smith");
        assertThat(actual.getEmail()).isEqualTo("jane.smith@pcms.lu");
        assertThat(existing.getRole()).isEqualTo(newRole);
        assertThat(existing.getDepartment()).isEqualTo(newDept);
        verify(userRepository).save(existing);
    }

    @Test
    @DisplayName("PUT avec email déjà utilisé par un autre → exception")
    void shouldThrowExceptionWhenUpdatingToExistingEmail() {
        User existing = mockUser(false);
        UpdateUserRequest request = new UpdateUserRequest(
                "Jane", "Smith", "other@pcms.lu", true, 1L, 1L
        );

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("other@pcms.lu")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(USER_ID, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email");
    }

    @Test
    @DisplayName("Mise à jour partielle (PATCH) avec succès")
    void shouldPatchUserSuccessfully() {
        User existing = mockUser(false);
        PatchUserRequest request = new PatchUserRequest(
                "Jane", null, "jane@pcms.lu", null, null, null
        );

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("jane@pcms.lu")).thenReturn(false);

        doAnswer(inv -> {
            User u = inv.getArgument(1);
            u.setFirstName("Jane");
            u.setEmail("jane@pcms.lu");
            return null;
        }).when(userMapper).patchEntity(request, existing);

        when(userRepository.save(existing)).thenReturn(existing);
        when(userMapper.toResponse(existing)).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        UserResponse actual = userService.patchUser(USER_ID, request);

        assertThat(actual.getFirstName()).isEqualTo("Jane");
        assertThat(actual.getEmail()).isEqualTo("jane@pcms.lu");
        assertThat(actual.getLastName()).isEqualTo("Doe");
        verify(userRepository).save(existing);
    }

    @Test
    @DisplayName("PATCH avec nouveau rôle et département")
    void shouldPatchUserWithNewRoleAndDepartment() {
        // Arrange
        User existing = mockUser(false);
        PatchUserRequest request = new PatchUserRequest(null, null, null, null, 2L, 2L);
        Role newRole = mockRole(2L, "ROLE_ADMIN");
        Department newDept = mockDepartment(2L, "HR", "Ressources Humaines");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existing));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(newRole));
        when(departmentRepository.findById(2L)).thenReturn(Optional.of(newDept));

        // Le service définit directement les relations sur existing
        // Le mapper patchEntity n'est pas utilisé pour les relations, on le laisse faire son travail (sans effet)
        when(userRepository.save(existing)).thenReturn(existing);
        when(userMapper.toResponse(existing)).thenAnswer(inv -> mockResponse(inv.getArgument(0)));

        // Act
        UserResponse actual = userService.patchUser(USER_ID, request);

        // Assert
        // 1. Vérifier que les repositories ont bien été interrogés
        verify(roleRepository).findById(2L);
        verify(departmentRepository).findById(2L);
        verify(userRepository).save(existing);

        // 2. Vérifier la réponse (les champs doivent être renseignés)
        assertThat(actual.getRoleId()).isEqualTo(2L);
        assertThat(actual.getRoleName()).isEqualTo("ROLE_ADMIN");
        assertThat(actual.getDepartmentId()).isEqualTo(2L);
        assertThat(actual.getDepartmentCode()).isEqualTo("HR");
        assertThat(actual.getDepartmentName()).isEqualTo("Ressources Humaines");
    }

    @Test
    @DisplayName("Suppression logique avec succès")
    void shouldDeleteUserSuccessfully() {
        User existing = mockUser(false);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existing));

        userService.deleteUser(USER_ID);

        assertThat(existing.getDeleted()).isTrue();
        verify(userRepository).save(existing);
    }

    @Test
    @DisplayName("Suppression d'un utilisateur déjà supprimé ne fait rien")
    void shouldDoNothingWhenDeletingAlreadyDeleted() {
        User existing = mockUser(true);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existing));

        userService.deleteUser(USER_ID);

        verify(userRepository, never()).save(existing);
    }
}