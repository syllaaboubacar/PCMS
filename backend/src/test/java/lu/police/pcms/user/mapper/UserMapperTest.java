package lu.police.pcms.user.mapper;

import lu.police.pcms.department.entity.Department;
import lu.police.pcms.role.entity.Role;
import lu.police.pcms.user.dto.CreateUserRequest;
import lu.police.pcms.user.dto.PatchUserRequest;
import lu.police.pcms.user.dto.UpdateUserRequest;
import lu.police.pcms.user.dto.UserResponse;
import lu.police.pcms.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires du mapper {@link UserMapper}.
 *
 * <p>
 * Ces tests vérifient que la conversion entre les DTO et l'entité
 * fonctionne correctement, y compris pour les cas de mise à jour
 * partielle avec {@code null}.
 * </p>
 */
class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    @DisplayName("Conversion CreateUserRequest → User")
    void shouldMapCreateRequestToEntity() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
                "John", "Doe", "john.doe@pcms.lu",
                "password123", true, 1L, 2L
        );

        // Act
        User entity = mapper.toEntity(request);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getFirstName()).isEqualTo("John");
        assertThat(entity.getLastName()).isEqualTo("Doe");
        assertThat(entity.getEmail()).isEqualTo("john.doe@pcms.lu");
        assertThat(entity.getPassword()).isEqualTo("password123");
        assertThat(entity.getEnabled()).isTrue();
        assertThat(entity.getId()).isNull();       // ignoré
        assertThat(entity.getRole()).isNull();     // ignoré
        assertThat(entity.getDepartment()).isNull(); // ignoré
    }

    @Test
    @DisplayName("Conversion User → UserResponse avec relations")
    void shouldMapEntityToResponse() {
        // Arrange
        Role role = new Role();
        role.setId(1L);
        role.setName("ROLE_ADMIN");

        Department department = new Department();
        department.setId(2L);
        department.setCode("IT");
        department.setName("Informatique");

        User entity = new User();
        entity.setId(10L);
        entity.setFirstName("Alice");
        entity.setLastName("Smith");
        entity.setEmail("alice@pcms.lu");
        entity.setEnabled(true);
        entity.setRole(role);
        entity.setDepartment(department);

        // Act
        UserResponse response = mapper.toResponse(entity);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getFirstName()).isEqualTo("Alice");
        assertThat(response.getLastName()).isEqualTo("Smith");
        assertThat(response.getEmail()).isEqualTo("alice@pcms.lu");
        assertThat(response.getEnabled()).isTrue();
        assertThat(response.getRoleId()).isEqualTo(1L);
        assertThat(response.getRoleName()).isEqualTo("ROLE_ADMIN");
        assertThat(response.getDepartmentId()).isEqualTo(2L);
        assertThat(response.getDepartmentCode()).isEqualTo("IT");
        assertThat(response.getDepartmentName()).isEqualTo("Informatique");
    }

    @Test
    @DisplayName("Mise à jour complète UpdateUserRequest → User (PUT)")
    void shouldUpdateEntity() {
        // Arrange
        User entity = new User();
        entity.setId(1L);
        entity.setFirstName("Old");
        entity.setLastName("Name");
        entity.setEmail("old@pcms.lu");
        entity.setEnabled(false);

        UpdateUserRequest request = new UpdateUserRequest(
                "New", "Name", "new@pcms.lu", true, 2L, 3L
        );

        // Act
        mapper.updateEntity(request, entity);

        // Assert
        assertThat(entity.getFirstName()).isEqualTo("New");
        assertThat(entity.getLastName()).isEqualTo("Name");
        assertThat(entity.getEmail()).isEqualTo("new@pcms.lu");
        assertThat(entity.getEnabled()).isTrue();
        assertThat(entity.getId()).isEqualTo(1L); // inchangé
        assertThat(entity.getPassword()).isNull(); // non modifié
    }

    @Test
    @DisplayName("Mise à jour partielle PatchUserRequest → User (PATCH)")
    void shouldPatchEntity() {
        // Arrange
        User entity = new User();
        entity.setId(1L);
        entity.setFirstName("Original");
        entity.setLastName("LastName");
        entity.setEmail("original@pcms.lu");
        entity.setEnabled(true);

        PatchUserRequest request = new PatchUserRequest(
                "Updated", null, null, null, null, null
        ); // seul le prénom change

        // Act
        mapper.patchEntity(request, entity);

        // Assert
        assertThat(entity.getFirstName()).isEqualTo("Updated");
        assertThat(entity.getLastName()).isEqualTo("LastName"); // inchangé
        assertThat(entity.getEmail()).isEqualTo("original@pcms.lu"); // inchangé
        assertThat(entity.getEnabled()).isTrue(); // inchangé
        assertThat(entity.getId()).isEqualTo(1L); // inchangé
    }

    @Test
    @DisplayName("Patch avec tous les champs à null ne modifie rien")
    void shouldPatchWithNullsDoNothing() {
        // Arrange
        User entity = new User();
        entity.setId(1L);
        entity.setFirstName("Original");
        entity.setLastName("Name");
        entity.setEmail("email@pcms.lu");
        entity.setEnabled(true);

        PatchUserRequest request = new PatchUserRequest(null, null, null, null, null, null);

        // Act
        mapper.patchEntity(request, entity);

        // Assert
        assertThat(entity.getFirstName()).isEqualTo("Original");
        assertThat(entity.getLastName()).isEqualTo("Name");
        assertThat(entity.getEmail()).isEqualTo("email@pcms.lu");
        assertThat(entity.getEnabled()).isTrue();
    }
}